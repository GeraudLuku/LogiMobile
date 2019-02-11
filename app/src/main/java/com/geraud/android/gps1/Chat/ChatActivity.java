package com.geraud.android.gps1.Chat;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.geraud.android.gps1.Models.Chat;
import com.geraud.android.gps1.Models.Message;
import com.geraud.android.gps1.Models.User;
import com.geraud.android.gps1.R;
import com.geraud.android.gps1.RecyclerAdapter.MediaAdapter;
import com.geraud.android.gps1.RecyclerAdapter.MessageAdapter;
import com.geraud.android.gps1.Utils.SendNotification;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {
    private RecyclerView mChat, mMedia;
    private RecyclerView.Adapter mChatAdapter, mMediaAdapter;
    private RecyclerView.LayoutManager mChatLayoutManager , mMediaLayoutManager;
    private String TYPE = "single";
    private String intentChatId = null;

    List<Message> messageList;

    Chat mChatObject;

    DatabaseReference mChatMessagesDb,mChatInfoDb;

    private ImageView mMapLocation,mCall,mImage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Find the toolbar view inside the activity layout
        Toolbar toolbar = findViewById(R.id.toolbar);
        // Sets the Toolbar to act as the ActionBar for this Activity window.
        // Make sure the toolbar exists in the activity and is not null
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mMapLocation = toolbar.findViewById(R.id.mapLocation);
        mImage = toolbar.findViewById(R.id.image);
        mCall = toolbar.findViewById(R.id.call);

        mChatObject = (Chat) getIntent().getSerializableExtra("chatObject");

        //check if it is a group Chat or single Chat
        if (mChatObject.getUserObjectArrayList().size() > 2){
            //means its a groupChat
            TYPE = "multiple";
        }

        mChatMessagesDb = FirebaseDatabase.getInstance().getReference().child("chat").child(mChatObject.getChatId()).child("messages");

        Button mSend = findViewById(R.id.send);
        mSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        Button mAddMedia = findViewById(R.id.addMedia);
        mAddMedia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        initializeMessage();
        initializeMedia();
        getChatMessages();
    }

    private void getChatMessages() {
        mChatMessagesDb.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.exists()) {
                    String text = "";
                    String creatorID = "";
                    ArrayList<String> mediaUrlList = new ArrayList<>();

                    if (dataSnapshot.child("text").getValue() != null)
                        text = dataSnapshot.child("text").getValue().toString();
                    if (dataSnapshot.child("creator").getValue() != null)
                        creatorID = dataSnapshot.child("creator").getValue().toString();
                    if (dataSnapshot.child("media").getChildrenCount() > 0){
                        for (DataSnapshot mediaSnapshot : dataSnapshot.child("media").getChildren())
                            mediaUrlList.add(mediaSnapshot.getValue().toString());
                    }

                    Message mMessage = new Message(dataSnapshot.getKey(), text, creatorID , mediaUrlList);
                    messageList.add(mMessage);
                    mChatLayoutManager.scrollToPosition(messageList.size() - 1); //scroll to the last message automatically
                    mChatAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    int totalMediaUploaded = 0;
    private ArrayList<String> mediaIdList = new ArrayList<>();
    private EditText mMessage;

    private void sendMessage() {
        mMessage = findViewById(R.id.message);

            String messageId = mChatMessagesDb.push().getKey();
            final DatabaseReference newMessagDb = mChatMessagesDb.push();

            final Map newMessageMap = new HashMap<>();

            if (!mMessage.getText().toString().isEmpty())
            newMessageMap.put("text", mMessage.getText().toString());

            newMessageMap.put("creator", FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber());

            if (!mediaUriList.isEmpty()){
                for (String mediaUri : mediaUriList){
                    String mediaId = newMessagDb.child("media").push().getKey();
                    mediaIdList.add(mediaId);
                    final StorageReference filepath = FirebaseStorage.getInstance().getReference().child("chat").child(mChatObject.getChatId()).child(messageId).child(mediaId);

                    UploadTask uploadTask = filepath.putFile(Uri.parse(mediaUri));
                    uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    newMessageMap.put("/media/"+ mediaIdList.get(totalMediaUploaded) + "/", uri.toString());
                                    totalMediaUploaded++;
                                    if (totalMediaUploaded == mediaUriList.size()){
                                        //all images uploaded succesfully
                                        updateDatabaseWithNewMessage(newMessagDb , newMessageMap);
                                    }
                                }
                            });
                        }
                    });
                }
            }else {
                if (!mMessage.getText().toString().isEmpty()){
                    updateDatabaseWithNewMessage(newMessagDb , newMessageMap);
                }
            }
        mMessage.setText(null);
    }

    private void updateDatabaseWithNewMessage(DatabaseReference newMessageDb , Map newMessageMap){
        newMessageDb.updateChildren(newMessageMap);
        mMessage.setText(null);
        mediaUriList.clear();
        mediaIdList.clear();
        mMediaAdapter.notifyDataSetChanged();

        String message;
        if (newMessageMap.get("text") != null) {
            message = newMessageMap.get("text").toString();
        }else
            message = "Sent Media";

        for (User mUser : mChatObject.getUserObjectArrayList()){
            if (!mUser.getPhone().equals(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber())){
                new SendNotification(mUser.getNotificationKey(),mChatObject.getChatId(),message,FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber(),TYPE,"","");
            }
        }
    }
    private void initializeMessage() {
        messageList = new ArrayList<>();
        mChat = findViewById(R.id.messageList);
        mChat.setNestedScrollingEnabled(false);
        mChat.setHasFixedSize(false);
        mChatLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        mChat.setLayoutManager(mChatLayoutManager);
        mChatAdapter = new MessageAdapter(messageList, this);
        mChat.setAdapter(mChatAdapter);
    }

    int PICK_IMAGE_INTENT = 1;
    ArrayList<String> mediaUriList = new ArrayList<>();

    private void initializeMedia() {
        mediaUriList = new ArrayList<>();
        mMedia = findViewById(R.id.mediaList);
        mMedia.setNestedScrollingEnabled(false);
        mMedia.setHasFixedSize(false);
        mMediaLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false);
        mMedia.setLayoutManager(mMediaLayoutManager);
        mMediaAdapter = new MediaAdapter(mediaUriList, this);
        mMedia.setAdapter(mMediaAdapter);
    }

    private void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true); //alowyou to select multiple images
        intent.setAction(intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture(s)"), PICK_IMAGE_INTENT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK)
            if (requestCode == PICK_IMAGE_INTENT) {
                if (data.getClipData() == null) { //if there are no multiple selections
                    mediaUriList.add(data.getData().toString());
                }else {
                    for (int i = 0 ; i <= data.getClipData().getItemCount() ; i++)
                        mediaUriList.add(data.getClipData().getItemAt(i).getUri().toString());
                }
                mMediaAdapter.notifyDataSetChanged();
            }
    }
}
