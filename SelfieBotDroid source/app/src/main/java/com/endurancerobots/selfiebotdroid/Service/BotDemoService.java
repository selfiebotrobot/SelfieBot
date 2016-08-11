package com.endurancerobots.selfiebotdroid.Service;
import android.util.Log;
import com.endurancerobots.selfiebotdroid.Common.Commands;
import com.endurancerobots.selfiebotdroid.Common.Global;
import com.endurancerobots.selfiebotdroid.Common.Messages;
import com.endurancerobots.selfiebotdroid.R;

public class BotDemoService extends BotBtService {
    protected static int mInstanceCount = 0;
    private DemoThread mThread;

    @Override
    public void onCreate() {
        TAG = "[[BotServerService]]";
        if (mInstanceCount < 1) {
            mInstanceCount++;
            super.onCreate();
        } else {
            Log.w(TAG, "Should be only one server");
            Global.publishProgress(this, "More than one server service created. Stopping!!!");
            stopSelf();
        }
    }

    @Override
    public void onDestroy() {
        mInstanceCount--;
        if (mInstanceCount < 0) {
            Log.e(TAG, "ERROR : NUMBER OF INSTANCES BELOW 0!");
        }
        if (mThread != null) {
            mThread.interrupt();
            mThread = null;
        }
        super.onDestroy();
    }

    @Override
    protected void handleBtSocketOpen() {
        setState(Messages.BOT_SERVICE_STATE_CONNECTED);
        Global.publishProgress(this, getString(R.string.msg_successful_connection));
        mThread = new DemoThread();
        mThread.start();
    }
    private class DemoThread extends Thread {
        private static final String TAG = " [[ DemoThread]] ";
        public DemoThread() {
            Log.d(TAG, "Starting demo");
        }
        private void runCmd(final byte[] cmd, int repeat) throws InterruptedException
        {
            int cmdFreq=getResources().getInteger(R.integer.cmd_frequency_ms);
            for(int i=0;i<repeat;i++)
            {
                writeCmd(cmd);
                Thread.sleep(cmdFreq);
            }
        }
        public void run() {
            while(true)
            {
                try {
                    runCmd(Commands.LeftMsg,10);
                    runCmd(Commands.RightMsg,  10);
                }
                catch (Exception e)
                {
                    return;
                }
            }
        }
    }
}