package com.geraud.android.gps1.Camera;

import android.Manifest;
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
import android.os.SystemClock;
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
import android.widget.Chronometer;
import android.widget.ProgressBar;
import android.widget.Toast;

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
import java.util.Timer;
import java.util.TimerTask;

public class CameraActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>,
        MediaStoreAdapter.OnClickThumbListener {

    private final static int READ_EXTERNAL_STORAGE_PERMISSION_RESULT = 0;
    private final static int WRITE_EXTERNAL_STORAGE_PERMISSION_RESULT = 1;
    private final static int REQUEST_CAMERA_PERMISSION_RESULT = 2;
    private final static int REQUEST_GET_IMAGE_FROM_GALLERY = 3;
    private final static int MEDIASTORE_LOADER_ID = 0;
    private static final int STATE_PREVIEW = 0;
    private static final int STATE_WAIT_LOCK = 1;

    public static final String CAMERA_FRONT = "1";
    public static final String CAMERA_BACK = "0";
    private String cameraId = CAMERA_FRONT;

    private boolean isFlashSupported;
    private boolean isTorchOn;

    private static final int MIN_CLICK_DURATION = 600;
    private long startClickTime;
    private boolean longClickActive;
    private boolean recording, pause = false;
    private long elapsed;
    private long remaningSecs = 0;
    private long elapsedSecs = 0;
    private Timer timer;

    private int mCaptureState = STATE_PREVIEW;
    private RecyclerView mThumbnailRecyclerView;
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
    private String mCameraId;
    private Size mPreviewSize;
    private Size mVideoSize;
    private Size mImageSize;
    private ImageReader mImageReader;
    private ImageReader.OnImageAvailableListener mOnImageAvailableListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            mBackgroundHandler.post(new ImageSaver(reader.acquireLatestImage()));
        }
    };

    private class ImageSaver implements Runnable {
        private final Image mImage;

        public ImageSaver(Image image) {
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

                Intent mediaStoreUpdateIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                mediaStoreUpdateIntent.setData(Uri.fromFile(new File(mImageFileName))); // uri of the file can use it to send to other activity
                sendBroadcast(mediaStoreUpdateIntent);

                //if this activity was opened from another one open find user to send it to
                //intent to the image full screen activity
                Intent fullScreenIntent = new Intent(getApplicationContext(), FullScreenImageActivity.class);
                if (getIntent().getExtras() != null)
                    fullScreenIntent.putExtra("chat","chatsActivity");
                fullScreenIntent.setData(Uri.fromFile(new File(mImageFileName)));
                startActivity(fullScreenIntent);

                if (fileOutputStream != null) {
                    try {
                        fileOutputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
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
                    if (afstate == CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED || afstate == CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED) {
                        Toast.makeText(getApplicationContext(), "auto focus locked", Toast.LENGTH_SHORT).show();
                        startStillCaptureRequest();
                    }
                    break;
            }
        }

        @Override
        public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
            process(result);
        }
    };
    private CaptureRequest.Builder mCaptureRequestBuilder;

    private Button mOpenGalleryButton;
    private ProgressBar mProgressBar;
    private boolean mIsRecording = false;

    private File mImageFolder;
    private String mImageFileName;
    private File mVideoFolder;
    private String mVideoFileName;
    private CameraDevice.StateCallback mCameraDeviceStateCallBack = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            mCameraDevice = camera;
            if (mIsRecording) {
                try {
                    createVideoFileName();
                    mMediaRecorder.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                startRecord();
                mMediaRecorder.start();
//                mChronometer.setBase(SystemClock.elapsedRealtime());
//                mChronometer.setVisibility(View.VISIBLE);
//                mChronometer.start();
            } else
                startPreview();
            //Toast.makeText(getApplicationContext(),"Connected To The Camera",Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onDisconnected(CameraDevice camera) {
            camera.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(CameraDevice camera, int error) {

        }
    };

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

        Button mCloseCamera = findViewById(R.id.closeCameraButton);
        mCloseCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeCamera();
                finish();
            }
        });

        Button mSwitchCamera = findViewById(R.id.orientationButton);
        mSwitchCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchCamera();
            }
        });

        Button mSwitchFlash = findViewById(R.id.flashButon);
        mSwitchFlash.setOnClickListener(new View.OnClickListener() {
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
            @Override
            public boolean onLongClick(View v) {
                try {
                    mProgressBar.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            final long INTERVAL = 1000;
                            final long TIMEOUT = 10000;
                            switch (event.getAction()) {
                                case MotionEvent.ACTION_DOWN:
                                    Log.i("ACTION_DOWN", "ACTION_DOWN::" + pause + " " + mIsRecording);
                                    if (longClickActive == false) {
                                        findViewById(R.id.thumbnailRecyclerView).setVisibility(View.INVISIBLE);
                                        longClickActive = true;
                                        startClickTime = Calendar.getInstance().getTimeInMillis();
                                    }
                                    break;
                                case MotionEvent.ACTION_MOVE:
                                    if (longClickActive == true) {
                                        long clickDuration = Calendar.getInstance().getTimeInMillis() - startClickTime;
                                        if (clickDuration >= MIN_CLICK_DURATION) {
                                            longClickActive = false;
                                            if (pause && !mIsRecording) {
                                                pause = false;
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

                                                recording = true;
                                                TimerTask task = new TimerTask() {
                                                    @Override
                                                    public void run() {
                                                        remaningSecs -= INTERVAL;

                                                        if (remaningSecs == 0) {
                                                            this.cancel();
                                                            try {
                                                                runOnUiThread(new Runnable() {
                                                                    @Override
                                                                    public void run() {
                                                                        recording = false;
                                                                        mIsRecording = false;
                                                                        // release the MediaRecorder object
                                                                        mMediaRecorder.stop();
                                                                        mMediaRecorder.reset();

                                                                        Intent mediaStoreUpdateIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                                                                        mediaStoreUpdateIntent.setData(Uri.fromFile(new File(mVideoFileName))); // uri of the file can use it to send t other activity
                                                                        sendBroadcast(mediaStoreUpdateIntent);

                                                                        Intent fullScreenIntent = new Intent(getApplicationContext(), VideoPlayActivity.class);
                                                                        if (getIntent().getExtras() != null)
                                                                            fullScreenIntent.putExtra("chat","chatsActivity");
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
//                                                        elapsedSecs = remaningSecs;
//                                                        mProgressBar.setProgress((int) (elapsedSecs / 1000));
                                                        Log.i("TIME camera video", "Milli::" + (remaningSecs / 1000));

                                                    }
                                                };
                                                timer = new Timer();
                                                timer.scheduleAtFixedRate(task, INTERVAL, INTERVAL);

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
                                                recording = true;
                                                TimerTask task = new TimerTask() {
                                                    @Override
                                                    public void run() {
                                                        elapsed += INTERVAL;
                                                        if (elapsed > TIMEOUT) {
                                                            this.cancel();
                                                            try {
                                                                runOnUiThread(new Runnable() {
                                                                    @Override
                                                                    public void run() {
                                                                        recording = false;
                                                                        mIsRecording = false;
                                                                        mMediaRecorder.stop();
                                                                        mMediaRecorder.reset(); // release the MediaRecorder object
                                                                        mProgressBar.setProgress(0);

                                                                        Intent mediaStoreUpdateIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                                                                        mediaStoreUpdateIntent.setData(Uri.fromFile(new File(mVideoFileName))); // uri of the file can use it to send t other activity
                                                                        sendBroadcast(mediaStoreUpdateIntent);

                                                                        Intent fullScreenIntent = new Intent(getApplicationContext(), VideoPlayActivity.class);
                                                                        if (getIntent().getExtras() != null)
                                                                            fullScreenIntent.putExtra("chat","chat");
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
                                                        elapsedSecs = 10000 - elapsed;
                                                        //mProgressBar.setProgress((int) (elapsedSecs / 1000));
                                                        Log.i("Time elapsed", "Milli::" + (elapsedSecs / 1000));
                                                        remaningSecs = elapsedSecs;
                                                        remaningSecs = Math.round(remaningSecs);
                                                        elapsedSecs = Math.round(elapsedSecs);
                                                    }
                                                };
                                                timer = new Timer();
                                                timer.scheduleAtFixedRate(task, INTERVAL, INTERVAL);
                                            }
                                        }
                                    }
                                    break;
                                case MotionEvent.ACTION_UP:
                                    Log.i("ACTION_UP", "ACTION_UP::" + recording);
                                    longClickActive = false;
                                    if (recording) {
                                        // stop recording and release camera
                                        timer.cancel();
                                        pause = true;
                                        mIsRecording = false;
                                        remaningSecs = remaningSecs + 1000;
                                        mMediaRecorder.stop();
                                        mMediaRecorder.reset(); // release the MediaRecorder object

                                        Intent mediaStoreUpdateIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                                        mediaStoreUpdateIntent.setData(Uri.fromFile(new File(mVideoFileName))); // uri of the file can use it to send t other activity
                                        sendBroadcast(mediaStoreUpdateIntent);

                                        Intent fullScreenIntent = new Intent(getApplicationContext(), VideoPlayActivity.class);
                                        if (getIntent().getExtras() != null)
                                            fullScreenIntent.putExtra("chat","chat");
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

        mOpenGalleryButton = findViewById(R.id.galleryButton);
        mOpenGalleryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        mThumbnailRecyclerView = findViewById(R.id.thumbnailRecyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mThumbnailRecyclerView.setLayoutManager(linearLayoutManager);
        mMediaStoreAdapter = new MediaStoreAdapter(this);
        mThumbnailRecyclerView.setAdapter(mMediaStoreAdapter);

        checkReadExternalStoragePermission();
    }

    private void setupCamera(int width, int height) {
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId);

            //setup flash
            Boolean available = cameraCharacteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
            isFlashSupported = available == null ? false : available;
            setupFlashButton();

            StreamConfigurationMap map = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            int deviceOrientation = getWindowManager().getDefaultDisplay().getRotation();
            mTotalRotation = sensorToDeviceRotation(cameraCharacteristics, deviceOrientation);
            boolean swapRotation = mTotalRotation == 90 || mTotalRotation == 270;
            int rotatedWidth = width;
            int rotatedHeight = height;

            if (swapRotation) {
                rotatedHeight = width;
                rotatedWidth = height;
            }
            mPreviewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class), rotatedWidth, rotatedHeight);
            mVideoSize = chooseOptimalSize(map.getOutputSizes(MediaRecorder.class), rotatedWidth, rotatedHeight);
            mImageSize = chooseOptimalSize(map.getOutputSizes(ImageFormat.JPEG), rotatedWidth, rotatedHeight);
            mImageReader = ImageReader.newInstance(mImageSize.getWidth(), mImageSize.getHeight(), ImageFormat.JPEG, 1);
            mImageReader.setOnImageAvailableListener(mOnImageAvailableListener, mBackgroundHandler);
            mCameraId = cameraId;
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void connectCamera() {
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
                        PackageManager.PERMISSION_GRANTED) {
                    cameraManager.openCamera(mCameraId, mCameraDeviceStateCallBack, mBackgroundHandler);
                } else {
                    if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                        Toast.makeText(this, "Video App Requires Access To Camera", Toast.LENGTH_SHORT).show();
                    }
                    requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO}, REQUEST_CAMERA_PERMISSION_RESULT);
                }
            } else {
                cameraManager.openCamera(mCameraId, mCameraDeviceStateCallBack, mBackgroundHandler);
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public void switchFlash() {
        Button flashButton = findViewById(R.id.flashButon);
        try {
            if (cameraId.equals(CAMERA_BACK)) {
                if (isFlashSupported) {
                    if (isTorchOn) {
                        mCaptureRequestBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_OFF);
                        mPreviewCaptureSession.setRepeatingRequest(mCaptureRequestBuilder.build(), null, null);
                        flashButton.setBackgroundColor(Color.GRAY);
                        isTorchOn = false;
                        flashButton.setBackground(getResources().getDrawable(R.drawable.flash_off));
                    } else {
                        mCaptureRequestBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_TORCH);
                        mPreviewCaptureSession.setRepeatingRequest(mCaptureRequestBuilder.build(), null, null);
                        flashButton.setBackgroundColor(Color.WHITE);
                        isTorchOn = true;
                        flashButton.setBackground(getResources().getDrawable(R.drawable.flash_on));
                    }
                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }


    public void setupFlashButton() {
        Button flashButton = findViewById(R.id.flashButon);
        if (cameraId.equals(CAMERA_BACK) && isFlashSupported) {
            flashButton.setVisibility(View.VISIBLE);

            if (isTorchOn) {
                flashButton.setBackground(getResources().getDrawable(R.drawable.flash_on));
            } else {
                flashButton.setBackground(getResources().getDrawable(R.drawable.flash_off));
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
                            public void onConfigured(CameraCaptureSession session) {
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
                            public void onConfigureFailed(CameraCaptureSession session) {

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

            mCameraDevice.createCaptureSession(Arrays.asList(previewSurface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession session) {
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
                public void onConfigureFailed(CameraCaptureSession session) {
                    Toast.makeText(getApplicationContext(), "Unable To setup CameraPreview", Toast.LENGTH_SHORT).show();
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
                public void onCaptureStarted(CameraCaptureSession session, CaptureRequest request, long timestamp, long frameNumber) {
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
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_GET_IMAGE_FROM_GALLERY);
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

    private void checkWriteExternalStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                mIsRecording = true;
                try {
                    createVideoFileName();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                startRecord();
                mMediaRecorder.start();
            } else {
                if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    //
                }
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_EXTERNAL_STORAGE_PERMISSION_RESULT);
            }

        } else {
            mIsRecording = true;
            try {
                createVideoFileName();
            } catch (IOException e) {
                e.printStackTrace();
            }
            startRecord();
            mMediaRecorder.start();
//            mChronometer.setBase(SystemClock.elapsedRealtime());
//            mChronometer.setVisibility(View.VISIBLE);
//            mChronometer.start();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case READ_EXTERNAL_STORAGE_PERMISSION_RESULT:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //call cursor loader
                    getSupportLoaderManager().initLoader(MEDIASTORE_LOADER_ID, null, this);
                }
                break;
            case REQUEST_CAMERA_PERMISSION_RESULT:
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(), "Application Cant run without camera", Toast.LENGTH_SHORT).show();
                }
                if (grantResults[1] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(), "Application Cant run without audio mic", Toast.LENGTH_SHORT).show();
                }
            case WRITE_EXTERNAL_STORAGE_PERMISSION_RESULT:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mIsRecording = true;
                    try {
                        createVideoFileName();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Application Needs To Save Video ", Toast.LENGTH_SHORT).show();
                }

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

        String selection = MediaStore.Files.FileColumns.MEDIA_TYPE + "="
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
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        mMediaStoreAdapter.changeCursor(cursor);
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
        mBackgroundHandlerThread = new HandlerThread("Camera2VideoImage");
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
        }
    }

    private static int sensorToDeviceRotation(CameraCharacteristics cameraCharacteristics, int deviceOrientation) {
        int sensorOrientation = cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
        deviceOrientation = ORIENTATIONS.get(deviceOrientation);
        return (sensorOrientation + deviceOrientation + 360) % 360;
    }

    private static Size chooseOptimalSize(Size[] choices, int width, int height) {
        List<Size> bigEnought = new ArrayList<Size>();
        for (Size option : choices) {
            if (option.getHeight() == option.getWidth() * height / width && option.getWidth() >= width && option.getHeight() >= height) { //aspect ration idk
                bigEnought.add(option);
            }
        }
        if (bigEnought.size() > 0) {
            return Collections.min(bigEnought, new CompareSizeByArea());
        } else
            return choices[0];
    }

    private void createVideoFolder() {
        File movieFile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
        mVideoFolder = new File(movieFile, "GPS-1");
        if (!mVideoFolder.exists()) {
            mVideoFolder.mkdirs();
        }
    }

    private File createVideoFileName() throws IOException {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String prepend = "VIDEO_" + timestamp + "_";
        File videoFile = File.createTempFile(prepend, ".mp4", mVideoFolder);
        mVideoFileName = videoFile.getAbsolutePath();
        return videoFile;
    }

    private void createImageFolder() {
        File imageFile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        mImageFolder = new File(imageFile, "GPS-1");
        if (!mImageFolder.exists()) {
            mImageFolder.mkdirs();
        }
    }

    private File createImageFileName() throws IOException {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String prepend = "IMAGE_" + timestamp + "_";
        File imageFile = File.createTempFile(prepend, ".jpg", mImageFolder);
        mImageFileName = imageFile.getAbsolutePath();
        return imageFile;
    }

    private void checkReadExternalStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // start cursor loader
                getSupportLoaderManager().initLoader(MEDIASTORE_LOADER_ID, null, this);
            } else {
                if (shouldShowRequestPermissionRationale(android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    Toast.makeText(this, "App Needs To View Thumbnails", Toast.LENGTH_SHORT).show();
                }
                requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, READ_EXTERNAL_STORAGE_PERMISSION_RESULT);
            }

        } else {
            // start cursor loader
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
        if (cursor.moveToFirst()) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            res = cursor.getString(column_index);
        }
        cursor.close();
        return res;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (resultCode == RESULT_OK) {
                if (requestCode == REQUEST_GET_IMAGE_FROM_GALLERY) {
                    Uri selectedImageUri = data.getData();
                    // Get the path from the Uri
                    final String path = getPathFromURI(selectedImageUri);
                    if (path != null) {
                        File f = new File(path);
                        selectedImageUri = Uri.fromFile(f);
                    }
                    // start intent to full screen image viewer
                    Intent fullScreenIntent = new Intent(this, FullScreenImageActivity.class);
                    fullScreenIntent.setData(selectedImageUri);
                    startActivity(fullScreenIntent);
                }
            }
        } catch (Exception e) {
            Log.e("FileSelectorActivity", "File select error", e);
        }
    }
}
