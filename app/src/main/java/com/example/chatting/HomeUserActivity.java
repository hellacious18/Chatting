package com.example.chatting;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class HomeUserActivity extends AppCompatActivity {

    private RecyclerView recyclerView, recyclerViewGroup;
    private FirebaseRecyclerAdapter<userModel, UserViewHolder> adapter;

    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private String currentUserID;
    FloatingActionButton createGroup, newGroup, community;
    TextView header;
    EditText groupName;
    static List<userModel> selectedUsers; // Declare the list
    String roomId;

    private DatabaseReference groupChatsRef;
    private List<GroupChatModel> groupChatsList;
    private GroupChatsAdapter groupChatsAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_user);

        header = findViewById(R.id.header);

        createGroup = findViewById(R.id.createGroup);
        newGroup = findViewById(R.id.newGroup);
        groupName = findViewById(R.id.groupName);
        community = findViewById(R.id.community);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerViewGroup = findViewById(R.id.recyclerViewGroup);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        groupChatsRef = databaseReference.child("chatRooms").child("group");

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            // If no user is signed in, redirect to MainActivity
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        currentUserID = currentUser.getUid();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("users");

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewGroup.setLayoutManager(new LinearLayoutManager(this));

        FirebaseRecyclerOptions<userModel> options =
                new FirebaseRecyclerOptions.Builder<userModel>()
                        .setQuery(mDatabase.orderByChild("displayName"), userModel.class)
                        .build();


        selectedUsers = new ArrayList<>();

        adapter = new FirebaseRecyclerAdapter<userModel, UserViewHolder>(options) {
            @Override
            public void onBindViewHolder(@NonNull UserViewHolder holder, int position, @NonNull userModel model) {
                // Exclude current user from being displayed
                if (!model.getUserId().equals(currentUserID)) {
                    holder.setUserName(model.getDisplayName());
                    holder.setUserPhoto(model.getPhotoUrl());
                    holder.bind(model); // Bind userModel object to the checkbox

                    // Set click listener for each user item
                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Get the clicked user's ID
                            String clickedUserID = getRef(position).getKey();
                            // Open ChatActivity with the clicked user's ID
                            Intent intent = new Intent(HomeUserActivity.this, ChatActivity.class);
                            intent.putExtra("userId", clickedUserID);
                            intent.putExtra("username", model.getDisplayName());
                            intent.putExtra("userPhotoUrl", model.getPhotoUrl());
                            startActivity(intent);
                        }
                    });
                } else {
                    // If it's the current user, hide the item
                    holder.itemView.setVisibility(View.GONE);
                    holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
                }
            }

            @NonNull
            @Override
            public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_user, parent, false);
                return new UserViewHolder(view);
            }
        };

        recyclerView.setAdapter(adapter);
        createGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                groupName.setVisibility(View.VISIBLE);
                newGroup.setVisibility(View.VISIBLE);
                createGroup.setVisibility(View.GONE);
                community.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                recyclerViewGroup.setVisibility(View.GONE);
                header.setText("S E L E C T   C O N T A C T S");

                for (int i = 0; i < recyclerView.getChildCount(); i++) {
                    View itemView = recyclerView.getChildAt(i);
                    UserViewHolder viewHolder = (UserViewHolder) recyclerView.getChildViewHolder(itemView);
                    viewHolder.checkbox.setVisibility(View.VISIBLE);
                }
            }
        });

        newGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(HomeUserActivity.this, GroupChatActivity.class);

                // Pass the selected users list as an extra

                if (selectedUsers != null && !selectedUsers.isEmpty()) {
                    DatabaseReference groupMemberRef = databaseReference.child("chatRooms").child("group").child(groupName.getText().toString()).child("members");
                    groupMemberRef.push().setValue(currentUser.getDisplayName());
                    Log.d("HomeActivity", "Selected Users:");
                    for (userModel user : selectedUsers) {
                        groupMemberRef.push().setValue(user.getDisplayName());
                        Log.d("HomeActivity", "User ID: " + user.getUserId() + ", User Name: " + user.getDisplayName());
                    }
                } else {
                    Log.d("HomeActivity", "No selected users");
                }

                // Add any other extras you need for ChatActivity
                intent.putExtra("groupName", groupName.getText().toString());
                intent.putExtra("selectedUsers", (Serializable) selectedUsers);

                // Start ChatActivity
                startActivity(intent);
            }
        });
        community.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createGroup.setVisibility(View.VISIBLE);
                community.setVisibility(View.GONE);
                recyclerView.setVisibility(View.GONE);
                recyclerViewGroup.setVisibility(View.VISIBLE);
                header.setText("C O M M U N I T I E S");

                for (int i = 0; i < recyclerViewGroup.getChildCount(); i++) {
                    View itemView = recyclerViewGroup.getChildAt(i);
                    GroupChatsAdapter.GroupChatViewHolder viewHolder = (GroupChatsAdapter.GroupChatViewHolder) recyclerViewGroup.getChildViewHolder(itemView);
                    viewHolder.getGroupNameTextView().setVisibility(View.VISIBLE);
                }
            }
        });

        // Initialize RecyclerView
        recyclerViewGroup = findViewById(R.id.recyclerViewGroup);
        recyclerViewGroup.setLayoutManager(new LinearLayoutManager(this));

        // Initialize groupChatsList and adapter
        groupChatsList = new ArrayList<>();
        groupChatsAdapter = new GroupChatsAdapter(this, groupChatsList, currentUserID, currentUser.getDisplayName());
        recyclerViewGroup.setAdapter(groupChatsAdapter);

        // Fetch group chat information from Firebase
        groupChatsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userModel member;
                List<userModel> memberList = new ArrayList<>();
                groupChatsList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    roomId = snapshot.getKey();
                    String groupPhoto = snapshot.toString();
                    String groupName = snapshot.getKey();

                    for (DataSnapshot memberSnapshot : snapshot.child("members").getChildren()) {
                        String memberId = memberSnapshot.getKey();
                        String memberName = (String) memberSnapshot.getValue(Boolean.parseBoolean("value"));
                        String memberPhotoUrl = memberSnapshot.child("photoUrl").getValue(String.class);

                        // Create userModel object for each member
                        member = new userModel(memberId, memberName, memberPhotoUrl);
                        memberList.add(member);
                    }

                    // Add more fields if needed (e.g., users, lastMessage, etc.)
                    GroupChatModel groupChat = new GroupChatModel(roomId, groupName, groupPhoto, memberList);
                    groupChatsList.add(groupChat);
                }
                // Sort groupChatsList by group name
                Collections.sort(groupChatsList, new Comparator<GroupChatModel>() {
                    @Override
                    public int compare(GroupChatModel chat1, GroupChatModel chat2) {
                        return chat1.getGroupName().compareToIgnoreCase(chat2.getGroupName());
                    }
                });
                groupChatsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle cancelled event if needed
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    // ViewHolder for user item
    public static class UserViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkbox; // Add checkbox
        ImageView userPhoto;
        TextView userName;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            userPhoto = itemView.findViewById(R.id.profileImageView);
            userName = itemView.findViewById(R.id.displayNameTextView);
            checkbox = itemView.findViewById(R.id.checkbox); // Initialize checkbox
        }

        public void setUserName(String name) {
            userName.setText(name);
        }

        public void setUserPhoto(String photoUrl) {
            RequestOptions requestOptions = new RequestOptions().circleCrop();
            Glide.with(itemView.getContext())
                    .load(photoUrl)
                    .apply(requestOptions)
                    .into(userPhoto);
        }
        public void bind(userModel user) {
            // Set the userModel object as the tag for the checkbox
            checkbox.setTag(user);
            checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    userModel selectedUser = (userModel) checkbox.getTag();
                    if (isChecked) {
                        selectedUsers.add(selectedUser);
                    } else {
                        selectedUsers.remove(selectedUser);
                    }
                }
            });
        }
    }
}
