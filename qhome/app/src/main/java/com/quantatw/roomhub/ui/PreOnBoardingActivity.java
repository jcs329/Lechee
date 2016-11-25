package com.quantatw.roomhub.ui;

import android.app.ActionBar;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.quantatw.myapplication.R;
import com.quantatw.roomhub.utils.Utils;

/**
 * Created by erin on 2/22/16.
 */
public class PreOnBoardingActivity extends AbstractRoomHubActivity {

    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding_start_desc);

        mContext = this;
        Button btnStartAdd = (Button)findViewById(R.id.btnStartAdd);
        btnStartAdd.setOnClickListener(onClickListener);

        LayoutInflater inflator = (LayoutInflater) this .getSystemService(LAYOUT_INFLATER_SERVICE);
        View v = inflator.inflate(R.layout.preonboarding_desc_header, null);

        ActionBar actionBar=getActionBar();
        actionBar.setCustomView(v);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);

        TextView skip = (TextView)v.findViewById(R.id.skip);
        skip.setOnClickListener(onClickListener);
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
                if (!checkMobileSettings())
                    loadSetupWifiPage();
        }
    };

    private void loadSetupWifiPage() {
        Intent intent = new Intent(this, SetupWifiActivity.class);
        intent.setAction("android.intent.action.CHANGE_WIFI");
        startActivity(intent);
    }

    private boolean checkMobileSettings() {
        // for Android 6.0 Marshmallow
        if(Build.VERSION.SDK_INT >= 23) {
            if(Utils.isMobileDataEnabled(this)) {
                promptTurnOffMobileDialog();
                return true;
            }
        }
        return false;
    }

    private void promptTurnOffMobileDialog() {
        final Dialog dialog = new Dialog(this,R.style.CustomDialog);

        dialog.setContentView(R.layout.custom_dialog);

        TextView txt_msg=(TextView)dialog.findViewById(R.id.txt_message);
        txt_msg.setText(getString(R.string.onboarding_prompt_turn_off_mobile));

        Button btn_yes = (Button)dialog.findViewById(R.id.btn_yes);
        btn_yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.setPromptDisableMobileData(mContext, true);
                Intent intent = new Intent();
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setComponent(
                        new ComponentName("com.android.settings", "com.android.settings.Settings$DataUsageSummaryActivity"));
                startActivity(intent);
                dialog.dismiss();
            }
        });

        Button btn_no = (Button)dialog.findViewById(R.id.btn_no);
        btn_no.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

}
