package com.quantatw.roomhub.ui;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.quantatw.myapplication.R;
import com.quantatw.roomhub.utils.GlobalDef;
import com.quantatw.roomhub.utils.Utils;
import com.quantatw.sls.key.ErrorKey;

/**
 * Created by erin on 1/5/16.
 */
public class ForgetPassActivity extends AbstractRoomHubActivity {
    private Context mContext;
    private EditText emailEdit;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            dismissProgressDialog();
            int result = (int) msg.obj;
            if (result == ErrorKey.Success) {
                Toast.makeText(mContext, getString(R.string.forget_pass_success_str), Toast.LENGTH_SHORT).show();
                finish();
            }
            else {
                Toast.makeText(mContext, Utils.getErrorCodeString(mContext, result), Toast.LENGTH_SHORT).show();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);
        mContext = this;

        emailEdit = (EditText)findViewById(R.id.loginAccount);
        Button button = (Button)findViewById(R.id.btnSend);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = Utils.trimSpace(emailEdit.getText().toString());
                if(TextUtils.isEmpty(email)) {
                    Toast.makeText(mContext,R.string.email_prompt,Toast.LENGTH_SHORT).show();
                    emailEdit.requestFocus();
                    return;
                }

                // check email format
                if(Utils.CheckEmailFormat(email) == false) {
                    Toast.makeText(mContext, R.string.wrong_account_format_str, Toast.LENGTH_SHORT).show();
                    emailEdit.requestFocus();
                    return;
                }

                showProgressDialog("", getString(R.string.process_str));
                new Thread() {
                    @Override
                    public void run() {
                        int ret = getAccountManager().forgetPass(email);
                        mHandler.sendMessage(mHandler.obtainMessage(0,ret));
                    }
                }.start();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
