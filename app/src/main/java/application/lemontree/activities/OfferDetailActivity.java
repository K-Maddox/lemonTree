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

    private void getOfferDetails(String offerId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("offers").document(offerId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Extract data
                        title = documentSnapshot.getString("offerName");
                        String description = documentSnapshot.getString("offerDescription");
                        String category = documentSnapshot.getString("offerCategory");
                        String availableDate = documentSnapshot.getString("offerAvailableDate");
                        String pickUpLocation = documentSnapshot.getString("offerPickUpLocation");
                        String imageUrl = documentSnapshot.getString("imageUrl");
                        Timestamp createdAt = documentSnapshot.getTimestamp("createdAt");
                        String createdById = documentSnapshot.getString("createdBy");
                        String createdByUsername = documentSnapshot.getString("createdByUsername");
                        String profilePictureUrl = documentSnapshot.getString("userProfileUrl");
                        String status = documentSnapshot.getString("status");

                        // Set data into views
                        offerTitleTextView.setText(title);
                        offerDescriptionTextView.setText(description);
                        categoryTextView.setText(category);
                        offerAvailableDateTextView.setText(availableDate);
                        offerPickUpLocationTextView.setText(pickUpLocation);

//                        if (createdAt != null) {
//                            createdAtTextView.setText(new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(createdAt.toDate()));
//                        }

                        // Load offer image
                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            Log.d("OfferDetailActivity", "Image" + imageUrl);
                            Glide.with(this)
                                    .load(imageUrl)
                                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                                    .into(offerImageView);
                        } else {
                            Log.e("OfferDetailActivity", "Image URL is null or empty");
                        }

                        // Set created by username
                        if (createdByUsername != null) {
                            offerUserId = createdById;
                            offerUserName = createdByUsername;
                            createdByUsernameTextView.setText("Offered by " + createdByUsername);
//                            chatButton.setText("Chat to " + createdByUsername);
                        }

                        // Check if the current user is the creator of the offer
                        if (currentUserId.equals(createdById)) {
//                            // Current user is the creator of the offer
//                            chatButton.setText("Open my messages");
//                            chatButton.setOnClickListener(v -> {
//                                // Open the user's own messages
//                                Intent intent = new Intent(OfferDetailActivity.this, MessageActivity.class);
//                                startActivity(intent);
//                            });
                            // Hide the entire offerCard if the current user is the creator
                            ConstraintLayout offerCard = findViewById(R.id.offerCard);
                            offerCard.setVisibility(View.GONE);

                            TextView offerDetailTitle = findViewById(R.id.offerDetailTitle);
                            offerDetailTitle.setText("My Offer Item Detail");

                            // Show archive button or archived status based on offer's status
                            if (status != null && status.equals("archive")) {
                                archiveButton.setVisibility(View.VISIBLE); // Hide the archive button
                                archiveButton.setText("Offer Archived");
                                // Disable the button
                                archiveButton.setEnabled(false);
                            } else {
                                archiveButton.setVisibility(View.VISIBLE); // Show the archive button
                            }

                            // Handle archive button click event
                            archiveButton.setOnClickListener(v -> {
                                // Create an AlertDialog to confirm the action
                                new AlertDialog.Builder(OfferDetailActivity.this)
                                        .setTitle("Archive Offer")
                                        .setMessage("Are you sure you want to archive this offer? It will no longer be visible to other users.")
                                        .setPositiveButton("Yes", (dialog, which) -> {
                                            // If the user confirms, archive the offer
                                            archiveOffer(offerId);
                                        })
                                        .setNegativeButton("No", (dialog, which) -> {
                                            // If the user cancels, just dismiss the dialog
                                            dialog.dismiss();
                                        })
                                        .show();
                            });

                        } else {
                            // Current user is not the creator of the offer
                            chatButton.setText("Chat to " + createdByUsername);
                            chatButton.setOnClickListener(v -> openChat());
                        }

                        // Load profile picture of uploader
                        if (profilePictureUrl != null && !profilePictureUrl.isEmpty()) {
                            offerUserURL = profilePictureUrl;
                            Glide.with(OfferDetailActivity.this)
                                    .load(profilePictureUrl)
                                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                                    .into(profileImageView);
                        }

                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("OfferDetailActivity", "Error fetching offer details", e);
                });
    }
}
