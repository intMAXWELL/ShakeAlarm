package bingyan.net.alarmclock.activity;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import bingyan.net.alarmclock.AlarmClockInfo;
import bingyan.net.alarmclock.AlarmListAdapter;
import bingyan.net.alarmclock.MyJsonHelper;
import bingyan.net.alarmclock.R;
import bingyan.net.alarmclock.RepeatMode;
import bingyan.net.alarmclock.broadcast.CancelClockReceiver;
import bingyan.net.alarmclock.broadcast.RestartAlarmClockManageReceiver;
import bingyan.net.alarmclock.service.AlarmClockManageService;

public class MainActivity extends AppCompatActivity {

    private FloatingActionButton addAlarmClock;
    private ListView clocksListView;
    private AlarmListAdapter alarmListAdapter;

    private List<AlarmClockInfo> alarmClockInfoList;

    private RestartAlarmClockManageReceiver restartAlarmClockManageBC;

    private CancelClockReceiver cancelClockReceiver;

    public static final int ADD_ALARM_CLOCK = 1;
    public static final int CHANGE_ALARM_CLOCK = 2;

    public static final String ACTION_ADD_ALARM_CLOCK = "bingyan.net.alarmclock.mainactivity.add.alarm.clock";
    public static final String ACTION_CHANGE_ALARM_CLOCK = "bingyan.net.alarmclock.mainactivity.change.alarm.clock";

    public static final String ALARM_INFO = "alarm_info";
    public static final String POSITION = "position";

    // 添加闹钟按钮点击回调
    private View.OnClickListener addAlarmOnclickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (alarmClockInfoList.size() > 20) {
                Snackbar.make(addAlarmClock, String.format("闹钟数不能多于%d", 20), Snackbar.LENGTH_LONG).show();
                return;
            }
            Intent intent = new Intent(MainActivity.this, AlarmSettingsActivity.class);
            intent.setAction(ACTION_ADD_ALARM_CLOCK);
            startActivityForResult(intent, ADD_ALARM_CLOCK);
        }
    };

    // 闹钟点击回调
    private AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Intent intent = new Intent(MainActivity.this, AlarmSettingsActivity.class);
            intent.putExtra(ALARM_INFO, alarmClockInfoList.get(position));
            intent.putExtra(POSITION, position);
            intent.setAction(ACTION_CHANGE_ALARM_CLOCK);
            startActivityForResult(intent, CHANGE_ALARM_CLOCK);
        }
    };

    // 处理闹钟开启关闭
    private AlarmListAdapter.SwitchCheckListener switchCheckListener = new AlarmListAdapter.SwitchCheckListener() {
        @Override
        public void switchIsCheck(int position) {
            AlarmClockInfo info = alarmClockInfoList.get(position);
            try {
                if (info.isOpen) {
                    Snackbar.make(addAlarmClock, alarmClockInfoList.get(position).generateDueTime(), Snackbar.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            AlarmClockManageService.startActionChangeAlarm(MainActivity.this, alarmClockInfoList.get(position), position);
        }
    };

    private RestartAlarmClockManageReceiver.RestartAlarmClockManageCallBack restartAlarmClockManageCallBack = new RestartAlarmClockManageReceiver.RestartAlarmClockManageCallBack() {
        @Override
        public void restart() {
            // 开启闹钟管理服务
            AlarmClockManageService.startActionManageAlarm(MainActivity.this, alarmClockInfoList);
        }
    };

    // 处理取消闹钟的数据更变
    private CancelClockReceiver.CancelClockCallback cancelClockCallback = new CancelClockReceiver.CancelClockCallback() {
        @Override
        public void CancelClock(int position) {
            Log.d("cancelClock", String.valueOf(position));
            AlarmClockInfo alarmClockInfo = alarmClockInfoList.get(position);
            if (RepeatMode.ONCE.equals(alarmClockInfo.repeatMode)) {// 简单处理开启一次的情况
                alarmClockInfo.isOpen = false;
                alarmClockInfoList.set(position, alarmClockInfo);
            }
//            }else if (RepeatMode.EVERYDAY.equals(alarmClockInfo.repeatMode)) {// 每天都开启的情况
//                alarmListAdapter.notifyDataSetChanged();
//                AlarmClockManageService.startActionChangeAlarm(MainActivity.this, alarmClockInfo, position);
//            }else if (RepeatMode.EXCEPT_HOLIDAY.equals(alarmClockInfo.repeatMode)) {// 周一至周五
//                alarmListAdapter.notifyDataSetChanged();
//                AlarmClockManageService.startActionChangeAlarm(MainActivity.this, alarmClockInfo, position);
//            }
            alarmListAdapter.notifyDataSetChanged();
            AlarmClockManageService.startActionChangeAlarm(MainActivity.this, alarmClockInfo, position);
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            return;
        }
        AlarmClockInfo info;
        switch (requestCode) {
            case ADD_ALARM_CLOCK:
                info = (AlarmClockInfo) data.getSerializableExtra(ALARM_INFO);
                alarmClockInfoList.add(info);
                // 服务更改
                AlarmClockManageService.startActionAddAlarm(this, info);
                // 更新UI
                alarmListAdapter.notifyDataSetChanged();
                try {
                    Snackbar.make(addAlarmClock, info.generateDueTime(), Snackbar.LENGTH_LONG).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case CHANGE_ALARM_CLOCK:
                info = (AlarmClockInfo) data.getSerializableExtra(ALARM_INFO);
                int position = data.getIntExtra(POSITION, -1);
                if (position != -1) {
                    alarmClockInfoList.set(position, info);
                    // 服务更改
                    AlarmClockManageService.startActionChangeAlarm(this, info, position);
                    // 更新UI
                    alarmListAdapter.notifyDataSetChanged();
                }
                if (info.isOpen) {
                    try {
                        Snackbar.make(addAlarmClock, info.generateDueTime(), Snackbar.LENGTH_LONG).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        try {
            alarmClockInfoList = MyJsonHelper.readAlarmInfos(this);
        } catch (JSONException e) {
            e.printStackTrace();
            alarmClockInfoList = new ArrayList<>();
        }finally {
            // 开启闹钟管理服务
            AlarmClockManageService.startActionManageAlarm(this, alarmClockInfoList);
        }

        addAlarmClock= (FloatingActionButton) findViewById(R.id.activity_main_add_alarm_clocks);
        clocksListView = (ListView) findViewById(R.id.activity_main_list_clocks);
        alarmListAdapter = new AlarmListAdapter(this, R.layout.item_clocks, alarmClockInfoList);
        clocksListView.setAdapter(alarmListAdapter);

        clocksListView.setOnItemClickListener(itemClickListener);
        addAlarmClock.setOnClickListener(addAlarmOnclickListener);
        alarmListAdapter.setSwitchSetCheckListener(switchCheckListener);

        // 注册广播
        // 关闭闹钟
        cancelClockReceiver = new CancelClockReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(CancelClockReceiver.ACTION_CANCEL_CLOCK);
        registerReceiver(cancelClockReceiver, intentFilter);
        cancelClockReceiver.setActionCancelClockCallback(cancelClockCallback);

//        restartAlarmClockManageBC = new RestartAlarmClockManageReceiver();
//        IntentFilter intentFilter = new IntentFilter();
//        intentFilter.addAction(RestartAlarmClockManageReceiver.ACTION);
//        registerReceiver(restartAlarmClockManageBC, intentFilter);
//        restartAlarmClockManageBC.setRestartAlarmClockManage(restartAlarmClockManageCallBack);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AlarmClockInfo info;
        SharedPreferences sp = this.getSharedPreferences(MyJsonHelper.ALARM_CLOCK_SET, MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        for (int i = 0; i < alarmClockInfoList.size(); i++) {
            info = alarmClockInfoList.get(i);
            String saveValue = MyJsonHelper.writeAlarmInfoJson(info);
            editor.putString(String.valueOf(i), saveValue);
        }
        editor.apply();

        // 取消注册
        unregisterReceiver(cancelClockReceiver);
//        unregisterReceiver(restartAlarmClockManageBC);
    }
}
