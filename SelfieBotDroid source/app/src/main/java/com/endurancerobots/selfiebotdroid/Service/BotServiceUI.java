package com.endurancerobots.selfiebotdroid.Service;

import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.PopupMenu;
import com.endurancerobots.selfiebotdroid.Common.Commands;
import com.endurancerobots.selfiebotdroid.Common.Messages;
import com.endurancerobots.selfiebotdroid.R;

import java.util.ArrayList;
import java.util.List;

/*
 handles floating (overlay) button UI which appears once connection has been established
 allows toggling buttons (pressed/non pressed image) through toggle() function
 uses broadcast messages to communicate UI events to service
    (not entirely true as we forward a ready network message rather than button event)
 4 control buttons + 1 button calls up pop-up menu
 */
public class BotServiceUI {
    private Handler mHandler;
    private WindowManager mWinMgr;
    private Context mContext;
    private Button bnBotControlUp, bnBotControlDown, bnBotControlLeft, bnBotControlRight, bnBotControlMenu;
    private List<View> mViews;
    private static final String TAG = "[[BotServiceUI]]";
    public BotServiceUI()
    {
        mHandler = new Handler(); //create a message rerouter on UI thread (since onCreate is always called from a UI thread)
    }
    public void runOnUiThread(Runnable runnable) {
        mHandler.post(runnable);
    }

    public void showUI(Context c) {
        Log.i(TAG, "showUI");
        if(mWinMgr!=null)
        {
            Log.d(TAG, " UI already visible so now point to show again");
            return;
        }
        mContext=c;
        mWinMgr = (WindowManager) mContext.getSystemService(mContext.WINDOW_SERVICE);
        mViews=new ArrayList<View>();
        setupButton(bnBotControlUp = new Button(c), Commands.UpMsg, R.drawable.arrow_white_up, R.drawable.arrow_white_up_pressed, Gravity.TOP | Gravity.CENTER_HORIZONTAL, true, false);
        setupButton(bnBotControlDown = new Button(c), Commands.DownMsg, R.drawable.arrow_white_down, R.drawable.arrow_white_down_pressed, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, true, false);
        setupButton(bnBotControlLeft = new Button(c), Commands.LeftMsg, R.drawable.arrow_white_left, R.drawable.arrow_white_left_pressed, Gravity.LEFT | Gravity.CENTER_VERTICAL, true, false);
        setupButton(bnBotControlRight = new Button(c), Commands.RightMsg, R.drawable.arrow_white_right, R.drawable.arrow_white_right_pressed, Gravity.RIGHT | Gravity.CENTER_VERTICAL, true, false);
        setupButton(bnBotControlMenu = new Button(c), null, R.drawable.selfie_menu, 0, Gravity.BOTTOM | Gravity.LEFT, false, true);
        bnBotControlMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handlePopupMenu(v);
            }
        });
    }
    private void setupButton(Button b, byte[] cmd, int drawable, int drawablePressed, int gravity, boolean isLarge, boolean customTouchListener)
    {
        setDrawable(b, drawable);
        if(!customTouchListener)
        {
            CtrlButtonStateController stateController=new CtrlButtonStateController(cmd, drawable, drawablePressed);
            b.setTag(R.id.TAG_STATE_CONTROLLER, stateController);
            b.setOnTouchListener(stateController);
       }
        addBn(b,gravity,isLarge);
    }
    private void setDrawable(View v, int drawable)
    {
        if(android.os.Build.VERSION.SDK_INT >= 21){
            v.setBackgroundDrawable(mContext.getResources().getDrawable(drawable, mContext.getTheme()));
        } else {
            v.setBackgroundDrawable(mContext.getResources().getDrawable(drawable));
        }
    }
    private class CtrlButtonStateController implements  View.OnTouchListener {
        private boolean isPressed;
        private int mDrawable, mDrawablePressed;
        private byte[] mCmd;
        public CtrlButtonStateController(byte[] cmd, int drawable, int drawablePressed)
        {
            isPressed=false;
            mCmd=cmd; mDrawable=drawable; mDrawablePressed=drawablePressed;
        }
        public void setPressed(boolean pressed,Button b)
        {
            isPressed=pressed;
            setDrawable(b, isPressed? mDrawablePressed : mDrawable);
        }
        public void toggle(Button b)
        {
            setPressed(!isPressed,b);
        }
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    isPressed=true;
                    writeCmd(mCmd);
                    break;
                case MotionEvent.ACTION_UP:
                    isPressed=false;
                    writeCmd(Commands.StopMsg);//mCmd);
                    break;
            }
            setDrawable(v, isPressed? mDrawablePressed : mDrawable);
            return true;
        }
    }
    private void writeCmd(byte[] cmd)
    {
        Messages.broadcast(mContext, Messages.CMD_BOT_SERVICE_WRITE_CMD, Messages.CMD_BOT_SERVICE_PARAMS_WRITE_CMD, cmd);
        //will get handled in broadcastReceiver of the service (BotServer or BotClient)
    }
    public void addBottom(View newV, int gravity, int w, int h, int padding) {
        for (View v : mViews) {
            mWinMgr.removeView(v);
        }
        add(newV, gravity, w, h, padding, true);
        for(int i=1;i<mViews.size();i++)
        {
            View v=mViews.get(i);
            mWinMgr.addView(v, (WindowManager.LayoutParams)v.getTag(R.id.TAG_LAYOUT_PARAMS));
        }
    }
    public void add(View v, int gravity, int w, int h, int padding) {
        add(v, gravity, w, h, padding, false);
    }
    private void add(View v, int gravity, int w, int h, int padding, boolean isBottom)
    {
        final int LayoutParamFlags = WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(w, h, WindowManager.LayoutParams.TYPE_PRIORITY_PHONE, LayoutParamFlags, PixelFormat.TRANSLUCENT);
        lp.gravity = gravity;
        lp.horizontalMargin=padding/100.0f;
        lp.verticalMargin=padding/100.0f;
        v.setTag(R.id.TAG_LAYOUT_PARAMS, lp);
        mWinMgr.addView(v, lp);
        if(isBottom) mViews.add(0, v);
        else mViews.add(v);
    }

    private void addBn(Button bn, int gravity, boolean large) {
        final int LayoutParamFlags = WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        int w = (int) mContext.getResources().getDimension(large ? R.dimen.button_width : R.dimen.smallButton);
        int h = (int) mContext.getResources().getDimension(large ? R.dimen.button_height : R.dimen.smallButton);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(w, h, WindowManager.LayoutParams.TYPE_PRIORITY_PHONE,
                LayoutParamFlags, PixelFormat.TRANSLUCENT);
        lp.gravity = gravity;

        lp.verticalMargin = mContext.getResources().getDimension(R.dimen.button_margin) /100.0f;
        lp.horizontalMargin = mContext.getResources().getDimension(R.dimen.button_margin) / 100.0f;
        bn.setTag(R.id.TAG_LAYOUT_PARAMS,lp);
        mWinMgr.addView(bn, lp);
        mViews.add(bn);
    }
    public void setButtonPressed(byte cmd)
    {
        if(mWinMgr==null) return; //UI not visible, don't bother with buttons
        setButtonsUnpressed();
        Button b=getButton(cmd);
        if(b==null) return;
        ((CtrlButtonStateController)b.getTag(R.id.TAG_STATE_CONTROLLER)).setPressed(true, b);
    }
    public void setButtonsUnpressed()
    {
        if(mWinMgr==null) return; //UI not visible, don't bother with buttons
        ((CtrlButtonStateController)bnBotControlUp.getTag(R.id.TAG_STATE_CONTROLLER)).setPressed(false, bnBotControlUp);
        ((CtrlButtonStateController)bnBotControlDown.getTag(R.id.TAG_STATE_CONTROLLER)).setPressed(false, bnBotControlDown);
        ((CtrlButtonStateController)bnBotControlLeft.getTag(R.id.TAG_STATE_CONTROLLER)).setPressed(false, bnBotControlLeft);
        ((CtrlButtonStateController)bnBotControlRight.getTag(R.id.TAG_STATE_CONTROLLER)).setPressed(false, bnBotControlRight);
    }
    private Button getButton(byte cmd)
    {
        switch (cmd)
        {
            case Commands.UP : return bnBotControlUp;
            case Commands.DOWN : return bnBotControlDown;
            case Commands.LEFT : return bnBotControlLeft;
            case Commands.RIGHT : return bnBotControlRight;
        }
        return null;
    }
    public void toggleCtrlButton(byte cmd)
    {
        if(mWinMgr==null) return; //UI not visible, don't bother with buttons
        Button b=getButton(cmd);
        if(b==null) return;
        ((CtrlButtonStateController)b.getTag(R.id.TAG_STATE_CONTROLLER)).toggle(b);
    }
    private void handlePopupMenu(View v)
    {
        PopupMenu popup = new PopupMenu(mContext, v);
        popup.getMenuInflater().inflate(R.menu.menu_bot, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.menuItemHide) {
                    hide();
                } else if (item.getItemId() == R.id.menuItemExit) {
                    mContext.sendBroadcast(new Intent(Messages.CMD_BOT_SERVICE_STOP));
                }
                return true;
            }
        });
        popup.show();
    }
    public void hide() {
        if (mWinMgr != null) {
            if(mViews!=null) {
                for (View v : mViews) {
                    mWinMgr.removeView(v);
                }
                mViews.clear();
                mViews = null;
            }
        }
        mContext = null;
        mWinMgr = null;
    }
}

