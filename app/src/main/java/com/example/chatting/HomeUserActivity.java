package com.example.chatting;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class HomeUserActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FirebaseRecyclerAdapter<userModel, UserViewHolder> adapter;

    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private String currentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_user);

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

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        FirebaseRecyclerOptions<userModel> options =
                new FirebaseRecyclerOptions.Builder<userModel>()
                        .setQuery(mDatabase, userModel.class)
                        .build();

        adapter = new FirebaseRecyclerAdapter<userModel, UserViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull UserViewHolder holder, int position, @NonNull userModel model) {
                // Exclude current user from being displayed
                if (!model.getUserId().equals(currentUserID)) {
                    holder.setUserName(model.getDisplayName());
                    holder.setUserPhoto(model.getPhotoUrl());

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
                    // Hide the item if it's the current user
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
        ImageView userPhoto;
        TextView userName;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            userPhoto = itemView.findViewById(R.id.profileImageView);
            userName = itemView.findViewById(R.id.displayNameTextView);
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
    }
}
