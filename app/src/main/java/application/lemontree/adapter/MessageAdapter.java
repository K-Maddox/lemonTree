package application.lemontree.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.List;

import application.lemontree.R;

public class MessageAdapter {
    private static final String TAG = "inMessageAdapter";

    private List<QueryDocumentSnapshot> messageList;
    private OnItemClickListener listener;

    FirebaseAuth mAuth = FirebaseAuth.getInstance();

    public MessageAdapter(List<QueryDocumentSnapshot> messageList, OnItemClickListener listener) {
        this.messageList = messageList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate item_message layout
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_message, parent, false);
        return new MessageViewHolder(view);
    }
}
