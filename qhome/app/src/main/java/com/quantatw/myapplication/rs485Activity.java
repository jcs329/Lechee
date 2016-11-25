package com.quantatw.myapplication;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;

import com.quanta.hcbiapi.Adam4055;
import com.quanta.hcbiapi.Gpio;
import com.quanta.hcbiapi.Rs485P1;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lecheel on 4/20/16.
 */
public class rs485Activity extends Activity {
    Switch switch_light;
    Switch switch_curtain;
    Adam4055 adam4055;
    String arch;

    boolean b_LightOn=false;
    boolean b_Curtain=false;
    List<Adam4055.Gpo> gpoList = new ArrayList<Adam4055.Gpo>();
    List<Adam4055.Level> levelList = new ArrayList<Adam4055.Level>();
    private static final String TAG = "rs485Activity";
    private final int REP_BULB_OPENED = 1;
    private final int REP_CURTAIN_OPENED = 2;
    private LinearLayout devicesView;
    private  LinearLayout loadingView;
    private boolean mForeground;

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(!mForeground) {
//                Log.w("rs485Activity", "handleMessage - NOT in foreground");
            } else {
                if ((msg.arg1&REP_BULB_OPENED) == REP_BULB_OPENED) {
                    switch_light.setChecked(true);
                } else {
                    switch_light.setChecked(false);
                }

                if ((msg.arg1&REP_CURTAIN_OPENED) == REP_CURTAIN_OPENED) {
                    switch_curtain.setChecked(true);
                } else {
                    switch_curtain.setChecked(false);
                }
                loadingView.setVisibility(View.GONE);
                devicesView.setVisibility(View.VISIBLE);
            }
        }
    };

    public void onCreate(Bundle savedInstanceState) {
        setTheme(android.R.style.Theme_DeviceDefault_Light_DialogWhenLarge_NoActionBar);
        super.onCreate(savedInstanceState);

        // 設定 Layout 為 main2.xml
        setContentView( R.layout.rs485 );
        WindowManager.LayoutParams params = getWindow().getAttributes();
//        params.x = -20;
        params.height = 800;
        params.width = 600;
//        params.y = -10

        this.getWindow().setAttributes(params);

        switch_light = (Switch) findViewById(R.id.switch_bulb);
        switch_light.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // The toggle is enabled
                    Rs485P1.getInstance().setGpo(Adam4055.Gpo.GPO0, Adam4055.Level.HIGH);
                } else {
                    // The toggle is disabled
                    Rs485P1.getInstance().setGpo(Adam4055.Gpo.GPO0, Adam4055.Level.LOW);
                }
            }
        });

        switch_curtain = (Switch) findViewById(R.id.switch_curtain);
        switch_curtain.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // The toggle is enabled
                    gpoList.add(Adam4055.Gpo.GPO1);
                    levelList.add(Adam4055.Level.HIGH);
                    gpoList.add(Adam4055.Gpo.GPO2);
                    levelList.add(Adam4055.Level.LOW);
                    gpoList.add(Adam4055.Gpo.GPO3);
                    levelList.add(Adam4055.Level.LOW);

                    Rs485P1.getInstance().setGpos(gpoList, levelList);
                    gpoList.clear();
                    levelList.clear();

                } else {
                    // The toggle is disabled
                    gpoList.add(Adam4055.Gpo.GPO1);
                    levelList.add(Adam4055.Level.LOW);
                    gpoList.add(Adam4055.Gpo.GPO2);
                    levelList.add(Adam4055.Level.LOW);
                    gpoList.add(Adam4055.Gpo.GPO3);
                    levelList.add(Adam4055.Level.HIGH);

                    Rs485P1.getInstance().setGpos(gpoList, levelList);
                    gpoList.clear();
                    levelList.clear();
                }
            }
        });

        devicesView = (LinearLayout)findViewById(R.id.devicesView);
        loadingView = (LinearLayout)findViewById(R.id.loadingView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mForeground = true;
        Rs485P1.getInstance().setUIHandler(mHandler);
        loadingView.setVisibility(View.VISIBLE);
        devicesView.setVisibility(View.GONE);
        if(android.os.Build.DEVICE.equals("byt_t_crv2")) {
            try {
                Rs485P1.getInstance().write("$016\r".getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mForeground = false;
        Rs485P1.getInstance().setUIHandler(null);
    }
}
