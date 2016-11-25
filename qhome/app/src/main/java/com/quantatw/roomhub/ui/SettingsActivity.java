package com.quantatw.roomhub.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.quantatw.myapplication.BuildConfig;
import com.quantatw.myapplication.R;
import com.quantatw.roomhub.utils.GlobalDef;
import com.quantatw.roomhub.utils.PreferenceEditor;

/**
 * Created by erin on 3/7/16.
 */
public class SettingsActivity extends AbstractRoomHubActivity implements View.OnClickListener {

    TextView appVerTextView;
    ImageView tempUnitImageView,notifyImageView;

    private PreferenceEditor mPref;
    int mTempUnit;
    boolean mNotify;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);

        mPref = new PreferenceEditor(this, GlobalDef.ROOMHUB_SETTINGS_PREFERENCE_NAME);
        initLayout();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshUI();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.pref_temp_unit) {
            int setValue = mTempUnit==0?1:0;
            mPref.setIntValue(GlobalDef.ROOMHUB_SETTINGS_TEMP_UNIT, setValue);
            notifyTempUnitChanged(setValue);
            refreshUI();
        }
        else if(v.getId() == R.id.pref_notification) {
            boolean setValue = mNotify==true?false:true;
            mPref.setStringValue(GlobalDef.ROOMHUB_SETTINGS_NOTIFICATION, Boolean.toString(setValue));
            notifyValueChanged(GlobalDef.ACTION_SETTINGS_NOTIFICATION_CHANGED, setValue);
            refreshUI();
        }
    }

    private void notifyTempUnitChanged(int val) {
        Intent intent = new Intent(GlobalDef.ACTION_SETTINGS_TEMP_UNIT_CHANGED);
        intent.putExtra(GlobalDef.KEY_SETTINGS_VALUE,val);
        sendBroadcast(intent);
    }

    private void notifyValueChanged(String action, boolean value) {
        Intent intent = new Intent(action);
        intent.putExtra(GlobalDef.KEY_SETTINGS_VALUE,value);
        sendBroadcast(intent);
    }

    private void refreshUI() {
        int val = mPref.getIntValue(GlobalDef.ROOMHUB_SETTINGS_TEMP_UNIT);
        mTempUnit = val;
        // 0:Celsius, 1: fahrenheit
        tempUnitImageView.setBackgroundResource(mTempUnit==0?R.drawable.btn_celsius:R.drawable.btn_fahrenheit);

        String notification = mPref.getStringValue(GlobalDef.ROOMHUB_SETTINGS_NOTIFICATION);
        if(TextUtils.isEmpty(notification)) {
            mNotify = true;
            notifyImageView.setBackgroundResource(R.drawable.switch_on);
        }
        else {
            mNotify = Boolean.valueOf(notification);
            notifyImageView.setBackgroundResource(mNotify==true?R.drawable.switch_on:R.drawable.switch_off);
        }

        appVerTextView.setText(BuildConfig.VERSION_NAME);
    }

    private void initLayout() {
        tempUnitImageView = (ImageView)findViewById(R.id.pref_temp_unit);
        tempUnitImageView.setOnClickListener(this);
        appVerTextView = (TextView)findViewById(R.id.pref_app_version);
        notifyImageView = (ImageView)findViewById(R.id.pref_notification);
        notifyImageView.setOnClickListener(this);
    }
}
