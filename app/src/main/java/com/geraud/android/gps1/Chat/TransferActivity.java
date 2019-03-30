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
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.geraud.android.gps1.Models.Chat;
import com.geraud.android.gps1.Models.ChatInfo;
import com.geraud.android.gps1.Models.User;
import com.geraud.android.gps1.R;
import com.geraud.android.gps1.RecyclerAdapter.TransferChatRecyclerAdapter;
import com.geraud.android.gps1.RecyclerAdapter.TransferRecyclerAdapter;
import com.geraud.android.gps1.Utils.Contacts;
import com.geraud.android.gps1.Utils.SendNotification;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TransferActivity extends AppCompatActivity {
    private static final int REQUEST_CODE = 1;

    private RecyclerView mContactsRecyclerView;
    private RecyclerView.Adapter mTransferRecyclerAdapter;
    private RecyclerView.LayoutManager mTransferLayoutManager;

    private RecyclerView mChatRecyclerView;
    private RecyclerView.Adapter mChatListAdapter;
    private RecyclerView.LayoutManager mChatListLayoutManager;

    private Button mSendBtn;

    private List<User> mUserList = new ArrayList<>();
    private List<Chat> mChatList = new ArrayList<>();
    private List<ChatInfo> mChatInfo = new ArrayList<>();
    private List<Chat> mSelectedChats = new ArrayList<>();

    private String mDescription;
    private Uri mMediaUrl;
    private User mUserInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer);

        mDescription = getIntent().getStringExtra("text");
        mMediaUrl = Uri.parse(getIntent().getStringExtra("uri"));

        mSendBtn = findViewById(R.id.sendBtn);
        mSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDescription != null && mMediaUrl != null)
                    shareData();
            }
        });

        initializeRecyclerView();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_CONTACTS, Manifest.permission.READ_CONTACTS}, REQUEST_CODE);
            } else
                for (String contact : new Contacts(this).getAllContacts()) {
                    getUsers(contact);
                    getUserChatList();
                }
        //get current user Object
        FirebaseDatabase.getInstance().getReference().child("USER").child(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber()).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists())
                            for (DataSnapshot dc : dataSnapshot.getChildren())
                                mUserInfo = dc.getValue(User.class);
                        Toast.makeText(getApplicationContext(), "Current user info gotten successfully", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                }
        );

    }

    private void initializeRecyclerView() {
        mChatRecyclerView = findViewById(R.id.chats);
        mChatRecyclerView.setNestedScrollingEnabled(false);
        mChatRecyclerView.setHasFixedSize(false);
        mChatRecyclerView.addItemDecoration(new DividerItemDecoration(mChatRecyclerView.getContext(), DividerItemDecoration.VERTICAL)); // list divider
        mChatListLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        mChatRecyclerView.setLayoutManager(mChatListLayoutManager);
        mChatListAdapter = new TransferChatRecyclerAdapter(mChatList, mChatInfo, getApplicationContext());
        mChatRecyclerView.setAdapter(mChatListAdapter);

        mContactsRecyclerView = findViewById(R.id.recyclerView);
        mContactsRecyclerView.setNestedScrollingEnabled(false);
        mContactsRecyclerView.setHasFixedSize(false);
        mContactsRecyclerView.addItemDecoration(new DividerItemDecoration(mChatRecyclerView.getContext(), DividerItemDecoration.VERTICAL));
        mTransferLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        mContactsRecyclerView.setLayoutManager(mTransferLayoutManager);
        mTransferRecyclerAdapter = new TransferRecyclerAdapter(this, mUserList);
        mContactsRecyclerView.setAdapter(mTransferRecyclerAdapter);
    }

    private void getUsers(String contact) {
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

    private void getUserChatList() {
        DatabaseReference mUserChatDB = FirebaseDatabase.getInstance().getReference().child("USER").child(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber()).child("chat");
        mUserChatDB.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                        Chat mChat = new Chat(childSnapshot.getKey()); //getting all the ids of the chat children

                        for (Chat mChatIterator : mChatList)   //method used not to duplicate chat ids in the chats activity
                            if (mChatIterator.getChatId().equals(mChat.getChatId()))
                                continue; //stop here in the code and  goto next iteration in for loop

                        mChatList.add(mChat);
                        getChatData(mChat.getChatId());
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    //thi is where you will get the info of all the chats including the destinator , last message and timestamp
    private void getChatData(String chatId) {
        DatabaseReference mChatDB = FirebaseDatabase.getInstance().getReference().child("CHAT").child(chatId).child("info");
        mChatDB.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String chatId = "";

                    //getting id of the chat
                    if (dataSnapshot.child("id").getValue() != null)
                        chatId = dataSnapshot.child("id").getValue().toString();

                    //getting all users
                    for (DataSnapshot userSnaphshot : dataSnapshot.child("users").getChildren())
                        for (Chat mChat : mChatList) {
                            if (mChat.getChatId().equals(chatId)) {
                                User mUser = new User(userSnaphshot.getKey());
                                mChat.addUserToArrayList(mUser);
                                getUserData(mUser, chatId);
                            }
                        }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void getUserData(User mUser, final String chatId) {
        DatabaseReference mUserDb = FirebaseDatabase.getInstance().getReference().child("USER").child(mUser.getPhone());
        mUserDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User mUser = new User(dataSnapshot.getKey());

                //getting notification key
                if (dataSnapshot.child("notificationKey").getValue() != null)
                    mUser.setNotificationKey(dataSnapshot.child("notificationKey").getValue().toString());

                for (Chat mChat : mChatList) {
                    for (User mUserIt : mChat.getUserObjectArrayList()) {
                        if (mUserIt.getPhone().equals(mUser.getPhone())) {
                            mUserIt.setNotificationKey(mUser.getNotificationKey());
                        }
                    }
                }
                //to put in the chatInfo object
                getChatMetaData(chatId);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void getChatMetaData(String chatId) {
        DatabaseReference mChatInfoDB = FirebaseDatabase.getInstance().getReference().child("CHAT").child(chatId).child("info");
        mChatInfoDB.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot dc : dataSnapshot.getChildren()) {
                        ChatInfo chatInfo = dc.getValue(ChatInfo.class);
                        mChatInfo.add(chatInfo);
                    }
                    mChatListAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private int position = 0;
    private void shareData() {
        for (Chat chat : mChatList)
            if (chat.getSelected()) {
                mSelectedChats.add(chat);
                sendToChat(mSelectedChats.get(position));
                position++;
            }
    }

    private void sendToChat(final Chat chat) {
        //here get the uri data and send them to the user
        DatabaseReference mChatMessagesDb = FirebaseDatabase.getInstance().getReference().child("CHAT").child(chat.getChatId()).child("message");
        final DatabaseReference chatInfoDB = FirebaseDatabase.getInstance().getReference().child("CHAT").child(chat.getChatId()).child("info");

        String messageId = mChatMessagesDb.push().getKey();
        final DatabaseReference newMessageDb = mChatMessagesDb.child(messageId);

        final Map newMessageMap = new HashMap<>();

        newMessageMap.put("text", mDescription);
        newMessageMap.put("creator", FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber());

        final String mediaId = newMessageDb.child("media").push().getKey();
        final StorageReference filepath = FirebaseStorage.getInstance().getReference().child("CHAT").child(chat.getChatId()).child(messageId).child(mediaId);

        UploadTask uploadTask = filepath.putFile(mMediaUrl);
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        newMessageMap.put("/media/" + mediaId + "/", uri.toString());
                        updateDatabaseWithNewMessage(newMessageDb, chatInfoDB, newMessageMap, chat);

                    }
                });
            }
        });
    }

    private void updateDatabaseWithNewMessage(DatabaseReference newMessageDb, DatabaseReference chatInfoDB, Map newMessageMap, Chat chat) {

        newMessageDb.updateChildren(newMessageMap);

        String message;
        if (newMessageMap.get("text") != null) {
            message = newMessageMap.get("text").toString();
        } else
            message = "Sent Media";

                for (User mUser : chat.getUserObjectArrayList())
                    if (!mUser.getPhone().equals(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber())) {
                        for (ChatInfo mChatInfo : mChatInfo)
                            if (mChatInfo.getId().equals(chat.getChatId()))
                                if (mChatInfo.getType().equals("single")) {
                                    new SendNotification(
                                            message,
                                            mUserInfo.getName(),
                                            mUser.getNotificationKey()
                                    );
                                } else if (mChatInfo.getType().equals("group")) {
                                    new SendNotification(
                                            String.format(" %s : %s ", mUserInfo.getName(), message),
                                            mChatInfo.getName(),
                                            mUser.getNotificationKey()
                                    );
                                }

                    }
        //here also update the last message section in Info database
        chatInfoDB.child("lastMessage").setValue(newMessageMap);
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
                        for (String contact : new Contacts(this).getAllContacts()) {
                            getUsers(contact);
                            getUserChatList();
                        }
                        Toast.makeText(getApplicationContext(), "Contact Permission Granted", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "Contact Permission Denied", Toast.LENGTH_LONG).show();
                    }
                }
        }
    }
}
