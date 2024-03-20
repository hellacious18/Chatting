package com.example.chatting;

import java.io.Serializable;
import java.util.Objects;

public class MessageModel implements Serializable {
    private String senderId;
    private String content;
    private String messageId;
    private long timestamp;

    public MessageModel() {
        // Default constructor required for calls to DataSnapshot.getValue(MessageModel.class)
    }

    // Constructor
    public MessageModel(String senderId, String content, long timestamp, String messageId) {
        this.senderId = senderId;
        this.content = content;
        this.timestamp = timestamp;
        this.messageId = messageId;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }


    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "MessageModel{" +
                "senderId='" + senderId + '\'' +
                ", content='" + content + '\'' +
                ", messageId='" + messageId + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        MessageModel messageModel = (MessageModel) obj;
        return senderId.equals(messageModel.senderId) &&
                content.equals(messageModel.content) &&
                messageId.equals(messageModel.messageId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(senderId, content, messageId);
    }
}
