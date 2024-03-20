package com.example.chatting;

// GroupChatModel.java
public class GroupChatModel {
    private String roomId;
    private String groupName;

    public GroupChatModel(String roomId, String groupName) {
        this.roomId = roomId;
        this.groupName = groupName;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
}
