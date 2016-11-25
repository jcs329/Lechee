package com.quantatw.roomhub.ui;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.quantatw.myapplication.R;
import com.quantatw.roomhub.manager.asset.listener.RoomHubChangeListener;
import com.quantatw.roomhub.listener.UserFriendChangedListener;
import com.quantatw.roomhub.manager.AccountManager;
import com.quantatw.roomhub.manager.asset.manager.RoomHubData;
import com.quantatw.roomhub.manager.asset.manager.RoomHubManager;
import com.quantatw.roomhub.utils.Utils;
import com.quantatw.sls.device.CloudDevice;
import com.quantatw.sls.device.FriendData;
import com.quantatw.sls.key.ErrorKey;

import java.util.ArrayList;

/**
 * Created by 95010915 on 2015/9/25.
 */
public class MyListActivity extends AbstractRoomHubActivity implements View.OnClickListener,RoomHubChangeListener,UserFriendChangedListener {
    private static final String TAG = "MyListActivity";
    private static boolean DEBUG=true;

    private AccountManager mAccountMgr;
    private RoomHubManager mRoomHubMgr;
    private Context mContext;

    private ListView mLstPeople;
    private Button mBtnAdd;

    private ArrayList<FriendData>  mFrinedDataList;
    private MyListAdapter mAdapter;

    private String mCurUuid;

    private final static int MESSAGE_REMOVE_FRIEND_ERROR =104;
    private final static int MESSAGE_REFRESH                =105;
    private final static int MESSAGE_UPDATE_NICK_NAME    =106;
    private final static int MESSAGE_LAUNCH_DEVICE_LIST  =107;

    public enum CMD{
        ADD,
        EDIT
    }

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_REMOVE_FRIEND_ERROR:
                    Toast.makeText(mContext, Utils.getErrorCodeString(getApplicationContext(),(int)msg.obj), Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_REFRESH:
                    mAdapter.notifyDataSetChanged();
                    break;
                case MESSAGE_UPDATE_NICK_NAME:
                    FriendData friend_data=(FriendData)msg.obj;

                    int idx=mFrinedDataList.indexOf(friend_data);

                    if(idx < 0) return;

                    int first_visible=mLstPeople.getFirstVisiblePosition();
                    int last_visible=mLstPeople.getLastVisiblePosition();
                    if((idx >= first_visible) && (idx <= last_visible)) {
                        int pos=idx-first_visible;
                        View v=mLstPeople.getChildAt(pos);
                        if(v != null){
                            TextView tv_nick_name = (TextView) v.findViewById(R.id.txt_people_nick_name);
                            tv_nick_name.setText(friend_data.getNickName());
                        }
                    }
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
        setContentView(R.layout.my_list);
        mContext=this;
        mAccountMgr=getAccountManager();
        mAccountMgr.registerUserFriendChanged(this);
        mRoomHubMgr=getRoomHubManager();
        mRoomHubMgr.registerRoomHubChange(this);
    }

    @Override
    protected void onResume() {
        initLayout();
        super.onResume();
    }

    private void initLayout(){
        mCurUuid=getIntent().getStringExtra(RoomHubManager.KEY_UUID);

        mLstPeople =  (ListView) findViewById(R.id.lst_my_list);
        mBtnAdd =  (Button) findViewById(R.id.btn_add_people);
        mBtnAdd.setOnClickListener(this);

        mFrinedDataList=mAccountMgr.GetUserFriendList();

        mAdapter=new MyListAdapter(this, mFrinedDataList);
        mLstPeople.setAdapter(mAdapter);
        //mLstPeople.setOnItemClickListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mAccountMgr != null){
            mAccountMgr.unRegisterUserFriendChanged(this);
        }
        if(mRoomHubMgr != null){
            mRoomHubMgr.unRegisterRoomHubChange(this);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_add_people:
                OpenEditPeople(CMD.ADD, null);
                break;
        }
    }

    public void OpenEditPeople(CMD cmd_type,FriendData friend_data){

        Intent intent = new Intent();
        intent.setClass(this, EditPeopleActivity.class);

        Bundle bundle = new Bundle();
        bundle.putSerializable(RoomHubManager.KEY_CMD_TYPE, cmd_type);
        if(friend_data != null)
            bundle.putSerializable(RoomHubManager.KEY_CMD_VALUE, friend_data);

        bundle.putString(RoomHubManager.KEY_UUID, mCurUuid);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    public void DeletePeople(FriendData friend_data){
        DeletePeopleDialog(friend_data);
    }

    private void DeletePeopleDialog(FriendData friend_data){
        final FriendData data=friend_data;

        final Dialog dialog = new Dialog(this,R.style.CustomDialog);

        dialog.setContentView(R.layout.custom_dialog);

        TextView txt_msg=(TextView)dialog.findViewById(R.id.txt_message);
        txt_msg.setText(getString(R.string.confirm_delete_people));

        Button btn_yes = (Button)dialog.findViewById(R.id.btn_yes);
        btn_yes.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();
                showProgressDialog("", getString(R.string.processing_str));
                Thread thread = new Thread() {
                    @Override
                    public void run() {
                        Log.d(TAG, "DeletePeopleDialog account=" + data.getUserAccount());
                        int ret = mAccountMgr.RemoveUserFriend(data.getUserAccount());
                        if (DEBUG)
                            Log.d(TAG, "DeletePeopleDialog ret=" + ret);
                        dismissProgressDialog();

                        if (ret != ErrorKey.Success) {
                            mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_REMOVE_FRIEND_ERROR, ret));
                        }
                    }
                };
                thread.start();
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

    @Override
    public void AddFriend(FriendData friend_data) {
        if(friend_data == null) return;

        synchronized (mFrinedDataList) {
            if(!mFrinedDataList.contains(friend_data)){
                mFrinedDataList.add(friend_data);
            }
        }

        mHandler.sendEmptyMessage(MESSAGE_REFRESH);
    }

    @Override
    public void UpdateFriend(FriendData friend_data) {
        if(friend_data == null) return;
        mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_UPDATE_NICK_NAME, friend_data));
    }

    @Override
    public void RemoveFriend(FriendData friend_data) {
        if(friend_data == null) return;

        synchronized (mFrinedDataList) {
            mFrinedDataList.remove(friend_data);
        }
        mHandler.sendEmptyMessage(MESSAGE_REFRESH);
    }

    @Override
    public void addDevice(RoomHubData data) {

    }

    @Override
    public void removeDevice(RoomHubData data) {
        if(data!=null) {
            if(!TextUtils.isEmpty(mCurUuid)) {
                if (data.getUuid().equals(mCurUuid))
                    mHandler.sendEmptyMessage(MESSAGE_LAUNCH_DEVICE_LIST);
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
