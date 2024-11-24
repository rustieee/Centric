package com.finals.centric;

import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.finals.centric.databinding.FragmentBookingBinding;
import com.finals.centric.databinding.FragmentPaymentBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link paymentFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class paymentFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private FirebaseUser user;

    public paymentFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment paymentFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static paymentFragment newInstance(String param1, String param2) {
        paymentFragment fragment = new paymentFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    FragmentPaymentBinding binding;
    FirebaseAuth auth;
    FirebaseFirestore db;
    TextView profileChangeName;
    TextView payRoomTime;
    TextView payRoomPrice;
    CardView profileEditCardLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPaymentBinding.inflate(getLayoutInflater(), container, false);
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        user = auth.getCurrentUser();

        // Initialize UI elements
        profileChangeName = binding.profileChangeName;
        payRoomTime = binding.payRoomTime;
        payRoomPrice = binding.payRoomPrice;
        profileEditCardLayout = binding.profileEditCardLayout; // Initialize the CardView
        profileEditCardLayout.setVisibility(View.GONE);


        // Fetch the billing data from Firestore
        fetchBillData();

        setButtonAnimation(binding.cancelbtn, this::cancelBookingData);
        setButtonAnimation(binding.changebtn, () -> replaceFragment(new bookingFragment()));
        setButtonAnimation(binding.paybtn, () -> replaceFragment(new paymentBillFragment()));
        return binding.getRoot();
    }

    private void cancelBookingData() {
        // Fetch the user's booking
        db.collection("booking")
                .whereEqualTo("user_id", user.getUid())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot bookingDoc : task.getResult()) {
                            String bookingId = bookingDoc.getId();
                            String roomId = bookingDoc.getString("roomId");

                            // Delete the booking document
                            db.collection("booking").document(bookingId)
                                    .delete()
                                    .addOnSuccessListener(aVoid -> {
                                        // After deleting booking, delete the bill document
                                        deleteBillData(roomId);
                                    })
                                    .addOnFailureListener(e -> {
                                        // Handle failure to delete booking
                                        showErrorMessage("Failed to cancel booking: " + e.getMessage());
                                    });
                        }
                    } else {
                        showErrorMessage("No active bookings found.");
                    }
                })
                .addOnFailureListener(e -> showErrorMessage("Error fetching booking: " + e.getMessage()));
    }

    private void deleteBillData(String roomId) {
        // Fetch and delete the user's bill
        db.collection("bills")
                .whereEqualTo("userId", user.getUid())
                .whereEqualTo("roomId", roomId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot billDoc : task.getResult()) {
                            String billId = billDoc.getId();

                            // Delete the bill document
                            db.collection("bills").document(billId)
                                    .delete()
                                    .addOnSuccessListener(aVoid -> {
                                        // Hide the payment card layout
                                        profileEditCardLayout.setVisibility(View.GONE);
                                        showSuccessMessage("Booking and associated data canceled successfully.");
                                    })
                                    .addOnFailureListener(e -> {
                                        // Handle failure to delete bill
                                        showErrorMessage("Failed to delete bill: " + e.getMessage());
                                    });
                        }
                    } else {
                        showErrorMessage("No bill found to delete.");
                    }
                })
                .addOnFailureListener(e -> showErrorMessage("Error fetching bill: " + e.getMessage()));
    }

    private void showErrorMessage(String message) {
        // Display error message
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void showSuccessMessage(String message) {
        // Display success message
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void fetchBillData() {
        // Fetch bill related to the current user
        db.collection("bills")
                .whereEqualTo("userId", user.getUid())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        boolean hasPendingPayment = false;

                        // Iterate through documents to find any pending payment
                        for (QueryDocumentSnapshot billDoc : task.getResult()) {
                            String paymentStatus = billDoc.getString("paymentStatus");

                            // Check if the paymentStatus is "pending"
                            if ("pending".equalsIgnoreCase(paymentStatus)) {
                                hasPendingPayment = true;
                                break; // Stop checking further as we found a pending payment
                            }
                        }

                        // Set visibility based on pending payment status
                        if (hasPendingPayment) {
                            profileEditCardLayout.setVisibility(View.VISIBLE);
                        } else {
                            profileEditCardLayout.setVisibility(View.GONE);
                        }

                        // Update room details if there's a pending payment
                        if (hasPendingPayment) {
                            for (QueryDocumentSnapshot billDoc : task.getResult()) {
                                String roomId = billDoc.getString("roomId");
                                String checkInDate = billDoc.getString("checkinDate");
                                String checkOutDate = billDoc.getString("checkoutDate");
                                updateRoomDetails(roomId, checkInDate, checkOutDate);
                            }
                        }
                    } else {
                        // Task failed or no results, hide the layout
                        profileEditCardLayout.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> {
                    // In case of error, also hide the layout
                    profileEditCardLayout.setVisibility(View.GONE);
                });
    }


    private void updateRoomDetails(String roomId, String checkInDate, String checkOutDate) {
        db.collection("rooms").document(roomId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot roomDoc = task.getResult();
                        String roomName = roomDoc.getString("roomName");
                        profileChangeName.setText(roomName);

                        payRoomTime.setText(String.format("%s ~ %s", checkInDate, checkOutDate));

                        int price = roomDoc.getLong("price").intValue(); // Adjust this if price is stored differently
                        payRoomPrice.setText(String.format("₱%,d", price));
                    }
                });
    }

    private void setButtonAnimation(View button, Runnable onClickAction) {
        button.setOnClickListener(v -> {
            v.animate()
                    .scaleX(0.9f)
                    .scaleY(0.9f)
                    .setDuration(100)
                    .withEndAction(() -> {
                        v.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(100)
                                .withEndAction(onClickAction) // Execute the passed action after scaling back
                                .start();
                    })
                    .start();
        });
    }

    private void replaceFragment(Fragment fragment) {
        getParentFragmentManager().beginTransaction()
                .replace(R.id.mainframe, fragment)
                .addToBackStack(null)  // Add to back stack to allow "back" navigation
                .commit();
    }
}