package com.quantatw.roomhub.ui;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;
import com.quantatw.myapplication.R;

public class LicenseActivity extends AbstractRoomHubActivity implements View.OnClickListener {
    private static final String TAG = "LicenseActivity";
    private static boolean DEBUG=true;
    private Button mBtnAgree;
    private Button mBtnDisagree;
    private WebView mWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.license_activity);

        getWindow().setBackgroundDrawableResource(R.drawable.bg_personal_info);
    }

    @Override
    protected void onResume() {
        super.onResume();

        mBtnAgree=(Button)findViewById(R.id.btn_agree);
        mBtnAgree.setOnClickListener(this);

        mBtnDisagree=(Button)findViewById(R.id.btn_disagree);
        mBtnDisagree.setOnClickListener(this);

        mWebView=(WebView)findViewById(R.id.webview);
        mWebView.loadUrl("file:///android_asset/license.htm");
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_agree:
                Intent intent = new Intent(this,MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addCategory(Intent.CATEGORY_HOME);
                startActivity(intent);
                finish();
                break;
            case R.id.btn_disagree:
                ChooseDisagreeDialog();
                break;
        }

    }

    private void ChooseDisagreeDialog(){
        final Dialog dialog = new Dialog(this,R.style.CustomDialog);

        dialog.setContentView(R.layout.custom_dialog);

        TextView txt_msg=(TextView)dialog.findViewById(R.id.txt_message);
        txt_msg.setText(getString(R.string.license_confirm_message));

        Button btn_yes = (Button)dialog.findViewById(R.id.btn_yes);
        btn_yes.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();
                finish();

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
