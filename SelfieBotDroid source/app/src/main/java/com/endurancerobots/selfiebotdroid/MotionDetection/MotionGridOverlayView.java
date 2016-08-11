package com.endurancerobots.selfiebotdroid.MotionDetection;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.SurfaceView;

import com.endurancerobots.selfiebotdroid.Common.Graphics;

import java.util.concurrent.atomic.AtomicBoolean;

public class MotionGridOverlayView extends SurfaceView {
    private MotionDetector detector = null;
    private GridMotionAreaProcessing processor=null;
    private TwoFramesDifferenceDetector detectorAlgo=null;
    private static final float THRESHOLD =0.2f;
    private float motionLevel;
    private Paint paint = new Paint();
    private static final int GRID_SIZE=3;
    private static volatile AtomicBoolean processing = new AtomicBoolean(false);
    public int displayRotation=0;

    public MotionGridOverlayView(Context context) {
            super(context);
            // Create out paint to use for drawing
            paint.setARGB(255, 200, 0, 0);
            paint.setTextSize(60);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(5);
            setWillNotDraw(false);

            processor = new GridMotionAreaProcessing(GRID_SIZE, GRID_SIZE, false);
            detectorAlgo= new TwoFramesDifferenceDetector();
            detector=new MotionDetector(detectorAlgo,processor);

        }
    public void process(byte[] data,int width,int height) {
        if (!processing.compareAndSet(false, true)) return;
        try {
            int[] img = Graphics.decodeYUV420SPtoRGB(data, width, height);
            motionLevel = detector.ProcessFrame(width, height, img);
            invalidate();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            processing.set(false);
        }
        processing.set(false);
    }
    @Override
    protected void onDraw(Canvas canvas) {
        try {
            if (motionLevel >0) {
                float[][] grid = processor.MotionGrid;
                int gridHeight=processor.getGridHeight();
                int gridWidth=processor.getGridWidth();
                int wSize = canvas.getWidth() / gridWidth;
                int hSize = canvas.getHeight() / gridHeight;

                float maxValue = -1;
                int[] maxCoord=new int[2];

                for (int i = 0; i < grid.length; i++)
                {
                    for (int j = 0; j < grid[i].length; j++)
                    {
                        float val = grid[i][j];
                        if(val> THRESHOLD) {
                            if (maxValue < val) {
                                maxValue = val;
                                maxCoord[0] = j;
                                maxCoord[1] = i;
                            }
                            int v = 255 - (int) Math.min(255, 1000 * val);
                            int[] coords={j,i};
                            _adjustCoordinatesForRotation(coords,gridWidth,gridHeight);
                            _drawRect(canvas, coords, Color.argb(100, 0, 0, v),wSize,hSize);
                        }
                    }
                }
                if (maxValue > 0)
                {
                    _adjustCoordinatesForRotation(maxCoord,gridWidth,gridHeight);
                    _drawRect(canvas, maxCoord, Color.argb(100, 0, 255, 255),wSize,hSize);
                    /*
                    long now = System.currentTimeMillis();
                    if (now > (lastCommandTime + COMMAND_TIMEOUT)) {
                        lastCommandTime = now;

                        if (maxCoord[0] == 2)
                            ServoControlService.getInstance().writeToBluetooth(CmdGenerator.getInstance().LeftMsg);
                        else if (maxCoord[0] == 0)
                            ServoControlService.getInstance().writeToBluetooth(CmdGenerator.getInstance().RightMsg);
                        else if (maxCoord[1] == 2)
                            ServoControlService.getInstance().writeToBluetooth(CmdGenerator.getInstance().UpMsg);
                        else if (maxCoord[1] == 0)
                            ServoControlService.getInstance().writeToBluetooth(CmdGenerator.getInstance().DownMsg);
                        //sendBroadcast(new Intent()); //<-- we can use intents, we would then registerReceiver in Service and use putExtra to pass params
                    }
                    */
                }

                canvas.drawText("ROT:"+displayRotation,10,50,paint);
                canvas.drawText("max:"+ maxValue,10,170,paint);
                canvas.drawText("max:"+ maxCoord[0] +" : " +maxCoord[1],10,290,paint);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    private void _adjustCoordinatesForRotation(int[] coords,int gridWidth, int gridHeight)
    {
        if(displayRotation==0) {
            coords[0]=gridWidth-coords[0]-1;
        }
        else if(displayRotation==180)
        {
            //int zz=x; x=y; y=zz;
            coords[1]=gridWidth-coords[1]-1;
        }
        else if(displayRotation==90)
        {
            int zz=coords[0]; coords[0]=coords[1]; coords[1]=zz;
            coords[0]=gridWidth-coords[0]-1;
            coords[1]=gridHeight-coords[1]-1;
        }
    }
    private void _drawRect(Canvas c,int[] coords, int color, int wSize, int hSize)
    {
        int x=coords[0], y=coords[1];
        Paint p=new Paint();
        p.setColor(color);
        p.setStyle(Paint.Style.FILL);

        c.drawRect(x * wSize, y * hSize,
                (x+1) * wSize, (y+1) * hSize, p);
    }

}
