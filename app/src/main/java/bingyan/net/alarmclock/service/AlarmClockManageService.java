package bingyan.net.alarmclock.service;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;

import java.util.List;

import bingyan.net.alarmclock.AlarmClockInfo;
import bingyan.net.alarmclock.AlarmClockInfoListSeri;
import bingyan.net.alarmclock.AlarmHelper;

public class AlarmClockManageService extends IntentService {

    private static final String ACTION_START_MANAGE = "bingyan.net.alarmclock.action.start.manage";
    private static final String ACTION_CHANGE_CLOCK = "bingyan.net.alarmclock.action.change.clock";
    private static final String ACTION_ADD_CLOCK = "bingyan.net.alarmclock.action.add.clock";

    private static final String PARAM_INFO_LIST_CONTENT = "bingyan.net.alarmclock.param.info.list.content";
    private static final String PARAM_ALARM_INFO_CONTENT = "bingyan.net.alarm.clock.param.alarm.info.content";
    private static final String PARAM_ALARM_INFO_POSITION = "bingyan.net.alarm.clock.param.alarm.info.position";

    // Single instance
    private static AlarmHelper helper;

    public AlarmClockManageService() {
        super("AlarmClockManageService");
    }

    public static void startActionManageAlarm(Context context, List<AlarmClockInfo> infoList) {
        Intent intent = new Intent(context, AlarmClockManageService.class);
        intent.setAction(ACTION_START_MANAGE);
        intent.putExtra(PARAM_INFO_LIST_CONTENT, new AlarmClockInfoListSeri(infoList));
        helper = AlarmHelper.getInstance(context);
        context.startService(intent);
    }

    public static void startActionChangeAlarm(Context context, AlarmClockInfo info, int position) {
        Intent intent = new Intent(context, AlarmClockManageService.class);
        intent.setAction(ACTION_CHANGE_CLOCK);
        intent.putExtra(PARAM_ALARM_INFO_CONTENT, info);
        intent.putExtra(PARAM_ALARM_INFO_POSITION, position);
        context.startService(intent);
    }

    public static void startActionAddAlarm(Context context, AlarmClockInfo info) {
        Intent intent = new Intent(context, AlarmClockManageService.class);
        intent.setAction(ACTION_ADD_CLOCK);
        intent.putExtra(PARAM_ALARM_INFO_CONTENT, info);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_START_MANAGE.equals(action)) {
                AlarmClockInfoListSeri listSeri = (AlarmClockInfoListSeri) intent.getSerializableExtra(PARAM_INFO_LIST_CONTENT);
                helper.addClocks(listSeri.alarmClockInfos);
            }
            else if (ACTION_CHANGE_CLOCK.equals(action)) {
                AlarmClockInfo info = (AlarmClockInfo) intent.getSerializableExtra(PARAM_ALARM_INFO_CONTENT);
                int position = intent.getIntExtra(PARAM_ALARM_INFO_POSITION, -1);
                if (position >= 0) {
                    helper.changeClock(position, info);
                }
            }
            else if (ACTION_ADD_CLOCK.equals(action)) {
                AlarmClockInfo info = (AlarmClockInfo) intent.getSerializableExtra(PARAM_ALARM_INFO_CONTENT);
                helper.addClock(info);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        // 重启服务
//        Intent intent = new Intent(RestartAlarmClockManageReceiver.ACTION);
//        sendBroadcast(intent);
    }
}
