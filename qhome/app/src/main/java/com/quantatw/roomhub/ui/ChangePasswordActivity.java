package com.quantatw.roomhub.ui;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import com.quantatw.myapplication.R;
import com.quantatw.roomhub.manager.AccountManager;
import com.quantatw.roomhub.utils.Utils;
import com.quantatw.sls.key.ErrorKey;

/**
 * Created by erin on 1/4/16.
 */
public class ChangePasswordActivity extends AbstractRoomHubActivity {
    private Context mContext;
    private EditText mPassEdit, mConfirmPassEdit;
    private Button mConfirmButton;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            dismissProgressDialog();
            int result = (int)msg.obj;
            if(result == ErrorKey.Success)
                Toast.makeText(mContext, getString(R.string.change_pass_result),Toast.LENGTH_SHORT).show();
            else {
                Toast.makeText(mContext, Utils.getErrorCodeString(mContext, result), Toast.LENGTH_SHORT).show();
            }
            finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        mContext = this;

        mPassEdit = (EditText)findViewById(R.id.newPassword);
        mConfirmPassEdit = (EditText)findViewById(R.id.confirmPassword);
        mConfirmButton = (Button)findViewById(R.id.btnConfirm);

        CheckBox chkShowPass = (CheckBox) findViewById(R.id.chkShowPass);
        chkShowPass.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked == true) {
                    mPassEdit.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
                    mConfirmPassEdit.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
                } else {
                    mPassEdit.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
                    mConfirmPassEdit.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
                }
                mPassEdit.setSelection(mPassEdit.getText().length());
                mConfirmPassEdit.setSelection(mConfirmPassEdit.getText().length());
            }
        });

        mConfirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!Utils.CheckPasswordAvailable(mPassEdit.getText().toString())) {
                    Toast.makeText(mContext, getString(R.string.password_len_wrong), Toast.LENGTH_SHORT).show();
                    mPassEdit.requestFocus();
                    return;
                }

                if(Utils.CheckConfirmPwd(mPassEdit.getText().toString(), mConfirmPassEdit.getText().toString()) == false) {
                    Toast.makeText(mContext, getString(R.string.wrong_password_confirm_str), Toast.LENGTH_SHORT).show();
                    mConfirmPassEdit.requestFocus();
                    return;
                }

                showProgressDialog("",getString(R.string.process_str));
                new Thread() {
                    @Override
                    public void run() {
                        AccountManager accountManager = getAccountManager();
                        String oldPass =  accountManager.getCurrentAccountPass();
                        int ret = getAccountManager().changePassword(oldPass, mPassEdit.getText().toString());
                        mHandler.sendMessage(mHandler.obtainMessage(0,ret));
                    }
                }.start();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
