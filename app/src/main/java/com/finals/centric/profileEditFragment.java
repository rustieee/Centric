package com.finals.centric;

import android.app.DatePickerDialog;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.finals.centric.databinding.FragmentProfileEditBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link profileEditFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class profileEditFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private FirebaseUser user;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String birthdate;
    private String address;
    private String username;
    private String profilePicUrl; // Variable to hold the profile picture URL

    public profileEditFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment profileEditFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static profileEditFragment newInstance(String param1, String param2) {
        profileEditFragment fragment = new profileEditFragment();
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

    FragmentProfileEditBinding binding;
    FirebaseAuth auth;
    FirebaseFirestore db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentProfileEditBinding.inflate(inflater, container, false);
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        user = auth.getCurrentUser();
        String editType = getArguments() != null ? getArguments().getString("editType") : "";
        updateUI(editType);
        fetchUserData();

        binding.newField4Calendar.setOnClickListener(v -> {setButtonAnimation(binding.newField4Calendar, this::openDate);});
        binding.newField4CalendarIc.setOnClickListener(v -> {setButtonAnimation(binding.newField4CalendarIc, this::openDate);});
        setButtonAnimation(binding.profileEditSaveBtn, () -> {
            String updatedValue1 = binding.newField1Base.getText().toString().trim();
            String updatedValue2 = binding.newField2Base.getText().toString().trim();
            String updatedValue3 = binding.newField3Base.getText().toString().trim();
            String updatedValue4 = binding.newField4Calendar.getText().toString().trim(); // For birthdate or calendar

            // Perform validation based on the editType
            switch (editType) {
                case "name":
                    // Validation for First and Last Name
                    if (updatedValue1.isEmpty() || updatedValue2.isEmpty()) {
                        Toast.makeText(requireContext(), "Please enter both First Name and Last Name.", Toast.LENGTH_SHORT).show();
                        return; // Stop further execution if validation fails
                    }
                    break;

                case "username":
                    // Validation for Username
                    if (updatedValue1.isEmpty()) {
                        Toast.makeText(requireContext(), "Please enter a Username.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    break;

                case "email":
                    // Validation for Email
                    if (updatedValue1.isEmpty() || !updatedValue1.contains("@")) {
                        Toast.makeText(requireContext(), "Please enter a valid Email Address.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    break;

                case "phone":
                    // Validation for Phone Number
                    if (updatedValue1.isEmpty()) {
                        Toast.makeText(requireContext(), "Please enter a Phone Number.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    break;

                case "birthdate":
                    // Validation for Birthdate
                    if (updatedValue4.isEmpty()) {
                        Toast.makeText(requireContext(), "Please select a Birthdate.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    break;

                case "location":
                    // Validation for Location (Address)
                    if (updatedValue1.isEmpty() || updatedValue2.isEmpty() || updatedValue3.isEmpty()) {
                        Toast.makeText(requireContext(), "Please enter all address fields (Street, City, Province).", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    break;

                default:
                    break;
            }

            // After validation, update Firestore data based on editType
            updateUserData(editType, updatedValue1, updatedValue2, updatedValue3, updatedValue4);
            replaceFragment(new profileFragment()); // Navigate back to the profile fragment
        });

        return binding.getRoot();
    }

    private void updateUserData(String editType, String updatedValue1, String updatedValue2, String updatedValue3, String updatedValue4) {
        if (user != null) {
            // Get reference to the user's Firestore document
            DocumentReference userRef = db.collection("users").document(user.getUid());

            switch (editType) {
                case "name":
                    // Update First Name and Last Name
                    userRef.update("first_name", updatedValue1, "last_name", updatedValue2)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(requireContext(), "Name updated successfully.", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(requireContext(), "Failed to update Name.", Toast.LENGTH_SHORT).show();
                            });
                    break;

                case "username":
                    // Update Username
                    userRef.update("username", updatedValue1)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(requireContext(), "Username updated successfully.", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(requireContext(), "Failed to update Username.", Toast.LENGTH_SHORT).show();
                            });
                    break;

                case "email":
                    // Update Email
                    userRef.update("email", updatedValue1)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(requireContext(), "Email updated successfully.", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(requireContext(), "Failed to update Email.", Toast.LENGTH_SHORT).show();
                            });
                    break;

                case "phone":
                    // Update Phone Number
                    userRef.update("phone_number", updatedValue1)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(requireContext(), "Phone Number updated successfully.", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(requireContext(), "Failed to update Phone Number.", Toast.LENGTH_SHORT).show();
                            });
                    break;

                case "birthdate":
                    // Update Birthdate
                    userRef.update("birthdate", updatedValue4)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(requireContext(), "Birthdate updated successfully.", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(requireContext(), "Failed to update Birthdate.", Toast.LENGTH_SHORT).show();
                            });
                    break;

                case "location":
                    // Update Location (Street, City, Province)
                    String newAddress = updatedValue1 + ", " + updatedValue2 + ", " + updatedValue3;
                    userRef.update("address", newAddress)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(requireContext(), "Address updated successfully.", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(requireContext(), "Failed to update Address.", Toast.LENGTH_SHORT).show();
                            });
                    break;

                default:
                    break;
            }
        }
    }

    private void fetchUserData() {
        if (user != null) {
            // Get the user document from Firestore using UID
            db.collection("users").document(user.getUid()).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            // Fetch data from Firestore document
                            firstName = documentSnapshot.getString("first_name");
                            lastName = documentSnapshot.getString("last_name");
                            phoneNumber = documentSnapshot.getString("phone_number");
                            birthdate = documentSnapshot.getString("birthdate");
                            address = documentSnapshot.getString("address");
                            username = documentSnapshot.getString("username");
                            profilePicUrl = documentSnapshot.getString("profilePicUrl"); // Fetch profile picture URL

                            // Load profile picture using Glide
                            if (profilePicUrl != null) {
                                Glide.with(this)
                                        .load(profilePicUrl)
                                        .into(binding.profileChangebase); // Assuming you have an ImageView with this ID
                            }

                            // Update UI with fetched data
                            updateUI(getArguments() != null ? getArguments().getString("editType") : "");
                        } else {
                            // Handle the case where the document does not exist
                        }
                    })
                    .addOnFailureListener(e -> {
                        // Handle failure to fetch user data
                        e.printStackTrace();
                    });
        }
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

    private void openDate() {
        DatePickerDialog dialog = new DatePickerDialog(requireContext(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                binding.newField4Calendar.setText((month + 1) + "/" + day + "/" + year);
            }
        }, 2000, 0, 1);
        dialog.show();
    }

    private void updateUI(String editType) {
        // Elements to update
        binding.currentfield2.setVisibility(View.GONE);
        binding.currentfield3.setVisibility(View.GONE);
        binding.newfield2.setVisibility(View.GONE);
        binding.newfield3.setVisibility(View.GONE);
        binding.newfield4.setVisibility(View.GONE);

        switch (editType) {
            case "name":
                binding.profileEditTitle.setText("Change Name");
                binding.currentTitle.setText("Old Name");
                binding.newTitle.setText("New Name");
                binding.currentfield2.setVisibility(View.VISIBLE);
                binding.currentField1Base.setText(firstName);
                binding.currentField2Base.setText(lastName);
                binding.newField1Base.setHint("Enter First Name");
                binding.newField2Base.setHint("Enter Last Name");
                binding.newfield2.setVisibility(View.VISIBLE);
                binding.newField1Base.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
                break;

            case "username":
                binding.profileEditTitle.setText("Change Username");
                binding.currentTitle.setText("Old Username");
                binding.currentField1Base.setText(username);
                binding.newTitle.setText("New Username");
                binding.newField1Base.setHint("Enter Username");
                binding.newField1Base.setInputType(InputType.TYPE_CLASS_TEXT);
                break;

            case "email":
                binding.profileEditTitle.setText("Change Email Address");
                binding.currentTitle.setText("Old Email Address");
                binding.newTitle.setText("New Email Address");
                binding.currentField1Base.setText(user.getEmail());
                binding.newField1Base.setHint("Enter Email Address");
                binding.newField1Base.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                break;

            case "phone":
                binding.profileEditTitle.setText("Change Phone Number");
                binding.currentTitle.setText("Old Phone Number");
                binding.currentField1Base.setText(phoneNumber);
                binding.newTitle.setText("New Phone Number");
                binding.newField1Base.setHint("Enter Phone Number");
                binding.newField1Base.setInputType(InputType.TYPE_CLASS_PHONE);
                break;

            case "birthdate":
                binding.profileEditTitle.setText("Change Birthdate");
                binding.currentTitle.setText("Old Birthdate");
                binding.currentField1Base.setText(birthdate);
                binding.newTitle.setText("New Birthdate");
                binding.newfield1.setVisibility(View.GONE);
                binding.newfield2.setVisibility(View.GONE);
                binding.newfield3.setVisibility(View.GONE);
                binding.newfield4.setVisibility(View.VISIBLE);
                break;

            case "location":
                binding.profileEditTitle.setText("Change Address");
                binding.currentTitle.setText("Old Address");
                if (address != null && !address.isEmpty()) {
                    String[] addressParts = address.split(",");

                    // Ensure the address has exactly 3 parts (Street, City, Province)
                    if (addressParts.length == 3) {
                        binding.currentField1Base.setText(addressParts[0].trim()); // Street
                        binding.currentField2Base.setText(addressParts[1].trim()); // City
                        binding.currentField3Base.setText(addressParts[2].trim()); // Province
                    } else {
                        // Optionally, you can set default values or leave fields blank
                        binding.currentField1Base.setText("");
                        binding.currentField2Base.setText("");
                        binding.currentField3Base.setText("");
                    }
                } else {
                    // Handle the case when the address is null or empty
                    binding.currentField1Base.setText("");
                    binding.currentField2Base.setText("");
                    binding.currentField3Base.setText("");
                }
                binding.newTitle.setText("New Address");
                binding.newField1Base.setHint("Enter Street");
                binding.newField2Base.setHint("Enter City");
                binding.newField3Base.setHint("Enter Province");
                binding.currentfield1.setVisibility(View.VISIBLE);
                binding.currentfield2.setVisibility(View.VISIBLE);
                binding.currentfield3.setVisibility(View.VISIBLE);
                binding.newfield2.setVisibility(View.VISIBLE);
                binding.newfield3.setVisibility(View.VISIBLE);
                binding.newField1Base.setInputType(InputType.TYPE_CLASS_TEXT);
                binding.newField2Base.setInputType(InputType.TYPE_CLASS_TEXT);
                binding.newField3Base.setInputType(InputType.TYPE_CLASS_TEXT);
                break;

            default:
                // Handle invalid type (optional)
                break;
        }
    }
}