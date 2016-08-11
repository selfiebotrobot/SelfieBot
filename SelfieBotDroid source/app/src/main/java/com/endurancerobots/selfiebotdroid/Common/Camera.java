package com.endurancerobots.selfiebotdroid.Common;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

public class Camera {
    private static final String TAG="[[Camera]]";
    public static final int RC_HANDLE_GMS = 9001;
    public static final int RC_HANDLE_CAMERA_PERM = 2;
    public static boolean isCameraReady(Activity a)
    {
        int rc = ActivityCompat.checkSelfPermission(a, Manifest.permission.CAMERA);
        if (rc == PackageManager.PERMISSION_GRANTED) { return true; }
        requestCameraPermission(a);
        return false;
    }
    private static void requestCameraPermission(Activity a) {
        Log.w(TAG, "Camera permission is not granted. Requesting permission");
        final String[] permissions = new String[]{Manifest.permission.CAMERA};
        if (!ActivityCompat.shouldShowRequestPermissionRationale(a, Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(a, permissions, RC_HANDLE_CAMERA_PERM);
            return;
        }
        ActivityCompat.requestPermissions(a, permissions, RC_HANDLE_CAMERA_PERM);
    }
}
