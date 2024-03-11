package com.example.chatting;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private List<userModel> userList;
    public UserAdapter(){
    }
    public UserAdapter(List<userModel> userList) {
        this.userList = new ArrayList<>(userList);
    }



//    public UserAdapter(List<userModel> userList) {
//        this.userList = userList;
//    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        userModel users = userList.get(position);
        holder.bind(users);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public class UserViewHolder extends RecyclerView.ViewHolder {

        private ImageView profileImageView;
        private TextView displayNameTextView;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImageView = itemView.findViewById(R.id.profileImageView);
            displayNameTextView = itemView.findViewById(R.id.displayNameTextView);
        }

        public void bind(userModel user) {
            if (user != null) {
                displayNameTextView.setText(user.getDisplayName());
                Glide.with(itemView.getContext()).load(user.getPhotoUrl()).circleCrop().placeholder(R.drawable.img).into(profileImageView);
//            displayNameTextView.setText(user.getDisplayName());
//            Glide.with(itemView.getContext()).load(user.getPhotoUrl()).circleCrop().placeholder(R.drawable.img).into(profileImageView);
//            // Placeholder image while loading.into(profileImageView);
            }
        }
    }
}