package bingyan.net.alarmclock.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import bingyan.net.alarmclock.R;

/**
 * 
 */
public class SettingItemView extends FrameLayout {

    public String title = "标题",subtitle = "子标题";
    private boolean isSwitch;

    private TextView titleTextView,subtitleTextView;
    private ImageView arrow;
    private Switch open;

    public SettingItemView(Context context) {
        super(context);
        init(null, 0);
    }

    public SettingItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public SettingItemView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.SettingItemView, defStyle, 0);

        title = a.getString(R.styleable.SettingItemView_setting_title);
        subtitle = a.getString(R.styleable.SettingItemView_setting_subtitle);
        isSwitch = a.getBoolean(R.styleable.SettingItemView_setting_is_switch, false);
        LayoutInflater.from(getContext()).inflate(R.layout.setting_item_view,this );
        a.recycle();

        titleTextView = (TextView) findViewById(R.id.setting_item_view_title);
        subtitleTextView = (TextView) findViewById(R.id.setting_item_view_subtitle);
        open = (Switch) findViewById(R.id.setting_item_view_switch);
        arrow = (ImageView) findViewById(R.id.setting_item_view_arrow);

        if (isSwitch) {
            open.setVisibility(VISIBLE);
            arrow.setVisibility(GONE);
        }
        else {
            open.setVisibility(GONE);
            arrow.setVisibility(VISIBLE);
        }

        titleTextView.setText(title);
        subtitleTextView.setText(subtitle);
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
        subtitleTextView.setText(subtitle);
    }

    public Switch getSwitch() {
        return open;
    }

}
