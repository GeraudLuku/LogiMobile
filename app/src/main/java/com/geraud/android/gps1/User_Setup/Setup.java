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

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.geraud.android.gps1.GoogleMap.MapsActivity;
import com.geraud.android.gps1.Models.User;
import com.geraud.android.gps1.R;
import com.google.android.gms.tasks.OnCompleteListener;
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

    private Location mLocation;

    private ImageView mUserImage;
    private EditText mUserName;
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

        //initialise image view , edit text , button and progress bar
        mUserImage = findViewById(R.id.profile_image);
        mUserName = findViewById(R.id.name);
        mDoneButton = findViewById(R.id.submit);
        mProgressBar = findViewById(R.id.progress_bar);

        //check location services permissions
        if (checkPermission()) {
            TextView locationView = findViewById(R.id.locationView);
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            //show user its approximated current location
            locationView.setText(String.format(Locale.getDefault(), "(Approximately)Currently Located At LAT: %.2f And LONG: %.2f", location.getLatitude(), location.getLongitude()));
        } else
            requestPermission();

        //image click listener to send the person to choose an image
        mUserImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //CALL THE IMAGE CROPPER LIBRARY TO CROP THE SELECTED IMAGE
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setMinCropResultSize(512, 512)
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

                    StorageReference filepath = mStorageReference.child("Profile Pictures").child(Objects.requireNonNull(mFirebaseUser.getPhoneNumber(), "Phone Number Cant Be Null")).child(mFirebaseUser.getPhoneNumber() + ".jpg");
                    filepath.putFile(mImageURI).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if (task.isSuccessful()) {
                                //gets the download uri of the file successfully uploaded to the storage and pass it as a string to downloadURI
                                mImageDownloadUri = Objects.requireNonNull(task.getResult().getDownloadUrl(), "Download Url Cant Be Null").toString();
                                //get the notification key
                                OneSignal.idsAvailable(new OneSignal.IdsAvailableHandler() {
                                    @Override
                                    public void idsAvailable(String userId, String registrationId) {
                                        //storing post info and data on firebase real-time database
                                        User user = new User(name, "user", mImageDownloadUri, 0, mFirebaseUser.getPhoneNumber(), userId);

                                        //save in real-time database
                                        mDatabaseReference.child("USER").child(mFirebaseUser.getPhoneNumber()).child("userInfo").setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    //if all the data was saved succesfully in the database
                                                    //i also want to add the location of the user in the database using geoFire
                                                    mGeoFire.setLocation(mFirebaseUser.getPhoneNumber(), new GeoLocation(mLocation.getLatitude(), mLocation.getLongitude()), new GeoFire.CompletionListener() {
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

                            } else {
                                Toasty.error(getApplicationContext(), "Couldn't Upload Profile Picture Check Your Internet Connection", Toast.LENGTH_SHORT, true).show();
                                mProgressBar.setVisibility(View.INVISIBLE);
                                mDoneButton.setVisibility(View.VISIBLE);

                            }
                        }
                    });

                } else {
                    Toast.makeText(Setup.this, "Enter Name And Select An Image", Toast.LENGTH_SHORT).show();
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
                mUserImage.setImageURI(mImageURI);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Toast.makeText(this, "Error Occurred In Image Cropper Activity", Toast.LENGTH_SHORT).show();
            }
        }
    }

    //get request permission results
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(), "Location Permissions Granted", Toast.LENGTH_SHORT).show();
                    if (checkPermission()) {
                        TextView locationView = findViewById(R.id.locationView);
                        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                        mLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        //show user its approximated current location
                        locationView.setText(String.format(Locale.getDefault(), "(Approximately)Currently Located At LAT: %.2f And LONG: %.2f", mLocation.getLatitude(), mLocation.getLongitude()));
                    }
                } else
                    Toast.makeText(getApplicationContext(), "App Needs Permission To Work", Toast.LENGTH_SHORT).show();
                break;

        }
    }

}
