package com.finals.centric;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.finals.centric.databinding.ActivityVerificationTwoBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class VerificationTwoActivity extends AppCompatActivity {

    ActivityVerificationTwoBinding binding;
    FirebaseAuth auth;
    FirebaseUser user;

    private CountDownTimer countDownTimer;
    private static final int RESEND_TIME = 60000; // 60 seconds
    private static final int INTERVAL_TIME = 1000; // 1 second interval
    private boolean isTimerActive = false;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityVerificationTwoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        if (user != null) {
            user.sendEmailVerification()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Verification email sent", Toast.LENGTH_SHORT).show();
                        }
                    });
        }

        if (user != null) {
            binding.context.setText("We have sent a verification email to\n" + user.getEmail());
        }

        startResendTimer();

        binding.nocode.setOnClickListener(v -> {
            if (!isTimerActive) {
                resendVerificationEmail();
            }
        });

        binding.verify.setOnClickListener(v -> {
            v.animate()
                    .scaleX(0.9f)
                    .scaleY(0.9f)
                    .setDuration(100)
                    .withEndAction(() -> {
                        v.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
                        if (user != null) {
                            user.reload().addOnCompleteListener(task -> {
                                if (task.isSuccessful() && user.isEmailVerified()) {
                                    Intent intent = new Intent(this, ProfilePicActivity.class);
                                    startActivity(intent);
                                } else {
                                    Toast.makeText(this, "Please verify your email first.", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }).start();
        });

        // Handle back press using OnBackPressedDispatcher
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (user != null && !user.isEmailVerified()) {
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    db.collection("users").document(user.getUid())
                            .delete()
                            .addOnCompleteListener(task -> {
                                auth.getCurrentUser().delete();
                                Toast.makeText(VerificationTwoActivity.this, "User details deleted.", Toast.LENGTH_SHORT).show();
                                finish();
                            });
                } else {
                    finish();
                }
            }
        });
    }

    private void startResendTimer() {
        isTimerActive = true;
        countDownTimer = new CountDownTimer(RESEND_TIME, INTERVAL_TIME) {
            @Override
            public void onTick(long millisUntilFinished) {
                long secondsRemaining = millisUntilFinished / 1000;
                String text = "Didn't receive an email? Resend(" + secondsRemaining + ")";
                SpannableString spannable = new SpannableString(text);
                int startIndex = text.indexOf(String.valueOf(secondsRemaining));
                spannable.setSpan(new ForegroundColorSpan(ContextCompat.getColor(VerificationTwoActivity.this, android.R.color.holo_blue_light)), startIndex, text.length(), 0);
                binding.nocode.setText(spannable);
            }

            @Override
            public void onFinish() {
                binding.nocode.setText("Didn't receive an email? Resend");
                binding.nocode.setClickable(true);
                isTimerActive = false;
            }
        };
        countDownTimer.start();
    }

    private void resendVerificationEmail() {
        if (user != null) {
            user.sendEmailVerification()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Verification email sent!", Toast.LENGTH_SHORT).show();
                            startResendTimer();
                        }
                    });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}
