package com.geraud.android.gps1.RecyclerAdapter;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.geraud.android.gps1.R;

import java.util.ArrayList;

public class MediaAdapter extends RecyclerView.Adapter<MediaAdapter.ViewHolder> {

    private ArrayList<String> mMediaList;
    private Context mContext;

    public MediaAdapter(ArrayList<String> mediaList, Context context){
        this.mContext = context;
        this.mMediaList = mediaList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View layoutView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.media_item,null,false);
        return new ViewHolder(layoutView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder mediaViewHolder, int position) {
        Glide.with(mContext).load(Uri.parse(mMediaList.get(position))).into(mediaViewHolder.mMedia);
    }

    @Override
    public int getItemCount() {
        return mMediaList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        ImageView mMedia;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mMedia = itemView.findViewById(R.id.media);
        }
    }
}
