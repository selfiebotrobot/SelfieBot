package com.endurancerobots.selfiebotdroid;

public class Snippets {
/*

            YuvImage temp = new YuvImage(d, camera.getParameters().getPreviewFormat(), camera.getParameters().getPictureSize().width, camera.getParameters().getPictureSize().height, null);
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            temp.compressToJpeg(new Rect(0, 0, temp.getWidth(), temp.getHeight()), 80, os);

            preview = BitmapFactory.decodeByteArray(os.toByteArray(), 0, os.toByteArray().length);
            //preview = Bitmap.createScaledBitmap(preview,preview.getWidth()/3,preview.getHeight()/3,false);
            Matrix matrix = new Matrix();
            matrix.postRotate(-90);
            matrix.postScale(-0.3f,0.3f);
            preview = Bitmap.createBitmap(preview, 0, 0, preview.getWidth(), preview.getHeight(), matrix, true);
*/
                /*
                if (motionLevel > threshold) {
                    motionBitmap = ImageProcessing.rotate(ImageProcessing.rgbToBitmap(img, width, height),-displayRotation);
                }
                */
    /*
    canvas.drawBitmap(motionBitmap,
            new Rect(0,0, motionBitmap.getWidth(),motionBitmap.getHeight()),
            new Rect(0,0,canvas.getWidth(), canvas.getHeight()),null);
*/
                /*
                canvas.drawText("TOP", (int) (canvas.getWidth() / 2.5), 100, paint);
                canvas.drawText("LEFT", 10, canvas.getHeight() / 2, paint);
                canvas.drawText("BOTTOM", (int) (canvas.getWidth() / 2.5), canvas.getHeight() - 270, paint);
                canvas.drawText("RIGHT", (int) (canvas.getWidth() * 2 / 3), canvas.getHeight() / 2, paint);
                */
    /*

                if (img != null && detector.detect(img, width, height)) {
                    long now = System.currentTimeMillis();
                    if (now > (mReferenceTime + 100)) {
                        mReferenceTime = now;
                        motionBitmap = null;
                        motionBitmap= ImageProcessing.rgbToBitmap(img, width, height);
                        Matrix matrix = new Matrix();
                        matrix.postRotate(-90);
                        matrix.postScale(-1.0f,1.0f);
                        motionBitmap = Bitmap.createBitmap(motionBitmap, 0, 0, motionBitmap.getWidth(), motionBitmap.getHeight(), matrix, true);
                        invalidate();
                    } else {
                        Log.i(TAG, "Not taking picture because not enough time has passed since the creation of the Surface");
                    }
                }
                */

                     /*   Matrix matrix = new Matrix();
                    matrix.postRotate(-displayRotation);
                    matrix.postScale(-1.0f,1.0f);
                    motionBitmap = Bitmap.createBitmap(motionBitmap, 0, 0, motionBitmap.getWidth(), motionBitmap.getHeight(), matrix, true);
*/
/*
    private final class DetectionThread extends Thread {

        private byte[] data;
        private int width;
        private int height;

        public DetectionThread(byte[] data, int width, int height) {
            this.data = data;
            this.width = width;
            this.height = height;
        }

        @Override
        public void run() {
        }
    }
    */
    /*
        mMyView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final float k = (float) 2.5;
                final float X = event.getRawX() / mLayoutParams.width / k;
                final float Y = event.getRawY() / mLayoutParams.height / k;

                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
                        mXDelta = X - (mLayoutParams.horizontalMargin);
                        mYDelta = Y - (mLayoutParams.verticalMargin);
                        Log.d(TAG, "ACTION_DOWN X=" + X + " Y=" + Y + " mXDelta=" + mXDelta + " mYDelta=" + mYDelta +
                                " lP(" + mLayoutParams.horizontalMargin + ", " + mLayoutParams.verticalMargin + ")");
                        break;
                    case MotionEvent.ACTION_UP:
                        Log.d(TAG, "ACTION_UP X=" + X + " Y=" + Y + " mXDelta=" + mXDelta + " mYDelta=" + mYDelta +
                                " lP(" + mLayoutParams.horizontalMargin + ", " + mLayoutParams.verticalMargin + ")");
                        break;
                    case MotionEvent.ACTION_POINTER_DOWN:
                        Log.v(TAG, "ACTION_POINTER_DOWN");
                        break;
                    case MotionEvent.ACTION_POINTER_UP:
                        Log.v(TAG, "ACTION_POINTER_UP");
                        break;
                    case MotionEvent.ACTION_MOVE:
                        Log.v(TAG, "ACTION_MOVE");
                        mLayoutParams.horizontalMargin = (X - mXDelta);
                        mLayoutParams.verticalMargin = (Y - mYDelta);
                        break;
                }
                mWinMgr.updateViewLayout(mMyView, mLayoutParams);
                return true;
            }
        });
*/
}
