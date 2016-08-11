package com.endurancerobots.selfiebotdroid.Common;


import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import com.endurancerobots.selfiebotdroid.MainActivity;
import com.endurancerobots.selfiebotdroid.R;

public class Global {
    public static final String SETTINGS_NAME = "com.endurancerobots.selfiebotdroid.settings";
    public static String getPhoneNumber(Context c)
    {
        TelephonyManager telephonyManager = (TelephonyManager)c.getSystemService(Context.TELEPHONY_SERVICE);
        String phoneNumber = telephonyManager.getLine1Number();//получаем номер телефона
        return phoneNumber;
    }
    public static String getKeyForValue(Context c,String key){
        // Restore preferences
        SharedPreferences settings = c.getSharedPreferences(SETTINGS_NAME, 0);
        return settings.getString(key, null);
    }
    public static void saveKeyValue(Context c, String key, String val){
        SharedPreferences settings = c.getSharedPreferences(SETTINGS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(key,val);
        editor.commit();
    }

    public static void toast(Context c, String s)
    {
        Toast.makeText(c,s,Toast.LENGTH_SHORT).show();
    }

    public static final int NOTIFICATION_ID = 101;
    /** Make notification in status bar on top of the phone*/
    public static void publishProgress(Context context, String s) {
        Messages.broadcast(context,Messages.MSG_BOT_SERVICE_MSG,Messages.MSG_BOT_SERVICE_PARAMS_MSG,s);
            //broadcast message in case MainActivity is able to display
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        Notification notification = buildNotification(context, s);
        notificationManager.notify(NOTIFICATION_ID, notification);
    }
    public static Notification buildNotification(Context context, String s) {
        Intent notificationIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context.getApplicationContext(), 0,
                notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setContentIntent(pendingIntent)   //pending intent will pull up MainActivity if notification is clicked
                .setSmallIcon(R.mipmap.ic_launcher)
                .setTicker(s)
                .setPriority(Notification.PRIORITY_HIGH)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(s); // show the message

        return builder.build();
    }
    public static void clearProgress(Context c)
    {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(c);
        notificationManager.cancel(NOTIFICATION_ID);
    }

}
