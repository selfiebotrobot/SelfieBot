package com.endurancerobots.selfiebotdroid.Network;

import android.nfc.Tag;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.endurancerobots.selfiebotdroid.Common.Commands;
import com.endurancerobots.selfiebotdroid.Common.Global;
import com.endurancerobots.selfiebotdroid.Common.Messages;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Arrays;

public class RelayServerConnection extends Thread {
    private static final String PROXY_IP = "46.38.49.133";
    private static final int TCP_PROXY_SERVER_PORT = 4445;

    private static final String TAG = "[[ ProxyConnector ]]";
    private static final String ERROR = "\r\nERROR\r\n";
    private static final String WAIT = "\r\nWAIT\r\n";
    private static final String CONNECT = "\r\nCONNECT\r\n";

    private Handler mIncomingDataHandler; //handle data coming in from RelayServer connection
    private Handler mExternalDataHandler; //external connection state and data handler
    private TcpDataTransferThread mTransferThread;
    private Socket mSocket = new Socket();

    private Role mRole;
    private String mStrId=null;
    private RelayConnectionStates mConnState=RelayConnectionStates.DISCONNECTED;
    public RelayConnectionStates getConnState() { return mConnState;}

    public enum Role {SERVER,CLIENT};

    public RelayServerConnection(Handler dataHandler)
    {
        mExternalDataHandler = dataHandler;
        mIncomingDataHandler = new Handler() { @Override public void handleMessage(Message msg) {
            super.handleMessage(msg); handleIncomingData(msg); } };
    }
    public Role getRole() { return mRole; }
    public void start(String headId, Role role){
        mStrId = ((role==Role.SERVER)?"S":"G") + headId + "\r";
        mRole=role;
        this.start();
    }
    @Override
    public synchronized void start() {
        if(mStrId!=null) super.start();
        else Log.i(TAG,"idStringIs null!");
    }
    @Override
    public void run() {
        super.run();
        connect(PROXY_IP, TCP_PROXY_SERVER_PORT);
    }
    public void write(byte[] cmd) throws IOException{
        if(mConnState==RelayConnectionStates.CONNECTED) mTransferThread.write(cmd);
    }
    private void connect(String host, int port) {
        if (mSocket.isConnected()) {
            Log.w(TAG, "Already connected as "+mStrId+" (was trying " + host + ":" + port+")");
        }
        else {
            try {
                mSocket.connect(new InetSocketAddress(host, port));
                if (mSocket.isConnected()) {
                    Log.i(TAG, "Successful connection to " + host + ":" + port);
                    mExternalDataHandler.obtainMessage(Messages.MSG_RELAY_SERVER_CONN_PROXY, null).sendToTarget();
                    mTransferThread = new TcpDataTransferThread(mSocket);
                    mTransferThread.setDataMsgHandler(mIncomingDataHandler);
                    mTransferThread.start();
                    try {
                        mTransferThread.write(mStrId.getBytes());
                    } catch (IOException e) {
                        e.printStackTrace();
                        mExternalDataHandler.obtainMessage(Messages.MSG_RELAY_SERVER_CONN_ERROR, null).sendToTarget();
                    }
                }
                else {
                    Log.w(TAG, "Can't connect to server" + host + ":" + port);
                }
            }
            catch (Exception e) {
                e.printStackTrace();
                mExternalDataHandler.obtainMessage(Messages.MSG_RELAY_SERVER_CONN_ERROR, null).sendToTarget();
            }
        }
    }
    private void handleIncomingData(Message msg){
        switch (msg.what) {
            case TcpDataTransferThread.TCP_DATA_READ_MSG:
                String s = new String((byte[]) msg.obj, 0, msg.arg1);
                if(mConnState==RelayConnectionStates.CONNECTED)
                {
                    byte[] cmd=(byte[])msg.obj;
                    mExternalDataHandler.obtainMessage(msg.what,msg.arg1,msg.arg2, msg.obj).sendToTarget();
                    Log.i(TAG, "Got only: " + Arrays.toString(cmd));
                }
                else {
                    if (s.contains(CONNECT)) {
                        Log.i(TAG, CONNECT);
                        mConnState = RelayConnectionStates.CONNECTED;
                        mExternalDataHandler.obtainMessage(Messages.MSG_RELAY_SERVER_CONNECTED, null).sendToTarget();
                    } else if (s.contains(WAIT)) {
                        Log.v(TAG, WAIT);
                        mConnState = RelayConnectionStates.WAIT;
                    } else if (s.contains(ERROR)) {
                        Log.e(TAG, ERROR);
                    }
                }
                break;
            case Commands.CLOSE:
                Log.i(TAG, "Close");
                mConnState=RelayConnectionStates.DISCONNECTED;
                mExternalDataHandler.obtainMessage(Commands.CLOSE, null).sendToTarget();
                break;
        }
    }
    public void close() {
        try {
            Log.d(TAG, "cancel and interrupt");
            mTransferThread.close();
            interrupt();
        } catch (NullPointerException e){
            Log.e(TAG,"Null pointer ex");
        }
        mConnState=RelayConnectionStates.DISCONNECTED;
    }
}
