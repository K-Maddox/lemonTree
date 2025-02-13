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

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
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

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        // Bind data to views in the item_message layout
        QueryDocumentSnapshot chat = messageList.get(position);
        String offerId = chat.getString("offerId");

        holder.itemNameTextView.setText(chat.getString("title"));

        holder.lastMessageTextView.setText(chat.getString("lastMessage"));


        // Get the other participant's name
        List<String> participants = (List<String>) chat.get(
                "participants");
        List<String> participantNames = (List<String>) chat.get(
                "participantsNames");
        List<String> profilePictureURLs = (List<String>) chat.get(
                "profilePictureURLs");

        String pictureURL = null;
        String otherParticipantName = null;
        String otherParticipantId = null;


        for (int i = 0; i < participants.size(); i++) {
            String participantId = participants.get(i);
            if (!participantId.equals(mAuth.getCurrentUser().getUid())) {
                // This is the other participant
                otherParticipantId = participantId;
                otherParticipantName = participantNames.get(i);
                pictureURL = profilePictureURLs.get(i);
                break;
            }
        }

        Glide.with(holder.itemView.getContext())
                .load(pictureURL)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.lemon)
                .into(holder.imageView);

        Log.i(TAG,
                mAuth.getCurrentUser().getUid() + " " + otherParticipantName + " " + otherParticipantId + " " + pictureURL);

        holder.fromUserTextView.setText(otherParticipantName != null ? otherParticipantName : "Unknown");

        // click to chat detail
        holder.itemView.setOnClickListener(v -> listener.onItemClick(chat));
    }

    @Override
    public int getItemCount() {
        Log.d(TAG, "Item count: " + messageList.size());
        return messageList.size();
    }

    public interface OnItemClickListener {
        void onItemClick(QueryDocumentSnapshot message);
    }

    // ViewHolder class to hold the views for each message item
    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView itemNameTextView, fromUserTextView, lastMessageTextView;
        ImageView imageView;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            itemNameTextView = itemView.findViewById(R.id.text_mes_itemname);
            fromUserTextView = itemView.findViewById(R.id.text_mes_username);
            lastMessageTextView = itemView.findViewById(R.id.text_mes_lastMessage);
            imageView = itemView.findViewById(R.id.imageView);
        }
    }
}
