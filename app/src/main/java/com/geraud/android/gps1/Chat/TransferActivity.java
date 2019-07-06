package com.geraud.android.gps1.Chat;

import android.Manifest;
import android.app.Activity;
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
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.geraud.android.gps1.Camera.FullScreenImageActivity;
import com.geraud.android.gps1.Models.Chat;
import com.geraud.android.gps1.Models.ChatInfo;
import com.geraud.android.gps1.Models.User;
import com.geraud.android.gps1.R;
import com.geraud.android.gps1.RecyclerAdapter.TransferChatRecyclerAdapter;
import com.geraud.android.gps1.RecyclerAdapter.TransferRecyclerAdapter;
import com.geraud.android.gps1.Registration;
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
import java.util.Objects;

public class TransferActivity extends AppCompatActivity {
    private static final int REQUEST_CODE = 1;

    private RecyclerView.Adapter mTransferRecyclerAdapter, mChatListAdapter;

    private List<User> mUserList = new ArrayList<>();
    private List<Chat> mChatList = new ArrayList<>();
    private List<ChatInfo> mChatInfo = new ArrayList<>();

    private List<Chat> mSelectedChats = new ArrayList<>();
    private List<User> mSelectedUsers = new ArrayList<>();

    private String mDescription;
    private String mUserPhone;
    private Uri mMediaUrl;
    private User mUserInfo;

    private Uri mActionFilterData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer);

        //tying something new here DeepLinking i will get image/videos from other activities and send them through my app
        // my app will appear in Send To of other activities and will receive either an image or video for transfer
        //i will first check that intent , if its not null then it was called from another activity and it will replace the mDescription
        // and mMediaUrl of this activity else if it is null continue with normal activity flow

        // atleast check if you are already a user of the app
        if (FirebaseAuth.getInstance().getCurrentUser() == null)
            startActivity(new Intent(getApplicationContext(), Registration.class));

        Intent intent = getIntent();
        mActionFilterData = intent.getData();

        // Figure out what to do based on the intent type
        if (mActionFilterData != null && Objects.equals(intent.getType(), "image/") || Objects.equals(intent.getType(), "video/")) {
            // Handle intents with image and video data ...
            mDescription = ""; // you can get it from either the VideoPlayActivity or FullScreenImageActivity its the same
            mMediaUrl = mActionFilterData;
        } else {
            // proceed with activity normal flow
            mDescription = getIntent().getStringExtra(FullScreenImageActivity.TEXT_EXTRA); // you can get it from either the VideoPlayActivity or FullScreenImageActivity its the same
            mMediaUrl = Uri.parse(getIntent().getStringExtra(FullScreenImageActivity.URI_EXTRA));
        }

        mUserPhone = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser(), "Current User Cant Be Null").getPhoneNumber();

        Button sendBtn = findViewById(R.id.sendBtn);
        sendBtn.setOnClickListener(new View.OnClickListener() {
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
                    getContacts(contact);
                    getUserChatList();
                }
        //get current user Object
        FirebaseDatabase.getInstance().getReference().child("USER").child(mUserPhone).addListenerForSingleValueEvent(
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
                        Toast.makeText(TransferActivity.this, "current user object ValueEventListener Cancelled", Toast.LENGTH_SHORT).show();
                    }
                }
        );

    }

    private void initializeRecyclerView() {
        RecyclerView chatsRecyclerView = findViewById(R.id.chats);
        chatsRecyclerView.setNestedScrollingEnabled(false);
        chatsRecyclerView.setHasFixedSize(false);
        chatsRecyclerView.addItemDecoration(new DividerItemDecoration(chatsRecyclerView.getContext(), DividerItemDecoration.VERTICAL)); // list divider
        RecyclerView.LayoutManager chatListLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        chatsRecyclerView.setLayoutManager(chatListLayoutManager);
        mChatListAdapter = new TransferChatRecyclerAdapter(mChatList, mChatInfo, getApplicationContext(), mUserPhone);
        chatsRecyclerView.setAdapter(mChatListAdapter);

        RecyclerView contactsRecyclerView = findViewById(R.id.recyclerView);
        contactsRecyclerView.setNestedScrollingEnabled(false);
        contactsRecyclerView.setHasFixedSize(false);
        contactsRecyclerView.addItemDecoration(new DividerItemDecoration(chatsRecyclerView.getContext(), DividerItemDecoration.VERTICAL));
        RecyclerView.LayoutManager contactsLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        contactsRecyclerView.setLayoutManager(contactsLayoutManager);
        mTransferRecyclerAdapter = new TransferRecyclerAdapter(this, mUserList);
        contactsRecyclerView.setAdapter(mTransferRecyclerAdapter);
    }

    private void getContacts(String contact) {
        DatabaseReference mUserDB = FirebaseDatabase.getInstance().getReference().child("USER").child("userInfo");
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
                Toast.makeText(TransferActivity.this, "getContacts ValueEventListener Cancelled", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getUserChatList() {
        DatabaseReference mUserChatDB = FirebaseDatabase.getInstance().getReference().child("USER").child(mUserPhone).child("chat");
        mUserChatDB.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                        Chat mChat = new Chat(childSnapshot.getKey()); //getting all the ids of the chat children

                        for (Chat mChatIterator : mChatList)   //method used not to duplicate chat ids in the chats activity
                            if (mChatIterator.getChatId().equals(mChat.getChatId()))
                                //continue; //stop here in the code and  goto next iteration in for loop

                                mChatList.add(mChat);
                        getChatData(mChat.getChatId());
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(TransferActivity.this, "getUserChatList ValueEventListener Cancelled", Toast.LENGTH_SHORT).show();
            }
        });

    }

    //this is where you will get the info of all the chats including the destinator , last message and timestamp
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
                        chatId = Objects.requireNonNull(dataSnapshot.child("id").getValue(), "id can't Be Null").toString();

                    //getting all users
                    for (DataSnapshot userSnapshot : dataSnapshot.child("users").getChildren())
                        for (Chat mChat : mChatList) {
                            if (mChat.getChatId().equals(chatId)) {
                                User mUser = new User(userSnapshot.getKey());
                                mChat.addUserToArrayList(mUser);
                                getUserData(mUser);
                            }
                        }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(TransferActivity.this, "getChatData ValueEventListener Cancelled", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void getUserData(User mUser) {
        DatabaseReference mUserDb = FirebaseDatabase.getInstance().getReference().child("USER").child(mUser.getPhone()).child("userInfo");
        mUserDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User mUser = new User(dataSnapshot.getKey());

                //getting notification key
                if (dataSnapshot.child("notificationKey").getValue() != null)
                    mUser.setNotificationKey(Objects.requireNonNull(dataSnapshot.child("notificationKey").getValue(), "notification key can't be null").toString());

                for (Chat mChat : mChatList) {
                    for (User mUserIt : mChat.getUserObjectArrayList()) {
                        if (mUserIt.getPhone().equals(mUser.getPhone())) {
                            mUserIt.setNotificationKey(mUser.getNotificationKey());
                        }
                    }
                }
                mChatListAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(TransferActivity.this, "getUserData ValueEventListener Cancelled", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private List<Chat> getSelectedChats() {
        for (Chat chat : mChatList)
            if (chat.getSelected())
                mSelectedChats.add(chat);

        return mSelectedChats;
    }

    private List<User> getSelectedUsers() {
        for (User user : mUserList)
            if (user.getSelected())
                mSelectedUsers.add(user);

        return mSelectedUsers;
    }

    private void shareData() {
        //send to chats first
        for (Chat chat : getSelectedChats())
            sendToChat(chat);

        // send to selected contacts
        for (User user : getSelectedUsers()) {
            //check if chat exists
            checkIfChatExists(user);
        }
    }

    private void sendToChat(final Chat chat) {
        //here get the uri data and send them to the user
        DatabaseReference mChatMessagesDb = FirebaseDatabase.getInstance().getReference().child("CHAT").child(chat.getChatId()).child("message");
        final DatabaseReference chatInfoDB = FirebaseDatabase.getInstance().getReference().child("CHAT").child(chat.getChatId()).child("info");

        String messageId = mChatMessagesDb.push().getKey();
        final DatabaseReference newMessageDb = mChatMessagesDb.child(messageId);

        final HashMap<String, Object> newMessageMap = new HashMap<>();

        newMessageMap.put("text", mDescription);
        newMessageMap.put("creator", mUserPhone);
        newMessageMap.put("timestamp", System.currentTimeMillis());

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
        if (mDescription != null) {
            message = mDescription;
        } else
            message = "Sent Media";

        for (User mUser : chat.getUserObjectArrayList())
            if (!mUser.getPhone().equals(mUserPhone)) {
                for (ChatInfo mChatInfo : mChatInfo)
                    if (mChatInfo.getId().equals(chat.getChatId()))
                        if (mChatInfo.getType().equals("single")) {
                            new SendNotification(getApplicationContext(),
                                    message,
                                    mUserInfo.getName(),
                                    mUser.getNotificationKey()
                            );
                        } else if (mChatInfo.getType().equals("group")) {
                            new SendNotification(getApplicationContext(),
                                    String.format(" %s : %s ", mUserInfo.getName(), message),
                                    mChatInfo.getName(),
                                    mUser.getNotificationKey()
                            );
                        }

            }
        //here also update the last message section in Info database
        chatInfoDB.child("lastMessage").setValue(newMessageMap);

        //if the action was performed for another activity send result ok back to it
        if (mActionFilterData != null) {
            Intent result = new Intent("com.example.RESULT_ACTION", Uri.parse("content://result_uri"));
            setResult(Activity.RESULT_OK, result);
            finish();
        } else
            finish();
    }

    private void checkIfChatExists(final User user) {
        //check if the chat already exists
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("USER").child("chat");
        Query query = reference.orderByChild("user").equalTo(user.getPhone());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Chat chat = dataSnapshot.getValue(Chat.class);
                    if (chat != null)
                        sendToChat(chat);
                } else {
                    //create a new chat
                    String key = FirebaseDatabase.getInstance().getReference().child("CHAT").push().getKey();

                    DatabaseReference userDB = FirebaseDatabase.getInstance().getReference().child("USER");
                    DatabaseReference chatInfoDB = FirebaseDatabase.getInstance().getReference().child("CHAT").child(key).child("info");

                    HashMap<String, Object> newMessageMap = new HashMap<>();
                    newMessageMap.put("id", key);
                    newMessageMap.put("lastMessage/", null); //set it to null because it will be updated just below
                    newMessageMap.put("users/" + mUserPhone, true);
                    newMessageMap.put("users/" + user.getPhone(), true);
                    newMessageMap.put("name", null);
                    newMessageMap.put("type", "single");
                    newMessageMap.put("image", null);

                    Map<String, Object> myChatMap = new HashMap<>();
                    myChatMap.put("type", "single");
                    myChatMap.put("user", user.getPhone());

                    Map<String, Object> singleChatMap = new HashMap<>();
                    singleChatMap.put("type", "single");
                    singleChatMap.put("user", mUserPhone);

                    //finally create the chat
                    chatInfoDB.updateChildren(newMessageMap);
                    userDB.child(mUserPhone).child("chat").child(key).setValue(myChatMap); //chat id set in my Document
                    userDB.child(user.getPhone()).child("chat").child(key).setValue(singleChatMap); // set chat id in the other users document

                    //open chatActivity for this newly created chat
                    Chat chat = new Chat(key);
                    sendToChat(chat);

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(TransferActivity.this, "checkIfChatExists ValueEventListener Cancelled", Toast.LENGTH_SHORT).show();
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
                        for (String contact : new Contacts(this).getAllContacts()) {
                            getContacts(contact);
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
