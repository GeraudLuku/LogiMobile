package com.geraud.android.gps1.User_Setup;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.geraud.android.gps1.GoogleMap.MapsActivity;
import com.geraud.android.gps1.Models.User;
import com.geraud.android.gps1.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.onesignal.OneSignal;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.Locale;
import java.util.Objects;

import es.dmoral.toasty.Toasty;

public class Setup extends AppCompatActivity {

    private final static int REQUEST_CODE = 1;

    private StorageReference mStorageReference;
    private FirebaseUser mFirebaseUser;
    private DatabaseReference mDatabaseReference;
    private GeoFire mGeoFire;

    private ImageView mUserImage;
    private EditText mUserName;
    private TextView mLocationView;
    private Button mDoneButton;
    private ProgressBar mProgressBar;
    private Uri mImageURI;

    private String mImageDownloadUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        //storage
        mStorageReference = FirebaseStorage.getInstance().getReference();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        mGeoFire = new GeoFire(mDatabaseReference.child("LOCATION"));

        //initialise image view , edit text , button and progress bar .etc
        mUserImage = findViewById(R.id.profile_image);
        mUserName = findViewById(R.id.name);
        mDoneButton = findViewById(R.id.submit);
        mProgressBar = findViewById(R.id.progress_bar);

        //image click listener to send the person to choose an image
        mUserImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //CALL THE IMAGE CROPPER LIBRARY TO CROP THE SELECTED IMAGE
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setActivityTitle("Profile Image")
                        .setMinCropResultSize(600, 600)
                        .setAspectRatio(1, 1)
                        .start(Setup.this);

            }
        });

        //post information
        mDoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String name = mUserName.getText().toString();
                if (!TextUtils.isEmpty(name) && mImageURI != null) { //if  image uri , name is not null
                    mProgressBar.setVisibility(View.VISIBLE);
                    mDoneButton.setVisibility(View.INVISIBLE);
                    //start uploading to storage

                    final StorageReference filepath = mStorageReference.child("Profile Pictures").child(Objects.requireNonNull(mFirebaseUser.getPhoneNumber(), "Phone Number Cant Be Null")).child(mFirebaseUser.getPhoneNumber() + ".jpg");
                    filepath.putFile(mImageURI).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if (task.isSuccessful()) {
                                //gets the download uri of the file successfully uploaded to the storage and pass it as a string to downloadURI
                                filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        mImageDownloadUri = uri.toString();
                                        //get the notification key
                                        OneSignal.idsAvailable(new OneSignal.IdsAvailableHandler() {
                                            @Override
                                            public void idsAvailable(String userId, String registrationId) {
                                                //storing post info and data on firebase real-time database
                                                User data = new User(name, "user", mImageDownloadUri, 0, mFirebaseUser.getPhoneNumber(), userId);

                                                //save in real-time database
                                                mDatabaseReference.child("USER").child(mFirebaseUser.getPhoneNumber()).child("userInfo").setValue(data).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            //if all the data was saved successfully in the database
                                                            //i also want to add the location of the user in the database using geoFire
                                                            mGeoFire.setLocation(mFirebaseUser.getPhoneNumber(), new GeoLocation(0,0), new GeoFire.CompletionListener() {
                                                                @Override
                                                                public void onComplete(String key, DatabaseError error) {
                                                                    if (error != null) {//there was an error
                                                                        Toast.makeText(Setup.this, "Couldn't upload Location To Database", Toast.LENGTH_SHORT).show();
                                                                        mProgressBar.setVisibility(View.INVISIBLE);
                                                                        mDoneButton.setVisibility(View.VISIBLE);
                                                                    } else {
                                                                        Toasty.success(getApplicationContext(), "Successfully Updated Account Info!", Toast.LENGTH_SHORT, true).show();
                                                                        startActivity(new Intent(getApplicationContext(), MapsActivity.class));  // Welcome to the main app
                                                                        finish();
                                                                    }
                                                                }
                                                            });

                                                        } else {
                                                            //if there was an error
                                                            Toasty.error(getApplicationContext(), "Couldn't Create Account Check Your Internet Connection", Toast.LENGTH_SHORT, true).show();
                                                            mProgressBar.setVisibility(View.INVISIBLE);
                                                            mDoneButton.setVisibility(View.VISIBLE);
                                                        }
                                                    }
                                                });
                                            }
                                        });
                                    }
                                });


                            } else {
                                Toasty.error(getApplicationContext(), "Couldn't Upload Profile Picture Check Your Internet Connection", Toast.LENGTH_SHORT, true).show();
                                mProgressBar.setVisibility(View.INVISIBLE);
                                mDoneButton.setVisibility(View.VISIBLE);

                            }
                        }
                    });

                } else {
                    Toast.makeText(Setup.this, "Enter Name And Select a Profile Picture", Toast.LENGTH_SHORT).show();
                    mUserName.setError("This Field Can't Be Null", ContextCompat.getDrawable(getApplicationContext(), R.drawable.error));
                }
            }
        });


    }

    private boolean checkPermission() {

        return (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED);
    }

    private void requestPermission() {
        requestPermissions(new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION},
                REQUEST_CODE);
    }

    //get the result form the image cropper and displays it on the new_post_image view
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                mImageURI = result.getUri();
                Glide.with(getApplicationContext()).load(mImageURI).into(mUserImage);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Toast.makeText(this, "Error Occurred In Image Cropper Activity", Toast.LENGTH_SHORT).show();
            }
        }
    }

    //get request permission results
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED)
                    Toast.makeText(getApplicationContext(), "Location Permissions Granted", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getApplicationContext(), "App Needs Permission To Work", Toast.LENGTH_SHORT).show();
                break;

        }
    }

}
