package com.example.chatting;

import java.io.Serializable;
import java.util.Objects;

public class Message implements Serializable {
    private final String message;
    private final String senderId;

    public Message(String message, String senderId) {
        if (message == null || senderId == null) {
            throw new IllegalArgumentException("Message and senderId cannot be null");
        }
        this.message = message;
        this.senderId = senderId;
    }

    public String getMessage() {
        return message;
    }

    public String getSenderId() {
        return senderId;
    }
    @Override
    public String toString() {
        return "Message{" +
                "message='" + message + '\'' +
                ", senderId='" + senderId + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Message message1 = (Message) obj;
        return message.equals(message1.message) &&
                senderId.equals(message1.senderId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(message, senderId);
    }

}
