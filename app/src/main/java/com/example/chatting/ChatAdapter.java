package com.example.chatting;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
        holder.timeStamp.setText(formatTimeStamp(message.getTimestamp()));

        // Check if the message was sent by the current user
        boolean isSentByCurrentUser = message.getSenderId().equals(senderId);

        // Adjust layout properties based on whether the message was sent by the current user
        RelativeLayout.LayoutParams messageLayoutParams = (RelativeLayout.LayoutParams) holder.messageText.getLayoutParams();
        RelativeLayout.LayoutParams timeStampLayoutParams = (RelativeLayout.LayoutParams) holder.timeStamp.getLayoutParams();

        if (isSentByCurrentUser) {
            // Set layout parameters for messages sent by the current user (align to right)
            messageLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_END); // Align message to the right side
            messageLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_START, 0); // Remove alignment to the left
            timeStampLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_END); // Align timestamp to the right side
            timeStampLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_START, 0); // Remove alignment to the left
            holder.messageText.setBackgroundResource(R.drawable.sent_message_background);
        } else {
            // Set layout parameters for messages received from other users (align to left)
            messageLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_START); // Align message to the left side
            messageLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_END, 0); // Remove alignment to the right
            timeStampLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_START); // Align timestamp to the left side
            timeStampLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_END, 0); // Remove alignment to the right
            holder.messageText.setBackgroundResource(R.drawable.received_message_background);
        }

        holder.messageText.setLayoutParams(messageLayoutParams);
        holder.timeStamp.setLayoutParams(timeStampLayoutParams);
    }

    private String formatTimeStamp(long timestamp) {
        // Create a SimpleDateFormat object with the desired date format
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM 'at' HH:mm", Locale.getDefault());

        // Convert timestamp (milliseconds) to Date
        Date date = new Date(timestamp);

        // Format the Date object using the SimpleDateFormat
        return sdf.format(date);
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText;
        TextView timeStamp;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.messageTextView);
            timeStamp = itemView.findViewById(R.id.timeStamp);
        }
    }
}
