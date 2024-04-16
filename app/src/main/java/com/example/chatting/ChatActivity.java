package com.example.chatting;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;

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

    private DatabaseReference chatRoomRef, RchatRoomREf, databaseReference;
    // Reference to groupChatsByName node
    private RecyclerView recyclerView;
    private EditText messageEditText;
    private Button sendButton, btnAttachment;
    private MessageAdapter chatAdapter;
    private List<MessageModel> messageList;
    private String receiverId;
    private String receiverName;
    private String receiverPhotoUrl;
    private String groupName;
    TextView receiverNameTextView;
    ImageView receiverPhotoImageView;
    ImageView backImageView;
    private String senderName, checker, myUrl;
    private static final int REQUEST_SELECT_GROUP_ICON = 2;
    private static Set<String> generatedIds = new HashSet<>();
    String roomId, currentUserId;
    FirebaseUser currentUser;
    StorageTask upload;
    Uri fileuri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Get receiver's information for single chat
        receiverId = getIntent().getStringExtra("userId");
        receiverName = getIntent().getStringExtra("username");
        receiverPhotoUrl = getIntent().getStringExtra("userPhotoUrl");

        recyclerView = findViewById(R.id.chatRecyclerView);
        messageEditText = findViewById(R.id.messageEditText);
        sendButton = findViewById(R.id.sendButton);
        receiverPhotoImageView = findViewById(R.id.userImageView);
        backImageView = findViewById(R.id.backImageView);
        btnAttachment = findViewById(R.id.btn_file_attach);
        receiverNameTextView = findViewById(R.id.userNameTextView);
        receiverNameTextView.setText(receiverName);
        RequestOptions requestOptions = new RequestOptions().circleCrop();
        Glide.with(receiverPhotoImageView)
                .load(receiverPhotoUrl)
                .apply(requestOptions)
                .placeholder(R.drawable.img)
                .into(receiverPhotoImageView);

        databaseReference = FirebaseDatabase.getInstance().getReference();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        currentUserId = currentUser.getUid();
        senderName = currentUser.getDisplayName();

        chatRoomRef = databaseReference.child("chatRooms").child("singleUserChats")
                .child(currentUser.getDisplayName()).child(receiverName);
        RchatRoomREf = databaseReference.child("chatRooms").child("singleUserChats")
                .child(receiverName).child(currentUser.getDisplayName());

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

        btnAttachment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CharSequence options[] = new CharSequence[]{
                        "Images",
                        "PDF Files",
                        "WORD Files"
                };
                AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
                builder.setTitle("Select the file");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(which == 0){
                            checker = "image";
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("image/*");
                            startActivityForResult(intent.createChooser(intent, "Select Image"), 200);
                        }
                        if(which == 1){
                            checker = "pdf";
                        }
                        if(which == 2){
                            checker = "docx";
                        }
                    }
                });
                builder.show();
            }
        });

        // Set up Firebase Realtime Database listener to receive messages
        chatRoomRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                MessageModel message = snapshot.getValue(MessageModel.class);
                if (message != null) {
                    // Check if the message already exists in the list
                    boolean messageExists = false;
                    for (MessageModel existingMessage : messageList) {
                        if (existingMessage.getMessageId().equals(message.getMessageId())) {
                            messageExists = true;
                            break;
                        }
                    }
                    // Add the message to the list if it doesn't exist
                    if (!messageExists) {
                        messageList.add(message);
                        // Notify adapter of new message
                        chatAdapter.notifyDataSetChanged();
                        // Scroll to the last message
                        recyclerView.smoothScrollToPosition(messageList.size() - 1);
                    }
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
            String imageurl = null;
            String messageId = chatRoomRef.push().getKey();
            String messageIdR = RchatRoomREf.push().getKey();

            MessageModel newMessage = new MessageModel(senderId, messageContent, imageurl, timestamp, messageId, senderName);
            messageList.add(newMessage);
            chatAdapter.notifyDataSetChanged();

            // Push the message object to Firebase Realtime Database
            chatRoomRef.child(messageId).setValue(newMessage);
            RchatRoomREf.child(messageIdR).setValue(newMessage);
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
                        .into(receiverPhotoImageView);
            }
        }

        if(requestCode==200 && resultCode==RESULT_OK && data!=null && data.getData()!=null){
            fileuri = data.getData();

            if(!checker.equals("image")){

            }
            else if(checker.equals("image")){

                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Images");

                String messageId = chatRoomRef.getKey();
                StorageReference filepath =  storageReference.child(messageId+ "."+ "jpg");

                upload = filepath.putFile(fileuri);

                upload.continueWithTask(new Continuation() {
                    @Override
                    public Object then(@NonNull Task task) throws Exception {
                        if(!task.isSuccessful()) {
                            throw task.getException();
                        }
                        Log.d("check", String.valueOf(filepath));
                        return filepath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if(task.isSuccessful()){
                            Log.d("check", String.valueOf(task));
                            Uri downloadUrl = task.getResult();
                            myUrl = downloadUrl.toString();
                            String senderId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                            long timestamp = System.currentTimeMillis();
                            String messageId = chatRoomRef.push().getKey();
                            String messageIdR = RchatRoomREf.push().getKey();
                            String message = null;

                            MessageModel newMessage = new MessageModel(senderId, message, myUrl, timestamp, messageId, senderName);
                            messageList.add(newMessage);
                            chatAdapter.notifyDataSetChanged();

                            // Push the message object to Firebase Realtime Database
                            chatRoomRef.child(messageId).setValue(newMessage);
                            RchatRoomREf.child(messageIdR).setValue(newMessage);
                            Log.d("check", myUrl);
                        }
                    }
                });

            }else{
                Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
            }
        }
    }
}