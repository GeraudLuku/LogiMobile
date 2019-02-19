package com.geraud.android.gps1.RecyclerAdapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.geraud.android.gps1.Models.Stories;
import com.geraud.android.gps1.Models.User;
import com.geraud.android.gps1.R;
import com.geraud.android.gps1.Stories.FullScreenStoryActivity;
import com.geraud.android.gps1.Utils.TimeAgo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class StoriesRecyclerAdapter extends RecyclerView.Adapter<StoriesRecyclerAdapter.ViewHolder> {
    private ArrayList<Stories> mStoriesList;
    private Context mContext;

    private DatabaseReference mDatabaseReference;

    public StoriesRecyclerAdapter(ArrayList<Stories> storiesList, Context context) {
        mStoriesList = storiesList;
        mContext = context;
        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("USER");
    }

    @NonNull
    @Override
    public StoriesRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.story_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final StoriesRecyclerAdapter.ViewHolder holder, final int position) {
        holder.setIsRecyclable(true);

        //set mName
        mDatabaseReference.child(mStoriesList.get(position).getStoryObjectArrayList().get(mStoriesList.get(position).getCount() - 1).getPhone())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            for (DataSnapshot dc : dataSnapshot.getChildren()){
                                User user = dc.getValue(User.class);
                                holder.setName(user.getName());
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

        //set image
        holder.setLastImage(mStoriesList.get(position).getStoryObjectArrayList().get(mStoriesList.get(position).getCount() - 1).getMedia());

        //set time ago
        holder.setTimeStamp(mStoriesList.get(position).getStoryObjectArrayList().get(mStoriesList.get(position).getCount() - 1).getTimestamp());

        //set onClick action
        holder.mStory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //send stories object to viewpager activity
                Intent intent = new Intent(v.getContext(),FullScreenStoryActivity.class);
                intent.putExtra("story", mStoriesList.get(position));
                v.getContext().startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return mStoriesList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private View mView;
        private TextView mName, mTimeStamp;
        private ImageView mLastImage;
        private RelativeLayout mStory;

        public ViewHolder(View itemView) {
            super(itemView);
            mView = itemView;

            mTimeStamp = mView.findViewById(R.id.story_timestamp);
            mLastImage = mView.findViewById(R.id.story_image);
            mName = mView.findViewById(R.id.story_name);
            mStory = mView.findViewById(R.id.story);
        }

        public void setName(String text) {
            mName.setText(text);
        }

        public void setTimeStamp(long timeStamp) {
            mTimeStamp.setText(TimeAgo.getTimeAgo(timeStamp));
        }

        public void setLastImage(String uri) {
            Glide.with(mContext).load(uri).into(mLastImage);
        }
    }
}

