package com.geraud.android.gps1.RecyclerAdapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.geraud.android.gps1.Chat.ChatActivity;
import com.geraud.android.gps1.Models.Chat;
import com.geraud.android.gps1.Models.ChatInfo;
import com.geraud.android.gps1.Models.Message;
import com.geraud.android.gps1.Models.User;
import com.geraud.android.gps1.R;
import com.geraud.android.gps1.Utils.TimeAgo;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ChatListViewHolder> {

    private List<Chat> mChatList;
    private List<ChatInfo> mChatInfo;
    private Context mContext;

    private String mPhone = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();

    public ChatListAdapter(List<Chat> ChatList, List<ChatInfo> ChatInfo, Context context) {
        this.mChatList = ChatList;
        this.mChatInfo = ChatInfo;
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

        if (mChatInfo.get(position).getType().equals("single")) {

            //set last message and timestamp
            Message lastMessage = mChatInfo.get(position).getLastMessage();
            holder.mTimeStamp.setText(TimeAgo.getTime(lastMessage.getTimestamp()));
            holder.mLastMessage.setText(lastMessage.getText());

            //set user Image and name
            DatabaseReference mChatUsersDB = FirebaseDatabase.getInstance().getReference().child("CHAT").child(mChatList.get(position).getChatId()).child("info").child("users");
            mChatUsersDB.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists())
                        for (DataSnapshot dc : dataSnapshot.getChildren())
                            if (!dc.getKey().equals(mPhone)) {

                                User user = dc.getValue(User.class);

                                holder.mImage.setImageURI(Uri.parse(user.getImage_uri()));
                                holder.mTitle.setText(user.getName());

                                mChatInfo.get(holder.getAdapterPosition()).setImage(user.getImage_uri());
                                mChatInfo.get(holder.getAdapterPosition()).setName(user.getName());
                            }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        } else { //its a group chat

            final Message lastMessage = mChatInfo.get(position).getLastMessage();
            holder.mTimeStamp.setText(TimeAgo.getTime(lastMessage.getTimestamp()));

            DatabaseReference mChatUsersDB = FirebaseDatabase.getInstance().getReference().child("USER").child(lastMessage.getSenderId());
            mChatUsersDB.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists())
                        for (DataSnapshot dc : dataSnapshot.getChildren()){
                            User user = dc.getValue(User.class);
                            holder.mLastMessage.setText(String.format(" %s : %s",user.getName(),lastMessage.getText()));
                        }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            holder.mImage.setImageURI(Uri.parse(mChatInfo.get(position).getImage()));
            holder.mTitle.setText(mChatInfo.get(position).getName());
        }

        holder.mLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, ChatActivity.class);
                intent.putExtra("chatObject", mChatList.get(holder.getAdapterPosition()));
                intent.putExtra("chatInfoObject", mChatInfo.get(holder.getAdapterPosition()));
                mContext.startActivity(intent);
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
            mLastMessage = view.findViewById(R.id.lastMessage);

        }
    }


}


