package com.example.lock;

import android.app.Activity;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends Activity {
    private Camera myCamera;
    private Camera.Parameters myParameters;
    private SurfaceHolder mSurfaceHolder;
    private SurfaceView mSurfaceview;
    private MediaRecorder mRecorder;
    private Handler mHandler = new Handler();


    private final String TAG = "mainAct";
    private long mLastTime;
    private boolean isRecording = false;
    private boolean isView = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initEvent();
        initData();
    }

    private void initView(){
        BrightnessHelper.setActivityBrightness(0f, MainActivity.this);
        mSurfaceview = findViewById(R.id.sv);
        SurfaceHolder holder = mSurfaceview.getHolder();
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        holder.addCallback(mCallback);
    }

    private void initEvent(){
        findViewById(R.id.tv_start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isRecording) return;
                long nowTime = System.currentTimeMillis();
                if(nowTime - mLastTime < 1000){
                    isRecording = true;
                    startRecord();
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            stopRecord();
                            isRecording = false;
                        }
                    }, 1000*15);
                }
                mLastTime = nowTime;
            }
        });
    }

    private void initData(){

    }

    @Override
    public void onDestroy(){
        BrightnessHelper.setActivityBrightness(-1f, MainActivity.this);
        mHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }


    private String getDate(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        return sdf.format(new Date());
    }

    public void initCamera()
    {
        if(myCamera == null && !isView)
        {
            myCamera = Camera.open();
            Log.i(TAG, "camera open");
        }
        if(myCamera != null && !isView) {
            try {
                myParameters = myCamera.getParameters();
                myParameters.setPreviewSize(1920, 1080);
                myParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
                myCamera.setParameters(myParameters);
                myCamera.setPreviewDisplay(mSurfaceHolder);
                myCamera.startPreview();
                isView = true;
            } catch (Exception e) {
                e.printStackTrace();
                Log.d(TAG, "init camera error:" + e.getMessage());
            }
        }
    }

    private SurfaceHolder.Callback mCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            mSurfaceHolder = holder;
            initCamera();
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            mSurfaceHolder = holder;
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            mSurfaceview = null;
            mSurfaceHolder = null;
            if (mRecorder != null) {
                mRecorder.release();
                mRecorder = null;
            }
        }
    };

    private void startRecord(){
        if (mRecorder == null) {
            mRecorder = new MediaRecorder(); // Create MediaRecorder
        }
        try {
            myCamera.unlock();
            mRecorder.setCamera(myCamera);
            // Set audio and video source and encoder
            // 这两项需要放在setOutputFormat之前
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
            mRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_720P));
            mRecorder.setPreviewDisplay(mSurfaceHolder.getSurface());

            // Set output file path
            String path = Environment.getExternalStorageDirectory().getPath();
            if (path != null) {

                File dir = new File(path + "/tlog");
                if (!dir.exists()) {
                    dir.mkdir();
                }
                path = dir + "/" + getDate() + "";
                mRecorder.setOutputFile(path);
                mRecorder.prepare();
                mRecorder.start();   // Recording is now started
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopRecord(){
        try {
            mRecorder.stop();
            mRecorder.reset();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
