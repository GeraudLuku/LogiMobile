package com.geraud.android.gps1.Chat;

import android.Manifest;
import android.content.Intent;
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
import com.geraud.android.gps1.Models.Chat;
import com.geraud.android.gps1.Models.ChatInfo;
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
import java.util.Objects;

public class FindUserActivity extends AppCompatActivity implements AddGroupChat.AddGroupChatListener {
    private static final int REQUEST_CODE = 1;

    private RecyclerView.Adapter mUserListAdapter;

    private DatabaseReference mChatInfoDB;
    private DatabaseReference mUserDB;

    private HashMap<String, Object> mNewChatMap;
    private HashMap<String, String> mGroupChatMap;
    private HashMap<String, String> mMyChatMap;
    private HashMap<String, String> mSingleChatMap;


    private String key;
    private String mUserPhone;

    private List<User> mUserList;
    private List<Chat> mChatList;
    private List<ChatInfo> mChatInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_user);

        // Find the toolbar view inside the activity layout
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mChatList = new ArrayList<>();
        mChatInfo = new ArrayList<>();
        mUserList = new ArrayList<>();

        mNewChatMap = new HashMap<>();
        mGroupChatMap = new HashMap<>();
        mMyChatMap = new HashMap<>();
        mSingleChatMap = new HashMap<>();

        mUserPhone = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser(), "Current User Cant Be Null").getPhoneNumber();

        ImageButton createChatBtn = toolbar.findViewById(R.id.createChat);
        createChatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createChat();
            }
        });

        initializeRecyclerView();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_CONTACTS, Manifest.permission.READ_CONTACTS}, REQUEST_CODE);
            } else
                for (String contact : new Contacts(this).getAllContacts())
                    getUserDetails(contact);

    }

    private void initializeRecyclerView() {
        RecyclerView userListRecyclerView = findViewById(R.id.userList);
        userListRecyclerView.setNestedScrollingEnabled(false); //make recycler view scroll seamlessly
        userListRecyclerView.setHasFixedSize(false);// out of preferrence
        userListRecyclerView.addItemDecoration(new DividerItemDecoration(userListRecyclerView.getContext(), DividerItemDecoration.VERTICAL)); // list divider
        RecyclerView.LayoutManager userListLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        userListRecyclerView.setLayoutManager(userListLayoutManager);
        mUserListAdapter = new UserListAdapter(mUserList, this);
        userListRecyclerView.setAdapter(mUserListAdapter);
    }

    private void getUserDetails(String contact) {
        DatabaseReference mUserDB = FirebaseDatabase.getInstance().getReference().child("USER").child("userInfo");
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
                Toast.makeText(FindUserActivity.this, "getUserDetails ValueEventListener Cancelled", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createChat() {
        key = FirebaseDatabase.getInstance().getReference().child("CHAT").push().getKey();

        mUserDB = FirebaseDatabase.getInstance().getReference().child("USER");
        mChatInfoDB = FirebaseDatabase.getInstance().getReference().child("CHAT").child(key).child("info");

        //default message for a new chat
        Message message = new Message(null, "Open to start chatting", null, System.currentTimeMillis(), null);

        mNewChatMap.put("id", key);
        mNewChatMap.put("lastMessage/", message);
        mNewChatMap.put("users/" + mUserPhone, true);

        mMyChatMap.put("type", "single");

        mGroupChatMap.put("type", "group");

        mSingleChatMap.put("type", "single");
        mSingleChatMap.put("user", mUserPhone);

        boolean validChat = false;
        int count = 0; //number of users selected
        for (User mUser : mUserList)
            if (mUser.getSelected()) {
                validChat = true;
                mNewChatMap.put("users/" + mUser.getPhone(), true);
                mMyChatMap.put("user", mUser.getPhone());
                count++;
            }


        if (validChat) {

            if (count > 1) { //means its a group chat and we should prompt the user to provide a name for it and also an image
                for (User mUser : mUserList)
                    if (mUser.getSelected())
                        mUserDB.child(mUser.getPhone()).child("chat").child(key).setValue(mGroupChatMap); //chat id set in all the users documents
                CreateGroupChat();
            } else {
                //if it doesn't exist create a new chat then......
                mNewChatMap.put("name", null);
                mNewChatMap.put("type", "single");
                mNewChatMap.put("image", null);

                mChatInfoDB.updateChildren(mNewChatMap);
                mUserDB.child(mUserPhone).child("chat").child(key).setValue(mMyChatMap); //chat id set in my Document
                mUserDB.child(Objects.requireNonNull(mMyChatMap.get("user"), "myChatMap user can't be NUll")).child("chat").child(key).setValue(mSingleChatMap); // set chat id in the other users document

                //open chatActivity for this newly created chat
                Chat chat = new Chat(key);
                mChatList.add(chat);
                getChatData(chat.getChatId());

            }
        } else
            Toast.makeText(this, "Not A valid chat", Toast.LENGTH_SHORT).show();

    }

    //open add place dailog
    public void CreateGroupChat() {
        AddGroupChat groupChat = new AddGroupChat();
        groupChat.show(getSupportFragmentManager(), "Create Group Chat");
    }

    @Override
    public void applyGroupInfo(String name, Uri image_uri) {
        //create a new field for group name and group icon/image
        mNewChatMap.put("type", "group");
        mNewChatMap.put("name", name);
        mNewChatMap.put("image", image_uri);

        mChatInfoDB.updateChildren(mNewChatMap);
        mUserDB.child(mUserPhone).child("chat").child(key).setValue(mGroupChatMap);

        //open activity for this chat
        Chat chat = new Chat(key);
        mChatList.add(chat);
        getChatData(chat.getChatId());
    }

    // this is where you will get the info of all the chats including the designator , last message and timestamp
    private void getChatData(String chatId) {
        DatabaseReference mChatDB = FirebaseDatabase.getInstance().getReference().child("CHAT").child(chatId).child("info");
        mChatDB.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    ChatInfo chatInfo = dataSnapshot.getValue(ChatInfo.class);
                    mChatInfo.add(chatInfo);
                    String chatId = "";

                    //getting id of the chat
                    if (dataSnapshot.child("id").getValue() != null)
                        chatId = Objects.requireNonNull(dataSnapshot.child("id").getValue(), "id can't be null").toString();

                    //getting all users
                    for (DataSnapshot userSnaphshot : dataSnapshot.child("users").getChildren())
                        for (Chat mChat : mChatList) {
                            if (mChat.getChatId().equals(chatId)) {
                                User mUser = new User(userSnaphshot.getKey());
                                mChat.addUserToArrayList(mUser);
                                getUserData(mUser);
                            }
                        }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(FindUserActivity.this, "getChatData ValueEventListener Cancelled", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void getUserData(User mUser) {
        DatabaseReference mUserDb = FirebaseDatabase.getInstance().getReference().child("USER").child(mUser.getPhone());
        mUserDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User mUser = new User(dataSnapshot.getKey());
                //getting notification key
                if (dataSnapshot.child("notificationKey").getValue() != null)
                    mUser.setNotificationKey(Objects.requireNonNull(dataSnapshot.child("notificationKey").getValue(), "NotificationKey Can't be null").toString());

                for (Chat mChat : mChatList) {
                    for (User mUserIt : mChat.getUserObjectArrayList()) {
                        if (mUserIt.getPhone().equals(mUser.getPhone())) {
                            mUserIt.setNotificationKey(mUser.getNotificationKey());
                        }
                    }
                }
                // create intent here and send extras (mChatinfo,mChatList)
                Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
                intent.putExtra("chatObject", mChatList.get(0));  // get the first document i the list because there is only one document
                intent.putExtra("chatInfoObject", mChatInfo.get(0));
                startActivity(intent);
                finish();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(FindUserActivity.this, "getUserData ValueEventListener Cancelled", Toast.LENGTH_SHORT).show();
            }
        });
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
