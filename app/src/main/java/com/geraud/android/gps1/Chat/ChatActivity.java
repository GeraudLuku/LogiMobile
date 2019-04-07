package com.geraud.android.gps1.Chat;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.LocationCallback;
import com.geraud.android.gps1.GoogleMap.MapsActivity;
import com.geraud.android.gps1.Models.Chat;
import com.geraud.android.gps1.Models.ChatInfo;
import com.geraud.android.gps1.Models.Message;
import com.geraud.android.gps1.Models.User;
import com.geraud.android.gps1.R;
import com.geraud.android.gps1.RecyclerAdapter.MediaAdapter;
import com.geraud.android.gps1.RecyclerAdapter.MessageAdapter;
import com.geraud.android.gps1.Services.SinchService;
import com.geraud.android.gps1.Sinch.BaseActivity;
import com.geraud.android.gps1.Sinch.CallScreenActivity;
import com.geraud.android.gps1.Utils.SendNotification;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.sinch.android.rtc.calling.Call;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ChatActivity extends BaseActivity {
    private static final int CAMERA_REQUEST = 0;
    private static final int CAMERA_PERMISSION_REQUEST = 2;

    private RecyclerView.Adapter mMessageAdapter, mMediaAdapter;
    private RecyclerView.LayoutManager mChatLayoutManager;

    private Chat mChatObject;
    private ChatInfo mChatInfoObject;

    private DatabaseReference mChatMessagesDb,
            mChatInfoDb;

    private User mUserInfo;
    private String mUserPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Find the toolbar view inside the activity layout
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar(), "Tool Bar Can't Be Null").setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ImageView mapLocation = toolbar.findViewById(R.id.mapLocation);
        ImageView image = toolbar.findViewById(R.id.image);
        ImageView call = toolbar.findViewById(R.id.call);
        TextView name = toolbar.findViewById(R.id.name);

        mMessage = findViewById(R.id.message);
        mChatObject = (Chat) getIntent().getSerializableExtra("chatObject");
        mChatInfoObject = (ChatInfo) getIntent().getSerializableExtra("chatInfoObject");

        if (mChatInfoObject.getType().equals("single")) {
            mapLocation.setVisibility(View.VISIBLE);
            call.setVisibility(View.VISIBLE);
        }

        //set name and image of chat
        name.setText(mChatInfoObject.getName());
        Glide.with(getApplicationContext()).load(Uri.parse(mChatInfoObject.getImage())).into(image);

        //call function and map function
        call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                call(mChatInfoObject.getName());
            }
        });
        mapLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showUserOnMap(mChatInfoObject.getName());
            }
        });

        mChatMessagesDb = FirebaseDatabase.getInstance().getReference().child("CHAT").child(mChatObject.getChatId()).child("messages");
        mChatInfoDb = FirebaseDatabase.getInstance().getReference().child("CHAT").child(mChatObject.getChatId()).child("info");

        Button sendButton = findViewById(R.id.send);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        Button addMediaButton = findViewById(R.id.addMedia);
        addMediaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(getApplicationContext())
                        .setTitle("Get Media From?")
                        .setItems(new CharSequence[]{"Gallery", "Camera"}, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case 0:
                                        openGallery();
                                        break;
                                    case 1:
                                        //create camera intent
                                        checkCameraPermission();
                                        break;
                                }
                            }
                        })
                        .show();
            }
        });

        //get current user Object
        mUserPhone = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser(), "Current User Cant Be Null").getPhoneNumber();
        if (mUserPhone != null) {
            FirebaseDatabase.getInstance().getReference().child("USER").child(mUserPhone).child("userInfo").addListenerForSingleValueEvent(
                    new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists())
                                mUserInfo = dataSnapshot.getValue(User.class);
                            Toast.makeText(getApplicationContext(), "Your Info Retrieved Successfully", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Toast.makeText(getApplicationContext(), "get current user Object ValueEventListener Cancelled", Toast.LENGTH_SHORT).show();
                        }
                    }
            );
        }

        initializeMessage();
        initializeMedia();
        getChatMessages();
    }

    private void checkCameraPermission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED) {
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            } else {
                if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                    Toast.makeText(this, "App Needs Camera permission", Toast.LENGTH_SHORT).show();
                }
                requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST);
            }

        } else {
            Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(cameraIntent, CAMERA_REQUEST);
        }

    }

    private void getChatMessages() {
        mChatMessagesDb.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.exists()) {
                    String text = "",
                            creatorID = "";
                    long timestamp = 0;
                    ArrayList<String> mediaUrlList = new ArrayList<>();

                    if (dataSnapshot.child("text").getValue() != null)
                        text = Objects.requireNonNull(dataSnapshot.child("text").getValue(), "text Null").toString();
                    if (dataSnapshot.child("timestamp").getValue() != null)
                        timestamp = Long.parseLong(Objects.requireNonNull(dataSnapshot.child("timestamp").getValue(), "timestamp Null").toString());
                    if (dataSnapshot.child("creator").getValue() != null)
                        creatorID = Objects.requireNonNull(dataSnapshot.child("creator").getValue(), "creatorId Null").toString();
                    if (dataSnapshot.child("media").getChildrenCount() > 0)
                        for (DataSnapshot mediaSnapshot : dataSnapshot.child("media").getChildren())
                            mediaUrlList.add(Objects.requireNonNull(mediaSnapshot.getValue(), "media Null").toString());


                    Message mMessage = new Message(dataSnapshot.getKey(), text, creatorID, timestamp, mediaUrlList).withType(mChatInfoObject.getType());
                    mMessageList.add(mMessage);
                    mChatLayoutManager.scrollToPosition(mMessageList.size() - 1); //scroll to the last message automatically
                    mMessageAdapter.notifyDataSetChanged();
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
                Toast.makeText(ChatActivity.this, "getChatMessages ValueEventListener Cancelled", Toast.LENGTH_SHORT).show();
            }
        });
    }

    int totalMediaUploaded = 0;
    private ArrayList<String> mediaIdList = new ArrayList<>();
    private EditText mMessage;

    private void sendMessage() {
        String messageId = mChatMessagesDb.push().getKey();
        final DatabaseReference newMessageDb = mChatMessagesDb.child(messageId);

        final Map<String, Object> newMessageMap = new HashMap<>();

        if (!mMessage.getText().toString().isEmpty())
            newMessageMap.put("text", mMessage.getText().toString());

        newMessageMap.put("creator", mUserPhone);
        newMessageMap.put("timestamp", System.currentTimeMillis());

        if (!mMediaUriList.isEmpty()) {
            for (String mediaUri : mMediaUriList) {
                String mediaId = newMessageDb.child("media").push().getKey();
                mediaIdList.add(mediaId);
                final StorageReference filepath = FirebaseStorage.getInstance().getReference().child("CHAT").child(mChatObject.getChatId()).child(messageId).child(mediaId);
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
        } else {
            newMessageMap.put("media", null);
        }
        if (!mMessage.getText().toString().isEmpty())
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
            message = Objects.requireNonNull(newMessageMap.get("text"),"message can't be null").toString();
        } else
            message = "Sent Media";

        for (User mUser : mChatObject.getUserObjectArrayList())
            if (!mUser.getPhone().equals(mUserPhone)) {
                if (mChatInfoObject.getType().equals("single")) {
                    new SendNotification(
                            message,
                            mUserInfo.getName(),
                            mUser.getNotificationKey()
                    );
                } else if (mChatInfoObject.getType().equals("group")) {
                    new SendNotification(
                            String.format(" %s : %s ", mUserInfo.getName(), message),
                            mChatInfoObject.getName(),
                            mUser.getNotificationKey()
                    );
                }

            }
        //here also update the last message section in Info database
        mChatInfoDb.child("lastMessage").setValue(newMessageMap);
    }

    private List<Message> mMessageList;

    private void initializeMessage() {
        mMessageList = new ArrayList<>();
        RecyclerView mChatRecyclerView = findViewById(R.id.messageList);
        mChatRecyclerView.setNestedScrollingEnabled(false);
        mChatRecyclerView.setHasFixedSize(false);
        mChatLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        mChatRecyclerView.setLayoutManager(mChatLayoutManager);
        mMessageAdapter = new MessageAdapter(mMessageList, this, mUserPhone);
        mChatRecyclerView.setAdapter(mMessageAdapter);
    }

    ArrayList<String> mMediaUriList;

    private void initializeMedia() {
        mMediaUriList = new ArrayList<>();
        RecyclerView mMediaRecyclerView = findViewById(R.id.mediaList);
        mMediaRecyclerView.setNestedScrollingEnabled(false);
        mMediaRecyclerView.setHasFixedSize(false);
        RecyclerView.LayoutManager mMediaLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false);
        mMediaRecyclerView.setLayoutManager(mMediaLayoutManager);
        mMediaAdapter = new MediaAdapter(mMediaUriList, this);
        mMediaRecyclerView.setAdapter(mMediaAdapter);
    }

    private static final int PICK_MEDIA_INTENT = 1;

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("video/* , image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true); //allow you to select multiple images
        startActivityForResult(Intent.createChooser(intent, "Select Media(s)"), PICK_MEDIA_INTENT);
    }

    private void call(String name) {
        DatabaseReference mUserDB = FirebaseDatabase.getInstance().getReference().child("USER");
        Query query = mUserDB.orderByChild("name").equalTo(name);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists())
                    for (DataSnapshot dc : dataSnapshot.getChildren()) {
                        User user = dc.getValue(User.class);
                        if (user != null) {
                            Call call = getSinchServiceInterface().callUser(user.getPhone());
                            String callId = call.getCallId();

                            Intent callScreen = new Intent(getApplicationContext(), CallScreenActivity.class);
                            callScreen.putExtra(SinchService.CALL_ID, callId);
                            callScreen.putExtra(SinchService.CALL_NAME, user.getName());
                            callScreen.putExtra(SinchService.CALL_IMAGE, user.getImage_uri());
                            startActivity(callScreen);
                        }
                    }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), "call ValueEventListener Cancelled", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showUserOnMap(String name) {

        DatabaseReference mUserDB = FirebaseDatabase.getInstance().getReference().child("USER").child("userInfo");
        Query query = mUserDB.orderByChild("name").equalTo(name);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists())
                    for (DataSnapshot dc : dataSnapshot.getChildren()) {
                        GeoFire geoFire = new GeoFire(FirebaseDatabase.getInstance().getReference("LOCATION"));
                        geoFire.getLocation(dc.getKey(), new LocationCallback() {
                            @Override
                            public void onLocationResult(String key, GeoLocation location) {
                                if (location != null) {
                                    Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                                    intent.putExtra("latitude", location.latitude);
                                    intent.putExtra("longitude", location.longitude);
                                    setResult(Activity.RESULT_OK, intent);
                                    startActivity(intent);
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Toast.makeText(getApplicationContext(), "getLocation ValueEventListener Cancelled", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), "showUserOnMap ValueEventListener Cancelled", Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case PICK_MEDIA_INTENT:
                    if (data != null) {
                        if (data.getClipData() == null) { //if there are no multiple selections
                            mMediaUriList.add(Objects.requireNonNull(data.getData(), "onActivityResult Data Can't Be Null").toString());
                        } else
                            for (int i = 0; i <= data.getClipData().getItemCount(); i++)
                                mMediaUriList.add(data.getClipData().getItemAt(i).getUri().toString());
                    }
                    mMediaAdapter.notifyDataSetChanged();
                    break;
                case CAMERA_REQUEST:
                    if (data != null)
                        mMediaUriList.add(Objects.requireNonNull(data.getData(), "onActivityResult Data Can't Be Null").toString());
                    break;
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CAMERA_PERMISSION_REQUEST:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "camera permission granted", Toast.LENGTH_SHORT).show();
                    Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(cameraIntent, CAMERA_REQUEST);
                } else {
                    Toast.makeText(this, "camera permission denied", Toast.LENGTH_SHORT).show();
                }
                break;

        }
    }
}
