package com.example.chatting;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

public class GroupChatModel implements Serializable {
    private String roomId;
    private String groupName;
    private List<userModel> members;
    private String groupPhoto;
    private String senderId;
    private String content;
    private String senderName;
    private String messageId;
    private long timestamp;

    public GroupChatModel() {
        // Default constructor required for calls to DataSnapshot.getValue(GroupChatModel.class)
    }

    public GroupChatModel(String roomId, String groupName, String groupPhoto, List<userModel> members) {
        this.roomId = roomId;
        this.groupName = groupName;
        this.groupPhoto = groupPhoto;
        this.members = members;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getGroupPhoto() {
        return groupPhoto;
    }

    public void setGroupPhoto(String groupPhoto) {
        this.groupPhoto = groupPhoto;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public List<userModel> getMembers() {
        return members;
    }

    public void setMembers(List<userModel> members) {
        this.members = members;
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

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
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
        return "GroupChatModel{" +
                "roomId='" + roomId + '\'' +
                ", groupName='" + groupName + '\'' +
                ", groupPhoto='" + groupPhoto + '\'' +
                ", members=" + members +
                ", senderId='" + senderId + '\'' +
                ", content='" + content + '\'' +
                ", senderName='" + senderName + '\'' +
                ", messageId='" + messageId + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        GroupChatModel that = (GroupChatModel) obj;
        return roomId.equals(that.roomId) &&
                groupName.equals(that.groupName) &&
                Objects.equals(groupPhoto, that.groupPhoto);
    }

    @Override
    public int hashCode() {
        return Objects.hash(roomId, groupName, groupPhoto);
    }
}
