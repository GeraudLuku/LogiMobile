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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class FullScreenImageActivity extends AppCompatActivity {
    private static final int REQUEST_PERMISSION = 0;

    private EditText mDescriptionInput;
    private Button mPostButton;
    private String mDescription;

    private Uri mImageUri;

    private StorageReference mStorageReference;
    private DatabaseReference mDatabaseReference;

    private LocationManager mLocationManager;
    private Location mLocation;

    private boolean location = true;
    private double mLatitude = 0;
    private double mLongitude = 0;

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
        mStorageReference = FirebaseStorage.getInstance().getReference().child("STORIES").child(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber());

        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_PERMISSION);
        } else {
            mLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            mLongitude = mLocation.getLongitude();
            mLatitude = mLocation.getLatitude();
        }

        ImageView fullScreenImageView = findViewById(R.id.fullScreenImageView);

        Intent callingActivityIntent = getIntent();
        if (callingActivityIntent != null) {
            mImageUri = callingActivityIntent.getData();
            if (mImageUri != null && fullScreenImageView != null) {
                Glide.with(this)
                        .load(mImageUri)
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
                    transferIntent.putExtra("uri", mImageUri);
                    transferIntent.putExtra("text", mDescriptionInput.getText().toString() == null ? " " : mDescriptionInput.getText().toString());
                    startActivity(transferIntent);
                    finish();
                } else
                    postStatus();
            }
        });
    }


    private void postStatus() {

        mDescription = mDescriptionInput.getText().toString() == null ? " " : mDescriptionInput.getText().toString();

        if (mLatitude == 0 && mLongitude == 0)
            location = false;

        StorageReference filepath = mStorageReference.child(RandomStringGenerator.randomString() + ".jpg");
        UploadTask uploadTask = filepath.putFile(mImageUri);
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                Stories stories = new Stories(location, taskSnapshot.getDownloadUrl().toString(),
                        mDescription, System.currentTimeMillis(), mLatitude, mLongitude, "image",
                        FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber());

                mDatabaseReference.push().setValue(stories).addOnCompleteListener(new OnCompleteListener<Void>() {
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
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_PERMISSION:
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(), "Coarse And Fine Location Permission Denied", Toast.LENGTH_SHORT).show();
                }
                mLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                mLongitude = mLocation.getLongitude();
                mLatitude = mLocation.getLatitude();
                Toast.makeText(getApplicationContext(), "Coarse And Fine Location Permission Granted", Toast.LENGTH_SHORT).show();
        }
    }
}
