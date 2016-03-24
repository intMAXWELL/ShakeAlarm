package bingyan.net.alarmclock;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.JsonReader;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import bingyan.net.alarmclock.activity.AlarmSettingsActivity;

/**
 * Created by Demon on 2016/3/17.
 *
 */
public class MyJsonHelper {

    public static final String INVALID = "JSONException";
    public static final String ALARM_CLOCK_SET = "alarm_clock_set";

    public static String writeAlarmInfoJson(AlarmClockInfo alarmClockInfo){

        JSONStringer writer = new JSONStringer();
        try {
            writer.object();
            writer.key("time").value(alarmClockInfo.time);
            writer.key("remark").value(alarmClockInfo.remark);
            writer.key("ringSongPath").value(alarmClockInfo.ringSongPath);
            writer.key("isOpen").value(alarmClockInfo.isOpen);
            writer.key("isVibrate").value(alarmClockInfo.isVibrate);
            writer.key("repeatMode").value(alarmClockInfo.repeatMode.name());
            writer.endObject();
            return writer.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return INVALID;
    }

    public static List<AlarmClockInfo> readAlarmInfos(Context context) throws JSONException {
        List<AlarmClockInfo> alarmClockInfoList = new ArrayList<>();
        SharedPreferences sharedPreferences = context.getSharedPreferences(MyJsonHelper.ALARM_CLOCK_SET, Context.MODE_PRIVATE);
        Map alarms = sharedPreferences.getAll();
        AlarmClockInfo info;
        for (int i = 0; i < alarms.size(); i++) {
            String jsonStr = (String) alarms.get(String.valueOf(i));
            JSONObject js = new JSONObject(jsonStr);
            info = new AlarmClockInfo(js.getString("time"), js.getString("remark"), js.getBoolean("isOpen"));
            info.ringSongPath = js.getString("ringSongPath");
            info.isVibrate = js.getBoolean("isVibrate");
            info.repeatMode = AlarmSettingsActivity.getRepeatModeEnum(js.getString("repeatMode"));
            alarmClockInfoList.add(info);
        }
        return alarmClockInfoList;
    }

}
