package com.geraud.android.gps1.Stories;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
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

import com.bumptech.glide.Glide;
import com.geraud.android.gps1.Chat.ChatActivity;
import com.geraud.android.gps1.Chat.ChatsActivity;
import com.geraud.android.gps1.GoogleMap.MapsActivity;
import com.geraud.android.gps1.Models.Chat;
import com.geraud.android.gps1.Models.ChatInfo;
import com.geraud.android.gps1.Models.Stories;
import com.geraud.android.gps1.Models.User;
import com.geraud.android.gps1.R;
import com.geraud.android.gps1.RecyclerAdapter.StoriesSliderAdapter;
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
import java.util.Objects;

public class FullScreenStoryActivity extends AppCompatActivity {

    public static final String STORY = "story";

    private ImageView mUserImage;
    private TextView mUserName, mTimeStamp;
    private ImageButton mChatButton;

    private LinearLayout mBarLayout;

    private ArrayList<Stories> mStoriesList = new ArrayList<>();
    private StoriesSliderAdapter mStoriesSliderAdapter;

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.chatButton:
                    startActivity(new Intent(getApplicationContext(), ChatsActivity.class));
                    break;
            }
        }
    };

    private DatabaseReference mUserReference;
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_story);

        //init views
        mUserImage = findViewById(R.id.userImage);
        mUserName = findViewById(R.id.userName);
        mTimeStamp = findViewById(R.id.timestamp);

        mUserReference = FirebaseDatabase.getInstance().getReference().child("USER");

        mChatButton = findViewById(R.id.chatButton);
        mChatButton.setOnClickListener(mOnClickListener);

        mBarLayout = findViewById(R.id.layoutBars);

        //get intent serialisable from intent data
        Stories model = (Stories) getIntent().getSerializableExtra(STORY);
        mStoriesList = model.getStoryObjectArrayList();

        //fill in dashboard
        mUserReference.child(mStoriesList.get(0).getPhone()).child("userInfo").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    User user = dataSnapshot.getValue(User.class);
                    Glide.with(FullScreenStoryActivity.this)
                            .load(user.getImage_uri())
                            .into(mUserImage);
                    mUserName.setText(user.getName());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mStoriesSliderAdapter = new StoriesSliderAdapter(getApplicationContext(), mStoriesList);

        addBottomDots(0);

        //on viewpager touch hide all views
        ViewPager viewPager = findViewById(R.id.viewPager);
        viewPager.setAdapter(mStoriesSliderAdapter);
        viewPager.addOnPageChangeListener(viewPagerPageChangeListener);
        viewPager.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                boolean result = false;
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
        public void onPageSelected(int position) {
            addBottomDots(position);

            if (position > mStoriesSliderAdapter.getCount() - 1) // if you scroll pass the last slide, end the activity
                finish();

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
            mChatButton.setVisibility(View.VISIBLE);
            mBarLayout.setVisibility(View.VISIBLE);
        } else {
            mUserImage.setVisibility(View.INVISIBLE);
            mUserName.setVisibility(View.INVISIBLE);
            mTimeStamp.setVisibility(View.INVISIBLE);
            mChatButton.setVisibility(View.INVISIBLE);
            mBarLayout.setVisibility(View.INVISIBLE);
        }
    }

    // set of Dots points
    private void addBottomDots(int currentPage) {
        TextView[] bars = new TextView[mStoriesSliderAdapter.getCount()];
        mBarLayout.removeAllViews();
        for (int i = 0; i < bars.length; i++) {
            bars[i] = new TextView(getApplicationContext());
            bars[i].setText(Html.fromHtml("<hr>"));
            bars[i].setTextSize(40);
            bars[i].setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.gray));  // dot_inactive
            mBarLayout.addView(bars[i]);
        }

        if (bars.length > 0)
            bars[currentPage].setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white)); // dot_active
    }

}

