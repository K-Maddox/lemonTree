package application.lemontree.activities;

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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import application.lemontree.R;
import application.lemontree.fragments.NavBarFragment;

public class OfferDetailActivity extends AppCompatActivity {

    private ImageView offerImageView;
    private TextView offerTitleTextView;
    private TextView offerDescriptionTextView;
    private TextView categoryTextView;
    private TextView offerAvailableDateTextView, offerPickUpLocationTextView;
    private TextView createdByUsernameTextView;
    private ImageView profileImageView;
    private Button chatButton;
    private Button archiveButton;
    private String currentUserId;
    private String title;
    private String offerUserId;
    private String offerUserName;
    private String offerUserURL;
    private String offerId;
    private static final String TAG = "inOfferDetailActivity";

    FirebaseAuth mAuth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.item_offer_in_detail);

        offerId = getIntent().getStringExtra("offerId");
        if (offerId == null) {
            Log.e(TAG, "offerId is null");
            finish();
            return;
        }

        // Initialize views
        offerImageView = findViewById(R.id.offerImageView);
        offerTitleTextView = findViewById(R.id.offerTitleTextView);
        offerDescriptionTextView = findViewById(R.id.offerDescriptionTextView);
        categoryTextView = findViewById(R.id.categoryTextView);
        offerAvailableDateTextView = findViewById(R.id.offerAvailableDateTextView);
        offerPickUpLocationTextView = findViewById(R.id.offerPickUpLocationTextView);
        createdByUsernameTextView = findViewById(R.id.createdByUsernameTextView);
        profileImageView = findViewById(R.id.profileImageView);
        chatButton = findViewById(R.id.chatButton);
        archiveButton = findViewById(R.id.archiveButton);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        // Check if the user is logged in
        if (currentUser == null) {
            Log.e("OfferDetailActivity", "No user is logged in");
            return;
        }

        currentUserId = currentUser.getUid();

        // Get the offer ID from the intent
        String offerId = getIntent().getStringExtra("offerId");

        if (offerId != null) {
            // Fetch and display offer details
            getOfferDetails(offerId);
        } else {
            Log.e("OfferDetailActivity", "No offer ID passed");
            finish();
        }

//        chatButton.setOnClickListener(v -> openChat());

        // use FragmentManager load NavBarFragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.nav_container, new NavBarFragment())
                .commit();
    }
}
