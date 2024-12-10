package com.finals.centric;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.finals.centric.databinding.ActivityCreateBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;


public class CreateActivity extends AppCompatActivity {

    ActivityCreateBinding binding;
    FirebaseAuth mAuth;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            Intent intent = new Intent(this, DetailsActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityCreateBinding.inflate(getLayoutInflater());
        mAuth = FirebaseAuth.getInstance();
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        binding.buttong.setOnClickListener(v -> {
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
                        Intent intent = new Intent(this, DetailsActivity.class);
                        startActivity(intent);
                    })
                    .start();
        });

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

                        // Start the new activity after the animation finishes
                        String email = String.valueOf(binding.email.getText());
                        String password = String.valueOf(binding.pass.getText());

                        // Validate email and password fields
                        if (email.isEmpty()) {
                            Toast.makeText(this, "Please enter an email", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Validate email format
                        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                            Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (password.isEmpty()) {
                            Toast.makeText(this, "Please enter a password", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Validate password length
                        if (password.length() < 6) {
                            Toast.makeText(this, "Password should be at least 6 characters", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        mAuth.createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener(this, task -> {
                                    if (task.isSuccessful()) {
                                        FirebaseUser user = mAuth.getCurrentUser();
                                        // Create user document in Firestore
                                        Map<String, Object> userData = new HashMap<>();
                                        userData.put("email", email);

                                        db.collection("users")
                                                .document(user.getUid())
                                                .set(userData)
                                                .addOnSuccessListener(aVoid -> {
                                                    Intent intent = new Intent(CreateActivity.this, DetailsActivity.class);
                                                    startActivity(intent);
                                                    finish();
                                                })
                                                .addOnFailureListener(e -> {
                                                    Toast.makeText(CreateActivity.this,
                                                            "Error creating user profile: " + e.getMessage(),
                                                            Toast.LENGTH_SHORT).show();
                                                });
                                    } else {
                                        Toast.makeText(CreateActivity.this,
                                                "Authentication failed: " + task.getException().getMessage(),
                                                Toast.LENGTH_SHORT).show();
                                    }
                                });

                    })
                    .start();
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Intent intent = new Intent(CreateActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        });
    }
}