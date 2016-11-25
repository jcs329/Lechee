package com.quantatw.roomhub.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.quantatw.myapplication.R;
import com.quantatw.roomhub.manager.AccountManager;
import com.quantatw.roomhub.utils.GlobalDef;

public class EditProfileActivity extends AbstractRoomHubActivity implements View.OnClickListener {
    private AccountManager mAccountMgr=null;
    private Context mContext;
    private Button btn_login;
    private TextView txt_account_name;
    private Button changePassButton;
    private boolean mIsLogin;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_profile);

        mContext=this;
        mAccountMgr=getAccountManager();
        mIsLogin=mAccountMgr.isLogin();

        initLayout();
    }

    private void initLayout(){

        txt_account_name=(TextView)findViewById(R.id.txt_account_name);
        txt_account_name.setText(mAccountMgr.getCurrentAccountName());

        btn_login=(Button)findViewById(R.id.btn_login);
        if(mIsLogin){
            btn_login.setText(R.string.logout_str);
        }else{
            btn_login.setText(R.string.login_str);
        }
        btn_login.setOnClickListener(this);
        changePassButton = (Button)findViewById(R.id.btn_change_pwd);
        changePassButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_login: {
                if(mIsLogin){
                   mAccountMgr.Logout();
                }

                Intent intent = new Intent(this, MainActivity.class);
                intent.setAction(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_LAUNCHER);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME);
                intent.putExtra(GlobalDef.KEY_IS_LOGOUT, true);
                startActivity(intent);
            }
                break;
            case R.id.btn_change_pwd: {
                if(!mAccountMgr.isLogin()) {
                    Toast.makeText(mContext, getString(R.string.not_login_yet), Toast.LENGTH_SHORT).show();
                    return;
                }

                Intent intent = new Intent(this, ChangePasswordActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
                break;
        }
    }
}
