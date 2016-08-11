package com.endurancerobots.selfiebotdroid.MotionDetection;

import com.endurancerobots.selfiebotdroid.Common.Graphics;

public class TwoFramesDifferenceDetector {
    // frame's dimension
    private int width;
    private int height;
    private int frameSize;

    // previous frame of video stream
    private byte[] previousFrame;
    // current frame of video sream
    /// <summary>
    /// Motion frame containing detected areas of motion.
    /// </summary>
    ///
    /// <remarks><para>Motion frame is a grayscale image, which shows areas of detected motion.
    /// All black pixels in the motion frame correspond to areas, where no motion is
    /// detected. But white pixels correspond to areas, where motion is detected.</para>
    ///
    /// <para><note>The property is set to <see langword="null"/> after processing of the first
    /// video frame by the algorithm.</note></para>
    /// </remarks>
    ///
    public byte[] motionFrame;
    // number of pixels changed in the new frame of video stream
    private int pixelsChanged;
    // threshold values
    private int differenceThreshold = 15;
    private int differenceThresholdNeg = -15;

    /// <summary>
    /// Difference threshold value, [1, 255].
    /// </summary>
    ///
    /// <remarks><para>The value specifies the amount off difference between pixels, which is treated
    /// as motion pixel.</para>
    ///
    /// <para>Default value is set to <b>15</b>.</para>
    /// </remarks>
    ///
    public int getDifferenceThreshold() {
        return differenceThreshold;
    }

    public void setDifferenceThreshold(int value) {
        differenceThreshold = Math.max(1, Math.min(255, value));
        differenceThresholdNeg = -differenceThreshold;
    }

    /// <summary>
    /// Motion level value, [0, 1].
    /// </summary>
    ///
    /// <remarks><para>Amount of changes in the last processed frame. For example, if value of
    /// this property equals to 0.1, then it means that last processed frame has 10% difference
    /// with previous frame.</para>
    /// </remarks>
    ///
    public float getMotionLevel() {
        return (float) pixelsChanged / (width * height);
    }

    public TwoFramesDifferenceDetector() {
    }

    /// <summary>
    /// Process new video frame.
    /// </summary>
    ///
    /// <param name="videoFrame">Video frame to process (detect motion in).</param>
    ///
    /// <remarks><para>Processes new frame from video source and detects motion in it.</para>
    ///
    /// <para>Check <see cref="MotionLevel"/> property to get information about amount of motion
    /// (changes) in the processed frame.</para>
    /// </remarks>
    ///
    public void ProcessFrame(int w, int h, int[] videoFrame) throws Exception {
        // check previous frame
        if (previousFrame == null) {
            // save image dimension
            width = w;
            height = h;

            // alocate memory for previous and current frames
            previousFrame = new byte[videoFrame.length];
            Graphics.rgbToGrayscale8pp(videoFrame, previousFrame);
            motionFrame = new byte[videoFrame.length];
            frameSize = width * height;
            return;
        }
        // check image dimension
        if ((w != width) || (h != height))
            throw new Exception(getClass().getName() + ": ProcessFrame dimensions mismatch");

        // convert current image to grayscale
        Graphics.rgbToGrayscale8pp(videoFrame, motionFrame);
        // difference value
        int diff;

        // 1 - get difference between frames
        // 2 - threshold the difference
        // 3 - copy current frame to previous frame
        pixelsChanged = 0;
        for (int i = 0; i < frameSize; i++) {
            // difference
            diff = motionFrame[i] - previousFrame[i];
            // copy current frame to previous
            previousFrame[i] = motionFrame[i];
            // treshold
            motionFrame[i] = ((diff >= differenceThreshold) || (diff <= differenceThresholdNeg)) ? (byte) 255 : (byte) 0;
            pixelsChanged += (motionFrame[i] & 1);
        }
    }

    /// <summary>
    /// Reset motion detector to initial state.
    /// </summary>
    ///
    /// <remarks><para>Resets internal state and variables of motion detection algorithm.
    /// Usually this is required to be done before processing new video source, but
    /// may be also done at any time to restart motion detection algorithm.</para>
    /// </remarks>
    ///
    public void Reset( ) {
        previousFrame = null;
        motionFrame = null;
    }
}