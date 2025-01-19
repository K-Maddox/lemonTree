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
    //    //public String getMessageId() {
//        return messageId;
//    }
//
//    public void setMessageId(String messageId) {
//        this.messageId = messageId;
//    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public long getTimestampInMillis() {
        return timestamp.toDate().getTime();
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }
}
