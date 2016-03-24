package bingyan.net.alarmclock;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Demon on 2016/3/23.
 * 序列化
 */
public class AlarmClockInfoListSeri implements Serializable {

    public List<AlarmClockInfo> alarmClockInfos;

    public AlarmClockInfoListSeri(List<AlarmClockInfo> alarmClockInfos) {
        this.alarmClockInfos = alarmClockInfos;
    }

}
