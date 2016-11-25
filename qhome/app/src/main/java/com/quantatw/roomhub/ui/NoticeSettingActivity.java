package com.quantatw.roomhub.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

import com.quantatw.myapplication.R;
import com.quantatw.roomhub.manager.asset.listener.AssetChangeListener;
import com.quantatw.roomhub.manager.NoticeSetting;
import com.quantatw.roomhub.manager.asset.manager.ACData;
import com.quantatw.roomhub.manager.asset.manager.ACManager;
import com.quantatw.roomhub.manager.asset.manager.RoomHubManager;
import com.quantatw.roomhub.utils.RoomHubDef;
import com.quantatw.sls.api.DeviceTypeConvertApi;

public class NoticeSettingActivity extends AbstractRoomHubActivity implements View.OnClickListener,AssetChangeListener {
    private static final String TAG = "NoticeSettingActivity";

    private TextView mTxtTempDelta;
    private TextView mTxtTempMsg;
    private TextView mTxtTime;
    private TextView mTxtTimeMsg;
    private ImageView mBtnDeltaLower;
    private ImageView mBtnDeltaHigher;
    private ImageView mBtnTimeLower;
    private ImageView mBtnTimeHigher;

    private ACManager mACMgr;
    private ACData mData;
    private NoticeSetting mNoticeSetting;
    private String mCurUuid;

    private final int MESSAGE_LAUNCH_DEVICE_LIST = 100;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Log.d(TAG,"message what="+msg.what);
            switch(msg.what) {
                case MESSAGE_LAUNCH_DEVICE_LIST:
                    launchDeviceList();
                    break;
                default:
                    super.handleMessage(msg);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notice_setting);
        getWindow().setBackgroundDrawableResource(R.drawable.background);

        WebView webview_content=(WebView)findViewById(R.id.webview_content);
        WebSettings web_seting=webview_content.getSettings();
        web_seting.setDefaultFontSize((int) getResources().getDimension(R.dimen.notice_desc_font_size));

        webview_content.setBackgroundColor(Color.TRANSPARENT);
        webview_content.loadDataWithBaseURL(null, getResources().getString(R.string.notice_msg), "text/html", "utf-8", null);

        mACMgr=(ACManager)getRoomHubManager().getAssetDeviceManager(DeviceTypeConvertApi.TYPE_ROOMHUB.AC);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mACMgr.registerAssetsChange(this);
        mCurUuid=getIntent().getStringExtra(RoomHubManager.KEY_UUID);
        mData=mACMgr.getCurrentACDataByUuid(mCurUuid);
        mNoticeSetting=mData.getNoticeSetting();
        initLayout();
    }

    private void initLayout(){

        mTxtTempDelta=(TextView)findViewById(R.id.txt_temp_delta);
        mTxtTempDelta.setText(String.valueOf(mNoticeSetting.getNoticeDelta()));

        mTxtTempMsg=(TextView)findViewById(R.id.txt_temp_msg);
        if(mNoticeSetting.getIsDefaultDelta() == 1)
            mTxtTempMsg.setText(R.string.notice_suggestion_delta);
        else
            mTxtTempMsg.setText(R.string.notice_custom_delta);

        mTxtTime=(TextView)findViewById(R.id.txt_time);
        mTxtTime.setText(String.valueOf(mNoticeSetting.getNoticeTime() / 60));

        mTxtTimeMsg=(TextView)findViewById(R.id.txt_time_msg);
        if(mNoticeSetting.getIsDefaultTime() == 1)
            mTxtTimeMsg.setText(R.string.notice_suggestion_time);
        else
            mTxtTimeMsg.setText(R.string.notice_custom_time);

        mBtnDeltaLower=(ImageView)findViewById(R.id.btn_delta_lower);
        mBtnDeltaLower.setOnClickListener(this);
        mBtnDeltaHigher=(ImageView)findViewById(R.id.btn_delta_higher);
        mBtnDeltaHigher.setOnClickListener(this);

        mBtnTimeLower=(ImageView)findViewById(R.id.btn_time_lower);
        mBtnTimeLower.setOnClickListener(this);
        mBtnTimeHigher=(ImageView)findViewById(R.id.btn_time_higher);
        mBtnTimeHigher.setOnClickListener(this);
    }
    @Override
    protected void onPause() {
        mACMgr.unRegisterAssetsChange(this);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    @Override
    public void onClick(View v) {
        int delta=mNoticeSetting.getNoticeDelta();
        int time=mNoticeSetting.getNoticeTime();
        switch(v.getId()){
            case R.id.btn_delta_lower:
                delta--;
                SaveTempDelta(delta);
                break;
            case R.id.btn_delta_higher:
                delta++;
                SaveTempDelta(delta);
                break;
            case R.id.btn_time_lower:
                time-=60;
                SaveTime(time);
                break;
            case R.id.btn_time_higher:
                time+=60;
                SaveTime(time);
                break;
        }
    }

    private void SaveTempDelta(int delta){
        if((delta >= 1) && (delta <= 5)){
            mNoticeSetting.setNoticeDelta(delta);
            mNoticeSetting.setIsDefaultDelta(0);
            mData.setNoticeSetting(mNoticeSetting);

            mTxtTempDelta.setText(String.valueOf(mNoticeSetting.getNoticeDelta()));
            if(mNoticeSetting.getIsDefaultDelta() == 1)
                mTxtTempMsg.setText(R.string.notice_suggestion_delta);
            else
                mTxtTempMsg.setText(R.string.notice_custom_delta);
        }
    }

    private void SaveTime(int time){
        if((time >= (5*60)) && (time <= (15*60))){
            mNoticeSetting.setNoticeTime(time);
            mNoticeSetting.setIsDefaultTime(0);
            mData.setNoticeSetting(mNoticeSetting);

            mTxtTime.setText(String.valueOf(mNoticeSetting.getNoticeTime() / 60));
            if(mNoticeSetting.getIsDefaultTime() == 1)
                mTxtTimeMsg.setText(R.string.notice_suggestion_time);
            else
                mTxtTimeMsg.setText(R.string.notice_custom_time);
        }
    }

    @Override
    public void addDevice(int asset_type, Object data) {

    }

    @Override
    public void removeDevice(int asset_type, Object data) {
        if(asset_type != DeviceTypeConvertApi.TYPE_ROOMHUB.AC)
            return;

        if(data != null) {
            if (((ACData)data).getAssetUuid().equals(mData.getAssetUuid())) {
                mHandler.sendEmptyMessage(MESSAGE_LAUNCH_DEVICE_LIST);
            }
        }
    }

    @Override
    public void UpdateAssetData(int asset_type, Object data) {

    }

    @Override
    public void UpdatePageStatus(int asset_type, boolean enabled, Object data) {
        if(asset_type != DeviceTypeConvertApi.TYPE_ROOMHUB.AC)
            return;

        if(data != null) {
            if (((ACData)data).getAssetUuid().equals(mData.getAssetUuid()) && (enabled == false)){
                mHandler.sendEmptyMessage(MESSAGE_LAUNCH_DEVICE_LIST);
            }
        }
    }

    @Override
    public void onCommandResult(int asset_type, String uuid, int result) {

    }
}
