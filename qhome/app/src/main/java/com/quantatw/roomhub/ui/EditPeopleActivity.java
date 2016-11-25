package com.quantatw.roomhub.ui;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.quantatw.myapplication.R;
import com.quantatw.roomhub.manager.asset.listener.RoomHubChangeListener;
import com.quantatw.roomhub.manager.AccountManager;
import com.quantatw.roomhub.manager.asset.manager.RoomHubData;
import com.quantatw.roomhub.manager.asset.manager.RoomHubManager;
import com.quantatw.roomhub.utils.Utils;
import com.quantatw.sls.device.CloudDevice;
import com.quantatw.sls.device.FriendData;
import com.quantatw.sls.key.ErrorKey;

/**
 * Created by 95010915 on 2015/9/25.
 */
public class EditPeopleActivity extends AbstractRoomHubActivity implements View.OnClickListener,AdapterView.OnItemClickListener,RoomHubChangeListener {
    private static final String TAG = "EditPeopleActivity";
    private static boolean DEBUG=true;

    private AccountManager mAccountMgr;
    private RoomHubManager mRoomHubMgr;
    private Context mContext;

    private EditText mTxtAccount;
    private EditText mTxtNickName;
    private Button mBtnOk;
    private MyListActivity.CMD cmd_type;
    private FriendData mFriendData=null;

    private String mCurUuid;

    private final static int MESSAGE_SHOW_ERROR         = 104;
    private final static int MESSAGE_LAUNCH_DEVICE_LIST = 105;

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_SHOW_ERROR:
                    int ret=(int)msg.obj;
                    dismissProgressDialog();
                    if(DEBUG)
                        Log.d(TAG,"MESSAGE_SHOW_ERROR ret="+ret);
                    if(ret < ErrorKey.Success){
                        Toast.makeText(mContext, Utils.getErrorCodeString(getApplicationContext(),ret), Toast.LENGTH_SHORT).show();
                    }
                    finish();
                    break;
                case MESSAGE_LAUNCH_DEVICE_LIST:
                    launchDeviceList();
                    break;
            }
            super.handleMessage(msg);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_people);

        getWindow().setBackgroundDrawableResource(R.color.color_blue);
        mContext=this;

        mAccountMgr = getAccountManager();

        mRoomHubMgr=getRoomHubManager();
        mRoomHubMgr.registerRoomHubChange(this);
    }

    @Override
    protected void onResume() {
        initLayout();
        super.onResume();
    }

    private void initLayout() {

        mCurUuid=getIntent().getStringExtra(RoomHubManager.KEY_UUID);

        mTxtAccount = (EditText) findViewById(R.id.txt_account);
        mTxtNickName = (EditText) findViewById(R.id.txt_nick_name);
        mBtnOk = (Button) findViewById(R.id.btn_people_ok);
        mBtnOk.setOnClickListener(this);

        cmd_type = (MyListActivity.CMD) getIntent().getSerializableExtra(RoomHubManager.KEY_CMD_TYPE);
        if (cmd_type == MyListActivity.CMD.ADD)
            getActionBar().setTitle(getResources().getString(R.string.add_people));
        else {
            getActionBar().setTitle(getResources().getString(R.string.edit_nick_name));
            mTxtAccount.setVisibility(View.GONE);
            mFriendData = (FriendData) getIntent().getSerializableExtra(RoomHubManager.KEY_CMD_VALUE);
            mTxtNickName.setText(mFriendData.getNickName());
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mRoomHubMgr != null){
            mRoomHubMgr.unRegisterRoomHubChange(this);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_people_ok:
                SavePeople();
                break;
        }
    }

    private void SavePeople(){
        final String nick_name=mTxtNickName.getText().toString().trim();

        if(TextUtils.isEmpty(nick_name)){
            Toast.makeText(mContext, R.string.nick_name_empty_error_msg, Toast.LENGTH_SHORT).show();
            return;
        }

        if (cmd_type == MyListActivity.CMD.ADD){
            final String account=mTxtAccount.getText().toString().trim();
            if(TextUtils.isEmpty(account)){
                Toast.makeText(mContext, R.string.account_empty_error_msg, Toast.LENGTH_SHORT).show();
                return;
            }
            if(account.equalsIgnoreCase(mAccountMgr.getCurrentAccountName()) || account.equalsIgnoreCase(mAccountMgr.getCurrentEmail())){
                Toast.makeText(mContext, R.string.self_not_need_join_list, Toast.LENGTH_SHORT).show();
                return;
            }

            if(CheckAccountIsExist()==false) {
                Toast.makeText(mContext, R.string.account_not_exist, Toast.LENGTH_SHORT).show();
                return;
            }
            showProgressDialog("", getString(R.string.processing_str));
            Thread thread = new Thread() {
                @Override
                public void run() {
                    int ret;
                    ret=mAccountMgr.AddUserFriend(account, nick_name);

                    if(DEBUG)
                        Log.d(TAG,"add to share ret="+ret);


                    mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_SHOW_ERROR,ret));
                }
            };
            thread.start();
        }else{
            showProgressDialog("", getString(R.string.processing_str));
            Thread thread = new Thread() {
                @Override
                public void run() {
                    int ret;
                    ret=mAccountMgr.modifyFrinedNickName(mFriendData.getUserAccount(), nick_name);

                    if(DEBUG)
                        Log.d(TAG,"modify nick name ret="+ret);


                    mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_SHOW_ERROR,ret));
                }
            };
            thread.start();
        }
    }

    private boolean CheckAccountIsExist(){
        String account=Utils.trimSpace(mTxtAccount.getText().toString());
        int ret;
        boolean retval=false;

        if(Utils.CheckEmailFormat(account) == true) {
            ret = getAccountManager().checkEmail(account);
            if(ret == ErrorKey.EmailExist) {
                retval=true;
            }
        }else{
            ret = getAccountManager().checkUserName(account);
            if(ret == ErrorKey.AccountExist) {
                retval=true;
            }
        }
        return retval;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void addDevice(RoomHubData data) {

    }

    @Override
    public void removeDevice(RoomHubData data) {
        if(data!=null) {
            if(!TextUtils.isEmpty(mCurUuid)) {
                if (data.getUuid().equals(mCurUuid)) {
                    mHandler.sendEmptyMessage(MESSAGE_LAUNCH_DEVICE_LIST);
                }
            }
        }
    }

    @Override
    public void UpdateRoomHubData(int type, RoomHubData data) {
        if(data != null) {
            if ((type == RoomHubManager.UPDATE_ROOMHUB_DATA)) {
                if(!TextUtils.isEmpty(mCurUuid)) {
                    if (data.getUuid().equals(mCurUuid) && !data.IsOnLine()) {
                        mHandler.sendEmptyMessage(MESSAGE_LAUNCH_DEVICE_LIST);
                    }
                }
            }
        }
    }
    /*
    @Override
    public void UpdateRoomHubDeviceSeq(MicroLocationData locationData) {

    }
    */
    @Override
    public void UpdateDeviceShareUser(CloudDevice device) {

    }

    @Override
    public void UpgradeStatus(String uuid, boolean is_upgrade) {
        if(!TextUtils.isEmpty(mCurUuid)) {
            if (uuid.equals(mCurUuid) && (is_upgrade == true)) {
                mHandler.sendEmptyMessage(MESSAGE_LAUNCH_DEVICE_LIST);
            }
        }
    }
}
