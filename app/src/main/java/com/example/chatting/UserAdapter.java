package com.example.chatting;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private final Context context;
    private final List<userModel> users;
    private final OnUserClickListener listener;
    private final List<userModel> selectedUsers; // Add selected users list

    public UserAdapter(Context context, List<userModel> users, OnUserClickListener listener) {
        this.context = context;
        this.users = users;
        this.listener = listener;
        this.selectedUsers = new ArrayList<>(); // Initialize selected users list
    }

    // Interface for click events
    public interface OnUserClickListener {
        void onUserClick(userModel user);
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        userModel user = users.get(position);
        holder.bind(user);

        // Set the initial state of the checkbox
        holder.checkbox.setChecked(selectedUsers.contains(user));

        // Set click listener for checkbox
        holder.checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // If checkbox is checked, add the user to selected users list
                    selectedUsers.add(user);
                } else {
                    // If checkbox is unchecked, remove the user from selected users list
                    selectedUsers.remove(user);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    // ViewHolder class
    public class UserViewHolder extends RecyclerView.ViewHolder {
        ImageView userPhoto;
        TextView userName;
        CheckBox checkbox; // Add checkbox

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.displayNameTextView);
            userPhoto = itemView.findViewById(R.id.profileImageView);
            checkbox = itemView.findViewById(R.id.checkbox); // Initialize checkbox

            // Set click listener for the itemView
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getBindingAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        userModel clickedUser = users.get(position);
                        listener.onUserClick(clickedUser);
                    }
                }
            });
        }

        public void bind(userModel user) {
            if (user != null) {
                userName.setText(user.getDisplayName());
                Glide.with(context)
                        .load(user.getPhotoUrl())
                        .apply(RequestOptions.circleCropTransform())
                        .placeholder(R.drawable.img)
                        .into(userPhoto);
            }
        }
    }
}