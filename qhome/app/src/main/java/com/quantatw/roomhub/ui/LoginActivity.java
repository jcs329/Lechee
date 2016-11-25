package com.quantatw.roomhub.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.quantatw.myapplication.R;
import com.quantatw.roomhub.manager.AccountManager;
import com.quantatw.roomhub.utils.GlobalDef;
import com.quantatw.roomhub.utils.Utils;
import com.quantatw.sls.key.ErrorKey;

/**
 * Created by 95010915 on 2015/9/25.
 */
public class LoginActivity extends AbstractRoomHubActivity {
    ImageEditTextWidget txtAccount, txtPassword;

    private AccountManager mAccountManager;

    private Class<?> mNextActivity;
    private HandlerThread mBackgroundThread;
    private BackgroundHandler mBackgroundHandler;
    private Context mContext;

    private final static int PROCESS_FORGET_PWD = 190016;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        mContext=this;

        Bundle data = getIntent().getExtras();
        mNextActivity = (Class) data.getSerializable("Activity");

        //txtAccount = (EditText) findViewById(R.id.loginAccount);
        //txtPassword = (EditText) findViewById(R.id.loginPwd);
        txtAccount = (ImageEditTextWidget) findViewById(R.id.loginAccount);
        txtAccount.setEditImage(R.drawable.icon_account);
        txtAccount.setEditHint(R.string.user_account_desc);
        txtAccount.setEditInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);

        txtPassword = (ImageEditTextWidget) findViewById(R.id.loginPwd);
        txtPassword.setEditImage(R.drawable.icon_password);
        txtPassword.setEditHint(R.string.password_desc);
        txtPassword.setEditInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
        txtPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());

        mAccountManager = getAccountManager();

        Button btnLogin = (Button) findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doLogin();
            }
        });

        TextView txtForgetPass = (TextView) findViewById(R.id.txtForgetPassword);
        txtForgetPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (txtAccount.getText().toString().length() == 0 ||
                        (!txtAccount.getText().toString().contains("@") || !txtAccount.getText().toString().contains("."))) {
                    Toast.makeText(mContext, getString(R.string.email_prompt), Toast.LENGTH_SHORT).show();
                    txtAccount.requestFocus();
                    return;
                }

                showProgressDialog("", getString(R.string.process_str));

                Message msg = new Message();
                msg.what = PROCESS_FORGET_PWD;
                Bundle data = new Bundle();
                data.putString("account", txtAccount.getText().toString());
                msg.setData(data);
                mBackgroundHandler.sendMessage(msg);
            }
        });

        mBackgroundThread=new HandlerThread("MainActivity");
        mBackgroundThread.start();
        mBackgroundHandler = new BackgroundHandler(mBackgroundThread.getLooper());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBackgroundThread != null ) {
            mBackgroundThread.quit();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        }
    }

    private LinearLayout.LayoutParams setMargin(int left, int right, int top, int bottom, LinearLayout.LayoutParams parms) {
        LinearLayout.LayoutParams marginParms = parms;
        marginParms.setMargins(left,top, right, bottom);

        return marginParms;
    }

    private void doLogin() {
        showProgressDialog("", getString(R.string.process_str));

        if(txtAccount.getText().toString().length() == 0) {
            Toast.makeText(mContext, getString(R.string.account_empty_error_msg), Toast.LENGTH_SHORT).show();
            txtAccount.requestFocus();
            dismissProgressDialog(1500, DIALOG_SHOW_NOTHING, 0);
            return;
        }

        if (txtPassword.getText().toString().isEmpty() || txtPassword.getText().toString().length() == 0) {
            txtPassword.requestFocus();
            dismissProgressDialog(1500, DIALOG_SHOW_NOTHING, 0);
            return;
        }

        /*int ret = mAccountManager.Login(txtAccount.getText().toString(), txtPassword.getText().toString(), true);
        if(ret == GlobalDef.STATUS_CODE_SUCCESS) {
            dismissProgressDialog(2000, DIALOG_SHOW_LOGIN_SUCCESS, ret);
            LoadActivity();
        } else {
            txtAccount.requestFocus();
            dismissProgressDialog(1500, DIALOG_SHOW_LOGING_FAIL, ret);
        }*/
        Message msg = new Message();
        msg.what = PROCESS_DO_LOGIN;
        Bundle data = new Bundle();
        data.putString("account", txtAccount.getText());
        data.putString("pass", txtPassword.getText());
        msg.setData(data);
        mOBHandler.sendMessage(msg);
    }

    private final static int PROCESS_DO_LOGIN = 790011;
    private final static int PROCESS_LOGIN_SUCCESS = 790012;
    private final static int PROCESS_LOGIN_FAIL = 790013;
    private Handler mOBHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case PROCESS_DO_LOGIN:
                    final String account = msg.getData().getString("account");
                    final String pass = msg.getData().getString("pass");
                    new Thread() {
                        public void run() {
                            int ret = ErrorKey.Success;
                            try {
                                ret = mAccountManager.Login(account, pass, true);
                            } catch (Exception e) {
                                e.printStackTrace();
                            } finally {
                                Message msgPost = new Message();
                                if(ret == ErrorKey.Success) {
                                    msgPost.what = PROCESS_LOGIN_SUCCESS;
                                } else {
                                    msgPost.what = PROCESS_LOGIN_FAIL;
                                    Bundle data = new Bundle();
                                    data.putInt("error_code", ret);
                                    msgPost.setData(data);
                                }
                                mOBHandler.sendMessage(msgPost);
                            }
                        }
                    }.start();
                    break;
                case PROCESS_LOGIN_SUCCESS:
                    dismissProgressDialog(2000, DIALOG_SHOW_LOGIN_SUCCESS, ErrorKey.Success);
                    LoadActivity();
                    break;
                case PROCESS_LOGIN_FAIL:
                    txtAccount.requestFocus();
                    int ret = msg.getData().getInt("error_code");
                    dismissProgressDialog(1500, DIALOG_SHOW_LOGING_FAIL, ret);
                    break;
            }
        }
    };

    private final class BackgroundHandler extends Handler {
        public BackgroundHandler (Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case PROCESS_FORGET_PWD:
                    final String account;

                    account = msg.getData().getString("account");

                    int ret_val = getAccountManager().forgetPass(account);
                    dismissProgressDialog();
                    if(ret_val != ErrorKey.Success) {
                        Toast.makeText(mContext, Utils.getErrorCodeString(getApplicationContext(), ret_val), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(mContext, getString(R.string.forget_pass_success_str), Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    }

    private void LoadActivity() {
        Intent intent = new Intent(LoginActivity.this, mNextActivity);
        startActivity(intent);
    }
}
