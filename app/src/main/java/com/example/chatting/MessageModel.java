package com.example.chatting;

// MessageModel.java
public class MessageModel {
    private String senderId;
    private String receiverId;
    private String content;
    private long timestamp;
    public MessageModel() {
        // Default constructor required for calls to DataSnapshot.getValue(MessageModel.class)
    }

    // Constructor
    public MessageModel(String senderId, String receiverId, String content, long timestamp) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.content = content;
        this.timestamp = timestamp;
    }

    // Getters
    public String getSenderId() {
        return senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public String getContent() {
        return content;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
