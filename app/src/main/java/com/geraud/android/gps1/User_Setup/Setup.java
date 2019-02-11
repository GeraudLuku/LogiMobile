package com.geraud.android.gps1.User_Setup;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
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
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.onesignal.OneSignal;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import es.dmoral.toasty.Toasty;

public class Setup extends AppCompatActivity {

    private final static int REQUEST_CODE = 1;
    private final static String TYPE = "user";

    private StorageReference mStorageReference;
    private FirebaseAuth mFirebaseAuth;
    private DatabaseReference mDatabaseReference = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference mLocationDatabaseReference = FirebaseDatabase.getInstance().getReference().child("LOCATION");
    private GeoFire mGeoFire;

    private ImageView mUserImage;
    private TextView mLocationView;
    private EditText mUserName;
    private Button mDoneButton;
    private ProgressBar mProgressBar;
    private Uri mImageURI;

    private String mDownloadUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        //storage
        mStorageReference = FirebaseStorage.getInstance().getReference();
        mFirebaseAuth = FirebaseAuth.getInstance();
        mGeoFire = new GeoFire(mLocationDatabaseReference);

        //initialise image view , edit text , button and progress bar
        mUserImage = findViewById(R.id.profile_image);
        mLocationView = findViewById(R.id.locationView);
        mUserName = findViewById(R.id.name);
        mDoneButton = findViewById(R.id.submit);
        mProgressBar = findViewById(R.id.progress_bar);

        //check location services permissions
        checkLocationPermission();

        //get the users location and create an entry into the database with geofire
        LocationManager mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Location location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        final Double longitude = location.getLongitude();
        final Double latitude = location.getLatitude();
        //show user its approximated current location
        if (latitude != null && longitude != null)
            mLocationView.setText("(Approximately)Currently Located At LAT: " + Math.abs(latitude) + " And LONG: " + Math.abs(longitude));


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

                    StorageReference filepath = mStorageReference.child("Profile Pictures").child(mFirebaseAuth.getCurrentUser().getPhoneNumber()).child(mFirebaseAuth.getCurrentUser().getPhoneNumber() + "jpg");
                    filepath.putFile(mImageURI).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if (task.isSuccessful()) {
                                //gets the download uri of the file successfully uploaded to the storage and pass it as a string to downloadURI
                                try {
                                    mDownloadUri = task.getResult().getDownloadUrl().toString();
                                } catch (NullPointerException e) {
                                    Toast.makeText(Setup.this, "null pointer exception for download URI", Toast.LENGTH_SHORT).show();
                                }
                                //get the notification key
                                OneSignal.idsAvailable(new OneSignal.IdsAvailableHandler() {
                                    @Override
                                    public void idsAvailable(String userId, String registrationId) {
                                        //storing post info and data on firebase real-time database
                                        User user = new User(name, TYPE, mDownloadUri, 0, mFirebaseAuth.getCurrentUser().getPhoneNumber(), userId);

                                        //save in real-time database
                                        mDatabaseReference.child("USER").child(mFirebaseAuth.getCurrentUser().getPhoneNumber()).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    //if all the data was saved succesfully in the database
                                                    //i also want to add the location of the user in the database using geoFire
                                                    mGeoFire.setLocation(mFirebaseAuth.getCurrentUser().getPhoneNumber(), new GeoLocation(latitude, longitude), new GeoFire.CompletionListener() {
                                                        @Override
                                                        public void onComplete(String key, DatabaseError error) {
                                                            if (error != null) {//there was an error
                                                                Toast.makeText(Setup.this, "Couldnt upload Location To Database", Toast.LENGTH_SHORT).show();
                                                            } else {
                                                                Toasty.success(getApplicationContext(), "Successfully Updated Account Info!", Toast.LENGTH_SHORT, true).show();
                                                                startActivity(new Intent(getApplicationContext(), MapsActivity.class));  // Welcome to the main app
                                                                finish();
                                                            }
                                                        }
                                                    });

                                                } else {
                                                    //if there was an error
                                                    Toasty.error(getApplicationContext(), "Couldnt Create Account Check Your Internet Connection", Toast.LENGTH_SHORT, true).show();
                                                    mProgressBar.setVisibility(View.INVISIBLE);
                                                    mDoneButton.setVisibility(View.VISIBLE);
                                                }
                                            }
                                        });
                                    }
                                });

                            } else {
                                Toasty.error(getApplicationContext(), "Couldnt Upload picture Check Your Internet Connection", Toast.LENGTH_SHORT, true).show();
                                mProgressBar.setVisibility(View.INVISIBLE);
                                mDoneButton.setVisibility(View.VISIBLE);

                            }
                        }
                    });

                }
            }
        });


    }

    private void checkLocationPermission() {
        //check permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_CODE);
            }
        }
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
                Toasty.error(getApplicationContext(), result.getError().toString(), Toast.LENGTH_SHORT, true).show();
            }
        }
    }

    //get request permission results
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    if (grantResults.length > 0 && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                        //now you can try to get the location
                    } else
                        Toast.makeText(getApplicationContext(), "Permission 2 denied to get location", Toast.LENGTH_SHORT).show();
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(getApplicationContext(), "Permission 1 denied to get location", Toast.LENGTH_SHORT).show();
                }
                return;
            }

        }
    }

}
