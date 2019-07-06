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

import com.geraud.android.gps1.GoogleMap.MapsActivity;
import com.geraud.android.gps1.Models.Stories;
import com.geraud.android.gps1.R;
import com.geraud.android.gps1.RecyclerAdapter.StoriesRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;


public class BodyFragment extends Fragment {

    private static final int REQUEST_CODE = 0;

    private DatabaseReference mDatabaseReference;

    private ArrayList<Stories> mStoriesList;
    private ArrayList<String> mContacts;
    private StoriesRecyclerAdapter storiesRecyclerAdapter;

    private Stories mStories;

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

        mContacts = Objects.requireNonNull(getArguments(),"body bundle is null").getStringArrayList(MapsActivity.CONTACTS);

        mStories = new Stories();
        mStoriesList = new ArrayList<>();

        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("STORY");

        storiesRecyclerAdapter = new StoriesRecyclerAdapter(mStoriesList, mContext);
        RecyclerView mRecyclerView = view.findViewById(R.id.story_body_recycler);
        mRecyclerView.setAdapter(storiesRecyclerAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        //cycle via all mContactList and get their mStories with a childEventListener
        if (checkPermission())
            loadStories();
        else
            requestPermission();

        return view;
    }

    private void loadStories() {
        for (String contact : mContacts)
            mDatabaseReference.child(contact).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot dc : dataSnapshot.getChildren())
                            mStories.addStoryToArray(dc.getValue(Stories.class));

                        mStoriesList.add(mStories);
                        storiesRecyclerAdapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(mContext, "BodyStories ValueEventListener Cancelled", Toast.LENGTH_SHORT).show();
                }
            });
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
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    loadStories();
        }

    }
}
