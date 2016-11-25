package com.quantatw.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;

public class SettingActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	setTheme(android.R.style.Theme_DeviceDefault_Light_DialogWhenLarge_NoActionBar);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.height = 800;
        params.width = 1000;

        this.getWindow().setAttributes(params);

        RelativeLayout settingView = (RelativeLayout) findViewById(R.id.SystemView);
        settingView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                //actions for setting
                Intent intent = new Intent();
                intent.setClassName("com.android.settings", "com.android.settings.Settings");
                startActivity(intent);
            }
        });

        RelativeLayout sipSettingView = (RelativeLayout) findViewById(R.id.LiveHDView);
        sipSettingView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                //actions for setting
                Intent intent = new Intent();
                if(MainActivity.isInstanciated() && !MainActivity.instance().getSipStatus())
                    intent.setClassName("com.quantatw.myapplication", "com.quantatw.myapplication.SipLoginActivity");
                else
                    intent.setClassName("com.quantatw.myapplication", "com.quantatw.myapplication.SipLogoutActivity");
                startActivity(intent);
            }
        });

        RelativeLayout miscSettingView = (RelativeLayout) findViewById(R.id.MiscView);
        miscSettingView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                //actions for setting
                Intent intent = new Intent();
                intent.setClassName("com.quantatw.myapplication", "com.quantatw.myapplication.MiscActivity");
                startActivity(intent);
            }
        });

        RelativeLayout ipcamSettingView = (RelativeLayout) findViewById(R.id.IpcamView);
        ipcamSettingView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                //actions for setting
                Intent intent = new Intent();
                intent.setClassName("com.quantatw.myapplication", "com.quantatw.myapplication.IpcamActivity");
                startActivity(intent);
            }
        });



    }
}
