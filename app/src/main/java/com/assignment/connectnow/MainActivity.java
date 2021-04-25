package com.assignment.connectnow;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import java.util.Random;

import io.agora.rtc.Constants;
import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;
import io.agora.rtc.video.VideoCanvas;

import io.agora.rtc.video.VideoEncoderConfiguration;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private RtcEngine mRtcEngine;
    private  IRtcEngineEventHandler mRtcEventHandler;
    private static final String YOUR_TOKEN = "006ba84fbae95d545f39cb93f4e3bfd0a90IABQM4M/wKb0sfIj1qv/k9DMqx5cVIJC6ZaW5tiSEXZzRH2rH+sAAAAAEAALtir+KHSGYAEAAQAmdIZg";

    private static final int PERMISSION_REQ_ID = 22;

    // Ask for Android device permissions at runtime.
    private static final String[] REQUESTED_PERMISSIONS = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRTCEventHandler();

        // If all the permissions are granted, initialize the RtcEngine object and join a channel.
        if (checkSelfPermission(REQUESTED_PERMISSIONS[0], PERMISSION_REQ_ID) &&
                checkSelfPermission(REQUESTED_PERMISSIONS[1], PERMISSION_REQ_ID)) {
            initializeEngine();
            setChannelProfile();
            setupLocalVideo();
            joinChannel();
        }

    }

    private void joinChannel() {

        // Join a channel with a token.
        mRtcEngine.joinChannel(YOUR_TOKEN, "demoChannel1", "Extra Optional Data", new Random(1000).nextInt() + 1);
    }

    private void setupLocalVideo() {

        // Enable the video module.
        mRtcEngine.enableVideo();

        // Create a SurfaceView object.
         FrameLayout mLocalContainer  = findViewById(R.id.local_video_view_container) ;
         SurfaceView mLocalView;

        mLocalView = RtcEngine.CreateRendererView(getBaseContext());
        mLocalView.setZOrderMediaOverlay(true);
        mLocalContainer.addView(mLocalView);
        // Set the local video view.
        VideoCanvas localVideoCanvas = new VideoCanvas(mLocalView, VideoCanvas.RENDER_MODE_FIT, 0);
        mRtcEngine.setupLocalVideo(localVideoCanvas);
    }
    private boolean checkSelfPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(this, permission) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, REQUESTED_PERMISSIONS, requestCode);
            return false;
        }

        return true;
    }
    private void setChannelProfile() {
        mRtcEngine.setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING);
        mRtcEngine.setClientRole(IRtcEngineEventHandler.ClientRole.CLIENT_ROLE_BROADCASTER);
    }

    private void setRTCEventHandler() {
        mRtcEventHandler = new IRtcEngineEventHandler() {
            @Override
            // Listen for the onJoinChannelSuccess callback.
            // This callback occurs when the local user successfully joins the channel.
            public void onJoinChannelSuccess(String channel, final int uid, int elapsed) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.i("agora","Join channel success, uid: " + (uid & 0xFFFFFFFFL));
                    }
                });
            }

            @Override
            // Listen for the onUserJoined callback.
            // This callback occurs when the remote host successfully joins the channel.
            // You can call the setupRemoteVideo method in this callback to set up the remote video view.
            public void onUserJoined(final int uid, int elapsed) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.i("agora","Remote user joined, uid: " + (uid & 0xFFFFFFFFL));
                        setupRemoteVideo(uid);
                    }
                });
            }

            @Override
            // Listen for the onUserOffline callback.
            // This callback occurs when the host leaves the channel or drops offline.
            public void onUserOffline(final int uid, int reason) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.i("agora","User offline, uid: " + (uid & 0xFFFFFFFFL));
                        onRemoteUserLeft();
                    }
                });
            }
        };
    }

    private void onRemoteUserLeft() {

    }


    private void setupRemoteVideo(int uid) {

        // Create a SurfaceView object.
         FrameLayout mRemoteContainer = findViewById(R.id.remote_video_view_container);
         SurfaceView mRemoteView;


        mRemoteView = RtcEngine.CreateRendererView(getBaseContext());
        mRemoteContainer.addView(mRemoteView);
        // Set the remote video view.
        mRtcEngine.setupRemoteVideo(new VideoCanvas(mRemoteView, VideoCanvas.RENDER_MODE_HIDDEN, uid));

    }

    // Initialize the RtcEngine object.
    private void initializeEngine() {
        try {
            mRtcEngine = RtcEngine.create(getBaseContext(), getString(R.string.agora_app_id), mRtcEventHandler);
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
            throw new RuntimeException("NEED TO check rtc sdk init fatal error\n" + Log.getStackTraceString(e));
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        leaveChannel();
        RtcEngine.destroy();
    }

    private void leaveChannel() {
        // Leave the current channel.
        mRtcEngine.leaveChannel();
    }


}