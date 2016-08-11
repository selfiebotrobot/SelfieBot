package com.endurancerobots.selfiebotdroid.FaceTracking;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

import com.google.android.gms.vision.CameraSource;

import java.util.HashSet;
import java.util.Set;

public class GraphicOverlay extends View {
    private final Object mLock = new Object();
    private int mPreviewWidth;
    private int mPreviewHeight;
    private float mWidthScaleFactor = 1.0f;
    public float getWidthScaleFactor() { return mWidthScaleFactor;}
    private float mHeightScaleFactor = 1.0f;
    public float getHeightScaleFactor() { return mHeightScaleFactor;}
    private int mFacing = CameraSource.CAMERA_FACING_BACK;
    public int getFacing() {return mFacing;}
    private Set<FaceTracker> mGraphics = new HashSet<>();
    public FaceTracker getMinIdFace()
    {
        FaceTracker res=null;
        for (FaceTracker fg:mGraphics) {
            if(res==null || res.getId()>fg.getId())
            {
                res=fg;
            }
        }
        return res;
    }
    public Set<FaceTracker> getFaceGraphics () { return mGraphics;}
    public int getNumFaces()
    {
        return mGraphics.size();
    }
    public GraphicOverlay(Context context) { super(context); }
    public void clear() {
        synchronized (mLock) {
            mGraphics.clear();
        }
        postInvalidate();
    }
    public void add(FaceTracker graphic) {
        synchronized (mLock) {
            mGraphics.add(graphic);
        }
        postInvalidate();
    }
    public void remove(FaceTracker graphic) {
        synchronized (mLock) {
            mGraphics.remove(graphic);
        }
        postInvalidate();
    }
    public void setCameraInfo(int previewWidth, int previewHeight, int facing) {
        synchronized (mLock) {
            mPreviewWidth = previewWidth;
            mPreviewHeight = previewHeight;
            mFacing = facing;
        }
        postInvalidate();
    }
   @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        synchronized (mLock) {
            if ((mPreviewWidth != 0) && (mPreviewHeight != 0)) {
                mWidthScaleFactor = (float) canvas.getWidth() / (float) mPreviewWidth;
                mHeightScaleFactor = (float) canvas.getHeight() / (float) mPreviewHeight;
            }

            for (FaceTracker graphic : mGraphics) {
                graphic.draw(canvas);
            }
        }
    }
}