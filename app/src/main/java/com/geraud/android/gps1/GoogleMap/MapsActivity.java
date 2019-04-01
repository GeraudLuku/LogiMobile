package com.geraud.android.gps1.GoogleMap;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.androidadvance.topsnackbar.TSnackbar;
import com.bumptech.glide.Glide;
import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.LocationCallback;
import com.geraud.android.gps1.Chat.ChatActivity;
import com.geraud.android.gps1.Chat.ChatsActivity;
import com.geraud.android.gps1.Dailogs.AddPlace;
import com.geraud.android.gps1.InfoWindow.PeopleMarkerInfoWindow;
import com.geraud.android.gps1.InfoWindow.PlacesMarkerInfoWindow;
import com.geraud.android.gps1.InfoWindow.TrackerInfoWindow;
import com.geraud.android.gps1.Models.Chat;
import com.geraud.android.gps1.Models.ChatInfo;
import com.geraud.android.gps1.Models.GeoFence;
import com.geraud.android.gps1.Models.Place;
import com.geraud.android.gps1.Models.User;
import com.geraud.android.gps1.R;
import com.geraud.android.gps1.Services.GeoQueries;
import com.geraud.android.gps1.Services.SinchService;
import com.geraud.android.gps1.Sinch.BaseActivity;
import com.geraud.android.gps1.Sinch.CallScreenActivity;
import com.geraud.android.gps1.Stories.BodyFragment;
import com.geraud.android.gps1.Stories.HeaderFragment;
import com.geraud.android.gps1.Utils.Contacts;
import com.geraud.android.gps1.Utils.PlacesTypeToDrawable;
import com.geraud.android.gps1.Utils.RandomStringGenerator;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.sinch.android.rtc.SinchError;
import com.sinch.android.rtc.calling.Call;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import es.dmoral.toasty.Toasty;

public class MapsActivity extends BaseActivity implements
        SinchService.StartFailedListener,
        AddPlace.AddPlaceListener {


    private static final int REQUEST_PERMISSION_CODE = 1;
    private static final int REQUEST_CHECK_SETTINGS = 123;
    private static final int PLACE_INTENT_REQUEST_CODE = 12;
    private static final String POPUP_CONSTANT = "mPopup";
    private static final String POPUP_FORCE_SHOW_ICON = "setForceShowIcon";


    private LinearLayout mBottomPeekLayout;

    private SeekBar mVerticalSeekBar;

    private Dialog mUserInfoDialog;
    private TextView closeText,
            locationView;
    private EditText userName;
    private ImageButton changeImage, changeName;
    private ImageView profileImage;
    private Button updateInfo;

    private DatabaseReference mDatabaseReference;
    private DatabaseReference mSearchRef;
    private String USER_KEY;

    private HeaderFragment StoryHeaderFragment;
    private BodyFragment StoryBodyFragment;
    private GoogleMap mMap;

    private BottomSheetBehavior mBottomSheetBehavior;

    private Gson mGson = new Gson();

    // The callback for the management of the user settings regarding location
    private ResultCallback<LocationSettingsResult> mResultCallbackFromSettings = new ResultCallback<LocationSettingsResult>() {
        @Override
        public void onResult(LocationSettingsResult result) {
            Status status = result.getStatus();
            String TAG = "gps";
            switch (status.getStatusCode()) {
                case LocationSettingsStatusCodes.SUCCESS:
                    // All location settings are satisfied. The client can initialize location
                    // requests here.
                    break;
                case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                    // Location settings are not satisfied. But could be fixed by showing the user
                    // a dialog.
                    try {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        status.startResolutionForResult(
                                MapsActivity.this,
                                REQUEST_CHECK_SETTINGS);
                    } catch (IntentSender.SendIntentException e) {
                        // Ignore the error.
                    }
                    break;
                case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                    Log.e(TAG, "Settings change unavailable. We have no way to fix the settings so we won't show the dialog.");
                    break;
            }
        }
    };

    private GoogleApiClient.ConnectionCallbacks mGoogleApiClientConnectionCallbacks = new GoogleApiClient.ConnectionCallbacks() {
        @Override
        public void onConnected(@Nullable Bundle bundle) {
            Toast.makeText(getApplicationContext(), "Connected - google maps api", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onConnectionSuspended(int i) {
            Toast.makeText(getApplicationContext(), "Suspended - google maps api", Toast.LENGTH_SHORT).show();

        }
    };

    private GoogleApiClient.OnConnectionFailedListener mGoogleApiClientConnectionFailedListener = new GoogleApiClient.OnConnectionFailedListener() {
        @Override
        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
            Toast.makeText(getApplicationContext(), "Failed - google maps api", Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        //fragments
        StoryHeaderFragment = new HeaderFragment();
        StoryBodyFragment = new BodyFragment();

        //start the sinch client/ initialise using my phone number
        if (!getSinchServiceInterface().isStarted()) {
            getSinchServiceInterface().startClient(USER_KEY);
        }

        mDatabaseReference = FirebaseDatabase.getInstance().getReference();
        mStorageReference = FirebaseStorage.getInstance().getReference();
        mSearchRef = mDatabaseReference.child("PLACES"); //by default

        FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
        USER_KEY = Objects.requireNonNull(mFirebaseAuth.getCurrentUser(), "User Cannot Be Null").getPhoneNumber();


        //floating action buttons
        FloatingActionButton mGetCurrentLocationBtn = findViewById(R.id.fab);
        mCancelRouteBtn = findViewById(R.id.cancelRoute);

        mGetCurrentLocationBtn.setOnClickListener(mFloatActionButtonClickListener);
        mCancelRouteBtn.setOnClickListener(mFloatActionButtonClickListener);

        //bottom sheet menu buttons
        mProfilePicture = findViewById(R.id.bottomSheet_Image);
        mProfileName = findViewById(R.id.bottomSheet_Name);

        LinearLayout mChatBtn = findViewById(R.id.chat);
        LinearLayout mPlacesBtn = findViewById(R.id.places);
        mChatBtn.setOnClickListener(mBottomViewClickListener);
        mPlacesBtn.setOnClickListener(mBottomViewClickListener);

        Switch mShareLocation = findViewById(R.id.showLocation);
        Switch mShowGeofences = findViewById(R.id.showGeofences);
        mShareLocation.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // The toggle is enabled
                    TextView textView = findViewById(R.id.showLocationTxt);
                    textView.setText(getString(R.string.hide_location));
                    if (!checkPermission())
                        requestPermission();
                    mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, INTERVAL, 1, mLocationListener);
                } else {
                    // The toggle is disabled
                    TextView textView = findViewById(R.id.showLocationTxt);
                    textView.setText(getString(R.string.share_location));
                    mLocationManager.removeUpdates(mLocationListener);
                    mGeoFire.removeLocation(USER_KEY);
                }
            }
        });
        mShowGeofences.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    TextView textView = findViewById(R.id.showGeofencesTxt);
                    textView.setText(getString(R.string.hide_geofences));
                    mGeoBoundary.setVisible(true);
                } else {
                    TextView textView = findViewById(R.id.showGeofencesTxt);
                    textView.setText(getString(R.string.show_geofences));
                    mGeoBoundary.setVisible(false);
                }
            }
        });

        mUserInfoDialog = new Dialog(getApplicationContext());
        //settings for user profile info
        ImageButton profileSettings = findViewById(R.id.userProfileSettings);
        profileSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createUserInfoDialog();
            }
        });


        //listen constantly to changes in text to the editText
        EditText mSearchBar = findViewById(R.id.search_text);
        mSearchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //do nothing if there is no text in the edit Text
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //constantly search on each key stroke
                Search(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                //search the final text in the edit Text
                Search(s.toString());
            }
        });


        mGeoFire = new GeoFire(mDatabaseReference.child("LOCATION"));

        mBottomPeekLayout = findViewById(R.id.tap_action_layout);
        mBottomPeekLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });
        View mBottomSheet = findViewById(R.id.bottom_sheet1);
        mBottomSheetBehavior = BottomSheetBehavior.from(mBottomSheet);
        mBottomSheetBehavior.setPeekHeight(mBottomPeekLayout.getHeight());
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        mBottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    mBottomPeekLayout.setVisibility(View.VISIBLE);
                    mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }

                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    mBottomPeekLayout.setVisibility(View.GONE);
                    mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                }

                if (newState == BottomSheetBehavior.STATE_DRAGGING) {
                    mBottomPeekLayout.setVisibility(View.GONE);
                    mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_DRAGGING);
                }
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    mBottomPeekLayout.setVisibility(View.GONE);
                    mBottomSheetBehavior.setPeekHeight(0);
                    mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                }

            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });

        //start mPolyLines
        mPolyLines = new ArrayList<>();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        //method for vertical seekbar
        mVerticalSeekBar = findViewById(R.id.verticalSeekBar);
        mVerticalSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //Animating the camera
                mMap.animateCamera(CameraUpdateFactory.zoomTo(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        // Find the toolbar view inside the activity layout
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar(), "Support Action Bar Can't Be Null").setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_status);

        //navigation drawer
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        //toggle search button to toggle the type of item to search on the map
        final ToggleButton mToggleSearchBtn = toolbar.findViewById(R.id.toggle_search);
        mToggleSearchBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    //change search to Users
                    mSearchRef = mDatabaseReference.child("USER").child("userInfo");
                    mToggleSearchBtn.setText("L");
                } else {
                    //change search to Places
                    mSearchRef = mDatabaseReference.child("PLACES");
                    mToggleSearchBtn.setText("P");
                }
            }
        });


        initialiseFragment();
        if (mapFragment != null)
            mapFragment.getMapAsync(mMapReadyCallback);

    }

    private void initialiseFragment() {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.story_header_fragment, StoryHeaderFragment);
        fragmentTransaction.add(R.id.story_body_fragment, StoryBodyFragment);
        fragmentTransaction.commit();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onStart() {
        mGoogleApiClient.connect();
        loadContacts();
        UserDetails();
        makeUserOnline();
        super.onStart();
    }

    @Override
    protected void onResume() {
        getCurrentLocation();
        makeUserOnline();
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        makeUserAway();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        makeUserOffline();
        StopTracking();
        StopLoadFriends();
        if (mLocationManager != null)
            mLocationManager.removeUpdates(mLocationListener);
        super.onStop();
    }

    @Override
    public void onDestroy() {
        if (getSinchServiceInterface() != null) {
            getSinchServiceInterface().stopClient();
        }
        if (mServiceIsBound) {
            unbindService(mServiceConnection);
            mServiceIsBound = false;
        }
        super.onDestroy();
    }

    private boolean mServiceIsBound = false;
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mGeoQueriesService = ((GeoQueries.LocalBinder) service).getService();
            Toast.makeText(getApplicationContext(), "GeoQueries service connection passed", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mGeoQueriesService = null;
            Toast.makeText(getApplicationContext(), "Geoqueries service Disconnected", Toast.LENGTH_SHORT).show();
        }
    };

    private View.OnClickListener mUserInfoDialogClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.txtclose: //close dialog
                    mUserInfoDialog.dismiss();
                    break;
                case R.id.changeImage: //choose image from gallery
                    CropImage.activity()
                            .setGuidelines(CropImageView.Guidelines.ON)
                            .setMinCropResultSize(512, 512)
                            .setAspectRatio(1, 1)
                            .start(MapsActivity.this);
                    break;
                case R.id.changeName:// set edit text focusable and enabled then change text
                    userName.setEnabled(true);
                    userName.setFocusable(true);
                    mModified = true;
                    break;
                case R.id.updateInfo:
                    if (mModified) {
                        if (!userName.getText().toString().equals(""))
                            mUserInfoObject.setName(userName.getText().toString());
                        //upload image first
                        UploadTask uploadTask = mStorageReference.child("Profile Pictures").child(USER_KEY).child(USER_KEY + ".jpg").putFile(Uri.parse(mUserInfoObject.getImage_uri()));
                        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                mUserInfoObject.setImage_uri(Objects.requireNonNull(taskSnapshot.getDownloadUrl(), "update Info downloadUri Cant Be Null").toString());
                                mDatabaseReference.child("USER").child(USER_KEY).child("userInfo").setValue(mUserInfoObject).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(getApplicationContext(), "Update Successful", Toast.LENGTH_SHORT).show();
                                            mUserInfoDialog.dismiss();
                                        } else {
                                            Toast.makeText(getApplicationContext(), "Couldn't Update User Object", Toast.LENGTH_SHORT).show();
                                            mUserInfoDialog.dismiss();
                                        }
                                    }
                                });
                            }
                        });

                    } else
                        mUserInfoDialog.dismiss();
                    break;
                default:
                    Toast.makeText(getApplicationContext(), "Invalid Click Id For Dialog", Toast.LENGTH_SHORT).show();
            }
        }
    };

    private boolean mModified = false;

    private void createUserInfoDialog() {
        //change user name and image
        // will initialise the dailog here first work with the dailog variables
        mUserInfoDialog.setContentView(R.layout.user_settings);

        closeText = mUserInfoDialog.findViewById(R.id.txtclose);
        locationView = mUserInfoDialog.findViewById(R.id.locationView);
        userName = mUserInfoDialog.findViewById(R.id.nameEdittext);
        changeImage = mUserInfoDialog.findViewById(R.id.changeImage);
        profileImage = mUserInfoDialog.findViewById(R.id.imageView);
        updateInfo = mUserInfoDialog.findViewById(R.id.updateInfo);
        changeName = mUserInfoDialog.findViewById(R.id.changeName);

        //setting user info
        Glide.with(getApplicationContext()).load(mUserInfoObject.getImage_uri()).into(profileImage);
        userName.setText(mUserInfoObject.getName());
        mGeoFire.getLocation(mUserInfoObject != null ? mUserInfoObject.getPhone() : null, new LocationCallback() {
            @Override
            public void onLocationResult(String key, GeoLocation location) {
                if (location != null) {
                    try {
                        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                        List<Address> address = geocoder.getFromLocation(location.latitude, location.longitude, 1);
                        locationView.setText(String.format(Locale.getDefault(), "Currently Located At : %s, %s %s", address.get(0).getFeatureName(), address.get(0).getLocality(), address.get(0).getCountryName()));
                    } catch (IOException e) {
                        Toast.makeText(getApplicationContext(), "Couldn't Get Address Of Your Location", Toast.LENGTH_SHORT).show();
                    }
                } else
                    Toast.makeText(getApplicationContext(), "Your Location Is Null", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), "Load Location Dialog ValueEventListener Cancelled", Toast.LENGTH_SHORT).show();
            }
        });

        //onClick Actions
        closeText.setOnClickListener(mUserInfoDialogClickListener);
        changeImage.setOnClickListener(mUserInfoDialogClickListener);
        changeName.setOnClickListener(mUserInfoDialogClickListener);
        updateInfo.setOnClickListener(mUserInfoDialogClickListener);

        Objects.requireNonNull(mUserInfoDialog.getWindow(), "UserInfoDialog Window Cant Be Null").setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mUserInfoDialog.show();
    }


    private int INTERVAL = 1000;
    private LocationManager mLocationManager;
    //all related map actions take place here
    private OnMapReadyCallback mMapReadyCallback = new OnMapReadyCallback() {
        @Override
        public void onMapReady(GoogleMap googleMap) {
            mMap = googleMap;

            //adaptive map function
            runOnUiThread(new Runnable() {
                Calendar calendar = Calendar.getInstance();
                int timeOfTheDay = calendar.get(Calendar.HOUR_OF_DAY);

                @Override
                public void run() {
                    mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getApplicationContext(),
                            (timeOfTheDay >= 6 && timeOfTheDay < 17) ? R.raw.day_map : R.raw.night_map
                    ));
                }
            });

            LocationRequest mLocationRequest = LocationRequest.create();
            mLocationRequest.setInterval(INTERVAL);
            mLocationRequest.setFastestInterval(INTERVAL);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

            mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

            getCurrentLocation();
            LoadPlaces();
            loadGeoFences();
            LoadFriends();

            //Setting onMarker/OnMap Listeners
            mMap.setOnMapLongClickListener(mOnMapLongCLickListener);
            mMap.setOnMapClickListener(mOnMapClickListener);
            mMap.setOnMarkerClickListener(mOnMarkerClickListener);
            mMap.setOnCircleClickListener(mOnCircleClickListener);


            if (checkPermission()) {
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, INTERVAL, 0, mLocationListener);
                // Check the location settings of the user and create the callback to react to the different possibilities
                LocationSettingsRequest.Builder locationSettingsRequestBuilder = new LocationSettingsRequest.Builder()
                        .addLocationRequest(mLocationRequest);
                PendingResult<LocationSettingsResult> result =
                        LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, locationSettingsRequestBuilder.build());
                result.setResultCallback(mResultCallbackFromSettings);
            } else {
                requestPermission();
            }
        }
    };


    private void requestPermission() {

        ActivityCompat.requestPermissions(MapsActivity.this, new String[]
                {
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.READ_CONTACTS
                }, REQUEST_PERMISSION_CODE);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {

            case REQUEST_PERMISSION_CODE:

                if (grantResults.length > 0) {

                    boolean finelocation = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean coarselocation = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    boolean readContacts = grantResults[2] == PackageManager.PERMISSION_GRANTED;

                    if (finelocation && coarselocation && readContacts) {
                        if (checkPermission()) {
                            buildGoogleApiClient();
                            mMap.setMyLocationEnabled(true);
                            mContactList = new Contacts(getApplicationContext()).getAllContacts();
                            bindService(new Intent(getApplicationContext(), GeoQueries.class), mServiceConnection, BIND_AUTO_CREATE);
                            mServiceIsBound = true;
                        }
                        Toast.makeText(MapsActivity.this, "Location And Contacts Permission Granted", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(MapsActivity.this, "Location And Contacts Permission Denied", Toast.LENGTH_LONG).show();

                    }
                }
                break;
        }
    }

    public boolean checkPermission() {

        int FirstPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION);
        int SecondPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION);
        int ThirdPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_CONTACTS);

        return FirstPermissionResult == PackageManager.PERMISSION_GRANTED &&
                SecondPermissionResult == PackageManager.PERMISSION_GRANTED &&
                ThirdPermissionResult == PackageManager.PERMISSION_GRANTED;

    }


    private GoogleApiClient mGoogleApiClient;

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(mGoogleApiClientConnectionCallbacks)
                .addOnConnectionFailedListener(mGoogleApiClientConnectionFailedListener)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    private List<String> mContactList = new ArrayList<>();

    private void loadContacts() {
        //check if contacts permission was accepted
        if (checkPermission()) {
            mContactList = new Contacts(getApplicationContext()).getAllContacts();
            bindService(new Intent(getApplicationContext(), GeoQueries.class), mServiceConnection, BIND_AUTO_CREATE);
            mServiceIsBound = true;
        } else
            requestPermission();
    }

    private Location mLastLocation;
    private GeoFire mGeoFire;
    private LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            mLastLocation = location;

//            //update marker on screen
//            if (mUserMarker != null) mUserMarker.remove();
//
//            mUserMarker = mMap.addMarker(new MarkerOptions()
//                    .position(latLng) //setting position
//                    .draggable(false) //Making the marker non draggable
//                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.rsz_personal_avatar)) //icon image here i am going to put the users bitmojii
//                    .title("Me")); //Adding my name as title
//
//            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
//            mMap.animateCamera(CameraUpdateFactory.zoomTo(15));

            //update location in real-time Database
            mGeoFire.setLocation(USER_KEY, new GeoLocation(location.getLatitude(), location.getLongitude()));
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {
            Toast.makeText(getApplicationContext(), provider + " - provider Enabled", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onProviderDisabled(String provider) {
            Toast.makeText(getApplicationContext(), provider + " - provider Disabled", Toast.LENGTH_SHORT).show();
        }
    };

    private boolean mToolBarState = true;
    private GoogleMap.OnMapClickListener mOnMapClickListener = new GoogleMap.OnMapClickListener() {
        @Override
        public void onMapClick(LatLng latLng) {
            //set toolbar visible / invisible on tap
            if (mToolBarState) {
                Objects.requireNonNull(getSupportActionBar(), "Support Action Bar Cant Be Null").hide();
                mVerticalSeekBar.setVisibility(View.INVISIBLE);
                mToolBarState = false;
            } else {
                Objects.requireNonNull(getSupportActionBar(), "Support Action Bar Cant Be Null").show();
                mVerticalSeekBar.setVisibility(View.VISIBLE);
                mToolBarState = true;
            }
        }
    };

    private LatLng mClickLatLng;
    private GoogleMap.OnMapLongClickListener mOnMapLongCLickListener = new GoogleMap.OnMapLongClickListener() {
        @Override
        public void onMapLongClick(LatLng latLng) {
            //information for places latitude and longitude
            mClickLatLng = latLng;
            //open dialog for options
            creatorWindow();
        }
    };


    private GoogleMap.OnCircleClickListener mOnCircleClickListener = new GoogleMap.OnCircleClickListener() {
        @Override
        public void onCircleClick(Circle circle) {
            final Circle radius = circle;
            //if circle is clicked you can make the user choose to delete it or not.
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getApplicationContext());
            alertDialogBuilder.setMessage("Do you want to Delete this GeoFence?")
                    .setNegativeButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //use co ordinates to delete it in firebase
                            mDatabaseReference.child("GEOFENCES").child(USER_KEY)
                                    .child(String.valueOf(radius.getCenter().latitude + radius.getCenter().longitude))
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.exists())
                                                dataSnapshot.getRef().removeValue();
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {
                                            Toast.makeText(getApplicationContext(), "Delete GeoFence ValueEventListener Cancelled", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    })
                    .setPositiveButton("Cancel", null)
                    .setIcon(R.drawable.ic_cancel_route)
                    .show();
        }
    };


    private LatLng mMarkerClickLatLng;
    private Marker mMarkerClickData;
    private GoogleMap.OnMarkerClickListener mOnMarkerClickListener = new GoogleMap.OnMarkerClickListener() {
        @Override
        public boolean onMarkerClick(Marker marker) {
            //markers latitude and longitude
            mMarkerClickLatLng = marker.getPosition();
            //get marker GSON object from the snippet
            mMarkerClickData = marker;
            // store the marker also inside the tracking marker
            mTracker = marker;

            switch (marker.getTitle()) {
                case "user":
                    //set the custom info window adapter
                    mMap.setInfoWindowAdapter(new PeopleMarkerInfoWindow(getApplicationContext()));
                    mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                        @Override
                        public void onInfoWindowClick(Marker marker) {
                            //here i will show the various options to do on the maker
                            peopleMenu();
                        }
                    });
                    break;
                case "place":
                    //set the custom info window adapter
                    mMap.setInfoWindowAdapter(new PlacesMarkerInfoWindow(MapsActivity.this));
                    mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                        @Override
                        public void onInfoWindowClick(Marker marker) {
                            //here i will show the various options to do on the maker
                            placeMenu();
                        }
                    });
                    break;
                case "track":
                    mMap.setInfoWindowAdapter(new TrackerInfoWindow(getApplicationContext()));
                    mMap.setOnInfoWindowLongClickListener(new GoogleMap.OnInfoWindowLongClickListener() {
                        @Override
                        public void onInfoWindowLongClick(Marker marker) {
                            Gson gson = new Gson();
                            User userInfo = gson.fromJson(marker.getSnippet(), User.class);
                            //close the tracking
                            new AlertDialog.Builder(getApplicationContext())
                                    .setTitle("User Tracking")
                                    .setMessage("Stop Tracking " + userInfo.getName() + "?")
                                    .setCancelable(true)
                                    .setNegativeButton("Stop", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            StopTracking();
                                        }
                                    })
                                    .setPositiveButton("Continue", null)
                                    .setIcon(R.drawable.ic_cancel_route)
                                    .show();
                        }
                    });
                    break;
                //else if its my marker (my location) so as not to cause a NUllPointer Exception ;)
                default:
                    Toast.makeText(getApplicationContext(), "My Marker", Toast.LENGTH_SHORT).show();
                    break;
            }

            return false;
        }
    };

    private PopupMenu.OnMenuItemClickListener mMenuItemClickListener = new PopupMenu.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.chatMenuBtn:
                    //check if the chat already exists
                    //get GSON and convert it
                    Gson gson = new Gson();
                    final User userInfo = gson.fromJson(mMarkerClickData.getSnippet(), User.class);

                    Query query = mDatabaseReference.child("USER").child("chat").orderByChild("user").equalTo(userInfo.getPhone());
                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                //if the chat already exists, open the chat in chat activity
                                Chat mChat = new Chat(dataSnapshot.getKey());
                                mChatList.add(mChat);
                                getChatData(mChat.getChatId());
                            } else {
                                //if it doesn't exist create a new chat then......
                                String key = mDatabaseReference.child("CHAT").push().getKey();
                                DatabaseReference userDB = mDatabaseReference.child("USER");
                                DatabaseReference chatInfoDB = mDatabaseReference.child("CHAT").child(key).child("info");

                                mNewChatMap.put("id", key);
                                mNewChatMap.put("lastMessage/", false);
                                mNewChatMap.put("users/" + USER_KEY, true);
                                mNewChatMap.put("users/" + userInfo.getPhone(), true);
                                mNewChatMap.put("name", false);
                                mNewChatMap.put("type", "single");
                                mNewChatMap.put("image", false);

                                Map<String, String> myChatMap = new HashMap<>();
                                myChatMap.put("type", "single");
                                myChatMap.put("user", userInfo.getPhone());

                                Map<String, String> singleChatMap = new HashMap<>();
                                singleChatMap.put("type", "single");
                                singleChatMap.put("user", USER_KEY);

                                chatInfoDB.updateChildren(mNewChatMap);
                                userDB.child(USER_KEY).child("chat").child(key).setValue(myChatMap); //chat id set in my Document
                                userDB.child(Objects.requireNonNull(myChatMap.get("user"), "User ID Cant Be Null")).child("chat").child(key).setValue(singleChatMap); // set chat id in the other users document

                                //open chatActivity for this newly created chat
                                getChatData(key);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Toast.makeText(getApplicationContext(), "ChatMenuBTN ValueEventListener Cancelled", Toast.LENGTH_SHORT).show();
                        }
                    });

                case R.id.gotoMenuBtn:
                    drawRoute(mMarkerClickLatLng);
                    break;
                case R.id.callMenuBtn:
                    call(mMarkerClickData);
                case R.id.trackMenuBtn:
                    TrackMarker(mTracker);

            }
            return false;
        }
    };

    private Map<String, Object> mNewChatMap = new HashMap<>();
    private List<Chat> mChatList = new ArrayList<>();
    private List<ChatInfo> mChatInfo = new ArrayList<>();

    private void getChatData(String chatId) {
        DatabaseReference mChatDB = FirebaseDatabase.getInstance().getReference().child("CHAT").child(chatId).child("info");
        mChatDB.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String chatId = "";

                    //getting id of the chat
                    if (dataSnapshot.child("id").getValue() != null)
                        chatId = Objects.requireNonNull(dataSnapshot.child("id").getValue(), "ID Cant Be Null").toString();

                    //getting all users
                    for (DataSnapshot userSnaphshot : dataSnapshot.child("users").getChildren())
                        for (Chat mChat : mChatList) {
                            if (mChat.getChatId().equals(chatId)) {
                                User mUser = new User(userSnaphshot.getKey());
                                mChat.addUserToArrayList(mUser);
                                getUserData(mUser, chatId);
                            }
                        }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), "getChatData ValueEventListener Cancelled", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void getUserData(User mUser, final String chatId) {
        DatabaseReference mUserDb = FirebaseDatabase.getInstance().getReference().child("USER").child(mUser.getPhone());
        mUserDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User mUser = new User(dataSnapshot.getKey());
                //getting notification key
                if (dataSnapshot.child("notificationKey").getValue() != null)
                    mUser.setNotificationKey(Objects.requireNonNull(dataSnapshot.child("notificationKey").getValue(), "Notification Key Cant Be Null").toString());

                for (Chat mChat : mChatList) {
                    for (User mUserIt : mChat.getUserObjectArrayList()) {
                        if (mUserIt.getPhone().equals(mUser.getPhone())) {
                            mUserIt.setNotificationKey(mUser.getNotificationKey());
                        }
                    }
                }
                //to put in the chatInfo object
                getChatMetaData(chatId);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), "getUserData ValueEventListener Cancelled", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getChatMetaData(String chatId) {
        DatabaseReference mChatInfoDB = FirebaseDatabase.getInstance().getReference().child("CHAT").child(chatId).child("info");
        mChatInfoDB.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot dc : dataSnapshot.getChildren()) {
                        ChatInfo chatInfo = dc.getValue(ChatInfo.class);
                        mChatInfo.add(chatInfo);
                    }
                    // create intent here and send extras (mChatInfo,mChatList)
                    Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
                    intent.putExtra("chatObject", mChatList.get(0));  // get the first document i the list because there is only one document
                    intent.putExtra("chatInfoObject", mChatInfo.get(0));
                    startActivity(intent);
                    finish();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), "getChatMetaData ValueEventListener Cancelled", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void peopleMenu() {
        //i will rather make a pop up menu it will be better for this
        PopupMenu popup = new PopupMenu(getApplicationContext(), Objects.requireNonNull(getCurrentFocus(), "People Menu Must Return Current Focus"));
        try {
            // Reflection apis to enforce show icon
            Field[] fields = popup.getClass().getDeclaredFields();
            for (Field field : fields) {
                if (field.getName().equals(POPUP_CONSTANT)) {
                    field.setAccessible(true);
                    Object menuPopupHelper = field.get(popup);
                    Class<?> classPopupHelper = Class.forName(menuPopupHelper.getClass().getName());
                    Method setForceIcons = classPopupHelper.getMethod(POPUP_FORCE_SHOW_ICON, boolean.class);
                    setForceIcons.invoke(menuPopupHelper, true);
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        popup.getMenuInflater().inflate(R.menu.info_window_people, popup.getMenu());
        popup.setOnMenuItemClickListener(mMenuItemClickListener);
        popup.show();
    }

    //alert dialog when pressing options on the info window of Places
    private void placeMenu() {
        PopupMenu popup = new PopupMenu(getApplicationContext(), Objects.requireNonNull(getCurrentFocus(), "Place Menu Must Return Current Focus"));
        try {
            // Reflection apis to enforce show icon
            Field[] fields = popup.getClass().getDeclaredFields();
            for (Field field : fields) {
                if (field.getName().equals(POPUP_CONSTANT)) {
                    field.setAccessible(true);
                    Object menuPopupHelper = field.get(popup);
                    Class<?> classPopupHelper = Class.forName(menuPopupHelper.getClass().getName());
                    Method setForceIcons = classPopupHelper.getMethod(POPUP_FORCE_SHOW_ICON, boolean.class);
                    setForceIcons.invoke(menuPopupHelper, true);
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        popup.getMenuInflater().inflate(R.menu.info_window_places, popup.getMenu());
        popup.setOnMenuItemClickListener(mMenuItemClickListener);
        popup.show();
    }

    /*-------------------------------------------- Map specific functions -----
    |  Function(s) getCurrentLocation
    |
    |  The blue dot and the precision circle are automatically managed by the map and you can't update
        it or change it's symbology.
        In fact, it's managed automatically using it's own LocationProvider so it gets the best location resolution available
        (you don't need to write code to update it, just enable it using mMap.setMyLocationEnabled(true);).
    |
    *-------------------------------------------------------------------*/

    private void getCurrentLocation() {

        if (mLastLocation != null) {
            //moving the map to my  location
            //Creating a LatLng Object to store Coordinates
            LatLng latLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
//            if (mUserMarker != null)
//                mUserMarker.remove();
//            //Adding marker to map
//            mUserMarker = mMap.addMarker(new MarkerOptions()
//                    .position(latLng) //setting position
//                    .draggable(false) //Making the marker non draggable
//                    //.icon(BitmapDescriptorFactory.fromResource(R.drawable.rsz_personal_avatar)) //icon image here i am going to put the users bitmojii
//                    .title("Me")); //Adding my name as title

            //Moving the camera
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            //Animating the camera
            mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
            //update value on seekbar
            mVerticalSeekBar.setProgress(15);
            //Displaying current coordinates in toast
            //  Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
        }
    }

    //goto method on the map
    public PolygonOptions mSearchPolygonOptions;
    private Polygon mSearchPolygon;

    private void goTO(LatLng latLng) {

        if (latLng != null) {

            if (mSearchPolygon != null) {
                mSearchPolygon.remove();
                mSearchPolygon = null;
            }

            //Moving the camera
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            //Animating the camera
            mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
            //update value on seekbar
            mVerticalSeekBar.setProgress(15);

            mSearchPolygonOptions.add(latLng);
            mSearchPolygonOptions.strokeWidth(5);
            mSearchPolygonOptions.strokeColor(ContextCompat.getColor(getApplicationContext(), R.color.light_sky_blue));
            mSearchPolygonOptions.fillColor(ContextCompat.getColor(getApplicationContext(), R.color.yellow_green));

            mSearchPolygon = mMap.addPolygon(mSearchPolygonOptions);

        }
    }

    private View.OnClickListener mBottomViewClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.chat:
                    startActivity(new Intent(getApplicationContext(), ChatsActivity.class));
                    break;
                case R.id.places:
                    //Todo: Create Activity That Will Show All Places And Return Location On Activity Result
                    Intent i = new Intent(getApplicationContext(), PlacesActivity.class);
                    startActivityForResult(i, PLACE_INTENT_REQUEST_CODE);
                    break;
            }
        }
    };

    private FloatingActionButton mCancelRouteBtn;
    private View.OnClickListener mFloatActionButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.fab:
                    getCurrentLocation();
                    break;
                case R.id.cancelRoute:
                    eraseAllRouteLines();
                    break;
            }
        }
    };

    //method to load Current user details into dashboard
    private ImageView mProfilePicture;
    private TextView mProfileName;
    private User mUserInfoObject;

    public void UserDetails() {
        mDatabaseReference.child("USER").child(USER_KEY).child("userInfo").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot doc : dataSnapshot.getChildren()) {
                        mUserInfoObject = doc.getValue(User.class);
                        if (mUserInfoObject != null) {
                            Glide.with(getApplicationContext()) //load profile picture
                                    .load(mUserInfoObject.getImage_uri())
                                    .into(mProfilePicture);
                            mProfileName.setText(mUserInfoObject.getName());
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toasty.error(getApplicationContext(), "Failed to load your information", 3000).show();
            }
        });
    }

    private StorageReference mStorageReference;

    public void OpenAddPlace() {
        AddPlace addPlace = new AddPlace();
        addPlace.show(getSupportFragmentManager(), "Add Place");
        //part 2 right below!!!!
    }

    //dialog completion action for creating a new Place
    @Override
    public void AddPlaceOnMap(final String name, final String description, final Uri imageUri, final String dec2) {

        final StorageReference filePath = mStorageReference.child("Places Images").child(USER_KEY).child(RandomStringGenerator.randomString() + ".jpg");
        UploadTask uploadTask = filePath.putFile(Uri.parse(imageUri.toString()));
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Place place = new Place(name, USER_KEY, description, uri.toString(), "place", mClickLatLng.latitude, mClickLatLng.longitude, dec2);
                        //create a database entry for the new place
                        mDatabaseReference.child("PLACES").child(USER_KEY).push().setValue(place).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    //if the place ws added successfully you want to reload the places on the map
                                    LoadPlaces();
                                    mClickLatLng = null;
                                } else
                                    Toast.makeText(getApplicationContext(), "couldn't create place", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
            }
        });
    }

    private Marker mPlaces;

    private void LoadPlaces() {
        if (mPlaces != null) mPlaces.remove();

        //load my places here before loading other peoples places
        mDatabaseReference.child("PLACES").child(USER_KEY).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists())
                    for (DataSnapshot dc : dataSnapshot.getChildren()) {
                        Place myPlace = dc.getValue(Place.class);
                        //create JSON object to send the info to infoWindow adapter through the snippet
                        String PlaceJSON = mGson.toJson(myPlace);
                        mPlaces = mMap.addMarker(new MarkerOptions()
                                .position(new LatLng(myPlace != null ? myPlace.getLatitude() : 0, myPlace != null ? myPlace.getLongitude() : 0))
                                .title(myPlace != null ? myPlace.getType() : null)
                                .snippet(PlaceJSON)
                                .icon(BitmapDescriptorFactory.fromResource(PlacesTypeToDrawable.getDrawable(myPlace != null ? myPlace.getDesc2() : null)))
                                .draggable(false)
                        );
                    }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), "Places ValueEventListener-1 Cancelled", Toast.LENGTH_SHORT).show();
            }
        });

        //now load my mContactList places also
        //for each friend
        for (String contact : mContactList)
            mDatabaseReference.child("PLACES").child(contact).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists())
                        for (DataSnapshot dc : dataSnapshot.getChildren()) {
                            Place myPlace = dc.getValue(Place.class);
                            //create JSON object to send the info to infoWindow adapter through the snippet
                            String PlaceJSON = mGson.toJson(myPlace);
                            mPlaces = mMap.addMarker(new MarkerOptions()
                                    .position(new LatLng(myPlace != null ? myPlace.getLatitude() : 0, myPlace != null ? myPlace.getLongitude() : 0))
                                    .title(myPlace != null ? myPlace.getType() : null)
                                    .snippet(PlaceJSON)
                                    .icon(BitmapDescriptorFactory.fromResource(PlacesTypeToDrawable.getDrawable(myPlace != null ? myPlace.getDesc2() : null)))
                                    .draggable(false)
                            );
                        }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(getApplicationContext(), "Places ValueEventListener-2 Cancelled", Toast.LENGTH_SHORT).show();
                }
            });

    }

    private Timer mFriendsTimer;
    private Marker mFriendsMarker;
    private TimerTask mLoadFriendsTimerTask = new TimerTask() {
        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mFriendsMarker != null)
                        mFriendsMarker.remove();
                    for (final String contact : mContactList) {
                        //first query the users section of the database to get the Contact information
                        mDatabaseReference.child("USER").child(contact).child("userInfo").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    for (DataSnapshot dc : dataSnapshot.getChildren()) {
                                        final User user = dc.getValue(User.class);
                                        final String friendExtraInfo = mGson.toJson(user);
                                        //second query to get the location to finally display mContactList location
                                        mGeoFire.getLocation(user != null ? user.getPhone() : null, new LocationCallback() {
                                            @Override
                                            public void onLocationResult(String key, GeoLocation location) {
                                                if (location != null) {
                                                    mFriendsMarker = mMap.addMarker(new MarkerOptions()
                                                            .position(new LatLng(location.latitude, location.longitude))
                                                            .title(user != null ? user.getType() : null)
                                                            .snippet(friendExtraInfo)
                                                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.friend_map_icon))
                                                            .draggable(false)
                                                    );
                                                } else
                                                    Toast.makeText(getApplicationContext(), "Location is null for contact", Toast.LENGTH_SHORT).show();
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {
                                                Toast.makeText(getApplicationContext(), "Load Location ValueEventListener Cancelled", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                } else
                                    Toast.makeText(getApplicationContext(), contact + " - Doesn't exist", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Toast.makeText(getApplicationContext(), "Load ContactInfo ValueEventListener Cancelled", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            });
        }
    };

    private void LoadFriends() {
        if (!mContactList.isEmpty()) {
            //start a timer to run after 1 second
            mFriendsTimer = new Timer();
            mFriendsTimer.schedule(mLoadFriendsTimerTask, TimeUnit.MINUTES.toMillis(1));

        } else
            Toast.makeText(getApplicationContext(), "No contacts found", Toast.LENGTH_SHORT).show();
    }

    private void StopLoadFriends() {
        mFriendsTimer.cancel();
        mFriendsTimer.purge();
    }

    private Marker mTracker;
    private Timer mTrackerTimer;
    private Boolean isTracking = false;
    private TimerTask mTrackFriendTimerTask = new TimerTask() {
        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //get user info from the marker
                    final User user = mGson.fromJson(mTracker.getSnippet(), User.class);
                    mGeoFire.getLocation(user.getPhone(), new LocationCallback() {
                        @Override
                        public void onLocationResult(String key, GeoLocation location) {
                            if (location != null) {
                                mTracker = mMap.addMarker(new MarkerOptions()
                                        .position(new LatLng(location.latitude, location.longitude))
                                        .title(user.getName())
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.tracker_map_icon))
                                        .draggable(false)
                                );
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Toast.makeText(getApplicationContext(), "Tracker ValueEventListener Cancelled", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        }
    };

    private void TrackMarker(Marker marker) {
        StopTracking();
        mTracker = marker;
        mTrackerTimer = new Timer();
        mTrackerTimer.schedule(mTrackFriendTimerTask, TimeUnit.SECONDS.toMillis((long) .5));
        isTracking = true;
    }

    private void StopTracking() {
        if (isTracking) {
            //if there is an actively tracked icon
            mTrackerTimer.cancel();
            mTrackerTimer.purge();
            mTracker.remove();
            isTracking = false;
        }
    }

    //creating a geo coding boundary
    private void AddGeoBoundary() {
        //save geofence in Realtime Databse for Future use
        GeoFence geoFence = new GeoFence(mClickLatLng.latitude, mClickLatLng.longitude);
        mDatabaseReference.child("GEOFENCES").child(USER_KEY).child(String.valueOf(mClickLatLng.latitude + mClickLatLng.longitude)).setValue(geoFence).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    loadGeoFences();
                    Toasty.success(getApplicationContext(), "Geofence created successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Couldn't create Geoboundary", Toast.LENGTH_SHORT).show();
                }
            }
        });
        mClickLatLng = null;
    }

    private int numGeofences = 0;
    private List<LatLng> mGeoLocationsList;
    private GeoQueries mGeoQueriesService;
    private Circle mGeoBoundary;

    private void loadGeoFences() {
        mGeoLocationsList = new ArrayList<>();
        mDatabaseReference.child("GEOFENCES").child(USER_KEY).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (mGeoBoundary != null)
                        mGeoBoundary.remove();
                    for (DataSnapshot dc : dataSnapshot.getChildren()) {
                        GeoFence geoFence = dc.getValue(GeoFence.class);
                        //save the location in the list of geoLoctions to be used in a service for query
                        LatLng latLng = new LatLng(geoFence != null ? geoFence.getLatitude() : 0, geoFence != null ? geoFence.getLongitude() : 0);
                        mGeoLocationsList.add(latLng);
                        mGeoBoundary = mMap.addCircle(new CircleOptions()
                                .center(latLng)
                                .radius(600)
                                .strokeColor(ContextCompat.getColor(getApplicationContext(), R.color.light_sky_blue))
                                .fillColor(ContextCompat.getColor(getApplicationContext(), R.color.sky_blue))
                                .strokeWidth(4.0f)
                        );
                        numGeofences++;
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), "Geofences ValueEventListener Cancelled", Toast.LENGTH_SHORT).show();
            }
        });
        //after all the geofences are loaded, start the queries listener service
        mGeoQueriesService.query(getApplicationContext(), mGeoLocationsList, mContactList);
    }

    private void creatorWindow() {

        AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());
        LayoutInflater mLayoutInflater = this.getLayoutInflater();
        View mDialogView = mLayoutInflater.inflate(R.layout.creator_window, null);
        builder.setView(mDialogView);

        //if add place to map
        mDialogView.findViewById(R.id.addPlaceToMap).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenAddPlace();
            }
        });
        //if add geo boundary
        mDialogView.findViewById(R.id.addGeo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (numGeofences < 6) {
                    AddGeoBoundary();
                } else
                    Toast.makeText(getApplicationContext(), "Geofence creation exceeded (MAX=5)", Toast.LENGTH_SHORT).show();
            }
        });

        builder.show();
    }

    //search method to search on the MAP
    private void Search(final String text) {
        //this method is case sensitive take note!!!
        mSearchRef.orderByChild("name")
                .startAt(text)
                .endAt(text + "\uf8ff")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.exists()) {

                            for (DataSnapshot dc : dataSnapshot.getChildren()) {

                                try {
                                    //if the datasnapshot can enter the user object
                                    User user = dc.getValue(User.class);
                                    //getting location with/from geofire
                                    if (user != null)
                                        mGeoFire.getLocation(user.getPhone(), new LocationCallback() {
                                            @Override
                                            public void onLocationResult(String key, GeoLocation location) {
                                                if (location != null) {
                                                    goTO(new LatLng(location.latitude, location.longitude));
                                                }
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {
                                                Toast.makeText(getApplicationContext(), "Search ValueEventListener Cancelled", Toast.LENGTH_SHORT).show();
                                            }
                                        });

                                } catch (Exception e) {
                                    //else insert it into the place object
                                    Toast.makeText(getApplicationContext(), "Couldn't Put Searched Object in User Object--- Trying Place Object", Toast.LENGTH_SHORT).show();
                                    Place place = dc.getValue(Place.class);
                                    if (place != null)
                                        goTO(new LatLng(place.getLatitude(), place.getLongitude()));
                                }
                            }

                        } else
                            Toast.makeText(getApplicationContext(), text + " - Searched doesn't exist", Toast.LENGTH_SHORT).show();

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    //make user online
    private void makeUserOnline() {
        //update Realtime Database status value
        mDatabaseReference.child("USER").child(USER_KEY).child("userInfo").child("status").setValue(0);
        // Adding on disconnect hook
        mDatabaseReference.child("USER").child(USER_KEY).child("userInfo").child("status").onDisconnect().setValue(1);
    }

    //make user offline
    private void makeUserOffline() {
        //change value in Realtime Database so that the cloud function will automatically change it in the realtime database
        mDatabaseReference.child("USER").child(USER_KEY).child("userInfo").child("status").setValue(1);
    }

    private void makeUserAway() {
        mDatabaseReference.child("USER").child(USER_KEY).child("userInfo").child("status").setValue(2);
    }

    //this might be useful when i will implement the search bar so that when u press on
    // a search result it go directly to the location of the user on the map
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            double latitude = data.getDoubleExtra("latitude", 0);
            double longitude = data.getDoubleExtra("longitude", 0);
            goTO(new LatLng(latitude, longitude));
        }

        if (requestCode == PLACE_INTENT_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                LatLng latLng = new LatLng(data.getDoubleExtra("latitude", 0), data.getDoubleExtra("longitude", 0));

                //Moving the camera
                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                //Animating the camera
                mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
                //update value on seekbar
                mVerticalSeekBar.setProgress(15);

                Toast.makeText(getApplicationContext(), "Result gotten from Place Activity", Toast.LENGTH_SHORT).show();
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
                Toast.makeText(getApplicationContext(), "No result gotten from Place Activity", Toast.LENGTH_SHORT).show();
            }
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                profileImage.setImageURI(result.getUri());
                mUserInfoObject.setImage_uri(result.getUri().toString());
                mModified = true;
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE)
                Toasty.error(getApplicationContext(), result.getError().toString(), Toast.LENGTH_SHORT, true).show();

        }
    }

    private TSnackbar mSnackBar;
    private List<Polyline> mPolyLines;
    private RoutingListener mRoutingListener = new RoutingListener() {
        @Override
        public void onRoutingFailure(RouteException e) {
            Toast.makeText(getApplicationContext(), "Routing Directions failed", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onRoutingStart() {
            Toast.makeText(getApplicationContext(), "Routing Started", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex) {
            //hide toolbar  to make space for the snakbar
            Objects.requireNonNull(getSupportActionBar(), "Action Bar Cannot Be Null").hide();

            if (mPolyLines.size() > 0)
                for (Polyline polyline : mPolyLines)
                    polyline.remove();

            mPolyLines = new ArrayList<>();
            //add route(s) to the map.
            for (int i = 0; i < route.size(); i++) {
                PolylineOptions mPolyLineOptions = new PolylineOptions();
                mPolyLineOptions.color(ContextCompat.getColor(getApplicationContext(), R.color.blue));
                mPolyLineOptions.width(10 + i * 3);
                mPolyLineOptions.addAll(route.get(i).getPoints());
                Polyline polyline = mMap.addPolyline(mPolyLineOptions);
                mPolyLines.add(polyline);
            }

            //mSnackBar to show distance and time to cover it
            mSnackBar = TSnackbar
                    .make(findViewById(R.id.mainWindow), String.format(Locale.getDefault(), "Distance -[ %d ] ~~ Duration -[ %d ]",
                            route.get(shortestRouteIndex).getDistanceValue(),
                            route.get(shortestRouteIndex).getDurationValue()), TSnackbar.LENGTH_INDEFINITE);
            mSnackBar.setIconRight(R.drawable.direction_btn, 24); //Size in dp - 24 is great!
            mSnackBar.setIconPadding(8);
            //mSnackBar.setMaxWidth(3000); //if you want full size on tablets
            View snackbarView = mSnackBar.getView();
            TextView textView = snackbarView.findViewById(com.androidadvance.topsnackbar.R.id.snackbar_text);
            textView.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
            snackbarView.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.black));
            mSnackBar.show();

            //make cancel route button visible only if route was created successfully
            mCancelRouteBtn.show();
            Toasty.success(getApplicationContext(), "Route successfully created", 1000, true).show();
        }

        @Override
        public void onRoutingCancelled() {
            Toast.makeText(getApplicationContext(), "Routing was cancelled", Toast.LENGTH_SHORT).show();
        }
    };

    //erase all Route lines from the map
    private void eraseAllRouteLines() {
        mSnackBar.dismiss();
        for (Polyline line : mPolyLines) {
            line.remove();
        }
        mPolyLines.clear();
    }

    private AbstractRouting.TravelMode mTravelMode;

    private void drawRoute(LatLng mMarkerClickLatLng) {
        //choose travel method before proceeding
        new AlertDialog.Builder(getApplicationContext())
                .setTitle("Pick A Travel Mode")
                .setItems(new CharSequence[]{"Driving", "Walking", "Transit"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                mTravelMode = AbstractRouting.TravelMode.DRIVING;
                                break;
                            case 1:
                                mTravelMode = AbstractRouting.TravelMode.WALKING;
                                break;
                            case 2:
                                mTravelMode = AbstractRouting.TravelMode.TRANSIT;
                                break;
                        }
                    }
                })
                .show();
        if (mTravelMode != null) {
            Routing routing = new Routing.Builder()
                    .travelMode(mTravelMode)
                    .withListener(mRoutingListener)
                    .alternativeRoutes(false)
                    .waypoints(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()), mMarkerClickLatLng)  // (start , end)
                    .build();
            routing.execute();
        } else
            Toast.makeText(getApplicationContext(), "You Select Travel Mode", Toast.LENGTH_SHORT).show();
    }

    //calling a user
    private void call(Marker mMarkerData) {

        User userInfo = mGson.fromJson(mMarkerData.getSnippet(), User.class);
        Call call = getSinchServiceInterface().callUser(userInfo.getPhone());
        String callId = call.getCallId();

        Intent callScreen = new Intent(getApplicationContext(), CallScreenActivity.class);
        callScreen.putExtra(SinchService.CALL_ID, callId);
        callScreen.putExtra(SinchService.CALL_NAME, userInfo.getName());
        callScreen.putExtra(SinchService.CALL_IMAGE, userInfo.getImage_uri());
        startActivity(callScreen);
    }


    @Override
    public void onStartFailed(SinchError error) {
        Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onStarted() {
        Toast.makeText(mGeoQueriesService, "service started...", Toast.LENGTH_SHORT).show();
    }

}
