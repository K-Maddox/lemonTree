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

}
