package com.endurancerobots.selfiebotdroid.MotionDetection;
public class MotionDetector
{
    private TwoFramesDifferenceDetector detector;
    private GridMotionAreaProcessing processor;
    public int videoWidth, videoHeight;
    // dummy object to lock for synchronization
    public MotionDetector( TwoFramesDifferenceDetector detector, GridMotionAreaProcessing processor )
    {
        this.detector  = detector;
        this.processor = processor;
    }
    public float ProcessFrame( int w, int h, int[] videoFrame ) throws Exception {
        if (detector == null) return 0;
        videoWidth = w;
        videoHeight = h;
        float motionLevel = 0;
        // call motion detection
        detector.ProcessFrame(w, h, videoFrame);
        motionLevel = detector.getMotionLevel();
        // call motion post processing
        if ((processor != null) && (detector.motionFrame != null)) {
            processor.ProcessFrame(w, h, videoFrame, detector.motionFrame);
        }
        return motionLevel;
    }
    public void Reset( ) {
        if (detector != null) {
            detector.Reset();
        }

        videoWidth = 0;
        videoHeight = 0;
    }
}