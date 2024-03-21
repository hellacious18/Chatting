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
    private String senderId;
    private static final int VIEW_TYPE_MESSAGE = 1;
    private static final int VIEW_TYPE_GROUP = 2;

    public ChatAdapter(Context context, List<MessageModel> messageList, String senderId) {
        this.context = context;
        this.messageList = messageList;
        this.senderId = senderId;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == VIEW_TYPE_GROUP) {
            view = LayoutInflater.from(context).inflate(R.layout.item_group_message, parent, false);
        } else {
            view = LayoutInflater.from(context).inflate(R.layout.item_message, parent, false);
        }
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        MessageModel message = messageList.get(position);
        holder.messageText.setText(message.getContent());
        holder.timeStamp.setText(formatTimeStamp(message.getTimestamp()));

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

    @Override
    public int getItemViewType(int position) {
        MessageModel message = messageList.get(position);
        if (message.getMessageType() != null && message.getMessageType().equals("group")) {
            return VIEW_TYPE_GROUP;
        } else {
            return VIEW_TYPE_MESSAGE;
        }
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
