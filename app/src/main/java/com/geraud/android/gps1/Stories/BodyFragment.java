package com.geraud.android.gps1.Stories;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.geraud.android.gps1.Models.Stories;
import com.geraud.android.gps1.R;
import com.geraud.android.gps1.RecyclerAdapter.StoriesRecyclerAdapter;
import com.geraud.android.gps1.Utils.Contacts;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class BodyFragment extends Fragment {

    private RecyclerView mRecyclerView;
    private ArrayList<Stories> mStoriesList;
    private StoriesRecyclerAdapter mStoriesRecyclerView;

    private DatabaseReference mDatabaseReference;

    private Stories mStories;
    private Stories mStory;

    public BodyFragment() {
        // Required empty public constructor
    }

    //this is the fragment for the body( Stories ) of the navigation bar
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_body, container, false);

        Contacts contacts = new Contacts(getContext());

        mStories = new Stories();
        mStory = new Stories();
        mStoriesList = new ArrayList<>();
        mStoriesRecyclerView = new StoriesRecyclerAdapter(mStoriesList, getContext());

        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("STORIES");

        mRecyclerView = view.findViewById(R.id.story_body_recycler);
        mRecyclerView.setAdapter(mStoriesRecyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        //cycle via all mContactList and get their mStories with a childEventListener
        for (String contact : contacts.getAllContacts()) {

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

                }
            });

        }


        return view;
    }

}
