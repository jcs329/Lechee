package com.quantatw.roomhub.ui;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.quantatw.myapplication.R;
import com.quantatw.roomhub.utils.GlobalDef;
import com.quantatw.roomhub.utils.Utils;
import com.quantatw.sls.key.ErrorKey;

/**
 * Created by 95010915 on 2015/9/24.
 */
public class SignupActivity extends AbstractRoomHubActivity {
    private EditText txtUserName,txtUserAccount,txtUserPass,txtConfirmPass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup_layout);

        View backView = (View)findViewById(R.id.backLayout);
        /*
        ImageView imgBack = (ImageView) backView.findViewById(R.id.imgBack);
        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        */

        txtUserName = (EditText) findViewById(R.id.loginUserName);
        txtUserAccount = (EditText) findViewById(R.id.loginAccount);
        txtUserPass = (EditText) findViewById(R.id.signupPwd);
        txtConfirmPass = (EditText) findViewById(R.id.confirmPwd);

        TextView txtTitle = (TextView) backView.findViewById(R.id.txtTitle);
        txtTitle.setText(getString(R.string.back));
        txtTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        setSignupProcess();
    }

    private void setSignupProcess() {
        Button btnSignup = (Button) findViewById(R.id.btnSignup);
        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doSignup();
            }
        });

        final EditText txtUserPass = (EditText) findViewById(R.id.signupPwd);
        final EditText txtConfirmPass = (EditText) findViewById(R.id.confirmPwd);

        CheckBox chkShowPass = (CheckBox) findViewById(R.id.chkShowPass);
        chkShowPass.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked == true) {
                    txtUserPass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
                    txtConfirmPass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
                } else {
                    txtUserPass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
                    txtConfirmPass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
                }
                txtUserPass.setSelection(txtUserPass.getText().length());
                txtConfirmPass.setSelection(txtConfirmPass.getText().length());
            }
        });
    }

    private void doSignup() {
        String userName = Utils.trimSpace(txtUserName.getText().toString());
        String account = Utils.trimSpace(txtUserAccount.getText().toString());

        if(TextUtils.isEmpty(userName)) {
            Toast.makeText(this, getString(R.string.account_empty_error_msg), Toast.LENGTH_SHORT).show();
            txtUserName.requestFocus();
            return;
        }

        if(userName.contains("@")) {
            Toast.makeText(this, R.string.account_format_error_msg,Toast.LENGTH_SHORT).show();
            txtUserName.requestFocus();
            return;
        }

        if(!Utils.CheckPasswordAvailable(txtUserPass.getText().toString())) {
            Toast.makeText(this, getString(R.string.password_len_wrong), Toast.LENGTH_SHORT).show();
            txtUserAccount.requestFocus();
            return;
        }

        if(Utils.CheckConfirmPwd(txtUserPass.getText().toString(), txtConfirmPass.getText().toString()) == false) {
            Toast.makeText(this, getString(R.string.wrong_password_confirm_str), Toast.LENGTH_SHORT).show();
            txtConfirmPass.requestFocus();
            return;
        }

        if(Utils.CheckEmailFormat(account) == false) {
            Toast.makeText(this, getString(R.string.wrong_account_format_str), Toast.LENGTH_SHORT).show();
            txtUserAccount.requestFocus();
            return;
        }

        if(isYahooMail(account) == true) {
            showYahooMailDialog();
            return;
        }
        else
            verifyEmailAccount();
    }

    private void showYahooMailDialog() {
        final Dialog dialog = new Dialog(this,R.style.CustomDialog);
        dialog.setContentView(R.layout.custom_dialog);
        TextView txt_msg=(TextView)dialog.findViewById(R.id.txt_message);
        txt_msg.setText(getString(R.string.yahoo_mail_warning_msg));

        Button btn_yes = (Button)dialog.findViewById(R.id.btn_yes);
        btn_yes.setText(R.string.reenter_str);
        btn_yes.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                txtUserAccount.requestFocus();
                dialog.dismiss();
            }
        });

        Button btn_no = (Button)dialog.findViewById(R.id.btn_no);
        btn_no.setText(R.string.continue_str);
        btn_no.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();
                verifyEmailAccount();
            }
        });

        dialog.setCancelable(false);
        dialog.show();
    }

    private void verifyEmailAccount() {
        String userName = Utils.trimSpace(txtUserName.getText().toString());
        String account = Utils.trimSpace(txtUserAccount.getText().toString());

        showProgressDialog("", getString(R.string.process_str));

        int ret = getAccountManager().checkUserName(userName);
        if(ret != ErrorKey.Success) {
            Toast.makeText(this, Utils.getErrorCodeString(getApplicationContext(), ret), Toast.LENGTH_SHORT).show();
            txtUserName.requestFocus();
            dismissProgressDialog(1500, DIALOG_SHOW_NOTHING, 0);
            return;
        }

        ret = getAccountManager().checkEmail(account);
        if(ret != ErrorKey.Success) {
            Toast.makeText(this, Utils.getErrorCodeString(getApplicationContext(), ret), Toast.LENGTH_SHORT).show();
            txtUserAccount.requestFocus();
            dismissProgressDialog(1500, DIALOG_SHOW_NOTHING, 0);
            return;
        }

        Message msg = new Message();
        msg.what = PROCESS_DO_SIGNUP;
        Bundle data = new Bundle();
        data.putString(LoginConfirmActivity.KEY_USER_NAME, userName);
        data.putString(LoginConfirmActivity.KEY_USER_PASS, txtUserPass.getText().toString());
        data.putString(LoginConfirmActivity.KEY_USER_ACCOUNT, account);
        msg.setData(data);
        mOBHandler.sendMessage(msg);
    }

    private boolean isYahooMail(String email) {
        if(email.contains("@yahoo.") || email.contains("@kimo.") || email.contains("@ymail.") || email.contains("@rocketmail."))
            return true;
        return false;
    }

    private final static int PROCESS_DO_SIGNUP = 390011;
    private final static int PROCESS_SIGNUP_SUCCESS = 390012;
    private final static int PROCESS_SIGNUP_FAIL = 390013;
    private Handler mOBHandler = new Handler() {
        public void handleMessage(final Message msg) {
            switch (msg.what) {
                case PROCESS_DO_SIGNUP:
                    final String username = msg.getData().getString(LoginConfirmActivity.KEY_USER_NAME);
                    final String account = msg.getData().getString(LoginConfirmActivity.KEY_USER_ACCOUNT);
                    final String pass = msg.getData().getString(LoginConfirmActivity.KEY_USER_PASS);

                    new Thread() {
                        public void run() {
                            int ret = ErrorKey.Success;
                            try {
                                ret = getAccountManager().createNewAccount(username, pass, account);
                            } catch (Exception e) {
                                e.printStackTrace();
                            } finally {
                                Message msgPost = new Message();
                                if(ret == ErrorKey.Success) {
                                    msgPost.what = PROCESS_SIGNUP_SUCCESS;
                                    Bundle newData = new Bundle();
                                    newData.putString(LoginConfirmActivity.KEY_USER_ACCOUNT, account);
                                    newData.putString(LoginConfirmActivity.KEY_USER_NAME, username);
                                    newData.putString(LoginConfirmActivity.KEY_USER_PASS,pass);
                                    msgPost.setData(newData);
                                } else {
                                    msgPost.what = PROCESS_SIGNUP_FAIL;
                                    Bundle data = new Bundle();
                                    data.putInt("error_code", ret);
                                    msgPost.setData(data);
                                }
                                mOBHandler.sendMessage(msgPost);
                            }
                        }
                    }.start();
                    break;
                case PROCESS_SIGNUP_SUCCESS:
                    dismissProgressDialog();
                    Intent intent = new Intent();
                    intent.putExtra(LoginConfirmActivity.KEY_USER_NAME, msg.getData().getString(LoginConfirmActivity.KEY_USER_NAME));
                    intent.putExtra(LoginConfirmActivity.KEY_USER_PASS, msg.getData().getString(LoginConfirmActivity.KEY_USER_PASS));
                    intent.putExtra(LoginConfirmActivity.KEY_USER_ACCOUNT, msg.getData().getString(LoginConfirmActivity.KEY_USER_ACCOUNT));
                    setResult(RESULT_OK,intent);
                    finish();
                    //LoadRoomHubMainPage();
                    break;
                case PROCESS_SIGNUP_FAIL:
                    EditText txtUserAccount = (EditText) findViewById(R.id.loginAccount);
                    txtUserAccount.requestFocus();
                    int ret = msg.getData().getInt("error_code");
                    dismissProgressDialog(2000, DIALOG_SHOW_SIGNUP_FAIL, ret);
                    break;
            }
        }
    };

    private void LoadRoomHubMainPage() {
        if(Utils.getProvision(this) == GlobalDef.PROVISION_SET) {
            Intent intent = new Intent(SignupActivity.this, RoomHubMainPage.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addCategory(Intent.CATEGORY_HOME);
            startActivity(intent);
            //System.exit(0);
        } else {
            Intent intent = new Intent(SignupActivity.this, SetupWifiActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addCategory(Intent.CATEGORY_HOME);
            startActivity(intent);
            //System.exit(0);
        }
    }
}
