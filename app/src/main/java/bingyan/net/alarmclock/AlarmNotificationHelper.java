package bingyan.net.alarmclock;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Demon on 2016/3/23.
 * 记录通知id与alarm_position的对应关系
 */
public class AlarmNotificationHelper {

    private static Map<Integer, Integer> association = new HashMap<>();

    public static int notificationID = 0;

    public static void put(int positionInList) {
        association.put(notificationID, positionInList);
        notificationID++;
    }

    public static int get(int notificationID) {
        return association.get(notificationID);
    }

}
