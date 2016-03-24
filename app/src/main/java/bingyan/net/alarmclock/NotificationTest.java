package bingyan.net.alarmclock;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;

import bingyan.net.alarmclock.broadcast.CancelNotificationReceiver;

public class NotificationTest extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_test);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.drawable.ic_alarm_add_white_48dp)
                .setContentTitle("花开")
                .setContentText("您收到了一条新消息");
        // Add your branding color on 5.0+devices
        builder.setColor(Color.BLUE);
        // Set style
        Intent mute = new Intent(CancelNotificationReceiver.ACTION_CANCEL_NOTIFICATION);
        mute.setAction("hh");
        PendingIntent muteIntent = PendingIntent.getBroadcast(this
                , 0, mute, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.addAction(R.drawable.ic_alarm_add_white_48dp, "Mute", muteIntent);

        Notification notification = builder.build();

        NotificationManagerCompat.from(this).notify(AlarmNotificationHelper.notificationID, notification);
    }
}
