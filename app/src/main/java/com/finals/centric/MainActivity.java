package com.finals.centric;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;  // Import Glide
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.finals.centric.databinding.ActivityMainBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        mAuth = FirebaseAuth.getInstance();
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Load GIF using Glide and handle GIF loop directly
        Glide.with(this)
                .asGif()
                .load(R.drawable.login_pic1)
                .into(new CustomTarget<GifDrawable>() {
                    @Override
                    public void onResourceReady(GifDrawable resource, Transition<? super GifDrawable> transition) {
                        binding.imageView2.setImageDrawable(resource);
                        resource.setLoopCount(1);  // Play the GIF only once
                        resource.start();  // Start the GIF manually
                    }

                    @Override
                    public void onLoadCleared(Drawable placeholder) {
                        // Handle placeholder if needed
                    }
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
                    })
                    .start();
        });

        binding.login.setOnClickListener(v -> {
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
                        String email,password;
                        email = String.valueOf(binding.email.getText());
                        password = String.valueOf(binding.pass.getText());

                        if(email.isEmpty()){
                            Toast.makeText(this, "Please enter username", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if(password.isEmpty()){
                            Toast.makeText(this, "Please enter password", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        mAuth.signInWithEmailAndPassword(email, password)
                                .addOnCompleteListener(this, task -> {
                                    if (task.isSuccessful()) {
                                        Intent intent = new Intent(MainActivity.this, MainActivity2.class);
                                        startActivity(intent);
                                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                                        finish();
                                    } else {
                                        // If sign in fails, display a message to the user.
                                        Log.w(TAG, "signInWithEmail:failure", task.getException());
                                        Toast.makeText(MainActivity.this, "Authentication failed.",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                });

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
                        Intent intent = new Intent(this, CreateActivity.class);
                        startActivity(intent);
                    })
                    .start();
        });

        binding.forgotPass.setOnClickListener(v -> {
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
                        Intent intent = new Intent(this, forgotPassActivity.class);
                        startActivity(intent);
                    })
                    .start();
        });

    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            Intent intent = new Intent(MainActivity.this, MainActivity2.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        }
    }
}