package application.lemontree.activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

import application.lemontree.R;
import application.lemontree.adapter.ChatMessageAdapter;
import application.lemontree.models.ChatMessage;

public class ChatActivity extends AppCompatActivity {
    private static final String TAG = "inChatActivity";
    private RecyclerView recyclerView;
    private ChatMessageAdapter chatMessageAdapter;
    private List<ChatMessage> chatMessageList;

    private String chatId;
    private String currentUserId;
    private String otherUserId;
    private String myProfilePicture;
    private String otherProfilePicture;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private EditText editTextMessage;
    private Button buttonSend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        chatId = getIntent().getStringExtra("chatId");
        String offerId = getIntent().getStringExtra("offerId");
        if (offerId == null) {
            Log.e(TAG, "offerId is null");
            finish();
            return;
        }
        Log.d(TAG, "Received offerId: " + offerId);
        currentUserId = auth.getCurrentUser().getUid();
        otherUserId = getIntent().getStringExtra("otherParticipantID");
        myProfilePicture = getIntent().getStringExtra("myProfilePicture");
        otherProfilePicture = getIntent().getStringExtra("otherProfilePicture");

        // init RecyclerView
        recyclerView = findViewById(R.id.recyclerViewChatMessages);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // init chat message and adapter
        chatMessageList = new ArrayList<>();
        chatMessageAdapter = new ChatMessageAdapter(chatMessageList, currentUserId);
        recyclerView.setAdapter(chatMessageAdapter);

        // init EditText input box and Button
        editTextMessage = findViewById(R.id.edittext_chat_chatmessage);
        buttonSend = findViewById(R.id.btn_chat_send);

        // show offer information associated with this chat
        loadOfferDetails(offerId);
        // load chat history
        loadChatMessages();

        // send chat message click listener
        buttonSend.setOnClickListener(v -> sendMessage(offerId));
    }

    private void loadOfferDetails(String offerId) {
        if (offerId == null || offerId.isEmpty()) {
            Log.e(TAG, "offerId is null or empty");
            return;
        }

        // load offer details associated with this chat
        db.collection("offers").document(offerId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String offerTitle = documentSnapshot.getString("title");
                        setTitle("Chat - " + offerTitle);
                    }
                }).addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading offer details", e);
                });
    }

    // load all chat messages from Firestore of this chat
    private void loadChatMessages() {
        // monitor collection 'chatmessages' by addSnapshotListener and update automatically
        db.collection("chats").document(chatId).collection("chatmessages")
                .orderBy("timestamp", Query.Direction.ASCENDING)   // load messages by time order
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.w(TAG, "Listen failed.", error);
                        return;
                    }

                    if (value != null && !value.isEmpty()) {
                        chatMessageList.clear();  // clear all chat messages to load new
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            ChatMessage message = doc.toObject(ChatMessage.class);
                            if (!chatMessageList.contains(message)) {   // if this text is sent by current user, and already show, don't add again
                                chatMessageList.add(message);
                            }
                        }
                        Log.d(TAG, "Chat messages loaded: " + chatMessageList.size());
                        chatMessageAdapter.notifyDataSetChanged();  // notify adapter to update UI
                        // roll to the newest chat message
                        recyclerView.scrollToPosition(chatMessageList.size() - 1);
                    }
                });
    }

    private void sendMessage(String offerId) {
        // get what user type in
        String messageText = editTextMessage.getText().toString().trim();
        if (messageText.isEmpty()) {
            return;
        }

        // create a chatmessage object
        ChatMessage chatMessage = new ChatMessage(
                currentUserId,
                otherUserId,
                messageText,
                Timestamp.now(),
                false,
                myProfilePicture
        );

        chatMessageList.add(chatMessage);
        chatMessageAdapter.notifyDataSetChanged();
        recyclerView.scrollToPosition(chatMessageList.size() - 1);

        // store chat message in Firestore
        DocumentReference chatRef = db.collection("chats").document(chatId);
        chatRef.collection("chatmessages")
                .add(chatMessage)
                .addOnSuccessListener(documentReference -> {
                    // clear input box after send success
                    editTextMessage.setText("");

                    // update chats doc lastMessage and timestamp
                    chatRef.update(
                            "lastMessage", messageText,
                            "lastMessageTimestamp", Timestamp.now(),
                            "offerId", offerId
                    ).addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Last message and timestamp updated successfully");
                    }).addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to update last message and timestamp", e);
                    });

                    recyclerView.scrollToPosition(chatMessageList.size() - 1);

                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error sending message", e);
                });
    }

}
