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


    }
}
