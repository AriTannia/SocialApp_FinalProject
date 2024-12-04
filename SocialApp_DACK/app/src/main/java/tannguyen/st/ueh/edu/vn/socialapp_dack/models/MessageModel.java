package tannguyen.st.ueh.edu.vn.socialapp_dack.models;

public class MessageModel {
    private String messageId; // ID duy nhất của tin nhắn
    private String sender;
    private String receiver;
    private String message;
    private String timestamp;
    private boolean isSeen; // Đã xem hay chưa

    // Default constructor (required for Firebase)
    public MessageModel() {
    }

    // Constructor with all fields
    public MessageModel(String messageId, String sender, String receiver, String message, String timestamp, boolean isSeen) {
        this.messageId = messageId;
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        this.timestamp = timestamp;
        this.isSeen = isSeen;
    }

    // Getters and Setters
    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isSeen() {
        return isSeen;
    }

    public void setSeen(boolean seen) {
        isSeen = seen;
    }
}
