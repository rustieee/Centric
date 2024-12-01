package com.finals.centric;

import static com.finals.centric.RoomInfo.roomImagesAvailableReserved;
import static com.finals.centric.RoomInfo.roomImagesOccupiedYou;
import static com.finals.centric.RoomInfo.roomImagesReservedYou;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.finals.centric.databinding.FragmentHomeBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link homeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class homeFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private FirebaseUser user;
    private RoomInfo[] roomData = new RoomInfo[4];
    private String username;

    public homeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment homeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static homeFragment newInstance(String param1, String param2) {
        homeFragment fragment = new homeFragment();
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

    FragmentHomeBinding binding;
    FirebaseAuth auth;
    FirebaseFirestore db;
    TextView profileChangeName;
    TextView payRoomTime;
    TextView payRoomPrice;
    CardView profileEditCardLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(getLayoutInflater(), container, false);
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        user = auth.getCurrentUser();
        profileChangeName = binding.profileChangeName;
        payRoomTime = binding.payRoomTime;
        payRoomPrice = binding.payRoomPrice;
        profileEditCardLayout = binding.profileEditCardLayout; // Initialize the CardView
        profileEditCardLayout.setVisibility(View.GONE);

        fetchBillData();
        fetchRoomData();

        db.collection("users").document(user.getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    username = documentSnapshot.getString("username");
                    binding.welcomemsg.setText("Welcome, "+username+"!");
                });

        // Set click listeners, passing the appropriate fragment
        binding.room1btn.setOnClickListener(v -> roomanimateButtonAndSwitchFragment(v, 0));
        binding.room2btn.setOnClickListener(v -> roomanimateButtonAndSwitchFragment(v, 1));
        binding.room3btn.setOnClickListener(v -> roomanimateButtonAndSwitchFragment(v, 2));
        binding.room4btn.setOnClickListener(v -> roomanimateButtonAndSwitchFragment(v, 3));

        setButtonAnimation(binding.changebtn, () -> replaceFragment(new bookingFragment()));
        setButtonAnimation(binding.cancelbtn, this::cancelBookingData);
        setButtonAnimation(binding.paybtn, () -> {
            replaceFragment(new paymentFragment());
        });
        setButtonAnimation(binding.directionbtn, () -> {
            // Start the maps intent for direction button
            String uri = "https://maps.app.goo.gl/N463hsxswuAqyJyQ6";
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            startActivity(intent);
        });

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
                                        Toast.makeText(requireContext(), "Booking canceled", Toast.LENGTH_SHORT).show();
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
                .whereEqualTo("user_id", user.getUid())
                .whereEqualTo("room_id", roomId)
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
        db.collection("bills")
                .whereEqualTo("user_id", user.getUid())  // Changed from userId to user_id
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        boolean hasPendingPayment = false;

                        for (QueryDocumentSnapshot billDoc : task.getResult()) {
                            String paymentStatus = billDoc.getString("payment_status");  // Changed from paymentStatus

                            if ("Pending".equals(paymentStatus)) {  // Match exact case
                                hasPendingPayment = true;
                                String roomId = billDoc.getString("room_id");  // Changed from roomId
                                String checkInDate = billDoc.getString("check_in_date");  // Changed from checkinDate
                                String checkOutDate = billDoc.getString("check_out_date");  // Changed from checkoutDate
                                updateRoomDetails(roomId, checkInDate, checkOutDate);
                                break;
                            }
                        }

                        profileEditCardLayout.setVisibility(hasPendingPayment ? View.VISIBLE : View.GONE);
                    }
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

    private void fetchRoomData() {
        db.collection("rooms").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                int index = 0;
                for (QueryDocumentSnapshot document : task.getResult()) {
                    RoomInfo roomInfo = document.toObject(RoomInfo.class);
                    roomInfo.setRoomId(document.getId());  // Set the roomId to the document ID
                    roomData[index] = roomInfo;
                    checkRoomBookingStatus(roomInfo.getRoomId(), roomInfo, index);  // Now pass roomId correctly
                    index++;
                    if (index >= 4) break; // Assume you have a fixed 4 rooms to display
                }
            }
        });
    }


    private void checkRoomBookingStatus(String roomId, RoomInfo roomInfo, int roomIndex) {
        db.collection("booking")
                .whereEqualTo("roomId", roomId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        boolean isReservedByCurrentUser = false;
                        boolean isOccupiedByCurrentUser = false;
                        boolean isReservedByOtherUser = false;
                        String checkinDate = null;
                        String checkoutDate = null;

                        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
                        Date currentDate = new Date();

                        for (QueryDocumentSnapshot bookingDoc : task.getResult()) {
                            String status = bookingDoc.getString("status");
                            String userId = bookingDoc.getString("user_id");
                            String bookingCheckIn = bookingDoc.getString("check_in_date");
                            String bookingCheckOut = bookingDoc.getString("check_out_date");

                            if (user.getUid().equals(userId)) {
                                // Current user's bookings show regardless of date
                                if ("RESERVED".equals(status)) {
                                    isReservedByCurrentUser = true;
                                    checkinDate = bookingCheckIn;
                                    checkoutDate = bookingCheckOut;
                                } else if ("OCCUPIED".equals(status)) {
                                    isOccupiedByCurrentUser = true;
                                    checkinDate = bookingCheckIn;
                                    checkoutDate = bookingCheckOut;
                                }
                            } else {
                                // Other users' bookings only show when within date range
                                try {
                                    Date checkIn = sdf.parse(bookingCheckIn);
                                    Date checkOut = sdf.parse(bookingCheckOut);
                                    if (currentDate.compareTo(checkIn) >= 0 && currentDate.compareTo(checkOut) <= 0) {
                                        isReservedByOtherUser = true;
                                    }
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        roomInfo.setCheckin(checkinDate);
                        roomInfo.setCheckout(checkoutDate);

                        if (isOccupiedByCurrentUser) {
                            roomInfo.setStatus("YOU ARE OCCUPYING THIS ROOM");
                        } else if (isReservedByCurrentUser) {
                            roomInfo.setStatus("YOU MADE A RESERVATION TO THIS ROOM");
                        } else if (isReservedByOtherUser) {
                            roomInfo.setStatus("RESERVED");
                        } else {
                            roomInfo.setStatus("AVAILABLE");
                        }

                        updateRoomInfo(roomIndex, roomInfo);
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

    private void roomanimateButtonAndSwitchFragment(View v, int roomIndex) {
        v.animate().translationX(10f).setDuration(100).withEndAction(() -> {
            v.animate().translationX(0f).setDuration(100).withEndAction(() -> {
                bookinginfoFragment bookingInfoFragment = bookinginfoFragment.newInstance(
                        roomData[roomIndex].getStatus(),
                        roomData[roomIndex].getCheckin(),
                        roomData[roomIndex].getCheckout(),
                        String.valueOf(roomData[roomIndex].getPrice()),
                        roomData[roomIndex].getRoomName(),
                        roomData[roomIndex].getRoomType(),
                        roomData[roomIndex].getDetails(),
                        roomData[roomIndex].getRoomId()
                );
                replaceFragment(bookingInfoFragment);
            }).start();
        }).start();
    }

    private void replaceFragment(Fragment fragment) {
        getParentFragmentManager().beginTransaction()
                .replace(R.id.mainframe, fragment)
                .addToBackStack(null)  // Add to back stack to allow "back" navigation
                .commit();
    }


    private void updateRoomInfo(int roomIndex, RoomInfo roomInfo) {
        // Update room information in the UI
        switch (roomIndex) {
            case 0:
                binding.room1.setText(roomInfo.getRoomName() + " - " + roomInfo.getRoomType());
                binding.price1.setText("₱"+ roomInfo.getPrice()+".00");
                binding.status1.setText(roomInfo.getStatus());
                updateRoomUI(roomIndex, roomInfo);
                break;
            case 1:
                binding.room2.setText(roomInfo.getRoomName() + " - " + roomInfo.getRoomType());
                binding.price2.setText("₱"+ roomInfo.getPrice()+".00");
                binding.status2.setText(roomInfo.getStatus());
                updateRoomUI(roomIndex, roomInfo);
                break;
            case 2:
                binding.room3.setText(roomInfo.getRoomName() + " - " + roomInfo.getRoomType());
                binding.price3.setText("₱"+ roomInfo.getPrice()+".00");
                binding.status3.setText(roomInfo.getStatus());
                updateRoomUI(roomIndex, roomInfo);
                break;
            case 3:
                binding.room4.setText(roomInfo.getRoomName() + " - " + roomInfo.getRoomType());
                binding.price4.setText("₱"+ roomInfo.getPrice()+".00");
                binding.status4.setText(roomInfo.getStatus());
                updateRoomUI(roomIndex, roomInfo);
                break;
        }
    }

    private void updateRoomUI(int roomIndex, RoomInfo roomInfo) {
        int roomImage = 0;
        String statusText = roomInfo.getStatus();
        int textColor = 0; // To hold the color for bookstatus

        // Use the index to handle visibility and set the appropriate image and date.
        View bookCheckBg = null;
        TextView bookCheckDate = null;

        // Determine which check date and background to use based on room index.
        switch (roomIndex) {
            case 0:
                bookCheckBg = binding.checkbg1;
                bookCheckDate = binding.checkdate1;
                break;
            case 1:
                bookCheckBg = binding.checkbg2;
                bookCheckDate = binding.checkdate2;
                break;
            case 2:
                bookCheckBg = binding.checkbg3;
                bookCheckDate = binding.checkdate3;
                break;
            case 3:
                bookCheckBg = binding.checkbg4;
                bookCheckDate = binding.checkdate4;
                break;
        }

        switch (statusText) {
            case "AVAILABLE":
                roomImage = roomImagesAvailableReserved[roomIndex];
                textColor = ContextCompat.getColor(requireContext(), R.color.green);
                bookCheckDate.setVisibility(View.GONE);
                bookCheckBg.setVisibility(View.GONE);
                break;
            case "RESERVED":
                roomImage = roomImagesAvailableReserved[roomIndex];
                textColor = ContextCompat.getColor(requireContext(), R.color.orange);
                bookCheckDate.setVisibility(View.GONE);
                bookCheckBg.setVisibility(View.GONE);
                break;
            case "MAINTENANCE":
                roomImage = roomImagesAvailableReserved[roomIndex];
                textColor = ContextCompat.getColor(requireContext(), R.color.orange);
                bookCheckDate.setVisibility(View.GONE);
                bookCheckBg.setVisibility(View.GONE);
                break;
            case "YOU MADE A RESERVATION TO THIS ROOM":
                roomImage = roomImagesReservedYou[roomIndex];
                textColor = ContextCompat.getColor(requireContext(), R.color.white);
                statusText = "YOU MADE A RESERVATION\nTO THIS ROOM";
                bookCheckDate.setVisibility(View.VISIBLE);
                bookCheckDate.setText(roomInfo.getCheckin());
                bookCheckBg.setVisibility(View.VISIBLE);
                bookCheckBg.setBackgroundResource(R.drawable.home_book2_checkin); // Set the appropriate background image
                break;
            case "YOU ARE OCCUPYING THIS ROOM":
                roomImage = roomImagesOccupiedYou[roomIndex];
                textColor = ContextCompat.getColor(requireContext(), R.color.white);
                statusText = "YOU ARE OCCUPYING\nTHIS ROOM";
                bookCheckDate.setVisibility(View.VISIBLE);
                bookCheckDate.setText(roomInfo.getCheckout());
                bookCheckBg.setVisibility(View.VISIBLE);
                bookCheckBg.setBackgroundResource(R.drawable.home_book3_checkout); // Set the appropriate background image
                break;
        }

        // Set text color based on the status
        int roomTextColor = (statusText.equals("AVAILABLE") || statusText.equals("RESERVED") || statusText.equals("MAINTENANCE")) ?
                ContextCompat.getColor(requireContext(), R.color.black) :
                ContextCompat.getColor(requireContext(), R.color.white);
        int priceTextColor = (statusText.equals("AVAILABLE") || statusText.equals("RESERVED") || statusText.equals("MAINTENANCE")) ?
                ContextCompat.getColor(requireContext(), R.color.blue) :
                ContextCompat.getColor(requireContext(), R.color.white);

        // Update UI dynamically based on roomIndex
        switch (roomIndex) {
            case 0:
                binding.room1.setTextColor(roomTextColor);
                binding.price1.setTextColor(priceTextColor);
                binding.rS1.setBackgroundColor(textColor);
                binding.room1btn.setBackgroundResource(roomImage);
                binding.status1.setTextColor(textColor);
                binding.status1.setText(statusText);
                break;
            case 1:
                binding.room2.setTextColor(roomTextColor);
                binding.price2.setTextColor(priceTextColor);
                binding.rS2.setBackgroundColor(textColor);
                binding.room2btn.setBackgroundResource(roomImage);
                binding.status2.setTextColor(textColor);
                binding.status2.setText(statusText);
                break;
            case 2:
                binding.room3.setTextColor(roomTextColor);
                binding.price3.setTextColor(priceTextColor);
                binding.rS3.setBackgroundColor(textColor);
                binding.room3btn.setBackgroundResource(roomImage);
                binding.status3.setTextColor(textColor);
                binding.status3.setText(statusText);
                break;
            case 3:
                binding.room4.setTextColor(roomTextColor);
                binding.price4.setTextColor(priceTextColor);
                binding.rS4.setBackgroundColor(textColor);
                binding.room4btn.setBackgroundResource(roomImage);
                binding.status4.setTextColor(textColor);
                binding.status4.setText(statusText);
                break;
        }
    }
}