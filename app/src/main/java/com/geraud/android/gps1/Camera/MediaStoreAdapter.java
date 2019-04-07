package com.geraud.android.gps1.Camera;

import android.app.Activity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.geraud.android.gps1.R;

import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

public class MediaStoreAdapter extends RecyclerView.Adapter<MediaStoreAdapter.ViewHolder> {

    private Cursor mMediaStoreCursor;
    private Activity mActivity;
    private OnClickThumbListener mOnClickThumbListener;

    public interface OnClickThumbListener {
        void OnClickImage(Uri imageUri);

        void OnClickVideo(Uri videoUri);
    }

    MediaStoreAdapter(Activity activity) {
        mActivity = activity;
        mOnClickThumbListener = (OnClickThumbListener) activity;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.camera_recycler_image_view, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {

        Glide.with(mActivity)
                .load(getUriFromMediaStore(i))
                .into(viewHolder.getImageView());
        //if its a video get duration of the video and if not null set it on the text
        if (getMimeType(getUriFromMediaStore(i)) != null && getMimeType(getUriFromMediaStore(i)).startsWith("video")) {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(mActivity, getUriFromMediaStore(i));
            long timeInMillisec = Long.parseLong(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
            viewHolder.setImageDuration(timeInMillisec);
            retriever.release();
        }

    }

    @Override
    public int getItemCount() {
        return (mMediaStoreCursor == null) ? 0 : mMediaStoreCursor.getCount();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final ImageView mImageView;
        private TextView mVideoDuration;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            mImageView = itemView.findViewById(R.id.mediastoreImageView);
            mVideoDuration = itemView.findViewById(R.id.videoDuration);
            mImageView.setOnClickListener(this);
        }

        private ImageView getImageView() {
            return mImageView;
        }

        private void setImageDuration(long duration) {
            Calendar cal = Calendar.getInstance(Locale.ENGLISH);
            cal.setTimeInMillis(duration);
            mVideoDuration.setVisibility(View.VISIBLE);
            mVideoDuration.setText(DateFormat.format("mm:ss", cal).toString());
        }

        @Override
        public void onClick(View v) {
            getOnClickUri(getAdapterPosition());
        }

    }

    private Cursor swapCursor(Cursor cursor) {
        if (mMediaStoreCursor == cursor) {
            return null;
        }
        Cursor oldCursor = mMediaStoreCursor;
        this.mMediaStoreCursor = cursor;
        if (cursor != null) {
            this.notifyDataSetChanged();
        }
        return oldCursor;
    }

    void changeCursor(Cursor cursor) {
        Cursor oldCursor = swapCursor(cursor);
        if (oldCursor != null) {
            oldCursor.close();
        }
    }

//    private Bitmap getBitmapFromMediaStore(int position) {
//        int idIndex = mMediaStoreCursor.getColumnIndex(MediaStore.Files.FileColumns._ID);
//        int mediaTypeIndex = mMediaStoreCursor.getColumnIndex(MediaStore.Files.FileColumns.MEDIA_TYPE);
//
//        mMediaStoreCursor.moveToPosition(position);
//
//        switch (mMediaStoreCursor.getInt(mediaTypeIndex)) {
//            case MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE:
//                return MediaStore.Images.Thumbnails.getThumbnail(
//                        mActivity.getContentResolver(),
//                        mMediaStoreCursor.getLong(idIndex),
//                        MediaStore.Images.Thumbnails.MICRO_KIND,
//                        null
//                );
//            case MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO:
//                return MediaStore.Video.Thumbnails.getThumbnail(
//                        mActivity.getContentResolver(),
//                        mMediaStoreCursor.getLong(idIndex),
//                        MediaStore.Video.Thumbnails.MICRO_KIND,
//                        null
//                );
//            default:
//                return null;
//        }
//    }

    private Uri getUriFromMediaStore(int position) {
        int dataIndex = mMediaStoreCursor.getColumnIndex(MediaStore.Files.FileColumns.DATA);

        mMediaStoreCursor.moveToPosition(position);

        String dataString = mMediaStoreCursor.getString(dataIndex);

        return Uri.parse("file://" + dataString);
    }

    private String getMimeType(Uri uri) {
        String mimeType;
        if (Objects.requireNonNull(uri.getScheme(),"uri.getScheme() cant be null").equals(ContentResolver.SCHEME_CONTENT)) {
            ContentResolver cr = mActivity.getContentResolver();
            mimeType = cr.getType(uri);
        } else {
            String fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri
                    .toString());
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                    fileExtension.toLowerCase());
        }
        return mimeType;
    }

    private void getOnClickUri(int position) {
        int mediaTypeIndex = mMediaStoreCursor.getColumnIndex(MediaStore.Files.FileColumns.MEDIA_TYPE);
        int dataIndex = mMediaStoreCursor.getColumnIndex(MediaStore.Files.FileColumns.DATA);

        mMediaStoreCursor.moveToPosition(position);

        String dataString = mMediaStoreCursor.getString(dataIndex);
        Uri mediaUri = Uri.parse("file://" + dataString);

        switch (mMediaStoreCursor.getInt(mediaTypeIndex)) {
            case MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE:
                mOnClickThumbListener.OnClickImage(mediaUri);
                break;
            case MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO:
                mOnClickThumbListener.OnClickVideo(mediaUri);
                break;

            default:
        }

    }
}
