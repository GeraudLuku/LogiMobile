package com.geraud.android.gps1.Chat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.geraud.android.gps1.Dailogs.AddGroupChat;
import com.geraud.android.gps1.Models.Message;
import com.geraud.android.gps1.Models.User;
import com.geraud.android.gps1.R;
import com.geraud.android.gps1.RecyclerAdapter.UserListAdapter;
import com.geraud.android.gps1.Utils.Contacts;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FindUserActivity extends AppCompatActivity implements AddGroupChat.AddGroupChatListener {
    private static final int REQUEST_CODE = 1;

    private RecyclerView mUserListRecyclerView;
    private RecyclerView.Adapter mUserListAdapter;
    private RecyclerView.LayoutManager mUserListLayoutManager;

    private DatabaseReference mChatInfoDB;
    private DatabaseReference mUserDB;

    private HashMap mNewChatMap = new HashMap();
    private List<User> mUserList;

    private String key;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_user);

        // Find the toolbar view inside the activity layout
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ImageButton createChatBtn = toolbar.findViewById(R.id.createChat);
        createChatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createChat();
            }
        });

        mUserList = new ArrayList<>();
        initializeRecyclerView();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_CONTACTS, Manifest.permission.READ_CONTACTS}, REQUEST_CODE);
            } else
                for (String contact : new Contacts(this).getAllContacts())
                    getUserDetails(contact);

    }

    private void initializeRecyclerView() {
        mUserListRecyclerView = findViewById(R.id.userList);
        mUserListRecyclerView.setNestedScrollingEnabled(false); //make recycler view scroll seamlessly
        mUserListRecyclerView.setHasFixedSize(false);// out of preferrence
        mUserListRecyclerView.addItemDecoration(new DividerItemDecoration(mUserListRecyclerView.getContext(), DividerItemDecoration.VERTICAL)); // list divider
        mUserListLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        mUserListRecyclerView.setLayoutManager(mUserListLayoutManager);
        mUserListAdapter = new UserListAdapter(mUserList, this);
        mUserListRecyclerView.setAdapter(mUserListAdapter);
    }

    private void createChat() {
        key = FirebaseDatabase.getInstance().getReference().child("CHAT").push().getKey();

        mUserDB = FirebaseDatabase.getInstance().getReference().child("USER");
        mChatInfoDB = FirebaseDatabase.getInstance().getReference().child("CHAT").child(key).child("info");

        Message message = new Message(null, "Open to start chatting", null, System.currentTimeMillis(), null);

        mNewChatMap.put("id", key);
        mNewChatMap.put("lastMessage/", message);
        mNewChatMap.put("users/" + FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber(), true);

        boolean validChat = false;
        int count = 0; //number of users selected
        for (User mUser : mUserList) {
            if (mUser.getSelected()) {
                validChat = true;
                mNewChatMap.put("users/" + mUser.getPhone(), true);
                mUserDB.child(mUser.getPhone()).child("chat").child(key).setValue(true); //chat id set in all the users documents
                count++;
            }
        }

        if (validChat) {
            if (count > 1) { //means its a group chat and we should promt the user to provide a name for it and also an image
                CreateGroupChat();
            } else {
                mNewChatMap.put("name", null);
                mNewChatMap.put("type", "single");
                mNewChatMap.put("image", null);

                mChatInfoDB.updateChildren(mNewChatMap);
                mUserDB.child(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber()).child("chat").child(key).setValue(true); //chat id set in my Document
                finish();
            }
        } else
            Toast.makeText(this, "Not A valid chat", Toast.LENGTH_SHORT).show();

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
                        mUserListAdapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    //open add place dailog
    public void CreateGroupChat() {
        AddGroupChat groupChat = new AddGroupChat();
        groupChat.show(getSupportFragmentManager(), "Add Group Chat");
    }

    @Override
    public void applyGroupInfo(String name, Uri image_uri) {
        //create a new field for group name and group icon/image
        mNewChatMap.put("type", "group");
        mNewChatMap.put("name", name);
        mNewChatMap.put("image", image_uri);

        mChatInfoDB.updateChildren(mNewChatMap);
        mUserDB.child(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber()).child("chat").child(key).setValue(true);
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_CODE:
                if (grantResults.length > 0) {

                    boolean writeContacts = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean readContacts = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if (writeContacts && readContacts) {
                        for (String contact : new Contacts(this).getAllContacts())
                            getUserDetails(contact);
                        Toast.makeText(getApplicationContext(), "Contact Permission Granted", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "Contact Permission Denied", Toast.LENGTH_LONG).show();
                    }
                }
        }
    }
}
