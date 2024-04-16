package com.example.chatting;

import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private final List<MessageModel> messageList;
    private final String currentUserID;

    public MessageAdapter(List<MessageModel> messageList, String currentUserID) {
        this.messageList = messageList;
        this.currentUserID = currentUserID;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view1, view2;
        if (viewType == MessageType.SENT.ordinal()) {
            view1 = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_sent, parent, false);
            return new MessageViewHolder(view1);
        } else {
            view2 = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_received, parent, false);
            return new MessageViewHolder(view2);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        MessageModel message = messageList.get(position);
        holder.bind(message);
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    @Override
    public int getItemViewType(int position) {
        MessageModel message = messageList.get(position);
        return message.getSenderId().equals(currentUserID) ? MessageType.SENT.ordinal() : MessageType.RECEIVED.ordinal();
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageTextView;
        TextView timeStampTextView;
        ImageView image;

        MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageTextView = itemView.findViewById(R.id.messageTextView);
            timeStampTextView = itemView.findViewById(R.id.timeStamp);
            image = itemView.findViewById(R.id.image_message);
        }

        void bind(MessageModel message) {
            timeStampTextView.setText(getFormattedTime(message.getTimestamp()));
            if(message.getImageUrl()!=null) {
                messageTextView.setVisibility(View.INVISIBLE);
                image.setVisibility(View.VISIBLE);
                Glide.with(itemView).load(message.getImageUrl()).into(image);
            }else {
                messageTextView.setText(message.getContent());
            }
        }

        private String getFormattedTime(long timestamp) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM 'at' HH:mm", Locale.getDefault());
            return sdf.format(new Date(timestamp));
        }
    }

    enum MessageType {
        SENT,
        RECEIVED
    }
}
