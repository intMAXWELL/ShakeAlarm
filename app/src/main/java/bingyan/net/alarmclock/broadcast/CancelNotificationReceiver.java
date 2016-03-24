package bingyan.net.alarmclock.broadcast;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import bingyan.net.alarmclock.AlarmNotificationHelper;

public class CancelNotificationReceiver extends BroadcastReceiver {

    public static String ACTION_CANCEL_NOTIFICATION = "bingyan.net.notificationcompattest.CancelNotificationReceiver";

    public CancelNotificationReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(getClass().getName(), "onReceive");
        Toast.makeText(context, "消息已忽略", Toast.LENGTH_SHORT).show();
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(AlarmNotificationHelper.notificationID);

    }
    }
