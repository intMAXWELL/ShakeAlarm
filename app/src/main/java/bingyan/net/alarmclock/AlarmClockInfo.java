package bingyan.net.alarmclock;

import java.io.Serializable;
import java.util.Calendar;

/**
 * Created by Demon on 2016/3/16.
 * 闹钟实体类
 */
public class AlarmClockInfo implements Serializable {

    public String time,remark,ringSongPath;
    public boolean isOpen,isVibrate;
    public RepeatMode repeatMode;

    public static long ONE_DAY_IN_MILLIONS = 24 * 60 * 60 * 1000;// 一天的毫秒数

    public AlarmClockInfo(String time, String remark, boolean isOpen) {
        this.time = time;
        this.remark = remark;
        this.isOpen = isOpen;
        this.repeatMode = RepeatMode.ONCE;
        this.isVibrate = false;
        this.ringSongPath="";
    }

    public boolean equals(AlarmClockInfo o) {
        return time.equals(o.time) && remark.equals(o.remark) && ringSongPath.equals(o.ringSongPath)
                && isVibrate == o.isVibrate && repeatMode.equals(o.repeatMode) && isOpen == o.isOpen;
    }

    public AlarmClockInfo(AlarmClockInfo o) {
        this.time = o.time;
        this.remark = o.remark;
        this.ringSongPath = o.ringSongPath;
        this.isOpen = o.isOpen;
        this.isVibrate = o.isVibrate;
        this.repeatMode = o.repeatMode;
    }

    public int getHour() throws Exception {
        String[] ss = time.split(":");
        if (ss.length != 2) {
            throw new Exception("Invalid time string!");
        }
        return Integer.valueOf(ss[0]);
    }

    public int getMinute() throws Exception {
        String[] ss = time.split(":");
        if (ss.length != 2) {
            throw new Exception("Invalid time string!");
        }
        return Integer.valueOf(ss[1]);
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public static String generateFormatTimeStr(int hour,int minute) {
        String time;
        // 时间格式处理
        if (hour >= 10 && minute >= 10) {
            time = String.format("%d:%d", hour, minute);
        }
        else if (hour >= 10 && minute < 10) {
            time = String.format("%d:0%d",hour, minute);
        }
        else if (hour < 10 && minute >= 10) {
            time = String.format("0%d:%d", hour, minute);
        } else {
            time = String.format("0%d:0%d", hour,minute);
        }
        return time;
    }

    private boolean needOverOneDay() throws Exception {
        int hour = getHour();
        int minute = getMinute();
        Calendar c = Calendar.getInstance();
        int currentH = c.get(Calendar.HOUR_OF_DAY);
        int currentM = c.get(Calendar.MINUTE);

        int differenceM = (hour - currentH) * 60 + (minute - currentM) - 1;

        return differenceM < 0;
    }

    public  String generateDueTime() throws Exception {
        int hour = getHour();
        int minute = getMinute();
        Calendar c = Calendar.getInstance();
        int currentH = c.get(Calendar.HOUR_OF_DAY);
        int currentM = c.get(Calendar.MINUTE);

        int differenceM = (hour - currentH) * 60 + (minute - currentM) - 1;
        if (differenceM < 0) {
            differenceM += 24 * 60;
        }

        hour = differenceM / 60;
        minute = differenceM % 60;
        return String.format("%d小时%d分钟后响铃", hour, minute);
    }

    public long generateDueTimeInMillions() {
        long dueTime = 0;
        if (repeatMode.equals(RepeatMode.ONCE)) {
            try {
                dueTime = generateDueTimeOnce();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else if (repeatMode.equals(RepeatMode.EVERYDAY)) {
            try {
                dueTime = generateDueTimeEveryDay();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else if (repeatMode.equals(RepeatMode.EXCEPT_HOLIDAY)) {
            try {
                dueTime = generateDueTimeExceptHoliday();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return dueTime;
    }

    public long generateDueTimeExceptHoliday() throws Exception {
        long dueTime = generateDueTimeOnce();
        Calendar c = Calendar.getInstance();
        int today = c.get(Calendar.DAY_OF_WEEK);
        if (today == Calendar.FRIDAY) {// 礼拜五
            dueTime += 2 * ONE_DAY_IN_MILLIONS;// +2 days
        }else if (today == Calendar.SATURDAY) {
            dueTime += ONE_DAY_IN_MILLIONS;
        }
        if (!needOverOneDay()) {// 不需要跨天
            dueTime += ONE_DAY_IN_MILLIONS;
        }
        return dueTime;
    }
    public long generateDueTimeOnce() throws Exception {

        int hour = getHour();
        int minute = getMinute();
        Calendar c = Calendar.getInstance();
        int currentH = c.get(Calendar.HOUR_OF_DAY);
        int currentM = c.get(Calendar.MINUTE);

        int differenceM = (hour - currentH) * 60 + (minute - currentM) - 1;
        if (differenceM < 0) {
            differenceM += 24 * 60;
        }

        hour = differenceM / 60;
        minute = differenceM % 60;

        return (hour * 60 + minute) * 60 * 1000;
    }

    public long generateDueTimeEveryDay() throws Exception {
        return generateDueTimeOnce();
    }


}
