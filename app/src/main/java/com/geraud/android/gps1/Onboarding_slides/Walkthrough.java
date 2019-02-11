package com.geraud.android.gps1.Onboarding_slides;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.geraud.android.gps1.R;
import com.geraud.android.gps1.RecyclerAdapter.SliderAdapter;
import com.geraud.android.gps1.Registration;

public class Walkthrough extends AppCompatActivity {
    private static final String TAG = "WalkthroughActivity";
    private SliderAdapter mSliderAdapter;
    private ViewPager mViewPager;
    private LinearLayout mLinearLayout;
    private TextView[] mDots;
    private Button back, next;
    private int mCurrentPge;
    ViewPager.OnPageChangeListener onPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            addDotIndicator(position);
            mCurrentPge = position;
            if (position == 0) {
                back.setVisibility(View.GONE);
                back.setEnabled(false);
                next.setEnabled(true);
                back.setText("");
                next.setText("Next");
            } else if (position == mDots.length - 1) {
                back.setEnabled(true);
                next.setEnabled(true);
                back.setVisibility(View.VISIBLE);
                back.setText("Back");
                next.setText("Finish");

            } else {
                back.setEnabled(true);
                next.setEnabled(true);
                back.setVisibility(View.VISIBLE);
                back.setText("Back");
                next.setText("Next");
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_walkthrough);

        //declare in the sharedpreference that this activity has been opened once
        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("firsStart", false);

        //configs
        mLinearLayout = findViewById(R.id.linearLayout);
        mViewPager = findViewById(R.id.pager);
        back = findViewById(R.id.button);
        next = findViewById(R.id.button2);
        mSliderAdapter = new SliderAdapter(this);
        mViewPager.setAdapter(mSliderAdapter);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mViewPager.setCurrentItem(mCurrentPge - 1);
            }
        });
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mViewPager.setCurrentItem(mCurrentPge + 1);
                if (mCurrentPge == 4) {
                    startActivity(new Intent(Walkthrough.this, Registration.class));
                }
            }
        });
        addDotIndicator(0);
        mViewPager.addOnPageChangeListener(onPageChangeListener);
    }

    public void addDotIndicator(int position) {
        Log.d(TAG, "addDotIndicator: ");
        mDots = new TextView[4];
        mLinearLayout.removeAllViews();
        for (int i = 0; i < mDots.length; i++) {
            mDots[i] = new TextView(this);
            mDots[i].setText(Html.fromHtml("&#8226;"));
            mDots[i].setTextColor(getResources().getColor(R.color.colorAccent));
            mDots[i].setTextSize(35);
            mLinearLayout.addView(mDots[i]);
        }
        if (mDots.length > 0) {
            mDots[position].setTextColor(getResources().getColor(R.color.black));
        }
    }
}