package com.endurancerobots.selfiebotdroid.Service;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.endurancerobots.selfiebotdroid.Common.Commands;
import com.endurancerobots.selfiebotdroid.Common.Global;
import com.endurancerobots.selfiebotdroid.Common.Messages;
import com.endurancerobots.selfiebotdroid.Network.RelayServerConnection;
import com.endurancerobots.selfiebotdroid.Network.TcpDataTransferThread;
import com.endurancerobots.selfiebotdroid.R;

import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

public class BotServerService extends BotBtService {
    protected static int mInstanceCount =0;
    private Timer mCmdTimer;

    @Override
    public void onCreate() {
        TAG = "[[BotServerService]]";
        if (mInstanceCount < 1) { mInstanceCount++; super.onCreate(); } else {
            Log.w(TAG, "Should be only one server");
            Global.publishProgress(this, "More than one server service created. Stopping!!!");
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
    protected void handleBtSocketOpen()
    {
        Global.publishProgress(this, getString(R.string.msg_connecting)+ "(" +
                getString(R.string.lbHeadID)+" "+mHeadId+")");//"Connecting as server " + mHeadId + "\naddr " + mBtController.getMacAddress());
        mDataMessageHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) { super.handleMessage(msg); handleServerDataMessage(msg);     }
        };
        mRelayServerConnection = new RelayServerConnection(mDataMessageHandler);
        mRelayServerConnection.start(mHeadId, RelayServerConnection.Role.SERVER);

    }
    private void handleServerDataMessage(Message msg) //gets commands from client and forwards them through BlueTooth to platform
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
                    Global.publishProgress(this, getString(R.string.msg_proxy_connected) + "(" +
                            getString(R.string.lbHeadID) + " " + mHeadId + ")");
                    break;
                case Messages.MSG_RELAY_SERVER_CONNECTED:
                    setState(Messages.BOT_SERVICE_STATE_CONNECTED);
                    Global.publishProgress(this, getString(R.string.msg_successful_connection));
                    break;
                case TcpDataTransferThread.TCP_DATA_READ_MSG:
                    final byte[] cmd=(byte[])msg.obj;
                    Log.i(TAG, "sDataHandler got message: " + msg.arg1 + " "
                            + msg.arg2 + " " + Arrays.toString(cmd));
                    if(Commands.isCommand(cmd))
                    {
                        if(Commands.controlByte(cmd)==Commands.NOP) {
                            //don't do anything, its a NOP
                        }
                        else {
                            //Global.publishProgress(this, "CMD:" + Commands.decode(cmd));
                            writeCmd(cmd); //forward the data to bt controller
                            stopTimer();
                            if(Commands.controlByte(cmd)!=Commands.STOP) {
                                //if it is a new command start timer which will repeat command
                               mCmdTimer = new Timer();
                                int timerFreq=getResources().getInteger(R.integer.cmd_frequency_ms);
                                mCmdTimer.schedule(new TimerTask() {
                                    @Override
                                    public void run() {
                                        writeCmd(cmd); //forward the data to bt controller
                                    }
                                }, timerFreq, timerFreq);
                            }
                        }
                    }
                    break;
                case Commands.CLOSE:
                    disconnect();
                    /*
                    switch (msg.arg1) {
                        case TcpDataTransferThread.BY_ERROR:
                            setInfo(getString(R.string.connection_closed_by_error));
                            break;
                        case TcpDataTransferThread.BY_CLIENT:
                            setInfo(getString(R.string.connection_closed_by_client));
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
    private void stopTimer()
    {
        if(mCmdTimer!=null) {
            mCmdTimer.cancel();
            mCmdTimer = null;
        }
    }
    @Override
    protected void disconnect()
    {
        stopTimer();
        super.disconnect();

    }
}