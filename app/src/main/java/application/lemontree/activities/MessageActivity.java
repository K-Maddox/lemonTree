package application.lemontree.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import application.lemontree.R;
import application.lemontree.adapters.MessageAdapter;
import application.lemontree.fragments.NavBarFragment;

public class MessageActivity extends AppCompatActivity {
    private static final String TAG = "inMessageActivity";
    private RecyclerView recyclerView;
    private MessageAdapter messageAdapter;

    private List<QueryDocumentSnapshot> retrievedChatsList;

    FirebaseAuth mAuth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // init recyclerview
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // init message list and add some messages
        retrievedChatsList = new ArrayList<>();

        // init adapter
        messageAdapter = new MessageAdapter(retrievedChatsList, new MessageAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(QueryDocumentSnapshot chat) {
                // jump to ChatActivity and send chatId

                Intent intent = new Intent(MessageActivity.this, ChatActivity.class);
                intent.putExtra("chatId", chat.getId());
                String offerId = chat.getString("offerId");
                intent.putExtra("offerId", offerId);

                // Get the other participant's name
                List<String> participants = (List<String>) chat.get(
                        "participants");
                List<String> participantNames = (List<String>) chat.get(
                        "participantsNames");
                List<String> profilePictureURLs = (List<String>) chat.get(
                        "profilePictureURLs");
                String otherParticipantName = null;
                String otherParticipantId = null;

                for (int i = 0; i < participants.size(); i++) {
                    String participantId = participants.get(i);
                    if (!participantId.equals(mAuth.getCurrentUser().getUid())) {
                        // This is the other participant
                        otherParticipantName = participantNames.get(i);
                        otherParticipantId = participants.get(i);

                        if (i == 0) {
                            intent.putExtra("myProfilePicture", profilePictureURLs.get(1));
                            intent.putExtra("otherProfilePicture", profilePictureURLs.get(0));
                        } else {
                            intent.putExtra("myProfilePicture", profilePictureURLs.get(0));
                            intent.putExtra("otherProfilePicture", profilePictureURLs.get(1));
                        }

                        break;
                    }
                }
                intent.putExtra("otherParticipantName", otherParticipantName);
                intent.putExtra("otherParticipantID", otherParticipantId);

                Log.d(TAG, "Launching ChatActivity with offerId: " + offerId);

                startActivity(intent);
            }
        });
        recyclerView.setAdapter(messageAdapter);

        // nav bar
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.nav_container, new NavBarFragment())
                .commit();
    }
}
