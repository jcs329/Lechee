package com.quantatw.roomhub.ui;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.quantatw.myapplication.R;
import com.quantatw.roomhub.manager.asset.listener.RoomHubChangeListener;
import com.quantatw.roomhub.manager.asset.manager.RoomHubData;
import com.quantatw.roomhub.manager.asset.manager.RoomHubManager;
import com.quantatw.roomhub.utils.GlobalDef;
import com.quantatw.sls.api.DeviceTypeConvertApi;
import com.quantatw.sls.device.CloudDevice;

/**
 * Created by 95010915 on 2015/9/25.
 */
public class ShareHubInfoActivity extends AbstractRoomHubActivity implements View.OnClickListener,RoomHubChangeListener {
    private static final String TAG = "ShareHubInfoActivity";
    private static boolean DEBUG=true;

    private RoomHubManager mRoomHubMgr;
    private TextView mTxtOwnerTitle;
    private TextView mTxtOwnerName;

    private ImageView mBtnCancel;

    private String mCurUuid;
    private String owner_name;
    private int mDeviceType;

    private final static int MESSAGE_REMOVE_DEVICE      = 103;

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_REMOVE_DEVICE:
                    RoomHubData data=(RoomHubData)msg.obj;
                    if(data.getUuid().equals(mCurUuid))
                        finish();
                    break;
            }
            super.handleMessage(msg);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.room_hub_share_info);

        mRoomHubMgr=getRoomHubManager();
        mRoomHubMgr.registerRoomHubChange(this);
    }

    @Override
    protected void onResume() {
        initLayout();
        super.onResume();
    }

    private void initLayout(){
        mCurUuid=getIntent().getExtras().getString(RoomHubManager.KEY_UUID);
        owner_name=getIntent().getExtras().getString("owner_name");
        mDeviceType=getIntent().getExtras().getInt(GlobalDef.KEY_DEVICE_TYPE);

        mTxtOwnerTitle = (TextView)findViewById(R.id.txt_owner_title);
        if(mDeviceType != DeviceTypeConvertApi.CATEGORY.ROOMHUB) {
            int titleResource = getHealthDeviceManager().getHealthDeviceTypeTitleResource(mDeviceType);
            mTxtOwnerTitle.setText(getString(R.string.health_device_owner,getString(titleResource)));
        }
        else
            mTxtOwnerTitle.setText(R.string.room_hub_owner);
        mTxtOwnerName = (TextView) findViewById(R.id.txt_owner_name);
        mTxtOwnerName.setText(owner_name);

        mBtnCancel = (ImageView) findViewById(R.id.btn_cancel);
        mBtnCancel.setOnClickListener(this);
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
            case R.id.btn_cancel:
                finish();
                break;
        }
    }

    @Override
    public void addDevice(RoomHubData data) {

    }

    @Override
    public void removeDevice(RoomHubData data) {
        if(data!=null)
            mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_REMOVE_DEVICE, data));
    }

    @Override
    public void UpdateRoomHubData(int type, RoomHubData data) {
        if(data != null) {
            if ((type == RoomHubManager.UPDATE_ONLINE_STATUS)) {
                if (data.getUuid().equals(mCurUuid) && !data.IsOnLine()) {
                    finish();
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
        if(uuid.equals(mCurUuid) && (is_upgrade == true)){
            finish();
        }
    }
}
