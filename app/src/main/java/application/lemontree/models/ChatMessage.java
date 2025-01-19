package application.lemontree.models;

import com.google.firebase.Timestamp;

public class ChatMessage {

    //private String messageId;
    private String senderId;
    private String receiverId;
    private String messageText;
    public String senderPictureURL;

    private Timestamp timestamp;
    private boolean isRead;

    public ChatMessage() {
    }

    public ChatMessage(String senderId, String receiverId,
                       String messageText, Timestamp timestamp, boolean isRead, String senderPictureURL) {
//        this.messageId = messageId;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.messageText = messageText;
        this.senderPictureURL = senderPictureURL;
        this.timestamp = timestamp;
        this.isRead = isRead;
    }
}
