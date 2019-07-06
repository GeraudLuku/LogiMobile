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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.erikagtierrez.multiple_media_picker.Gallery;
import com.geraud.android.gps1.Camera.CameraActivity;
import com.geraud.android.gps1.Camera.FullScreenImageActivity;
import com.geraud.android.gps1.Camera.VideoPlayActivity;
import com.geraud.android.gps1.Camera.VideoTrimmerActivity;
import com.geraud.android.gps1.GoogleMap.MapsActivity;
import com.geraud.android.gps1.Models.Stories;
import com.geraud.android.gps1.R;
import com.geraud.android.gps1.Utils.TimeAgo;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Objects;

import static android.app.Activity.RESULT_OK;


public class HeaderFragment extends Fragment {

    public static final int REQUEST_VIDEO_CAPTURE = 1;
    public static final int REQUEST_CHOSE_GALLERY = 3;
    public static final int REQUEST_VIDEO_TRIMMER = 4;

    public static final String HEADER_LAT = "latitude";
    public static final String HEADER_LNG = "longitude";

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
        View view = inflater.inflate(R.layout.fragment_header, container, false);

        mContext = getContext();
        mStories = new Stories();

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("STORY");
        String userPhone = Objects.requireNonNull(getArguments(), "Header Argument Is Null").getString(MapsActivity.USER_PHONE);

        mImageView = view.findViewById(R.id.add_status_image);
        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //open and show status
                Intent intent = new Intent(mContext, FullScreenStoryActivity.class);
                intent.putExtra(FullScreenStoryActivity.STORY, mStories);
                startActivity(intent);
            }
        });
        mTextSnippet = view.findViewById(R.id.add_status_text);

        ImageButton mAddStatusButton = view.findViewById(R.id.add_status_button);
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
        Toast.makeText(mContext, "clicked new story", Toast.LENGTH_SHORT).show();
        final CharSequence[] options = {"Take Photo", "Capture Video"};

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Add From?");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {

                if (options[item].equals("Take Photo")) {
                    dispatchTakePictureIntent();
                }
                else if (options[item].equals("Capture Video")) {
                    dispatchTakeVideoIntent();
                }
            }
        });
        builder.setCancelable(true);
        builder.show();
    }

    private void dispatchTakeVideoIntent() {
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        takeVideoIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);
        takeVideoIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 10);
        if (takeVideoIntent.resolveActivity(Objects.requireNonNull(getActivity(), "activity is null").getPackageManager()) != null) {
            startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE);
        }
    }

    private void dispatchTakePictureIntent() {
        //crop image activity
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setMinCropResultSize(512, 512)
                .setAspectRatio(1, 1)
                .start(getContext(), HeaderFragment.this);
    }

    private void dispatchChooseGallery() {
        // third party API
        Intent intent = new Intent(mContext, Gallery.class);
        // Set the title
        intent.putExtra("title", "Select media");
        // Mode 1 for both images and videos selection, 2 for images only and 3 for videos!
        intent.putExtra("mode", 1);
//        intent.putExtra("maxSelection",3); // Optional
        startActivityForResult(intent, REQUEST_CHOSE_GALLERY);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK) {
            Uri videoUri = intent.getData();
            Intent videoIntent = new Intent(mContext, VideoPlayActivity.class);
            videoIntent.setData(videoUri);
            videoIntent.putExtra(HEADER_LAT,getArguments().getDouble(MapsActivity.LATITUDE,0));
            videoIntent.putExtra(HEADER_LNG,getArguments().getDouble(MapsActivity.LONGITUDE,0));
            startActivity(videoIntent);
        }
        if (requestCode == REQUEST_CHOSE_GALLERY && resultCode == RESULT_OK) {
//            Uri selectedMediaUri = intent.getData(); assert selectedMediaUri != null;
//            if (selectedMediaUri.toString().contains("image")) {
//                //handle image
//                Intent imageIntent = new Intent(mContext, FullScreenImageActivity.class);
//                imageIntent.setData(selectedMediaUri);
//            } else  if (selectedMediaUri.toString().contains("video")) {
//                //handle video
//                //send video for size trimming activity
//                startTrimActivity(selectedMediaUri);
//            }

            if (resultCode == RESULT_OK && intent != null) {
                ArrayList<String> selectionResult = intent.getStringArrayListExtra("result");
                for (String media : selectionResult){
                    if (media.contains("image")){
                        Intent imageIntent = new Intent(mContext, FullScreenImageActivity.class);
                        imageIntent.setData(Uri.parse(media));
                        startActivity(imageIntent);
                    }else {
                        startTrimActivity(Uri.parse(media));
                    }
                }
            }
        }
        if (requestCode == REQUEST_VIDEO_TRIMMER && resultCode == RESULT_OK) {
            Uri videoUri = Uri.parse(intent.getStringExtra("result"));
            //send video
            Intent videoIntent = new Intent(mContext, VideoPlayActivity.class);
            videoIntent.setData(videoUri);
            startActivity(videoIntent);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(intent);
            if (resultCode == RESULT_OK) {
                Intent imageIntent = new Intent(mContext, FullScreenImageActivity.class);
                imageIntent.setData(result.getUri());
                imageIntent.putExtra(HEADER_LAT,getArguments().getDouble(MapsActivity.LATITUDE,0));
                imageIntent.putExtra(HEADER_LNG,getArguments().getDouble(MapsActivity.LONGITUDE,0));
                startActivity(imageIntent);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Toast.makeText(getContext(), "failed to get image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startTrimActivity(@NonNull Uri uri) {
        Intent intent = new Intent(mContext, VideoTrimmerActivity.class);
        intent.putExtra(CameraActivity.EXTRA_VIDEO_PATH, uri);
        startActivityForResult(intent, REQUEST_VIDEO_TRIMMER);
    }

    private Uri getImageUri(Context context, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), inImage, "Logi", "Logi Status Image");
        return Uri.parse(path);
    }
}
