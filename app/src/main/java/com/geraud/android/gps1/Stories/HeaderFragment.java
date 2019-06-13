package com.geraud.android.gps1.Stories;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.geraud.android.gps1.Camera.CameraActivity;
import com.geraud.android.gps1.Camera.FullScreenImageActivity;
import com.geraud.android.gps1.Camera.VideoPlayActivity;
import com.geraud.android.gps1.Camera.VideoTrimmerActivity;
import com.geraud.android.gps1.Models.Stories;
import com.geraud.android.gps1.R;
import com.geraud.android.gps1.Utils.TimeAgo;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

import static android.app.Activity.RESULT_OK;


public class HeaderFragment extends Fragment {

    public static final int REQUEST_VIDEO_CAPTURE = 1;
    public static final int REQUEST_IMAGE_CAPTURE = 2;
    public static final int REQUEST_CHOSE_GALLERY = 3;
    public static final int REQUEST_VIDEO_TRIMMER = 4;

    private ImageView mImageView;
    private TextView mTextSnippet;

    private Stories mStories;
    private Context mContext;

    public HeaderFragment() {
    }

    //this is the fragment for the header( Stories ) of the navigation bar
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_header, container, true);

        mContext = getContext();
        mStories = new Stories();

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("STORY");
        String userPhone = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser(), "Current User Cant Be NUll").getPhoneNumber();

        mImageView = view.findViewById(R.id.add_status_image);
        mTextSnippet = view.findViewById(R.id.add_status_text);

        Button mAddStatusButton = view.findViewById(R.id.add_status_button);
        mAddStatusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //new Intent(mContext,CameraActivity.class);
                //instead make a camera intent to get either a video or a picture to avoid complicated things
                selectImage(mContext);

            }
        });

        //load users status media from firebase
        if (userPhone != null) {
            databaseReference.child(userPhone).addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    Stories story = dataSnapshot.getValue(Stories.class);
                    if (story != null) {
                        mStories.addStoryToArray(story);
                        try { // if its a video it will crash
                            Glide.with(mContext)
                                    .load(story.getMedia()) //last image gotten
                                    .into(mImageView);
                        } catch (Exception e) {
                            Toast.makeText(mContext, "Exception - " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            Glide.with(mContext)
                                    .load(R.drawable.movies) //load video default background
                                    .into(mImageView);
                        }
                        mTextSnippet.setText(TimeAgo.getTimeAgo(story.getTimestamp()));
                    }
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(mContext, "HeaderStories ValueEventListener Cancelled", Toast.LENGTH_SHORT).show();
                }
            });
        }

        return view;
    }

    private void selectImage(Context context) {
        final CharSequence[] options = {"Take Photo", "Choose from Gallery", "Capture Video"};

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Add From?");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {

                if (options[item].equals("Take Photo")) {
                    dispatchTakePictureIntent();
                } else if (options[item].equals("Choose from Gallery")) {
                    dispatchChooseGallery();
                } else if (options[item].equals("Capture Video")) {
                    dispatchTakeVideoIntent();
                }
            }
        });
        builder.setCancelable(true);
        builder.show();
    }

    private void dispatchTakeVideoIntent() {
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        takeVideoIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
        takeVideoIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 10);
        if (takeVideoIntent.resolveActivity(Objects.requireNonNull(getActivity(), "activity is null").getPackageManager()) != null) {
            startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE);
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(Objects.requireNonNull(getActivity(), "activity is null").getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private void dispatchChooseGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[] {"image/*", "video/*"});
        if (intent.resolveActivity(Objects.requireNonNull(getActivity(), "activity is null").getPackageManager()) != null)
            startActivityForResult(intent, REQUEST_CHOSE_GALLERY);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = intent.getExtras(); assert extras != null;
            Bitmap imageBitmap = (Bitmap) extras.get("data"); // this is the thumbnail its kinda low quality
            //send to fullscreen image activity
            //convert bitmap to uri
            Intent imageIntent = new Intent(mContext, FullScreenImageActivity.class);
            imageIntent.setData(Uri.parse(""));

        }
        if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK) {
            Uri videoUri = intent.getData();
            Intent videoIntent = new Intent(mContext, VideoPlayActivity.class);
            videoIntent.setData(videoUri);
        }
        if (requestCode == REQUEST_CHOSE_GALLERY && resultCode == RESULT_OK) {
            Uri selectedMediaUri = intent.getData(); assert selectedMediaUri != null;
            if (selectedMediaUri.toString().contains("image")) {
                //handle image
                Intent imageIntent = new Intent(mContext, FullScreenImageActivity.class);
                imageIntent.setData(selectedMediaUri);
            } else  if (selectedMediaUri.toString().contains("video")) {
                //handle video
                //send video for size trimming activity
                startTrimActivity(selectedMediaUri);
            }
        }
        if (requestCode == REQUEST_VIDEO_TRIMMER && resultCode == RESULT_OK){
            Uri videoUri = Uri.parse(intent.getStringExtra("result"));
            //send video
            Intent videoIntent = new Intent(mContext, VideoPlayActivity.class);
            videoIntent.setData(videoUri);
        }
    }

    private void startTrimActivity(@NonNull Uri uri) {
        Intent intent = new Intent(mContext, VideoTrimmerActivity.class);
        intent.putExtra(CameraActivity.EXTRA_VIDEO_PATH, uri);
        startActivityForResult(intent, REQUEST_VIDEO_TRIMMER);
    }
}
