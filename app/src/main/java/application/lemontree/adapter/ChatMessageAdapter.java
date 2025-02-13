package application.lemontree.adapter;

import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.List;

import application.lemontree.R;
import application.lemontree.models.ChatMessage;

public class ChatMessageAdapter extends RecyclerView.Adapter<ChatMessageAdapter.ChatMessageViewHolder> {

    private static final String TAG = "inChatMessageAdapter";
    private List<ChatMessage> chatMessageList;
    private String currentUserId;

    public ChatMessageAdapter(List<ChatMessage> chatMessageList, String currentUserId) {
        this.chatMessageList = chatMessageList;
        this.currentUserId = currentUserId;
    }

    @NonNull
    @Override
    public ChatMessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat_message, parent, false);
        return new ChatMessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatMessageViewHolder holder, int position) {
        ChatMessage chatMessage = chatMessageList.get(position);
        // set chatmessage text
        holder.messageTextView.setText(chatMessage.getMessageText());

        loadProfileImage(chatMessage.senderPictureURL, holder.profileImageView);

        // check message sender and adjust bubble layout
        if (chatMessage.getSenderId().equals(currentUserId)) {
            // current user message show on right
            holder.messageTextView.setBackgroundResource(R.drawable.my_bubble);
            holder.messageTextView.setTextColor(Color.WHITE);
            holder.chatmessageContainer.setGravity(Gravity.END);

            // put avatar at right, bubble at left
            holder.chatmessageContainer.removeAllViews();
            holder.chatmessageContainer.addView(holder.messageTextView);
            holder.chatmessageContainer.addView(holder.profileImageView);
        } else {
            // the user chat with show on left
            holder.messageTextView.setBackgroundResource(R.drawable.left_bubble);
            holder.messageTextView.setTextColor(Color.BLACK);
            holder.chatmessageContainer.setGravity(Gravity.START);

            holder.chatmessageContainer.removeAllViews();
            holder.chatmessageContainer.addView(holder.profileImageView);
            holder.chatmessageContainer.addView(holder.messageTextView);
        }
    }

    @Override
    public int getItemCount() {
        return chatMessageList.size();
    }

    public static class ChatMessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageTextView;
        LinearLayout chatmessageContainer;
        ImageView profileImageView;

        public ChatMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageTextView = itemView.findViewById(R.id.textChatMessage);
            chatmessageContainer = itemView.findViewById(R.id.chatmessageContainer);
            profileImageView = itemView.findViewById(R.id.imageViewProfile);
        }
    }

    private void loadProfileImage(String profilePictureURL, ImageView imageView) {
        Glide.with(imageView.getContext())
                .load(profilePictureURL)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.ic_default_avatar)
                .error(R.drawable.ic_default_avatar)
                .into(imageView);
    }
}
