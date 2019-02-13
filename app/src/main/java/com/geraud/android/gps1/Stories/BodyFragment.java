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
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class BodyFragment extends Fragment {

    private RecyclerView recyclerView;
    private ArrayList<Stories> storiesList;
    private StoriesRecyclerAdapter storiesRecyclerAdapter;
    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child("stories");
    private FirebaseAuth auth = FirebaseAuth.getInstance();

    private Stories stories = new Stories();
    private Stories story;

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

        storiesList = new ArrayList<>();
        storiesRecyclerAdapter = new StoriesRecyclerAdapter(storiesList, getContext());

        recyclerView = view.findViewById(R.id.story_body_recycler);
        recyclerView.setAdapter(storiesRecyclerAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        //cycle via all mContactList and get thier stories with a childEventListener
        for (String user : contacts.getAllContacts()) {

            mDatabase.child(user).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot dc : dataSnapshot.getChildren()) {
                            story = dc.getValue(Stories.class);
                            stories.addStoryToArray(story);
                        }
                        storiesList.add(stories);
                        storiesRecyclerAdapter.notifyDataSetChanged();
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
