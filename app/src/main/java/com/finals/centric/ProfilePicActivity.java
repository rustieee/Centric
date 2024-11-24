package com.finals.centric;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.finals.centric.databinding.ActivityProfilePicBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;

public class ProfilePicActivity extends AppCompatActivity {

    ActivityProfilePicBinding binding;
    Bitmap selectedBitmap;
    FirebaseAuth auth;
    FirebaseUser currentUser;
    FirebaseStorage storage;
    FirebaseFirestore db;
    StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfilePicBinding.inflate(getLayoutInflater());
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        storage = FirebaseStorage.getInstance();
        db = FirebaseFirestore.getInstance();
        storageReference = storage.getReference();
        setContentView(binding.getRoot());
        binding.imageView8.setImageResource(R.drawable.profile_pic);

        binding.complete.setOnClickListener(v -> {
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

                        // Proceed to the next activity regardless of the selectedBitmap
                        if (selectedBitmap != null) {
                            uploadProfilePicture(selectedBitmap); // Upload the selected Bitmap
                        } else {
                            // If no picture is selected, just navigate to the next activity
                            Intent intent = new Intent(ProfilePicActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    })
                    .start();
        });

        // Show dialog for gallery or camera when "Choose" is clicked
        binding.choose.setOnClickListener(v -> {
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
                        showImageSourceDialog(); // Show dialog with options for gallery or camera
                    })
                    .start();
        });
    }

    // Show dialog with options to choose between gallery or camera
    private void showImageSourceDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Image Source")
                .setItems(new CharSequence[]{"Gallery", "Camera"}, (DialogInterface.OnClickListener) (dialog, which) -> {
                    if (which == 0) {
                        // Gallery option selected
                        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        galleryLauncher.launch(galleryIntent);
                    } else if (which == 1) {
                        // Camera option selected
                        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        cameraLauncher.launch(cameraIntent);
                    }
                });
        builder.show();
    }

    // Launch gallery for image selection
    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri selectedImageUri = result.getData().getData();
                    try {
                        selectedBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                        binding.imageView8.setImageBitmap(selectedBitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });

    // Launch camera to take a picture
    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedBitmap = (Bitmap) result.getData().getExtras().get("data");
                    binding.imageView8.setImageBitmap(selectedBitmap);
                }
            });

    // Upload Profile Picture to Firebase Storage
    private void uploadProfilePicture(Bitmap bitmap) {
        if (bitmap != null) {
            // Convert Bitmap to ByteArray
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();

            String fileName = UUID.randomUUID().toString(); // Unique file name
            StorageReference profilePicRef = storageReference.child("profile_pics/" + currentUser.getUid() + "/" + fileName);

            profilePicRef.putBytes(data)
                    .addOnSuccessListener(taskSnapshot -> {
                        // Get the download URL
                        profilePicRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            String imageUrl = uri.toString();
                            saveImageUrlToFirestore(imageUrl);  // Save URL in Firestore
                        });
                    })
                    .addOnFailureListener(e -> {
                        Log.e("ProfilePicActivity", "Upload failed", e);
                        Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(this, "Please select a profile picture first", Toast.LENGTH_SHORT).show();
        }
    }

    // Save Image URL to Firestore
    private void saveImageUrlToFirestore(String imageUrl) {
        db.collection("users").document(currentUser.getUid())
                .update("profilePicUrl", imageUrl)  // Update the profilePicUrl field
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Profile picture uploaded successfully", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(ProfilePicActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to save image URL", Toast.LENGTH_SHORT).show());
    }
}
