package com.geraud.android.gps1.RecyclerAdapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.geraud.android.gps1.Models.Chat;
import com.geraud.android.gps1.R;

import java.util.List;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ChatListViewHolder> {

    private List<Chat> mChatList;
    private Context mContext;

    public ChatListAdapter(List<Chat> ChatList, Context context) {
        this.mChatList = ChatList;
        this.mContext = context;
    }

    @NonNull
    @Override
    public ChatListViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View layoutView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.chat_item, null, false);
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutView.setLayoutParams(lp);

        ChatListViewHolder rcv = new ChatListViewHolder(layoutView);
        return rcv;
    }

    @Override
    public void onBindViewHolder(@NonNull final ChatListViewHolder holder, final int position) {

        holder.mTitle.setText(mChatList.get(position).getChatId());

        holder.mLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }


    @Override
    public int getItemCount() {
        return mChatList.size();
    }

    public class ChatListViewHolder extends RecyclerView.ViewHolder {
        public ImageView mImage;
        public TextView mTitle, mTimeStamp, mLastMessage;
        public LinearLayout mLayout;

        public ChatListViewHolder(View view) {
            super(view);
            mTitle = view.findViewById(R.id.title);
            mLayout = view.findViewById(R.id.layout);
            mImage = view.findViewById(R.id.image);
            mTimeStamp = view.findViewById(R.id.timestamp);
            mLastMessage= view.findViewById(R.id.lastMessage);

        }
    }


    }


