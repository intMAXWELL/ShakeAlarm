package bingyan.net.alarmclock.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TimePicker;

import java.io.File;
import java.util.Objects;

import bingyan.net.alarmclock.AlarmClockInfo;
import bingyan.net.alarmclock.R;
import bingyan.net.alarmclock.RepeatMode;
import bingyan.net.alarmclock.view.SettingItemView;

public class AlarmSettingsActivity extends AppCompatActivity {

    private SettingItemView repeatMode,ringTone,vibrate,remark;
    private TimePicker timePicker;
    private PopupWindow repeatModePopupWindow,remarkPopupWindow;
    private ListView repeatModePopupList;
    private View repeatModePopupView;

    private AlarmClockInfo thisClock, receivedClock;

    private static final String[] repeatModeStrs = {"只响一次", "每天", "周一至周五", "自定义"};
    private static final RepeatMode[] repeatModeEnums = {RepeatMode.ONCE, RepeatMode.EVERYDAY, RepeatMode.EXCEPT_HOLIDAY, RepeatMode.CUSTOM};

    private static final int GET_RINGTONE = 1;

    // 闹铃Uri
    private Uri defRingtone;

    // 更改闹钟传入的位置参数
    private int position = -1;

    // 添加还是修改
    private String action;

    // 中间变量，记录闹钟时间
    private int hour,minute;

    // 根据RepeatMode枚举获取对应的中文描述
    public static String getRepeatModeStr(RepeatMode repeatMode) {
        for (int i = 0; i < repeatModeEnums.length; i++) {
            if(repeatMode == repeatModeEnums[i])
                return repeatModeStrs[i];
        }
        return "";
    }

    //
    public static RepeatMode getRepeatModeEnum(String mode) {
        for (int i = 0; i < repeatModeStrs.length; i++) {
            if(Objects.equals(mode, repeatModeStrs[i]))
                return repeatModeEnums[i];
        }
        return RepeatMode.ONCE;
    }

    // toolbar点击返回箭头回调
    private View.OnClickListener toolbarClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            thisClock.isVibrate = vibrate.getSwitch().isChecked();// 不通过监听控制,取最后结果
            thisClock.time = AlarmClockInfo.generateFormatTimeStr(hour, minute);
            if (Objects.equals(action, MainActivity.ACTION_CHANGE_ALARM_CLOCK)) {
                if (thisClock.equals(receivedClock)) {
                    finish();
                    return;
                }
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(AlarmSettingsActivity.this);
            builder.setTitle("舍弃更改")
                    .setMessage("要舍弃对该闹钟的更改吗")
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .setPositiveButton("舍弃更改", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            AlarmSettingsActivity.this.finish();
                        }
                    });
            builder.create().show();
        }
    };

    // PopupWindow消失时调用
    private PopupWindow.OnDismissListener dismissListener = new PopupWindow.OnDismissListener() {
        @Override
        public void onDismiss() {
            final Window window = getWindow();
            WindowManager.LayoutParams params= window.getAttributes();
            params.alpha=1f;
            window.setAttributes(params);
        }
    };

    // 重复模式点击回调
    private View.OnClickListener repeatModeClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            if(repeatModePopupWindow ==null){
                initRepeatModePopupWindow();
            }
            WindowManager.LayoutParams params= getWindow().getAttributes();
            params.alpha=0.5f;
            getWindow().setAttributes(params);

            repeatModePopupView.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in));
            repeatModePopupView.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.push_bottom_in));

            repeatModePopupWindow.setContentView(repeatModePopupView);
            repeatModePopupWindow.showAtLocation(timePicker, Gravity.BOTTOM, 0, 0);

            repeatModePopupWindow.update();
        }
    };

    // 铃声设置点击回调，详情见setRingtone()
    private View.OnClickListener ringtoneOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            setRingtone();
        }
    };

    // 重复模式底部弹窗选项点击回调
    private AdapterView.OnItemClickListener repeatModeOnItemClick = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            // 设置重复模式
            thisClock.repeatMode = repeatModeEnums[position];
            repeatMode.setSubtitle(repeatModeStrs[position]);
            repeatModePopupWindow.dismiss();
        }
    };

    // TimePicker时间改变回调
    private TimePicker.OnTimeChangedListener timeChangedListener = new TimePicker.OnTimeChangedListener() {
        @Override
        public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
//            Log.d("onTimeChanged", String.format("hour:%d,minute:%d", hourOfDay, minute));
            hour = hourOfDay;
            AlarmSettingsActivity.this.minute = minute;
        }
    };

    // 初始化底部弹窗
    private void initRepeatModePopupWindow(){
        repeatModePopupWindow = new PopupWindow(AlarmSettingsActivity.this);
        repeatModePopupWindow.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        repeatModePopupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        repeatModePopupWindow.setFocusable(true);
        repeatModePopupWindow.setOutsideTouchable(true);
        repeatModePopupWindow.setBackgroundDrawable(null);
    }

    // 设置闹玲铃声
    private void setRingtone(){
        Intent intent = new Intent();
        intent.setAction(RingtoneManager.ACTION_RINGTONE_PICKER);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, false);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "设置闹玲铃声");
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALL);
        Uri pickedUri = RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_ALARM);
        if (pickedUri!=null) {
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI,pickedUri);
            defRingtone = pickedUri;
        }
        startActivityForResult(intent, GET_RINGTONE);
    }

    // 根据铃声Uri获取铃声名
    private String getRingtoneName(Uri uri) {
        File file = new File(uri.getPath());
        String ringToneName = file.getName();
        Cursor cursor = this.getContentResolver().query
                (uri, new String[]{MediaStore.Audio.Media.TITLE}, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                ringToneName= cursor.getString(0);

            }
            cursor.close();
        }
        return ringToneName;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode!=RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case GET_RINGTONE:
                defRingtone = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                thisClock.ringSongPath = defRingtone.getPath();
                String ringToneName = getRingtoneName(defRingtone);
                if (null != ringToneName) {
                    ringTone.setSubtitle(ringToneName);
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        {
            repeatMode = (SettingItemView) findViewById(R.id.activity_setting_repeat_mode);
            ringTone = (SettingItemView) findViewById(R.id.activity_setting_ringtone);
            vibrate = (SettingItemView) findViewById(R.id.activity_setting_vibrate);
            remark = (SettingItemView) findViewById(R.id.activity_setting_remark);
            timePicker = (TimePicker) findViewById(R.id.activity_setting_time_picker);

            repeatModePopupView = View.inflate(this, R.layout.setting_repeat_mode_popup, null);
            repeatModePopupView.setBackgroundColor(Color.WHITE);
            repeatModePopupList = (ListView) repeatModePopupView.findViewById(R.id.setting_repeat_mode_popup_list);
            repeatModePopupList.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, repeatModeStrs));

            if (null == repeatModePopupWindow) {
                initRepeatModePopupWindow();
            }

            if (null != toolbar) {
                toolbar.setNavigationIcon(R.drawable.ic_arrow_back_blue_500_24dp);
                toolbar.setNavigationOnClickListener(toolbarClick);
            }

        }
        {
            repeatMode.setOnClickListener(repeatModeClick);
            repeatModePopupWindow.setOnDismissListener(dismissListener);
            ringTone.setOnClickListener(ringtoneOnClick);
            repeatModePopupList.setOnItemClickListener(repeatModeOnItemClick);
            timePicker.setOnTimeChangedListener(timeChangedListener);
        }

        Intent intent = getIntent();
        action = intent.getAction();
        // 处理更改闹钟逻辑
        if (Objects.equals(action, MainActivity.ACTION_CHANGE_ALARM_CLOCK)) {
            position = intent.getIntExtra(MainActivity.POSITION, -1);
            receivedClock = (AlarmClockInfo) intent.getSerializableExtra(MainActivity.ALARM_INFO);
            thisClock = new AlarmClockInfo(receivedClock);
            // 初始化控件显示内容
            String repeatModeStr = getRepeatModeStr(thisClock.repeatMode);
            repeatMode.setSubtitle(repeatModeStr);
            ringTone.setSubtitle(getRingtoneName(Uri.parse(thisClock.ringSongPath)));
            vibrate.getSwitch().setChecked(thisClock.isVibrate);
            remark.setSubtitle(thisClock.remark);
            try {
                int hour = thisClock.getHour();
                int minute = thisClock.getMinute();

                if (Build.VERSION.SDK_INT >= 23) {
                    timePicker.setHour(hour);
                    timePicker.setMinute(minute);
                }
                else {
                    timePicker.setCurrentHour(hour);
                    timePicker.setCurrentMinute(minute);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (Objects.equals(action, MainActivity.ACTION_ADD_ALARM_CLOCK)) {//  处理添加闹钟逻辑
            thisClock = new AlarmClockInfo("", "", true);
            Uri pickedUri = RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_ALARM);
            thisClock.ringSongPath = pickedUri.toString();
            if (Build.VERSION.SDK_INT >= 23) {
                hour = timePicker.getHour();
                minute = timePicker.getMinute();
            }
            else {
                hour = timePicker.getCurrentHour();
                minute = timePicker.getCurrentMinute();
            }
            thisClock.time = AlarmClockInfo.generateFormatTimeStr(hour, minute);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_setting, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        // 点击确定处理逻辑
        if (id == R.id.menu_setting_action_confirm) {
            thisClock.isVibrate = vibrate.getSwitch().isChecked();
            thisClock.time = AlarmClockInfo.generateFormatTimeStr(hour, minute);

            Intent intent = new Intent();
            if (Objects.equals(action, MainActivity.ACTION_ADD_ALARM_CLOCK)) {
                thisClock.isOpen = true;// 新建闹钟默认打开
            }
            else if (Objects.equals(action, MainActivity.ACTION_CHANGE_ALARM_CLOCK)) {
                if (!thisClock.equals(receivedClock)) {// 如果更改了闹钟,存储
                    thisClock.isOpen = true;// 更改闹钟后自动启动
                }
            }
            intent.putExtra(MainActivity.ALARM_INFO, thisClock);
            intent.putExtra(MainActivity.POSITION, position);
            this.setResult(RESULT_OK, intent);
            finish();
        }

        return super.onOptionsItemSelected(item);
    }
}
