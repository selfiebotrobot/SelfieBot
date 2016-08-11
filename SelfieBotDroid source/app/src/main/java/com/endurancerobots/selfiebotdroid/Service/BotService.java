package com.endurancerobots.selfiebotdroid.Service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.endurancerobots.selfiebotdroid.Common.Global;
import com.endurancerobots.selfiebotdroid.Common.Messages;
import com.endurancerobots.selfiebotdroid.Network.RelayServerConnection;
import com.endurancerobots.selfiebotdroid.R;

public abstract class BotService extends Service {
    protected static String TAG = null;
    protected String mHeadId;
    protected Handler mDataMessageHandler;
    protected RelayServerConnection mRelayServerConnection;
    protected int mState = Messages.BOT_SERVICE_STATE_DISCONNECTED;
    protected BotServiceUI mUI = new BotServiceUI();

    protected final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.w(TAG, "Service got action: " + action);
            if (action.equals(Messages.MSG_MAIN_ACTIVITY_STARTED)) {
                if (mState != Messages.BOT_SERVICE_STATE_DISCONNECTED) {
                    //no need to republish disconnected states as MainActivity already resumes in this state
                    Messages.broadcast(context, Messages.MSG_BOT_SERVICE_STATE_CHANGED, Messages.MSG_BOT_SERVICE_PARAMS_STATE, mState);
                    if (mState == Messages.BOT_SERVICE_STATE_CONNECTED) {
                        //if MainActivity informs Service of onResume and connection is already in progress, just show UI
                        mUI.showUI(context);
                    }
                }
            } else if (action.equals(Messages.CMD_BOT_SERVICE_DISCONNECT) ||
                    action.equals(Messages.CMD_BOT_SERVICE_STOP)) {
                disconnect();
                stopSelf();
            } else if (action.equals(Messages.CMD_BOT_SERVICE_WRITE_CMD)) {
                writeCmd(intent.getByteArrayExtra(Messages.CMD_BOT_SERVICE_PARAMS_WRITE_CMD));
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Service Created");
        Messages.registerBroadcastHandler(this, receiver, Messages.MSG_MAIN_ACTIVITY_STARTED,
                Messages.CMD_BOT_SERVICE_DISCONNECT, Messages.CMD_BOT_SERVICE_STOP, Messages.CMD_BOT_SERVICE_WRITE_CMD);
    }

    protected void setState(int state) {
        if (state != mState) {
            mState = state;
            Messages.broadcast(this, Messages.MSG_BOT_SERVICE_STATE_CHANGED, Messages.MSG_BOT_SERVICE_PARAMS_STATE, mState);
            if (state == Messages.BOT_SERVICE_STATE_CONNECTED) {
                mUI.showUI(this);
            }
        }
    }

    protected abstract void writeCmd(byte[] cmd);

    protected void disconnect() {
        Global.publishProgress(this, getString(R.string.msg_disconnected));
        mUI.hide();
        if (mRelayServerConnection != null) {
            try {
                mRelayServerConnection.close();
                mRelayServerConnection = null;
                setState(Messages.BOT_SERVICE_STATE_DISCONNECTED);
                Global.toast(this, getString(R.string.msg_disconnected));
            } catch (Exception mRelayCloseEx) {
            }
        }
        stopSelf();
    }

    @Override
    public boolean stopService(Intent name) {
        Log.d(TAG, "stopService");
        return super.stopService(name);
    }
    @Override
    public void onDestroy() {
        Log.d(TAG, "Service atempting to destroy");
        try { unregisterReceiver(receiver); } catch (Exception ex){}
        setState(Messages.BOT_SERVICE_STATE_STOPPED);
        mUI.hide();
        Log.d(TAG, "Service destroyed");
        Global.publishProgress(this, "BotService Disconnected");
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
