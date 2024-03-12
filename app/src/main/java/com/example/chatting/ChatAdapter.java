package com.example.chatting;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MessageViewHolder> {

    private Context context;
    private List<MessageModel> messageList;
    private String senderId; // Current user's ID

    // Constructor
    public ChatAdapter(Context context, List<MessageModel> messageList, String senderId) {
        this.context = context;
        this.messageList = messageList;
        this.senderId = senderId;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        MessageModel message = messageList.get(position);
        holder.messageText.setText(message.getContent());

        // Check if the message was sent by the current user
        boolean isSentByCurrentUser = message.getSenderId().equals(senderId);

        // Adjust layout properties based on whether the message was sent by the current user
        if (isSentByCurrentUser) {
            // Set layout parameters for messages sent by the current user (align to right)
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) holder.messageText.getLayoutParams();
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_END); // Align to the right side
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_START, 0); // Remove alignment to the left
            holder.messageText.setLayoutParams(layoutParams);
            holder.messageText.setBackgroundResource(R.drawable.sent_message_background);
        } else {
            // Set layout parameters for messages received from other users (align to left)
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) holder.messageText.getLayoutParams();
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_START); // Align to the left side
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_END, 0); // Remove alignment to the right
            holder.messageText.setLayoutParams(layoutParams);
            holder.messageText.setBackgroundResource(R.drawable.received_message_background);
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.messageTextView);
        }
    }
}
