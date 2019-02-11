package com.geraud.android.gps1.RecyclerAdapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.geraud.android.gps1.Models.Chat;
import com.geraud.android.gps1.Models.Message;
import com.geraud.android.gps1.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.stfalcon.frescoimageviewer.ImageViewer;

import java.util.ArrayList;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    List<Message> messageList;
    Context context;

    public MessageAdapter(List<Message> Message, Context context) {
        this.messageList = Message;
        this.context = context;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View layoutView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.message_item, null, false);
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutView.setLayoutParams(lp);

        MessageViewHolder rcv = new MessageViewHolder(layoutView);
        return rcv;
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder holder, final int position) {
        holder.mMessage.setText(messageList.get(position).getText());
        holder.mSender.setText(messageList.get(position).getSenderId());

        if (messageList.get(holder.getAdapterPosition()).getMediaUrlist().isEmpty())
            holder.mViewMedia.setVisibility(View.GONE);
        // count number of media present
        holder.mViewMedia.setText(messageList.get(position).getMediaUrlist().size());

        holder.mViewMedia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show media files
                new ImageViewer.Builder(context, messageList.get(holder.getAdapterPosition()).getMediaUrlist())
                        .setStartPosition(0)
                        .show();
            }
        });

    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView mMessage, mSender;
        public LinearLayout mLayout;
        Button mViewMedia;

        public MessageViewHolder(View view) {
            super(view);
            mMessage = view.findViewById(R.id.message);
            mSender = view.findViewById(R.id.sender);
            mLayout = view.findViewById(R.id.layout);
            mViewMedia = view.findViewById(R.id.viewMedia);
        }
    }
}


