package com.example.chatting;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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
import com.google.firebase.database.ValueEventListener;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ChatActivity extends AppCompatActivity {

    private DatabaseReference chatRoomRef;
    private DatabaseReference groupChatsByNameRef; // Reference to groupChatsByName node
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
    private String senderName;
    private static final int REQUEST_SELECT_GROUP_ICON = 2;
    private static Set<String> generatedIds = new HashSet<>();

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
        List<userModel> selectedUsers = (List<userModel>) getIntent().getSerializableExtra("selectedUsers");

        if (selectedUsers == null) {
            selectedUsers = new ArrayList<>();
        }

        // Initialize Firebase
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String currentUserId = currentUser.getUid();
        String roomId;

        // Check if it's a single chat or a group chat
        if (receiverId != null) {
            // Single chat mode
            roomId = generateChatRoomID(Arrays.asList(currentUserId, receiverId));
            chatRoomRef = databaseReference.child("chatRooms").child("singleUserChats").child(roomId);
            initializeSingleChatView();
        } else {
            // Group chat mode
            roomId = generateChatRoomID(getUserIdsFromSelectedUsers(selectedUsers));
            chatRoomRef = databaseReference.child("chatRooms").child("groupChats").child(roomId);
            groupChatsByNameRef = databaseReference.child("chatRooms").child("groupChatsByName"); // Initialize reference
            initializeGroupChatView();
            createGroupChat(groupName, getUserIdsFromSelectedUsers(selectedUsers));
        }

        // Initialize views
        recyclerView = findViewById(R.id.chatRecyclerView);
        messageEditText = findViewById(R.id.messageEditText);
        sendButton = findViewById(R.id.sendButton);
        receiverPhotoImageView = findViewById(R.id.userImageView);
        backImageView = findViewById(R.id.backImageView);

        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        messageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(this, messageList, currentUserId);
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

        // Set up back button click listener
        backImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), HomeUserActivity.class));
            }
        });

        // Set up receiver photo click listener for group chat
        receiverPhotoImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openGallery();
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

            MessageModel newMessage = new MessageModel(senderId, messageContent, timestamp, messageId, senderName);
            messageList.add(newMessage);
            chatAdapter.notifyDataSetChanged();

            // Push the message object to Firebase Realtime Database
            chatRoomRef.child(messageId).setValue(newMessage);
            messageEditText.setText("");
        }
    }

    // Method to generate chat room ID based on user IDs
    private String generateChatRoomID(List<String> userIds) {
        StringBuilder stringBuilder = new StringBuilder();

        // Sort the user IDs to ensure consistency
        userIds.sort(String::compareTo);

        // Concatenate all user IDs
        for (String userId : userIds) {
            stringBuilder.append(userId);
        }

        // Generate MD5 hash of concatenated user IDs
        String concatenatedIds = stringBuilder.toString();
        String chatRoomID = null;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hashBytes = md.digest(concatenatedIds.getBytes());

            // Convert byte array to hexadecimal format
            StringBuilder hexString = new StringBuilder();
            for (byte hashByte : hashBytes) {
                String hex = Integer.toHexString(0xff & hashByte);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            chatRoomID = hexString.toString();

            // Ensure uniqueness and fixed size
            chatRoomID = chatRoomID.substring(0, Math.min(chatRoomID.length(), 8)); // Adjust size as needed

            // Check if chat room ID already exists
            if (generatedIds.contains(chatRoomID)) {
                // If it does, generate a new one
                return generateChatRoomID(userIds);
            } else {
                // If it doesn't, add it to the set of generated IDs
                generatedIds.add(chatRoomID);
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return chatRoomID;
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
        receiverPhotoImageView = findViewById(R.id.userImageView);

        receiverNameTextView.setText(groupName);
        RequestOptions requestOptions = new RequestOptions().circleCrop();
        Glide.with(receiverPhotoImageView)
                .load(receiverPhotoUrl)
                .apply(requestOptions)
                .placeholder(R.drawable.add_image)
                .into(receiverPhotoImageView);
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

    // Method to open gallery for selecting group icon
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_SELECT_GROUP_ICON);
    }

    // Method to create a group chat and store it in Firebase
    // Method to create a group chat and store it in Firebase
    private void createGroupChat(String groupName, List<String> userNames) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        // Update groupChats node
        Map<String, Object> chatData = new HashMap<>();
        chatData.put("groupName", groupName);
        Map<String, Boolean> members = new HashMap<>();
        for (String userName : userNames) {
            members.put(userName, true);
        }
        chatData.put("members", members);
        // Set chatId as groupName
        databaseReference.child("chatRooms").child("groupChats").child(groupName).setValue(chatData);

        // Update groupChatsByName node
        groupChatsByNameRef.child(groupName).setValue(groupName);
    }


    // Method to retrieve chatId by groupName
    private void getChatIdByGroupName(String groupName) {
        groupChatsByNameRef.child(groupName)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            String chatId = dataSnapshot.getValue(String.class);
                            // Do something with the chatId
                        } else {
                            // Group chat not found
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Handle onCancelled if needed
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SELECT_GROUP_ICON && resultCode == RESULT_OK) {
            if (data != null && data.getData() != null) {
                Uri selectedImageUri = data.getData();
                // Update the group icon in the UI
                Glide.with(this)
                        .load(selectedImageUri)
                        .apply(RequestOptions.circleCropTransform())
                        .into(receiverPhotoImageView);
            }
        }
    }
}