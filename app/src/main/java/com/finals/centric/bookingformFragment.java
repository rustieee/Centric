package com.finals.centric;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.finals.centric.databinding.FragmentBookingformBinding;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link bookingformFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class bookingformFragment extends Fragment implements DatePickerDialog.OnDateSetListener {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private FirebaseUser user;


    public bookingformFragment() {
        // Required empty public constructor
    }


    // TODO: Rename and change types and number of parameters

    public static bookingformFragment newInstance(String status,String checkin, String checkout, String price, String name, String name2, String details,String roomId) {
        bookingformFragment fragment = new bookingformFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, status);
        args.putString(ARG_PARAM2, price);
        args.putString("ROOM_CHECKIN", checkin);
        args.putString("ROOM_CHECKOUT", checkout);
        args.putString("ROOM_NAME", name);
        args.putString("ROOM_NAME2", name2);
        args.putString("ROOM_DETAILS", details);
        args.putString("roomId", roomId);
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

    FragmentBookingformBinding binding;
    private LinearLayout container; // Reference to the LinearLayout
    private ImageView bookformCompAdd; // Reference to the add button
    private ImageView bookformCompDel; // Reference to the delete button
    FirebaseAuth auth;
    FirebaseFirestore db;

    private int companionCount = 1; // Keep track of the number of companions
    private static final int MAX_COMPANIONS = 3; // Maximum number of companions allowed

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentBookingformBinding.inflate(getLayoutInflater(), container, false);
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        user = auth.getCurrentUser();

        if (getArguments() != null) {
            String status = getArguments().getString(ARG_PARAM1);
            String checkin = getArguments().getString("ROOM_CHECKIN");
            String checkout = getArguments().getString("ROOM_CHECKOUT");
            String name = getArguments().getString("ROOM_NAME");
            String name2 = getArguments().getString("ROOM_NAME2");

            // Use these values in the fragment as needed
            binding.roombookName.setText(name);
            binding.roombookName2.setText(name2);

            switch (status) {
                case "YOU MADE A RESERVATION TO THIS ROOM":
                    binding.bookformAsk1.setText("What new date would you like for your bookingFragment?");
                    binding.bookformAsk1.setTextSize(16);
                    binding.bookformAsk2.setVisibility(View.GONE);
                    binding.bookformCompName.setVisibility(View.GONE);
                    binding.bookformCompAdd.setVisibility(View.GONE);
                    binding.bookformCompDel.setVisibility(View.GONE);
                    binding.bookformAsk3.setVisibility(View.GONE);
                    binding.bookformfieldID.setVisibility(View.GONE);
                    binding.bookformcompBtn.setVisibility(View.GONE);
                    binding.bookformcompBtnIc.setVisibility(View.GONE);
                    binding.idpicFrame.setVisibility(View.GONE);
                    binding.bookIDnote.setVisibility(View.GONE);
                    binding.bookformcheck2.setText(checkin);
                    break;
                case "YOU ARE OCCUPYING THIS ROOM":
                    binding.bookformAsk1.setText("How long would you like to extend your stay?");
                    binding.bookformAsk1.setTextSize(16);
                    binding.bookformAsk2.setVisibility(View.GONE);
                    binding.bookformCompName.setVisibility(View.GONE);
                    binding.bookformCompAdd.setVisibility(View.GONE);
                    binding.bookformCompDel.setVisibility(View.GONE);
                    binding.bookformAsk3.setVisibility(View.GONE);
                    binding.bookformfieldID.setVisibility(View.GONE);
                    binding.bookformcompBtn.setVisibility(View.GONE);
                    binding.bookformcompBtnIc.setVisibility(View.GONE);
                    binding.idpicFrame.setVisibility(View.GONE);
                    binding.bookIDnote.setVisibility(View.GONE);
                    binding.bookformcheck1.setText("CURRENT CHECK OUT");
                    binding.bookformcheck2.setVisibility(View.GONE);
                    binding.bookformDate.setText(checkout);
                    binding.bookformDate.setEnabled(false);
                    if (binding.bookformday.getText().toString().isEmpty()) {
                        binding.bookformday.setText("1"); // Default to 1 day
                    }

                    // Trigger the updateDays method to calculate the checkout date
                    updateDays(0);
                    break;
                default:
                    updateButtonVisibility();
                    binding.bookformcheck1.setVisibility(View.GONE);
                    binding.bookformcheck2.setVisibility(View.GONE);
            }
        }


        this.container = binding.container; // Assuming your LinearLayout ID is 'container'
        this.bookformCompAdd = binding.bookformCompAdd;
        this.bookformCompDel = binding.bookformCompDel;

        // Set initial visibility
        setupCalendarView();
        setDefaultDate();


        // Add this to ensure calculation happens when fragment is first loaded
        binding.bookformDate.post(() -> {
            updateDays(0);
        });
        binding.bookformtime.setOnClickListener(v -> showTimePickerDialog());
        binding.bookformtimeopen.setOnClickListener(v -> showTimePickerDialog());
        binding.bookformdaysub.setOnClickListener(v -> animateButton(v, () -> updateDays(-1)));
        binding.bookformdayadd.setOnClickListener(v -> animateButton(v, () -> updateDays(1)));

        // Set click listener for adding companion names
        bookformCompAdd.setOnClickListener(v -> animateButton(v, this::addCompanionNameInput));
        binding.bookformCompDel.setOnClickListener(v -> animateButton(v, this::removeCompanionNameInput));

        // Set click listener for confirming the bookingFragment
        binding.bookformConfirm.setOnClickListener(v -> animateButton(v, () -> {
            // Validate time is set
            String timeText = binding.bookformtime.getText().toString();
            if (timeText.isEmpty()) {
                if (isAdded() && getContext() != null) {
                    Toast.makeText(requireContext(), "Please select a time", Toast.LENGTH_SHORT).show();
                }
                return;
            }
            String checkinDate = binding.bookformDate.getText().toString();
            String checkinTime = binding.bookformtime.getText().toString();
            String combinedCheckinDateTime = checkinDate + " " + checkinTime;
            String checkoutDate = calculatedCheckoutDate; // This should already be in the full datetime format
            String roomId = getArguments().getString("roomId");
            String userId = user.getUid();

            if (checkinDate.isEmpty()) {
                Toast.makeText(getContext(), "Please enter a check-in date", Toast.LENGTH_SHORT).show();
                return; // Exit if check-in date is not provided
            }


            // Collect companion names from the dynamic fields
            List<String> companions = new ArrayList<>();
            for (int i = 0; i < container.getChildCount(); i++) {
                View childView = container.getChildAt(i);
                if (childView instanceof EditText) {
                    EditText companionNameField = (EditText) childView;
                    String companionName = companionNameField.getText().toString().trim();
                    if (!companionName.isEmpty()) {
                        companions.add(companionName);
                    }
                }
            }

            // Determine the status based on the current situation
            String status;
            if ("YOU MADE A RESERVATION TO THIS ROOM".equals(mParam1)) {
                // User is making a new reservation
                status = "RESERVED";
            } else if ("YOU ARE OCCUPYING THIS ROOM".equals(mParam1)) {
                // User is extending their stay or already occupying the room
                status = "OCCUPIED";
            } else {
                status = "RESERVED"; // Default status, can be customized
            }


            // Check if the form corresponds to an update or new booking
            db.collection("booking")
                    .whereEqualTo("roomId", roomId)
                    .whereEqualTo("user_id", userId)
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        if (!querySnapshot.isEmpty()) {
                            // Document found, update it
                            String bookingId = querySnapshot.getDocuments().get(0).getId();  // Get the document ID
                            // Update booking with combined datetime
                            Map<String, Object> updateData = new HashMap<>();
                            updateData.put("check_in_date", combinedCheckinDateTime);
                            updateData.put("check_out_date", checkoutDate);
                            updateData.put("status", status);
                            updateData.put("companions", companions);

                            db.collection("booking").document(bookingId)
                                    .update(updateData)
                                    .addOnSuccessListener(aVoid -> {
                                        // Safely show Toast with context checking
                                        if (isAdded() && getContext() != null) {
                                            Toast.makeText(requireContext(), "Booking updated", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Log.e("BookingFormFragment", "Cannot show Toast - fragment not attached");
                                        }

                                        // Create or update bill with new datetime
                                        createOrUpdateBill(
                                                bookingId,
                                                roomId,
                                                userId,
                                                combinedCheckinDateTime,
                                                checkoutDate,
                                                calculatePrice(combinedCheckinDateTime, checkoutDate),
                                                "Pending",
                                                null,
                                                null
                                        );
                                    })
                                    .addOnFailureListener(e -> {
                                        if (isAdded() && getContext() != null) {
                                            Toast.makeText(requireContext(), "Failed to update booking", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Log.e("BookingFormFragment", "Cannot show error Toast - fragment not attached");
                                        }
                                    });
                        } else {
                            // Booking not found, create a new one
                            createNewBooking(combinedCheckinDateTime, checkoutDate, roomId, status, userId, companions, newBookingId -> {
                                // After creating the new booking, create a new bill
                                createOrUpdateBill(
                                        newBookingId,
                                        roomId,
                                        userId,
                                        combinedCheckinDateTime,
                                        checkoutDate,
                                        calculatePrice(combinedCheckinDateTime, checkoutDate),
                                        "Pending",
                                        null,
                                        null
                                );
                            });
                        }
                    })
                    .addOnFailureListener(e -> {
                        // Error checking booking

                    });

            // Navigate back to the bookingFragment or desired fragment
            replaceFragment(new bookingFragment());
        }));


        return binding.getRoot();
    }

    private void setDefaultDate() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        String formattedDate = String.format("%02d/%02d/%04d", month + 1, day, year);
        binding.bookformDate.setText(formattedDate);
    }


    private void setupCalendarView() {
        binding.calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            String formattedDate = String.format("%02d/%02d/%04d", month + 1, dayOfMonth, year);
            binding.bookformDate.setText(formattedDate);

            // Ensure day is set
            if (binding.bookformday.getText().toString().isEmpty()) {
                binding.bookformday.setText("1");
            }

            // Only trigger calculation if time is set
            if (!binding.bookformtime.getText().toString().isEmpty()) {
                updateDays(0);
            }
        });

        // Initial date setup without forcing a time
        setInitialDate();
    }

    private void setInitialDate() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // Set default date
        String formattedDate = String.format("%02d/%02d/%04d", month + 1, day, year);
        binding.bookformDate.setText(formattedDate);

        // Leave time blank
        binding.bookformtime.setText("");

        // Set default days
        binding.bookformday.setText("1");
    }


    // Add in the class
    private void showTimePickerDialog() {
        TimePickerWheelDialog dialog = new TimePickerWheelDialog(getContext());
        dialog.setOnTimeSelectedListener(time -> {
            binding.bookformtime.setText(time);
            // Automatically trigger calculation when time is set
            updateDays(0);
        });
        dialog.show();
    }


    private void createOrUpdateBill(String bookingId, String roomId, String userId, String checkinDate,
                                    String checkoutDate, double price, String paymentStatus,
                                    String paymentMethod, String paymentReceiptUrl) {
        long timestamp = System.currentTimeMillis();

        Log.d("BillUpdate", "Searching for bill - BookingID: " + bookingId + ", UserID: " + userId);

        // Query using both booking_id and user_id
        db.collection("bills")
                .whereEqualTo("user_id", userId)  // Add user_id to query
                .whereEqualTo("booking_id", bookingId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    Log.d("BillUpdate", "Found " + querySnapshot.size() + " matching bills");

                    if (!querySnapshot.isEmpty()) {
                        // Get the existing bill document
                        String billId = querySnapshot.getDocuments().get(0).getId();
                        Log.d("BillUpdate", "Updating bill ID: " + billId);

                        Map<String, Object> updates = new HashMap<>();
                        updates.put("user_id", userId);
                        updates.put("booking_id", bookingId);
                        updates.put("room_id", roomId);
                        updates.put("check_in_date", checkinDate);
                        updates.put("check_out_date", checkoutDate);
                        updates.put("price", price);
                        updates.put("updated_at", timestamp);

                        db.collection("bills").document(billId)
                                .update(updates)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d("BillUpdate", "Bill updated successfully");
                                    if (isAdded() && getContext() != null) {
                                        Toast.makeText(requireContext(), "Bill updated", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        // Create new bill with all required fields
                        Map<String, Object> newBill = new HashMap<>();
                        newBill.put("user_id", userId);
                        newBill.put("booking_id", bookingId);
                        newBill.put("room_id", roomId);
                        newBill.put("check_in_date", checkinDate);
                        newBill.put("check_out_date", checkoutDate);
                        newBill.put("price", price);
                        newBill.put("payment_status", paymentStatus);
                        newBill.put("payment_method", paymentMethod);
                        newBill.put("payment_receipt_url", paymentReceiptUrl);
                        newBill.put("created_at", timestamp);
                        newBill.put("updated_at", timestamp);

                        db.collection("bills")
                                .add(newBill)
                                .addOnSuccessListener(documentReference -> {
                                    Log.d("BillUpdate", "New bill created with ID: " + documentReference.getId());
                                    if (isAdded() && getContext() != null) {
                                        Toast.makeText(requireContext(), "New bill created", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                });
    }



    private Map<String, Integer> roomPrices = new HashMap<>();

    // Modify calculatePrice method to use combined datetime
    private double calculatePrice(String checkinDate, String checkoutDate) {
        // Fetch price from Firestore if not already cached
        String roomId = getArguments().getString("roomId");
        if (!roomPrices.containsKey(roomId)) {
            fetchRoomPrice(roomId);
        }

        // Retrieve the cached price as a double, defaulting to 0.0 if not found
        double pricePerDay = roomPrices.getOrDefault(roomId, 0);

        // Calculate the total price based on check-in and check-out dates
        return calculateTotalPrice(checkinDate, checkoutDate, pricePerDay);
    }

    private double calculateTotalPrice(String checkinDate, String checkoutDate, double pricePerDay) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm", Locale.getDefault());
        try {
            Date checkIn = dateFormat.parse(checkinDate);
            Date checkOut = dateFormat.parse(checkoutDate);
            if (checkIn != null && checkOut != null) {
                long diffInMillis = checkOut.getTime() - checkIn.getTime();
                long days = TimeUnit.MILLISECONDS.toDays(diffInMillis);
                return days * pricePerDay;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;  // Return 0 if calculation fails
    }

    private void fetchRoomPrice(String roomId) {
        db.collection("rooms")
                .document(roomId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Fetch the price as an integer from Firestore
                        Long priceLong = documentSnapshot.getLong("price");
                        if (priceLong != null) {
                            // Store the price as an integer in the roomPrices cache
                            roomPrices.put(roomId, priceLong.intValue());
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("BookingForm", "Error fetching room price", e);
                });
    }



    // Method to create a new booking with companions
    private void createNewBooking(String checkinDate, String checkoutDate, String roomId, String status,
                                  String userId, List<String> companions, OnBookingCreatedListener listener) {
        // Prepare the booking data
        Map<String, Object> bookingData = new HashMap<>();
        bookingData.put("check_in_date", checkinDate);
        bookingData.put("check_out_date", checkoutDate);
        bookingData.put("roomId", roomId);
        bookingData.put("status", status);
        bookingData.put("user_id", userId);
        bookingData.put("companions", companions);

        // Add booking to Firestore
        db.collection("booking").add(bookingData)
                .addOnSuccessListener(documentReference -> {
                    // Successfully created booking, pass the booking ID to the listener
                    String generatedBookingId = documentReference.getId();
                    listener.onBookingCreated(generatedBookingId); // Pass the ID to the callback
                })
                .addOnFailureListener(e -> {
                    // Handle failure
                    Toast.makeText(getContext(), "Failed to create booking", Toast.LENGTH_SHORT).show();
                });
    }

    public interface OnBookingCreatedListener {
        void onBookingCreated(String bookingId);
    }
    

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        // After selecting the date, show the TimePickerDialog
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(), (timePicker, selectedHour, selectedMinute) -> {
            // Format the date and time and set it to the TextView
            String formattedDateTime = (month + 1) + "/" + dayOfMonth + "/" + year + " "
                    + String.format("%02d:%02d", selectedHour, selectedMinute);
            binding.bookformDate.setText(formattedDateTime);
            if (binding.bookformday.getText().toString().isEmpty()) {
                binding.bookformday.setText("1"); // Default to 1 day
            }

            // Trigger the updateDays method to calculate the checkout date
            updateDays(0);
        }, hour, minute, true); // true for 24-hour format, change to false for 12-hour format

        timePickerDialog.show();
    }

    private String calculatedCheckoutDate; // Variable to store the calculated checkout date

    // Modify updateDays to handle potential empty time
    private void updateDays(int change) {
        String currentText = binding.bookformday.getText().toString();
        int currentValue = currentText.isEmpty() ? 1 : Integer.parseInt(currentText);
        int newValue = currentValue + change;

        if (newValue > 99) {
            newValue = 99; // Set to max limit
        } else if (newValue < 1) {
            newValue = 1;
        }

        // Store the new value in bookformday for check-in
        binding.bookformday.setText(String.valueOf(newValue));

        // Combine date and time
        String dateText = binding.bookformDate.getText().toString();
        String timeText = binding.bookformtime.getText().toString();

        // Only proceed if both date and time are available
        if (!dateText.isEmpty() && !timeText.isEmpty()) {
            // Combine date and time
            String combinedDateTime = dateText + " " + timeText;

            SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm", Locale.getDefault());
            try {
                Date checkInDate = dateFormat.parse(combinedDateTime);
                if (checkInDate != null) {
                    // Calculate the checkout date based on the number of days
                    int hoursToAdd = newValue * 22; // 1 night = 22 hours
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(checkInDate);
                    calendar.add(Calendar.HOUR_OF_DAY, hoursToAdd);

                    // Format the new checkout date
                    calculatedCheckoutDate = formatDate(calendar);
                } else {
                    calculatedCheckoutDate = null;
                }
            } catch (ParseException e) {
                e.printStackTrace();
                calculatedCheckoutDate = null;
            }
        } else {
            calculatedCheckoutDate = null;
        }
    }



    // Format the date for display
    private String formatDate(Calendar calendar) {
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1; // Month is 0-based
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        return String.format("%02d/%02d/%04d %02d:%02d", month, day, year, hour, minute);
    }

    private void addCompanionNameInput() {
        if (companionCount < MAX_COMPANIONS) {
            // Create new EditText for companion name
            EditText newCompanionName = new EditText(getContext());

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 25, 0, 0); // Change these values as needed
            newCompanionName.setLayoutParams(params);
            newCompanionName.setHint("Full name");
            newCompanionName.setTextSize(16);
            newCompanionName.setBackgroundResource(R.drawable.booking_form_comp); // Set your background drawable
            newCompanionName.setPadding(30, 12, 12, 12);

            // Add the EditText to the container
            container.addView(newCompanionName);

            // Increment the companion count
            companionCount++;
            updateButtonVisibility(); // Update button visibility
        }
    }

    private void removeCompanionNameInput() {
        if (companionCount > 0) {
            // Get the last added view
            int lastIndex = companionCount - 1; // Index of the last view
            View lastCompanionView = container.getChildAt(lastIndex); // Get last view

            // Ensure the view is indeed an EditText
            if (lastCompanionView instanceof EditText) {
                container.removeView(lastCompanionView); // Remove the last EditText
                companionCount--; // Decrement companion count
            }
            // Update button visibility
            updateButtonVisibility();
        }
    }

    private void updateButtonVisibility() {
        binding.bookformCompDel.setVisibility(companionCount > 1 ? View.VISIBLE : View.INVISIBLE);
        binding.bookformCompAdd.setVisibility(companionCount < MAX_COMPANIONS ? View.VISIBLE : View.INVISIBLE);
    }

    private void animateButton(View view, Runnable action) {
        view.animate()
                .scaleX(0.9f)
                .scaleY(0.9f)
                .setDuration(100)
                .withEndAction(() -> {
                    view.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(100)
                            .start();
                    action.run(); // Execute the action passed as a parameter
                })
                .start();
    }

    private void replaceFragment(Fragment fragment) {
        getParentFragmentManager().beginTransaction()
                .replace(R.id.mainframe, fragment)
                .commit();
    }






}