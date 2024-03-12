package com.example.chatting;

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

import java.util.ArrayList;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        receiverId = getIntent().getStringExtra("userId");

        // Get receiver's name and photo from Intent extras
        receiverName = getIntent().getStringExtra("userName");
        receiverPhotoUrl = getIntent().getStringExtra("userPhotoUrl");

        // Initialize Firebase
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String currentUserId = currentUser.getUid();
        String roomId = generateChatRoomID(currentUserId, receiverId);
        chatRoomRef = databaseReference.child("chatRooms").child(roomId);

        // Initialize views
        recyclerView = findViewById(R.id.chatRecyclerView);
        messageEditText = findViewById(R.id.messageEditText);
        sendButton = findViewById(R.id.sendButton);
        receiverNameTextView = findViewById(R.id.usernameTextView);
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
                    messageList.add(message);
                    chatAdapter.notifyDataSetChanged();
                    recyclerView.scrollToPosition(messageList.size() - 1);
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
            MessageModel newMessage = new MessageModel(senderId, receiverId, messageContent, timestamp);
            messageList.add(newMessage);
            chatAdapter.notifyDataSetChanged();

            // Push the message object to Firebase Realtime Database
            chatRoomRef.push().setValue(newMessage);
            messageEditText.setText("");
        }
    }

    // Method to generate chat room ID based on user IDs
    private String generateChatRoomID(String userID1, String userID2) {
        // Example: concatenate user IDs and sort to ensure consistency
        List<String> ids = new ArrayList<>();
        ids.add(userID1);
        ids.add(userID2);
        ids.sort(String::compareTo);
        return String.join("_", ids);
    }
}