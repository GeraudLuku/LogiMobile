package com.geraud.android.gps1.Camera;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;

import com.geraud.android.gps1.R;

import life.knowledge4.videotrimmer.K4LVideoTrimmer;
import life.knowledge4.videotrimmer.interfaces.OnTrimVideoListener;

public class VideoTrimmerActivity extends AppCompatActivity implements OnTrimVideoListener{

    private K4LVideoTrimmer mVideoTrimmer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_trimmer);

        Intent extraIntent = getIntent();
        String path = "";

        if (extraIntent != null) {
            path = extraIntent.getStringExtra(CameraActivity.EXTRA_VIDEO_PATH);
        }

        mVideoTrimmer = findViewById(R.id.timeLine);
        if (mVideoTrimmer != null) {
            mVideoTrimmer.setMaxDuration(10);
            mVideoTrimmer.setOnTrimVideoListener(this);
            mVideoTrimmer.setDestinationPath(Environment.getExternalStorageDirectory() + "/" + "Logi" + "/" + "Logi Videos" + "/");
            mVideoTrimmer.setVideoURI(Uri.parse(path));
        }
    }

    @Override
    public void getResult(final Uri uri) {
        Intent returnIntent = new Intent();
        returnIntent.putExtra("result",uri);
        setResult(Activity.RESULT_OK,returnIntent);
        finish();
    }

    @Override
    public void cancelAction() {
        mVideoTrimmer.destroy();
        finish();
    }

}