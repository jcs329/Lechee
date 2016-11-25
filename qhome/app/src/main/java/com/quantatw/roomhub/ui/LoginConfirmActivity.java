package com.quantatw.roomhub.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.quantatw.myapplication.R;
import com.quantatw.roomhub.utils.Utils;
import com.quantatw.sls.key.ErrorKey;

/**
 * Created by erin on 12/15/15.
 */
public class LoginConfirmActivity extends AbstractRoomHubActivity implements View.OnClickListener {
    public static final String KEY_USER_ACCOUNT = "KEY_USER_ACCOUNT";
    public static final String KEY_USER_NAME = "KEY_USER_USERNAME";
    public static final String KEY_USER_PASS = "KEY_USER_PASS";

    private Context mContext;
    private String userName;
    private String userAccount;
    private String userPass;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            dismissProgressDialog();
            int errCode = (int)msg.obj;
            String message = getString(R.string.confirm_email_has_sent);;
            if(errCode != ErrorKey.Success)
                message = Utils.getErrorCodeString(mContext, errCode);

            Toast.makeText(mContext,message,Toast.LENGTH_SHORT).show();
            Intent intent = new Intent();
            intent.putExtra(KEY_USER_NAME, userName);
            intent.putExtra(KEY_USER_ACCOUNT, userAccount);
            intent.putExtra(KEY_USER_PASS, userPass);
            setResult(RESULT_OK, intent);
            finish();
            super.handleMessage(msg);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_confirm_mail);

        mContext = this;
        if(getIntent() != null) {
            userName = getIntent().getExtras().getString(LoginConfirmActivity.KEY_USER_NAME);
            userAccount = getIntent().getExtras().getString(LoginConfirmActivity.KEY_USER_ACCOUNT);
            userPass = getIntent().getExtras().getString(LoginConfirmActivity.KEY_USER_PASS);
        }

        Button send = (Button)findViewById(R.id.btn_send);
        send.setOnClickListener(this);
        Button login = (Button)findViewById(R.id.btn_login);
        login.setOnClickListener(this);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra(KEY_USER_NAME, userName);
        intent.putExtra(KEY_USER_ACCOUNT, userAccount);
        intent.putExtra(KEY_USER_PASS, userPass);
        setResult(RESULT_OK, intent);
        super.onBackPressed();
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.btn_send) {
            showProgressDialog("", getString(R.string.process_str));
            new Thread() {
                @Override
                public void run() {
                    int ret = getAccountManager().sendAuthorizeEmail(userAccount);
                    mHandler.sendMessage(mHandler.obtainMessage(0, ret));
                }
            }.start();
        }
        else {
            Intent intent = new Intent();
            intent.putExtra(KEY_USER_NAME, userName);
            intent.putExtra(KEY_USER_ACCOUNT, userAccount);
            intent.putExtra(KEY_USER_PASS, userPass);
            setResult(RESULT_OK, intent);
            finish();
        }
    }
}
