package com.geraud.android.gps1.Chat;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;

import com.geraud.android.gps1.Models.User;
import com.geraud.android.gps1.R;
import com.geraud.android.gps1.RecyclerAdapter.TransferRecyclerAdapter;
import com.geraud.android.gps1.Utils.Contacts;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class TransferActivity extends AppCompatActivity {

    private RecyclerView mContactsRecyclerView;
    private RecyclerView.Adapter mTransferRecyclerAdapter;
    private RecyclerView.LayoutManager mTransferLayoutManager;

    private Button mSendBtn;

    private List<String> mContactList = new ArrayList<>();
    private List<User> mUserList = new ArrayList<>();

    private String description, mediaUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer);

        description = getIntent().getStringExtra("text");
        mediaUri = getIntent().getStringExtra("uri");

        mSendBtn = findViewById(R.id.sendBtn);
        mSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareData();
            }
        });

        initializeRecyclerView();

        mContactList = new Contacts(this).getAllContacts();
        for (String contact : mContactList) {
            getUserDetails(contact);
        }


    }

    private void initializeRecyclerView() {
        mContactsRecyclerView = findViewById(R.id.recyclerView);
        mContactsRecyclerView.setNestedScrollingEnabled(false);
        mContactsRecyclerView.setHasFixedSize(false);
        mTransferLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        mContactsRecyclerView.setLayoutManager(mTransferLayoutManager);
        mTransferRecyclerAdapter = new TransferRecyclerAdapter(this, mUserList);
        mContactsRecyclerView.setAdapter(mTransferRecyclerAdapter);
    }

    private void getUserDetails(String contact) {
        DatabaseReference mUserDB = FirebaseDatabase.getInstance().getReference().child("USER");
        Query query = mUserDB.orderByChild("phone").equalTo(contact);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot dc : dataSnapshot.getChildren()) {
                        User mUser = dc.getValue(User.class);
                        mUserList.add(mUser);
                        mTransferRecyclerAdapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void shareData() {
        Boolean valid = false;
        int count = 0; //number of users selected
        for (User user : mUserList) {
            if (user.getSelected()) {
                valid = true;
                count++;
                //send it here
                send(user);
            }
        }
        finish();
    }

    private void send(User user) {
        //here get the uri data and send them to the user
    }
}
