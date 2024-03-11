package com.example.chatting;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class HomeUserActivity extends AppCompatActivity {

    private List<UserAdapter> userList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_user);

    }
}
