package bingyan.net.alarmclock;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Demon on 2016/3/16.
 * AlarmClocksList适配器
 */
public class AlarmListAdapter extends ArrayAdapter<AlarmClockInfo> {

    private int resId;
    private List<AlarmClockInfo> alarmClockInfoList;
    private SwitchCheckListener switchCheckListener;

    public AlarmListAdapter(Context context, int resource, List<AlarmClockInfo> objects) {
        super(context, resource, objects);
        this.resId = resource;
        this.alarmClockInfoList = objects;
    }

    public interface SwitchCheckListener{
        void switchIsCheck(int position);
    }

    public void setSwitchSetCheckListener(SwitchCheckListener listener) {
        this.switchCheckListener = listener;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final AlarmClockInfo info = getItem(position);
        final ViewHolder viewHolder;
        View view;
        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(resId, null);
            viewHolder = new ViewHolder();
            viewHolder.time = (TextView) view.findViewById(R.id.item_clocks_time);
            viewHolder.remark = (TextView) view.findViewById(R.id.item_clocks_remark);
            viewHolder.isOpen = (Switch) view.findViewById(R.id.item_clocks_switch);
            view.setTag(viewHolder);
        } else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }
        viewHolder.time.setText(info.time);
        if (!info.isOpen) {
            viewHolder.remark.setText(String.format("未开启|%s", info.remark));
        }
        else {
            try {
                viewHolder.remark.setText(String.format("%s|%s",info.generateDueTime(), info.remark));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

//        viewHolder.isOpen.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                alarmClockInfoList.get(position).isOpen = isChecked;
//                switchCheckListener.switchIsCheck(position);
//                if (isChecked) {
//                    try {
//                        viewHolder.remark.setText(String.format("%s|%s",info.generateDueTime(), info.remark));
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }else viewHolder.remark.setText(String.format("未开启|%s", info.remark));
//            }
//        });

        viewHolder.isOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Switch s = (Switch) v;
                boolean isChecked = s.isChecked();
                if (isChecked) {
                    try {
                        viewHolder.remark.setText(String.format("%s|%s",info.generateDueTime(), info.remark));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }else viewHolder.remark.setText(String.format("未开启|%s", info.remark));

                alarmClockInfoList.get(position).isOpen = isChecked;
                switchCheckListener.switchIsCheck(position);
            }
        });

        viewHolder.isOpen.setChecked(info.isOpen);

        return view;
    }

    private class ViewHolder {
        TextView time,remark;
        Switch isOpen;
    }
}
