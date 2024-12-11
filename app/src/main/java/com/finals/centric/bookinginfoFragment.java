package com.finals.centric;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.finals.centric.databinding.FragmentBookinginfoBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Arrays;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link bookinginfoFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class bookinginfoFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public bookinginfoFragment() {
        // Required empty public constructor
    }


    // TODO: Rename and change types and number of parameters
    public static bookinginfoFragment newInstance(String status,String checkin, String checkout, String price, String name, String name2, String details,String roomId) {
        bookinginfoFragment fragment = new bookinginfoFragment();
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

    FragmentBookinginfoBinding binding;
    private ViewPager2 viewPager;
    // Image arrays for different rooms
    private int[] room1Images = {
            R.drawable.booking_room1_1,
            R.drawable.booking_room1_2,
    };

    private int[] room2Images = {
            R.drawable.booking_room2_1,
            R.drawable.booking_room2_2,
    };

    private int[] room3Images = {
            R.drawable.booking_room3_1,
            R.drawable.booking_room3_2,
    };

    private int[] room4Images = {
            R.drawable.booking_room4_1,
            R.drawable.booking_room4_2,
    };

    // Variable to hold the selected images array
    private int[] images;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentBookinginfoBinding.inflate(getLayoutInflater(), container, false);
        // Get the roomId from the arguments
        String roomId = getArguments().getString("roomId");

// Dynamically select the image array based on roomId
        if (roomId != null) {
            switch (roomId) {
                case "1":
                    images = room1Images;
                    break;
                case "2":
                    images = room2Images;
                    break;
                case "3":
                    images = room3Images;
                    break;
                case "4":
                    images = room4Images;
                    break;
            }
        }
        // Pass the OnImageClickListener to the adapter
        ImageSliderAdapter adapter = new ImageSliderAdapter(getContext(), images, this::showImageDialog);
        binding.viewPager.setAdapter(adapter);



        // Get the arguments
        if (getArguments() != null) {
            String status = getArguments().getString(ARG_PARAM1);
            String price = getArguments().getString(ARG_PARAM2);
            String checkin = getArguments().getString("ROOM_CHECKIN");
            String checkout = getArguments().getString("ROOM_CHECKOUT");
            String name = getArguments().getString("ROOM_NAME");
            String name2 = getArguments().getString("ROOM_NAME2");
            String details = getArguments().getString("ROOM_DETAILS");

            // Set the values in the TextViews
            binding.roomStatus.setText(status);
            binding.roomPrice.setText("₱"+price+".00");
            binding.roomName.setText(name);
            binding.roomName2.setText(name2);
            binding.roomDetail.setText(formatRoomDetails(details));


            switch (status) {
                case "RESERVED":
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    db.collection("booking")
                            .whereEqualTo("roomId", roomId)
                            .whereIn("status", Arrays.asList("RESERVED", "OCCUPIED"))
                            .get()
                            .addOnSuccessListener(queryDocumentSnapshots -> {
                                if (!queryDocumentSnapshots.isEmpty()) {
                                    DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);
                                    String sstatus = document.getString("status");
                                    String reservedCheckin = document.getString("check_in_date").split(" ")[0];
                                    String reservedCheckout = document.getString("check_out_date").split(" ")[0];

                                    if (reservedCheckin != null && reservedCheckout != null) {
                                        String statusText = sstatus + " FOR\n" + reservedCheckin + " to " + reservedCheckout;
                                        binding.roomStatus.setText(statusText);

                                        // Set color based on status
                                        int colorRes = sstatus.equals("OCCUPIED") ? R.color.blue : R.color.orange;
                                        binding.roomStatus.setTextColor(ContextCompat.getColor(requireContext(), colorRes));
                                    }
                                }
                            });
                    binding.roomStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.orange));
                    binding.checkDetail1.setVisibility(View.GONE);
                    binding.checkDetail2.setVisibility(View.GONE);
                    binding.payinfobtn.setVisibility(View.GONE);
                    binding.earlyinfobtn2.setVisibility(View.GONE);
                    binding.bookInfoCancel.setVisibility(View.GONE);
                    break;
                case "YOU MADE A RESERVATION TO THIS ROOM":
                    binding.roomStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.orange));
                    binding.bookNowbtn.setImageResource(R.drawable.booking_info_change);
                    binding.checkDetail1.setText("CHECK IN");
                    binding.checkDetail2.setText(checkin);
                    binding.payinfobtn.setVisibility(View.VISIBLE);
                    binding.earlyinfobtn2.setVisibility(View.GONE);
                    binding.bookInfoCancel.setVisibility(View.VISIBLE);
                    break;
                case "YOU ARE OCCUPYING THIS ROOM":
                    binding.roomStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.blue));
                    binding.bookNowbtn.setImageResource(R.drawable.booking_form_extend);
                    binding.checkDetail1.setText("CHECK OUT");
                    binding.checkDetail1.setTextColor(ContextCompat.getColor(requireContext(), R.color.blue));
                    binding.checkDetail2.setText(checkout);
                    binding.checkDetail2.setTextColor(ContextCompat.getColor(requireContext(), R.color.blue));
                    binding.payinfobtn.setVisibility(View.VISIBLE);
                    binding.earlyinfobtn2.setVisibility(View.VISIBLE);
                    binding.bookInfoCancel.setVisibility(View.GONE);
                    break;
                case "MAINTENANCE":
                    binding.roomStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.orange));
                    binding.bookNowbtn.setImageResource(R.drawable.booking_info_unavail);
                    binding.bookNowbtn.setEnabled(false);
                    binding.checkDetail1.setVisibility(View.GONE);
                    binding.checkDetail2.setVisibility(View.GONE);
                    binding.payinfobtn.setVisibility(View.GONE);
                    binding.earlyinfobtn2.setVisibility(View.GONE);
                    binding.bookInfoCancel.setVisibility(View.GONE);
                    break;
                default:
                    binding.roomStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.green));
                    binding.bookNowbtn.setImageResource(R.drawable.booking_info_bookbtn);
                    binding.checkDetail1.setVisibility(View.GONE);
                    binding.checkDetail2.setVisibility(View.GONE);
                    binding.payinfobtn.setVisibility(View.GONE);
                    binding.earlyinfobtn2.setVisibility(View.GONE);
                    binding.bookInfoCancel.setVisibility(View.GONE);
                    break;
            }
        }

        // Optional: Add page indicator (e.g., 1/5)
        binding.viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                binding.indicator.setText((position + 1) + "/" + images.length);
            }
        });

        binding.bookNowbtn.setOnClickListener(v -> {
            v.animate()
                    .scaleX(0.9f)
                    .scaleY(0.9f)
                    .setDuration(100)
                    .withEndAction(() -> {
                        // Animate the scale back up after the initial scale down
                        v.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(100)
                                .start();

                        // Start the new activity after the animation finishes
                        replaceFragment(
                                mParam1,
                                getArguments().getString("ROOM_CHECKIN"),
                                getArguments().getString("ROOM_CHECKOUT"),
                                mParam2,
                                getArguments().getString("ROOM_NAME"),
                                getArguments().getString("ROOM_NAME2"),
                                getArguments().getString("ROOM_DETAILS"),
                                getArguments().getString("roomId")
                        );
                    })
                    .start();
        });

        binding.payinfobtn.setOnClickListener(v -> {
            v.animate()
                    .scaleX(0.9f)
                    .scaleY(0.9f)
                    .setDuration(100)
                    .withEndAction(() -> {
                        // Animate the scale back up after the initial scale down
                        v.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(100)
                                .start();

                        // Start the new activity after the animation finishes
                    })
                    .start();
        });

        binding.earlyinfobtn2.setOnClickListener(v -> {
            v.animate()
                    .scaleX(0.9f)
                    .scaleY(0.9f)
                    .setDuration(100)
                    .withEndAction(() -> {
                        // Animate the scale back up after the initial scale down
                        v.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(100)
                                .start();

                        // Start the new activity after the animation finishes
                    })
                    .start();
        });

        binding.bookInfoCancel.setOnClickListener(v -> {
            v.animate()
                    .scaleX(0.9f)
                    .scaleY(0.9f)
                    .setDuration(100)
                    .withEndAction(() -> {
                        v.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(100)
                                .start();

                        // Retrieve roomId from the arguments
                        if (roomId != null && !roomId.isEmpty()) {
                            // Fetch the bookingId from Firestore based on roomId and userId
                            fetchBookingIdAndDelete(roomId);
                        } else {
                            Toast.makeText(requireContext(), "Room ID is missing.", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .start();
        });



        return binding.getRoot();
    }

    private void fetchBookingIdAndDelete(String roomId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid(); // Get the current user's ID

        // Query the booking collection to get the document where roomId and user_id match
        db.collection("booking")
                .whereEqualTo("roomId", roomId)
                .whereEqualTo("user_id", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Extract the bookingId from the first document (assuming one booking per user and room)
                        DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);
                        String bookingId = document.getId(); // The document ID is the bookingId

                        // Now delete the booking and bills using the bookingId
                        deleteBookingEntryAndBills(bookingId, roomId);
                    } else {
                        Toast.makeText(requireContext(), "No booking found to cancel.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Error fetching booking details.", Toast.LENGTH_SHORT).show();
                });
    }

    private void deleteBookingEntryAndBills(String bookingId, String roomId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();

        // First get the booking document to retrieve the image URL
        db.collection("booking")
                .document(bookingId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    String idImageUrl = documentSnapshot.getString("id_image_url");

                    // Delete the image from Storage if URL exists
                    if (idImageUrl != null && !idImageUrl.isEmpty()) {
                        // Convert full URL to storage path
                        StorageReference imageRef = FirebaseStorage.getInstance().getReferenceFromUrl(idImageUrl);
                        imageRef.delete()
                                .addOnSuccessListener(aVoid -> {
                                    Log.d("Storage", "ID image deleted successfully");
                                    // After successful image deletion, proceed with booking deletion
                                    deleteBookingAndBills(documentSnapshot.getReference(), bookingId);
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("Storage", "Error deleting ID image: " + e.getMessage());
                                    // Still proceed with booking deletion even if image deletion fails
                                    deleteBookingAndBills(documentSnapshot.getReference(), bookingId);
                                });
                    } else {
                        // If no image URL, proceed directly to booking deletion
                        deleteBookingAndBills(documentSnapshot.getReference(), bookingId);
                    }
                });
    }

    private void deleteBookingAndBills(DocumentReference bookingRef, String bookingId) {
        bookingRef.delete()
                .addOnSuccessListener(aVoid -> deleteBillEntry(bookingId))
                .addOnFailureListener(e -> {
                    if (isAdded()) {
                        Toast.makeText(requireContext(), "Failed to cancel booking. Try again.", Toast.LENGTH_SHORT).show();
                    }
                });
    }



    private void deleteBillEntry(String bookingId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Reference to the bills collection where the bookingId matches
        db.collection("bills")
                .whereEqualTo("booking_id", bookingId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Delete the first matching bill entry (assuming one bill per booking)
                        for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                            document.getReference().delete()
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(requireContext(), "Booking cancelled successfully.", Toast.LENGTH_SHORT).show();
                                        requireActivity().getSupportFragmentManager().popBackStack(); // Navigate back
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(requireContext(), "Failed to delete bill. Try again.", Toast.LENGTH_SHORT).show();
                                    });
                        }
                    } else {
                        Toast.makeText(requireContext(), "No bill found for this booking.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Error deleting bill.", Toast.LENGTH_SHORT).show();
                });
    }




    private void deleteBookingEntry(String roomId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid(); // Get the current user's ID

        // Reference to the user's booking entry
        db.collection("booking")
                .whereEqualTo("roomId", roomId)
                .whereEqualTo("user_id", userId) // Ensure only the user's booking is targeted
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Delete all matching booking documents
                        for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                            document.getReference().delete()
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(requireContext(), "Booking cancelled successfully.", Toast.LENGTH_SHORT).show();
                                        requireActivity().getSupportFragmentManager().popBackStack(); // Navigate back
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(requireContext(), "Failed to cancel booking. Try again.", Toast.LENGTH_SHORT).show();
                                    });
                        }
                    } else {
                        Toast.makeText(requireContext(), "No booking found to cancel.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Error fetching booking details.", Toast.LENGTH_SHORT).show();
                });
    }


    private String formatRoomDetails(String details) {
        // Check if details are empty
        if (details == null || details.isEmpty()) {
            return "Details not available";
        }

        // Split the details string using " - " as a delimiter
        String[] items = details.split(" - ");

        // Start formatting the text
        StringBuilder formattedDetails = new StringBuilder("About the room\n\nProvides:\n");

        // Append each item in a new line with a hyphen
        for (String item : items) {
            // Skip empty strings if any
            if (!item.trim().isEmpty()) {
                formattedDetails.append("- ").append(item.trim()).append("\n");
            }
        }

        return formattedDetails.toString().trim(); // Return the formatted string
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // Method to open the image in a full-screen dialog
    private void showImageDialog(int imageResId) {
        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_fullscreen_image);

        ImageView imageView = dialog.findViewById(R.id.fullscreenImageView);
        imageView.setImageResource(imageResId);

        dialog.show();
    }

    private void replaceFragment(String status,String checkin, String checkout, String price, String name, String name2, String details, String roomId) {
        bookingformFragment formFragment = bookingformFragment.newInstance(status,checkin,checkout, price, name, name2, details, roomId);
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.mainframe, formFragment)
                .commit();
    }
}