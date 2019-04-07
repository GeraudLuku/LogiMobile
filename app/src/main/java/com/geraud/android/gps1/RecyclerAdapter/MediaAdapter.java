package com.geraud.android.gps1.RecyclerAdapter;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.geraud.android.gps1.R;

import java.util.ArrayList;
import java.util.Objects;

public class MediaAdapter extends RecyclerView.Adapter<MediaAdapter.ViewHolder> {

    private ArrayList<String> mMediaList;
    private Context mContext;

    public MediaAdapter(ArrayList<String> mediaList, Context context) {
        mContext = context;
        mMediaList = mediaList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View layoutView = LayoutInflater.from(mContext).inflate(R.layout.media_item, viewGroup, false);
        return new ViewHolder(layoutView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder mediaViewHolder, int position) {
        ContentResolver cr = mContext.getContentResolver();
        if (Objects.requireNonNull(cr.getType(Uri.parse(mMediaList.get(position)))).startsWith("image")){
            Glide.with(mContext).load(Uri.parse(mMediaList.get(position))).into(mediaViewHolder.mMedia);
        }else { //it is a video so get thumbnail and load it on View
            Bitmap bitmap2 = ThumbnailUtils.createVideoThumbnail( mMediaList.get(position) , MediaStore.Images.Thumbnails.MINI_KIND );
            Glide.with(mContext).load(bitmap2).into(mediaViewHolder.mMedia);
        }

    }

    @Override
    public int getItemCount() {
        return mMediaList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        ImageView mMedia;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            mMedia = itemView.findViewById(R.id.media);
        }
    }
}
