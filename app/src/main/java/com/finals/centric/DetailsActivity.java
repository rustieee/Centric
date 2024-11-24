package com.finals.centric;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.finals.centric.databinding.ActivityDetailsBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;


public class DetailsActivity extends AppCompatActivity {
    ActivityDetailsBinding binding;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        binding.etbirthdate.setOnClickListener(v -> {setButtonAnimation(binding.etbirthdate, this::openDate);});
        binding.dateic.setOnClickListener(v -> {setButtonAnimation(binding.etbirthdate, this::openDate);});




        binding.signup.setOnClickListener(v -> {
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

                        if (validateInputs()) {
                            saveUserDetailsToFirestore();
                        }
                    })
                    .start();
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                FirebaseAuth auth = FirebaseAuth.getInstance();
                if (auth.getCurrentUser() != null) {
                    auth.getCurrentUser().delete()
                            .addOnCompleteListener(task -> {
                                Toast.makeText(DetailsActivity.this, "User deleted successfully.", Toast.LENGTH_SHORT).show();
                                finish();
                            });
                } else {
                    finish();
                }
            }
        });


    }

    private boolean validateInputs() {
        String firstName = binding.firstname.getText().toString();
        String lastName = binding.lastname.getText().toString();
        String phone = binding.etnumber.getText().toString();
        String birthdate = binding.etbirthdate.getText().toString();
        String username = binding.usernameEt.getText().toString();
        String address = binding.etastreet.getText().toString() + ", " +
                binding.etcity.getText().toString() + ", " +
                binding.etstate.getText().toString();

        // Validate that none of the required fields are empty
        if (firstName.isEmpty() || lastName.isEmpty() || phone.isEmpty() || birthdate.isEmpty() || username.isEmpty()) {
            Toast.makeText(this, "Please fill in all the fields", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Validate phone number (simple check to ensure it's digits and a reasonable length)
        if (!phone.matches("[0-9]+") || phone.length() < 10 || phone.length() > 15) {
            Toast.makeText(this, "Please enter a valid phone number", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void saveUserDetailsToFirestore() {
        String firstName = binding.firstname.getText().toString();
        String lastName = binding.lastname.getText().toString();
        String username = binding.usernameEt.getText().toString();
        String phone = binding.etnumber.getText().toString();
        String birthdate = binding.etbirthdate.getText().toString();
        String address = binding.etastreet.getText().toString() + ", " +
                binding.etcity.getText().toString() + ", " +
                binding.etstate.getText().toString();

        // Get current user from Firebase Auth
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Create a map to hold the user data
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("first_name", firstName);
        userMap.put("last_name", lastName);
        userMap.put("username", username);
        userMap.put("phone_number", phone);
        userMap.put("birthdate", birthdate);
        userMap.put("address", address);

        // Get Firestore instance and store the user data
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .document(userId) // Store under the user's UID
                .set(userMap)
                .addOnSuccessListener(aVoid -> {
                    // Successfully saved user data
                    Intent intent = new Intent(DetailsActivity.this, VerificationTwoActivity.class);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    // Handle error
                    Toast.makeText(DetailsActivity.this, "Error saving user details", Toast.LENGTH_SHORT).show();
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

    private void openDate() {
        DatePickerDialog dialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                binding.etbirthdate.setText((month+1) + "/" + day + "/" + year);
            }
        }, 2000, 0, 1);
        dialog.show();
    }
}