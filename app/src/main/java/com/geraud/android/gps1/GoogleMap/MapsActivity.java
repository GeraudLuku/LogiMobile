package com.geraud.android.gps1.GoogleMap;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
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
import com.geraud.android.gps1.Dailogs.AddPlace;
import com.geraud.android.gps1.InfoWindow.PeopleMarkerInfoWindow;
import com.geraud.android.gps1.InfoWindow.PlacesMarkerInfoWindow;
import com.geraud.android.gps1.InfoWindow.TrackerInfoWindow;
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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.sinch.android.rtc.SinchError;
import com.sinch.android.rtc.calling.Call;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import es.dmoral.toasty.Toasty;

public class MapsActivity extends BaseActivity implements
        SinchService.StartFailedListener,
        AddPlace.AddPlaceListener {


    private static final int REQUEST_PERMISSION_CODE = 1;
    private static final int READ_CONTACT_PERMISSION_REQUEST = 12;
    private static final int REQUEST_CHECK_SETTINGS = 123;
    private static final String POPUP_CONSTANT = "mPopup";
    private static final String POPUP_FORCE_SHOW_ICON = "setForceShowIcon";


    LinearLayout mBottomPeekLayout;
    View mBottomSheet;
    private EditText mSearchBar;

    //for creating random colors for geofences
    private SeekBar mVerticalSeekBar;

    private FirebaseAuth mFirebaseAuth;
    private DatabaseReference mDatabaseReference;
    private DatabaseReference mSearchRef;
    private String USER_KEY;

    private Switch mShowGeofences, mShareLocation;

    private HeaderFragment StoryHeaderFragment;
    private BodyFragment StoryBodyFragment;
    private GoogleMap mMap;

    private String TAG = "gps";
    private BottomSheetBehavior mBottomSheetBehavior;

    private Gson mGson = new Gson();

    // The callback for the management of the user settings regarding location
    private ResultCallback<LocationSettingsResult> mResultCallbackFromSettings = new ResultCallback<LocationSettingsResult>() {
        @Override
        public void onResult(LocationSettingsResult result) {
            final Status status = result.getStatus();
            //final LocationSettingsStates locationSettingsStates = result.getLocationSettingsStates();
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
        mSearchRef = FirebaseDatabase.getInstance().getReference().child("PLACES"); //by default
        mFirebaseAuth = FirebaseAuth.getInstance();
        USER_KEY = mFirebaseAuth.getCurrentUser().getPhoneNumber();


        //floating action buttons
        mGetCurrentLocationBtn = findViewById(R.id.fab);
        mCancelRouteBtn = findViewById(R.id.cancelRoute);

        mGetCurrentLocationBtn.setOnClickListener(mFloatActionButtonClickListener);
        mCancelRouteBtn.setOnClickListener(mFloatActionButtonClickListener);

        //bottom sheet menu buttons
        mChatBtn = findViewById(R.id.chat);
        mPlacesBtn = findViewById(R.id.places);
        mShareLocation = findViewById(R.id.showLocation);
        mShowGeofences = findViewById(R.id.showGeofences);
        mProfilePicture = findViewById(R.id.bottomSheet_Image);
        mProfileName = findViewById(R.id.bottomSheet_Name);

        mChatBtn.setOnClickListener(mBottomViewClickListener);
        mPlacesBtn.setOnClickListener(mBottomViewClickListener);
        mShareLocation.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // The toggle is enabled
                    TextView textView = findViewById(R.id.showLocationTxt);
                    textView.setText("Hide Location");
                    if (!checkPermission())
                        requestPermission();
                    mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, INTERVAL, 1, mLocationListener);
                } else {
                    // The toggle is disabled
                    TextView textView = findViewById(R.id.showLocationTxt);
                    textView.setText("Share Location");
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
                    textView.setText("Hide Geofences");
                    mGeoBoundary.setVisible(true);
                } else {
                    TextView textView = findViewById(R.id.showGeofencesTxt);
                    textView.setText("Show Geofences");
                    mGeoBoundary.setVisible(false);
                }
            }
        });


        mSearchBar = findViewById(R.id.search_text);
        mGeoFire = new GeoFire(FirebaseDatabase.getInstance().getReference("LOCATION"));

        mBottomPeekLayout = findViewById(R.id.tap_action_layout);
        mBottomPeekLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });
        mBottomSheet = findViewById(R.id.bottom_sheet1);
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
                //dont know this
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        // Find the toolbar view inside the activity layout
        Toolbar toolbar = findViewById(R.id.toolbar);
        // Sets the Toolbar to act as the ActionBar for this Activity window.
        // Make sure the toolbar exists in the activity and is not null
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
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
                    mSearchRef = FirebaseDatabase.getInstance().getReference().child("USER");
                    mToggleSearchBtn.setText("L");
                } else {
                    //change search to Places
                    mSearchRef = FirebaseDatabase.getInstance().getReference().child("PLACES");
                    mToggleSearchBtn.setText("P");
                }
            }
        });

        //listen constantly to changes in text to the editText
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

        mapFragment.getMapAsync(mMapReadyCallback);
        initialiseFragment();
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
        if (mLocationManager != null)
            mLocationManager.removeUpdates(mLocationListener);
        super.onDestroy();
    }

    private boolean mServiceIsBound = false;
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mGeoQueriesService = ((GeoQueries.LocalBinder) service).getService();
            Toast.makeText(getApplicationContext(), "geoqueries service connection passed", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mGeoQueriesService = null;
            Toast.makeText(getApplicationContext(), "Geoqueries service connection failed", Toast.LENGTH_SHORT).show();
        }
    };


    private int INTERVAL = 1000;
    private int FASTEST_INTERVAL = 500;
    private LocationManager mLocationManager;
    private LocationRequest mLocationRequest;
    //all related map actions take place here
    private OnMapReadyCallback mMapReadyCallback = new OnMapReadyCallback() {
        @Override
        public void onMapReady(GoogleMap googleMap) {
            mMap = googleMap;

            mLocationRequest = LocationRequest.create();
            mLocationRequest.setInterval(INTERVAL);
            mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

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
                        Manifest.permission.ACCESS_COARSE_LOCATION
                }, REQUEST_PERMISSION_CODE);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {

            case REQUEST_PERMISSION_CODE:

                if (grantResults.length > 0) {

                    boolean finelocation = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean coarselocation = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if (finelocation && coarselocation) {
                        if (checkPermission()) {
                            buildGoogleApiClient();
                            mMap.setMyLocationEnabled(true);
                        }
                        Toast.makeText(MapsActivity.this, "Permission Granted", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(MapsActivity.this, "Permission Denied", Toast.LENGTH_LONG).show();

                    }
                }
                break;
            case READ_CONTACT_PERMISSION_REQUEST:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mContactList = new Contacts(getApplicationContext()).getAllContacts();
                    bindService(new Intent(getApplicationContext(), GeoQueries.class), mServiceConnection, BIND_AUTO_CREATE);
                    mServiceIsBound = true;
                } else
                    Toast.makeText(getApplicationContext(), "App requires contact to work", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    public boolean checkPermission() {

        int FirstPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION);
        int SecondPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION);

        return FirstPermissionResult == PackageManager.PERMISSION_GRANTED &&
                SecondPermissionResult == PackageManager.PERMISSION_GRANTED;

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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                mContactList = new Contacts(getApplicationContext()).getAllContacts();
                bindService(new Intent(getApplicationContext(), GeoQueries.class), mServiceConnection, BIND_AUTO_CREATE);
                mServiceIsBound = true;
            } else {
                requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, READ_CONTACT_PERMISSION_REQUEST);
            }
        }

    }

    private Location mLastLocation;
    private GeoFire mGeoFire;
    private LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            mLastLocation = location;
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

//            //update marker on screen
//            if (mUserMarker != null) mUserMarker.remove();
//
//            mUserMarker = mMap.addMarker(new MarkerOptions()
//                    .position(latLng) //setting position
//                    .draggable(false) //Making the marker non draggable
//                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.rsz_personal_avatar)) //icon image here i am going to put the users bitmojii
//                    .title("Me")); //Adding my name as title
//
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(15));

            //update location in real-time Database
            mGeoFire.setLocation(USER_KEY, new GeoLocation(location.getLatitude(), location.getLongitude()));
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    private boolean mToolBarState = true;
    private GoogleMap.OnMapClickListener mOnMapClickListener = new GoogleMap.OnMapClickListener() {
        @Override
        public void onMapClick(LatLng latLng) {
            //set toolbar visible / invisible on tap
            if (mToolBarState) {
                getSupportActionBar().hide();
                mVerticalSeekBar.setVisibility(View.INVISIBLE);
                mToolBarState = false;
            } else {
                getSupportActionBar().show();
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
            //open dailog for options
            creatorWindow();
        }
    };


    private GoogleMap.OnCircleClickListener mOnCircleClickListener = new GoogleMap.OnCircleClickListener() {
        @Override
        public void onCircleClick(Circle circle) {
            final Circle radius = circle;
            //if circle is clicked you can make the user choose to delete it or not.
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getApplicationContext());
            alertDialogBuilder.setMessage("Do you want to Delete this GeoFence ?")
                    .setNegativeButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //use co ordinates to delete it in firebase
                            mDatabaseReference.child("GEOFENCES").child(USER_KEY)
                                    .child(String.valueOf(radius.getCenter().latitude + radius.getCenter().longitude))
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            dataSnapshot.getRef().removeValue();
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });
                        }
                    })
                    .setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
        }
    };


    private LatLng mMarkerClickLatLng;
    private Marker mMarkerData;
    private GoogleMap.OnMarkerClickListener mOnMarkerClickListener = new GoogleMap.OnMarkerClickListener() {
        @Override
        public boolean onMarkerClick(Marker marker) {
            //markers latitude and longitude
            mMarkerClickLatLng = marker.getPosition();
            //get marker GSON object from the snippet
            mMarkerData = marker;
            // store the marker also inside the tracking marker
            mTracker = marker;

            if (marker.getTitle().equals("user")) {
                //set the custom info window adapter
                mMap.setInfoWindowAdapter(new PeopleMarkerInfoWindow(MapsActivity.this));
                mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                    @Override
                    public void onInfoWindowClick(Marker marker) {
                        //here i will show the various options to do on the maker
                        peopleMenu(marker);
                    }
                });
            } else if (marker.getTitle().equals("place")) {
                //set the custom info window adapter
                mMap.setInfoWindowAdapter(new PlacesMarkerInfoWindow(MapsActivity.this));
                mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                    @Override
                    public void onInfoWindowClick(Marker marker) {
                        //here i will show the various options to do on the maker
                        placeMenu(marker);
                    }
                });
            } else if (marker.getTitle().equals("track")) {
                mMap.setInfoWindowAdapter(new TrackerInfoWindow(getApplicationContext()));
                mMap.setOnInfoWindowLongClickListener(new GoogleMap.OnInfoWindowLongClickListener() {
                    @Override
                    public void onInfoWindowLongClick(Marker marker) {
                        //close the tracking
                        StopTracking();
                    }
                });
            }
            //else if its my marker (my location) so as not to cause a NUllPointer Exception ;)
            else
                Toast.makeText(getApplicationContext(), "My Marker", Toast.LENGTH_SHORT).show();

            return false;
        }
    };

    private PopupMenu.OnMenuItemClickListener mMenuItemClickListener = new PopupMenu.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.chatMenuBtn:
                    //startActivity(new Intent(getApplicationContext(), ChatUsersActivity.class));
                case R.id.gotoMenuBtn:
                    drawRoute(mMarkerClickLatLng);
                    break;
                case R.id.callMenuBtn:
                    call(mMarkerData);
                case R.id.trackMenuBtn:
                    TrackMarker(mTracker);

            }
            return false;
        }
    };

    private void peopleMenu(Marker marker) {
        //i will rather make a pop up menu it will be better for this
        PopupMenu popup = new PopupMenu(getApplicationContext(), getCurrentFocus());
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

    //alert dailog when pressing options on the info window of Places
    private void placeMenu(Marker marker) {
        PopupMenu popup = new PopupMenu(getApplicationContext(), getCurrentFocus());
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
    private void goTO(LatLng latLng) {

        if (latLng != null) {
            //Moving the camera
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            //Animating the camera
            mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
            //update value on seekbar
            mVerticalSeekBar.setProgress(15);
        }
    }

    private LinearLayout mChatBtn, mPlacesBtn;
    private View.OnClickListener mBottomViewClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.chat:
                    break;
                case R.id.places:
                    break;
            }
        }
    };

    private FloatingActionButton mGetCurrentLocationBtn;
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

    public void UserDetails() {
        mDatabaseReference.child("USER").child(USER_KEY).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot doc : dataSnapshot.getChildren()) {
                        User user = doc.getValue(User.class);
                        Glide.with(getApplicationContext()) //load profile picture
                                .load(user.getImage_uri())
                                .into(mProfilePicture);
                        mProfileName.setText(user.getName());
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

        StorageReference filePath = mStorageReference.child("Places Images").child(USER_KEY).child(RandomStringGenerator.randomString() + ".jpg");
        filePath.putFile(imageUri).addOnCompleteListener(this, new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()) {
                    String downloadURI = task.getResult().getDownloadUrl().toString();
                    Place place = new Place(name, USER_KEY, description, downloadURI, "place", mClickLatLng.latitude, mClickLatLng.longitude, dec2);

                    //create a database entry for the new place
                    mDatabaseReference.child("PLACES").child(USER_KEY).push().setValue(place).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                //if the place ws added succefully you want to reload the places on the map
                                LoadPlaces();
                            } else
                                Toast.makeText(getApplicationContext(), "couldn't create place", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else
                    Toast.makeText(getApplicationContext(), "Image couldn't be uploaded successfully", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private Marker mPlaces;

    private void LoadPlaces() {
        if (mPlaces != null) mPlaces.remove();

        //load my places here before loading other peoples places
        mDatabaseReference.child("PLACES").child(USER_KEY).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    for (DataSnapshot dc : dataSnapshot.getChildren()) {
                        Place myPlace = dc.getValue(Place.class);
                        //create JSON object to send the info to infoWindow adapter through the snippet
                        String PlaceJSON = mGson.toJson(myPlace);
                        mPlaces = mMap.addMarker(new MarkerOptions()
                                .position(new LatLng(myPlace.getLatitude(), myPlace.getLongitude()))
                                .title(myPlace.getType())
                                .snippet(PlaceJSON)
                                .icon(BitmapDescriptorFactory.fromResource(PlacesTypeToDrawable.getDrawable(myPlace.getDesc2())))
                                .draggable(false)
                        );
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //now load my mContactList places also
        //for each friend
        for (String contact : mContactList) {
            //load all places
            mDatabaseReference.child("PLACES").child(contact).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot dc : dataSnapshot.getChildren()) {
                            Place myPlace = dc.getValue(Place.class);
                            //create JSON object to send the info to infoWindow adapter through the snippet
                            String PlaceExtraInfo = mGson.toJson(myPlace);
                            mPlaces = mMap.addMarker(new MarkerOptions()
                                    .position(new LatLng(myPlace.getLatitude(), myPlace.getLongitude())) //convert the lat and long to double
                                    .title(myPlace.getType())
                                    .snippet(PlaceExtraInfo)
                                    .icon(BitmapDescriptorFactory.fromResource(PlacesTypeToDrawable.getDrawable(myPlace.getDesc2())))
                                    .draggable(false)
                            );
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }
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
                        mDatabaseReference.child("USER").child(contact).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    for (DataSnapshot dc : dataSnapshot.getChildren()) {
                                        final User user = dc.getValue(User.class);
                                        final String friendExtraInfo = mGson.toJson(user);
                                        //second query to get the location to finally display mContactList location
                                        mGeoFire.getLocation(user.getPhone(), new LocationCallback() {
                                            @Override
                                            public void onLocationResult(String key, GeoLocation location) {
                                                if (location != null) {
                                                    mFriendsMarker = mMap.addMarker(new MarkerOptions()
                                                            .position(new LatLng(location.latitude, location.longitude))
                                                            .title(user.getType())
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
                                    Toast.makeText(getApplicationContext(), contact + " Doesnt exist", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

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
            mFriendsTimer.schedule(mLoadFriendsTimerTask, TimeUnit.MINUTES.toMillis(30));

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
                    User user = mGson.fromJson(mTracker.getSnippet(), User.class);
                    mGeoFire.getLocation(user.getPhone(), new LocationCallback() {
                        @Override
                        public void onLocationResult(String key, GeoLocation location) {
                            if (location != null) {
                                mTracker = mMap.addMarker(new MarkerOptions()
                                        .position(new LatLng(location.latitude, location.longitude))
                                        .title("track")
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.tracker_map_icon))
                                        .draggable(false)
                                );
                            }
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
        mTracker = marker;
        mTrackerTimer = new Timer();
        mTrackerTimer.schedule(mTrackFriendTimerTask, TimeUnit.SECONDS.toMillis(1));
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
                    Toasty.success(getApplicationContext(), "Geofence created successfully", 2000).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Couldnt create geoboundary", Toast.LENGTH_SHORT).show();
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
                        LatLng latLng = new LatLng(geoFence.getLatitude(), geoFence.getLongitude());
                        mGeoLocationsList.add(latLng);
                        mGeoBoundary = mMap.addCircle(new CircleOptions()
                                .center(latLng)
                                .radius(600)
                                .strokeColor(getResources().getColor(R.color.light_sky_blue))
                                .fillColor(getResources().getColor(R.color.sky_blue))
                                .strokeWidth(4.0f)
                        );
                    }
                    numGeofences++;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        //after all the geofences are loaded, start the queries listener service
        mGeoQueriesService.query(this, mGeoLocationsList, mContactList);
    }


    private void creatorWindow() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater mLayoutInflater = this.getLayoutInflater();
        View mDailogView = mLayoutInflater.inflate(R.layout.creator_window, null);
        builder.setView(mDailogView);

        //if add place to map
        mDailogView.findViewById(R.id.addPlaceToMap).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenAddPlace();
            }
        });
        //if add geo boundary
        mDailogView.findViewById(R.id.addGeo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (numGeofences < 5) {
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
                                    //getting location with geofire
                                    mGeoFire.getLocation(user.getPhone(), new LocationCallback() {
                                        @Override
                                        public void onLocationResult(String key, GeoLocation location) {
                                            if (location != null) {
                                                goTO(new LatLng(location.latitude, location.longitude));
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });

                                } catch (Exception e) {
                                    //else insert it into the place object
                                    Place place = dc.getValue(Place.class);
                                    goTO(new LatLng(place.getLatitude(), place.getLongitude()));
                                }

                            }

                        } else
                            Toast.makeText(getApplicationContext(), text + " doesnt exist", Toast.LENGTH_SHORT).show();

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    //make user online
    private void makeUserOnline() {
        //update Realtime Database status value
        mDatabaseReference.child("USER").child(USER_KEY).child("status").setValue(0);
        // Adding on disconnect hook
        mDatabaseReference.child("USER").child(USER_KEY).child("status").onDisconnect().setValue(1);
    }

    //make user offline
    private void makeUserOffline() {
        //change value in Realtime Database so that the cloud function will automatically change it in the realtime database
        mDatabaseReference.child("USER").child(USER_KEY).child("status").setValue(1);
    }

    private void makeUserAway() {
        mDatabaseReference.child("USER").child(USER_KEY).child("status").setValue(2);
    }

    //this might be useful when i will implement the search bar so that when u press on
    // a search result it go directly to the location of the user on the map
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {

                double latitude = data.getDoubleExtra("latitude", 0);
                double longitude = data.getDoubleExtra("longitude", 0);

                if (latitude == 0 && longitude == 0) {
                    Toasty.info(this, "No location gotten from the activity", 3000).show();
                } else {
                    //move to that location on the map
                    LatLng latLng = new LatLng(latitude, longitude);
                    //Moving the camera
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

                    //Animating the camera
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
                }

            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
                Toast.makeText(getApplicationContext(), "No result gotten from intent", Toast.LENGTH_SHORT).show();
            }
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
            getSupportActionBar().hide();

            if (mPolyLines.size() > 0)
                for (Polyline polyline : mPolyLines)
                    polyline.remove();

            mPolyLines = new ArrayList<>();
            //add route(s) to the map.
            for (int i = 0; i < route.size(); i++) {
                PolylineOptions mPolyLineOptions = new PolylineOptions();
                mPolyLineOptions.color(getResources().getColor(R.color.light_blue));
                mPolyLineOptions.width(10 + i * 3);
                mPolyLineOptions.addAll(route.get(i).getPoints());
                Polyline polyline = mMap.addPolyline(mPolyLineOptions);
                mPolyLines.add(polyline);
            }

            //mSnackBar to show distance and time to cover it
            mSnackBar = TSnackbar
                    .make(findViewById(R.id.mainWindow), String.format("Distance - [ %d ] ~~ Duration - [ %d ]",
                            route.get(shortestRouteIndex).getDistanceValue(),
                            route.get(shortestRouteIndex).getDurationValue()), TSnackbar.LENGTH_INDEFINITE);
            mSnackBar.setIconRight(R.drawable.direction_btn, 24); //Size in dp - 24 is great!
            mSnackBar.setIconPadding(8);
            //mSnackBar.setMaxWidth(3000); //if you want fullsize on tablets
            View snackbarView = mSnackBar.getView();
            TextView textView = snackbarView.findViewById(com.androidadvance.topsnackbar.R.id.snackbar_text);
            textView.setTextColor(getResources().getColor(R.color.white));
            snackbarView.setBackgroundColor(getResources().getColor(R.color._light_green));
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

    private void drawRoute(LatLng mMarkerClickLatLng) {
        Routing routing = new Routing.Builder()
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(mRoutingListener)
                .alternativeRoutes(false)
                .waypoints(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()), mMarkerClickLatLng)  // (start , end)
                .build();
        routing.execute();
    }

    //calling a user
    private void call(Marker mMarkerData) {

        User userInfo = mGson.fromJson(mMarkerData.getSnippet(), User.class);
        Call call = getSinchServiceInterface().callUser(userInfo.getPhone());
        String callId = call.getCallId();

        Intent callScreen = new Intent(this, CallScreenActivity.class);
        callScreen.putExtra(SinchService.CALL_ID, callId);
        callScreen.putExtra(SinchService.CALL_NAME, userInfo.getName());
        callScreen.putExtra(SinchService.CALL_IMAGE, userInfo.getImage_uri());
        startActivity(callScreen);
    }


    @Override
    public void onStartFailed(SinchError error) {
        Toast.makeText(this, error.toString(), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onStarted() {
        Toast.makeText(mGeoQueriesService, "service started...", Toast.LENGTH_SHORT).show();
    }

}
