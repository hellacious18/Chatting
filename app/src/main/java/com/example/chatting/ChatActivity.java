package com.example.chatting;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private DatabaseReference chatRoomRef;
    private RecyclerView recyclerView;
    private EditText messageEditText;
    private Button sendButton;
    private ChatAdapter chatAdapter;
    private List<MessageModel> messageList;
    private String receiverId;
    private String receiverName;
    private String receiverPhotoUrl;
    private String groupName;
    TextView receiverNameTextView;
    ImageView receiverPhotoImageView;
    ImageView backImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Get receiver's information for single chat
        receiverId = getIntent().getStringExtra("userId");
        receiverName = getIntent().getStringExtra("username");
        receiverPhotoUrl = getIntent().getStringExtra("userPhotoUrl");

        // Get group information for group chat
        groupName = getIntent().getStringExtra("groupName");
        ArrayList<userModel> selectedUsers = (ArrayList<userModel>) getIntent().getSerializableExtra("selectedUsers");

        if (selectedUsers != null && !selectedUsers.isEmpty()) {
            Log.d("ChatActivity", "Selected Users:");
            for (userModel user : selectedUsers) {
                Log.d("ChatActivity", "User ID: " + user.getUserId() + ", User Name: " + user.getDisplayName());
            }
        } else {
            Log.d("ChatActivity", "No selected users");
        }

        backImageView = findViewById(R.id.backImageView);
        backImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), HomeUserActivity.class));
            }
        });

        // Initialize Firebase
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String currentUserId = currentUser.getUid();
        String roomId;

        // Check if it's a single chat or a group chat
        if (receiverId != null) {
            // Single chat mode
            roomId = generateChatRoomID(Arrays.asList(currentUserId, receiverId));
            initializeSingleChatView();
        } else {
            // Group chat mode
            roomId = generateChatRoomID(getUserIdsFromSelectedUsers(selectedUsers));
            initializeGroupChatView();
        }

        chatRoomRef = databaseReference.child("chatRooms").child(roomId);

        // Initialize views
        recyclerView = findViewById(R.id.chatRecyclerView);
        messageEditText = findViewById(R.id.messageEditText);
        sendButton = findViewById(R.id.sendButton);

        // Initialize message list and adapter
        messageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(this, messageList, currentUserId);

        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(chatAdapter);

        // Set up send button click listener
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        // Set up Firebase Realtime Database listener to receive messages
        chatRoomRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                MessageModel message = snapshot.getValue(MessageModel.class);
                if (message != null) {
                    // Add the message to the list
                    messageList.add(message);
                    // Notify adapter of new message
                    chatAdapter.notifyDataSetChanged();
                    // Scroll to the last message
                    recyclerView.smoothScrollToPosition(messageList.size() - 1);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                // Handle changed messages if needed
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                // Handle removed messages if needed
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                // Handle moved messages if needed
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle cancelled event if needed
            }
        });
    }

    // Method to send a message
    private void sendMessage() {
        String messageContent = messageEditText.getText().toString().trim();
        if (!messageContent.isEmpty()) {
            String senderId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            long timestamp = System.currentTimeMillis();
            String messageId = chatRoomRef.push().getKey();

            MessageModel newMessage = new MessageModel(senderId, receiverId, messageContent, timestamp, messageId);
            messageList.add(newMessage);
            chatAdapter.notifyDataSetChanged();

            // Push the message object to Firebase Realtime Database
            chatRoomRef.child(messageId).setValue(newMessage);
            messageEditText.setText("");
        }
    }

    // Method to generate chat room ID based on user IDs
    private String generateChatRoomID(List<String> userIds) {
        // Sort the user IDs to ensure consistent room ID generation
        Collections.sort(userIds);
        // Concatenate user IDs to generate the chat room ID
        return TextUtils.join("_", userIds);
    }

    // Method to get user IDs from selected users in a group chat
    private List<String> getUserIdsFromSelectedUsers(List<userModel> selectedUsers) {
        List<String> userIds = new ArrayList<>();
        for (userModel user : selectedUsers) {
            userIds.add(user.getUserId());
        }
        return userIds;
    }

    // Method to initialize views for single chat mode
    private void initializeSingleChatView() {
        receiverNameTextView = findViewById(R.id.userNameTextView);
        receiverPhotoImageView = findViewById(R.id.userImageView);

        // Set receiver's name and photo
        receiverNameTextView.setText(receiverName);
        RequestOptions requestOptions = new RequestOptions().circleCrop();
        Glide.with(receiverPhotoImageView)
                .load(receiverPhotoUrl)
                .apply(requestOptions)
                .placeholder(R.drawable.img)
                .into(receiverPhotoImageView);
    }

    // Method to initialize views for group chat mode
    private void initializeGroupChatView() {
        receiverNameTextView = findViewById(R.id.userNameTextView);
        receiverNameTextView.setText(groupName);
        // Hide receiver photo view since it's a group chat
        findViewById(R.id.userImageView).setVisibility(View.GONE);
    }

    // Method to sort the message list by timestamp
    private void sortMessageListByTimestamp() {
        Collections.sort(messageList, new Comparator<MessageModel>() {
            @Override
            public int compare(MessageModel o1, MessageModel o2) {
                return Long.compare(o1.getTimestamp(), o2.getTimestamp());
            }
        });
    }
}
