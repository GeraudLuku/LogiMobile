package com.geraud.android.gps1.RecyclerAdapter;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.geraud.android.gps1.Models.Message;
import com.geraud.android.gps1.R;
import com.geraud.android.gps1.Utils.TimeAgo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
import java.util.Objects;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private static final int GROUP_CHAT_MESSAGE = 0;
    private static final int SINGLE_CHAT_MESSAGE = 1;
    private static final int MY_MESSAGE = 2;

    private List<Message> mMessageList;
    private Context mContext;
    private String mUserPhone;

    private View mLayoutView;

    private DatabaseReference mUserDatabase = FirebaseDatabase.getInstance().getReference().child("USER");

    public MessageAdapter(List<Message> Message, Context context, String userPhone) {
        mMessageList = Message;
        mContext = context;
        mUserPhone = userPhone;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        switch (viewType) {
            case GROUP_CHAT_MESSAGE:
                mLayoutView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.group_message, viewGroup, false);
                RecyclerView.LayoutParams layoutParams = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                mLayoutView.setLayoutParams(layoutParams);
                break;
            case SINGLE_CHAT_MESSAGE:
                mLayoutView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.chat_message, viewGroup, false);
                layoutParams = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                mLayoutView.setLayoutParams(layoutParams);
                break;
            case MY_MESSAGE:
                mLayoutView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.my_message, viewGroup, false);
                layoutParams = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                mLayoutView.setLayoutParams(layoutParams);
                break;
            default:
                Toast.makeText(mContext, "Couldn't find any view type for the message", Toast.LENGTH_SHORT).show();
        }

        return new MessageViewHolder(mLayoutView);
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder holder, int position) {

        holder.mMessage.setText(mMessageList.get(position).getText());

        //set name and image
        mUserDatabase.child(mMessageList.get(position).getSenderId()).child("userInfo").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists())
                        if (dataSnapshot.child("name").getValue() != null && dataSnapshot.child("image_uri").getValue() != null) {
                            holder.mSenderName.setText(Objects.requireNonNull(dataSnapshot.child("name").getValue()).toString());
                            Glide.with(mContext).load(Uri.parse(Objects.requireNonNull(dataSnapshot.child("image_uri").getValue()).toString())).into(holder.mImageView);
                        }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(mContext, "set name and image of user ValueEventListener Cancelled", Toast.LENGTH_SHORT).show();
            }
        });

        if (!mMessageList.get(holder.getAdapterPosition()).getMediaUrlist().isEmpty()) {
            holder.mViewMedia.setVisibility(View.VISIBLE);
            holder.mViewMedia.setText(mMessageList.get(position).getMediaUrlist().size());
        }

        holder.mViewMedia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               // ToDo : Find A Way To Distinguish between video and image uri and view dem accordingly
            }
        });

        holder.mTimeStamp.setText(TimeAgo.getTime(mMessageList.get(position).getTimestamp()));
    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }

     class MessageViewHolder extends RecyclerView.ViewHolder {
        private TextView mMessage,
                mSenderName, mTimeStamp;
        private Button mViewMedia;
        private ImageView mImageView;

         MessageViewHolder(View view) {
            super(view);

            mMessage = view.findViewById(R.id.message);
            mImageView = view.findViewById(R.id.image);
            mSenderName = view.findViewById(R.id.name);
            mTimeStamp = view.findViewById(R.id.timestamp);
            mTimeStamp = view.findViewById(R.id.timestamp);
            mViewMedia = view.findViewById(R.id.viewMedia);
        }
    }

    @Override
    public int getItemViewType(int position) {

        if (mMessageList.get(position).mChatType.equals("group")) {
            if (mMessageList.get(position).getSenderId().equals(mUserPhone)) {
                return MY_MESSAGE;
            } else {
                return GROUP_CHAT_MESSAGE;
            }
        } else if (mMessageList.get(position).mChatType.equals("single")) {
            if (mMessageList.get(position).getSenderId().equals(mUserPhone)) {
                return MY_MESSAGE;
            } else {
                return SINGLE_CHAT_MESSAGE;
            }
        }
        return MY_MESSAGE;
    }
}


