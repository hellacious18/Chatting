package com.example.chatting;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

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
        ImageView image, pdf;

        MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageTextView = itemView.findViewById(R.id.messageTextView);
            timeStampTextView = itemView.findViewById(R.id.timeStamp);
            image = itemView.findViewById(R.id.image_message);
            pdf = itemView.findViewById(R.id.pdf_message);
        }

        void bind(MessageModel message) {
            timeStampTextView.setText(getFormattedTime(message.getTimestamp()));
            if(message.getImageUrl() != null) {
                messageTextView.setVisibility(View.INVISIBLE);
                image.setVisibility(View.VISIBLE);
                pdf.setVisibility(View.INVISIBLE);
                int sizeInDp = 250;
                int sizeInPx = (int) (sizeInDp * itemView.getResources().getDisplayMetrics().density);
                image.getLayoutParams().height = sizeInPx;
                image.getLayoutParams().width = sizeInPx;
                Glide.with(itemView).load(message.getImageUrl()).override(sizeInPx, sizeInPx).into(image);

            } else if (message.getPdfUrl() != null) {
                messageTextView.setVisibility(View.INVISIBLE);
                pdf.setVisibility(View.VISIBLE);
                image.setVisibility(View.INVISIBLE);
                int sizeInDp = 30;
                int sizeInPx = (int) (sizeInDp * itemView.getResources().getDisplayMetrics().density);
                image.getLayoutParams().height = sizeInPx;
                image.getLayoutParams().width = sizeInPx;
                Glide.with(itemView).load(R.drawable.ic_file).override(sizeInPx, sizeInPx).into(pdf);
                pdf.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(message.getPdfUrl()));
                        MessageViewHolder.this.itemView.getContext().startActivity(intent);
                    }
                });
            } else {
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
