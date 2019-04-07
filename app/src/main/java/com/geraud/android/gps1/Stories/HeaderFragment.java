package com.geraud.android.gps1.Stories;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.geraud.android.gps1.Camera.CameraActivity;
import com.geraud.android.gps1.Models.Stories;
import com.geraud.android.gps1.R;
import com.geraud.android.gps1.Utils.TimeAgo;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;


public class HeaderFragment extends Fragment {

    private ImageView mImageView;
    private TextView mTextSnippet;

    private Stories mStories = new Stories();
    private Stories mStory = new Stories();

    private Context mContext;

    public HeaderFragment() {
    }

    //this is the fragment for the header( Stories ) of the navigation bar
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_header, container, false);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("STORIES");
        String userPhone = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser(),"Current User Cant Be NUll").getPhoneNumber();

        mImageView = view.findViewById(R.id.add_status_image);
        mTextSnippet = view.findViewById(R.id.add_status_text);

        mContext = getContext();

        Button mAddStatusButton = view.findViewById(R.id.add_status_button);
        mAddStatusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Intent(mContext,CameraActivity.class);
            }
        });

        //load users status media from firebase
        if (userPhone != null) {
            databaseReference.child(userPhone).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()){
                        for (DataSnapshot dc : dataSnapshot.getChildren()) {
                            mStory = dc.getValue(Stories.class);
                            mStories.addStoryToArray(mStory);
                        }
                        Glide.with(mContext)
                                .load(mStory.getMedia()) //last image gotten
                                .into(mImageView);
                        mTextSnippet.setText(TimeAgo.getTimeAgo(mStory.getTimestamp()));
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(mContext, "HeaderStories ValueEventListener Cancelled", Toast.LENGTH_SHORT).show();
                }
            });
        }

        return view;
    }

}
