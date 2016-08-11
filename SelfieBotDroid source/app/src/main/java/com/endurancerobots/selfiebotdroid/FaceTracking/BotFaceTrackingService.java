package com.endurancerobots.selfiebotdroid.FaceTracking;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.TextView;

import com.endurancerobots.selfiebotdroid.Common.Commands;
import com.endurancerobots.selfiebotdroid.Common.Global;
import com.endurancerobots.selfiebotdroid.Common.Messages;
import com.endurancerobots.selfiebotdroid.R;
import com.endurancerobots.selfiebotdroid.Service.BotBtService;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.face.FaceDetector;

import java.io.IOException;

public class BotFaceTrackingService extends BotBtService {
    protected static int mInstanceCount =0;
    private long lastCommandTime=0;
    private static final long DEVIATION_THRESHOLD =15;
    private static final long COMMAND_TIMEOUT=750;
    private CameraSource mCameraSource = null;
    private CameraSourcePreview mPreview;
    private GraphicOverlay mGraphicOverlay;
    private static final int WIDTH=480, HEIGHT=640, MARGIN=20;
    private TextView lbInfo;
    private byte[] mLastCMD;

    @Override
    public void onCreate() {
        TAG = "[[BotMotionDetectionService]]";
        if (mInstanceCount < 1) { mInstanceCount++; super.onCreate(); } else {
            Log.w(TAG, "Should be only one server");
            Global.publishProgress(this, "More than one server service created. Stopping!!!");
            stopSelf();
        }
        mPreview=new CameraSourcePreview(this);
        mGraphicOverlay=new GraphicOverlay(this);
        lbInfo=new TextView(this);
        lbInfo.setText("FACE TRACKER");
        lbInfo.setTextColor(Color.MAGENTA);
        createCameraSource();
    }
    private void createCameraSource() {
        Context context = getApplicationContext();
        FaceDetector detector = new FaceDetector.Builder(context).setClassificationType(FaceDetector.ALL_CLASSIFICATIONS).build();
        detector.setProcessor(new MultiProcessor.Builder<>(FaceTracker.getFactory(
                new FaceTracker.IFaceUpdateHandler() {
                    @Override
                    public void HandleFaceUpdate(FaceTracker f) {
                        updateLabel(f);
                    }
                },
                mGraphicOverlay)).build());

        if (!detector.isOperational()) {
            // Note: The first time that an app using face API is installed on a device, GMS will
            // download a native library to the device in order to do detection.  Usually this
            // completes before the app is run for the first time.  But if that download has not yet
            // completed, then the above call will not detect any faces.
            // isOperational() can be used to check if the required native library is currently
            // available.  The detector will automatically become operational once the library
            // download completes on device.
            Global.toast(this, "Face detector dependencies are not yet available.");
        }
        mCameraSource = new CameraSource.Builder(context, detector).setRequestedPreviewSize(640, 480)
                .setFacing(CameraSource.CAMERA_FACING_FRONT).setRequestedFps(30.0f).build();
        //todo: allow switching cameras
    }
    @Override
    protected void handleBtSocketOpen() {
        setState(Messages.BOT_SERVICE_STATE_CONNECTED);
        Global.publishProgress(this, getString(R.string.msg_successful_connection));
        mUI.add(mPreview, Gravity.BOTTOM | Gravity.RIGHT, WIDTH, HEIGHT, MARGIN);
        mUI.add(mGraphicOverlay, Gravity.BOTTOM | Gravity.RIGHT,WIDTH,HEIGHT,MARGIN);
        mUI.add(lbInfo, Gravity.CENTER, WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,0);
        /**
         * Starts or restarts the camera source, if it exists.  If the camera source doesn't exist yet
         * (e.g., because onResume was called before the camera source was created), this will be called
         * again when the camera source is created.
         */
        // check that the device has play services available.
        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(getApplicationContext());
        if (code != ConnectionResult.SUCCESS) {
            Global.toast(this, GoogleApiAvailability.getInstance().getErrorString(code));
            //GoogleApiAvailability.getInstance().getErrorDialog(this, code, Camera.RC_HANDLE_GMS).show();
            disconnect();
        }
        if (mCameraSource != null) {
            try {
                mPreview.start(mCameraSource, mGraphicOverlay);
            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source.", e);
                mCameraSource.release();
                mCameraSource = null;
            }
        }
    }
    private void updateLabel(FaceTracker fg) {
        int numFaces = mGraphicOverlay.getNumFaces();
        FaceTracker f = mGraphicOverlay.getMinIdFace();
        String s = "# faces " + numFaces + " \n";
        long now = System.currentTimeMillis();
        if(mLastCMD!=null && (now < (lastCommandTime + COMMAND_TIMEOUT)))
        {
            return;
        }
        lastCommandTime = now;
        byte[] cmd = null;
        if (f != null) {
            int xDeviation = (int) (100 * f.getX() / mGraphicOverlay.getWidth() - 50);
            int yDeviation = (int) (100 * f.getY() / mGraphicOverlay.getHeight() - 50);
            s += ((int) f.getX()) + ":" + ((int) f.getY()) + "\n";
            s += xDeviation + ":" + yDeviation + "\n";
            if (xDeviation < -DEVIATION_THRESHOLD) {
                cmd = Commands.RightMsg;
            } else if (xDeviation > DEVIATION_THRESHOLD) {
                cmd = Commands.LeftMsg;
            }
            if(yDeviation<-DEVIATION_THRESHOLD) { cmd=Commands.UpMsg; }
            else if (yDeviation>DEVIATION_THRESHOLD) { cmd=Commands.DownMsg; }
        }
        if (cmd == null) { //no face was found or it is in the center
            if (mLastCMD != null) {
                //stop moving
                cmd = Commands.StopMsg; //mLastCMD;
                mLastCMD = null;
            }
        } else {
            if (mLastCMD == null) {
                mLastCMD = cmd;
            } else if (Commands.controlByte(mLastCMD) == Commands.controlByte(cmd)) {
                //cmd = null; //no need to repeat command if its still the same
            } else {
                writeCmd(Commands.StopMsg);//mLastCMD); //otherwise write a stop command
                mLastCMD = cmd;
            }
        }
        if (cmd != null) {
            writeCmd(cmd);
        }
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
        mPreview.stop();
        if (mCameraSource != null) { mCameraSource.release(); }
        super.onDestroy();
    }
 }