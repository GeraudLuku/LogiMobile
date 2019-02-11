package com.geraud.android.gps1.RecyclerAdapter;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.geraud.android.gps1.Models.Stories;
import com.geraud.android.gps1.R;

import java.util.ArrayList;

public class StoriesSliderAdapter extends PagerAdapter {
    private Context context;
    private ArrayList<Stories> stories;
    private LayoutInflater layoutInflater;
    private View view;
    private int position;

    public StoriesSliderAdapter(Context context, ArrayList<Stories> stories){
        this.context = context;
        this.stories = stories;
    }

    @Override
    public int getCount() {
        return stories.size();
    }

    public int getCurrentPosition(){
        return this.position;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
        return view == o;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        layoutInflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        this.position = position;

        if (stories.get(position).getType().equals("image")){
            view = layoutInflater.inflate(R.layout.image_story_slide_layout, container,false);
            container.addView(view);

            ImageView imageView = view.findViewById(R.id.imageView);
            TextView textView = view.findViewById(R.id.imageDescription);
            View line = view.findViewById(R.id.view);

            //load image with Glide on View
            Glide.with(context)
                    .load(stories.get(position).getMedia())
                    .into(imageView);

            //load description text
            if (stories.get(position).getDescription().equals("")){
                line.setVisibility(View.INVISIBLE);
                textView.setVisibility(View.INVISIBLE);
            }else {
                textView.setText(stories.get(position).getDescription());
            }


        }

        else if(stories.get(position).getType().equals("video")){
            view = layoutInflater.inflate(R.layout.video_story_slide_layout, container,false);
            container.addView(view);

            final VideoView videoView = view.findViewById(R.id.videoView);
            TextView textView = view.findViewById(R.id.imageDescription);
            View line = view.findViewById(R.id.view);

            //load video on videoView
            videoView.setVideoURI(Uri.parse(stories.get(position).getMedia()));
            videoView.start();

            //load description text
            if (stories.get(position).getDescription().equals("")){
                line.setVisibility(View.INVISIBLE);
                textView.setVisibility(View.INVISIBLE);
            }else {
                textView.setText(stories.get(position).getDescription());
            }

            //set on Touch listener onRelease it plays onTouch it pauses
            videoView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    Boolean result = false;
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            //play video
                            if (videoView.isPlaying()){
                                videoView.pause();
                            }
                            result = true;
                            break;

                        case MotionEvent.ACTION_UP:
                            // pause video
                            if (!videoView.isPlaying())
                                videoView.start();
                            result = true;
                            break;
                    }
                    return result;
                }
            });

        }
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

}
