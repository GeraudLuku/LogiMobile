package com.geraud.android.gps1.Stories;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.geraud.android.gps1.Chat.ChatActivity;
import com.geraud.android.gps1.GoogleMap.MapsActivity;
import com.geraud.android.gps1.Models.Chat;
import com.geraud.android.gps1.Models.ChatInfo;
import com.geraud.android.gps1.Models.Stories;
import com.geraud.android.gps1.Models.User;
import com.geraud.android.gps1.R;
import com.geraud.android.gps1.RecyclerAdapter.StoriesSliderAdapter;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FullScreenStoryActivity extends AppCompatActivity implements GestureDetector.OnGestureListener {

    public static final int SWIPE_THRESHOLE = 100;
    public static final int VELOCITY_THRESHOLE = 100;

    private ViewPager mViewPager;
    private ImageView mUserImage;
    private TextView mUserName, mTimeStamp;
    private ImageButton mGotoLocationButton, mChatButton;

    private TextView bars[];

    private LinearLayout mBarLayout;

    private ArrayList<Stories> mStoriesList = new ArrayList<>();
    private StoriesSliderAdapter mStoriesSliderAdapter;

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.gotoLocationButton:
                    if (mStoriesList.get(mStoriesSliderAdapter.getCurrentPosition()).getLocation()) {
                        Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                        intent.putExtra("latitude", mStoriesList.get(mStoriesSliderAdapter.getCurrentPosition()).getLatitude());
                        intent.putExtra("longitude", mStoriesList.get(mStoriesSliderAdapter.getCurrentPosition()).getLongitude());
                        setResult(Activity.RESULT_OK, intent);
                        startActivity(intent);
                    } else
                        Toast.makeText(getApplicationContext(), "Location not available", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.chatButton:
                    //check if chat exist or create chat with the user
                    //check if the chat already exists
                    //get GSON and convert it
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("USER").child("chat");
                    Query query = reference.orderByChild("user").equalTo(mStoriesList.get(mStoriesSliderAdapter.getCurrentPosition()).getPhone());
                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                //if the chat already exists, open the chat in chat activity
                                getChatData(dataSnapshot.getKey());
                            } else {
                                //if it doesn't exist create a new chat then......
                                String key = FirebaseDatabase.getInstance().getReference().child("CHAT").push().getKey();
                                DatabaseReference userDB = FirebaseDatabase.getInstance().getReference().child("USER");
                                DatabaseReference chatInfoDB = FirebaseDatabase.getInstance().getReference().child("CHAT").child(key).child("info");

                                mNewChatMap.put("id", key);
                                mNewChatMap.put("lastMessage/", null);
                                mNewChatMap.put("users/" + FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber(), true);
                                mNewChatMap.put("users/" + mStoriesList.get(mStoriesSliderAdapter.getCurrentPosition()).getPhone(), true);
                                mNewChatMap.put("name", null);
                                mNewChatMap.put("type", "single");
                                mNewChatMap.put("image", null);

                                Map<String, String> myChatMap = new HashMap<>();
                                myChatMap.put("type", "single");
                                myChatMap.put("user", mStoriesList.get(mStoriesSliderAdapter.getCurrentPosition()).getPhone());

                                Map<String, String> singleChatMap = new HashMap<>();
                                singleChatMap.put("type", "single");
                                singleChatMap.put("user", FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber());

                                chatInfoDB.updateChildren(mNewChatMap);
                                userDB.child(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber()).child("chat").child(key).setValue(myChatMap); //chat id set in my Document
                                userDB.child(myChatMap.get("user")).child("chat").child(key).setValue(singleChatMap); // set chat id in the other users document

                                //open chatActivity for this newly created chat
                                getChatData(key);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                    break;
            }
        }
    };

    private HashMap mNewChatMap = new HashMap();
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
                        chatId = dataSnapshot.child("id").getValue().toString();

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
                    mUser.setNotificationKey(dataSnapshot.child("notificationKey").getValue().toString());

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
                    // create intent here and send extras (mChatinfo,mChatList)
                    Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
                    intent.putExtra("chatObject", mChatList.get(0));  // get the first document i the list because there is only one document
                    intent.putExtra("chatInfoObject", mChatInfo.get(0));
                    startActivity(intent);
                    finish();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_story);

        //init views
        mViewPager = findViewById(R.id.viewPager);
        mUserImage = findViewById(R.id.userImage);
        mUserName = findViewById(R.id.userName);
        mTimeStamp = findViewById(R.id.timestamp);

        mGotoLocationButton = findViewById(R.id.gotoLocationButton);
        mChatButton = findViewById(R.id.chatButton);
        mGotoLocationButton.setOnClickListener(mOnClickListener);
        mChatButton.setOnClickListener(mOnClickListener);

        mBarLayout = findViewById(R.id.layoutBars);

        //get intent serialisable from intent data
        Stories model = (Stories) getIntent().getSerializableExtra("story");
        mStoriesList = model.getStoryObjectArrayList();

        mStoriesSliderAdapter = new StoriesSliderAdapter(getApplicationContext(), mStoriesList);
        mViewPager.setAdapter(mStoriesSliderAdapter);
        mViewPager.addOnPageChangeListener(viewPagerPageChangeListener);

        addBottomDots(mStoriesSliderAdapter.getCurrentPosition());

        //on viewpager touch hide all views
        mViewPager.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Boolean result = false;
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        visibility(false);
                        result = true;
                        break;
                    case MotionEvent.ACTION_UP:
                        visibility(true);
                        result = true;
                        break;
                }
                return result;
            }
        });
    }

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

    ViewPager.OnPageChangeListener viewPagerPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int i, float v, int i1) {

        }

        @Override
        public void onPageSelected(int i) {
            addBottomDots(i);

        }

        @Override
        public void onPageScrollStateChanged(int i) {

        }
    };

    private void visibility(Boolean visible) {
        if (visible) {
            mUserImage.setVisibility(View.VISIBLE);
            mUserName.setVisibility(View.VISIBLE);
            mTimeStamp.setVisibility(View.VISIBLE);
            mGotoLocationButton.setVisibility(View.VISIBLE);
            mChatButton.setVisibility(View.VISIBLE);
            mBarLayout.setVisibility(View.VISIBLE);
        } else {
            mUserImage.setVisibility(View.INVISIBLE);
            mUserName.setVisibility(View.INVISIBLE);
            mTimeStamp.setVisibility(View.INVISIBLE);
            mGotoLocationButton.setVisibility(View.INVISIBLE);
            mChatButton.setVisibility(View.INVISIBLE);
            mBarLayout.setVisibility(View.INVISIBLE);
        }
    }

    //btnNextClick
    public void nextSlide(View v) {
        // checking for last page
        // if last page home screen will be launched
        int current = getItem(mStoriesSliderAdapter.getCurrentPosition());
//        if (current < layouts.length) {
        if (current < mStoriesSliderAdapter.getCount()) {
            // move to next screen
            mViewPager.setCurrentItem(current);
        } else {
            finish();
        }
    }

    private int getItem(int i) {
        return mViewPager.getCurrentItem() + i;
    }

    // set of Dots points
    private void addBottomDots(int currentPage) {
        bars = new TextView[mStoriesSliderAdapter.getCount()];
        mBarLayout.removeAllViews();
        for (int i = 0; i < bars.length; i++) {
            bars[i] = new TextView(this);
            bars[i].setText(Html.fromHtml("<hr>"));
            bars[i].setTextSize(40);
            bars[i].setTextColor(getResources().getColor(R.color.gray));  // dot_inactive
            mBarLayout.addView(bars[i]);
        }

        if (bars.length > 0)
            bars[currentPage].setTextColor(getResources().getColor(R.color.white)); // dot_active
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent downEvent, MotionEvent moveEvent, float velocityX, float velocityY) {
        Boolean result = false;

        float diffY = moveEvent.getY() - downEvent.getY();
        float diffX = moveEvent.getX() - downEvent.getX();

        //which was greater movement across y or x
        if (Math.abs(diffX) > Math.abs(diffY)) {
            //right or left swipe
            if (Math.abs(diffX) > SWIPE_THRESHOLE && Math.abs(velocityX) > VELOCITY_THRESHOLE) {
                if (diffX > 0) {
                    //on swipe right
                } else {
                    //on swipe left
                }
            }
        } else {
            // up or down swipe
            if (Math.abs(diffY) > SWIPE_THRESHOLE && Math.abs(velocityY) > VELOCITY_THRESHOLE) {
                if (diffY > 0) {
                    //on swipe down
                    finish();
                    result = true;
                } else {
                    //on swipe top
                }

            }


        }
        return result;
    }
}

