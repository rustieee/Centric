package com.finals.centric;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.finals.centric.databinding.FragmentProfileBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link profileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class profileFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

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

    public profileFragment() {
        // Required empty public constructor
    }

    public static profileFragment newInstance(String param1, String param2) {
        profileFragment fragment = new profileFragment();
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

    FragmentProfileBinding binding;
    FirebaseAuth auth;
    FirebaseFirestore db;

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        user = auth.getCurrentUser();

        // Fetch user data from Firestore
        db.collection("users").document(user.getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    // Fetch data from Firestore document
                    firstName = documentSnapshot.getString("first_name");
                    lastName = documentSnapshot.getString("last_name");
                    phoneNumber = documentSnapshot.getString("phone_number");
                    birthdate = documentSnapshot.getString("birthdate");
                    address = documentSnapshot.getString("address");
                    username = documentSnapshot.getString("username");
                    profilePicUrl = documentSnapshot.getString("profilePicUrl"); // Fetch profile picture URL

                    // Set the fetched data to views
                    binding.profileChangeName.setText(firstName + " " + lastName);
                    binding.profileChangeUserame.setText(username);
                    binding.profileChangeEmail.setText(user.getEmail());
                    binding.profileChangePhone.setText(phoneNumber);
                    binding.profileChangeBday.setText(birthdate);
                    if (address != null && !address.isEmpty()) {
                        String[] addressParts = address.split(",");
                        if (addressParts.length == 3) {
                            binding.profileChangeLocation.setText(addressParts[0].trim() + "\n" + addressParts[1].trim() + "," + addressParts[2].trim());
                        } else {
                            binding.profileChangeLocation.setText("");
                        }
                    } else {
                        binding.profileChangeLocation.setText("");
                    }

                    // Load profile picture using Glide
                    if (profilePicUrl != null) {
                        Glide.with(this)
                                .load(profilePicUrl)
                                .into(binding.profileChangebase); // Assuming you have an ImageView with this ID
                    }
                });

        setButtonAnimation(binding.profileChangebase, () -> {});
        setButtonAnimation(binding.imageView25, () -> {});
        setButtonAnimation(binding.logoutBtn, () -> {
            // Sign out the user
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(requireActivity(), MainActivity.class);
            startActivity(intent);
            requireActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            requireActivity().finish();
        });

        setButtonAnimation2(binding.nameBtn, () -> openProfileEditFragment("name"));
        setButtonAnimation2(binding.usernameBtn, () -> openProfileEditFragment("username"));
        setButtonAnimation2(binding.emailBtn, () -> openProfileEditFragment("email"));
        setButtonAnimation2(binding.phoneBtn, () -> openProfileEditFragment("phone"));
        setButtonAnimation2(binding.bdayBtn, () -> openProfileEditFragment("birthdate"));
        setButtonAnimation2(binding.locationBtn, () -> openProfileEditFragment("location"));

        setButtonAnimation(binding.bookHisBtn, () -> {});
        return binding.getRoot();
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
                                .withEndAction(onClickAction)
                                .start();
                    })
                    .start();
        });
    }

    private void setButtonAnimation2(View button, Runnable onClickAction) {
        button.setOnClickListener(v -> {
            v.animate()
                    .translationX(20f)
                    .setDuration(100)
                    .withEndAction(() -> {
                        v.animate()
                                .translationX(0f)
                                .setDuration(100)
                                .withEndAction(onClickAction)
                                .start();
                    })
                    .start();
        });
    }

    private void replaceFragment(Fragment fragment) {
        getParentFragmentManager().beginTransaction()
                .replace(R.id.mainframe, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void openProfileEditFragment(String type) {
        Bundle args = new Bundle();
        args.putString("editType", type);
        profileEditFragment editFragment = new profileEditFragment();
        editFragment.setArguments(args);
        replaceFragment(editFragment);
    }
}
