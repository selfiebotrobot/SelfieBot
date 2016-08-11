package com.endurancerobots.selfiebotdroid.MotionDetection;

import android.content.Intent;
import android.graphics.Color;
import android.hardware.Camera;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.endurancerobots.selfiebotdroid.Common.Global;
import com.endurancerobots.selfiebotdroid.Common.Messages;
import com.endurancerobots.selfiebotdroid.R;
import com.endurancerobots.selfiebotdroid.Service.BotBtService;

public class BotMotionDetectionService extends BotBtService {
    protected static int mInstanceCount =0;

    private SurfaceView mPreviewSurface = null;
    private SurfaceHolder mPreviewSurfaceHolder = null;
    private Camera camera = null;
    private boolean inPreview = false;
    private MotionGridOverlayView mMotionGridView;
    private TextView lbInfo;

    private Camera.PreviewCallback mPreviewCallback = new Camera.PreviewCallback() {
        @Override
        public void onPreviewFrame(byte[] data, Camera cam) {
            if (data == null) return;
            Camera.Size size = cam.getParameters().getPreviewSize();
            if (size == null) return;
            mMotionGridView.process(data,size.width,size.height);
        }
    };
    private SurfaceHolder.Callback mSurfaceCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            try {
                camera.setPreviewDisplay(mPreviewSurfaceHolder);
                camera.setPreviewCallback(mPreviewCallback);
            } catch (Throwable t) {
                Log.e(TAG, "Exception in setPreviewDisplay()", t);
            }
        }
        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            if(camera!=null) {
                Camera.Parameters parameters = camera.getParameters();
                Camera.Size size = getBestPreviewSize(width, height, parameters);
                if (size != null) {
                    parameters.setPreviewSize(size.width, size.height);
                }
                camera.setParameters(parameters);
                camera.setDisplayOrientation(mMotionGridView.displayRotation);
                camera.startPreview();
                inPreview = true;
            }
        }
        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            // Ignore
        }
    };

    @Override
    public void onCreate() {
        TAG = "[[BotMotionDetectionService]]";
        if (mInstanceCount < 1) { mInstanceCount++; super.onCreate(); } else {
            Log.w(TAG, "Should be only one server");
            Global.publishProgress(this, "More than one server service created. Stopping!!!");
            stopSelf();
        }
        lbInfo=new TextView(this);
        lbInfo.setText("MOTION DETECTOR");
        lbInfo.setTextColor(Color.MAGENTA);

        mPreviewSurface = new SurfaceView(this);
        mPreviewSurface.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));
        mPreviewSurfaceHolder = mPreviewSurface.getHolder();
        mPreviewSurfaceHolder.addCallback(mSurfaceCallback);
        mPreviewSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mMotionGridView =new MotionGridOverlayView(this);
    }
    //todo : no need to override onStartCommand - we just do it for now to avoid BtConnect
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStart");
        if (intent != null) {
            final String action = intent.getAction();
            if (Messages.CMD_BOT_SERVICE_START.equals(action)) {
                startForeground(Global.NOTIFICATION_ID, Global.buildNotification(this, getString(R.string.msg_connecting_bt)));
                setState(Messages.BOT_SERVICE_STATE_CONNECTED);

                mUI.addBottom(mMotionGridView, Gravity.CENTER, WindowManager.LayoutParams.FILL_PARENT,
                        WindowManager.LayoutParams.FILL_PARENT, 0);
                mUI.addBottom(mPreviewSurface, Gravity.CENTER, WindowManager.LayoutParams.FILL_PARENT,
                        WindowManager.LayoutParams.FILL_PARENT, 0);
                mUI.add(lbInfo, Gravity.CENTER, WindowManager.LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.WRAP_CONTENT, 0);

                try {
                    camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
                    Display display = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
                    int orientation = display.getRotation();
                    //todo: dynamically change camera rotation, not just at start of service

                    if (orientation == Surface.ROTATION_90) { mMotionGridView.displayRotation=0; }
                    else if (orientation == Surface.ROTATION_270) { mMotionGridView.displayRotation = 180; }
                    else { mMotionGridView.displayRotation=90; }

                }
                catch (Exception ex)
                {
                    disconnect();
                }
            }
        }
        return START_NOT_STICKY;
    }
    @Override
    protected void handleBtSocketOpen() {
    }
    private Camera.Size getBestPreviewSize(int width, int height, Camera.Parameters parameters) {
        Camera.Size result = null;

        for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
            if (size.width <= width && size.height <= height) {
                if (result == null) {
                    result = size;
                } else {
                    int resultArea = result.width * result.height;
                    int newArea = size.width * size.height;

                    if (newArea > resultArea) result = size;
                }
            }
        }

        return result;
    }
    private void updateLabel(String s) {
        final String x = s;
        mUI.runOnUiThread(new Runnable() {
            public void run() {
                lbInfo.setText(x);
            }
        });
    }
    @Override
    public void onDestroy()
    {
        mInstanceCount--;
        if(mInstanceCount<0)
        {
            Log.e(TAG, "ERROR : NUMBER OF INSTANCES BELOW 0!");
        }
        camera.setPreviewCallback(null);
        if (inPreview) camera.stopPreview();
        inPreview = false;
        camera.release();
        camera = null;

        super.onDestroy();
    }
 }