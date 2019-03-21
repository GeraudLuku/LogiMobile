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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView mChatRecyclerView, mMediaRecyclerView;
    private RecyclerView.Adapter mChatAdapter, mMediaAdapter;
    private RecyclerView.LayoutManager mChatLayoutManager, mMediaLayoutManager;

    private String mChatType;

    private Chat mChatObject;

    private DatabaseReference mChatMessagesDb, mChatInfoDb;

    private ImageView mMapLocation, mCall, mImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Find the toolbar view inside the activity layout
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mMapLocation = toolbar.findViewById(R.id.mapLocation);
        mImage = toolbar.findViewById(R.id.image);
        mCall = toolbar.findViewById(R.id.call);
        mMessage = findViewById(R.id.message);

        mChatObject = (Chat) getIntent().getSerializableExtra("chatObject");
        mChatType = (mChatObject.getUserObjectArrayList().size() > 1) ? "group" : "single"; //check if it is a group Chat or single Chat

        mChatMessagesDb = FirebaseDatabase.getInstance().getReference().child("CHAT").child(mChatObject.getChatId()).child("messages");

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
                    if (dataSnapshot.child("media").getChildrenCount() > 0) {
                        for (DataSnapshot mediaSnapshot : dataSnapshot.child("media").getChildren())
                            mediaUrlList.add(mediaSnapshot.getValue().toString());
                    }

                    Message mMessage = new Message(dataSnapshot.getKey(), text, creatorID, System.currentTimeMillis(), mediaUrlList).withType(mChatType);
                    mMessageList.add(mMessage);
                    mChatLayoutManager.scrollToPosition(mMessageList.size() - 1); //scroll to the last message automatically
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

        String messageId = mChatMessagesDb.push().getKey();
        final DatabaseReference newMessageDb = mChatMessagesDb.child(messageId);

        final Map newMessageMap = new HashMap<>();

        if (!mMessage.getText().toString().isEmpty())
            newMessageMap.put("text", mMessage.getText().toString());

        newMessageMap.put("creator", FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber());

        if (!mMediaUriList.isEmpty()) {
            for (String mediaUri : mMediaUriList) {
                String mediaId = newMessageDb.child("media").push().getKey();
                mediaIdList.add(mediaId);
                final StorageReference filepath = FirebaseStorage.getInstance().getReference().child("chat").child(mChatObject.getChatId()).child(messageId).child(mediaId);

                UploadTask uploadTask = filepath.putFile(Uri.parse(mediaUri));
                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                newMessageMap.put("/media/" + mediaIdList.get(totalMediaUploaded) + "/", uri.toString());
                                totalMediaUploaded++;
                                if (totalMediaUploaded == mMediaUriList.size()) //all images uploaded succesfully
                                    updateDatabaseWithNewMessage(newMessageDb, newMessageMap);

                            }
                        });
                    }
                });
            }
        } else if (!mMessage.getText().toString().isEmpty())
            updateDatabaseWithNewMessage(newMessageDb, newMessageMap);


    }

    private void updateDatabaseWithNewMessage(DatabaseReference newMessageDb, Map newMessageMap) {

        newMessageDb.updateChildren(newMessageMap);
        mMessage.setText(null);
        mMediaUriList.clear();
        mediaIdList.clear();
        mMediaAdapter.notifyDataSetChanged();

        String message;
        if (newMessageMap.get("text") != null) {
            message = newMessageMap.get("text").toString();
        } else
            message = "Sent Media";

        for (User mUser : mChatObject.getUserObjectArrayList())
            if (!mUser.getPhone().equals(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber()))
                new SendNotification(mUser.getNotificationKey(), mChatObject.getChatId(), message, FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber(), mChatType, "", "");

    }

    private List<Message> mMessageList;

    private void initializeMessage() {
        mMessageList = new ArrayList<>();
        mChatRecyclerView = findViewById(R.id.messageList);
        mChatRecyclerView.setNestedScrollingEnabled(false);
        mChatRecyclerView.setHasFixedSize(false);
        mChatLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        mChatRecyclerView.setLayoutManager(mChatLayoutManager);
        mChatAdapter = new MessageAdapter(mMessageList, this);
        mChatRecyclerView.setAdapter(mChatAdapter);
    }

    ArrayList<String> mMediaUriList;

    private void initializeMedia() {
        mMediaUriList = new ArrayList<>();
        mMediaRecyclerView = findViewById(R.id.mediaList);
        mMediaRecyclerView.setNestedScrollingEnabled(false);
        mMediaRecyclerView.setHasFixedSize(false);
        mMediaLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false);
        mMediaRecyclerView.setLayoutManager(mMediaLayoutManager);
        mMediaAdapter = new MediaAdapter(mMediaUriList, this);
        mMediaRecyclerView.setAdapter(mMediaAdapter);
    }

    private static final int PICK_IMAGE_INTENT = 1;

    private void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true); //allow you to select multiple images
        intent.setAction(intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture(s)"), PICK_IMAGE_INTENT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK)
            if (requestCode == PICK_IMAGE_INTENT) {
                if (data.getClipData() == null) { //if there are no multiple selections
                    mMediaUriList.add(data.getData().toString());
                } else
                    for (int i = 0; i <= data.getClipData().getItemCount(); i++)
                        mMediaUriList.add(data.getClipData().getItemAt(i).getUri().toString());
                mMediaAdapter.notifyDataSetChanged();
            }
    }
}
