package bingyan.net.alarmclock.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import bingyan.net.alarmclock.service.MediaPlayService;

public class CancelClockReceiver extends BroadcastReceiver {

    public static String ACTION_CANCEL_CLOCK = "bingyan.net.alarmclock.cancelclockreceiver.action.cancel.clock";

    public CancelClockReceiver() {

    }

    private CancelClockCallback callback;

    public void setActionCancelClockCallback(CancelClockCallback cancelClockCallback) {
        callback = cancelClockCallback;
    }


    public interface CancelClockCallback{
        void CancelClock(int position);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("receive", "onReceive");
        int position = intent.getIntExtra(MediaPlayService.ALARM_CLOCK_POSITION, -1);
        if (position >= 0) {
            callback.CancelClock(position);
        }
    }
}
