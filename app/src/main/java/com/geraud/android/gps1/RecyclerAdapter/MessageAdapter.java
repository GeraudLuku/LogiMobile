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

import com.geraud.android.gps1.Models.Message;
import com.geraud.android.gps1.R;
import com.geraud.android.gps1.Utils.TimeAgo;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.stfalcon.frescoimageviewer.ImageViewer;

import java.net.URI;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private static final int GROUP_CHAT_MESSAGE = 0;
    private static final int SINGLE_CHAT_MESSAGE = 1;
    private static final int MY_MESSAGE = 2;

    private List<Message> mMessageList;
    private Context mContext;

    private View mLayoutView;
    private RecyclerView.LayoutParams mLayoutParams;

    private String mPhoneNumber = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();
    private DatabaseReference mUserDatabase = FirebaseDatabase.getInstance().getReference().child("USER");

    public MessageAdapter(List<Message> Message, Context context) {
        this.mMessageList = Message;
        this.mContext = context;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        switch (viewType) {
            case GROUP_CHAT_MESSAGE:
                mLayoutView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.group_message, null, false);
                mLayoutParams = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                mLayoutView.setLayoutParams(mLayoutParams);
                break;
            case SINGLE_CHAT_MESSAGE:
                mLayoutView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.chat_message, null, false);
                mLayoutParams = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                mLayoutView.setLayoutParams(mLayoutParams);
                break;
            case MY_MESSAGE:
                mLayoutView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.my_message, null, false);
                mLayoutParams = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                mLayoutView.setLayoutParams(mLayoutParams);
                break;
            default:
                Toast.makeText(mContext, "Couldn't find any view type for the received message", Toast.LENGTH_SHORT).show();
        }

        MessageViewHolder rcv = new MessageViewHolder(mLayoutView);
        return rcv;
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder holder, final int position) {

        holder.mMessage.setText(mMessageList.get(position).getText());

        mUserDatabase.child(mMessageList.get(position).getSenderId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists())
                    for (DataSnapshot childsnapshot : dataSnapshot.getChildren())
                        if (childsnapshot.child("name").getValue() != null && childsnapshot.child("image_uri").getValue() != null) {
                            holder.mSenderName.setText(childsnapshot.child("name").getValue().toString());
                            holder.mImageView.setImageURI(Uri.parse(childsnapshot.child("image_uri").getValue().toString()));
                        }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        if (!mMessageList.get(holder.getAdapterPosition()).getMediaUrlist().isEmpty()) {
            holder.mViewMedia.setVisibility(View.VISIBLE);
            holder.mViewMedia.setText(mMessageList.get(position).getMediaUrlist().size());
        }

        holder.mViewMedia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show media files with FRESCO
                new ImageViewer.Builder(mContext, mMessageList.get(holder.getAdapterPosition()).getMediaUrlist())
                        .setStartPosition(0)
                        .show();
            }
        });

        holder.mTimeStamp.setText(TimeAgo.getTime(mMessageList.get(position).getTimestamp()));
    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {
        private TextView mMessage,
                mSenderName, mTimeStamp;
        private Button mViewMedia;
        private ImageView mImageView;

        public MessageViewHolder(View view) {
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
            if (mMessageList.get(position).getSenderId() == mPhoneNumber) {
                return MY_MESSAGE;
            } else {
                return GROUP_CHAT_MESSAGE;
            }
        } else if (mMessageList.get(position).mChatType.equals("single")) {
            if (mMessageList.get(position).getSenderId() == mPhoneNumber) {
                return MY_MESSAGE;
            } else {
                return SINGLE_CHAT_MESSAGE;
            }
        }
        return MY_MESSAGE;
    }
}


