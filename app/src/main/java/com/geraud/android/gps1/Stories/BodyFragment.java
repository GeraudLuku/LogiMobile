package com.geraud.android.gps1.Stories;


import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.geraud.android.gps1.Models.Stories;
import com.geraud.android.gps1.R;
import com.geraud.android.gps1.RecyclerAdapter.StoriesRecyclerAdapter;
import com.geraud.android.gps1.Utils.Contacts;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class BodyFragment extends Fragment {

    private static final int REQUEST_CODE = 0;

    private DatabaseReference mDatabaseReference;

    private ArrayList<Stories> mStoriesList;
    private Contacts mContacts;
    private StoriesRecyclerAdapter mStoriesRecyclerView;

    private Stories mStories;
    private Stories mStory;

    private Context mContext;

    public BodyFragment() {
        // Required empty public constructor
    }

    //this is the fragment for the body( Stories ) of the navigation bar
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_body, container, false);
        mContext = getContext();

        mContacts = new Contacts(mContext);

        mStories = new Stories();
        mStory = new Stories();
        mStoriesList = new ArrayList<>();
        mStoriesRecyclerView = new StoriesRecyclerAdapter(mStoriesList, getContext());

        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("STORIES");

        RecyclerView mRecyclerView = view.findViewById(R.id.story_body_recycler);
        mRecyclerView.setAdapter(mStoriesRecyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        //cycle via all mContactList and get their mStories with a childEventListener
        if (checkPermission()) {
            for (String contact : mContacts.getAllContacts()) {
                mDatabaseReference.child(contact).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot dc : dataSnapshot.getChildren()) {
                                mStory = dc.getValue(Stories.class);
                                mStories.addStoryToArray(mStory);
                            }
                            mStoriesList.add(mStories);
                            mStoriesRecyclerView.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(mContext, "BodyStories ValueEventListener Cancelled", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } else
            requestPermission();


        return view;
    }

    private boolean checkPermission() {

        return ActivityCompat.checkSelfPermission(mContext, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    for (String contact : mContacts.getAllContacts()) {
                        mDatabaseReference.child(contact).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    for (DataSnapshot dc : dataSnapshot.getChildren()) {
                                        mStory = dc.getValue(Stories.class);
                                        mStories.addStoryToArray(mStory);
                                    }
                                    mStoriesList.add(mStories);
                                    mStoriesRecyclerView.notifyDataSetChanged();
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Toast.makeText(mContext, "BodyStories ValueEventListener Cancelled", Toast.LENGTH_SHORT).show();
                            }
                        });
                        Toast.makeText(mContext, "Contact permission Granted", Toast.LENGTH_SHORT).show();
                    }
                }
        }

    }
}
