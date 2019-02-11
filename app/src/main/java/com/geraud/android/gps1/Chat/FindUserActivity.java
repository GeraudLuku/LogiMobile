package com.geraud.android.gps1.Chat;

import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

    private RecyclerView mUserList;
    private RecyclerView.Adapter mUserListAdapter;
    private RecyclerView.LayoutManager mUserListLayoutManager;

    DatabaseReference chatInfoDb;
    DatabaseReference userDb;

    HashMap newChatMap = new HashMap();

    String key;

    List<String> contactList = new ArrayList<>();
    List<User> userList = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_user);

        initializeRecyclerView();

        contactList = new Contacts(this).getAllContacts();
        for (String contact : contactList){
            getUserDetails(contact);
        }

    }

    private void createChat(){

        key = FirebaseDatabase.getInstance().getReference().child("chat").push().getKey();
        userDb = FirebaseDatabase.getInstance().getReference().child("user");
        chatInfoDb = FirebaseDatabase.getInstance().getReference().child("chat").child(key).child("info");

        newChatMap.put("id",key);
        newChatMap.put("users/" + FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber(),true);

        Boolean validChat = false;
        int count = 0; //number of users selected
        for (User mUser : userList){
            if (mUser.getSelected()){
                validChat = true;
                count++;
                newChatMap.put("users/" + mUser.getPhone(),true);
                userDb.child(mUser.getPhone()).child("chat").child(key).setValue(true);
            }
        }

        if (validChat) {
            if (count > 1){
                //means its a group chat and we should promt the user to provide a name for it and also an image
                CreateGroupChat();
            }else {
                chatInfoDb.updateChildren(newChatMap);
                userDb.child(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber()).child("chat").child(key).setValue(true);
                finish();
            }
        }else
            Toast.makeText(this,"Not A valid chat",Toast.LENGTH_LONG).show();

    }

    private void getUserDetails(String contact) {
        DatabaseReference mUserDB = FirebaseDatabase.getInstance().getReference().child("user");
        Query query = mUserDB.orderByChild("phone").equalTo(contact);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    for (DataSnapshot dc : dataSnapshot.getChildren()){
                        User mUser = dc.getValue(User.class);
                        userList.add(mUser);
                        mUserListAdapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void initializeRecyclerView() {
        mUserList = findViewById(R.id.userList);
        mUserList.setNestedScrollingEnabled(false);
        mUserList.setHasFixedSize(false);
        mUserListLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL , false);
        mUserList.setLayoutManager(mUserListLayoutManager);
        mUserListAdapter = new UserListAdapter(userList, this);
        mUserList.setAdapter(mUserListAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.find_user_activity, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
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
        newChatMap.put("name",name);
        newChatMap.put("image",image_uri);

        chatInfoDb.updateChildren(newChatMap);
        userDb.child(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber()).child("chat").child(key).setValue(true);

        finish();
    }
}
