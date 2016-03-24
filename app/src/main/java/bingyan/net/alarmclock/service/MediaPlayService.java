package bingyan.net.alarmclock.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.Vibrator;

import java.io.IOException;

import bingyan.net.alarmclock.AlarmClockInfo;
import bingyan.net.alarmclock.broadcast.CancelClockReceiver;

public class MediaPlayService extends Service implements SensorEventListener {

    public static final String ALARM_CLOCK_INFO = "alarm.clock.info";
    public static final String ALARM_CLOCK_POSITION = "alarm.clock.position";

    private static MediaPlayer mp;
    private static Vibrator vibrator;

    private AlarmClockInfo info;

    private SensorManager sensorManager;

    private int position;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        // 传感器监听
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);

        info = (AlarmClockInfo) intent.getSerializableExtra(ALARM_CLOCK_INFO);
        position = intent.getIntExtra(ALARM_CLOCK_POSITION, -1);

        // 开始闹铃和振动
        new Thread(new Runnable() {
            @Override
            public void run() {
                startAlarm(info);
            }
        }).start();

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        boolean isScreenOn;
        if (Build.VERSION.SDK_INT >= 20) {
            isScreenOn = pm.isInteractive();
        } else {
            //noinspection deprecation
            isScreenOn = pm.isScreenOn();
        }

        if (isScreenOn) {// 非锁屏状态处理逻辑

        } else {// 锁屏状态处理逻辑

        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void startAlarm(final AlarmClockInfo info) {
        if (info != null) {
            // 播放音乐
            if (mp == null) {
                mp = new MediaPlayer();
            }
            try {
                mp.reset();
                mp.setDataSource(Uri.parse(info.ringSongPath).getPath());
                mp.prepare();
                if (!mp.isPlaying()) {
                    mp.start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            // 振动
            if (info.isVibrate) {
                if (vibrator == null) {
                    vibrator = (Vibrator) getSystemService(Service.VIBRATOR_SERVICE);
                }
                long[] pattern = {0, 400, 500};
                vibrator.vibrate(pattern, 0);
            }
        }
    }

    public void cancel() {
        if (mp != null && mp.isPlaying()) {
            mp.stop();
            mp.reset();
            mp.release();
            mp = null;
        }

        if (vibrator != null && vibrator.hasVibrator()) {
            vibrator.cancel();
            vibrator = null;
        }

        Intent intent = new Intent(CancelClockReceiver.ACTION_CANCEL_CLOCK);
        intent.putExtra(ALARM_CLOCK_POSITION, position);
        sendBroadcast(intent);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        int sensorType = event.sensor.getType();
        // values[0]:X轴，values[1]：Y轴，values[2]：Z轴
        float[] values = event.values;
        if (sensorType == Sensor.TYPE_ACCELEROMETER)
        {
            if ((Math.abs(values[0]) > 17 || Math.abs(values[1]) > 17 || Math
                    .abs(values[2]) > 17))
            {
                cancel();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
