package com.geraud.android.gps1.Onboarding_slides;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
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

    private ViewPager mViewPager;
    private LinearLayout mLinearLayout;

    private TextView[] mDots;
    private Button mBack, mNext;

    private int mCurrentPage;

    ViewPager.OnPageChangeListener mOnPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            addDotIndicator(position);
            mCurrentPage = position;

            if (position == 0) {
                mBack.setVisibility(View.GONE);
                mBack.setEnabled(false);
                mNext.setEnabled(true);
                mBack.setText("");
                mNext.setText(getString(R.string.next));
            } else if (position == mDots.length - 1) {
                mBack.setEnabled(true);
                mNext.setEnabled(true);
                mBack.setVisibility(View.VISIBLE);
                mBack.setText(getString(R.string.back));
                mNext.setText(getString(R.string.finish));

            } else {
                mBack.setEnabled(true);
                mNext.setEnabled(true);
                mBack.setVisibility(View.VISIBLE);
                mBack.setText(getString(R.string.back));
                mNext.setText(getString(R.string.next));
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
        //        If you do not call commit() or apply(), your changes will not be saved.
        //                Commit() writes the changes synchronously and directly to the file
        //                Apply() writes the changes to the in-memory SharedPreferences immediately but begins an asynchronous commit to disk
        SharedPreferences sharedPreferences = getSharedPreferences("mSharedPreferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("firsStart", false);
        editor.apply();

        //configs
        mLinearLayout = findViewById(R.id.linearLayout);
        mViewPager = findViewById(R.id.pager);
        mBack = findViewById(R.id.button);
        mNext = findViewById(R.id.button2);
        SliderAdapter mSliderAdapter = new SliderAdapter(getApplicationContext());
        mViewPager.setAdapter(mSliderAdapter);

        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mViewPager.setCurrentItem(mCurrentPage - 1);
            }
        });
        mNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mViewPager.setCurrentItem(mCurrentPage + 1);
                if (mCurrentPage == 4) {
                    startActivity(new Intent(Walkthrough.this, Registration.class));
                }
            }
        });
        addDotIndicator(0);
        mViewPager.addOnPageChangeListener(mOnPageChangeListener);
    }

    public void addDotIndicator(int position) {
        Log.d(TAG, "addDotIndicator: ");
        mDots = new TextView[4];
        mLinearLayout.removeAllViews();
        for (int i = 0; i < mDots.length; i++) {
            mDots[i] = new TextView(getApplicationContext());
            mDots[i].setText(Html.fromHtml("&#8226;"));
            mDots[i].setTextColor(ContextCompat.getColor(getApplicationContext(),R.color.colorAccent));
            mDots[i].setTextSize(35);
            mLinearLayout.addView(mDots[i]);
        }
        if (mDots.length > 0) {
            mDots[position].setTextColor(ContextCompat.getColor(getApplicationContext(),R.color.black));
        }
    }
}