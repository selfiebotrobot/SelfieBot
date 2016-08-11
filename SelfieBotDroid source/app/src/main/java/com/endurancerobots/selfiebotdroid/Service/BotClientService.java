package com.endurancerobots.selfiebotdroid.Service;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.endurancerobots.selfiebotdroid.Common.Commands;
import com.endurancerobots.selfiebotdroid.Common.Messages;
import com.endurancerobots.selfiebotdroid.Network.RelayServerConnection;
import com.endurancerobots.selfiebotdroid.Network.TcpDataTransferThread;
import com.endurancerobots.selfiebotdroid.R;
import com.endurancerobots.selfiebotdroid.Common.Global;

import java.io.IOException;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

public class BotClientService extends BotService {
    protected static int mInstanceCount =0;
    private Timer mNoOpTimer;
    @Override
    public void onCreate() {
        TAG = "[[BotClientService]]";
        super.onCreate();
        if (mInstanceCount < 1) {    mInstanceCount++; } else {
            Log.e(TAG, "ERROR : Should be only one server");
            stopSelf();
        }
    }
    @Override
    public void onDestroy()
    {
        mInstanceCount--;
        if(mInstanceCount<0)
        {
            Log.e(TAG, "ERROR : NUMBER OF INSTANCES BELOW 0!");
        }
        super.onDestroy();
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Log.d(TAG, "onStart");
        if (intent != null) {
            final String action = intent.getAction();
            if (Messages.CMD_BOT_SERVICE_START.equals(action)) {
                mHeadId = intent.getStringExtra(Messages.CMD_BOT_SERVICE_PARAMS_HEAD_ID);
                Log.i(TAG, "mHeadId:" + mHeadId);
                startForeground(Global.NOTIFICATION_ID, Global.buildNotification(this, getString(R.string.msg_connecting) + "(" +
                        getString(R.string.lbHeadID) + " " + mHeadId + ")"));
                setState(Messages.BOT_SERVICE_STATE_CONNECTING);
                mDataMessageHandler = new Handler() {
                    @Override
                    public void handleMessage(Message msg) { super.handleMessage(msg); handleClientDataMessage(msg);     }
                };
                mRelayServerConnection = new RelayServerConnection(mDataMessageHandler); // Пытаемся подключиться
                mRelayServerConnection.start(mHeadId, RelayServerConnection.Role.CLIENT);
            }
        }
        return START_NOT_STICKY;
    }
    private void handleClientDataMessage(Message msg) //this receives feedback from server (which is connected to bot)
    {
        try {
            switch (msg.what) {
                case Messages.MSG_RELAY_SERVER_CONN_ERROR:
                    Global.publishProgress(this, getString(R.string.msg_connect_error));
                    setState(Messages.BOT_SERVICE_STATE_DISCONNECTED);
                    Log.d(TAG, "Error connecting to server");
                    break;
                case Messages.MSG_RELAY_SERVER_CONN_PROXY:
                    Log.d(TAG, "Connected to proxy");
                    setState(Messages.BOT_SERVICE_STATE_WAIT);
                    Global.publishProgress(this, getString(R.string.msg_proxy_connected));
                    break;
                case Messages.MSG_RELAY_SERVER_CONNECTED:
                    setState(Messages.BOT_SERVICE_STATE_CONNECTED);
                    Log.d(TAG, "CONNECT");
                    Global.publishProgress(this, getString(R.string.msg_successful_connection));

                    //this is NO-OPERATION timer which sends keep-alive every R.integer.nop_frequency_ms (10) seconds
                    mNoOpTimer = new Timer();
                    int noOpTimerFreq=getResources().getInteger(R.integer.nop_frequency_ms);
                    mNoOpTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            noOpTimerMethod();
                        }
                    }, 500, noOpTimerFreq);
                    break;
                case TcpDataTransferThread.TCP_DATA_READ_MSG:
                    Log.i(TAG, "sDataHandler got message: " + msg.arg1 + " "
                            + msg.arg2 + " " + Arrays.toString(((byte[]) msg.obj)));
                    //Global.publishProgress(this, "CL:" + Commands.decode((byte[]) msg.obj));
                    //strange to receive command messages on client - but we still handle somehow
                    // TODO: add feedback from the platform of some kind
                    break;
                case Commands.CLOSE:
                    disconnect();
                    /*
                    setState(Messages.BOT_SERVICE_STATE_DISCONNECTED);
                    switch (msg.arg1) {
                        case TcpDataTransferThread.BY_ERROR:
                            Global.publishProgress(this, "CL" + getString(R.string.connection_closed_by_error));
                            break;
                        case TcpDataTransferThread.BY_CLIENT:
                            Global.publishProgress(this, "CL" + getString(R.string.connection_closed_by_client));
                            break;
                    }
                    */
                    break;
                default:
                    Log.w(TAG, "Unknown message " + Arrays.toString((byte[]) msg.obj)
                            + "with length: " + msg.arg1);
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            Global.publishProgress(this, "Error handling data:" + ex.getMessage());
        }
    }
    private void noOpTimerMethod()
    {
        writeCmd(Commands.NoOpMsg);
    }
    protected void writeCmd(byte[] cmd) {

        Log.d(TAG, "CL:sending: " + Arrays.toString(cmd));
        try {
            if(mRelayServerConnection!=null)
                mRelayServerConnection.write(cmd);
            //todo:perhaps catch the error event here
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "sent: " + Arrays.toString(cmd));
    }
    @Override
    protected void disconnect()
    {
        if(mNoOpTimer!=null) {
            mNoOpTimer.cancel();
            mNoOpTimer = null;
        }
        super.disconnect();

    }
}