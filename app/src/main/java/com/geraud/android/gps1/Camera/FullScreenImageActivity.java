package com.geraud.android.gps1.Camera;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.geraud.android.gps1.Chat.TransferActivity;
import com.geraud.android.gps1.Models.Stories;
import com.geraud.android.gps1.R;
import com.geraud.android.gps1.Utils.RandomStringGenerator;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class FullScreenImageActivity extends AppCompatActivity {

    private EditText mDescriptionInput;
    private Button mPostButton;
    private String mDescription;

    private Uri imageUri;

    private StorageReference mStorageReference;
    private DatabaseReference mDatabaseReference;

    private double latitude = 0;
    private double longitude = 0;
    private boolean location = true;
    private static final String TYPE = "image";

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        View decorView = getWindow().getDecorView();
        if (hasFocus) {
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            );
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_image);

        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("STORIES").child(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber());
        mStorageReference = FirebaseStorage.getInstance().getReference();

        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        longitude = location.getLongitude();
        latitude = location.getLatitude();

        ImageView fullScreenImageView = findViewById(R.id.fullScreenImageView);

        Intent callingActivityIntent = getIntent();
        if (callingActivityIntent != null) {
            imageUri = callingActivityIntent.getData();
            if (imageUri != null && fullScreenImageView != null) {
                Glide.with(this)
                        .load(imageUri)
                        .into(fullScreenImageView);
            }
        }

        mDescriptionInput = findViewById(R.id.descriptionEditText);
        mPostButton = findViewById(R.id.postButton);
        mPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getIntent().getExtras() != null) {
                    Intent transferIntent = new Intent(getApplicationContext(), TransferActivity.class);
                    transferIntent.putExtra("uri", imageUri);
                    transferIntent.putExtra("text", mDescriptionInput.getText().toString() == null ? " " : mDescriptionInput.getText().toString());
                    startActivity(transferIntent);
                    finish();
                } else
                    postStatus();
            }
        });
    }


    private void postStatus() {

        final long currentTime = System.currentTimeMillis();

        mDescription = mDescriptionInput.getText().toString() == null ? " " : mDescriptionInput.getText().toString();

        if (latitude == 0 && longitude == 0) {
            location = false;
        }

        StorageReference filepath = mStorageReference.child("STORIES").child(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber()).child(RandomStringGenerator.randomString() + ".jpg");
        filepath.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()) {
                    final String downloadURI = task.getResult().getDownloadUrl().toString();
                    Stories stories = new Stories(location, downloadURI, mDescription, currentTime, latitude, longitude, TYPE, FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber());
                    mDatabaseReference.push().setValue(stories).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(getApplicationContext(), "Succesfully created story", Toast.LENGTH_SHORT).show();
                                finish();
                            } else {
                                Toast.makeText(getApplicationContext(), "Couldnt create story", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } else {
                    Toast.makeText(getApplicationContext(), "Couldnt upload status", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
