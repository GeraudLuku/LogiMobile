package com.geraud.android.gps1.GoogleMap;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.geraud.android.gps1.Models.Place;
import com.geraud.android.gps1.R;
import com.geraud.android.gps1.RecyclerAdapter.PlacesRecyclerAdapter;
import com.geraud.android.gps1.Utils.Contacts;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PlacesActivity extends AppCompatActivity {
    public static final String CONTACT_LIST = "contacts";

    private RecyclerView.Adapter mPlaceAdapter;

    private DatabaseReference mDatabaseReference;
    private String mPhone;

    public RadioGroup mRadioGroup;

    private List<Place> mPlaces;
    private ArrayList<String> mContactList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_places);

        //get contacts list from intent
        mContactList = getIntent().getStringArrayListExtra(CONTACT_LIST);

        mRadioGroup = findViewById(R.id.radioGroup);
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();
        mPhone = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser(), "Current User Cannot Be Null").getPhoneNumber();

        initializeRecyclerView();
    }

    private void initializeRecyclerView() {
        mPlaces = new ArrayList<>();
        mContactList = new ArrayList<>();
        RecyclerView placesRecyclerView = findViewById(R.id.recyclerView);
        placesRecyclerView.setNestedScrollingEnabled(false);
        placesRecyclerView.setHasFixedSize(false);
        placesRecyclerView.addItemDecoration(new DividerItemDecoration(placesRecyclerView.getContext(), DividerItemDecoration.VERTICAL));
        placesRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false));
        mPlaceAdapter = new PlacesRecyclerAdapter(mPlaces, getApplicationContext(),mPhone);
        placesRecyclerView.setAdapter(mPlaceAdapter);

        LoadPlaces();
    }

    public void checkButton(View v) {
        int radioId = mRadioGroup.getCheckedRadioButtonId();
        switch (radioId) {
            case R.id.radio_one:
                LoadPlaces();
                break;
            case R.id.radio_two:
                LoadFriendsPlaces();
                break;
            default:
                Toast.makeText(getApplicationContext(), "Cant Recognise Radio Option Selected", Toast.LENGTH_SHORT).show();
        }

    }

    private void LoadPlaces() {
        mPlaces.clear(); // clear list
        mPlaceAdapter.notifyDataSetChanged(); // let your adapter know about the changes and reload view.
        mDatabaseReference.child("PLACE").child(mPhone).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists())
                    for (DataSnapshot dc : dataSnapshot.getChildren()) {
                        Place myPlace = dc.getValue(Place.class);
                        mPlaces.add(myPlace);
                        mPlaceAdapter.notifyDataSetChanged();
                    }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), "Places ValueEventListener-1 Cancelled", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void LoadFriendsPlaces() {
        mPlaces.clear(); // clear list
        mPlaceAdapter.notifyDataSetChanged(); // let your adapter know about the changes and reload view.
        //for each friend
        for (String contact : mContactList)
            mDatabaseReference.child("PLACE").child(contact).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists())
                        for (DataSnapshot dc : dataSnapshot.getChildren()) {
                            Place myPlace = dc.getValue(Place.class);
                            mPlaces.add(myPlace);
                            mPlaceAdapter.notifyDataSetChanged();
                        }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(getApplicationContext(), "Places ValueEventListener-2 Cancelled", Toast.LENGTH_SHORT).show();
                }
            });
    }

}
