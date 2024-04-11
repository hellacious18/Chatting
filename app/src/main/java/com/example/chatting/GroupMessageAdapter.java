package com.example.chatting;

import android.util.Log;
        import android.view.LayoutInflater;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.TextView;

        import androidx.annotation.NonNull;
        import androidx.recyclerview.widget.RecyclerView;

        import java.text.SimpleDateFormat;
        import java.util.Date;
        import java.util.List;
        import java.util.Locale;

public class GroupMessageAdapter extends RecyclerView.Adapter<GroupMessageAdapter.GroupMessageViewHolder> {

    private final List<MessageModel> messageList;
    private final String currentUserID;

    public GroupMessageAdapter(List<MessageModel> messageList, String currentUserID) {
        this.messageList = messageList;
        this.currentUserID = currentUserID;
    }

    @NonNull
    @Override
    public GroupMessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view1, view2;
        if (viewType == MessageType.SENT.ordinal()) {
            view1 = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_group_message_sent, parent, false);
            return new GroupMessageViewHolder(view1);
        } else {
            view2 = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_group_message_received, parent, false);
            return new GroupMessageViewHolder(view2);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull GroupMessageViewHolder holder, int position) {
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

    static class GroupMessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageTextView;
        TextView timeStampTextView;
        TextView receiver;


        GroupMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageTextView = itemView.findViewById(R.id.messageTextView);
            timeStampTextView = itemView.findViewById(R.id.timeStamp);
            receiver = itemView.findViewById(R.id.receiver);

        }

        void bind(MessageModel message) {
            messageTextView.setText(message.getContent());
            timeStampTextView.setText(getFormattedTime(message.getTimestamp()));
                receiver.setText("@"+message.getSenderName());
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
