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

    private void getWantDetails(String wantId) {
        db.collection("wants").document(wantId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        title = documentSnapshot.getString("wantName");
                        String category = documentSnapshot.getString("wantCategory");
                        String description = documentSnapshot.getString("wantDescription");
                        String imageUrl = documentSnapshot.getString("imageUrl");
                        Timestamp createdAt = documentSnapshot.getTimestamp("createdAt");
                        String wantAvailableDate = documentSnapshot.getString("wantAvailableDate");
                        String createdById = documentSnapshot.getString("createdBy");
                        String createdByUsername = documentSnapshot.getString("createdByUsername");
                        String profilePictureUrl = documentSnapshot.getString("userProfileUrl");
                        String status = documentSnapshot.getString("status");

                        wantTitleTextView.setText(title);
                        wantDescriptionTextView.setText(description);
                        wantAvailableDateTextView.setText(wantAvailableDate);
                        categoryTextView.setText(category);
                        if (createdAt != null) {
                            createdAtTextView.setText(new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(createdAt.toDate()));
                        }

                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            Glide.with(this)
                                    .load(imageUrl)
                                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                                    .into(wantImageView);
                        }

                        createdByUsernameTextView.setText("Wanted by " + createdByUsername);
                        wantUserId = createdById;
                        wantUserName = createdByUsername;

                        if (currentUserId.equals(createdById)) {
//                            chatButton.setText("Open my messages");
//                            chatButton.setOnClickListener(v -> {
//                                Intent intent = new Intent(WantedDetailActivity.this, MessageActivity.class);
//                                startActivity(intent);
//                            });

                            ConstraintLayout offerCard = findViewById(R.id.offerCard);
                            offerCard.setVisibility(View.GONE);

                            TextView offerDetailTitle = findViewById(R.id.offerDetailTitle);
                            offerDetailTitle.setText("My Wanted Item Detail");

                            // Show archive button or archived status based on offer's status
                            if (status != null && status.equals("archive")) {
                                archiveButton.setVisibility(View.VISIBLE); // show the archive button
                                archiveButton.setText("Offer Archived");
                                // Disable the button
                                archiveButton.setEnabled(false);
                            } else {
                                archiveButton.setVisibility(View.VISIBLE); // Show the archive button
                            }

                            // Handle archive button click event
                            archiveButton.setOnClickListener(v -> {
                                // Create an AlertDialog to confirm the action
                                new AlertDialog.Builder(WantedDetailActivity.this)
                                        .setTitle("Archive Offer")
                                        .setMessage("Are you sure you want to archive this offer? It will no longer be visible to other users.")
                                        .setPositiveButton("Yes", (dialog, which) -> {
                                            // If the user confirms, archive the offer
                                            archiveOffer(wantId);
                                        })
                                        .setNegativeButton("No", (dialog, which) -> {
                                            // If the user cancels, just dismiss the dialog
                                            dialog.dismiss();
                                        })
                                        .show();
                            });

                        } else {
                            chatButton.setText("Chat to " + createdByUsername);
                            chatButton.setOnClickListener(v -> openChat());
                        }

                        if (profilePictureUrl != null && !profilePictureUrl.isEmpty()) {
                            wantUserURL = profilePictureUrl;
                            Glide.with(WantedDetailActivity.this)
                                    .load(profilePictureUrl)
                                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                                    .into(profileImageView);
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("WantedDetailActivity", "Error fetching want details", e));
    }

    private void archiveOffer(String offerId) {
        db.collection("wants").document(offerId)
                .update("status", "archive")
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Offer archived", Toast.LENGTH_SHORT).show();
                    archiveButton.setText("Offer Archived");
                    archiveButton.setEnabled(false);
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error archiving offer", e));
    }

    private void openChat() {
        checkIfChatExistsForWantAndUsers(wantId, currentUserId, wantUserId, (existingChatId) -> {
            if (existingChatId != null) {
                // Go to existing chat
                db.collection("profiles").document(currentUserId).get()
                        .addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.exists()) {
                                String currentUserProfileUrl = documentSnapshot.getString("profilePictureURL");
                                Intent intent = new Intent(WantedDetailActivity.this, ChatActivity.class);
                                intent.putExtra("chatId", existingChatId);
                                intent.putExtra("otherParticipantID", wantUserId);
                                intent.putExtra("otherParticipantName", wantUserName);
                                intent.putExtra("offerId", wantId);
                                intent.putExtra("myProfilePicture", currentUserProfileUrl);
                                startActivity(intent);
                            }
                        })
                        .addOnFailureListener(e -> {
                            Log.e("wanted", "Failed to retrieve current user profile", e);
                        });
            } else {
                createNewChat("Want: " + title + " by " + wantUserName, currentUserId, wantUserId,
                        wantUserName, wantId);
            }
        });
    }

    private void checkIfChatExistsForWantAndUsers(String wantId, String userId1, String userId2, ChatCheckCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        List<String> participants = Arrays.asList(userId1, userId2);
        Collections.sort(participants);

        db.collection("chats")
                .whereEqualTo("offerId", wantId)
                .whereArrayContains("participants", userId1)  // Check for both users in the participants list
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        for (DocumentSnapshot document : task.getResult()) {
                            List<String> storedParticipants = (List<String>) document.get("participants");
                            if (storedParticipants.contains(userId1) && storedParticipants.contains(userId2)) {
                                Log.d("WantedDetailActivity", "Chat exists!");
                                callback.onChatChecked(document.getId());
                                return;
                            }
                        }
                        Log.d("WantedDetailActivity", "No chat found.");
                        callback.onChatChecked(null);
                    } else {
                        Log.d("WantedDetailActivity", "Error checking for existing chat or no chat found.");
                        callback.onChatChecked(null);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("WantedDetailActivity", "Error checking chat", e);
                    callback.onChatChecked(null);
                });
    }

    private interface ChatCheckCallback {
        void onChatChecked(String chatId);
    }
}
