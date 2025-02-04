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
import application.lemontree.adapters.ChatMessageAdapter;
import application.lemontree.models.ChatMessage;

public class ChatActivity {
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
}
