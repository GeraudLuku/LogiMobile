package com.geraud.android.gps1.RecyclerAdapter;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.danikula.videocache.CacheListener;
import com.danikula.videocache.HttpProxyCacheServer;
import com.geraud.android.gps1.Models.Stories;
import com.geraud.android.gps1.R;

import java.io.File;
import java.util.ArrayList;

public class StoriesSliderAdapter extends PagerAdapter {
    private Context mContext;
    private ArrayList<Stories> mStoriesList;

    private VideoView mVideoView;

    private View mView;
    private int mPosition;

    public StoriesSliderAdapter(Context context, ArrayList<Stories> stories) {
        mContext = context;
        mStoriesList = stories;
    }

    @Override
    public int getCount() {
        return mStoriesList.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
        return view == o;
    }

    @SuppressLint("ClickableViewAccessibility")
    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mPosition = position;

//        if (mStoriesList.get(position).getType().equals("image")) {
            mView = layoutInflater.inflate(R.layout.image_story_slide_layout, container, false);
            container.addView(mView);

            ImageView imageView = mView.findViewById(R.id.imageView);
            TextView imageTextView = mView.findViewById(R.id.imageDescription);
            View line = mView.findViewById(R.id.view);

            //load image with Glide on View
            Glide.with(mContext)
                    .load(mStoriesList.get(position).getMedia())
                    .into(imageView);

            //load description text
            if (mStoriesList.get(position).getDescription().equals(""))
                line.setVisibility(View.INVISIBLE);
            else
                imageTextView.setText(mStoriesList.get(position).getDescription());

            return mView;

//        } else {
//            mView = layoutInflater.inflate(R.layout.video_story_slide_layout, container, false);
//            final ProgressBar progressBar = mView.findViewById(R.id.bufferProgress);
//            container.addView(mView);
//
//            mVideoView = mView.findViewById(R.id.videoView);
//            TextView mVideoTextView = mView.findViewById(R.id.videoDescription);
//            View line = mView.findViewById(R.id.view);
//
//            //load video on videoView
//            mVideoView.setVideoPath(mStoriesList.get(position).getMedia());
//            mVideoView.requestFocus();
//            //buffering listener for the video
//            mVideoView.setOnInfoListener(new MediaPlayer.OnInfoListener() {
//                @Override
//                public boolean onInfo(MediaPlayer mp, int what, int extra) {
//                    if (what == mp.MEDIA_INFO_BUFFERING_START) {
//                        progressBar.setVisibility(View.VISIBLE);
//                    } else if (what == mp.MEDIA_INFO_BUFFERING_END) {
//                        progressBar.setVisibility(View.INVISIBLE);
//                    }
//                    return false;
//                }
//            });
//            mVideoView.start();
//
//            mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//                @Override
//                public void onCompletion(MediaPlayer mp) {
//                    mp.seekTo(0);
//                    mp.start();
//                }
//            });
//
//            //load description text
//            if (mStoriesList.get(position).getDescription().equals(""))
//                line.setVisibility(View.INVISIBLE);
        }
    }