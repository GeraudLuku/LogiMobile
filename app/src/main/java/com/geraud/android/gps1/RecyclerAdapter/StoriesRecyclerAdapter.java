package com.geraud.android.gps1.RecyclerAdapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.geraud.android.gps1.Models.Stories;
import com.geraud.android.gps1.Models.User;
import com.geraud.android.gps1.R;
import com.geraud.android.gps1.Stories.FullScreenStoryActivity;
import com.geraud.android.gps1.Utils.TimeAgo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class StoriesRecyclerAdapter extends RecyclerView.Adapter<StoriesRecyclerAdapter.ViewHolder> {
    private ArrayList<Stories> storiesList;
    private Context context;
    private ViewHolder holder;

    public StoriesRecyclerAdapter(ArrayList<Stories> storiesList, Context context) {
        this.storiesList = storiesList;
        this.context = context;
    }

    @NonNull
    @Override
    public StoriesRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.story_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final StoriesRecyclerAdapter.ViewHolder holder, final int position) {
        holder.setIsRecyclable(true);
        this.holder = holder;

        //set name
        FirebaseDatabase.getInstance().getReference().child("user").child(storiesList.get(position).getStoryObjectArrayList().get(storiesList.get(position).getCount() - 1).getPhone())
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
        holder.setLastImage(storiesList.get(position).getStoryObjectArrayList().get(storiesList.get(position).getCount() - 1).getMedia());

        //set time ago
        holder.setTimeStamp(storiesList.get(position).getStoryObjectArrayList().get(storiesList.get(position).getCount() - 1).getTimestamp());

        //set onClick action
        holder.lastImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //send stories object to viewpager activity
                Intent intent = new Intent(v.getContext(),FullScreenStoryActivity.class);
                intent.putExtra("story",storiesList.get(position));
                v.getContext().startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return storiesList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private View mView;
        private TextView name;
        private TextView timeStamp;
        private ImageView lastImage;

        public ViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setName(String text) {
            name = mView.findViewById(R.id.story_name);
            name.setText(text);
        }

        public void setTimeStamp(long timeStamp) {
            this.timeStamp = mView.findViewById(R.id.story_timestamp);
            this.timeStamp.setText(TimeAgo.getTimeAgo(timeStamp));
        }

        public void setLastImage(String uri) {
            lastImage = mView.findViewById(R.id.story_image);
            Glide.with(context).load(uri).into(lastImage);
        }
    }
}

