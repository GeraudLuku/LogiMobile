package com.geraud.android.gps1.Camera;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.geraud.android.gps1.Chat.ChatsActivity;
import com.geraud.android.gps1.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class CameraActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>,
        MediaStoreAdapter.OnClickThumbListener {

    private final static int READ_EXTERNAL_STORAGE_PERMISSION_RESULT = 0;
    private final static int WRITE_EXTERNAL_STORAGE_PERMISSION_RESULT = 1;
    public static final int REQUEST_VIDEO_TRIMMER = 11;
    public static final String EXTRA_VIDEO_PATH = "video";
    private final static int REQUEST_CAMERA_PERMISSION_RESULT = 2;
    private final static int REQUEST_MEDIA_FROM_GALLERY = 3;
    private final static int MEDIASTORE_LOADER_ID = 0;
    private static final int STATE_PREVIEW = 0;
    private static final int STATE_WAIT_LOCK = 1;

    private static final int MIN_CLICK_DURATION = 600;

    public static final String CAMERA_FRONT = "1";
    public static final String CAMERA_BACK = "0";
    private String cameraId = CAMERA_FRONT;

    private boolean mIsFlashSupported;
    private boolean mIsTorchOn;

    private String mLogiFolderName;

    private long mStartClickTime;
    private boolean mLongClickActive,
            mIsRecording, mPause = false;
    private long mElapsed;
    private long mRemainingSecs = 0;
    private long mElapsedSeconds = 0;
    private Timer mTimer;

    private int mCaptureState = STATE_PREVIEW;
    private MediaStoreAdapter mMediaStoreAdapter;
    private TextureView mTextureView;
    private TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            setupCamera(width, height);
            connectCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    };

    private CameraDevice mCameraDevice;
    private CameraDevice.StateCallback mCameraDeviceStateCallBack = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            mCameraDevice = camera;
            if (mIsRecording) {
                try {
                    createVideoFileName();
                    startRecord();
                    mMediaRecorder.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else
                startPreview();
            Toast.makeText(getApplicationContext(), "Connected To The Camera", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onDisconnected(CameraDevice camera) {
            camera.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            camera.close();
            mCameraDevice = null;
            Toast.makeText(CameraActivity.this, "Error Opening Camera Closing...", Toast.LENGTH_SHORT).show();
        }
    };
    private String mCameraId;
    private Size mPreviewSize;
    private Size mVideoSize;
    public Size mImageSize;
    private ImageReader mImageReader;
    private ImageReader.OnImageAvailableListener mOnImageAvailableListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            mBackgroundHandler.post(new ImageSaver(reader.acquireLatestImage()));
        }
    };

    private class ImageSaver implements Runnable {

        private final Image mImage;

        private ImageSaver(Image image) {
            mImage = image;
        }

        @Override
        public void run() {
            ByteBuffer byteBuffer = mImage.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[byteBuffer.remaining()];
            byteBuffer.get(bytes);

            FileOutputStream fileOutputStream = null;
            try {
                fileOutputStream = new FileOutputStream(mImageFileName);
                fileOutputStream.write(bytes);
            } catch (IOException exception) {
                exception.printStackTrace();
            } finally {
                mImage.close();
                updateImageMediaStore();
                if (fileOutputStream != null) {
                    try {
                        fileOutputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                //if this activity was opened from another one open transfer Activity
                //intent to the image full screen activity
                Intent fullScreenIntent = new Intent(getApplicationContext(), FullScreenImageActivity.class);
                if (getIntent().getStringExtra(ChatsActivity.CHATS_ACTIVITY_CAMERA_EXTRA) != null)
                    fullScreenIntent.putExtra(ChatsActivity.CHATS_ACTIVITY_CAMERA_EXTRA, "chatsActivity");
                fullScreenIntent.setData(Uri.fromFile(new File(mImageFileName)));
                startActivity(fullScreenIntent);
            }
        }
    }

    private MediaRecorder mMediaRecorder;
    private int mTotalRotation;
    private CameraCaptureSession mPreviewCaptureSession;
    private CameraCaptureSession.CaptureCallback mPreviewCaptureCallback = new CameraCaptureSession.CaptureCallback() {

        private void process(CaptureResult captureResult) {
            switch (mCaptureState) {
                case STATE_PREVIEW:
                    //do nothing
                    break;
                case STATE_WAIT_LOCK:
                    mCaptureState = STATE_PREVIEW;
                    Integer afstate = captureResult.get(CaptureResult.CONTROL_AF_STATE);
                    if (afstate != null)
                        if (afstate == CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED || afstate == CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED) {
                            Toast.makeText(getApplicationContext(), "auto focus locked", Toast.LENGTH_SHORT).show();
                            startStillCaptureRequest();
                        }
                    break;
            }
        }

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
            process(result);
        }
    };
    private CaptureRequest.Builder mCaptureRequestBuilder;

    private ProgressBar mProgressBar;

    private File mImageFolder;
    private File mVideoFolder;
    private String mImageFileName;
    private String mVideoFileName;

    private HandlerThread mBackgroundHandlerThread;
    private Handler mBackgroundHandler;
    private static SparseIntArray ORIENTATIONS = new SparseIntArray();

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 0);
        ORIENTATIONS.append(Surface.ROTATION_90, 90);
        ORIENTATIONS.append(Surface.ROTATION_180, 180);
        ORIENTATIONS.append(Surface.ROTATION_270, 270);
    }

    private static class CompareSizeByArea implements Comparator<Size> {

        @Override
        public int compare(Size lhs, Size rhs) {
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() /
                    (long) rhs.getWidth() * rhs.getHeight());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera2);

        createVideoFolder();
        createImageFolder();

        mMediaRecorder = new MediaRecorder();

        //create LOGI Folder in Device Storage
        mLogiFolderName = "Logi";
        File f = new File(Environment.getExternalStorageDirectory(), mLogiFolderName);
        if (!f.exists()) {
            boolean wasSuccessful = f.mkdirs();
            if (wasSuccessful)
                Toast.makeText(getApplicationContext(), "Logi Folder Created In Storage", Toast.LENGTH_SHORT).show();
        }

        Button closeCamera = findViewById(R.id.closeCameraButton);
        closeCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeCamera();
                finish();
            }
        });

        Button switchCamera = findViewById(R.id.orientationButton);
        switchCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchCamera();
            }
        });

        Button toggleFlash = findViewById(R.id.flashButon);
        toggleFlash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchFlash();
            }
        });

        mTextureView = findViewById(R.id.textureView);

        mProgressBar = findViewById(R.id.progressBar2);
        mProgressBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lockFocus();
            }
        });
        mProgressBar.setOnLongClickListener(new View.OnLongClickListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onLongClick(View v) {
                try {
                    mProgressBar.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            final long INTERVAL = 1000;
                            final long TIMEOUT = 11000;
                            switch (event.getAction()) {
                                case MotionEvent.ACTION_DOWN:
                                    Log.i("ACTION_DOWN", "ACTION_DOWN::" + mPause + " " + mIsRecording);
                                    if (!mLongClickActive) {
                                        mLongClickActive = true;
                                        mStartClickTime = Calendar.getInstance().getTimeInMillis();
                                    }
                                    break;
                                case MotionEvent.ACTION_MOVE:
                                    if (mLongClickActive) {
                                        long clickDuration = Calendar.getInstance().getTimeInMillis() - mStartClickTime;
                                        if (clickDuration >= MIN_CLICK_DURATION) {
                                            mLongClickActive = false;
                                            if (mPause && !mIsRecording) {
                                                mPause = false;
                                                // work on UiThread for better performance
                                                runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        try {
                                                            checkWriteExternalStoragePermission();
                                                        } catch (final Exception ex) {
                                                            // Log.i("---","Exception in thread");
                                                        }
                                                    }
                                                });

                                                TimerTask task1 = new TimerTask() {
                                                    @Override
                                                    public void run() {
                                                        mRemainingSecs -= INTERVAL;

                                                        if (mRemainingSecs == 0) {
                                                            this.cancel();
                                                            try {
                                                                runOnUiThread(new Runnable() {
                                                                    @Override
                                                                    public void run() {
                                                                        mIsRecording = false;
                                                                        // release the MediaRecorder object
                                                                        mMediaRecorder.stop();
                                                                        mMediaRecorder.reset();
                                                                        updateVideoMediaStore();

                                                                        Intent fullScreenIntent = new Intent(getApplicationContext(), VideoPlayActivity.class);
                                                                        if (getIntent().getStringExtra(ChatsActivity.CHATS_ACTIVITY_CAMERA_EXTRA) != null)
                                                                            fullScreenIntent.putExtra(ChatsActivity.CHATS_ACTIVITY_CAMERA_EXTRA, "chatsActivity");
                                                                        fullScreenIntent.setData(Uri.fromFile(new File(mVideoFileName)));
                                                                        startActivity(fullScreenIntent);

                                                                        Log.e("Video captured!", "Video sucessfully captured");
                                                                    }
                                                                });
                                                            } catch (Exception e) {
                                                                e.printStackTrace();
                                                            }
                                                            return;
                                                        }

                                                        mElapsedSeconds = mRemainingSecs;
                                                        mProgressBar.setProgress((int) (mElapsedSeconds / 1000));
                                                        Log.i("TIME camera video", "Milli::" + (mRemainingSecs / 1000));

                                                    }
                                                };
                                                mTimer = new Timer();
                                                mTimer.scheduleAtFixedRate(task1, INTERVAL, INTERVAL);

                                            } else {
                                                // work on UiThread for better performance
                                                runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        try {
                                                            checkWriteExternalStoragePermission();
                                                        } catch (final Exception ex) {
                                                            // Log.i("---","Exception in thread");
                                                        }
                                                    }
                                                });
                                                TimerTask task2 = new TimerTask() {
                                                    @Override
                                                    public void run() {
                                                        mElapsed += INTERVAL;
                                                        if (mElapsed == TIMEOUT) {
                                                            this.cancel();
                                                            try {
                                                                runOnUiThread(new Runnable() {
                                                                    @Override
                                                                    public void run() {
                                                                        mIsRecording = false;
                                                                        mMediaRecorder.stop();
                                                                        mMediaRecorder.reset(); // release the MediaRecorder object
                                                                        mProgressBar.setProgress(0);
                                                                        updateVideoMediaStore();

                                                                        Intent fullScreenIntent = new Intent(getApplicationContext(), VideoPlayActivity.class);
                                                                        if (getIntent().getStringExtra(ChatsActivity.CHATS_ACTIVITY_CAMERA_EXTRA) != null)
                                                                            fullScreenIntent.putExtra(ChatsActivity.CHATS_ACTIVITY_CAMERA_EXTRA, "chatsActivity");
                                                                        fullScreenIntent.setData(Uri.fromFile(new File(mVideoFileName)));
                                                                        startActivity(fullScreenIntent);

                                                                        Log.e("Video captured!", "");
                                                                    }
                                                                });


                                                            } catch (Exception e) {
                                                                e.printStackTrace();
                                                            }
                                                            return;
                                                        }
                                                        mElapsedSeconds = 10000 - mElapsed;
                                                        mProgressBar.setProgress((int) (mElapsedSeconds / 1000));
                                                        Log.i("Time mElapsed", "Milli::" + (mElapsedSeconds / 1000));
                                                        mRemainingSecs = mElapsedSeconds;
                                                        mRemainingSecs = Math.round(mRemainingSecs);
                                                        mElapsedSeconds = Math.round(mElapsedSeconds);
                                                    }
                                                };
                                                mTimer = new Timer();
                                                mTimer.scheduleAtFixedRate(task2, INTERVAL, INTERVAL);
                                            }
                                        }
                                    }
                                    break;
                                case MotionEvent.ACTION_UP:
                                    Log.i("ACTION_UP", "ACTION_UP::" + mIsRecording);
                                    mLongClickActive = false;
                                    if (mIsRecording) {
                                        // stop recording and release camera
                                        mProgressBar.setProgress((int) (mElapsedSeconds / 1000));
                                        mTimer.cancel();
                                        mPause = true;
                                        mIsRecording = false;
                                        mRemainingSecs = mRemainingSecs + 1000;
                                        mMediaRecorder.stop();
                                        mMediaRecorder.reset(); // release the MediaRecorder object
                                        updateVideoMediaStore();

                                        Intent fullScreenIntent = new Intent(getApplicationContext(), VideoPlayActivity.class);
                                        if (getIntent().getStringExtra(ChatsActivity.CHATS_ACTIVITY_CAMERA_EXTRA) != null)
                                            fullScreenIntent.putExtra(ChatsActivity.CHATS_ACTIVITY_CAMERA_EXTRA, "chatsActivity");
                                        fullScreenIntent.setData(Uri.fromFile(new File(mVideoFileName)));
                                        startActivity(fullScreenIntent);
                                    }
                                    break;
                            }

                            return true;
                        }
                    });
                } catch (Exception e) {
                    // TODO: handle exception
                    e.printStackTrace();
                }
                return true;
            }
        });

        Button openGalleryButton = findViewById(R.id.galleryButton);
        openGalleryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        RecyclerView thumbnailRecyclerView = findViewById(R.id.thumbnailRecyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        thumbnailRecyclerView.setLayoutManager(linearLayoutManager);
        mMediaStoreAdapter = new MediaStoreAdapter(this);
        thumbnailRecyclerView.setAdapter(mMediaStoreAdapter);

        checkReadExternalStoragePermission();
    }

    private void updateVideoMediaStore() {
        Intent mediaStoreUpdateIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaStoreUpdateIntent.setData(Uri.fromFile(new File(mVideoFileName))); // uri of the file can use it to send t other activity
        sendBroadcast(mediaStoreUpdateIntent);
    }

    private void updateImageMediaStore() {
        Intent mediaStoreUpdateIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaStoreUpdateIntent.setData(Uri.fromFile(new File(mImageFileName))); // uri of the file can use it to send to other activity
        sendBroadcast(mediaStoreUpdateIntent);
    }

    private void setupCamera(int width, int height) {
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        mCameraId = cameraId;
        try {
            CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(mCameraId);

            //setup flash
            Boolean available = cameraCharacteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
            mIsFlashSupported = available == null ? false : available;
            setupFlashButton();

            StreamConfigurationMap map = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            int deviceOrientation = getWindowManager().getDefaultDisplay().getRotation();
            mTotalRotation = sensorToDeviceRotation(cameraCharacteristics, deviceOrientation);
            boolean swapRotation = mTotalRotation == 90 || mTotalRotation == 270;
            int rotatedWidth = width,
                    rotatedHeight = height;

            if (swapRotation) {
                rotatedHeight = width;
                rotatedWidth = height;
            }

            if (map != null) {
                mPreviewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class), rotatedWidth, rotatedHeight);
                mVideoSize = chooseOptimalSize(map.getOutputSizes(MediaRecorder.class), rotatedWidth, rotatedHeight);
                mImageSize = chooseOptimalSize(map.getOutputSizes(ImageFormat.JPEG), rotatedWidth, rotatedHeight);
                mImageReader = ImageReader.newInstance(mImageSize.getWidth(), mImageSize.getHeight(), ImageFormat.JPEG, 1);
                mImageReader.setOnImageAvailableListener(mOnImageAvailableListener, mBackgroundHandler);
            }

        } catch (CameraAccessException e) {
            Toast.makeText(this, "Couldnt Setup Camera " + e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    private void connectCamera() {
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                    cameraManager.openCamera(mCameraId, mCameraDeviceStateCallBack, mBackgroundHandler);
                } else {
                    if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                        Toast.makeText(this, "LOGI Requires Access To Camera", Toast.LENGTH_SHORT).show();
                    }
                    requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO}, REQUEST_CAMERA_PERMISSION_RESULT);
                }
            } else
                cameraManager.openCamera(mCameraId, mCameraDeviceStateCallBack, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public void switchFlash() {
        Button flashButton = findViewById(R.id.flashButon);
        try {
            if (cameraId.equals(CAMERA_BACK)) {
                if (mIsFlashSupported) {
                    if (mIsTorchOn) {
                        mCaptureRequestBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_OFF);
                        mPreviewCaptureSession.setRepeatingRequest(mCaptureRequestBuilder.build(), null, mBackgroundHandler);
                        flashButton.setBackgroundColor(Color.GRAY);
                        mIsTorchOn = false;
                        flashButton.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.flash_off));
                    } else {
                        mCaptureRequestBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_TORCH);
                        mPreviewCaptureSession.setRepeatingRequest(mCaptureRequestBuilder.build(), null, mBackgroundHandler);
                        flashButton.setBackgroundColor(Color.WHITE);
                        mIsTorchOn = true;
                        flashButton.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.flash_on));
                    }
                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }


    public void setupFlashButton() {
        Button flashButton = findViewById(R.id.flashButon);
        if (cameraId.equals(CAMERA_BACK) && mIsFlashSupported) {
            flashButton.setVisibility(View.VISIBLE);
            if (mIsTorchOn) {
                flashButton.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.flash_on));
            } else {
                flashButton.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.flash_off));
            }

        } else {
            flashButton.setVisibility(View.GONE);
        }
    }

    private void startRecord() {
        try {
            setupMediaRecorder();
            SurfaceTexture surfaceTexture = mTextureView.getSurfaceTexture();
            surfaceTexture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            Surface previewSurface = new Surface(surfaceTexture);
            Surface recordSurface = mMediaRecorder.getSurface();
            try {
                mCaptureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
            mCaptureRequestBuilder.addTarget(previewSurface);
            mCaptureRequestBuilder.addTarget(recordSurface);

            try {
                mCameraDevice.createCaptureSession(Arrays.asList(previewSurface, recordSurface),
                        new CameraCaptureSession.StateCallback() {
                            @Override
                            public void onConfigured(@NonNull CameraCaptureSession session) {
                                try {
                                    session.setRepeatingRequest(
                                            mCaptureRequestBuilder.build(),
                                            null,
                                            null
                                    );
                                } catch (CameraAccessException e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                                Toast.makeText(CameraActivity.this, "StartRecord Camera Config Failed", Toast.LENGTH_SHORT).show();
                            }
                        }, null);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startPreview() {
        SurfaceTexture surfaceTexture = mTextureView.getSurfaceTexture();
        surfaceTexture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
        Surface previewSurface = new Surface(surfaceTexture);

        try {
            mCaptureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mCaptureRequestBuilder.addTarget(previewSurface);

            mCameraDevice.createCaptureSession(Arrays.asList(previewSurface, mImageReader.getSurface()), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    mPreviewCaptureSession = session;
                    try {
                        mPreviewCaptureSession.setRepeatingRequest(mCaptureRequestBuilder.build(),
                                null,
                                mBackgroundHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                    Toast.makeText(getApplicationContext(), "Unable To setup Camera Preview", Toast.LENGTH_SHORT).show();
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void startStillCaptureRequest() {
        try {
            mCaptureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            mCaptureRequestBuilder.addTarget(mImageReader.getSurface());
            mCaptureRequestBuilder.set(CaptureRequest.JPEG_ORIENTATION, mTotalRotation);

            CameraCaptureSession.CaptureCallback stillCaptureCallback = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureStarted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, long timestamp, long frameNumber) {
                    try {
                        createImageFileName();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            };
            mPreviewCaptureSession.capture(mCaptureRequestBuilder.build(), stillCaptureCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    //open gallery intent
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("video/* , image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Media"), REQUEST_MEDIA_FROM_GALLERY);
    }


    @Override
    protected void onResume() {
        super.onResume();

        startBackgroundThread();
        if (mTextureView.isAvailable()) {
            setupCamera(mTextureView.getWidth(), mTextureView.getHeight());
            connectCamera();
        } else {
            mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        View decorView = getWindow().getDecorView();
        if (hasFocus) {
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            );
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case READ_EXTERNAL_STORAGE_PERMISSION_RESULT:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //call cursor loader
                    android.support.v4.app.LoaderManager.getInstance(this).initLoader(MEDIASTORE_LOADER_ID, null, this);
                } else {
                    Toast.makeText(getApplicationContext(), "Application Needs Read Storage Permission ", Toast.LENGTH_SHORT).show();
                }
                break;
            case REQUEST_CAMERA_PERMISSION_RESULT:
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(), "Application Cant run without camera", Toast.LENGTH_SHORT).show();
                }
                if (grantResults[1] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(), "Application Cant run without audio mic", Toast.LENGTH_SHORT).show();
                }
                break;
            case WRITE_EXTERNAL_STORAGE_PERMISSION_RESULT:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mIsRecording = true;
                    findViewById(R.id.thumbnailRecyclerView).setVisibility(View.INVISIBLE);
                    try {
                        createVideoFileName();
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Failed To Create VideoFileName", Toast.LENGTH_SHORT).show();
                    }
                    startRecord();
                    mMediaRecorder.start();
                    Toast.makeText(this, "Write External Storage Permission Granted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Application Needs To Save Media ", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int i, @Nullable Bundle bundle) {
        String[] projection = {
                MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.DATE_ADDED,
                MediaStore.Files.FileColumns.DATA,
                MediaStore.Files.FileColumns.MEDIA_TYPE,
        };

        String selection =
                MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                        + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
                        + " OR "
                        + MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                        + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;
        return new CursorLoader(
                this,
                MediaStore.Files.getContentUri("external"),
                projection,
                selection,
                null,
                MediaStore.Files.FileColumns.DATE_ADDED + "DESC"
        );
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        mMediaStoreAdapter.changeCursor(data);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        mMediaStoreAdapter.changeCursor(null);
    }

    @Override
    public void OnClickImage(Uri imageUri) {
        Intent fullScreenIntent = new Intent(this, FullScreenImageActivity.class);
        fullScreenIntent.setData(imageUri);
        startActivity(fullScreenIntent);
    }

    @Override
    public void OnClickVideo(Uri videoUri) {
        Intent fullScreenIntent = new Intent(this, VideoPlayActivity.class);
        fullScreenIntent.setData(videoUri);
        startActivity(fullScreenIntent);
    }

    @Override
    protected void onPause() {
        closeCamera();
        stopBackgroundThread();
        super.onPause();
    }

    private void closeCamera() {
        if (mCameraDevice != null) {
            mCameraDevice.close();
            mCameraDevice = null;
        }
    }

    private void startBackgroundThread() {
        mBackgroundHandlerThread = new HandlerThread("LogiCameraVideoImage");
        mBackgroundHandlerThread.start();
        mBackgroundHandler = new Handler(mBackgroundHandlerThread.getLooper());
    }

    private void stopBackgroundThread() {
        mBackgroundHandlerThread.quitSafely();
        try {
            mBackgroundHandlerThread.join();
            mBackgroundHandlerThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static int sensorToDeviceRotation(CameraCharacteristics cameraCharacteristics, int deviceOrientation) {
        Integer sensorOrientation = cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
        deviceOrientation = ORIENTATIONS.get(deviceOrientation);

        return (sensorOrientation != null) ? (sensorOrientation + deviceOrientation + 360) % 360
                : 0;
    }

    private static Size chooseOptimalSize(Size[] choices, int width, int height) {
        List<Size> bigEnough = new ArrayList<>();

        for (Size option : choices)
            if (option.getHeight() == option.getWidth() * height / width &&
                    option.getWidth() >= width &&
                    option.getHeight() >= height) { //aspect ration idk
                bigEnough.add(option);
            }

        if (bigEnough.size() > 0)
            return Collections.min(bigEnough, new CompareSizeByArea()); //find minimum value in the bigEnough List
        else
            return choices[0];
    }

    private void createVideoFolder() {
        File movieFile = new File(Environment.getExternalStorageDirectory() + "/" + mLogiFolderName);
        mVideoFolder = new File(movieFile, "Logi Videos");
        if (!mVideoFolder.exists()) {
            boolean wasSuccessful = mVideoFolder.mkdirs();
            if (wasSuccessful)
                Toast.makeText(getApplicationContext(), "Video Folder Created SuccessFully", Toast.LENGTH_SHORT).show();
        }
    }

    private void createVideoFileName() throws IOException {
        String timestamp = new SimpleDateFormat("dd/MM/yyyy_HH:mm:ss", Locale.getDefault()).format(new Date());
        String prepend = "VIDEO_" + timestamp + "_";
        File videoFile = File.createTempFile(prepend, ".mp4", mVideoFolder);
        mVideoFileName = videoFile.getAbsolutePath();
    }

    private void createImageFolder() {
        File imageFile = new File(Environment.getExternalStorageDirectory() + "/" + mLogiFolderName);
        mImageFolder = new File(imageFile, "Logi Pictures");
        if (!mImageFolder.exists()) {
            boolean wasSuccessful = mImageFolder.mkdirs();
            if (wasSuccessful)
                Toast.makeText(getApplicationContext(), "Image Folder Created SuccessFully", Toast.LENGTH_SHORT).show();
        }
    }

    private void createImageFileName() throws IOException {
        String timestamp = new SimpleDateFormat("dd/MM/yyyy_HH:mm:ss", Locale.getDefault()).format(new Date());
        String prepend = "IMAGE_" + timestamp + "_";
        File imageFile = File.createTempFile(prepend, ".jpg", mImageFolder);
        mImageFileName = imageFile.getAbsolutePath();
    }

    private void checkReadExternalStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // start cursor loader
                android.support.v4.app.LoaderManager.getInstance(this).initLoader(MEDIASTORE_LOADER_ID, null, this);
            } else {
                if (shouldShowRequestPermissionRationale(android.Manifest.permission.READ_EXTERNAL_STORAGE))
                    Toast.makeText(this, "App Needs To View Thumbnails", Toast.LENGTH_SHORT).show();
                requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, READ_EXTERNAL_STORAGE_PERMISSION_RESULT);
            }

        } else {
            // start cursor loader
            android.support.v4.app.LoaderManager.getInstance(this).initLoader(MEDIASTORE_LOADER_ID, null, this);
        }
    }

    private void checkWriteExternalStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                mIsRecording = true;
                findViewById(R.id.thumbnailRecyclerView).setVisibility(View.INVISIBLE);
                try {
                    createVideoFileName();
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Couldn't Create VideoFileName", Toast.LENGTH_SHORT).show();
                }
                startRecord();
                mMediaRecorder.start();
            } else {
                if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    Toast.makeText(this, "App Needs To Save Media in Storage", Toast.LENGTH_SHORT).show();
                }
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_EXTERNAL_STORAGE_PERMISSION_RESULT);
            }

        } else {
            mIsRecording = true;
            findViewById(R.id.thumbnailRecyclerView).setVisibility(View.INVISIBLE);
            try {
                createVideoFileName();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Couldn't Create VideoFileName", Toast.LENGTH_SHORT).show();
            }
            startRecord();
            mMediaRecorder.start();
        }
    }

    private void setupMediaRecorder() throws IOException {
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mMediaRecorder.setOutputFile(mVideoFileName);
        mMediaRecorder.setVideoEncodingBitRate(1000000);
        mMediaRecorder.setVideoFrameRate(30);
        mMediaRecorder.setVideoSize(mVideoSize.getWidth(), mVideoSize.getHeight());
        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mMediaRecorder.setOrientationHint(mTotalRotation);
        mMediaRecorder.prepare();
    }

    private void lockFocus() {
        mCaptureState = STATE_WAIT_LOCK;
        mCaptureRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CaptureRequest.CONTROL_AF_TRIGGER_START);
        try {
            mPreviewCaptureSession.capture(mCaptureRequestBuilder.build(), mPreviewCaptureCallback, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public void switchCamera() {
        if (cameraId.equals(CAMERA_FRONT)) {
            cameraId = CAMERA_BACK;
            closeCamera();
            reopenCamera();
        } else if (cameraId.equals(CAMERA_BACK)) {
            cameraId = CAMERA_FRONT;
            closeCamera();
            reopenCamera();
        }
    }

    public void reopenCamera() {
        if (mTextureView.isAvailable()) {
            setupCamera(mTextureView.getWidth(), mTextureView.getHeight());
            connectCamera();
        } else {
            mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        }
    }

    public String getPathFromURI(Uri contentUri) {
        String res = null;
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            res = cursor.getString(column_index);
        }
        if (cursor != null)
            cursor.close();

        return res;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Uri selectedMediaUri = data.getData();
        switch (requestCode) {
            case REQUEST_MEDIA_FROM_GALLERY:
                if (resultCode == RESULT_OK) {
                    ContentResolver cr = this.getContentResolver();
                    if (selectedMediaUri != null) {
                        if (Objects.requireNonNull(cr.getType(selectedMediaUri)).startsWith("image")) {
                            // start intent to full screen image viewer
                            Intent fullScreenIntent = new Intent(this, FullScreenImageActivity.class);
                            if (getIntent().getExtras() != null)
                                fullScreenIntent.putExtra("chat", "chat");
                            fullScreenIntent.setData(selectedMediaUri);
                            startActivity(fullScreenIntent);
                        }
                        if (Objects.requireNonNull(cr.getType(selectedMediaUri)).startsWith("video")) {
                            //open video trimmer activity for activity get result
                            startTrimActivity(selectedMediaUri);
                        }

                    }
                }
                break;
            case REQUEST_VIDEO_TRIMMER:
                if (resultCode == RESULT_OK) {
                    Uri videoUri = Uri.parse(data.getStringExtra("result"));
                    Intent fullScreenIntent = new Intent(getApplicationContext(), VideoPlayActivity.class);
                    if (getIntent().getExtras() != null)
                        fullScreenIntent.putExtra("chat", "chat");
                    fullScreenIntent.setData(videoUri);
                    startActivity(fullScreenIntent);
                }

        }

    }

    private void startTrimActivity(@NonNull Uri uri) {
        Intent intent = new Intent(this, VideoTrimmerActivity.class);
        intent.putExtra(EXTRA_VIDEO_PATH, getPathFromURI(uri));
        startActivityForResult(intent, REQUEST_VIDEO_TRIMMER);
    }

}
