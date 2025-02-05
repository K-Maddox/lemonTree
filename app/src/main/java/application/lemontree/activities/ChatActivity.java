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

    }
}
