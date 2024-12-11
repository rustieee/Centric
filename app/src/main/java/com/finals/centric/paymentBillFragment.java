package com.finals.centric;

import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.finals.centric.databinding.FragmentPaymentBillBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link paymentBillFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class paymentBillFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private FirebaseUser user;

    public paymentBillFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment paymentBillFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static paymentBillFragment newInstance(String param1, String param2) {
        paymentBillFragment fragment = new paymentBillFragment();
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

    FragmentPaymentBillBinding binding;
    FirebaseAuth auth;
    FirebaseFirestore db;
    TextView profileChangeName;
    TextView payRoomTime;
    TextView payRoomPrice;
    CardView profileEditCardLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPaymentBillBinding.inflate(getLayoutInflater(), container, false);
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        user = auth.getCurrentUser();
        profileEditCardLayout = binding.profileEditCardLayout; // Initialize the CardView
        profileEditCardLayout.setVisibility(View.GONE);
        fetchUserName();
        fetchBillData();

        // Load GIF using Glide and handle GIF loop directly
        Glide.with(this)
                .asGif()
                .load(R.drawable.payment_billbg)
                .into(new CustomTarget<GifDrawable>() {
                    @Override
                    public void onResourceReady(GifDrawable resource, Transition<? super GifDrawable> transition) {
                        binding.imageView2.setImageDrawable(resource);
                        resource.setLoopCount(1);  // Play the GIF only once
                        resource.start();  // Start the GIF manually
                    }

                    @Override
                    public void onLoadCleared(Drawable placeholder) {
                        // Handle placeholder if needed
                    }
                });

        setButtonAnimation(binding.payCounterBtn, () -> replaceFragment(new paymentNoticeFragment()));
        setButtonAnimation(binding.payGcashBtn, () -> replaceFragment(new paymentGcashFragment()));

        return binding.getRoot();
    }

    private void fetchUserName() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            db.collection("users").document(currentUser.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        String firstName = documentSnapshot.getString("first_name");
                        String lastName = documentSnapshot.getString("last_name");
                        String fullName = firstName + " " + lastName;
                        binding.payName.setText(fullName);
                    });
        }
    }

    private void fetchBillData() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        LinearLayout bookingsContainer = binding.currentBookingsContainer;
        bookingsContainer.removeAllViews();

        if (currentUser != null) {
            db.collection("bills")
                    .whereEqualTo("user_id", currentUser.getUid())
                    .whereEqualTo("payment_status", "Pending")
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null) {
                            int totalBookings = task.getResult().size();
                            int currentBooking = 0;
                            int totalPrice = 0; // Add this variable

                            for (QueryDocumentSnapshot billDoc : task.getResult()) {
                                currentBooking++;
                                String roomId = billDoc.getString("room_id");
                                String checkInDate = billDoc.getString("check_in_date");
                                String checkOutDate = billDoc.getString("check_out_date");
                                String price = billDoc.getString("price");
                                totalPrice += Integer.parseInt(price);
                                addBookingView(roomId, checkInDate, checkOutDate, price, currentBooking, totalBookings);
                            }

                            binding.payTotalPrice.setText("₱" + totalPrice + ".00");
                            profileEditCardLayout.setVisibility(totalBookings > 0 ? View.VISIBLE : View.GONE);
                        }
                    });
        }
    }


    private void addBookingView(String roomId, String checkInDate, String checkOutDate, String price, int currentBooking, int totalBookings) {
        View bookingView = getLayoutInflater().inflate(R.layout.booking_item, binding.currentBookingsContainer, false);

        TextView roomNameView = bookingView.findViewById(R.id.roomName);
        TextView bookingTimeView = bookingView.findViewById(R.id.bookingTime);
        TextView roomPriceView = bookingView.findViewById(R.id.roomPrice);
        View divider = bookingView.findViewById(R.id.divider);

        divider.setVisibility(View.GONE);

        db.collection("rooms").document(roomId)
                .get()
                .addOnSuccessListener(roomDoc -> {
                    String roomName = roomDoc.getString("roomName");
                    String roomType = roomDoc.getString("roomType");
                    roomNameView.setText(roomName + " - " + roomType);
                    bookingTimeView.setText(String.format("%s ~ %s", checkInDate, checkOutDate));
                    roomPriceView.setText("₱" + price + ".00");
                });

        binding.currentBookingsContainer.addView(bookingView);
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
                .commit();
    }
}