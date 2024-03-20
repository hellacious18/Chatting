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
    TextView receiverNameTextView;
    ImageView receiverPhotoImageView;
    ImageView backImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        receiverId = getIntent().getStringExtra("userId");
        // Get receiver's name and photo from Intent extras
        receiverName = getIntent().getStringExtra("username");
        receiverPhotoUrl = getIntent().getStringExtra("userPhotoUrl");

        String groupName = getIntent().getStringExtra("groupName");
        ArrayList<userModel> selectedUsers = (ArrayList<userModel>) getIntent().getSerializableExtra("selectedUsers");

        Log.d("ChatActivity", "Group Name: " + groupName);

        // Log selected users
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
        String[] userIdsArray = {currentUserId, receiverId};
        List<String> userIdsList = Arrays.asList(userIdsArray);
        String roomId = generateChatRoomID(userIdsList);


        chatRoomRef = databaseReference.child("chatRooms").child(roomId);

        // Initialize views
        recyclerView = findViewById(R.id.chatRecyclerView);
        messageEditText = findViewById(R.id.messageEditText);
        sendButton = findViewById(R.id.sendButton);
        receiverNameTextView = findViewById(R.id.userNameTextView);
        receiverPhotoImageView = findViewById(R.id.userImageView);

        // Set receiver's name and photo
        receiverNameTextView.setText(receiverName);
        RequestOptions requestOptions = new RequestOptions().circleCrop();
        Glide.with(receiverPhotoImageView)
                .load(receiverPhotoUrl)
                .apply(requestOptions).placeholder(R.drawable.img)
                .into(receiverPhotoImageView);

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

            MessageModel newMessage = new MessageModel(senderId, receiverId, messageContent, timestamp,messageId );
            messageList.add(newMessage);
            chatAdapter.notifyDataSetChanged();

            // Push the message object to Firebase Realtime Database
            chatRoomRef.push().setValue(newMessage);
            messageEditText.setText("");
        }
    }

    // Method to generate chat room ID based on user IDs
    private String generateChatRoomID(List<String> userIds) {
        // Convert the collection to ArrayList to support removal operations
        ArrayList<String> userIdsList = new ArrayList<>(userIds);

        // Remove null elements from the list before sorting
        userIdsList.removeIf(userId -> userId == null);

        // Sort the list after removing null elements
        Collections.sort(userIdsList);

        // Concatenate user IDs to generate the chat room ID
        return TextUtils.join("_", userIdsList);
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

