package com.quantatw.roomhub.ui;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.quantatw.myapplication.R;
import com.quantatw.roomhub.listener.OTAStateChangeListener;
import com.quantatw.roomhub.manager.OTADevice;
import com.quantatw.roomhub.manager.OTAManager;
import com.quantatw.roomhub.manager.OTAState;

/**
 * Created by erin on 1/18/16.
 */
public class OTAActivity extends AbstractRoomHubActivity {
    private final String TAG=OTAActivity.class.getSimpleName();
    private Context mContext;

    private final int ARROW_CHECK_INDEX = 0;
    private final int ARROW_DOWNLOAD_INDEX = 1;
    private final int ARROW_UPDATE_INDEX = 2;

    private View mStateLayout, mManualCheckLayout;
    private int[] mStateArrowRes = {R.id.ota_arrow_check, R.id.ota_arrow_download, R.id.ota_arrow_update};
    private ImageView[] mArrowImageView = new ImageView[mStateArrowRes.length];

    private int[] mStateTextRes = {R.id.ota_state_check, R.id.ota_state_download, R.id.ota_state_update};
    private TextView[] mStateTextView = new TextView[mStateTextRes.length];

    private OTAManager mOTAManager;
    private String mUuid;
    private TextView mDeviceNameText;

    private final int MESSAGE_CHECK_VERSION_ONGOING = 100;
    private final int MESSAGE_CHECK_VERSION_DONE = 101;
    private final int MESSAGE_UPGRADE_STATE_CHANGE = 102;
    private final int MESSAGE_UPGRADE_STATE_CHANGE_TIMEOUT = 103;

    private final int DIALOG_HAS_NO_UPDATES = 100;
    private final int DIALOG_CONFIRM_UPDATE = 101;
    private final int DIALOG_UPDATE_FAIL = 102;
    private final int DIALOG_UDPATE_DONE = 103;
    private final int DIALOG_UPDATE_TIMEOUT = 104;

    private boolean isLaunchByAutoUpdate = false;
    private ImageView mOTAImageView;
    private Animation mRotateAnimation;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_CHECK_VERSION_ONGOING:
                    enableManualCheck(true);
                    showProgressDialog("",getString(R.string.ota_version_check_onprogress));
                    break;
                case MESSAGE_CHECK_VERSION_DONE:
                    mOTAManager.log(TAG,"---MESSAGE_CHECK_VERSION_DONE---");
                    dismissProgressDialog();
                    if(msg.obj != null) {
                        OTADevice.NewVersionInfo newVersion = (OTADevice.NewVersionInfo) msg.obj;
                        mOTAManager.log(TAG,"---MESSAGE_CHECK_VERSION_DONE newVersion="+newVersion);
                        if (newVersion != null && !TextUtils.isEmpty(newVersion.getVersion())) {
                            showConfirmDialog(newVersion);
                        }
                    }
                    else {
                        showDialog(DIALOG_HAS_NO_UPDATES, "");
                    }
                    break;
                case MESSAGE_UPGRADE_STATE_CHANGE:
                    mOTAManager.log(TAG,"---MESSAGE_UPGRADE_STATE_CHANGE---");
                    refreshUpgradeState((int) msg.obj);
                    break;
                case MESSAGE_UPGRADE_STATE_CHANGE_TIMEOUT:
                    mOTAManager.log(TAG,"---MESSAGE_UPGRADE_STATE_CHANGE_TIMEOUT---");
                    enableManualCheck(true);
                    refreshUpgradeTimeoutState((int) msg.obj);
                    break;
                default:
                    super.handleMessage(msg);
                    break;
            }
        }
    };

    private void showConfirmDialog(OTADevice.NewVersionInfo newVersion) {
        if(newVersion == null)
            return;
        String name=mOTAManager.getDeviceName(mUuid);
        showDialog(DIALOG_CONFIRM_UPDATE,
                getString(R.string.ota_show_latest_version, name, newVersion.getVersion()));
    }

    private OTAStateChangeListener otaStateChangeListener = new OTAStateChangeListener() {
        @Override
        public void checkVersionStart() {
            mHandler.sendEmptyMessage(MESSAGE_CHECK_VERSION_ONGOING);
        }

        @Override
        public void checkVersionDone(OTADevice.NewVersionInfo newVersionInfo) {
            mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_CHECK_VERSION_DONE,newVersionInfo));
        }

        @Override
        public void upgradeStateChange(int upgradeState) {
            mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_UPGRADE_STATE_CHANGE, upgradeState));
        }

        @Override
        public void upgradeStateChangeTimeout(int upgradeState) {
            OTADevice.NewVersionInfo newVersionInfo = mOTAManager.getFirmwareNewVersion(mUuid);
            String currentVersion = mOTAManager.getDeviceCurrentVersion(mUuid);
            if (newVersionInfo != null && !newVersionInfo.getVersion().equals("")
                    && !currentVersion.equals("")) {
                if (currentVersion.equals(newVersionInfo.getVersion())) {
                    mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_UPGRADE_STATE_CHANGE, 3));
                    return;
                }
            }
            mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_UPGRADE_STATE_CHANGE_TIMEOUT, upgradeState));
        }
    };

    private void enableManualCheck(boolean enable) {
        if(enable) {
            mStateLayout.setVisibility(View.GONE);
            mManualCheckLayout.setVisibility(View.VISIBLE);
        }
        else {
            mStateLayout.setVisibility(View.VISIBLE);
            mManualCheckLayout.setVisibility(View.GONE);
        }
    }

    private void refreshUpgradeTimeoutState(int upgradeState) {
        /*
        String msg="";
        switch (upgradeState) {
            case 0:
            case 1:
                msg = "Download file failed!";
                break;
            case 2:
            case 3:
                msg = "Update firmware failed!";
                break;
            default:
                break;
        }
        showDialog(DIALOG_UPDATE_FAIL, msg);
        */
        showDialog(DIALOG_UPDATE_TIMEOUT, "");
    }

    private void refreshUI(OTAState currentState,boolean doCheckVersion) {
        if(currentState == null) {
            enableArrowView(ARROW_CHECK_INDEX);
            return;
        }

        if(currentState.getValue() < OTAState.VERIFY.getValue()) {
            enableArrowView(ARROW_CHECK_INDEX);
        }
        else if(currentState == OTAState.VERIFY && !doCheckVersion) {
            OTADevice.NewVersionInfo newVersionInfo = mOTAManager.getFirmwareNewVersion(mUuid);
            if(newVersionInfo != null) {
                mOTAManager.log(TAG, "refreshUI: " + newVersionInfo);
                showConfirmDialog(newVersionInfo);
            }
        }
        else if(currentState.getValue() >= OTAState.VERIFY_DONE.getValue()) {
            enableManualCheck(false);
            int upgradeState = mOTAManager.getFirmwareUpdateOngoingState(mUuid);
            refreshUpgradeState(upgradeState);
        }
    }

    private void refreshUpgradeState(int upgradeState) {
        mOTAManager.log(TAG, "refreshUpgradeState upgradeState=" + upgradeState);
        if(upgradeState < 0) {
            showDialog(DIALOG_UPDATE_FAIL, "");
            return;
        }
        switch(upgradeState) {
            case 0:
                if (!mRotateAnimation.hasStarted()) {
                    mOTAImageView.startAnimation(mRotateAnimation);
                }
            case 1:
                enableArrowView(ARROW_DOWNLOAD_INDEX);
                if (!mRotateAnimation.hasStarted()) {
                    mOTAImageView.startAnimation(mRotateAnimation);
                }
                break;
            case 2:
            case 3:
                enableArrowView(ARROW_UPDATE_INDEX);
                if(upgradeState == 3) {
                    showDialog(DIALOG_UDPATE_DONE, "");
                    mOTAImageView.clearAnimation();
                    if (mRotateAnimation.hasStarted()) {
                        mRotateAnimation.cancel();
                    }
                } else {
                    if (!mRotateAnimation.hasStarted()) {
                        mOTAImageView.startAnimation(mRotateAnimation);
                    }
                }
                break;
        }
    }

    private void autoExit() {
        if(isLaunchByAutoUpdate)
            finish();
    }

    private void showDialog(int dialogId, String msg) {
        final Dialog dialog = new Dialog(this,R.style.CustomDialog);
        dialog.setContentView(R.layout.custom_dialog_ota_confirm);
        dialog.setCancelable(false);
        dialog.setTitle("");
        TextView txt_msg=(TextView)dialog.findViewById(R.id.txt_message);
        Button btn_yes = (Button)dialog.findViewById(R.id.btn_yes);
        btn_yes.setVisibility(View.VISIBLE);
        Button btn_no = (Button)dialog.findViewById(R.id.btn_no);
        btn_no.setVisibility(View.VISIBLE);
        switch (dialogId) {
            case DIALOG_HAS_NO_UPDATES:
                txt_msg.setText(getString(R.string.ota_has_no_updates));
                btn_yes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        enableManualCheck(true);
                        dialog.dismiss();
                        autoExit();
                    }
                });
                btn_no.setVisibility(View.GONE);
                mOTAImageView.clearAnimation();
                break;
            case DIALOG_CONFIRM_UPDATE:
                txt_msg.setText(msg);
                btn_no.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mOTAManager.cancelUpgrade(mUuid);
                        enableManualCheck(true);
                        dialog.dismiss();
                        autoExit();
                    }
                });
                btn_yes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mOTAManager.startUpgrade(mUuid);
                        enableManualCheck(false);
                        dialog.dismiss();
                        //autoExit();
                        mOTAImageView.startAnimation(mRotateAnimation);
                    }
                });
                break;
            case DIALOG_UDPATE_DONE:
                txt_msg.setText(getString(R.string.ota_done));
                btn_yes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        enableManualCheck(true);
                        dialog.dismiss();
                        autoExit();
                    }
                });
                btn_no.setVisibility(View.GONE);
                mOTAImageView.clearAnimation();
                break;
            case DIALOG_UPDATE_FAIL:
                txt_msg.setText(getString(R.string.ota_fail));
                btn_yes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        enableManualCheck(true);
                        dialog.dismiss();
                        autoExit();
                    }
                });
                btn_no.setVisibility(View.GONE);
                mOTAImageView.clearAnimation();
                break;
            case DIALOG_UPDATE_TIMEOUT:
                txt_msg.setText(getString(R.string.ota_fail_timeout));
                btn_yes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        enableManualCheck(true);
                        dialog.dismiss();
                        autoExit();
                    }
                });
                btn_no.setVisibility(View.GONE);
                mOTAImageView.clearAnimation();
                break;
        }
        dialog.show();
    }

    private void enableArrowView(int index) {
        for(int i=0;i<mArrowImageView.length;i++) {
            ImageView imageView = mArrowImageView[i];
            TextView textView = mStateTextView[i];
            if(i == index) {
                imageView.setVisibility(View.VISIBLE);
                textView.setTextColor(mContext.getResources().getColor(R.color.color_white));
            }
            else {
                imageView.setVisibility(View.INVISIBLE);
                textView.setTextColor(mContext.getResources().getColor(R.color.white_mask));
            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ota_startup);
        mContext = this;
        mOTAImageView = (ImageView) findViewById(R.id.ota_renew_image);
        mRotateAnimation = AnimationUtils.loadAnimation(this, R.anim.rotate);

        mOTAManager = getOTAManager();
        Bundle bundle = getIntent().getExtras();
        mUuid = bundle.getString(OTAManager.OTA_DEVICE_UUID);
        isLaunchByAutoUpdate = bundle.getBoolean(OTAManager.OTA_AUTO_UPDATE);

        mOTAManager.log(TAG,"onCreate");
        mStateLayout = findViewById(R.id.ota_state_layout);
        mManualCheckLayout  = findViewById(R.id.ota_manual_check_layout);
        for(int i=0;i<mStateArrowRes.length;i++) {
            mArrowImageView[i] = (ImageView)findViewById(mStateArrowRes[i]);
        }
        for(int i=0;i<mStateTextRes.length;i++) {
            mStateTextView[i] = (TextView)findViewById(mStateTextRes[i]);
        }
        mDeviceNameText = (TextView)findViewById(R.id.ota_device_name);
        Button buttonCheckNow = (Button)findViewById(R.id.btnCheckNow);
        buttonCheckNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOTAManager.checkVersion(mUuid);
            }
        });

        boolean doCheckVersion = false;
        mOTAManager.registerForOTAStateChange(mUuid, otaStateChangeListener);
        OTAState currentState = mOTAManager.getFirmwareUpdateCurrentState(mUuid);
        if(currentState != null) {
            mOTAManager.log(TAG, "current state=" + currentState.getValue());
            if (currentState.getValue() < OTAState.VERIFY.getValue()) {
                doCheckVersion = true;
                mOTAManager.checkVersion(mUuid);
            }
        }
        refreshUI(currentState,doCheckVersion);
    }

    @Override
    protected void onResume() {
        mOTAManager.log(TAG,"onResume");
        super.onResume();
        mDeviceNameText.setText(mOTAManager.getDeviceName(mUuid));
    }

    @Override
    protected void onPause() {
        mOTAManager.log(TAG,"onPause");
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mOTAManager.log(TAG,"onDestroy");
        super.onDestroy();
        mOTAManager.unregisterForOTAStateChange(mUuid,otaStateChangeListener);
    }
}
