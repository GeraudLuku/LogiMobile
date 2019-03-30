package com.geraud.android.gps1.Sinch;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.geraud.android.gps1.R;
import com.geraud.android.gps1.Services.SinchService;
import com.geraud.android.gps1.Utils.AudioPlayer;
import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallEndCause;
import com.sinch.android.rtc.calling.CallListener;

import java.util.List;

public class IncomingCallScreenActivity extends BaseActivity {

    private String mCallId;
    private String mCallName;
    private String mCallImage;
    private AudioPlayer mAudioPlayer;

    private OnClickListener mClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.call_accept:
                    answerClicked();
                    break;
                case R.id.hangupButton:
                    declineClicked();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incoming_call_screen);

        mAudioPlayer = new AudioPlayer(getApplicationContext());
        mAudioPlayer.playRingtone();
        mCallId = getIntent().getStringExtra(SinchService.CALL_ID);
        mCallName = getIntent().getStringExtra(SinchService.CALL_NAME);
        mCallImage = getIntent().getStringExtra(SinchService.CALL_IMAGE);

        Button answer = findViewById(R.id.call_accept);
        Button decline = findViewById(R.id.hangupButton);
        answer.setOnClickListener(mClickListener);
        decline.setOnClickListener(mClickListener);

    }

    @Override
    protected void onServiceConnected() {
        Call call = getSinchServiceInterface().getCall(mCallId);
        if (call != null) {
            call.addCallListener(new SinchCallListener());
            ImageView callImage = findViewById(R.id.call_image);
            Glide.with(this).load(mCallImage).into(callImage);
            TextView callName = findViewById(R.id.remoteUserName);
            callName.setText(mCallName);
        } else {
            Toast.makeText(getApplicationContext(), "Started with invalid callId, aborting", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void answerClicked() {

        mAudioPlayer.stopRingtone();
        Call call = getSinchServiceInterface().getCall(mCallId);
        if (call != null) {
            call.answer();
            Intent intent = new Intent(this, CallScreenActivity.class);
            intent.putExtra(SinchService.CALL_ID, mCallId);
            intent.putExtra(SinchService.CALL_NAME, mCallName);
            intent.putExtra(SinchService.CALL_IMAGE, mCallImage);
            startActivity(intent);
        } else {
            finish();
        }
    }

    private void declineClicked() {
        mAudioPlayer.stopRingtone();
        Call call = getSinchServiceInterface().getCall(mCallId);
        if (call != null) {
            call.hangup();
        }
        finish();
    }

    private class SinchCallListener implements CallListener {

        @Override
        public void onCallEnded(Call call) {
            CallEndCause cause = call.getDetails().getEndCause();
            Toast.makeText(getApplicationContext(), "Call ended, cause: " + cause.toString(), Toast.LENGTH_SHORT).show();
            mAudioPlayer.stopRingtone();
            finish();
        }

        @Override
        public void onCallEstablished(Call call) {
            Toast.makeText(getApplicationContext(), "Call Established", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCallProgressing(Call call) {
            Toast.makeText(getApplicationContext(), "Call progressing", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onShouldSendPushNotification(Call call, List<PushPair> pushPairs) {
            // Send a push through your push provider here, e.g. GCM
        }
    }
}
