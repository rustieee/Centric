package com.finals.centric;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.finals.centric.databinding.ActivityForgotPassBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class forgotPassActivity extends AppCompatActivity {

    ActivityForgotPassBinding binding;
    FirebaseAuth auth;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityForgotPassBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance();
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        binding.resetConfirm.setOnClickListener(v -> {
            // Add button press animation
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

                        // Original logic moved inside the animation end action
                        String emailOrUsername = binding.resetEmail.getText().toString().trim();

                        if (emailOrUsername.isEmpty()) {
                            binding.resetEmail.setError("Please enter email or username");
                            return;
                        }

                        if (android.util.Patterns.EMAIL_ADDRESS.matcher(emailOrUsername).matches()) {
                            sendPasswordResetEmail(emailOrUsername);
                        } else {
                            db.collection("users")
                                    .whereEqualTo("username", emailOrUsername)
                                    .get()
                                    .addOnSuccessListener(queryDocumentSnapshots -> {
                                        if (!queryDocumentSnapshots.isEmpty()) {
                                            String email = queryDocumentSnapshots.getDocuments()
                                                    .get(0)
                                                    .getString("email");
                                            if (email != null) {
                                                sendPasswordResetEmail(email);
                                            }
                                        } else {
                                            db.collection("users")
                                                    .whereEqualTo("username", emailOrUsername.toLowerCase())
                                                    .get()
                                                    .addOnSuccessListener(lowerCaseSnapshots -> {
                                                        if (!lowerCaseSnapshots.isEmpty()) {
                                                            String email = lowerCaseSnapshots.getDocuments()
                                                                    .get(0)
                                                                    .getString("email");
                                                            if (email != null) {
                                                                sendPasswordResetEmail(email);
                                                            }
                                                        } else {
                                                            binding.resetEmail.setError("Username not found");
                                                        }
                                                    });
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(forgotPassActivity.this,
                                                "Error: " + e.getMessage(),
                                                Toast.LENGTH_SHORT).show();
                                    });
                        }
                    })
                    .start();
        });



    }

    private void sendPasswordResetEmail(String email) {
        // First check if email exists in Firestore
        db.collection("users")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Email exists, proceed with password reset
                        auth.sendPasswordResetEmail(email)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(forgotPassActivity.this,
                                                "Password reset email sent to " + email,
                                                Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(forgotPassActivity.this, MainActivity.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                });
                    } else {
                        // Email doesn't exist
                        binding.resetEmail.setError("Email not found");
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(forgotPassActivity.this,
                            "Error checking email: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }


}