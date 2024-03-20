package com.example.chatting;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class GroupChatsAdapter extends RecyclerView.Adapter<GroupChatsAdapter.GroupChatViewHolder> {
    private List<GroupChatModel> groupChatsList;
    private Context context;

    public GroupChatsAdapter(Context context, List<GroupChatModel> groupChatsList) {
        this.context = context;
        this.groupChatsList = groupChatsList;
    }

    @NonNull
    @Override
    public GroupChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_group_chat, parent, false);
        return new GroupChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupChatViewHolder holder, int position) {
        GroupChatModel groupChat = groupChatsList.get(position);
        holder.bind(groupChat);
    }

    @Override
    public int getItemCount() {
        return groupChatsList.size();
    }

    public class GroupChatViewHolder extends RecyclerView.ViewHolder {
        TextView groupNameTextView;

        public GroupChatViewHolder(@NonNull View itemView) {
            super(itemView);
            groupNameTextView = itemView.findViewById(R.id.groupNameTextView);
        }

        public void bind(GroupChatModel groupChat) {
            groupNameTextView.setText(groupChat.getGroupName());

//            groupNameTextView.setText(groupChat.getGroupName());
//
//            // Set click listener to open chat activity with group chat data
//            itemView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    Intent intent = new Intent(context, ChatActivity.class);
//                    intent.putExtra("groupId", groupChat.getRoomId());
//                    intent.putExtra("groupName", groupChat.getGroupName());
//                    // Add more data if needed
//                    context.startActivity(intent);
//                }
//            });
        }
    }
}
