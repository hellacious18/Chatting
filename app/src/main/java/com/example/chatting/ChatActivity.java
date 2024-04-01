package com.example.chatting;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ChatActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_PICK = 1001;
    private DatabaseReference chatRoomRef;
    private DatabaseReference groupChatRoomRef;
    private RecyclerView recyclerView;
    private EditText messageEditText;
    private Button sendButton;
    private ChatAdapter chatAdapter;
    private GroupChatsAdapter groupChatsAdapter;
    private List<MessageModel> messageList;
    private List<GroupChatModel> groupChatList;
    private String receiverId;
    private String receiverName;
    private String receiverPhotoUrl;
    private String groupName;
    TextView receiverNameTextView;
    ImageView receiverPhotoImageView;
    ImageView backImageView;
    private static final Set<String> generatedIds = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Initialize views
        recyclerView = findViewById(R.id.chatRecyclerView);
        messageEditText = findViewById(R.id.messageEditText);
        sendButton = findViewById(R.id.sendButton);
        receiverPhotoImageView = findViewById(R.id.userImageView);
        backImageView = findViewById(R.id.backImageView);

        // Get receiver's information for single chat or group information for group chat
        receiverId = getIntent().getStringExtra("userId");
        receiverName = getIntent().getStringExtra("username");
        receiverPhotoUrl = getIntent().getStringExtra("userPhotoUrl");
        groupName = getIntent().getStringExtra("groupName");
        List<userModel> selectedUsers = (List<userModel>) getIntent().getSerializableExtra("selectedUsers");
        if (selectedUsers == null) {
            selectedUsers = new ArrayList<>();
        }

        // Initialize Firebase
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String currentUserId = currentUser.getUid();

        // Determine chat room reference based on whether it's a single chat or group chat
        if (receiverId != null) {
            String roomId = generateChatRoomID(Arrays.asList(currentUserId, receiverId));
            chatRoomRef = databaseReference.child("chatRooms").child("singleUserChats").child(roomId);
            initializeSingleChatView();
            chatAdapter = new ChatAdapter(this, messageList, currentUserId);
            recyclerView.setAdapter(chatAdapter);
        } else {
            String roomId = generateChatRoomID(getUserIdsFromSelectedUsers(selectedUsers));
            groupChatRoomRef = databaseReference.child("chatRooms").child("groupChatsBy").child(groupName);
            initializeGroupChatView();
            groupChatsAdapter = new GroupChatsAdapter(this, groupChatList, currentUserId, currentUser.getDisplayName());
            recyclerView.setAdapter(groupChatsAdapter);
            createGroupChat(groupName, getUserIdsFromSelectedUsers(selectedUsers));
        }

        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        messageList = new ArrayList<>();

        // Set up send button click listener
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        // Set up Firebase Realtime Database listener to receive messages
        if (chatRoomRef != null) {
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

        if (groupChatRoomRef != null) {
            groupChatRoomRef.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    GroupChatModel groupChatModel = snapshot.getValue(GroupChatModel.class);
                    if(groupChatModel != null){
                        groupChatList.add(groupChatModel);
                        groupChatsAdapter.notifyDataSetChanged();
                        recyclerView.smoothScrollToPosition(groupChatList.size()-1);
                    }
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

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
            String senderName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();

            MessageModel newMessage = new MessageModel(senderId, messageContent, timestamp, messageId, senderName);
            messageList.add(newMessage);
            chatAdapter.notifyDataSetChanged();
            groupChatsAdapter.notifyDataSetChanged();

            // Push the message object to Firebase Realtime Database
            if (chatRoomRef != null) {
                chatRoomRef.child(messageId).setValue(newMessage);
            }
            if (groupChatRoomRef != null) {
                groupChatRoomRef.child(messageId).setValue(newMessage);
            }
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
            userIds.add(user.getDisplayName());
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

    // Method to open gallery for selecting group icon
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_IMAGE_PICK);
    }

    // Method to create a group chat and store it in Firebase
    private void createGroupChat(String groupName, List<String> userIds) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        String chatId = String.valueOf(databaseReference.child("chatRooms").child("groupChats"));

        // Update groupChats node
        Map<String, Object> chatData = new HashMap<>();
        chatData.put("groupName", groupName);
        Map<String, Boolean> members = new HashMap<>();
        for (String userId : userIds) {
            members.put(userId, true);
        }
        chatData.put("members", members);
        databaseReference.child("chatRooms").child("groupChats").child(groupName).setValue(chatData);

        // Update groupChatsByName node
        groupChatRoomRef.child(groupName).setValue(chatId);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            receiverPhotoImageView.setImageURI(selectedImageUri);
        }
    }
}
