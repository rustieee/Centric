package com.finals.centric;

import static com.finals.centric.RoomInfo.bookroomImagesAvailableReserved;
import static com.finals.centric.RoomInfo.bookroomImagesOccupiedYou;
import static com.finals.centric.RoomInfo.bookroomImagesReservedYou;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.finals.centric.databinding.FragmentBookingBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link bookingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class bookingFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private FirebaseUser user;
    private RoomInfo[] roomData = new RoomInfo[4];

    public bookingFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment bookingFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static bookingFragment newInstance(String param1, String param2) {
        bookingFragment fragment = new bookingFragment();
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

    FragmentBookingBinding binding;
    FirebaseAuth auth;
    FirebaseFirestore db;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentBookingBinding.inflate(getLayoutInflater(), container, false);
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        user = auth.getCurrentUser();


        fetchRoomData();

        // Set click listeners, passing the appropriate fragment
        binding.bookroom1btn.setOnClickListener(v -> roomanimateButtonAndSwitchFragment(v, 0));
        binding.bookroom2btn.setOnClickListener(v -> roomanimateButtonAndSwitchFragment(v, 1));
        binding.bookroom3btn.setOnClickListener(v -> roomanimateButtonAndSwitchFragment(v, 2));
        binding.bookroom4btn.setOnClickListener(v -> roomanimateButtonAndSwitchFragment(v, 3));


        return binding.getRoot();
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

                        // Create a list to store and sort booking dates
                        List<BookingDate> bookingDatesList = new ArrayList<>();

                        for (QueryDocumentSnapshot bookingDoc : task.getResult()) {
                            String status = bookingDoc.getString("status");
                            String userId = bookingDoc.getString("user_id");
                            String bookingCheckIn = bookingDoc.getString("check_in_date");
                            String bookingCheckOut = bookingDoc.getString("check_out_date");

                            // Add to booking dates list
                            // Only add to booking dates list if status is RESERVED or OCCUPIED
                            if (bookingCheckIn != null && bookingCheckOut != null &&
                                    ("RESERVED".equals(status) || "OCCUPIED".equals(status))) {
                                boolean isUserBooking = user.getUid().equals(bookingDoc.getString("user_id"));
                                bookingDatesList.add(new BookingDate(bookingCheckIn, bookingCheckOut, isUserBooking));
                            }

                            if (user.getUid().equals(userId)) {
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

                        // Sort booking dates
                        Collections.sort(bookingDatesList, (a, b) -> {
                            try {
                                Date date1 = sdf.parse(a.checkIn);
                                Date date2 = sdf.parse(b.checkIn);
                                return date1.compareTo(date2);
                            } catch (ParseException e) {
                                return 0;
                            }
                        });

                        // Build sorted dates string
                        StringBuilder bookingDates = new StringBuilder();
                        int displayLimit = 3;
                        int totalDates = bookingDatesList.size();

                        for (int i = 0; i < Math.min(displayLimit, totalDates); i++) {
                            BookingDate date = bookingDatesList.get(i);
                            if (i > 0) {
                                bookingDates.append("\n");
                            }
                            bookingDates.append(date.checkIn)
                                    .append(" ~ ")
                                    .append(date.checkOut)
                                    .append(date.isUserBooking ? " (Your booking)" : "");
                        }

                        // Add ellipsis if there are more dates
                        if (totalDates > displayLimit) {
                            bookingDates.append("\n...");
                        }

                        // Get the relevant views based on room index
                        TextView currentBookTitle = null;
                        TextView currentBooks = null;
                        switch (roomIndex) {
                            case 0:
                                currentBookTitle = binding.currentBookTitle;
                                currentBooks = binding.currentBooks;
                                break;
                            case 1:
                                currentBookTitle = binding.currentBookTitle2;
                                currentBooks = binding.currentBooks2;
                                break;
                            case 2:
                                currentBookTitle = binding.currentBookTitle3;
                                currentBooks = binding.currentBooks3;
                                break;
                            case 3:
                                currentBookTitle = binding.currentBookTitle4;
                                currentBooks = binding.currentBooks4;
                                break;
                        }

                        // Update booking dates visibility and text
                        if (currentBookTitle != null && currentBooks != null) {
                            if (!bookingDatesList.isEmpty()) {
                                currentBookTitle.setVisibility(View.VISIBLE);
                                currentBooks.setVisibility(View.VISIBLE);
                                currentBooks.setText(bookingDates.toString());
                            } else {
                                currentBookTitle.setVisibility(View.GONE);
                                currentBooks.setVisibility(View.GONE);
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

    // Add this helper class at the end of your bookingFragment class
    private static class BookingDate {
        String checkIn;
        String checkOut;
        boolean isUserBooking;

        BookingDate(String checkIn, String checkOut, boolean isUserBooking) {
            this.checkIn = checkIn;
            this.checkOut = checkOut;
            this.isUserBooking = isUserBooking;
        }
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
                binding.bookroom1.setText(roomInfo.getRoomName() + " - " + roomInfo.getRoomType());
                binding.bookprice1.setText("₱"+ roomInfo.getPrice()+".00");
                binding.bookstatus1.setText(roomInfo.getStatus());
                updateRoomUI(roomIndex, roomInfo);
                break;
            case 1:
                binding.bookroom2.setText(roomInfo.getRoomName() + " - " + roomInfo.getRoomType());
                binding.bookprice2.setText("₱"+ roomInfo.getPrice()+".00");
                binding.bookstatus2.setText(roomInfo.getStatus());
                updateRoomUI(roomIndex, roomInfo);
                break;
            case 2:
                binding.bookroom3.setText(roomInfo.getRoomName() + " - " + roomInfo.getRoomType());
                binding.bookprice3.setText("₱"+ roomInfo.getPrice()+".00");
                binding.bookstatus3.setText(roomInfo.getStatus());
                updateRoomUI(roomIndex, roomInfo);
                break;
            case 3:
                binding.bookroom4.setText(roomInfo.getRoomName() + " - " + roomInfo.getRoomType());
                binding.bookprice4.setText("₱"+ roomInfo.getPrice()+".00");
                binding.bookstatus4.setText(roomInfo.getStatus());
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
                bookCheckBg = binding.bookcheckbg1;
                bookCheckDate = binding.bookcheckdate1;
                break;
            case 1:
                bookCheckBg = binding.bookcheckbg2;
                bookCheckDate = binding.bookcheckdate2;
                break;
            case 2:
                bookCheckBg = binding.bookcheckbg3;
                bookCheckDate = binding.bookcheckdate3;
                break;
            case 3:
                bookCheckBg = binding.bookcheckbg4;
                bookCheckDate = binding.bookcheckdate4;
                break;
        }

        switch (statusText) {
            case "AVAILABLE":
                roomImage = bookroomImagesAvailableReserved[roomIndex];
                textColor = ContextCompat.getColor(requireContext(), R.color.green);
                bookCheckDate.setVisibility(View.GONE);
                bookCheckBg.setVisibility(View.GONE);
                break;
            case "RESERVED":
                roomImage = bookroomImagesAvailableReserved[roomIndex];
                textColor = ContextCompat.getColor(requireContext(), R.color.orange);
                bookCheckDate.setVisibility(View.GONE);
                bookCheckBg.setVisibility(View.GONE);
                break;
            case "MAINTENANCE":
                roomImage = bookroomImagesAvailableReserved[roomIndex];
                textColor = ContextCompat.getColor(requireContext(), R.color.orange);
                bookCheckDate.setVisibility(View.GONE);
                bookCheckBg.setVisibility(View.GONE);
                break;
            case "YOU MADE A RESERVATION TO THIS ROOM":
                roomImage = bookroomImagesReservedYou[roomIndex];
                textColor = ContextCompat.getColor(requireContext(), R.color.white);
                statusText = "YOU MADE A RESERVATION\nTO THIS ROOM";
                bookCheckDate.setText(roomInfo.getCheckin());
                bookCheckBg.setVisibility(View.VISIBLE);
                bookCheckBg.setBackgroundResource(R.drawable.home_book2_checkin); // Set the appropriate background image
                switch (roomIndex) {
                    case 0:
                        binding.bookroom1btn.setCardBackgroundColor(Color.parseColor("#d49e34"));
                        binding.currentBookTitle.setTextColor(Color.parseColor("white"));
                        binding.currentBooks.setTextColor(Color.parseColor("white"));
                        break;
                    case 1:
                        binding.bookroom2btn.setCardBackgroundColor(Color.parseColor("#d49e34"));
                        binding.currentBookTitle2.setTextColor(Color.parseColor("white"));
                        binding.currentBooks2.setTextColor(Color.parseColor("white"));
                        break;
                    case 2:
                        binding.bookroom3btn.setCardBackgroundColor(Color.parseColor("#d49e34"));
                        binding.currentBookTitle3.setTextColor(Color.parseColor("white"));
                        binding.currentBooks3.setTextColor(Color.parseColor("white"));
                        break;
                    case 3:
                        binding.bookroom4btn.setCardBackgroundColor(Color.parseColor("#d49e34"));
                        binding.currentBookTitle4.setTextColor(Color.parseColor("white"));
                        binding.currentBooks4.setTextColor(Color.parseColor("white"));
                        break;
                }
                break;
            case "YOU ARE OCCUPYING THIS ROOM":
                roomImage = bookroomImagesOccupiedYou[roomIndex];
                textColor = ContextCompat.getColor(requireContext(), R.color.white);
                statusText = "YOU ARE OCCUPYING\nTHIS ROOM";
                bookCheckDate.setText(roomInfo.getCheckout());
                bookCheckBg.setVisibility(View.VISIBLE);
                bookCheckBg.setBackgroundResource(R.drawable.home_book3_checkout); // Set the appropriate background image
                switch (roomIndex) {
                    case 0:
                        binding.bookroom1btn.setCardBackgroundColor(Color.parseColor("#1f8bcc"));
                        binding.currentBookTitle.setTextColor(Color.parseColor("white"));
                        binding.currentBooks.setTextColor(Color.parseColor("white"));
                        break;
                    case 1:
                        binding.bookroom2btn.setCardBackgroundColor(Color.parseColor("#1f8bcc"));
                        binding.currentBookTitle2.setTextColor(Color.parseColor("white"));
                        binding.currentBooks2.setTextColor(Color.parseColor("white"));
                        break;
                    case 2:
                        binding.bookroom3btn.setCardBackgroundColor(Color.parseColor("#1f8bcc"));
                        binding.currentBookTitle3.setTextColor(Color.parseColor("white"));
                        binding.currentBooks3.setTextColor(Color.parseColor("white"));
                        break;
                    case 3:
                        binding.bookroom4btn.setCardBackgroundColor(Color.parseColor("#1f8bcc"));
                        binding.currentBookTitle4.setTextColor(Color.parseColor("white"));
                        binding.currentBooks4.setTextColor(Color.parseColor("white"));
                        break;
                }
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
                binding.bookroom1.setTextColor(roomTextColor);
                binding.bookprice1.setTextColor(priceTextColor);
                binding.rS1.setBackgroundColor(textColor);
                binding.bookroom1bg.setImageResource(roomImage);
                binding.bookstatus1.setTextColor(textColor);
                binding.bookstatus1.setText(statusText);
                break;
            case 1:
                binding.bookroom2.setTextColor(roomTextColor);
                binding.bookprice2.setTextColor(priceTextColor);
                binding.rS2.setBackgroundColor(textColor);
                binding.bookroom2bg.setImageResource(roomImage);
                binding.bookstatus2.setTextColor(textColor);
                binding.bookstatus2.setText(statusText);
                break;
            case 2:
                binding.bookroom3.setTextColor(roomTextColor);
                binding.bookprice3.setTextColor(priceTextColor);
                binding.rS3.setBackgroundColor(textColor);
                binding.bookroom3bg.setImageResource(roomImage);
                binding.bookstatus3.setTextColor(textColor);
                binding.bookstatus3.setText(statusText);
                break;
            case 3:
                binding.bookroom4.setTextColor(roomTextColor);
                binding.bookprice4.setTextColor(priceTextColor);
                binding.rS4.setBackgroundColor(textColor);
                binding.bookroom4bg.setImageResource(roomImage);
                binding.bookstatus4.setTextColor(textColor);
                binding.bookstatus4.setText(statusText);
                break;
        }
    }
}