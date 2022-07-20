package com.example.daily_selfie;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

public class AlarmReceiver extends BroadcastReceiver {

    public static final int NOTIFICATION_ID = 1;

    // Notification action elements
    private Intent mNotificationIntent;
    private PendingIntent mPendingIntent;

    // Notification sound and vibration on arrival
    private final Uri soundURI = Uri.parse("android.resource://com.example.daily_selfie/" + R.raw.alarm_rooster);
    //private final long[] mVibrationPattern = { 0, 200, 200, 300 };

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            mNotificationIntent = new Intent(context, MainActivity.class);
            mPendingIntent = PendingIntent.getActivity(context, 0, mNotificationIntent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_CANCEL_CURRENT);

            // Build notification
            Notification.Builder notificationBuilder = new Notification.Builder(context)
                    .setTicker("Time for another selfie")
                    .setSmallIcon(R.drawable.ic_camera)
                    .setAutoCancel(true)
                    .setContentTitle(context.getString(R.string.app_name))
                    .setContentText("Đến giờ chụp hình!")
                    .setContentIntent(mPendingIntent);

            String channelId = "ALARM";
        }
        catch (Exception exception) {
            Log.d("NOTIFICATION", exception.getMessage().toString());
        }
    }

}

