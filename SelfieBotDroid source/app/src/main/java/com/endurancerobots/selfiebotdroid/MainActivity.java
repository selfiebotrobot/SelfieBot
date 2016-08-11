package com.endurancerobots.selfiebotdroid;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.endurancerobots.selfiebotdroid.Bluetooth.BtController;
import com.endurancerobots.selfiebotdroid.Bluetooth.BtDevicePickingActivity;
import com.endurancerobots.selfiebotdroid.Common.Camera;
import com.endurancerobots.selfiebotdroid.Common.Global;
import com.endurancerobots.selfiebotdroid.Common.Messages;
import com.endurancerobots.selfiebotdroid.MotionDetection.BotMotionDetectionService;
import com.endurancerobots.selfiebotdroid.Network.NetUtils;
import com.endurancerobots.selfiebotdroid.Service.BotClientService;
import com.endurancerobots.selfiebotdroid.Service.BotDemoService;
import com.endurancerobots.selfiebotdroid.FaceTracking.BotFaceTrackingService;
import com.endurancerobots.selfiebotdroid.Service.BotServerService;
/*todo:
login / identifaction setup with webapi
connect / pick connect target screen / api
allow/not allow connections from
*/

public class MainActivity extends FragmentActivity {
    private static final String TAG = "[[ MainActivity ]]";

    private EditText tbHeadId;
    private TextView lbInfo;
    private ViewGroup layoutConnectControls, layoutProgressControls;
    private Class mTargetServiceClass;
    private static final int MIN_HEAD_ID_LENGTH=5;

    private int visibilityState=-1;
    private void setVisibility(int state) {
        if(visibilityState==state) return; //no need to update anything
        visibilityState=state;
        switch (state)
        {
            case Messages.BOT_SERVICE_STATE_CONNECTED:
                layoutProgressControls.setVisibility(View.GONE);
                layoutConnectControls.setVisibility(View.GONE);
                lbInfo.setText(getString(R.string.msg_successful_connection));
                finish(); //shut off the main screen as overlay control buttons should be used with Global.publishProgress for notifications
                break;
            case Messages.BOT_SERVICE_STATE_WAIT:
                layoutProgressControls.setVisibility(View.VISIBLE);
                layoutConnectControls.setVisibility(View.GONE);
                lbInfo.setText(getString(R.string.msg_proxy_connected));
                break;
            case Messages.BOT_SERVICE_STATE_CONNECTING:
                layoutProgressControls.setVisibility(View.VISIBLE);
                layoutConnectControls.setVisibility(View.GONE);
                lbInfo.setText(getString(R.string.msg_connecting));
                break;
            case Messages.BOT_SERVICE_STATE_DISCONNECTED:
            case Messages.BOT_SERVICE_STATE_STOPPED:
                layoutProgressControls.setVisibility(View.GONE);
                layoutConnectControls.setVisibility(View.VISIBLE);
                lbInfo.setText(R.string.msg_ready_to_connect);
                break;
            default:
                Log.e(TAG, "Unknown bot service state"+state);
        }
    }
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String act=intent.getAction();
            if(act.equals(Messages.MSG_BOT_SERVICE_STATE_CHANGED)){
                Log.d(TAG,"Setting visibility to "+Messages.msgBotServiceStateToString(intent.getIntExtra(Messages.MSG_BOT_SERVICE_PARAMS_STATE,0)));
                setVisibility(intent.getIntExtra(Messages.MSG_BOT_SERVICE_PARAMS_STATE, 0));
            }
            else if(act.equals(Messages.MSG_BOT_SERVICE_MSG))
            {
                String msg=intent.getStringExtra(Messages.MSG_BOT_SERVICE_PARAMS_MSG);
                lbInfo.setText(msg);
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(BuildConfig.VERSION_NAME);
        if (!NetUtils.IsInternetOn(getApplicationContext())) {
            Global.toast(getApplicationContext(), getString(R.string.msg_connect_to_internet));
            //finish();
        }
        setContentView(R.layout.activity_main);
        tbHeadId = (EditText) findViewById(R.id.headId);
        lbInfo = (TextView) findViewById(R.id.lbInfo);
        layoutProgressControls = (ViewGroup) findViewById(R.id.layoutProgressControls);
        layoutConnectControls = (ViewGroup) findViewById(R.id.layoutConnectControls);
        findViewById(R.id.bnMain_Cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "Cancel clicked, sending Messages.CMD_BOT_SERVICE_DISCONNECT broadcast");
                sendBroadcast(new Intent(Messages.CMD_BOT_SERVICE_DISCONNECT));
            }
        });
        setVisibility(Messages.BOT_SERVICE_STATE_DISCONNECTED);
        Messages.registerBroadcastHandler(this, broadcastReceiver,
                Messages.MSG_BOT_SERVICE_STATE_CHANGED,
                Messages.MSG_BOT_SERVICE_MSG);
        String savedHeadId = Global.getKeyForValue(this, Messages.CMD_BOT_SERVICE_PARAMS_HEAD_ID);
        if (savedHeadId == null) {
            try {
                savedHeadId = Global.getPhoneNumber(this);
            }catch(Exception ex) { savedHeadId="";}
        }
        tbHeadId.setText(savedHeadId);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menuItemAbout) {
            startActivity(new Intent(getApplicationContext(), AboutActivity.class));
            return true;
        }
        else if(id==R.id.menuItemQuit)
        {
            sendBroadcast(new Intent(Messages.CMD_BOT_SERVICE_STOP));
            finish();
        }
        //todo: add settings such as relay server connection, configuration of the buttons, showing buttons on server or not, etc
        return super.onOptionsItemSelected(item);
    }

    public void makeServerOnClick(View view) {
        Log.i(TAG, "makeServerOnClick pressed");
        //button clicked to connect over bluetooth to platform and then connect to relay as the server
        if (NetUtils.IsInternetOn(this)) {
            if(!validateHeadId(true)) return;
            mTargetServiceClass= BotServerService.class;
            BtController.getBtDevice(this); //first start off with turning on bluetooth if needed and picking a device
            //--> function call results in onActivityResult (BT_REQUEST_ENABLE_RESULT or BT_DEVICE_PICKING_ACTIVITY_RESULT)
        } else {
            Global.toast(getApplicationContext(), getString(R.string.msg_connect_to_internet));
        }
    }
    public void startFaceTracking(View view) {
        Log.i(TAG, "startFaceTracking pressed");
        mTargetServiceClass= BotFaceTrackingService.class;
        if(Camera.isCameraReady(this))
        {
            BtController.getBtDevice(this); //first start off with turning on bluetooth if needed and picking a device
        }
    }
    public void startMotionDetection(View view) {
        Log.i(TAG, "startFaceTracking pressed");
        mTargetServiceClass= BotMotionDetectionService.class;
        /*
        if(Camera.isCameraReady(this))
        {
            BtController.getBtDevice(this); //first start off with turning on bluetooth if needed and picking a device
        }
        */

        //for now ignore the bt and just start
        setVisibility(Messages.BOT_SERVICE_STATE_CONNECTING);
        startService(Messages.createIntent(getApplicationContext(), mTargetServiceClass,
                Messages.CMD_BOT_SERVICE_START));
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode != Camera.RC_HANDLE_CAMERA_PERM) {
            Log.d(TAG, "Got unexpected permission result: " + requestCode);
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }
        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Camera permission granted - start service");
            BtController.getBtDevice(this); //first start off with turning on bluetooth if needed and picking a device
            return;
        }
        Global.toast(this, "Not allowed to use camera ");
    }
    public void startDemo(View view) {
        Log.i(TAG, "startDemo pressed");
        mTargetServiceClass= BotDemoService.class;
        BtController.getBtDevice(this); //first start off with turning on bluetooth if needed and picking a device
    }
    public void connectToRemoteBotOnClick(View view) {
        Log.i(TAG, "connectToRemoteBotOnClick pressed");
        //button clicked to act as client, just start the service to form a connection
        if (NetUtils.IsInternetOn(this)) {
            if(!validateHeadId(false)) return;
            setVisibility(Messages.BOT_SERVICE_STATE_CONNECTING);
            startService(Messages.createIntent(getApplicationContext(), BotClientService.class,
                    Messages.CMD_BOT_SERVICE_START, Messages.CMD_BOT_SERVICE_PARAMS_HEAD_ID, getHeadId()));
        } else {
            Global.toast(getApplicationContext(), getString(R.string.msg_connect_to_internet));
        }
    }

    private boolean validateHeadId(boolean isServer) {
        String headId= getHeadId();
        headId=headId.replaceAll("[^A-Za-z0-9@.]", "");
        tbHeadId.setText(headId);
        if(headId.length()<MIN_HEAD_ID_LENGTH)
        {
            Global.toast(this,getString(R.string.msg_min_head_id));
            return false;
        }
        Global.saveKeyValue(this, Messages.CMD_BOT_SERVICE_PARAMS_HEAD_ID, headId);
        return true;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "get Result:" + "requestCode" + requestCode + "resultCode" + resultCode);
        switch (requestCode) {
            case Messages.BT_REQUEST_ENABLE_RESULT:  //bluetooth has been enabled, lets pick a device
                if (resultCode == Activity.RESULT_OK) {
                    Log.i(TAG, getString(R.string.msg_bluetooth_enabled));
                    Global.publishProgress(this, getString(R.string.msg_bluetooth_enabled));
                    BtController.getBoundBtDevices(this);
                    //-->results in onActivityResult(BT_DEVICE_PICKING_ACTIVITY_RESULT) after Bluetooth device is picked
                } else if (resultCode == Activity.RESULT_CANCELED) {
                    Log.i(TAG, getString(R.string.msg_bluetooth_not_enabled));
                    Global.publishProgress(this, getString(R.string.msg_bluetooth_not_enabled));
                }
                break;
            case Messages.BT_DEVICE_PICKING_ACTIVITY_RESULT: //blue tooth device has been picked
                switch (resultCode) {
                    case RESULT_OK:
                        setVisibility(Messages.BOT_SERVICE_STATE_CONNECTING);
                        String macAddr= data.getStringExtra(BtDevicePickingActivity.BLUETOOTH_MAC);
                        Log.d(TAG, "Picked device : " + macAddr);
                        startService(Messages.createIntent(getApplicationContext(), mTargetServiceClass,
                                Messages.CMD_BOT_SERVICE_START,
                                Messages.CMD_BOT_SERVICE_PARAMS_HEAD_ID, getHeadId(),
                                Messages.CMD_BOT_SERVICE_PARAMS_MAC_ADDR, macAddr));
                        break;
                    case RESULT_CANCELED:
                        Log.i(TAG, "Bluetooth device picker canceled");
                        break;
                }
                break;
        }
    }
    private String getHeadId() { return tbHeadId.getText().toString(); }
    @Override
    protected void onResume() {
        super.onResume();
        sendBroadcast(new Intent(Messages.MSG_MAIN_ACTIVITY_STARTED));
    }
    @Override
    protected void onDestroy() {
        Log.i(TAG, "onDestroy");
        /* Toast.makeText(getApplicationContext(), getString(R.string.msg_connection_closed), Toast.LENGTH_LONG).show(); */
        try {
            unregisterReceiver(broadcastReceiver);
        } catch (Exception ex){}
        super.onDestroy();
    }
}