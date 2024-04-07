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

    private final Context context;
    private final List<MessageModel> messageList;
    private final String senderId;

    public ChatAdapter(Context context, List<MessageModel> messageList, String senderId) {
        this.context = context;
        this.messageList = messageList;
        this.senderId = senderId;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        view = LayoutInflater.from(context).inflate(R.layout.item_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        MessageModel message = messageList.get(position);

        // Check if content and timestamp have been set
        if (!holder.isContentSet) {
            holder.messageText.setText(message.getContent());
            holder.isContentSet = true;
        }

        // Check if timestamp has been set
        if (!holder.isTimeStampSet) {
            holder.timeStamp.setText(formatTimeStamp(message.getTimestamp()));
            holder.isTimeStampSet = true;
        }

        boolean isSentByCurrentUser = message.getSenderId().equals(senderId);

        RelativeLayout.LayoutParams messageLayoutParams = (RelativeLayout.LayoutParams) holder.messageText.getLayoutParams();
        RelativeLayout.LayoutParams timeStampLayoutParams = (RelativeLayout.LayoutParams) holder.timeStamp.getLayoutParams();

        if (isSentByCurrentUser) {
            messageLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_END);
            messageLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_START, 0);
            timeStampLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_END);
            timeStampLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_START, 0);
            holder.messageText.setBackgroundResource(R.drawable.sent_message_background);
        } else {
            messageLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_START);
            messageLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_END, 0);
            timeStampLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_START);
            timeStampLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_END, 0);
            holder.messageText.setBackgroundResource(R.drawable.received_message_background);
        }

        holder.messageText.setLayoutParams(messageLayoutParams);
        holder.timeStamp.setLayoutParams(timeStampLayoutParams);
    }

    private String formatTimeStamp(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM 'at' HH:mm", Locale.getDefault());
        Date date = new Date(timestamp);
        return sdf.format(date);
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        public boolean isContentSet;
        public boolean isTimeStampSet;
        TextView messageText;
        TextView timeStamp;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.messageTextView);
            timeStamp = itemView.findViewById(R.id.timeStamp);
        }
    }
}
