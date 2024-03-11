package com.example.chatting;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class HomeUserActivity extends AppCompatActivity {

    private List<userModel> userList;
    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
//    private List<UserAdapter> userList;
//    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_user);

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        userModel currentUser = new userModel();
        currentUser.setDisplayName(firebaseUser.getDisplayName());
        currentUser.setPhotoUrl(String.valueOf(firebaseUser.getPhotoUrl()));

        userList = new ArrayList<>(); // Initialize your user list

        // Add currentUser to the list if needed
        userList.add(currentUser);

        // Assuming you have a custom adapter for your RecyclerView
        UserAdapter adapter = new UserAdapter(userList);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

    }
}
