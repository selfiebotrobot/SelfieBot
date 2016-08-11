package com.endurancerobots.selfiebotdroid.Service;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.endurancerobots.selfiebotdroid.Bluetooth.BtConnectionStates;
import com.endurancerobots.selfiebotdroid.Bluetooth.BtController;
import com.endurancerobots.selfiebotdroid.Common.Commands;
import com.endurancerobots.selfiebotdroid.Common.Global;
import com.endurancerobots.selfiebotdroid.Common.Messages;
import com.endurancerobots.selfiebotdroid.Network.RelayServerConnection;
import com.endurancerobots.selfiebotdroid.Network.TcpDataTransferThread;
import com.endurancerobots.selfiebotdroid.R;

import java.util.Arrays;

public abstract class BotBtService extends BotService {
    private String mMacAddr;
    private BtController mBtController;
    private Handler btControllerMessageHandler;

    @Override
    public void onCreate() {
        TAG = "[[BotServerService]]";
        super.onCreate();
        btControllerMessageHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                handleBtThreadMessage(msg);
            }
        };
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Log.d(TAG, "onStart");
        if (intent != null) {
            final String action = intent.getAction();
            if (Messages.CMD_BOT_SERVICE_START.equals(action)) {
                mHeadId = intent.getStringExtra(Messages.CMD_BOT_SERVICE_PARAMS_HEAD_ID);
                mMacAddr = intent.getStringExtra(Messages.CMD_BOT_SERVICE_PARAMS_MAC_ADDR);
                startForeground(Global.NOTIFICATION_ID,Global.buildNotification(this, getString(R.string.msg_connecting_bt)));
                setState(Messages.BOT_SERVICE_STATE_CONNECTING);
                Log.i(TAG, "MakeServerOnClick: Connecting mHeadId:" + mHeadId + " Bt MAC ADDR:" + mMacAddr);
                mBtController = new BtController(mMacAddr);
                try {
                    Log.d(TAG, "Connecting BT CONTROLLER...");
                    mBtController.connect(btControllerMessageHandler); //-->results in handleBtTrhreadMessage()
                } catch (Exception ex) {
                    Global.publishProgress(this, getString(R.string.msg_connect_error_bt));
                    setState(Messages.BOT_SERVICE_STATE_DISCONNECTED);
                    Log.d(TAG, "Error connecting BT CONTROLLER");
                    mBtController = null;
                    ex.printStackTrace();
                }
            }
        }
        return START_NOT_STICKY;
    }
    protected abstract void handleBtSocketOpen();
    private void handleBtThreadMessage(Message msg)
    {
        switch (msg.what){
            case Messages.BLUETOOTH_SOCKET_OPEN:
                Log.d(TAG,"BT SOCKET OPEN " +mBtController.getMacAddress());
                handleBtSocketOpen();
                break;
            case Messages.BLUETOOTH_SOCKET_ERROR:
                //TODO - need to close instead of writing error
                Log.d(TAG, "Error connecting to bt");
                Global.publishProgress(this, getString(R.string.msg_error_writing_bt)+Commands.decode((byte[]) msg.obj));
                break;
            case Messages.BLUETOOTH_SOCKET_CLOSE:
                Global.publishProgress(this, getString(R.string.msg_disconnected_bt));
                disconnect();
                Log.d(TAG, "Bt socket closed");
                break;
            default:
                Log.w(TAG,"Unknown message "+ Arrays.toString((byte[]) msg.obj)
                        +"with length: "+msg.arg1);
        }

    }
    @Override
    protected void writeCmd(final byte[] cmd) {
        //Global.publishProgress(this, "BT CMD : " + Commands.decode(cmd));
        if(Commands.isCommand(cmd) && Commands.controlByte(cmd)!=Commands.NOP && mBtController!=null) {
            mBtController.write(cmd); //write command from button that was locally presssed
        }
        mUI.runOnUiThread(new Runnable() {
            public void run() {
                mUI.setButtonPressed(Commands.controlByte(cmd));
            }
        });
        Log.d(TAG, "sent: " + Arrays.toString(cmd));
    }
    @Override
    protected void disconnect()
    {
        if(mBtController!=null && mBtController.getConnectionState()!= BtConnectionStates.DISCONNECTED) {
            try {
                mBtController.cancel(false);
            } catch (Exception btCloseEx) {
            }
        }
        super.disconnect();
    }

}