package com.geraud.android.gps1.RecyclerAdapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
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

public class TransferChatRecyclerAdapter extends RecyclerView.Adapter<TransferChatRecyclerAdapter.ViewHolder> {

    private List<Chat> mChatList;
    private List<ChatInfo> mChatInfo;
    private Context mContext;

    private String mPhone = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();

    public TransferChatRecyclerAdapter(List<Chat> ChatList, List<ChatInfo> ChatInfo, Context context) {
        this.mChatList = ChatList;
        this.mChatInfo = ChatInfo;
        this.mContext = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View layoutView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.user_item, null, false);
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutView.setLayoutParams(lp);

        ViewHolder rcv = new ViewHolder(layoutView);
        return rcv;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {

        //load info on view
        if (mChatInfo.get(position).getType().equals("single")) {
            //set user Image and name
            DatabaseReference mChatUsersDB = FirebaseDatabase.getInstance().getReference().child("CHAT").child(mChatList.get(position).getChatId()).child("info").child("users");
            mChatUsersDB.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists())
                        for (DataSnapshot dc : dataSnapshot.getChildren())
                            if (!dc.getKey().equals(mPhone)) {
                                User user = dc.getValue(User.class);

                                Glide.with(mContext).load(Uri.parse(user.getImage_uri())).into(holder.mImageView);
                                holder.mContactName.setText(user.getName());

                                mChatInfo.get(holder.getAdapterPosition()).setImage(user.getImage_uri());
                                mChatInfo.get(holder.getAdapterPosition()).setName(user.getName());
                            }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        } else { //its a group chat

            Glide.with(mContext).load(Uri.parse(mChatInfo.get(position).getImage())).into(holder.mImageView);
            holder.mContactName.setText(mChatInfo.get(position).getName());
        }

        //select a chat
        holder.mIsSelected.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mChatList.get(position).setSelected(isChecked);
            }
        });
    }


    @Override
    public int getItemCount() {
        return mChatList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView mImageView;
        public TextView mContactName;
        public CheckBox mIsSelected;

        public ViewHolder(View view) {
            super(view);

            mImageView = itemView.findViewById(R.id.friend_image);
            mContactName = itemView.findViewById(R.id.friend_name);
            mIsSelected = itemView.findViewById(R.id.add);
        }
    }


}