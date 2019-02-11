package com.geraud.android.gps1.GoogleMap;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
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
import com.geraud.android.gps1.Dailogs.AddPlace;
import com.geraud.android.gps1.InfoWindow.InfoWindow_People;
import com.geraud.android.gps1.InfoWindow.InfoWindow_Places;
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import es.dmoral.toasty.Toasty;

public class MapsActivity extends BaseActivity implements
        SinchService.StartFailedListener,
        RoutingListener,
        AddPlace.AddPlaceListener {


    public static final int REQUEST_PERMISSION_CODE = 1;
    public static final int REQUEST_CHECK_SETTINGS = 123;
    public List<String> friends;
    GoogleApiClient mGoogleApiClient;

    int INTERVAL = 10000;
    int FASTEST_INTERVAL = 5000;
    FloatingActionButton currentLocationBtn;
    FloatingActionButton cancelRoute;
    LinearLayout tapactionlayout;
    View bottomSheet;
    EditText searchbar;

    ImageView profile_pic;
    TextView profile_name;
    int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
    //for creating random colors for geofences
    Random rnd = new Random();
    TSnackbar snackbar;
    SeekBar verticalSeekBar;

    private FirebaseAuth mFirebaseAuth;
    private DatabaseReference mDatabaseReference;
    private DatabaseReference mSearchRef;
    private String USER_KEY;


    Marker mFriends;
    Marker mMarkerData;
    Circle mGeoBoundary;
    LatLng mMarkerClickLatLng;
    boolean showGeofences = true;
    private HeaderFragment StoryHeaderFragment;
    private BodyFragment StoryBodyFragment;
    private GoogleMap mMap;
    //Google ApiClient
    private GoogleApiClient googleApiClient;
    private String TAG = "gps";
    private BottomSheetBehavior mBottomSheetBehavior1;
    private List<Polyline> polylines;
    private Gson gson = new Gson();
    private ActionBarDrawerToggle toggle;

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
        mFirebaseAuth = FirebaseAuth.getInstance();
        USER_KEY = mFirebaseAuth.getCurrentUser().getPhoneNumber();

        profile_pic = findViewById(R.id.bottomSheet_Image);
        profile_name = findViewById(R.id.bottomSheet_Name);
        currentLocationBtn = findViewById(R.id.fab);
        cancelRoute = findViewById(R.id.cancelRoute);
        tapactionlayout = findViewById(R.id.tap_action_layout);
        searchbar = findViewById(R.id.search_text);
        mGeoFire = new GeoFire(FirebaseDatabase.getInstance().getReference("LOCATION"));

        bottomSheet = findViewById(R.id.bottom_sheet1);
        mBottomSheetBehavior1 = BottomSheetBehavior.from(bottomSheet);
        mBottomSheetBehavior1.setPeekHeight(120);
        mBottomSheetBehavior1.setState(BottomSheetBehavior.STATE_HIDDEN);
        mBottomSheetBehavior1.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    tapactionlayout.setVisibility(View.VISIBLE);
                    mBottomSheetBehavior1.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }

                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    tapactionlayout.setVisibility(View.GONE);
                    mBottomSheetBehavior1.setState(BottomSheetBehavior.STATE_EXPANDED);
                }

                if (newState == BottomSheetBehavior.STATE_DRAGGING) {
                    tapactionlayout.setVisibility(View.GONE);
                }
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    tapactionlayout.setVisibility(View.GONE);
                    mBottomSheetBehavior1.setState(BottomSheetBehavior.STATE_HIDDEN);
                    mBottomSheetBehavior1.setPeekHeight(0);
                }

            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });

        //start polylines
        polylines = new ArrayList<>();

        //chats button pressed
        findViewById(R.id.chats).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        //method for vertical seekbar
        verticalSeekBar = findViewById(R.id.verticalSeekBar);
        verticalSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
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

        tapactionlayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBottomSheetBehavior1.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
                    mBottomSheetBehavior1.setState(BottomSheetBehavior.STATE_EXPANDED);
                }
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
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        //toggle search button to toggle the type of item to search on the map
        final ToggleButton toggleSearch = (ToggleButton) toolbar.findViewById(R.id.toggle_search);
        toggleSearch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    //change search to Users
                    mSearchRef = FirebaseDatabase.getInstance().getReference().child("user");
                    toggleSearch.setText("Places");
                } else {
                    //change search to Places
                    mSearchRef = FirebaseDatabase.getInstance().getReference().child("places");
                    toggleSearch.setText("Users");
                }
            }
        });

        //listen constantly to changes in text to the editText
        searchbar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //do nothing if there is no text in the edit Text
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //constantly search on each key stroke
                String text = s.toString();
                Search(text);
            }

            @Override
            public void afterTextChanged(Editable s) {
                //search the final text in the edit Text
                String text = s.toString();
                Search(text);
            }
        });


        mapFragment.getMapAsync(mMapReadyCallback);

        //goto users current location in app
        currentLocationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getCurrentLocation();
            }
        });

        //cancel directions API polyline route(s)
        cancelRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                eraseAllRouteLines();
                //cancelRoute.setVisibility(View.GONE);
                Toasty.info(getApplicationContext(), "Route cancelled", 1000, true).show();
            }
        });

        //get search text and search on the map
        //findViewById(R.id.search)

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

        googleApiClient.connect();
        //do some actions here.....
        //first get all contacts
        Contacts contacts = new Contacts(this);
        friends = contacts.getAllContacts();
        //load user information
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
        googleApiClient.disconnect();
        makeUserOffline();
        super.onStop();
    }

    @Override
    public void onDestroy() {
        if (getSinchServiceInterface() != null) {
            getSinchServiceInterface().stopClient();
        }
        if (mServiceIsBound){
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
            Toast.makeText(getApplicationContext(), "geoqueries service connection passed", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mGeoQueriesService = null;
            Toast.makeText(getApplicationContext(), "Geoqueries service connection failed", Toast.LENGTH_SHORT).show();
        }
    };

    //all related map actions take place here
    private OnMapReadyCallback mMapReadyCallback = new OnMapReadyCallback() {
        @Override
        public void onMapReady(GoogleMap googleMap) {
            mMap = googleMap;

            LocationRequest mLocationRequest = LocationRequest.create();
            mLocationRequest.setInterval(INTERVAL);
            mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

            bindService(new Intent(getApplicationContext(), GeoQueries.class), mServiceConnection, BIND_AUTO_CREATE);
            mServiceIsBound = true;

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

                        if (checkPermission())
                            buildGoogleApiClient();
                        Toast.makeText(MapsActivity.this, "Permission Granted", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(MapsActivity.this, "Permission Denied", Toast.LENGTH_LONG).show();

                    }
                }

                break;
        }
    }

    public boolean checkPermission() {

        int FirstPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION);
        int SecondPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION);

        return FirstPermissionResult == PackageManager.PERMISSION_GRANTED &&
                SecondPermissionResult == PackageManager.PERMISSION_GRANTED;

    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(mGoogleApiClientConnectionCallbacks)
                .addOnConnectionFailedListener(mGoogleApiClientConnectionFailedListener)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }



    private Location mLastLocation;
    private Marker mUserMarker;
    private GeoFire mGeoFire;
    private LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            mLastLocation = location;
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

            //update marker on screen
            if (mUserMarker != null) mUserMarker.remove();

            mUserMarker = mMap.addMarker(new MarkerOptions()
                    .position(latLng) //setting position
                    .draggable(false) //Making the marker non draggable
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.rsz_personal_avatar)) //icon image here i am going to put the users bitmojii
                    .title("Me")); //Adding my name as title

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
                verticalSeekBar.setVisibility(View.INVISIBLE);
                mToolBarState = false;
            } else {
                getSupportActionBar().show();
                verticalSeekBar.setVisibility(View.VISIBLE);
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
                            mDatabaseReference.child("geofences").child(mFirebaseAuth.getCurrentUser().getPhoneNumber()).child("latitude").equalTo(radius.getCenter().latitude).addListenerForSingleValueEvent(new ValueEventListener() {
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
                            // Do nothing
                        }
                    });
        }
    };


    private GoogleMap.OnMarkerClickListener mOnMarkerClickListener = new GoogleMap.OnMarkerClickListener() {
        @Override
        public boolean onMarkerClick(Marker marker) {
            boolean clicked = false;

            //markers latitude and longitude
            mMarkerClickLatLng = marker.getPosition();
            //get marker GSON objet from the snippet
            mMarkerData = marker;

            if (marker.getTitle().equals("USER")) {
                //set the custom info window adapter
                mMap.setInfoWindowAdapter(new InfoWindow_People(MapsActivity.this));
                mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                    @Override
                    public void onInfoWindowClick(Marker marker) {
                        //here i will show the various options to do on the maker
                        peopleMenu();
                    }
                });
                clicked = true;
            } else if (marker.getTitle().equals("PLACE")) {
                //set the custom info window adapter
                mMap.setInfoWindowAdapter(new InfoWindow_Places(MapsActivity.this));
                mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                    @Override
                    public void onInfoWindowClick(Marker marker) {
                        //here i will show the various options to do on the maker
                        placeMenu();
                    }
                });
                clicked = true;
            }
            //else if its my marker (my location) so as not to cause a NUllPointer Exception ;)
            else if (marker.getTitle().equals("Me")) {
                Toasty.normal(getApplicationContext(), "Me", 1000).show();
            }
            return clicked; //return false else it wont work dont know why tho
        }
    };


    //Getting current location
    private void getCurrentLocation() {

        if (mLastLocation != null) {
            //moving the map to my  location
            //Creating a LatLng Object to store Coordinates
            LatLng latLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());

            if (mUserMarker != null) mUserMarker.remove();
            //Adding marker to map
            mUserMarker = mMap.addMarker(new MarkerOptions()
                    .position(latLng) //setting position
                    .draggable(false) //Making the marker non draggable
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.rsz_personal_avatar)) //icon image here i am going to put the users bitmojii
                    .title("Me")); //Adding my name as title

            //Moving the camera
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            //Animating the camera
            mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
            //update value on seekbar
            verticalSeekBar.setProgress(15);
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
            verticalSeekBar.setProgress(15);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (toggle.onOptionsItemSelected(item)) {
            //open the drawer and also initialise fragments
            initialiseFragment();
            return true;
        }
        switch (item.getItemId()) {
            case R.id.show_geofences:
                //show or hide geofences depends
                if (showGeofences) {
                    showGeofences = false;
                    item.setIcon(R.drawable.ic_hide_geofences);
                    mGeoBoundary.setVisible(false);
                } else {
                    showGeofences = true;
                    item.setIcon(R.drawable.ic_show_geofences);
                    mGeoBoundary.setVisible(true);
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //method to load Current user details into dashboard
    public void UserDetails() {

        mDatabaseReference.child("user").child(mFirebaseAuth.getCurrentUser().getPhoneNumber()).addListenerForSingleValueEvent(new ValueEventListener() {  // to get the data only once and remove the listner
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot doc : dataSnapshot.getChildren()) {

                        User user = doc.getValue(User.class);
                        Glide.with(getApplicationContext()) //load profile picture
                                .load(user.getImage_uri())
                                .into(profile_pic);
                        profile_name.setText(user.getName());
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
                    mDatabaseReference.child("places").child(USER_KEY).push().setValue(place).addOnCompleteListener(new OnCompleteListener<Void>() {
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
                        String PlaceJSON = gson.toJson(myPlace);
                        mPlaces = mMap.addMarker(new MarkerOptions()
                                .position(new LatLng(myPlace.getLatitude(), myPlace.getLongitude())) //convert the lat and long to double
                                .title(myPlace.getType())
                                .snippet(PlaceJSON)
                                .icon(BitmapDescriptorFactory.fromPath(String.format("R.drawable.%s", myPlace.getDesc2())))
                                .draggable(false)
                        );
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //now load my friends places also
        //for each friend
        for (String friends : friends) {
            //load all places
            mDatabaseReference.child("places").child(friends).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot dc : dataSnapshot.getChildren()) {

                            Place myPlace = dc.getValue(Place.class);
                            //create JSON object to send the info to infoWindow adapter through the snippet
                            String PlaceExtraInfo = gson.toJson(myPlace);
                            mPlaces = mMap.addMarker(new MarkerOptions()
                                    .position(new LatLng(myPlace.getLatitude(), myPlace.getLongitude())) //convert the lat and long to double
                                    .title(myPlace.getType().toString())
                                    .snippet(PlaceExtraInfo)
                                    .icon(BitmapDescriptorFactory.fromPath(String.format("R.drawable.%s", myPlace.getDesc2())))
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

    private void LoadFriends() {

        if (!friends.isEmpty()) {
            //start a timer to run after 1 second
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    //runOnUithread....
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (mFriends != null) mFriends.remove();
                            for (final String friends : friends) {

                                //first query the users section of the database to get the friends information
                                mDatabaseReference.child("user").child(friends).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.exists()) {
                                            for (DataSnapshot dc1 : dataSnapshot.getChildren()) {
                                                final User user = dc1.getValue(User.class);
                                                final String friendExtraInfo = gson.toJson(user);
                                                //second query to get the location to finally display friends location
                                                mDatabaseReference.child("location").child(friends).child("l").addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                        if (dataSnapshot.exists()) {
                                                            for (DataSnapshot dc2 : dataSnapshot.getChildren()) {

                                                                List<Object> map = (List<Object>) dc2.getValue();
                                                                double Lat = 0;
                                                                double Lng = 0;
                                                                if (map.get(0) != null) {
                                                                    Lat = Double.parseDouble(map.get(0).toString());
                                                                }
                                                                if (map.get(1) != null) {
                                                                    Lng = Double.parseDouble(map.get(1).toString());
                                                                }

                                                                mFriends = mMap.addMarker(new MarkerOptions()
                                                                        .position(new LatLng(Lat, Lng)) //convert the lat and long to double
                                                                        .title(user.getType())
                                                                        .snippet(friendExtraInfo)
                                                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.rsz_people))
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
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });

                            }
                        }
                    });


                }
            }, 0, 5000);

        } else Toasty.error(this, "No contacts present", 1000, true).show();
    }

    //creating a geo coding boundary
    private void AddGeoBoundary() {
        //save geofence in Realtime Databse for Future use
        GeoFence geoFence = new GeoFence(mClickLatLng.latitude, mClickLatLng.longitude);
        mDatabaseReference.child("GEOFENCES").child(USER_KEY).setValue(geoFence).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    loadGeoFences();
                    numGeofences++;
                    Toasty.success(getApplicationContext(), "Geofence created successfully", 2000).show();
                }else {
                    Toast.makeText(getApplicationContext(), "Couldnt create geoboundary", Toast.LENGTH_SHORT).show();
                }
            }
        });
        mClickLatLng = null;
    }

    private int numGeofences = 0;
    private List<LatLng> mGeoLocationsList;
    private GeoQueries mGeoQueriesService;
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
                                .strokeColor(0xFF0000FF)
                                .fillColor(0x110000FF)
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
        mGeoQueriesService.query(this, mGeoLocationsList, friends);
    }


    //alert dailog when pressing options on the info window of friends
    private void peopleMenu() {
        CharSequence options[] = new CharSequence[]{"Chat", "Go-To", "Poke", "Call"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("What Do You Want?");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // the user clicked on options[which]
                switch (which) {
                    case 0:
                        //startActivity(new Intent(getApplicationContext(), ChatUsersActivity.class));
                    case 1:
                        drawRoute(mMarkerClickLatLng);
                        break;
                    case 3:
                        call(mMarkerData);
                }
            }
        });
        builder.show();
    }

    //alert dailog when pressing options on the info window of Places
    private void placeMenu() {
        CharSequence options[] = new CharSequence[]{"Go-To"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("You Want?");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //if the user presses on GO-TO a route should be created from the user to that position
                switch (which) {
                    case 0:
                        drawRoute(mMarkerClickLatLng);
                        break;
                }
            }
        });
        builder.show();
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
                if (numGeofences <= 5) {
                    AddGeoBoundary();
                }else
                    Toast.makeText(getApplicationContext(), "Geofence creation exceeded (MAX=5)", Toast.LENGTH_SHORT).show();
            }
        });

        builder.show();
    }

    //search method to search on the MAP
    private void Search(String text) {
        //this method is case sensitive take note!!!
        mSearchRef.orderByChild("name")
                .startAt(text)
                .endAt(text + "\uf8ff")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.exists()) {

                            for (DataSnapshot dc : dataSnapshot.getChildren()) {

                                try { //if the datasnapshot can enter the user object
                                    User user = dc.getValue(User.class);
                                    mDatabaseReference.child("location").child(user.getPhone()).child("l").addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.exists()) {
                                                for (DataSnapshot dc : dataSnapshot.getChildren()) {

                                                    List<Object> map = (List<Object>) dc.getValue();
                                                    double Lat = 0;
                                                    double Lng = 0;
                                                    if (map.get(0) != null) {
                                                        Lat = Double.parseDouble(map.get(0).toString());
                                                    }
                                                    if (map.get(1) != null) {
                                                        Lng = Double.parseDouble(map.get(1).toString());
                                                    }
                                                    LatLng latLng = new LatLng(Lat, Lng);

                                                    //move to location on Map
                                                    goTO(latLng);
                                                }
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });

                                } catch (Exception e) { //else insert it into the place object

                                    Place place = dc.getValue(Place.class);
                                    LatLng latLng = new LatLng(place.getLatitude(), place.getLongitude());
                                    goTO(latLng);
                                }

                            }

                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    //make user online
    private void makeUserOnline() {

        //update Realtime Database status value
        mDatabaseReference.child("user").child(mFirebaseAuth.getCurrentUser().getPhoneNumber()).child("status").setValue(0);
        // Adding on disconnect hook
        mDatabaseReference.child("user").child(mFirebaseAuth.getCurrentUser().getPhoneNumber()).child("status").onDisconnect().setValue(1);
    }

    //make user offline
    private void makeUserOffline() {
        //change value in Realtime Database so that the cloud function will automatically change it in the realtime database
        mDatabaseReference.child("user").child(mFirebaseAuth.getCurrentUser().getPhoneNumber()).child("status").setValue(1);
    }

    private void makeUserAway() {
        mDatabaseReference.child("user").child(mFirebaseAuth.getCurrentUser().getPhoneNumber()).child("status").setValue(2);
    }

    //this might be useful when i will implement the search bar so that when u press on
    // a search result it go directly to the location of the user on the map
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

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
            }
        }
    }

    private void drawRoute(LatLng mMarkerClickLatLng) {

        Routing routing = new Routing.Builder()
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(this)
                .alternativeRoutes(false) //will make if true to experiment it sometime
                .waypoints(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()), mMarkerClickLatLng)  // (start , end)
                .build();
        routing.execute();
    }

    //erase all Route lines from the map
    private void eraseAllRouteLines() {

        snackbar.dismiss();
        for (Polyline line : polylines) {
            line.remove();
        }
        polylines.clear();
    }

    @Override
    public void onRoutingFailure(RouteException e) {

        Toasty.error(getApplicationContext(), "Route couldnt be created check code", 2000).show();
    }

    @Override
    public void onRoutingStart() {

    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex) {

        //hide toolbar  to make space for the snakbar
        getSupportActionBar().hide();

        if (polylines.size() > 0)
            for (Polyline poly : polylines)
                poly.remove();

        polylines = new ArrayList<>();
        //add route(s) to the map.
        for (int i = 0; i < route.size(); i++) {

            PolylineOptions polyOptions = new PolylineOptions();
            polyOptions.color(getResources().getColor(R.color.cornflower_blue));
            polyOptions.width(10 + i * 3);
            polyOptions.addAll(route.get(i).getPoints());
            Polyline polyline = mMap.addPolyline(polyOptions);
            polylines.add(polyline);

        }

        //snackbar to show distance and time to cover it
        snackbar = TSnackbar
                .make(findViewById(R.id.mainWindow), String.format("Distance - [ %d ] ~~ Duration - [ %d ]", route.get(shortestRouteIndex).getDistanceValue(), route.get(shortestRouteIndex).getDurationValue()), TSnackbar.LENGTH_INDEFINITE);
        snackbar.setActionTextColor(Color.WHITE);
        snackbar.setIconLeft(R.drawable.ic_directions_black_24dp, 24); //Size in dp - 24 is great!
        snackbar.setIconPadding(8);
        //snackbar.setMaxWidth(3000); //if you want fullsize on tablets
        View snackbarView = snackbar.getView();
        snackbarView.setBackgroundColor(Color.BLACK);
        snackbar.show();

        //make cancel route button visible only if route was created successfully
        //cancelRoute.setVisibility(View.VISIBLE);
        Toasty.success(getApplicationContext(), "Route succesfully created", 1000, true).show();
    }

    @Override
    public void onRoutingCancelled() {

    }

    //calling a user
    private void call(Marker mMarkerData) {
        User userInfo = gson.fromJson(mMarkerData.getSnippet(), User.class);
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
