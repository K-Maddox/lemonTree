package application.lemontree.activities;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import application.lemontree.R;
import application.lemontree.fragments.NavBarFragment;

public class WantedDetailActivity extends AppCompatActivity {
    private ImageView wantImageView;
    private TextView wantTitleTextView;
    private TextView wantDescriptionTextView;
    private TextView createdAtTextView;
    private TextView categoryTextView;
    private TextView createdByUsernameTextView, wantAvailableDateTextView;
    private TextView archivedTextView;
    private ImageView profileImageView;
    private Button chatButton;
    private Button archiveButton;
    private String currentUserId;
    private String title;
    private String wantUserId;
    private String wantUserName;
    private String wantUserURL;
    private String wantId;

    FirebaseAuth mAuth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.item_wanted_in_detail);

        wantId = getIntent().getStringExtra("wantId");
        if (wantId == null) {
            Log.e("WantedDetailActivity", "wantId is null");
            finish();
            return;
        }

        // Initialize views
        wantImageView = findViewById(R.id.wantedImageView);
        wantTitleTextView = findViewById(R.id.wantedTitleTextView);
        categoryTextView = findViewById(R.id.categoryTextView);
        wantDescriptionTextView = findViewById(R.id.wantedDescriptionTextView);
        wantAvailableDateTextView = findViewById(R.id.availableDateTextView);
        createdAtTextView = findViewById(R.id.createdAtTextView);
        createdByUsernameTextView = findViewById(R.id.createdByUsernameTextView);
        profileImageView = findViewById(R.id.profileImageView);
        chatButton = findViewById(R.id.chatButton);
        archiveButton = findViewById(R.id.archiveButton);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            Log.e("WantedDetailActivity", "No user is logged in");
            return;
        }

        currentUserId = currentUser.getUid();
        getWantDetails(wantId);

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.nav_container, new NavBarFragment())
                .commit();
    }
}
