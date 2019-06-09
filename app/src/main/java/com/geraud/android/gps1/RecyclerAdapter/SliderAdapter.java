package com.geraud.android.gps1.RecyclerAdapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.geraud.android.gps1.R;

/**
 * Created by Geraud on 12/6/2018.
 */

public class SliderAdapter extends PagerAdapter {
    private Context mContext;

    public SliderAdapter(Context context) {
        mContext = context;
    }

    private int[] slideImages = {
            R.drawable.locations1,
            R.drawable.locations1,
            R.drawable.privacy1,
            R.drawable.adaptive_map1,
            R.drawable.add_location
    };
    private String[] headings = {
            "Location",
            "Chat",
            "Privacy",
            "Adaptive Maps",
            "Discover & Add Locations",
            "Be a Customer"
    };
    private String[] descriptions = {
            "Get access to real-time location of all available friends and also locations of areas around you",
            "Send messages to your friends using our well optimised cloud messaging system",
            "We got your back on the moments you will like to disappear from the radar",
            "The map's contrast automatically adapts to your environment to improve your vision",
            "Don't just watch default locations you can create custom locations on the map and share with your friends",
            "Receive and chose to subscribe to businesses around you to receive promotion updates from them"
    };

    @Override
    public int getCount() {
        return slideImages.length;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @Override
    @NonNull
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        LayoutInflater mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

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
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }
}