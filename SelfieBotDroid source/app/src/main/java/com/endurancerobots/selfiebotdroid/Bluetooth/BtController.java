package com.endurancerobots.selfiebotdroid.Bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import com.endurancerobots.selfiebotdroid.Common.Global;
import com.endurancerobots.selfiebotdroid.R;
import com.endurancerobots.selfiebotdroid.Common.Messages;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Arrays;

/*
    Controls all blue-tooth related activities
 */
public class BtController extends Thread {
    private static final String TAG = " [[ BtController ]] ";
    private BluetoothSocket mSocket;

    private String mBtMacAddress; //address of the bluetooth Selfie platform
    private BtConnectionStates mConnectionState;
    private BluetoothAdapter mBluetoothAdapter;
    private OutputStream mOutStream;
    private Boolean mIsRunning=false;
    private Handler mOutHandler;  //bt event handler for forwarding messages outside
    public BtConnectionStates getConnectionState() { return mConnectionState;}

    public String getMacAddress() { return mBtMacAddress; }
    public BtController(String btMacAddress)
    {
        Log.d(TAG,"Starting btcoontroller with " + btMacAddress);
        mBtMacAddress=btMacAddress;
        mConnectionState=BtConnectionStates.DISCONNECTED;
    }
    public void connect(Handler handler) throws Exception {
        Log.d(TAG,"Connecting " +mBtMacAddress);
        if(mConnectionState!=BtConnectionStates.DISCONNECTED) throw  new Exception("BtController already connected");
        mOutHandler=handler;
        mConnectionState=BtConnectionStates.CONNECTING;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(mBtMacAddress);
        try {
            Method m = device.getClass().getMethod("createRfcommSocket",new Class[] {int.class});
            mSocket = (BluetoothSocket) m.invoke(device,1);
        }
        catch (Exception e) { e.printStackTrace(); }
        if(mSocket ==null) {
            NullPointerException e = new NullPointerException("Device was not connected");
            Log.e(TAG, "mSocket is nul");
            throw e;
        }
        start();
    }
    public void run() {
        mIsRunning=true;
        Log.d(TAG, "thread started");
        mBluetoothAdapter.cancelDiscovery();
        Log.i(TAG, "cancelDiscovery");
        try {
            mSocket.connect();
            mOutStream = mSocket.getOutputStream();
            mOutHandler.obtainMessage(Messages.BLUETOOTH_SOCKET_OPEN, mSocket).sendToTarget();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage() + "\ntrying to close the socket");
            // Unable to connect; close the socket and get out
            try {
                mSocket.close();
            } catch (IOException closeException) {
                Log.e(TAG,closeException.getMessage());
            } catch (NullPointerException ne){
                Log.e(TAG,"BT Socket is null: "+ne.getMessage());
            }
       //     mOutHandler.obtainMessage(Messages.BLUETOOTH_SOCKET_ERROR).sendToTarget();
            //comment this line and uncomment the next to
            // ignore the error if we are testing without the actual platform to not report bt conn problem
            mOutHandler.obtainMessage(Messages.BLUETOOTH_SOCKET_OPEN).sendToTarget();
        }
        mIsRunning=false;
    }
    /** Will cancel an in-progress connection, and close the socket */
    public void cancel(Boolean forwardMessages) {
        if(mIsRunning) interrupt();
        try { mOutStream.close(); } catch (Exception e) {  }
        try { mSocket.close(); } catch (Exception e) {  }
        if(forwardMessages) mOutHandler.obtainMessage(Messages.BLUETOOTH_SOCKET_CLOSE).sendToTarget();
        Log.d(TAG, "thread canceled");
    }

    public void write(byte[] bytes)  {
        try
        {
            Log.v(TAG, "write " + bytes.length + " bytes:" + Arrays.toString(bytes));
            mOutStream.write(bytes);
        }
        catch (Exception ex)
        {
            mOutHandler.obtainMessage(Messages.BLUETOOTH_SOCKET_ERROR,bytes).sendToTarget();
            //TODO : handle bluetooth errors somehow
            //cancel(true);
        }
    }

    //show bluetooth settings screen (system config)
    public static void showBTSettings(Activity parentActitivity)
    {
        Intent intentOpenBluetoothSettings = new Intent();
        intentOpenBluetoothSettings.setAction(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
        parentActitivity.startActivityForResult(intentOpenBluetoothSettings, Messages.BT_SETTINGS_RESULT);
    }

    //enable bluetooth and show bluetooth picker activity
    public static void getBtDevice(Activity parentActivity) {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter != null) {
            Log.d(TAG, "Device support bluetooth");
            if (!mBluetoothAdapter.isEnabled()) {
                Log.d(TAG, "Bluetooth was not Enabled");
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                parentActivity.startActivityForResult(enableBtIntent, Messages.BT_REQUEST_ENABLE_RESULT);
            } else {
                Log.d(TAG, "Bluetooth was Enabled");
                getBoundBtDevices(parentActivity);
            }
        } else {
            // Device does not support Bluetooth
            Global.toast(parentActivity.getApplicationContext(), parentActivity.getString(R.string.msg_bluetooth_not_supported));
        }
    }

    //show bluetooth picker activity
    public static void getBoundBtDevices(Activity parentActivity) {
        Log.i(TAG, "gettingBoundedDevices");
        Intent btDevicePickingIntent = new Intent(parentActivity.getApplicationContext(),BtDevicePickingActivity.class);
        parentActivity.startActivityForResult(btDevicePickingIntent, Messages.BT_DEVICE_PICKING_ACTIVITY_RESULT);
    }
    /*
    public static boolean IsBlueToothEnabled() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        return (mBluetoothAdapter == null)? false : mBluetoothAdapter.isEnabled();
    }*/
}
