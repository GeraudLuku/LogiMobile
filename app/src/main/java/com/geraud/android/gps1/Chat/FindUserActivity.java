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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.geraud.android.gps1.Dailogs.Add_GroupChat;
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

public class FindUserActivity extends AppCompatActivity implements Add_GroupChat.AddGroupChatListener {
    private static final int REQUEST_CODE = 1;

    private RecyclerView mUserListRecyclerView;
    private RecyclerView.Adapter mUserListAdapter;
    private RecyclerView.LayoutManager mUserListLayoutManager;

    DatabaseReference chatInfoDb;
    DatabaseReference userDb;

    HashMap newChatMap = new HashMap();

    String key;

    private List<User> mUserList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_user);

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

        key = FirebaseDatabase.getInstance().getReference().child("chat").push().getKey();
        userDb = FirebaseDatabase.getInstance().getReference().child("user");
        chatInfoDb = FirebaseDatabase.getInstance().getReference().child("chat").child(key).child("info");

        newChatMap.put("id", key);
        newChatMap.put("users/" + FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber(), true);

        Boolean validChat = false;
        int count = 0; //number of users selected
        for (User mUser : mUserList) {
            if (mUser.getSelected()) {
                validChat = true;
                count++;
                newChatMap.put("users/" + mUser.getPhone(), true);
                userDb.child(mUser.getPhone()).child("chat").child(key).setValue(true);
            }
        }

        if (validChat) {
            if (count > 1) {
                //means its a group chat and we should promt the user to provide a name for it and also an image
                CreateGroupChat();
            } else {
                chatInfoDb.updateChildren(newChatMap);
                userDb.child(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber()).child("chat").child(key).setValue(true);
                finish();
            }
        } else
            Toast.makeText(this, "Not A valid chat", Toast.LENGTH_LONG).show();

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


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.find_user_activity, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.createChat:
                createChat();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    //open add place dailog
    public void CreateGroupChat() {

        Add_GroupChat add_groupChat = new Add_GroupChat();
        add_groupChat.show(getSupportFragmentManager(), "Add Group Chat");
        //part 2 right below!!!!
    }

    @Override
    public void applyGroupInfo(String name, Uri image_uri) {
        //create a new field for group name and group icon/image
        newChatMap.put("name", name);
        newChatMap.put("image", image_uri);

        chatInfoDb.updateChildren(newChatMap);
        userDb.child(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber()).child("chat").child(key).setValue(true);

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
