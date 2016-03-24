package bingyan.net.alarmclock.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class RestartAlarmClockManageReceiver extends BroadcastReceiver {

    public static final String ACTION = "bingyan.net.alarmclock.broadcast.restartalarmclockManageBC";

    public interface RestartAlarmClockManageCallBack{
        void restart();
    }

    private RestartAlarmClockManageCallBack callback;

    public void setRestartAlarmClockManage(RestartAlarmClockManageCallBack clockManage) {
        this.callback = clockManage;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (callback != null) {
            callback.restart();
        }
    }
}
