package com.erkindilekci.javasharingbook.view;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.erkindilekci.javasharingbook.databinding.ActivityUploadBinding;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.UUID;

public class UploadActivity extends AppCompatActivity {

    private ActivityUploadBinding binding;

    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;
    private FirebaseStorage storage;
    private StorageReference storageReference;

    private ActivityResultLauncher<String> permissionActivityResultLauncher;
    private ActivityResultLauncher<Intent> galleryActivityResultLauncher;
    private Uri selectedImageUri;

    String permissionString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUploadBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionString = android.Manifest.permission.READ_MEDIA_IMAGES;
        } else {
            permissionString = android.Manifest.permission.READ_EXTERNAL_STORAGE;
        }

        registerLauncher();

        binding.imageView.setOnClickListener(v -> handleImageClick());

        binding.btUpload.setOnClickListener(v -> handleButtonClick());
    }

    private void handleImageClick() {
        if (ContextCompat.checkSelfPermission(this, permissionString) == PackageManager.PERMISSION_GRANTED) {
            Intent intentToTheGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            galleryActivityResultLauncher.launch(intentToTheGallery);
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permissionString)) {
                Snackbar.make(binding.getRoot(), "Gallery permission needed to select an art image", Snackbar.LENGTH_INDEFINITE)
                        .setAction("Give Permission", v -> {
                            permissionActivityResultLauncher.launch(permissionString);
                        })
                        .show();
            } else {
                permissionActivityResultLauncher.launch(permissionString);
            }
        }
    }

    private void handleButtonClick() {
        String comment = binding.etComment.getText().toString().strip();

        if (comment.isBlank() || selectedImageUri == null) {
            Toast.makeText(this, "Field(s) can't be empty", Toast.LENGTH_SHORT).show();
        } else {
            binding.progressBar.setVisibility(View.VISIBLE);
            binding.imageView.setVisibility(View.INVISIBLE);
            binding.commentTextInput.setVisibility(View.INVISIBLE);
            binding.btUpload.setVisibility(View.INVISIBLE);

            UUID uuid = UUID.randomUUID();
            String imagePath = "/images/" + uuid + ".jpg";

            storageReference.child(imagePath).putFile(selectedImageUri)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            StorageReference imageReference = storage.getReference(imagePath);
                            imageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                                String userEmail = mAuth.getCurrentUser().getEmail();

                                HashMap<String, Object> postData = new HashMap<>();
                                postData.put("userEmail", userEmail);
                                postData.put("downloadUrl", uri.toString());
                                postData.put("comment", comment);
                                postData.put("date", FieldValue.serverTimestamp());

                                Task<DocumentReference> postTask = firestore.collection("Posts").add(postData);

                                postTask.addOnSuccessListener(documentReference -> {
                                    Toast.makeText(getApplicationContext(), "Post successfully uploaded to database.", Toast.LENGTH_SHORT).show();

                                    Intent intent = new Intent(this, FeedActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(intent);
                                });

                                postTask.addOnFailureListener(e -> {
                                    Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();

                                    binding.progressBar.setVisibility(View.GONE);
                                    binding.imageView.setVisibility(View.VISIBLE);
                                    binding.etComment.setVisibility(View.VISIBLE);
                                    binding.btUpload.setVisibility(View.VISIBLE);
                                });
                            });
                        } else {
                            Toast.makeText(this, task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void registerLauncher() {
        galleryActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), activityResult -> {
            if (activityResult.getResultCode() == RESULT_OK) {
                Intent intentFromResult = activityResult.getData();
                if (intentFromResult != null) {
                    Uri imageUri = intentFromResult.getData();
                    if (imageUri != null) {
                        selectedImageUri = imageUri;
                        binding.imageView.setImageURI(imageUri);
                    }
                }
            }
        });

        permissionActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                Intent intentToTheGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                galleryActivityResultLauncher.launch(intentToTheGallery);
            } else {
                Toast.makeText(this, "Permission needed!", Toast.LENGTH_LONG).show();
            }
        });
    }
}