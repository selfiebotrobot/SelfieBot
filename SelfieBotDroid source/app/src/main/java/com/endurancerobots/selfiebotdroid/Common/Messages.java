package com.endurancerobots.selfiebotdroid.Common;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;

import com.endurancerobots.selfiebotdroid.Service.BotClientService;

public class Messages {
    public static final int MSG_RELAY_SERVER_CONNECTED = 211;
    public static final int MSG_RELAY_SERVER_CONN_PROXY = 213;
    public static final int MSG_RELAY_SERVER_CONN_ERROR = 212;

    public static final int BT_REQUEST_ENABLE_RESULT = 9001;
    public static final int BT_SETTINGS_RESULT = 9004;
    public static final int BT_DEVICE_PICKING_ACTIVITY_RESULT=9002;

    public static final int BLUETOOTH_SOCKET_OPEN = 11111;
    public static final int BLUETOOTH_SOCKET_ERROR = 33333;
    public static final int BLUETOOTH_SOCKET_CLOSE = 22222;

    public static final String CMD_BOT_SERVICE_START = "com.endurancerobots.selfiebotdroid.cmd.BotService.START";
    public static final String CMD_BOT_SERVICE_DISCONNECT = "com.endurancerobots.selfiebotdroid.cmd.BotService.DISCONNECT";
    public static final String CMD_BOT_SERVICE_WRITE_CMD ="com.endurancerobots.selfiebotdroid.cmd.BotService.WRITE_CMD";
    public static final String CMD_BOT_SERVICE_STOP = "com.endurancerobots.selfiebotdroid.cmd.BotService.STOP";

    public static final String MSG_MAIN_ACTIVITY_STARTED = "com.endurancerobots.selfiebotdroid.msg.MainActivity.STARTED";
    public static final String MSG_BOT_SERVICE_STATE_CHANGED = "com.endurancerobots.selfiebotdroid.msg.BotService.STATE_CHANGED"; //
    public static final String MSG_BOT_SERVICE_PARAMS_STATE = "com.endurancerobots.selfiebotdroid.msg.params.BotService.STATE"; //integer
    public static final String MSG_BOT_SERVICE_MSG= "com.endurancerobots.selfiebotdroid.msg.BotService.MSG"; //a message is forwarded to foreground from Service
    public static final String MSG_BOT_SERVICE_PARAMS_MSG= "com.endurancerobots.selfiebotdroid.msg.params.BotService.MSG"; //a turn command has arrived to bot service

    public static final int BOT_SERVICE_STATE_DISCONNECTED=0;
    public static final int BOT_SERVICE_STATE_CONNECTING=1;
    public static final int BOT_SERVICE_STATE_WAIT=2;
    public static final int BOT_SERVICE_STATE_CONNECTED=3;
    public static final int BOT_SERVICE_STATE_STOPPED=4;
    public static String msgBotServiceStateToString(int intExtra) {
        switch (intExtra)
        {
            case BOT_SERVICE_STATE_DISCONNECTED: return "DISCONNECTED";
            case BOT_SERVICE_STATE_CONNECTING: return "CONNECTING";
            case BOT_SERVICE_STATE_WAIT: return "WAIT";
            case BOT_SERVICE_STATE_CONNECTED: return "CONNECTED";
            case BOT_SERVICE_STATE_STOPPED: return "STOPPED";
            default: return "UNKNOWN";
        }
    }

    public static final String CMD_BOT_SERVICE_PARAMS_HEAD_ID = "com.endurancerobots.selfiebotdroid.msg.params.BotService.HEAD_ID";
    public static final String CMD_BOT_SERVICE_PARAMS_MAC_ADDR = "com.endurancerobots.selfiebotdroid.msg.params.BotService.MAC_ADDR";
    public static final String CMD_BOT_SERVICE_PARAMS_WRITE_CMD="com.endurancerobots.selfiebotdroid.cmd.params.BotService.WRITE_CMD";

    public static void registerBroadcastHandler(ContextWrapper cw, BroadcastReceiver broadcastHandler, String... filters) {
        cw.registerReceiver(broadcastHandler, createFilter(filters));
    }
    public static Intent createIntent(Context ctx,Class cls,String cmd,String... params)
    {
        Intent intent = new Intent(ctx, cls);
        intent.setAction(cmd);
        for(int i=0;i<params.length;i+=2) {
            intent.putExtra(params[i], params[i + 1]);
        }
        return intent;
    }
    public static void broadcast(Context c,String msg,String paramName,String paramValue)
    {
        Intent i=new Intent(msg);
        i.putExtra(paramName, paramValue);
        c.sendBroadcast(i);
    }
    public static void broadcast(Context c,String msg,String paramName,int paramValue)
    {
        Intent i=new Intent(msg);
        i.putExtra(paramName, paramValue);
        c.sendBroadcast(i);
    }
    public static void broadcast(Context c,String msg,String paramName,byte[] paramValue)
    {
        Intent i=new Intent(msg);
        i.putExtra(paramName,paramValue);
        c.sendBroadcast(i);
    }
    public static IntentFilter createFilter(String... actions)
    {
        IntentFilter res=new IntentFilter();
        for(String a : actions) res.addAction(a);
        return res;
    }

}
