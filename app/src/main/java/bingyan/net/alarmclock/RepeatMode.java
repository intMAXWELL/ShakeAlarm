package bingyan.net.alarmclock;

/**
 * Created by Demon on 2016/3/16.
 * 闹钟重复的模式
 */
public enum RepeatMode {
    ONCE,
    EVERYDAY,
    EXCEPT_HOLIDAY, // 周一至周五
    CUSTOM // 自定义一周的几天
}
