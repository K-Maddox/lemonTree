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

    private void archiveOffer(String offerId) {
        db.collection("offers").document(offerId)
                .update("status", "archive")
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Offer archived", Toast.LENGTH_SHORT).show();
                    archiveButton.setText("Offer Archived");
                    archiveButton.setEnabled(false);
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error archiving offer", e));
    }

    private void openChat() {
        Log.d(TAG, "Attempting to open chat for offer: " + offerId);
        checkIfChatExistsForOfferAndUsers(offerId, currentUserId, offerUserId, (existingChatId) -> {
            if (existingChatId != null) {
                Log.d(TAG, "Chat already exists. Opening existing chat.");
                openExistingChat(existingChatId);
            } else {
                Log.d(TAG, "No chat found. Creating a new one.");
                createNewChat("Offer: " + title + " by " + offerUserName, currentUserId, offerUserId, offerUserName, offerId);
            }
        });
    }

    private void openExistingChat(String existingChatId) {
        // Retrieve current user's profile picture from Firestore
        db.collection("profiles").document(currentUserId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String currentUserProfileUrl = documentSnapshot.getString("profilePictureURL");

                        Intent intent = new Intent(OfferDetailActivity.this, ChatActivity.class);
                        intent.putExtra("chatId", existingChatId);
                        intent.putExtra("otherParticipantID", offerUserId);
                        intent.putExtra("otherParticipantName", offerUserName);
                        intent.putExtra("offerId", offerId);
                        intent.putExtra("myProfilePicture", currentUserProfileUrl);
                        intent.putExtra("otherProfilePicture", offerUserURL);
                        startActivity(intent);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to retrieve current user profile", e);
                });
    }

    // check if this offer already has chat
    private void checkIfChatExistsForOfferAndUsers(String offerId, String userId1, String userId2, ChatCheckCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Ensure participants are sorted
        List<String> participants = Arrays.asList(userId1, userId2);
        Collections.sort(participants);

        Log.d(TAG, "Checking chat for offerId: " + offerId + ", participants: " + participants);

        // Check Firestore for an existing chat associated with this offerId and participants
        db.collection("chats")
                .whereEqualTo("offerId", offerId)
                .whereArrayContains("participants", userId1)  // You can use whereArrayContains for both participants
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        for (DocumentSnapshot document : task.getResult()) {
                            List<String> storedParticipants = (List<String>) document.get("participants");
                            if (storedParticipants.contains(userId1) && storedParticipants.contains(userId2)) {
                                Log.d(TAG, "Chat exists!");
                                callback.onChatChecked(document.getId());
                                return;
                            }
                        }
                        Log.d(TAG, "No chat found.");
                        callback.onChatChecked(null);
                    } else {
                        Log.d(TAG, "No chat found, creating a new one.");
                        callback.onChatChecked(null);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error checking chat", e);
                    callback.onChatChecked(null);
                });
    }


    // Callback interface to handle the result of checking for an existing chat
    private interface ChatCheckCallback {
        void onChatChecked(String chatId);
    }


    /**
     * Create a new chat between two users based on an offer
     *
     * @param title
     * @param currentUserId
     * @param createdByUserId
     * @param createdByUsername
     * @param offerId
     */
    private void createNewChat(String title, String currentUserId, String createdByUserId,
                               String createdByUsername, String offerId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // First, retrieve current user's details from Firestore
        db.collection("profiles").document(currentUserId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String currentUserName = documentSnapshot.getString("username");
                        String currentUserProfileUrl = documentSnapshot.getString("profilePictureURL");

                        List<String> participants = Arrays.asList(currentUserId, createdByUserId);
                        List<String> participantNames = Arrays.asList(currentUserName, createdByUsername);
                        List<String> profilePictureURLs = Arrays.asList(currentUserProfileUrl, offerUserURL);

                        // Now that we have the details, proceed to create the new chat
                        Map<String, Object> chatData = new HashMap<>();
                        chatData.put("title", title);  // Empty initial message
                        chatData.put("lastMessage", "");  // Empty initial message
                        chatData.put("lastMessageTimestamp", FieldValue.serverTimestamp());
                        chatData.put("participants", participants);
                        chatData.put("participantsNames", participantNames);
                        chatData.put("profilePictureURLs", profilePictureURLs);
                        chatData.put("offerId", offerId);  // Associate the chat with the offer

                        Log.d(TAG, "Participants " + participants);
                        Log.d(TAG, "User names " + Arrays.asList(currentUserName, createdByUsername));
                        Log.d(TAG, "User pictures " + Arrays.asList(currentUserProfileUrl, offerUserURL));
                        Log.d(TAG, "Offer ID " + offerId);

                        // Create the chat in Firestore
                        db.collection("chats").add(chatData)
                                .addOnSuccessListener(documentReference -> {
                                    String newChatId = documentReference.getId();

                                    // Launch ChatActivity with the newly created chatId
                                    Intent intent = new Intent(OfferDetailActivity.this, ChatActivity.class);
                                    intent.putExtra("chatId", newChatId);
                                    intent.putExtra("offerId", offerId);
                                    intent.putExtra("otherParticipantID", createdByUserId);
                                    intent.putExtra("otherParticipantName", createdByUsername);
                                    intent.putExtra("myProfilePicture", currentUserProfileUrl);
                                    startActivity(intent);
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("OfferDetailActivity", "Error creating new chat", e);
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("OfferDetailActivity", "Failed to retrieve current user profile", e);
                });
    }
}
