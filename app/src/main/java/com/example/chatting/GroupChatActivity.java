package com.example.chatting;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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

public class GroupChatActivity extends AppCompatActivity {

    private DatabaseReference groupChatRoomRef;
    private DatabaseReference groupChatsByNameRef; // Reference to groupChatsByName node
    private RecyclerView recyclerView;
    private EditText messageEditText;
    private Button sendButton;
    private MessageAdapter chatAdapter;
    private List<MessageModel> messageList;
    private String receiverId;
    private String receiverName;
    private String receiverPhotoUrl;
    private String groupName, groupId;
    public TextView groupNameTextView;
    public ImageView groupPhoto;
    public ImageView backImageView;
    private String senderName;
    private static final int REQUEST_SELECT_GROUP_ICON = 2;
    private static Set<String> generatedIds = new HashSet<>();
    String roomId, currentUserId;
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
    FirebaseUser currentUser;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);


        groupName = getIntent().getStringExtra("groupName");
        groupId = getIntent().getStringExtra("groupId");
        Log.d("Z", groupName);

        groupNameTextView = findViewById(R.id.txt_groupname);
        groupPhoto = findViewById(R.id.img_groupphoto);
        recyclerView = findViewById(R.id.groupChatRecyclerView);
        messageEditText = findViewById(R.id.g_messageEditText);
        sendButton = findViewById(R.id.g_sendButton);
        backImageView = findViewById(R.id.g_backImageView);

        groupNameTextView.setText(groupName);
        RequestOptions requestOptions = new RequestOptions().circleCrop();

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        currentUserId = currentUser.getUid();
                    // Group chat mode

        groupChatRoomRef = databaseReference.child("chatRooms").child("group").child(groupName).child("chats");
        groupChatsByNameRef = databaseReference.child("chatRooms").child("groupChatsByName"); // Initialize reference



        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        messageList = new ArrayList<>();
        chatAdapter = new MessageAdapter( messageList, currentUserId);
        recyclerView.setAdapter(chatAdapter);

        // Set up send button click listener
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        // Set up Firebase Realtime Database listener to receive messages
        groupChatRoomRef.addChildEventListener(new ChildEventListener() {
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
        groupPhoto.setOnClickListener(new View.OnClickListener() {
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
            String messageId = groupChatRoomRef.push().getKey();

            MessageModel newMessage = new MessageModel(senderId, messageContent, timestamp, messageId, senderName);
            messageList.add(newMessage);
            chatAdapter.notifyDataSetChanged();

            // Push the message object to Firebase Realtime Database
            groupChatRoomRef.child(messageId).setValue(newMessage);
            messageEditText.setText("");
        }
    }

    // Method to open gallery for selecting group icon
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_SELECT_GROUP_ICON);
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
                        .into(groupPhoto);
            }
        }
    }
}