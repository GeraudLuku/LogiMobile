package com.geraud.android.gps1.Stories;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

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


public class HeaderFragment extends Fragment {

    private ImageView images;
    private Button add;
    private TextView textSnippet;
    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child("stories");
    private FirebaseAuth auth = FirebaseAuth.getInstance();

    private Stories stories = new Stories();
    private Stories story;

    public HeaderFragment() {
        // Required empty public constructor
    }


    //this is the fragment for the header( Stories ) of the navigation bar
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_header, container, false);

        images = view.findViewById(R.id.add_status_image);
        add = view.findViewById(R.id.add_status_button);
        textSnippet = view.findViewById(R.id.add_status_text);

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Intent(getContext(),CameraActivity.class);
            }
        });

        //load users status media from firebase
        mDatabase.child(auth.getCurrentUser().getPhoneNumber()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    for (DataSnapshot dc : dataSnapshot.getChildren()) {
                        story = dc.getValue(Stories.class);
                        stories.addStoryToArray(story);
                    }
                    Glide.with(getContext())
                            .load(story.getMedia())
                            .into(images);
                    textSnippet.setText(TimeAgo.getTimeAgo(story.getTimestamp()));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        return view;
    }

}
