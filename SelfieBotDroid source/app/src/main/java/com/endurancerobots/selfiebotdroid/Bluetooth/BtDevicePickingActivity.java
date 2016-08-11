package com.endurancerobots.selfiebotdroid.Bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.endurancerobots.selfiebotdroid.Common.Global;
import com.endurancerobots.selfiebotdroid.Common.Messages;
import com.endurancerobots.selfiebotdroid.R;

import java.util.ArrayList;
import java.util.Set;

public class BtDevicePickingActivity extends Activity {
    private static final String TAG = " [[ BtDevicePicking ]] ";
    public static final String BLUETOOTH_MAC = "com.endurancerobots.selfiebotdroid.BLUETOOTH_MAC";

    private ListAdapter mBluetoothList;
    private ArrayList<String> mDeviceArray = new ArrayList<String>();
    private ArrayAdapter<String> mDevicesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bt_device_picking);
        Log.d(TAG, "onCreate");
        ListView listView = (ListView) findViewById(R.id.bluetooth_devices);
        mDevicesAdapter = new ArrayAdapter<String>(this, R.layout.bluetooth_device_item, mDeviceArray);
        listView.setAdapter(mDevicesAdapter);
        fillBtBoundDevicesList();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView tv = (TextView) view;
                Log.i(TAG, mDevicesAdapter.getItem(position));
                Intent answerIntent = new Intent();
                answerIntent.putExtra(BLUETOOTH_MAC, mDevicesAdapter.getItem(position).split("\n")[1]);
                setResult(RESULT_OK, answerIntent);
                finish();
            }
        });
    }
    private void fillBtBoundDevicesList() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mBluetoothAdapter.startDiscovery();
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        mDeviceArray.clear();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                mDeviceArray.add(device.getName() + "\n" + device.getAddress());
                Log.i(TAG, device.getName() + " " + device.getAddress());
            }
            mDevicesAdapter.notifyDataSetChanged();
        }
    }
    public void onBtSettingsClick(View view) {
        Log.d(TAG, "onBtSettingsClick");
        BtController.showBTSettings(this);
    }
    //this will be called after user returns from settings pulled up in onBtSettingsClick
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "get Result:" + "requestCode" + requestCode + "resultCode" + resultCode);
        switch (requestCode) {
            case Messages.BT_SETTINGS_RESULT:
                fillBtBoundDevicesList();
                Global.toast(this, "List refreshed");
                break;
        }
    }
    public void onBackClick(View view) {
        Log.d(TAG, "onBackClick");
        setResult(RESULT_CANCELED);
        finish();
    }
}