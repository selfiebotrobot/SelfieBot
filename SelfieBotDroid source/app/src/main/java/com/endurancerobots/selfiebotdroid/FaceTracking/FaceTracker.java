package com.endurancerobots.selfiebotdroid.FaceTracking;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

/**
 * Face tracker for each detected individual. This maintains a face graphic within the app's
 * associated face overlay.
 */
public class FaceTracker extends Tracker<Face> {
    public interface IFaceUpdateHandler{
        void HandleFaceUpdate(FaceTracker f);
    }
    public static MultiProcessor.Factory<Face> getFactory(final IFaceUpdateHandler handler, final GraphicOverlay overlay) {
        return new MultiProcessor.Factory<Face>() {
            @Override
            public Tracker<Face> create(Face face) {
                return new FaceTracker(overlay, handler);
            }
        };
    }

    private static final float FACE_POSITION_RADIUS = 10.0f;
    private static final float ID_TEXT_SIZE = 40.0f;
    private static final float ID_Y_OFFSET = 50.0f;
    private static final float ID_X_OFFSET = -50.0f;
    private static final float BOX_STROKE_WIDTH = 5.0f;

    private static final int COLOR_CHOICES[] = { Color.BLUE, Color.CYAN, Color.GREEN, Color.MAGENTA, Color.RED, Color.WHITE, Color.YELLOW };
    private static int mCurrentColorIndex = 0;

    private Paint mFacePositionPaint;
    private Paint mIdPaint;
    private Paint mBoxPaint;

    private GraphicOverlay mOverlay;
    private volatile Face mFace;
    private int mFaceId;
    private IFaceUpdateHandler mFaceUpdateHandler;

    public float scaleX(float horizontal) { return horizontal * mOverlay.getWidthScaleFactor(); }
    public float scaleY(float vertical) { return vertical * mOverlay.getHeightScaleFactor(); }
    public float translateX(float x) {
        if (mOverlay.getFacing() == CameraSource.CAMERA_FACING_FRONT) {
            return mOverlay.getWidth() - scaleX(x);
        } else {
            return scaleX(x);
        }
    }
    public float translateY(float y) { return scaleY(y); }
    public void postInvalidate() { mOverlay.postInvalidate(); }

    FaceTracker(GraphicOverlay overlay, IFaceUpdateHandler faceUpdateHandler) {
        mOverlay = overlay;
        mFaceUpdateHandler = faceUpdateHandler;
        mCurrentColorIndex = (mCurrentColorIndex + 1) % COLOR_CHOICES.length;
        final int selectedColor = COLOR_CHOICES[mCurrentColorIndex];

        mFacePositionPaint = new Paint();
        mFacePositionPaint.setColor(selectedColor);

        mIdPaint = new Paint();
        mIdPaint.setColor(selectedColor);
        mIdPaint.setTextSize(ID_TEXT_SIZE);

        mBoxPaint = new Paint();
        mBoxPaint.setColor(selectedColor);
        mBoxPaint.setStyle(Paint.Style.STROKE);
        mBoxPaint.setStrokeWidth(BOX_STROKE_WIDTH);
    }
    public Face getFace() { return mFace;}
    public int getId() { return mFaceId; }
    /**
     * Start tracking the detected face instance within the face overlay.
     */
    @Override
    public void onNewItem(int faceId, Face item) {
        mFaceId=faceId;
    }
    /**
     * Update the position/characteristics of the face within the overlay.
     */
    @Override
    public void onUpdate(FaceDetector.Detections<Face> detectionResults, Face face) {
        mOverlay.add(this);
        mFace = face;
        postInvalidate();
        mFaceUpdateHandler.HandleFaceUpdate(this);
    }
    /**
     * Hide the graphic when the corresponding face was not detected.  This can happen for
     * intermediate frames temporarily (e.g., if the face was momentarily blocked from
     * view).
     */
    @Override
    public void onMissing(FaceDetector.Detections<Face> detectionResults) {
        mOverlay.remove(this);
        mFaceUpdateHandler.HandleFaceUpdate(this);
    }
    /**
     * Called when the face is assumed to be gone for good. Remove the graphic annotation from
     * the overlay.
     */
    @Override
    public void onDone() {
        mOverlay.remove(this);
        mFaceUpdateHandler.HandleFaceUpdate(this);
    }
    public double getX()
    {
        if(mFace==null) return 0;
        return translateX(mFace.getPosition().x + mFace.getWidth() / 2);
    }
    public double getY()
    {
        if(mFace==null) return 0;
        return translateY(mFace.getPosition().y + mFace.getHeight() / 2);
    }
    public GraphicOverlay getOverlay() { return mOverlay; }
    public void draw(Canvas canvas) {
        Face face = mFace;
        if (face == null) {
            return;
        }

        // Draws a circle at the position of the detected face, with the face's track id below.
        float x = translateX(face.getPosition().x + face.getWidth() / 2);
        float y = translateY(face.getPosition().y + face.getHeight() / 2);
        canvas.drawCircle(x, y, FACE_POSITION_RADIUS, mFacePositionPaint);
        canvas.drawText("id: " + mFaceId, x + ID_X_OFFSET, y + ID_Y_OFFSET, mIdPaint);

        // Draws a bounding box around the face.
        float xOffset = scaleX(face.getWidth() / 2.0f);
        float yOffset = scaleY(face.getHeight() / 2.0f);
        float left = x - xOffset;
        float top = y - yOffset;
        float right = x + xOffset;
        float bottom = y + yOffset;
        canvas.drawRect(left, top, right, bottom, mBoxPaint);

    }
}
