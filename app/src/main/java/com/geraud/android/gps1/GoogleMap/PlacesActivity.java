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

    private static final int REQUEST_PERMISSION_CODE = 1;
    private RecyclerView mPlacesRecyclerView;
    private RecyclerView.Adapter mPlaceAdapter;

    private DatabaseReference mDatabaseReference = FirebaseDatabase.getInstance().getReference();
    private String mPhone = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser(), "Current User Cannot Be Null").getPhoneNumber();

    private RadioGroup mRadioGroup;

    private List<Place> mPlaces;
    private List<String> mContactList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_places);

        mRadioGroup = findViewById(R.id.radioGroup);

        initializeRecyclerView();
    }

    public boolean checkPermission() {
        return ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED;

    }

    private void requestPermission() {

        ActivityCompat.requestPermissions(PlacesActivity.this, new String[]
                {
                        Manifest.permission.READ_CONTACTS
                }, REQUEST_PERMISSION_CODE);

    }

    private void initializeRecyclerView() {
        mPlaces = new ArrayList<>();
        mContactList = new ArrayList<>();
        mPlacesRecyclerView = findViewById(R.id.recyclerView);
        mPlacesRecyclerView.setNestedScrollingEnabled(false);
        mPlacesRecyclerView.setHasFixedSize(false);
        mPlacesRecyclerView.addItemDecoration(new DividerItemDecoration(mPlacesRecyclerView.getContext(), DividerItemDecoration.VERTICAL));
        mPlacesRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false));
        mPlaceAdapter = new PlacesRecyclerAdapter(mPlaces, getApplicationContext(),mPhone);
        mPlacesRecyclerView.setAdapter(mPlaceAdapter);
    }

    public void checkButton(View v) {
        int radioId = mRadioGroup.getCheckedRadioButtonId();
        switch (radioId) {
            case R.id.radio_one:
                LoadPlaces();
                break;
            case R.id.radio_two:
                if (checkPermission())
                    LoadFriendsPlaces();
                else
                    requestPermission();
                break;
            default:
                Toast.makeText(getApplicationContext(), "Cant Recognise Radio Option Selected", Toast.LENGTH_SHORT).show();
        }

    }

    private void LoadPlaces() {
        mPlaces.clear(); //first clear the list
        mDatabaseReference.child("PLACES").child(mPhone).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists())
                    for (DataSnapshot dc : dataSnapshot.getChildren()) {
                        Place myPlace = dc.getValue(Place.class);
                        mPlaces.add(myPlace);
                        mPlaceAdapter.notifyDataSetChanged();
                    }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), "Places ValueEventListener-1 Cancelled", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void LoadFriendsPlaces() {
        mPlaces.clear(); //first clear the list

        //for each friend
        for (String contact : mContactList)
            mDatabaseReference.child("PLACES").child(contact).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists())
                        for (DataSnapshot dc : dataSnapshot.getChildren()) {
                            Place myPlace = dc.getValue(Place.class);
                            mPlaces.add(myPlace);
                            mPlaceAdapter.notifyDataSetChanged();
                        }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(getApplicationContext(), "Places ValueEventListener-2 Cancelled", Toast.LENGTH_SHORT).show();
                }
            });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {

            case REQUEST_PERMISSION_CODE:
                boolean readContacts = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                if (readContacts) {
                    if (checkPermission()) {
                        mContactList = new Contacts(getApplicationContext()).getAllContacts();
                    }
                    Toast.makeText(PlacesActivity.this, "Contacts Permission Granted", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(PlacesActivity.this, "Contacts Permission Denied Cant Load COntacts Location", Toast.LENGTH_LONG).show();

                }
                break;
        }
    }
}
