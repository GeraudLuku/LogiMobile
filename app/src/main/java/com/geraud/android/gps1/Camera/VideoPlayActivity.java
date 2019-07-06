package com.geraud.android.gps1.Camera;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.VideoView;

import com.geraud.android.gps1.Chat.TransferActivity;
import com.geraud.android.gps1.Models.Stories;
import com.geraud.android.gps1.R;
import com.geraud.android.gps1.Stories.HeaderFragment;
import com.geraud.android.gps1.Utils.RandomStringGenerator;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Objects;

public class VideoPlayActivity extends AppCompatActivity{
    public static final String URI_EXTRA = "uri";
    public static final String TEXT_EXTRA = "text";

    private DatabaseReference mDatabaseReference;
    private StorageReference mStorageReference;

    private EditText mDescriptionEditText;
    private VideoView mVideoView;
    private String mDescription;
    private Uri mVideoUri;
    private String mUserPhone;

    private boolean mLocationAllowed = false;
    private double mLatitude = 0;
    private double mLongitude = 0;


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_play);

        //get latitude and longitude
        if (getIntent() != null) {
            mLatitude = getIntent().getDoubleExtra(HeaderFragment.HEADER_LAT, 0);
            mLongitude = getIntent().getDoubleExtra(HeaderFragment.HEADER_LNG, 0);
        }

        Intent callingIntent = getIntent();
        if (callingIntent != null) {
            mVideoUri = Uri.parse(callingIntent.getData().toString());
        }

        mDescriptionEditText = findViewById(R.id.descriptionEditText);
        mUserPhone = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser(), "Current User Cant Be Null").getPhoneNumber();
        if (mUserPhone != null) {
            mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("STORY").child(mUserPhone);
            mStorageReference = FirebaseStorage.getInstance().getReference().child("STORY").child(mUserPhone);
        }

        //get videoview and load video uri on it
        mVideoView = findViewById(R.id.videoSurfaceView);
        mVideoView.setVideoURI(mVideoUri);
        mVideoView.requestFocus();
        mVideoView.start();
        mVideoView.setOnCompletionListener(mCompletionListener);


        Button postbutton = findViewById(R.id.postButton);
        postbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if (getIntent().getExtras() != null) {
//                    Intent transferIntent = new Intent(getApplicationContext(), TransferActivity.class);
//                    transferIntent.putExtra(URI_EXTRA, mVideoUri);
//                    transferIntent.putExtra(TEXT_EXTRA, mDescriptionEditText.getText().toString());
//                    startActivity(transferIntent);
//                    finish();
//                } else
                new AlertDialog.Builder(VideoPlayActivity.this, R.style.alertDialog)
                        .setMessage("Share Location Of This Status?")
                        .setPositiveButton("Share", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mLocationAllowed = true;
                                postStatus();
                            }
                        })
                        .setNegativeButton("No, Please", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                postStatus();
                            }
                        })
                        .show();
            }
        });


    }

    private void postStatus() {
        if (mVideoView.isPlaying())
            mVideoView.pause();
        mDescription = mDescriptionEditText.getText().toString();
        //even if mLocationAllowed was allowed by user, if there is no LatLng available then set mLocationAllowed to false
        if (mLatitude == 0 && mLongitude == 0)
            mLocationAllowed = false;

        final StorageReference filepath = mStorageReference.child(RandomStringGenerator.randomString());
        UploadTask uploadTask = filepath.putFile(mVideoUri);
        uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()){
                    filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Stories story = new Stories(mLocationAllowed, uri.toString(),
                                    mDescription, System.currentTimeMillis(), mLatitude, mLongitude, "video",
                                    mUserPhone);
                            String key = mDatabaseReference.push().getKey();
                            story.setKey(key);
                            mDatabaseReference.child(key).setValue(story).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(getApplicationContext(), "Successfully created story", Toast.LENGTH_SHORT).show();
                                        finish();
                                    } else
                                        Toast.makeText(getApplicationContext(), "Couldn't create story", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });
                }else{
                    //resume video play
                    mVideoView.resume();
                    Toast.makeText(getApplicationContext(), "Couldn't upload story", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private MediaPlayer.OnCompletionListener mCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
            mp.seekTo(0);
            mp.start();
        }
    };

    @Override
    protected void onStop() {
        if (mVideoView.isPlaying())
            mVideoView.stopPlayback();
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mVideoView.isPlaying())
            mVideoView.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mVideoView != null && mVideoUri != null) {
        mVideoView.resume();
        }
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
