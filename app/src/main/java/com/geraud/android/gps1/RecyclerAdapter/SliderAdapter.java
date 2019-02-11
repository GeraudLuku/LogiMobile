package com.geraud.android.gps1.RecyclerAdapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.geraud.android.gps1.R;

/**
 * Created by Elia on 12/4/2017.
 */

public class SliderAdapter extends PagerAdapter {
    private Context mContext;
    private LayoutInflater mLayoutInflater;

    private int[] slideImages = {
            R.drawable.locations1,
            R.drawable.chat1,
            R.drawable.privacy1,
            R.drawable.adaptive_map1,
            R.drawable.add_location
    };
    private String[] headings = {
            "Location",
            "Chat",
            "Privacy",
            "Adaptive Maps",
            "Discover & Add Locations"
    };
    private String[] descriptions = {
            "Get access to real time location of all friends and also locations of areas around you",
            "Send messages to your contacts using our well optimised cloud messaging systems",
            "We got your back on the moments you will like to disappear from the radar",
            "The maps contrast automatically adapts to your environment to improve your vision",
            "Don't just watch default locations you can create custom locations on the map and share with your friends"
    };

    public SliderAdapter(Context context) {
        this.mContext = context;
    }

    @Override
    public int getCount() {
        return slideImages.length;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == (RelativeLayout) object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View v = mLayoutInflater.inflate(R.layout.slide_layout, container, false);


        TextView heading = v.findViewById(R.id.top);
        TextView bottom = v.findViewById(R.id.bottom);
        ImageView imageView = v.findViewById(R.id.pic);

        imageView.setImageResource(slideImages[position]);
        heading.setText(headings[position]);
        bottom.setText(descriptions[position]);

        container.addView(v);

        return v;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }
}