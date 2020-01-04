package com.geraud.android.gps1.GoogleMap;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
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
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.ContextThemeWrapper;
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
import com.geraud.android.gps1.Chat.ChatActivity;
import com.geraud.android.gps1.Chat.ChatsActivity;
import com.geraud.android.gps1.Dailogs.AddPlace;
import com.geraud.android.gps1.InfoWindow.BranchMarkerInfoWindow;
import com.geraud.android.gps1.InfoWindow.PeopleMarkerInfoWindow;
import com.geraud.android.gps1.InfoWindow.PlacesMarkerInfoWindow;
import com.geraud.android.gps1.InfoWindow.TrackerInfoWindow;
import com.geraud.android.gps1.Models.Branch;
import com.geraud.android.gps1.Models.Chat;
import com.geraud.android.gps1.Models.ChatInfo;
import com.geraud.android.gps1.Models.GeoFence;
import com.geraud.android.gps1.Models.Message;
import com.geraud.android.gps1.Models.Place;
import com.geraud.android.gps1.Models.Subscription;
import com.geraud.android.gps1.Models.User;
import com.geraud.android.gps1.R;
import com.geraud.android.gps1.Registration;
import com.geraud.android.gps1.Services.GeoQueries;
import com.geraud.android.gps1.Services.SinchService;
import com.geraud.android.gps1.Sinch.BaseActivity;
import com.geraud.android.gps1.Sinch.CallScreenActivity;
import com.geraud.android.gps1.Stories.BodyFragment;
import com.geraud.android.gps1.Stories.HeaderFragment;
import com.geraud.android.gps1.User_Setup.Setup;
import com.geraud.android.gps1.Utils.Contacts;
import com.geraud.android.gps1.Utils.PlacesTypeToDrawable;
import com.geraud.android.gps1.Utils.RandomStringGenerator;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
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
import com.google.gson.Gson;
import com.onesignal.OneSignal;
import com.sinch.android.rtc.SinchError;
import com.sinch.android.rtc.calling.Call;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
    public static final String CONTACTS = "contacts_logi";
    public static final String USER_PHONE = "phone_logi";
    public static final String LATITUDE = "lllat";
    public static final String LONGITUDE = "lllong";

    private LinearLayout mBottomPeekLayout;
    private FusedLocationProviderClient mFusedLocationClient;

    private SeekBar mVerticalSeekBar;

    private Dialog mUserInfoDialog;
    private TextView dLocationView;
    private EditText dUserName;
    private ImageView dProfileImage;

    public DatabaseReference mDatabaseReference;
    private DatabaseReference mLocationReference;
    private DatabaseReference mSearchRef;
    private String USER_KEY;

    public HeaderFragment StoryHeaderFragment;
    private BodyFragment StoryBodyFragment;
    private GoogleMap mMap;

    private BottomSheetBehavior mBottomSheetBehavior;

    private Gson mGson = new Gson();

    // The callback for the management of the user settings regarding location
    public OnCompleteListener<LocationSettingsResponse> mResultCallbackFromSettings = new OnCompleteListener<LocationSettingsResponse>() {
        @Override
        public void onComplete(@NonNull Task<LocationSettingsResponse> task) {
            try {
                LocationSettingsResponse response = task.getResult(ApiException.class);
                Toast.makeText(getApplicationContext(), "Location Settings sucessfull", Toast.LENGTH_SHORT).show();
                // All location settings are satisfied. The client can initialize location
                // requests here.
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    requestPermission();
                }
                mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
            } catch (ApiException exception) {
                switch (exception.getStatusCode()) {
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied. But could be fixed by showing the
                        // user a dialog.
                        try {
                            // Cast to a resolvable exception.
                            ResolvableApiException resolvable = (ResolvableApiException) exception;
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            resolvable.startResolutionForResult(
                                    MapsActivity.this,
                                    REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        } catch (ClassCastException e) {
                            // Ignore, should be an impossible error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way to fix the
                        // settings so we won't show the dialog.
                        break;
                }
            }
        }
    };

    private GoogleApiClient.ConnectionCallbacks mGoogleApiClientConnectionCallbacks = new GoogleApiClient.ConnectionCallbacks() {
        @Override
        public void onConnected(@Nullable Bundle bundle) {
            Toast.makeText(getApplicationContext(), "Connected - google maps api", Toast.LENGTH_SHORT).show();
            findViewById(R.id.DatabaseConnectionStatus).setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.online));
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

    private Toolbar mToolBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        //for loading friends
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mContactList.size() > 0 && mMap != null)
                    LoadFriends();
            }
        });

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        USER_KEY = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser(), "user phone number is null").getPhoneNumber();

        // Find the toolbar view inside the activity layout
        mToolBar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolBar);
        Objects.requireNonNull(getSupportActionBar(), "Support Action Bar Can't Be Null").setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_status);

        //navigation drawer
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, mToolBar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        //toggle search button to toggle the type of item to search on the map
        final ToggleButton mToggleSearchBtn = mToolBar.findViewById(R.id.toggle_search);
        mToggleSearchBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    //change search to Users
                    mSearchRef = mDatabaseReference.child("USER").child("userInfo");
                    mToggleSearchBtn.setText("L");
                } else {
                    //change search to Places
                    mSearchRef = mDatabaseReference.child("PLACE").child(USER_KEY);
                    mToggleSearchBtn.setText("P");
                }
            }
        });

        //arguments(user phone and contact list) for fragment
        final Bundle bundle = new Bundle();
        bundle.putStringArrayList(CONTACTS, (ArrayList<String>) mContactList);
        bundle.putString(USER_PHONE, USER_KEY);
        if (mLastLocation != null) {
            bundle.putDouble(LATITUDE, mLastLocation.getLatitude());
            bundle.putDouble(LONGITUDE, mLastLocation.getLongitude());
        } else {
            mFusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        mLastLocation = location;
                        bundle.putDouble(LATITUDE, mLastLocation.getLatitude());
                        bundle.putDouble(LONGITUDE, mLastLocation.getLongitude());
                    } else
                        Toast.makeText(MapsActivity.this, "Couldn't get current location", Toast.LENGTH_SHORT).show();
                }
            });
        }

        //fragments
        StoryHeaderFragment = new HeaderFragment();
        StoryHeaderFragment.setArguments(bundle);
        StoryBodyFragment = new BodyFragment();
        StoryBodyFragment.setArguments(bundle);

        mDatabaseReference = FirebaseDatabase.getInstance().getReference();
        mStorageReference = FirebaseStorage.getInstance().getReference();
        mSearchRef = mDatabaseReference.child("PLACE"); //by default
        mLocationReference = mDatabaseReference.child("LOCATION");

        //floating action buttons
        FloatingActionButton getCurrentLocationBtn = findViewById(R.id.fab);
        mCancelRouteBtn = findViewById(R.id.cancelRoute);

        getCurrentLocationBtn.setOnClickListener(mFloatActionButtonClickListener);
        mCancelRouteBtn.setOnClickListener(mFloatActionButtonClickListener);

        //bottom sheet menu buttons
        mProfilePicture = findViewById(R.id.bottomSheet_Image);
        mProfileName = findViewById(R.id.bottomSheet_Name);

        LinearLayout chatBtn = findViewById(R.id.chat);
        LinearLayout placesBtn = findViewById(R.id.places);
        ImageView refresh = findViewById(R.id.refreshContent);

        refresh.setOnClickListener(mBottomViewClickListener);
        chatBtn.setOnClickListener(mBottomViewClickListener);
        placesBtn.setOnClickListener(mBottomViewClickListener);

        Switch shareLocation = findViewById(R.id.showLocation);
        Switch showGeofences = findViewById(R.id.showGeofences);
        shareLocation.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // The toggle is enabled
                    TextView textView = findViewById(R.id.showLocationTxt);
                    textView.setText(getString(R.string.share_location));
                    if (!checkPermission())
                        requestPermission();
                    mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                } else {
                    // The toggle is disabled
                    TextView textView = findViewById(R.id.showLocationTxt);
                    textView.setText(getString(R.string.hide_location));
                    if (mFusedLocationClient != null) {
                        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
                        //mGeoFire.removeLocation(USER_KEY, mGeofireCompletionListener);
                    }
                }
            }
        });
        showGeofences.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    TextView textView = findViewById(R.id.showGeofencesTxt);
                    textView.setText(getString(R.string.show_geofences));
                    mGeoBoundary.setVisible(true);
                } else {
                    TextView textView = findViewById(R.id.showGeofencesTxt);
                    textView.setText(getString(R.string.hide_geofences));
                    if (mGeoBoundary != null)
                        mGeoBoundary.setVisible(false);
                }
            }
        });

        mUserInfoDialog = new Dialog(mContextThemeWrapper);
        //settings for user profile info
        ImageButton profileSettings = findViewById(R.id.userProfileSettings);
        profileSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createUserInfoDialog();
            }
        });


        //listen constantly to changes in text to the editText
        EditText searchBar = findViewById(R.id.search_text);
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //do nothing if there is no text in the edit Text
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //constantly search on each key stroke
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
        View bottomSheet = findViewById(R.id.bottom_sheet1);
        mBottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HALF_EXPANDED);
        mBottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
         /*       if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
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
                }*/

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
                if (mMap != null)
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        if (mapFragment != null) {
            mapFragment.getMapAsync(mMapReadyCallback);
            bindService(new Intent(getApplicationContext(), GeoQueries.class), mServiceConnection, BIND_AUTO_CREATE);
            initialiseFragments();
        }

    }

    private void initialiseFragments() {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

        fragmentTransaction.add(R.id.head_frame, StoryHeaderFragment);
        fragmentTransaction.add(R.id.body_frame, StoryBodyFragment);

        fragmentTransaction.commit();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START))
            drawer.closeDrawer(GravityCompat.START, true);
        else {
            super.onBackPressed();
        }
        if (mBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED)
            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        else
            super.onBackPressed();

    }

    @Override
    protected void onStart() {
        super.onStart();
        //mGoogleApiClient.connect();
        loadContacts();
        UserDetails();
        makeUserOnline();
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

        if (mFusedLocationClient != null) {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        }
        super.onStop();
    }

    @Override
    public void onDestroy() {
        //i still want to listen to incoming calls and geo triggers even if the app is stopped
      /*  if (getSinchServiceInterface() != null) {
            getSinchServiceInterface().stopClient();
        }
        if (mServiceIsBound) {
            unbindService(mServiceConnection);
            mServiceIsBound = false;
        }*/
        super.onDestroy();
    }

    @Override
    protected void onServiceConnected() {
        getSinchServiceInterface().setStartListener(this);
    }

    private boolean mServiceIsBound;
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mServiceIsBound = true;
            mGeoQueriesService = ((GeoQueries.LocalBinder) service).getService();
            Toast.makeText(getApplicationContext(), "GeoQueries service connection passed", Toast.LENGTH_SHORT).show();
            loadGeoFences();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mGeoQueriesService = null;
            mServiceIsBound = false;
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
                    dUserName.setEnabled(true);
                    dUserName.setFocusable(true);
                    mModified = true;
                    break;
                case R.id.updateInfo:
                    if (mModified) {
                        if (!TextUtils.isEmpty(dUserName.getText().toString()))
                            mUserInfoObject.setName(dUserName.getText().toString());
                        //upload image first
                        UploadTask uploadTask = mStorageReference.child("Profile Pictures").child(USER_KEY).child(USER_KEY + ".jpg").putFile(Uri.parse(mUserInfoObject.getImage_uri()));
                        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                mUserInfoObject.setImage_uri(Objects.requireNonNull(taskSnapshot.toString(), "update Info downloadUri Cant Be Null").toString());
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
                case R.id.logOut:
                    Toast.makeText(getApplicationContext(), "Hold Button To LogOut", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    Toast.makeText(getApplicationContext(), "Invalid Click Id For Dialog", Toast.LENGTH_SHORT).show();
            }
        }
    };

    private boolean mModified = false;

    private void createUserInfoDialog() {
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        //change user name and image
        // will initialise the dialog here first work with the dialog variables
        mUserInfoDialog.setContentView(R.layout.user_settings);


        dLocationView = mUserInfoDialog.findViewById(R.id.locationView);
        dProfileImage = mUserInfoDialog.findViewById(R.id.imageView);
        dUserName = mUserInfoDialog.findViewById(R.id.nameEdittext);

        ImageButton changeImage = mUserInfoDialog.findViewById(R.id.changeImage);
        TextView closeText = mUserInfoDialog.findViewById(R.id.txtclose);
        Button updateInfo = mUserInfoDialog.findViewById(R.id.updateInfo);
        Button logOut = mUserInfoDialog.findViewById(R.id.logOut);
        ImageButton changeName = mUserInfoDialog.findViewById(R.id.changeName);

        //setting user info
        Glide.with(getApplicationContext()).load(mUserInfoObject.getImage_uri()).into(dProfileImage);
        dUserName.setText(mUserInfoObject.getName());
        mLocationReference.child(mUserInfoObject != null ? mUserInfoObject.getPhone() : null).child("l").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    List<Object> map = (List<Object>) dataSnapshot.getValue();
                    double locationLat = 0;
                    double locationLng = 0;
                    if (map.get(0) != null) {
                        locationLat = Double.parseDouble(map.get(0).toString());
                    }
                    if (map.get(1) != null) {
                        locationLng = Double.parseDouble(map.get(1).toString());
                    }
                    try {
                        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                        List<Address> address = geocoder.getFromLocation(locationLat, locationLng, 1);
                        dLocationView.setText(String.format(Locale.getDefault(), "Currently Located At : %s, %s %s", address.get(0).getFeatureName(), address.get(0).getLocality(), address.get(0).getCountryName()));
                    } catch (IOException e) {
                        dLocationView.setText("Couldn't Get Address Of Your Location");
                    }

                } else
                    Toast.makeText(getApplicationContext(), "Location is null", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        //onClick Actions
        closeText.setOnClickListener(mUserInfoDialogClickListener);
        changeImage.setOnClickListener(mUserInfoDialogClickListener);
        changeName.setOnClickListener(mUserInfoDialogClickListener);
        updateInfo.setOnClickListener(mUserInfoDialogClickListener);
        logOut.setOnClickListener(mUserInfoDialogClickListener);
        logOut.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                new AlertDialog.Builder(mContextThemeWrapper)
                        .setTitle("LogOut")
                        .setMessage("Are You Sure You Really Want To LogOut?")
                        .setCancelable(true)
                        .setNegativeButton("Logout", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                LogOut();
                            }
                        })
                        .setPositiveButton("Cancel", null)
                        .setIcon(R.drawable.logout)
                        .show();
                return true;
            }
        });

        Objects.requireNonNull(mUserInfoDialog.getWindow(), "UserInfoDialog Window Cant Be Null").setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mUserInfoDialog.show();
    }


    private long INTERVAL = TimeUnit.MINUTES.toMillis(1); // minutes
    private long FASTEST_INTERVAL = TimeUnit.SECONDS.toMillis(30);
    private LocationRequest mLocationRequest;
    //all related map actions take place here
    private OnMapReadyCallback mMapReadyCallback = new OnMapReadyCallback() {
        @Override
        public void onMapReady(GoogleMap googleMap) {
            mMap = googleMap;

            //adaptive map function
            runOnUiThread(new Runnable() {
                int timeOfTheDay = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);

                @Override
                public void run() {
                    if (timeOfTheDay >= 6 && timeOfTheDay < 17) //if its day time
                        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getApplicationContext(), R.raw.day_map));
                    else
                        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getApplicationContext(), R.raw.night_map));
                }
            });

            mLocationRequest = LocationRequest.create();
            mLocationRequest.setInterval(INTERVAL);
            mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

            //Setting onMarker/OnMap Listeners
            mMap.setOnMapLongClickListener(mOnMapLongCLickListener);
            mMap.setOnMapClickListener(mOnMapClickListener);
            mMap.setOnMarkerClickListener(mOnMarkerClickListener);
            mMap.setOnCircleClickListener(mOnCircleClickListener);


            if (checkPermission()) {
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().isRotateGesturesEnabled();
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                // Check the location settings of the user and create the callback to react to the different possibilities
                LocationSettingsRequest.Builder locationSettingsRequestBuilder = new LocationSettingsRequest.Builder()
                        .addLocationRequest(mLocationRequest);
                locationSettingsRequestBuilder.setAlwaysShow(true);
                Task<LocationSettingsResponse> result =
                        LocationServices.getSettingsClient(getApplicationContext()).checkLocationSettings(locationSettingsRequestBuilder.build());
                result.addOnCompleteListener(mResultCallbackFromSettings);
            } else {
                requestPermission();
            }

            getCurrentLocation();
            LoadPlaces(); //my places
            loadPlace(); //friends places
            loadSubscriptions();
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
                            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
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
        if (checkPermission())
            mContactList = new Contacts(getApplicationContext()).getAllContacts();
        else
            requestPermission();
    }

    public Location mLastLocation;
    private GeoFire mGeoFire;

    private LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            if (locationResult == null) {
                return;
            }
            for (Location location : locationResult.getLocations()) {
                if (location != null) {
                    Toast.makeText(MapsActivity.this, "Location Changed", Toast.LENGTH_SHORT).show();
                    mLastLocation = location;
                    //update location in real-time Database
                    mGeoFire.setLocation(USER_KEY, new GeoLocation(location.getLatitude(), location.getLongitude()), mGeofireCompletionListener);

                }
            }
        }
    };

    private GeoFire.CompletionListener mGeofireCompletionListener = new GeoFire.CompletionListener() {
        @Override
        public void onComplete(String key, DatabaseError error) {
            if (error != null)
                Toast.makeText(MapsActivity.this, "Error setting location in Geofire", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(MapsActivity.this, "location saved/removed sucessfully", Toast.LENGTH_SHORT).show();
        }
    };


    private boolean mToolBarState = true;
    private GoogleMap.OnMapClickListener mOnMapClickListener = new GoogleMap.OnMapClickListener() {
        @Override
        public void onMapClick(LatLng latLng) {
            //if bottomsheet layout is ioen close it
            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            //set toolbar visible / invisible on tap
            if (mToolBarState) {
                Objects.requireNonNull(getSupportActionBar(), "Support Action Bar Cant Be Null").hide();
                mVerticalSeekBar.setVisibility(View.GONE);
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
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContextThemeWrapper);
            alertDialogBuilder.setMessage("Do you want to Disable this GeoFence?")
                    .setNegativeButton("Disable", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //use co ordinates to delete it in firebase
                            double keyR = radius.getCenter().latitude + radius.getCenter().longitude;
                            mDatabaseReference.child("GEOFENCE").child(USER_KEY)
                                    .child(md5(Double.toString(keyR))).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        radius.setVisible(false);
                                        Toasty.success(getApplicationContext(), "Deleted GeoFence", Toast.LENGTH_SHORT).show();
                                    } else
                                        Toast.makeText(MapsActivity.this, "Error deleting geofence", Toast.LENGTH_SHORT).show();

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

            switch (marker.getTitle()) {
                case "user":
                    //set the custom info window adapter
                    mMap.setInfoWindowAdapter(new PeopleMarkerInfoWindow(getApplicationContext()));
                    mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                        @Override
                        public void onInfoWindowClick(Marker marker) {
                            //here i will show the various options to do on the maker
                            peopleMenu(marker);
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
                            placeMenu(marker);
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
                                    .setMessage("Stop Tracking " + userInfo.getName() + " ?")
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
                case "branch":
                    mMap.setInfoWindowAdapter(new BranchMarkerInfoWindow(getApplicationContext()));
                    mMap.setOnInfoWindowLongClickListener(new GoogleMap.OnInfoWindowLongClickListener() {
                        @Override
                        public void onInfoWindowLongClick(Marker marker) {
                            placeMenu(marker);
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
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                //if the chat already exists, open the chat in chat activity
                                Chat mChat = new Chat(dataSnapshot.getKey());
                                mChatList.add(mChat);
                                getChatData(mChat.getChatId());
                            } else {
                                //if it doesn't exist create a new chat then......
                                String key = mDatabaseReference.child("CHAT").push().getKey();
                                DatabaseReference userDB = mDatabaseReference.child("USER");
                                assert key != null;
                                DatabaseReference chatInfoDB = mDatabaseReference.child("CHAT").child(key).child("info");
                                Message message = new Message(null, "Open to start chatting", null, System.currentTimeMillis(), null);

                                mNewChatMap.put("id", key);
                                mNewChatMap.put("lastMessage/", message);
                                mNewChatMap.put("users/" + USER_KEY, true);
                                mNewChatMap.put("users/" + userInfo.getPhone(), true);
                                mNewChatMap.put("name", null);
                                mNewChatMap.put("type", "single");
                                mNewChatMap.put("image", null);

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
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Toast.makeText(getApplicationContext(), "ChatMenuBTN ValueEventListener Cancelled", Toast.LENGTH_SHORT).show();
                        }
                    });

                case R.id.gotoMenuBtn:
                    drawRoute(mMarkerClickLatLng);
                    break;
                case R.id.callMenuBtn:
                    call(mMarkerClickData);
                case R.id.trackMenuBtn:
                    TrackMarker(mMarkerClickData);

            }
            return false;
        }
    };

    private HashMap<String, Object> mNewChatMap = new HashMap<>();
    private List<Chat> mChatList = new ArrayList<>();
    private List<ChatInfo> mChatInfo = new ArrayList<>();

    private void getChatData(String chatId) {
        DatabaseReference mChatDB = FirebaseDatabase.getInstance().getReference().child("CHAT").child(chatId).child("info");
        mChatDB.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    ChatInfo chatInfo = dataSnapshot.getValue(ChatInfo.class);
                    mChatInfo.add(chatInfo);
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
                                getUserData(mUser);
                            }
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
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), "getChatData ValueEventListener Cancelled", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void getUserData(User mUser) {
        DatabaseReference mUserDb = FirebaseDatabase.getInstance().getReference().child("USER").child(mUser.getPhone());
        mUserDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
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
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), "getUserData ValueEventListener Cancelled", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void peopleMenu(Marker marker) {
        new AlertDialog.Builder(mContextThemeWrapper)
                .setItems(new CharSequence[]{"Chat", "GO-TO", "Call", "Track"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                startActivity(new Intent(MapsActivity.this,ChatsActivity.class));
                                break;
                            case 1:
                                drawRoute(mMarkerClickLatLng);
                                break;
                            case 2:
                                call(mMarkerClickData);
                                break;
                            case 3:
                                TrackMarker(mMarkerClickData);
                                break;
                        }
                    }
                })
                .show();
    }

    //alert dialog when pressing options on the info window of Places
    private void placeMenu(Marker marker) {
        new AlertDialog.Builder(mContextThemeWrapper)
                .setItems(new CharSequence[]{"GO-TO"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                drawRoute(mMarkerClickLatLng);
                                break;
                        }
                    }
                })
                .show();
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
        } else {
            mFusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        mLastLocation = location;
                        getCurrentLocation();
                    } else
                        Toast.makeText(MapsActivity.this, "Couldn't get current location", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    //goto method on the map
    public PolygonOptions mSearchPolygonOptions;
    private Polygon mSearchPolygon;

    private void goTO(LatLng latLng) {

        if (latLng == null) {
            Toast.makeText(this, "goto input is null", Toast.LENGTH_SHORT).show();
        }

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

    private View.OnClickListener mBottomViewClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.chat:
                    mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    startActivity(new Intent(getApplicationContext(), ChatsActivity.class));
                    break;
                case R.id.places:
                    mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    Intent i = new Intent(getApplicationContext(), PlacesActivity.class);
                    i.putStringArrayListExtra(PlacesActivity.CONTACT_LIST, (ArrayList<String>) mContactList);
                    startActivityForResult(i, PLACE_INTENT_REQUEST_CODE);
                    break;
                case R.id.refreshContent:
                    loadContacts();
                    loadPlace();
                    mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
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
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    mUserInfoObject = dataSnapshot.getValue(User.class);
                    if (mUserInfoObject != null) {
                        Glide.with(getApplicationContext()) //load profile picture
                                .load(mUserInfoObject.getImage_uri())
                                .into(mProfilePicture);
                        mProfileName.setText(mUserInfoObject.getName());
                    }
                } else startActivity(new Intent(getApplicationContext(), Setup.class));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
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

        Toast.makeText(this, "addPlaceOnMap", Toast.LENGTH_SHORT).show();
        //key of place
        final String key = mDatabaseReference.child("PLACE").child(USER_KEY).push().getKey();

        final StorageReference filePath = mStorageReference.child("Places Images").child(USER_KEY).child(RandomStringGenerator.randomString() + ".jpg");
        UploadTask uploadTask = filePath.putFile(Uri.parse(imageUri.toString()));
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Place place = new Place(key, name, USER_KEY, description, uri.toString(), "place", mClickLatLng.latitude, mClickLatLng.longitude, dec2);
                        //create a database entry for the new place
                        mDatabaseReference.child("PLACE").child(USER_KEY).push().setValue(place).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    mClickLatLng = null;
                                    Toasty.success(getApplicationContext(), "place created sucessfull", Toast.LENGTH_SHORT, true).show();
                                    LoadP
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
    private Marker mFriendPlace;
    private Marker mSubscriptions;

    private void LoadPlaces() {
//        if (mPlaces != null) mPlaces.remove();

        //load my places here before loading other peoples places
        mDatabaseReference.child("PLACE").child(USER_KEY).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
//                for (DataSnapshot dc : dataSnapshot.getChildren()) {
                Place myPlace = dataSnapshot.getValue(Place.class);
                //create JSON object to send the info to infoWindow adapter through the snippet
                Bitmap bitmap = scaleMarker(PlacesTypeToDrawable.getDrawable(myPlace != null ? myPlace.getDesc2() : null));
                String PlaceJSON = mGson.toJson(myPlace);
                mPlaces = mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(myPlace != null ? myPlace.getLatitude() : 0, myPlace != null ? myPlace.getLongitude() : 0))
                        .title(myPlace != null ? myPlace.getType() : "place")
                        .snippet(PlaceJSON)
                        .icon(BitmapDescriptorFactory.fromBitmap(bitmap))
                        .draggable(false)
                );
                //}
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
                Toast.makeText(getApplicationContext(), "Places ChildEventListener-1 Cancelled", Toast.LENGTH_SHORT).show();
            }
        });
        Toast.makeText(this, "MY-Place Loaded", Toast.LENGTH_SHORT).show();
    }

    private void loadSubscriptions() {
        mDatabaseReference.child("USER").child(USER_KEY).child("subscriptions").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                final Subscription subscription = dataSnapshot.getValue(Subscription.class);
                //get branch info
                mDatabaseReference.child("BRANCH").child(Objects.requireNonNull(subscription, "company ID is null").getCompanyId())
                        .child(subscription.getBranchId())
                        .child("info").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        final Branch branch = dataSnapshot.getValue(Branch.class);
                        final String BranchJSON = mGson.toJson(branch);
                        final Bitmap bitmap = scaleMarker(PlacesTypeToDrawable.getDrawable(branch.getType()));
                        //get location
                        mLocationReference.child(subscription.getBranchId()).child("l").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    List<Object> map = (List<Object>) dataSnapshot.getValue();
                                    double locationLat = 0;
                                    double locationLng = 0;
                                    if (map.get(0) != null) {
                                        locationLat = Double.parseDouble(map.get(0).toString());
                                    }
                                    if (map.get(1) != null) {
                                        locationLng = Double.parseDouble(map.get(1).toString());
                                    }
                                    //show location on map
                                    mSubscriptions = mMap.addMarker(new MarkerOptions()
                                            .position(new LatLng(locationLat, locationLng))
                                            .title("branch")
                                            .snippet(BranchJSON)
                                            .icon(BitmapDescriptorFactory.fromBitmap(bitmap))
                                            .draggable(false)
                                    );

                                } else
                                    Toast.makeText(getApplicationContext(), "Location is null for subscription", Toast.LENGTH_SHORT).show();

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(MapsActivity.this, "Branch ChildEventListener Cancelled", Toast.LENGTH_SHORT).show();
                    }
                });
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
                Toast.makeText(MapsActivity.this, "Subscriptions ChildEventListener Cancelled", Toast.LENGTH_SHORT).show();
            }
        });
        Toast.makeText(this, "Subscriptions loaded", Toast.LENGTH_SHORT).show();
    }

    private void loadPlace() {
        if (mFriendPlace != null) mFriendPlace.remove();
        //now load my mContactList places also
        //for each friend
        if (!mContactList.isEmpty())
            for (String contact : mContactList)
                mDatabaseReference.child("PLACE").child(contact).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists())
                            for (DataSnapshot dc : dataSnapshot.getChildren()) {
                                Place myPlace = dc.getValue(Place.class);
                                Bitmap bitmap = scaleMarker(PlacesTypeToDrawable.getDrawable(myPlace != null ? myPlace.getDesc2() : null));
                                //create JSON object to send the info to infoWindow adapter through the snippet
                                String PlaceJSON = mGson.toJson(myPlace);
                                mFriendPlace = mMap.addMarker(new MarkerOptions()
                                        .position(new LatLng(myPlace != null ? myPlace.getLatitude() : 0, myPlace != null ? myPlace.getLongitude() : 0))
                                        .title(myPlace != null ? myPlace.getType() : "place")
                                        .snippet(PlaceJSON)
                                        .icon(BitmapDescriptorFactory.fromBitmap(bitmap))
                                        .draggable(false)
                                );
                            }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(getApplicationContext(), "Friends Places ValueEventListener-2 Cancelled", Toast.LENGTH_SHORT).show();
                    }
                });
        Toast.makeText(this, "Friends Place Loaded", Toast.LENGTH_SHORT).show();
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
                        mFriendsMarker.remove(); //
                    for (final String contact : mContactList) {
                        //first query the users section of the database to get the Contact information
                        mDatabaseReference.child("USER").child(contact).child("userInfo").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    for (DataSnapshot dc : dataSnapshot.getChildren()) {
                                        final User user = dc.getValue(User.class);
                                        final String friendExtraInfo = mGson.toJson(user);
                                        //second query to get the location to finally display mContactList location
                                        mLocationReference.child(user != null ? user.getPhone() : null).child("l").addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                if (dataSnapshot.exists()) {
                                                    List<Object> map = (List<Object>) dataSnapshot.getValue();
                                                    double locationLat = 0;
                                                    double locationLng = 0;
                                                    if (map.get(0) != null) {
                                                        locationLat = Double.parseDouble(map.get(0).toString());
                                                    }
                                                    if (map.get(1) != null) {
                                                        locationLng = Double.parseDouble(map.get(1).toString());
                                                    }
                                                    mFriendsMarker = mMap.addMarker(new MarkerOptions()
                                                            .position(new LatLng(locationLat, locationLng))
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
                                            }
                                        });
                                    }
                                } else
                                    Toast.makeText(getApplicationContext(), contact + " - Doesn't exist", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Toast.makeText(getApplicationContext(), "Load ContactInfo ValueEventListener Cancelled", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            });
        }
    };

    private void LoadFriends() {
        Toast.makeText(getApplicationContext(), "loading friends location", Toast.LENGTH_SHORT).show();
        if (!mContactList.isEmpty()) {
            //start a timer to run after 1 second
            mFriendsTimer = new Timer();
            mFriendsTimer.schedule(mLoadFriendsTimerTask, TimeUnit.MINUTES.toMillis(2));

        } else
            Toast.makeText(getApplicationContext(), "No contacts found", Toast.LENGTH_SHORT).show();
    }

    private void StopLoadFriends() {
        if (mFriendsTimer != null) {
            mFriendsTimer.cancel();
            mFriendsTimer.purge();
        }
    }

    public Marker mTracker;
    private User mTrackedUser;
    private Timer mTrackerTimer;
    private Boolean isTracking = false;
    private TimerTask mTrackFriendTimerTask = new TimerTask() {
        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mLocationReference.child(mTrackedUser.getPhone()).child("l").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                List<Object> map = (List<Object>) dataSnapshot.getValue();
                                double locationLat = 0;
                                double locationLng = 0;
                                if (map.get(0) != null) {
                                    locationLat = Double.parseDouble(map.get(0).toString());
                                }
                                if (map.get(1) != null) {
                                    locationLng = Double.parseDouble(map.get(1).toString());
                                }
                                //update the marker position
                                mTracker.setPosition(new LatLng(locationLat, locationLng));
                                //move camera
                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mTracker.getPosition(), 15));
                                //update value on seek bar
                                mVerticalSeekBar.setProgress(15);

                            } else
                                Toast.makeText(getApplicationContext(), "Location is null for tracker", Toast.LENGTH_SHORT).show();

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });
                }
            });
        }
    };

    private void TrackMarker(Marker marker) {
        StopTracking();
        //pause friends location updates
        StopLoadFriends();
        //remove the original marker that wants to be tracked
        marker.setVisible(false);
        //put the old marker instance in a new one and update the location
        mTracker = marker;
        mTrackedUser = mGson.fromJson(mTracker.getSnippet(), User.class);
        mTracker.setTitle("track");
        mTracker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.tracker_map_icon));
        //set visibility of the new instance since the old object was removed from the map
        mTracker.setVisible(true);

        mTrackerTimer = new Timer();
        mTrackerTimer.schedule(mTrackFriendTimerTask, TimeUnit.MINUTES.toMillis(6));
        isTracking = true;
    }

    private void StopTracking() {
        if (isTracking) {
            //if there is an actively tracked icon
            mTrackerTimer.cancel();
            mTrackerTimer.purge();
            mTracker.remove();
            isTracking = false;

            //start loading friends again
            LoadFriends();
        }
    }

    //creating a geo coding boundary
    private void AddGeoBoundary() {
        //save geofence in Realtime Databse for Future use
        String key = mDatabaseReference.child("GEOFENCE").child(USER_KEY).push().getKey();
        GeoFence geoFence = new GeoFence(mClickLatLng.latitude, mClickLatLng.longitude, key);
        double keyR = geoFence.getLatitude() + geoFence.getLongitude();
        mDatabaseReference.child("GEOFENCE").child(USER_KEY).child(md5(Double.toString(keyR))).setValue(geoFence).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful())
                    Toasty.success(getApplicationContext(), "Geofence created successfully", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getApplicationContext(), "Couldn't create Geoboundary", Toast.LENGTH_SHORT).show();
            }
        });
        mClickLatLng = null;
    }

    public String md5(String s) {
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < messageDigest.length; i++)
                hexString.append(Integer.toHexString(0xFF & messageDigest[i]));
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    private int numGeofences = 0;
    private LatLng mGeoLocation;
    private GeoQueries mGeoQueriesService;
    private Circle mGeoBoundary;

    private void loadGeoFences() {
        mDatabaseReference.child("GEOFENCE").child(USER_KEY).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                numGeofences++;
                GeoFence geoFence = dataSnapshot.getValue(GeoFence.class);
                //save the location in the list of geoLocations to be used in a service for query
                LatLng latLng = new LatLng(geoFence != null ? geoFence.getLatitude() : 0, geoFence != null ? geoFence.getLongitude() : 0);
                mGeoLocation = latLng;
                mGeoBoundary = mMap.addCircle(new CircleOptions()
                        .center(latLng)
                        .radius(200) //meters
                        .strokeColor(ContextCompat.getColor(getApplicationContext(), R.color.light_sky_blue))
                        .fillColor(ContextCompat.getColor(getApplicationContext(), R.color.cornflower_blue))
                        .strokeWidth(4.0f)
                        .clickable(true)
                );
                Toast.makeText(MapsActivity.this, "num Geo-" + numGeofences, Toast.LENGTH_SHORT).show();
                if (mGeoQueriesService != null)
                    mGeoQueriesService.query(mGeoLocation);
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
                Toast.makeText(getApplicationContext(), "Geofences ValueEventListener Cancelled", Toast.LENGTH_SHORT).show();
            }

        });

//        //after all the geofences are loaded, start the queries listener service
//        mGeoQueriesService.query(mGeoLocationsList, mContactList);

//        mDatabaseReference.child("GEOFENCES").child(USER_KEY).addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                if (dataSnapshot.exists()) {
//                    if (mGeoBoundary != null)
//                        mGeoBoundary.remove();
//                    for (DataSnapshot dc : dataSnapshot.getChildren()) {
//                        GeoFence geoFence = dc.getValue(GeoFence.class);
//                        //save the location in the list of geoLoctions to be used in a service for query
//                        LatLng latLng = new LatLng(geoFence != null ? geoFence.getLatitude() : 0, geoFence != null ? geoFence.getLongitude() : 0);
//                        mGeoLocationsList.add(latLng);
//                        mGeoBoundary = mMap.addCircle(new CircleOptions()
//                                .center(latLng)
//                                .radius(600)
//                                .strokeColor(ContextCompat.getColor(getApplicationContext(), R.color.light_sky_blue))
//                                .fillColor(ContextCompat.getColor(getApplicationContext(), R.color.sky_blue))
//                                .strokeWidth(4.0f)
//                        );
//                        numGeofences++;
//                    }
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//                Toast.makeText(getApplicationContext(), "Geofences ValueEventListener Cancelled", Toast.LENGTH_SHORT).show();
//            }
//        });
        Toast.makeText(this, "Geofence Loaded", Toast.LENGTH_SHORT).show();
    }

    private ContextThemeWrapper mContextThemeWrapper = new ContextThemeWrapper(MapsActivity.this, R.style.alertDialog);
    private AlertDialog.Builder mCreatorWindow;

    private void creatorWindow() {

        mCreatorWindow = new AlertDialog.Builder(mContextThemeWrapper);
        LayoutInflater mLayoutInflater = this.getLayoutInflater();
        View dialogView = mLayoutInflater.inflate(R.layout.creator_window, null);
        mCreatorWindow.setView(dialogView);

        //disable geo fence option if user has reached limit
        if (numGeofences > 0)
            dialogView.findViewById(R.id.addGeo).setClickable(false);
        //if add place to map
        dialogView.findViewById(R.id.addPlaceToMap).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenAddPlace();
            }
        });
        //if add geo boundary
        dialogView.findViewById(R.id.addGeo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddGeoBoundary();
            }
        });

        mCreatorWindow.show();
    }

    //search method to search on the MAP
    private void Search(final String text) {
        //this method is case sensitive take note!!!
        mSearchRef.orderByChild("name")
                .startAt(text)
                .endAt(text + "\uf8ff")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.exists()) {

                            for (DataSnapshot dc : dataSnapshot.getChildren()) {

                                try {
                                    //if the data snapshot can enter the user object
                                    User user = dc.getValue(User.class);
                                    //getting location with/from geofire
                                    if (user != null)
                                        mLocationReference.child(user.getPhone()).child("l").addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                if (dataSnapshot.exists()) {
                                                    List<Object> map = (List<Object>) dataSnapshot.getValue();
                                                    double locationLat = 0;
                                                    double locationLng = 0;
                                                    if (map.get(0) != null) {
                                                        locationLat = Double.parseDouble(map.get(0).toString());
                                                    }
                                                    if (map.get(1) != null) {
                                                        locationLng = Double.parseDouble(map.get(1).toString());
                                                    }
                                                    goTO(new LatLng(locationLat, locationLng));

                                                } else
                                                    Toast.makeText(getApplicationContext(), "Location is null for search", Toast.LENGTH_SHORT).show();

                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {
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
                            Toast.makeText(getApplicationContext(), text + " - doesn't exist", Toast.LENGTH_SHORT).show();

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    //make user online
    private void makeUserOnline() {
        //update Realtime Database status value
        mDatabaseReference.child("USER").child(USER_KEY).child("userInfo").child("status").setValue(0).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                findViewById(R.id.DatabaseConnectionStatus).setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.online));
                Toast.makeText(MapsActivity.this, "User is online", Toast.LENGTH_SHORT).show();
            }
        });

//        // Adding on disconnect hook
//        mDatabaseReference.child("USER").child(USER_KEY).child("userInfo").child("status").onDisconnect().setValue(1).addOnSuccessListener(new OnSuccessListener<Void>() {
//            @Override
//            public void onSuccess(Void aVoid) {
//                findViewById(R.id.DatabaseConnectionStatus).setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.offline));
//                Toast.makeText(MapsActivity.this, "User is offline", Toast.LENGTH_SHORT).show();
//            }
//        });
    }

    //make user offline
    private void makeUserOffline() {
        //change value in Realtime Database so that the cloud function will automatically change it in the realtime database
        mDatabaseReference.child("USER").child(USER_KEY).child("userInfo").child("status").setValue(1);
    }

    private void makeUserAway() {
        mDatabaseReference.child("USER").child(USER_KEY).child("userInfo").child("status").setValue(2);
    }

    private void LogOut() {
        OneSignal.setSubscription(false);
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(getApplicationContext(), Registration.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    //this might be useful when i will implement the search bar so that when u press on
    // a search result it go directly to the location of the user on the map
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

//        if (resultCode == Activity.RESULT_OK) {
//            double latitude = data.getDoubleExtra("latitude", 0);
//            double longitude = data.getDoubleExtra("longitude", 0);
//            if (latitude != 0 && longitude != 0)
//                goTO(new LatLng(latitude, longitude));
//        }

        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == Activity.RESULT_OK) {
                // All required changes were successfully made
                mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                Toast.makeText(getApplicationContext(), "Thanks for Enabling Location!", Toast.LENGTH_SHORT).show();
            } else {
                // The user was asked to change settings, but chose not to
                Toast.makeText(getApplicationContext(), "Keep in mind your contacts wont be able to get location updates", Toast.LENGTH_SHORT).show();
            }
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
            } else if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
                Toast.makeText(getApplicationContext(), "No result gotten from Place Activity", Toast.LENGTH_SHORT).show();
            }
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                dProfileImage.setImageURI(result.getUri());
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
            Toast.makeText(getApplicationContext(), "Routing failed = " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        @Override
        public void onRoutingStart() {
            Toast.makeText(getApplicationContext(), "Routing Started", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex) {
            //hide toolbar  to make space for the snackbar
            Objects.requireNonNull(getSupportActionBar(), "Action Bar Cannot Be Null").hide();
            mToolBar.setVisibility(View.GONE);

            if (mPolyLines.size() > 0)
                for (Polyline polyline : mPolyLines)
                    polyline.remove();

            mPolyLines = new ArrayList<>();
            //add route(s) to the map.
            for (int i = 0; i < route.size(); i++) {
                PolylineOptions polyLineOptions = new PolylineOptions();
                polyLineOptions.color(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimaryDark));
                polyLineOptions.width(10 + i * 3);
                polyLineOptions.addAll(route.get(i).getPoints());
                Polyline polyline = mMap.addPolyline(polyLineOptions);
                mPolyLines.add(polyline);
            }

            //mSnackBar to show distance and time to cover distance
            mSnackBar = TSnackbar
                    .make(findViewById(R.id.mainWindow), String.format(Locale.getDefault(), "Distance -[ %d Meters] ~~ Duration -[ %d Minutes]",
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
            mToolBar.setVisibility(View.VISIBLE);
            //show tool bar
            Objects.requireNonNull(getSupportActionBar(), "Action Bar Cannot Be Null").show();
        }
    };

    //erase all Route lines from the map
    private void eraseAllRouteLines() {
        mToolBar.setVisibility(View.VISIBLE);
        mCancelRouteBtn.setVisibility(View.GONE);
        if (mSnackBar != null)
            mSnackBar.dismiss();
        for (Polyline line : mPolyLines) {
            line.remove();
        }
        mPolyLines.clear();
    }

    private AbstractRouting.TravelMode mTravelMode;

    private void drawRoute(final LatLng mMarkerClickLatLng) {
        //choose travel method before proceeding
        new AlertDialog.Builder(mContextThemeWrapper)
                .setTitle("Pick A Travel Mode")
                .setItems(new CharSequence[]{"Driving", "Walking", "Transit"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                mTravelMode = AbstractRouting.TravelMode.DRIVING;
                                draw(mMarkerClickLatLng);
                                break;
                            case 1:
                                mTravelMode = AbstractRouting.TravelMode.WALKING;
                                draw(mMarkerClickLatLng);
                                break;
                            case 2:
                                mTravelMode = AbstractRouting.TravelMode.TRANSIT;
                                draw(mMarkerClickLatLng);
                                break;
                        }
                    }
                })
                .show();
    }

    private void draw(LatLng mMarkerClickLatLng) {
        if (mTravelMode != null) {
            Routing routing = new Routing.Builder()
                    .travelMode(mTravelMode)
                    .withListener(mRoutingListener)
                    .alternativeRoutes(false)
                    .waypoints(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()), mMarkerClickLatLng)  // (start , end)
                    .key("AIzaSyA2ypCoAoIG_pqEFPzorAobHbuw3C0_iPk")
                    .build();
            routing.execute();
        } else
            Toasty.error(getApplicationContext(), "Select Travel Mode", Toast.LENGTH_SHORT, true).show();
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
        //start the sinch client/ initialise using my phone number
        if (!getSinchServiceInterface().isStarted()) {
            getSinchServiceInterface().startClient(USER_KEY);
        }
    }

    private Bitmap scaleMarker(int drawable) {
        BitmapDrawable bitmapdraw = (BitmapDrawable) ContextCompat.getDrawable(MapsActivity.this, drawable);
        Bitmap b = Objects.requireNonNull(bitmapdraw, "Bitmap cannot be null").getBitmap();
        return Bitmap.createScaledBitmap(b, 64, 64, false);
    }

}
