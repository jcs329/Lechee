package com.quantatw.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class SipLogoutActivity extends Activity {

    final String LIVEHD_SERVICE = "com.quanta.livehd_v2.LivehdService";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sip_logout);

        Button SignOutButton = (Button) findViewById(R.id.btnSignout);
        SignOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logoutLiveHD();
                MainActivity.instance().resetSipStatus();
                Intent intent = new Intent(SipLogoutActivity.this, SipLoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        TextView name = (TextView) findViewById(R.id.myName);
        TextView status = (TextView) findViewById(R.id.myStatus);
        name.setText(MainActivity.instance().getUserId());
        status.setText(MainActivity.instance().getSipStatus() ? "online" : "offline");
        if(MainActivity.instance().getSipStatus())
            status.setTextColor(Color.argb(255,108,191,17));
        else
            status.setTextColor(Color.GRAY);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if(keyCode == KeyEvent.KEYCODE_BACK)
        {
            Intent intent = new Intent(SipLogoutActivity.this, SettingActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
        return false;
    }

    public void logoutLiveHD()
    {
        if(true)
        {
            Intent intent = new Intent(LIVEHD_SERVICE);

            // Please put Action parameter in the intent
            Bundle bundle = new Bundle();
            bundle.putInt("Action", TLAction.ACTION_LOGOUT.getActionType());

            intent.putExtras(bundle);
            startService(intent);
            //handleTLAction(TLAction.ACTION_LOGOUT, "I", null);
        }
        //else
        //    Toast.makeText(this, "LiveHD already running at background.", Toast.LENGTH_SHORT).show();
    }
}
