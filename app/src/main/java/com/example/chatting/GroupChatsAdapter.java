package com.example.chatting;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class GroupChatsAdapter extends RecyclerView.Adapter<GroupChatsAdapter.GroupChatViewHolder> {
    private final List<GroupChatModel> groupChatList;
    private final Context context;
    private final String senderId;
    private final String senderName;

    public GroupChatsAdapter(Context context, List<GroupChatModel> groupChatList, String senderId, String senderName) {
        this.context = context;
        this.groupChatList = groupChatList;
        this.senderId = senderId;
        this.senderName = senderName;
    }

    @NonNull
    @Override
    public GroupChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_group_chat, parent, false);
        return new GroupChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupChatViewHolder holder, int position) {
        GroupChatModel groupChatModel = groupChatList.get(position);

        holder.bind( groupChatModel);



    }

    private String formatTimeStamp(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM 'at' HH:mm", Locale.getDefault());
        Date date = new Date(timestamp);
        return sdf.format(date);
    }

    @Override
    public int getItemCount() {
        return groupChatList.size();
    }

    public class GroupChatViewHolder extends RecyclerView.ViewHolder {
        TextView groupNameTextView;

        public GroupChatViewHolder(@NonNull View itemView) {
            super(itemView);
            groupNameTextView = itemView.findViewById(R.id.groupNameTextView);

        }

        public void bind(GroupChatModel groupChat) {

            groupNameTextView.setText(groupChat.getRoomId());

            // Set click listener to open chat activity with group chat data
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, ChatActivity.class);
                    intent.putExtra("groupId", groupChat.getRoomId());
                    intent.putExtra("groupName", groupChat.getGroupName());
                    // Add more data if needed
                    context.startActivity(intent);
                }
            });
        }
        public TextView getGroupNameTextView() {
            return groupNameTextView;
        }
    }
}
