package com.geraud.android.gps1.Stories;

import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.geraud.android.gps1.Models.Stories;
import com.geraud.android.gps1.R;
import com.geraud.android.gps1.RecyclerAdapter.StoriesSliderAdapter;

import java.util.ArrayList;

public class FullScreenStoryActivity extends AppCompatActivity implements GestureDetector.OnGestureListener {

    public static final int SWIPE_THRESHOLE = 100;
    public static final int VELOCITY_THRESHOLE = 100;
    private ViewPager mViewPager;
    private ImageView mUserImage;
    private TextView mUserName,
            mTimeStamp;
    private Button mGotoLocationButton;
    private LinearLayout mBarLayout;
    private RelativeLayout mContainer;
    private TextView dots[];

    private ArrayList<Stories> stories = new ArrayList<>();
    private StoriesSliderAdapter storiesSliderAdapter;

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
        mBarLayout = findViewById(R.id.layoutBars);
        mContainer = findViewById(R.id.container);

        //on viewpager touch hide all views
        mViewPager.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Boolean result = false;
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mUserImage.setVisibility(View.INVISIBLE);
                        mUserName.setVisibility(View.INVISIBLE);
                        mTimeStamp.setVisibility(View.INVISIBLE);
                        mGotoLocationButton.setVisibility(View.INVISIBLE);
                        mBarLayout.setVisibility(View.INVISIBLE);
                        result = true;
                        break;
                    case MotionEvent.ACTION_UP:
                        mUserImage.setVisibility(View.VISIBLE);
                        mUserName.setVisibility(View.VISIBLE);
                        mTimeStamp.setVisibility(View.VISIBLE);
                        mGotoLocationButton.setVisibility(View.VISIBLE);
                        mBarLayout.setVisibility(View.VISIBLE);
                        result = true;
                        break;
                }
                return result;
            }
        });

        //get intent serialisable from intent data
        Stories model = (Stories) getIntent().getSerializableExtra("story");
        stories = model.getStoryObjectArrayList();

        storiesSliderAdapter = new StoriesSliderAdapter(this, stories);
        mViewPager.setAdapter(storiesSliderAdapter);
        mViewPager.addOnPageChangeListener(viewPagerPageChangeListener);

        addBottomDots(storiesSliderAdapter.getCurrentPosition());

        //i want a swipe down gesture to close the activity
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

    //btnNextClick
    public void nextSlide(View v) {
        // checking for last page
        // if last page home screen will be launched
        int current = getItem(storiesSliderAdapter.getCurrentPosition());
//        if (current < layouts.length) {
        if (current < storiesSliderAdapter.getCount()) {
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
        dots = new TextView[storiesSliderAdapter.getCount()];
        mBarLayout.removeAllViews();
        for (int i = 0; i < dots.length; i++) {
            dots[i] = new TextView(this);
            dots[i].setText(Html.fromHtml("<hr>"));
            dots[i].setTextSize(40);
            dots[i].setTextColor(getResources().getColor(R.color.gray));  // dot_inactive
            mBarLayout.addView(dots[i]);
        }

        if (dots.length > 0)
            dots[currentPage].setTextColor(getResources().getColor(R.color.white)); // dot_active
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
