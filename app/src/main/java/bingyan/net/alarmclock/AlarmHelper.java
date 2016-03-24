package bingyan.net.alarmclock;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import bingyan.net.alarmclock.service.MediaPlayService;

/**
 * Created by Demon on 2016/3/22.
 * AlarmClock调度逻辑
 */
public class AlarmHelper {

    private static String ACTION_CLOCK_PENDING_INTENT = "bingyan.net.alarmclock.AlarmHelper.action.clock.pendingintent";
    private static List<AlarmClockInfo> clockInfoList = new ArrayList<>();

    // 记录开启过的pendingIntent
    private static List<PendingIntent> pendingIntentList = new ArrayList<>();

    // 记录PendingIntent的position与所在clockInfoList的position对应关系
    // 前一个Integer存储clockInfoList的position，后一个Integer存储pendingIntentList的position
    private static Map<Integer, Integer> associationMap = new HashMap<>();

    private static AlarmManager alarmManager;

    private static AlarmHelper alarmHelper;

    private Context context;

    private AlarmHelper(Context context) {
        this.context = context;
    }

    public static AlarmHelper getInstance(Context context) {
        if (alarmHelper == null) {
            alarmHelper = new AlarmHelper(context);
        }
        return alarmHelper;
    }

    public void addClock(AlarmClockInfo info) {
        if (clockInfoList == null) {
            clockInfoList = new ArrayList<>();
        }
        clockInfoList.add(info);
        addPendingIntent(info, clockInfoList.size() - 1);
    }

    public void addClocks(List<AlarmClockInfo> infoList) {
        AlarmClockInfo info;
        for (int i = 0; i < infoList.size(); i++) {
            info = infoList.get(i);
            addPendingIntent(info, i + clockInfoList.size());
        }
        clockInfoList.addAll(infoList);
    }


    /**
     * @param info     需要添加的闹钟
     * @param position 闹钟list的position
     */
    private void addPendingIntent(AlarmClockInfo info, int position) {

        if (!info.isOpen) return;// 若未开启,不开启对应闹钟提醒

        alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, MediaPlayService.class);
        intent.putExtra(MediaPlayService.ALARM_CLOCK_INFO, info);
        intent.putExtra(MediaPlayService.ALARM_CLOCK_POSITION, position);
        intent.setAction(ACTION_CLOCK_PENDING_INTENT + position);// Make pi unique
        PendingIntent pi = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        // Add to pendingIntentList for  further cancel.
        if (pendingIntentList == null) {
            pendingIntentList = new ArrayList<>();
        }
        pendingIntentList.add(pi);
        // 记录position依赖关系
        associationMap.put(position, pendingIntentList.size() - 1);
        alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + info.generateDueTimeInMillions(), pi);
    }

    /**
     *
     * @param position AlarmClockInfo在List中的位置
     * @param info 需要修改的AlarmClockInfo
     */
    public void changeClock(int position, AlarmClockInfo info) {
        changePendingIntent(position, info);
        clockInfoList.set(position, info);
    }

    private void changePendingIntent(int position, AlarmClockInfo info) {
        PendingIntent pi;
        int piPosition;
        if (associationMap.containsKey(position)) {// 存在表明闹钟开启，取消之
            piPosition = associationMap.get(position);
            // 取消PendingIntent
            alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            pi = pendingIntentList.get(piPosition);
            alarmManager.cancel(pi);
            if (info.isOpen) {// 修改后的闹钟为开启状态
                Intent intent = new Intent(context, MediaPlayService.class);
                intent.putExtra(MediaPlayService.ALARM_CLOCK_INFO, info);
                intent.putExtra(MediaPlayService.ALARM_CLOCK_POSITION, position);
                intent.setAction(ACTION_CLOCK_PENDING_INTENT + position);// Make pi unique
                pi = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                pendingIntentList.set(piPosition, pi);
                alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + info.generateDueTimeInMillions(), pi);
            } else {// 修改后的闹钟为关闭状态，删除相关信息
                pendingIntentList.remove(piPosition);// 删除list中元素
                // 同时对应HashMap中的piPosition后的元素需要更新
                Iterator<Map.Entry<Integer, Integer>> iter = associationMap.entrySet().iterator();
                Map.Entry<Integer, Integer> entry;int key;int value;
                while (iter.hasNext()) {
                    entry = iter.next();
                    key = entry.getKey();
                    value = entry.getValue();
                    if (value > piPosition) {
                        associationMap.put(key, value - 1);// 更新
                    }
                }
                associationMap.remove(position);
            }
        } else {// 闹钟原先未开启
            if (info.isOpen) {// 修改后的闹钟为开启状态，则添加
                addPendingIntent(info, position);
            }
        }
    }
}
