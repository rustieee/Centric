package com.finals.centric;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;
    private Uri imageUri;
    private StorageReference storageRef;
    private String selectedFileName;



    private int companionCount = 1; // Keep track of the number of companions
    private static final int MAX_COMPANIONS = 3; // Maximum number of companions allowed

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentBookingformBinding.inflate(getLayoutInflater(), container, false);
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        user = auth.getCurrentUser();
        this.container = binding.container;
        storageRef = FirebaseStorage.getInstance().getReference("id_uploads");


        // Modify the camera result handling
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        binding.profileIDPic.setImageURI(imageUri);
                        binding.profileEditCardBg.setVisibility(View.GONE);
                        selectedFileName = "Camera_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                                .format(new Date()) + ".jpg";
                        binding.imageFileName.setText(selectedFileName);
                    }
                }
        );

// Modify the gallery result handling
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        imageUri = result.getData().getData();
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(
                                    requireActivity().getContentResolver(),
                                    imageUri
                            );
                            binding.profileIDPic.setImageBitmap(bitmap);
                            binding.profileEditCardBg.setVisibility(View.GONE);

                            // Get and set the actual file name from gallery
                            String[] projection = {MediaStore.Images.Media.DISPLAY_NAME};
                            Cursor cursor = requireActivity().getContentResolver().query(
                                    imageUri, projection, null, null, null);

                            if (cursor != null && cursor.moveToFirst()) {
                                int nameIndex = cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME);
                                selectedFileName = cursor.getString(nameIndex);
                                binding.imageFileName.setText(selectedFileName);
                                cursor.close();
                            } else {
                                selectedFileName = "Gallery_" + new SimpleDateFormat("yyyyMMdd_HHmmss",
                                        Locale.getDefault()).format(new Date()) + ".jpg";
                                binding.imageFileName.setText(selectedFileName);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
        );

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
                    binding.imageFileName.setVisibility(View.GONE);
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
        binding.bookformtime.setOnClickListener(v -> animateButton(v, this::showTimePickerDialog));
        binding.bookformtimeopen.setOnClickListener(v -> animateButton(v, this::showTimePickerDialog));
        binding.bookformdaysub.setOnClickListener(v -> animateButton(v, () -> updateDays(-1)));
        binding.bookformdayadd.setOnClickListener(v -> animateButton(v, () -> updateDays(1)));

        // In onCreateView(), add these click listeners:
        binding.bookformcompBtn.setOnClickListener(v -> {
            animateButton(v, this::showImageSourceDialog);
        });

        binding.imageFileName.setOnClickListener(v -> {
            animateButton(v, this::showImageSourceDialog);
        });


        // Set click listener for adding companion names
        bookformCompAdd.setOnClickListener(v -> animateButton(v, this::addCompanionNameInput));
        binding.bookformCompDel.setOnClickListener(v -> animateButton(v, this::removeCompanionNameInput));

        // Set click listener for confirming the bookingFragment
        binding.bookformConfirm.setOnClickListener(v -> animateButton(v, () -> {

            // Add overlap validation
            int selectedDays = Integer.parseInt(binding.bookformday.getText().toString());
            Calendar selectedDate = Calendar.getInstance();
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
                selectedDate.setTime(sdf.parse(binding.bookformDate.getText().toString()));

                if (binding.customCalendarView.hasBookingOverlap(selectedDate, selectedDays)) {
                    Toast.makeText(getContext(), "Selected days overlap with an existing booking", Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }

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

            // Finally check ID photo if visible
            if (binding.idpicFrame.getVisibility() == View.VISIBLE && imageUri == null) {
                Toast.makeText(getContext(), "Please upload an ID photo", Toast.LENGTH_SHORT).show();
                return;
            }

            if (binding.bookMsg.getVisibility() == View.VISIBLE) {
                Toast.makeText(getContext(), "This date is already booked", Toast.LENGTH_SHORT).show();
                return;
            }


            // When collecting companion names:
            List<String> companions = new ArrayList<>();

            // Add the main companion name
            String mainCompanionName = binding.bookformCompName.getText().toString().trim();
            if (!mainCompanionName.isEmpty()) {
                companions.add(mainCompanionName);
                Log.d("CompanionsDebug", "Added main companion: " + mainCompanionName);
            }

            // Get the correct container reference and add dynamic companions
            LinearLayout dynamicContainer = binding.container;
            for (int i = 0; i < dynamicContainer.getChildCount(); i++) {
                View childView = dynamicContainer.getChildAt(i);
                if (childView instanceof EditText) {
                    String companionName = ((EditText) childView).getText().toString().trim();
                    if (!companionName.isEmpty()) {
                        companions.add(companionName);
                        Log.d("CompanionsDebug", "Added dynamic companion: " + companionName);
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

    private void uploadImage(String bookingId) {
        if (imageUri != null) {
            String fileName = "id_" + bookingId + "_" + System.currentTimeMillis();
            StorageReference fileReference = storageRef.child(fileName);

            fileReference.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                            String imageUrl = uri.toString();
                            db.collection("booking").document(bookingId)
                                    .update("id_image_url", imageUrl)
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d("BookingForm", "ID uploaded successfully: " + imageUrl);
                                    });
                        });
                    })
                    .addOnFailureListener(e -> {
                        Log.e("BookingForm", "Upload failed: " + e.getMessage());
                    });
        }
    }


    // Add this method to show the image source dialog
    private void showImageSourceDialog() {
        String[] options = {"Take Photo", "Choose from Gallery"};

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Upload ID Photo");
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                openCamera();
            } else {
                openGallery();
            }
        });
        builder.show();
    }

    // Add these helper methods
    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photoFile = createImageFile();
        if (photoFile != null) {
            imageUri = FileProvider.getUriForFile(requireContext(),
                    "com.finals.centric.fileprovider",
                    photoFile);
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            cameraLauncher.launch(cameraIntent);
        }
    }

    private File createImageFile() {
        try {
            File storageDir = requireContext().getCacheDir();
            File image = File.createTempFile(
                    "JPEG_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()),
                    ".jpg",
                    storageDir
            );
            return image;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(galleryIntent);
    }

    private Uri getImageUri(Context context, Bitmap originalBitmap) {
        // Target resolution that's clear enough for ID verification but not excessive
        int targetWidth = 1920;
        int targetHeight = 1440;

        // Scale the bitmap to target resolution while maintaining aspect ratio
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(
                originalBitmap,
                targetWidth,
                targetHeight,
                true
        );

        try {
            File cachePath = new File(context.getCacheDir(), "temp_images");
            if (!cachePath.exists()) {
                cachePath.mkdirs();
            }

            File imageFile = new File(cachePath, "temp_" + System.currentTimeMillis() + ".jpg");
            FileOutputStream stream = new FileOutputStream(imageFile);
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 85, stream);
            stream.close();

            return Uri.fromFile(imageFile);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
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
        // Hide bookMsg initially
        binding.bookMsg.setVisibility(View.GONE);

        binding.customCalendarView.setOnDateSelectedListener(date -> {
            // Handle regular date selection
            binding.bookMsg.setVisibility(View.GONE);
            SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
            binding.bookformDate.setText(dateFormat.format(date.getTime()));

            if (binding.bookformday.getText().toString().isEmpty()) {
                binding.bookformday.setText("1");
            }

            if (!binding.bookformtime.getText().toString().isEmpty()) {
                updateDays(0);
            }
        });

        binding.customCalendarView.setOnBookedDateClickListener((checkIn, checkOut) -> {
            // Show booking message
            binding.bookMsg.setVisibility(View.VISIBLE);
            binding.bookMsg.setText("BOOKED: " + checkIn + " ~ " + checkOut);

            // Update the date field
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
            try {
                Date date = new SimpleDateFormat("MM/dd/yyyy HH:mm", Locale.getDefault()).parse(checkIn);
                if (date != null) {
                    binding.bookformDate.setText(sdf.format(date));
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        });


        String roomId = getArguments().getString("roomId");
        fetchBookedDates(roomId);
        setInitialDate();
    }


    private void fetchBookedDates(String roomId) {
        db.collection("booking")
                .whereEqualTo("roomId", roomId)
                .whereEqualTo("status", "RESERVED")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Calendar> bookedDates = new ArrayList<>();
                    Map<String, String[]> dateRanges = new HashMap<>();
                    SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm", Locale.getDefault());
                    SimpleDateFormat keyFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());

                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                        String checkInStr = document.getString("check_in_date");
                        String checkOutStr = document.getString("check_out_date");

                        try {
                            if (checkInStr != null && checkOutStr != null) {
                                Date checkIn = dateFormat.parse(checkInStr);
                                Date checkOut = dateFormat.parse(checkOutStr);

                                if (checkIn != null && checkOut != null) {
                                    Calendar calendar = Calendar.getInstance();
                                    calendar.setTime(checkIn);

                                    // Get end of day for checkout date
                                    Calendar checkOutCal = Calendar.getInstance();
                                    checkOutCal.setTime(checkOut);
                                    checkOutCal.set(Calendar.HOUR_OF_DAY, 23);
                                    checkOutCal.set(Calendar.MINUTE, 59);

                                    while (calendar.getTime().before(checkOutCal.getTime())) {
                                        Calendar bookDate = (Calendar) calendar.clone();
                                        bookedDates.add(bookDate);
                                        String dateKey = keyFormat.format(calendar.getTime());
                                        dateRanges.put(dateKey, new String[]{checkInStr, checkOutStr});
                                        calendar.add(Calendar.DAY_OF_MONTH, 1);
                                    }
                                }
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                    }

                    binding.customCalendarView.setBookedDates(bookedDates);
                    binding.customCalendarView.setBookedDatesWithRanges(dateRanges);
                });
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
                                    String checkoutDate, String price, String paymentStatus,
                                    String paymentMethod, String paymentReceiptUrl) {
        // Ensure price is fetched
        if (!roomPrices.containsKey(roomId)) {
            fetchRoomPrice(roomId);
            Log.d("PriceDebug", "Fetching price for room: " + roomId);
            // Wait for fetch to complete
            db.collection("rooms").document(roomId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String calculatedPrice = calculatePrice(checkinDate, checkoutDate);
                            Log.d("PriceDebug", "Final calculated price: " + calculatedPrice);
                            proceedWithBillCreation(bookingId, roomId, userId, checkinDate,
                                    checkoutDate, calculatedPrice, paymentStatus,
                                    paymentMethod, paymentReceiptUrl);
                        }
                    });
        } else {
            String calculatedPrice = calculatePrice(checkinDate, checkoutDate);
            Log.d("PriceDebug", "Using cached price calculation: " + calculatedPrice);
            proceedWithBillCreation(bookingId, roomId, userId, checkinDate,
                    checkoutDate, calculatedPrice, paymentStatus,
                    paymentMethod, paymentReceiptUrl);
        }
    }

    private void proceedWithBillCreation(String bookingId, String roomId, String userId, String checkinDate,
                                    String checkoutDate, String price, String paymentStatus,
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
                        updates.put("price", calculatePrice(checkinDate, checkoutDate)); // HERE for updates
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
                        newBill.put("price", calculatePrice(checkinDate, checkoutDate)); // HERE for new bill
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



    private Map<String, String> roomPrices = new HashMap<>();

    private String calculatePrice(String checkinDate, String checkoutDate) {
        String roomId = getArguments().getString("roomId");
        if (!roomPrices.containsKey(roomId)) {
            fetchRoomPrice(roomId);
        }

        String pricePerDay = roomPrices.getOrDefault(roomId, "0");
        return calculateTotalPrice(checkinDate, checkoutDate, pricePerDay);
    }


    private void fetchRoomPrice(String roomId) {
        db.collection("rooms")
                .document(roomId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Object priceObj = documentSnapshot.get("price");
                        Log.d("PriceDebug", "Raw price from Firestore: " + priceObj);

                        String price = String.valueOf(priceObj);
                        Log.d("PriceDebug", "Converted price: " + price);
                        roomPrices.put(roomId, price);
                    }
                });
    }

    private String calculateTotalPrice(String checkinDate, String checkoutDate, String pricePerDay) {
        Log.d("PriceDebug", "Price per day: " + pricePerDay);
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm", Locale.getDefault());
        try {
            Date checkIn = dateFormat.parse(checkinDate);
            Date checkOut = dateFormat.parse(checkoutDate);
            if (checkIn != null && checkOut != null) {
                String daysText = binding.bookformday.getText().toString();
                long days = Long.parseLong(daysText);

                Log.d("PriceDebug", "Days from input: " + days);

                double priceValue = Double.parseDouble(pricePerDay);
                double totalPrice = days * priceValue;

                // Format to remove decimal if it's a whole number
                return String.format("%.0f", totalPrice);
            }
        } catch (Exception e) {
            Log.e("PriceDebug", "Error calculating price", e);
        }
        return pricePerDay;
    }



    // Method to create a new booking with companions
    private void createNewBooking(String checkinDate, String checkoutDate, String roomId, String status,
                                  String userId, List<String> companions, OnBookingCreatedListener listener) {
        Map<String, Object> bookingData = new HashMap<>();
        bookingData.put("check_in_date", checkinDate);
        bookingData.put("check_out_date", checkoutDate);
        bookingData.put("roomId", roomId);
        bookingData.put("status", status);
        bookingData.put("user_id", userId);
        bookingData.put("companions", new ArrayList<>(companions));
        bookingData.put("id_image_url", ""); // Initialize empty, will be updated after upload

        db.collection("booking").add(bookingData)
                .addOnSuccessListener(documentReference -> {
                    String generatedBookingId = documentReference.getId();
                    uploadImage(generatedBookingId);  // This will update the id_image_url field
                    listener.onBookingCreated(generatedBookingId);
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
            newValue = 99;
        } else if (newValue < 1) {
            newValue = 1;
        }

        binding.bookformday.setText(String.valueOf(newValue));
        binding.customCalendarView.setSelectedDays(newValue);

        // Only calculate checkout date if we have both date and time
        String dateText = binding.bookformDate.getText().toString();
        String timeText = binding.bookformtime.getText().toString();

        if (!dateText.isEmpty() && !timeText.isEmpty()) {
            String combinedDateTime = dateText + " " + timeText;
            calculateCheckoutDate(combinedDateTime, newValue);
        }
    }

    private void calculateCheckoutDate(String checkInDateTime, int days) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm", Locale.getDefault());
            Date checkInDate = dateFormat.parse(checkInDateTime);

            if (checkInDate != null) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(checkInDate);

                // Add full days first
                calendar.add(Calendar.DAY_OF_MONTH, days);
                // Subtract 2 hours for each day booked
                calendar.add(Calendar.HOUR_OF_DAY, -2 * days);

                calculatedCheckoutDate = dateFormat.format(calendar.getTime());
                Log.d("BookingForm", "Calculated checkout date: " + calculatedCheckoutDate);
            }
        } catch (ParseException e) {
            e.printStackTrace();
            Log.d("BookingForm", "Error calculating checkout date", e);
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