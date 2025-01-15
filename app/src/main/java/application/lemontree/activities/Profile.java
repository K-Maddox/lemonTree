package application.lemontree.activities;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.FragmentManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import application.lemontree.R;
import application.lemontree.fragments.NavBarFragment;

public class Profile extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private ImageView profileImageView;
    private TextView usernameTextView, emailTextView, cityTextView;

    private Button myOffersButton;
    private Button myWantedButton;
    private ImageButton settingsButton;

    // URL for default profile image in Firebase Storage
    private final String defaultProfilePictureUrl = "https://firebasestorage.googleapis.com/v0/b/lemontreedev-eef1e.appspot.com/o/ProfilePictures%2Fdefaultprofile.jpg?alt=media&token=70136e46-db11-405b-a541-a7fc5e501914";

    private static final int CAMERA_PERM_CODE = 101;
    private static final int REQUEST_IMAGE_CAPTURE = 102;
    private static final int REQUEST_GALLERY_CODE = 103;
    private String currentPhotoPath;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);  // Links to the XML layout for Profile

        // Log location
        Log.d("Profile Page", "Inside the profile page");

        // Initialize Firebase Auth and Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize views
        profileImageView = findViewById(R.id.profileImageView);
        usernameTextView = findViewById(R.id.usernameTextView);
        emailTextView = findViewById(R.id.emailTextView);
        cityTextView = findViewById(R.id.cityTextView);
        myOffersButton = findViewById(R.id.btn_my_offers);
        myWantedButton = findViewById(R.id.btn_my_wanted);
        settingsButton = findViewById(R.id.btn_settings);


        // Get the current logged-in user
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // Fetch user data from Firestore
            getUserProfile(currentUser.getUid());

        }


        // Set onClickListeners for the new buttons
        myOffersButton.setOnClickListener(v -> {
            Intent intent = new Intent(Profile.this, MyOffersActivity.class);
            startActivity(intent);
        });

        myWantedButton.setOnClickListener(v -> {
            Log.d("ProfileActivity", "My Wanted button was clicked");
            Intent intent = new Intent(Profile.this, MyWantedActivity.class);
            startActivity(intent);
        });

        settingsButton.setOnClickListener(v -> {
            Log.d("ProfileActivity", "Settings button was clicked");
            Intent intent = new Intent(Profile.this, SettingsActivity.class);
            startActivity(intent);
        });

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.nav_container, new NavBarFragment())
                .commit();

        // Set click listener for profile image
        profileImageView.setOnClickListener(v -> {
            new AlertDialog.Builder(Profile.this)
                    .setTitle("Edit Image")
                    .setMessage("Would you like to change your image?")
                    .setPositiveButton("Yes", (dialog, which) -> askCameraPermission())
                    .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                    .show();
        });
    }

    private void getUserProfile(String userId) {
        db.collection("profiles").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Get the user data and log it
                        String username = documentSnapshot.getString("username");
                        String email = documentSnapshot.getString("email");
                        String imageUrl = documentSnapshot.getString("profilePictureURL");

                        Log.d("Profile Page", "Username: " + username);
                        Log.d("Profile Page", "Email: " + email);
                        Log.d("Profile Page", "Image: " + imageUrl);

                        // Extract the location (latitude, longitude)
                        GeoPoint lastLocation = documentSnapshot.getGeoPoint("lastLocation");
                        if (lastLocation != null) {
                            double latitude = lastLocation.getLatitude();
                            double longitude = lastLocation.getLongitude();

                            Log.d("Profile Page", "Long and lat: " + latitude + " " + longitude);

                            // Get nearest city using the Geocoder
                            findNearestCity(latitude, longitude);
                        }

                        // Set the user info to the views
                        usernameTextView.setText(username);
                        emailTextView.setText(email);

                        // Load the profile picture with Glide
                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            Glide.with(this)
                                    .load(imageUrl)
                                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                                    .error(Glide.with(this).load(defaultProfilePictureUrl))  // Default image if URL is invalid
                                    .into(profileImageView);
                        } else {
                            // Load default profile picture from Firebase Storage if no URL provided
                            Glide.with(this)
                                    .load(defaultProfilePictureUrl)
                                    .into(profileImageView);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("ProfileActivity", "Error fetching profile", e);
                });
    }

    private void askCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
            }, CAMERA_PERM_CODE);
        } else {
            showImagePickerOptions();
        }
    }

    private void showImagePickerOptions() {
        CharSequence[] options = {"Take Photo", "Choose from Gallery", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose an option");
        builder.setItems(options, (dialog, which) -> {
            switch (which) {
                case 0:
                    dispatchTakePictureIntent();
                    break;
                case 1:
                    Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(gallery, REQUEST_GALLERY_CODE);
                    break;
                default:
                    dialog.dismiss();
            }
        });
        builder.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERM_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showImagePickerOptions();
            } else {
                Toast.makeText(this, "Camera Permission is Required to Use Camera.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            File f = new File(currentPhotoPath);
            imageUri = Uri.fromFile(f);
            profileImageView.setImageURI(imageUri);
            uploadProfilePictureToFirebase(imageUri);
        }

        if (requestCode == REQUEST_GALLERY_CODE && resultCode == Activity.RESULT_OK && data != null) {
            imageUri = data.getData();
            profileImageView.setImageURI(imageUri);
            uploadProfilePictureToFirebase(imageUri);
        }
    }

    private void uploadProfilePictureToFirebase(Uri uri) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }
        String userId = user.getUid();

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        StorageReference imageRef = storageRef.child("ProfilePictures/" + userId + "/" + timeStamp + "." + getFileExt(uri));

        UploadTask uploadTask = imageRef.putFile(uri);

        uploadTask.addOnFailureListener(exception -> {
            Toast.makeText(this, "Profile picture upload failed", Toast.LENGTH_SHORT).show();
        }).addOnSuccessListener(taskSnapshot -> {
            imageRef.getDownloadUrl().addOnSuccessListener(uriResult -> {
                String imageUrl = uriResult.toString();

                DocumentReference userRef = db.collection("profiles").document(userId);
                userRef.update("profilePictureURL", imageUrl)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "Profile picture updated successfully", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Error updating profile picture URL", Toast.LENGTH_SHORT).show();
                        });
            });
        });
    }

    private String getFileExt(Uri uri) {
        if (uri == null) return null;
        ContentResolver c = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(c.getType(uri));
    }
}
