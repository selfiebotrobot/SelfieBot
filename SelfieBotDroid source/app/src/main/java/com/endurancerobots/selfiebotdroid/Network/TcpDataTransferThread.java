package com.endurancerobots.selfiebotdroid.Network;

import android.os.Handler;
import android.util.Log;

import com.endurancerobots.selfiebotdroid.Common.Commands;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class TcpDataTransferThread extends Thread {
    private String TAG;
    private static int numOfThreads = 0;

    public static final int TCP_DATA_READ_MSG = 10;     //messages to mDataReceivedHandler handler
    public static final int BY_CLIENT = 1007;
    public static final int BY_ERROR = 999;

    private final InputStream mInStream;
    private final OutputStream mOutStream;
    private final Socket mSocket;

    private Handler mDataReceivedHandler = null;  //data and general message handler

    public TcpDataTransferThread(Socket socket) throws Exception {
        numOfThreads++;
        TAG = "[[ TcpTransfer_"+numOfThreads + " ]]";
        mSocket = socket;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;
        try {
            tmpIn = socket.getInputStream();          // Get the input and output streams, using temp objects because
            tmpOut = socket.getOutputStream();             // member streams are final
        } catch (Exception e) { Log.e(TAG, "Exeption: " + e.getMessage()); }
        mInStream = tmpIn;
        mOutStream = tmpOut;
    }
    public void run() {
        TAG+="_"+getName();
        Log.d(TAG, "thread started");
        byte[] buffer = new byte[128];  // buffer store for the stream
        int bytes; // bytes returned from read()

        // Keep listening to the InputStream until an exception occurs
        while (!isInterrupted()) {
            try {
                bytes = mInStream.read(buffer);
                if(bytes==-1)
                {
                    Log.v(TAG, "connection closed by server ");
                    close(null, Commands.CLOSE, BY_ERROR);
                    break;
                }
                Log.v(TAG, "read " + bytes + " bytes:" + new String(buffer, 0, bytes));
                if (buffer[0] == Commands.CLOSE) { close(null, Commands.CLOSE, BY_CLIENT); }
                if (mDataReceivedHandler != null) {
                    byte[] msg = new byte[bytes];
                    System.arraycopy(buffer, 0, msg, 0, bytes);
                    mDataReceivedHandler.obtainMessage(TCP_DATA_READ_MSG, bytes, -1, msg).sendToTarget();
                }
            } catch (Exception e) {
                e.printStackTrace();
                close(e, Commands.CLOSE, BY_ERROR);
                break;
            }
        }
    }
    public void close()
    {
        close(null, Commands.CLOSE, BY_CLIENT);
    }

    private void close(Exception e,int msg, int param) {
        if(e!=null) Log.e(TAG,e.getMessage());
        if (mDataReceivedHandler != null) {
            mDataReceivedHandler.obtainMessage(msg, param,0).sendToTarget();
            Log.i(TAG, "Connection is to be closed. I'll send msg to service");
        }else Log.w(TAG,"Oops, mControl handler is null");
        try {
            mInStream.close();
            mOutStream.close();
            Log.i(TAG, "Streams closed");
            mSocket.close();
            Log.i(TAG, "SOCKET closed");
        } catch (IOException ex) { ex.printStackTrace(); }
        interrupt();
    }
    public void write(byte[] buf) throws IOException {
        if(!isInterrupted()) {
            mOutStream.write(buf);
            Log.v(TAG, "write " + buf.length + " bytes:" + new String(buf));
        }
    }
    @Override
    public void interrupt() { super.interrupt(); Log.i(TAG, "interrupt()"); }
    public void cancel() { interrupt(); } /* Call this from the main activity to shutdown the connection */
    public void setDataMsgHandler(Handler mOutHandler) { this.mDataReceivedHandler = mOutHandler; }
}
