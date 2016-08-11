package com.endurancerobots.selfiebotdroid.MotionDetection;


import com.endurancerobots.selfiebotdroid.Common.Graphics;

public class GridMotionAreaProcessing {
    // color used for highlighting motion grid
    public int HighlightColor = Graphics.getARGB(255, 255, 0, 0);
    // highlight motion grid or not
    public boolean HighlightMotionGrid = true;
    public float MotionAmountToHighlight = 0.15f;
    private int gridWidth = 16;
    private int gridHeight = 16;
    public float[][] MotionGrid = null;

    public int getGridWidth() {
        return gridWidth;
    }

    public void setGridWidth(int value) {
        gridWidth = Math.min(64, Math.max(2, value));
        MotionGrid = new float[gridHeight][gridWidth];
    }

    public int getGridHeight() {
        return gridHeight;
    }

    public void setGridHeight(int value) {
        gridHeight = Math.min(64, Math.max(2, value));
        MotionGrid = new float[gridHeight][gridWidth];
    }

    public GridMotionAreaProcessing() {
        this(16, 16);
    }

    public GridMotionAreaProcessing(int gridWidth, int gridHeight) {
        this(gridWidth, gridHeight, true);
    }

    public GridMotionAreaProcessing(int gridWidth, int gridHeight, boolean highlightMotionGrid) {
        this(gridWidth, gridHeight, highlightMotionGrid, 0.15f);
    }

    public GridMotionAreaProcessing(int gridWidth, int gridHeight, boolean highlightMotionGrid, float motionAmountToHighlight) {
        this.gridWidth = Math.min(64, Math.max(2, gridWidth));
        this.gridHeight = Math.min(64, Math.max(2, gridHeight));

        MotionGrid = new float[gridHeight][gridWidth];

        HighlightMotionGrid = highlightMotionGrid;
        MotionAmountToHighlight = motionAmountToHighlight;
    }

    public void ProcessFrame(int width, int height, int[] videoFrame, byte[] motionFrame) {
        int cellWidth = width / gridWidth;
        int cellHeight = height / gridHeight;
        // temporary variables
        int xCell, yCell;
        // process motion frame calculating amount of changed pixels
        // in each grid's cell
        int motion=0;
        for (int y = 0; y < height; y++) {
            // get current grid's row
            yCell = y / cellHeight;
            // correct row number if image was not divided by grid equally
            if (yCell >= gridHeight)
                yCell = gridHeight - 1;

            for (int x = 0; x < width; x++, motion++) {
                if (motionFrame[motion] != 0)
                {
                    // get current grid's collumn
                    xCell = x / cellWidth;
                    // correct column number if image was not divided by grid equally
                    if (xCell >= gridWidth) xCell = gridWidth - 1;
                    MotionGrid[yCell][xCell]++;
                }
            }
        }
        // update motion grid converting absolute number of changed
        // pixel to relative for each cell
        int gridHeightM1 = gridHeight - 1;
        int gridWidthM1 = gridWidth - 1;

        int lastRowHeight = height - cellHeight * gridHeightM1;
        int lastColumnWidth = width - cellWidth * gridWidthM1;

        for (int y = 0; y < gridHeight; y++) {
            int ch = (y != gridHeightM1) ? cellHeight : lastRowHeight;
            for (int x = 0; x < gridWidth; x++) {
                int cw = (x != gridWidthM1) ? cellWidth : lastColumnWidth;
                MotionGrid[y][ x]/=(cw * ch);
            }
        }

        if (HighlightMotionGrid) {
            // highlight motion grid - cells, which have enough motion
            int videoFrameIdx = 0;
            // color case
            for (int y = 0; y < height; y++) {
                yCell = y / cellHeight;
                if (yCell >= gridHeight)
                    yCell = gridHeight - 1;

                for (int x = 0; x < width; x++, videoFrameIdx++) {
                    xCell = x / cellWidth;
                    if (xCell >= gridWidth)
                        xCell = gridWidth - 1;

                    if ((MotionGrid[yCell][xCell]>MotionAmountToHighlight)&&(((x + y) & 1) == 0)) //note the semi-transparent highlighting with (x+y)&1
                    {
                        videoFrame[videoFrameIdx]=HighlightColor;
                    }
                }
            }
        }
    }
}
