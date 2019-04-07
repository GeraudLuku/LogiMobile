package com.geraud.android.gps1.RecyclerAdapter;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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

public class StoriesSliderAdapter extends PagerAdapter implements CacheListener {
    private Context mContext;
    private ArrayList<Stories> mStoriesList;

    private VideoView mVideoView;

    private HttpProxyCacheServer mProxy;

    private View mView;
    private int mPosition;

    private AudioBecomingNoisy mAudioBecomingNoisy;
    private AudioManager mAudioManager;
    private IntentFilter mNoisyIntentFilter;
    private AudioManager.OnAudioFocusChangeListener mOnAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    mediaPause();
                    break;

                case AudioManager.AUDIOFOCUS_GAIN:
                    mediaPlay();
                    break;
                case AudioManager.AUDIOFOCUS_LOSS:
                    mediaPause();
                    break;
            }
        }
    };

    private class AudioBecomingNoisy extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            mediaPause();
        }
    }

    public StoriesSliderAdapter(Context context, ArrayList<Stories> stories) {
        mContext = context;
        mStoriesList = stories;

        mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        mAudioBecomingNoisy = new AudioBecomingNoisy();
        mNoisyIntentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);

        mProxy = getProxy();
    }

    private HttpProxyCacheServer getProxy() {
        return new HttpProxyCacheServer.Builder(mContext)
                .maxCacheSize(15360)       // 5 Gb for cache 1GB = 3072
                .build();
    }


    @Override
    public int getCount() {
        return mStoriesList.size();
    }

    public int getCurrentPosition() {
        return mPosition;
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

        if (mStoriesList.get(position).getType().equals("image")) {
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


        } else if (mStoriesList.get(position).getType().equals("video")) {
            mView = layoutInflater.inflate(R.layout.video_story_slide_layout, container, false);
            container.addView(mView);

            mVideoView = mView.findViewById(R.id.videoView);
            TextView mVideoTextView = mView.findViewById(R.id.videoDescription);
            View line = mView.findViewById(R.id.view);

            //load video on videoView
            String proxyUrl = mProxy.getProxyUrl(mStoriesList.get(position).getMedia());
            mVideoView.setVideoPath(proxyUrl);
            mVideoView.start();

            //load description text
            if (mStoriesList.get(position).getDescription().equals(""))
                line.setVisibility(View.INVISIBLE);
            else
                mVideoTextView.setText(mStoriesList.get(position).getDescription());


            //set on Touch listener onRelease it plays onTouch it pauses
            mVideoView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    boolean result = false;
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            //play video
                            mediaPlay();
                            result = true;
                            break;

                        case MotionEvent.ACTION_UP:
                            // pause video
                            mediaPause();
                            result = true;
                            break;
                    }
                    return result;
                }
            });

        }
        return mView;
    }

    private void mediaPause() {
        mVideoView.pause();
        mAudioManager.abandonAudioFocus(mOnAudioFocusChangeListener);
        mContext.unregisterReceiver(mAudioBecomingNoisy);
    }

    private void mediaPlay() {
        mContext.registerReceiver(mAudioBecomingNoisy, mNoisyIntentFilter);
        int requestAudioFocusResult = mAudioManager.requestAudioFocus(mOnAudioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        if (requestAudioFocusResult == AudioManager.AUDIOFOCUS_REQUEST_GRANTED)
            mVideoView.start();
    }

    //progress of downloading status video
    @Override
    public void onCacheAvailable(File cacheFile, String url, int percentsAvailable) {
        Toast.makeText(mContext, "Percentage Video Downloaded : " + percentsAvailable + "%", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }


}
