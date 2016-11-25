package com.quantatw.roomhub.ui;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.quantatw.myapplication.R;
import com.quantatw.roomhub.ir.ApIRParingInfo;
import com.quantatw.roomhub.manager.asset.listener.RoomHubChangeListener;
import com.quantatw.roomhub.manager.IRController;
import com.quantatw.roomhub.manager.asset.manager.RoomHubData;
import com.quantatw.roomhub.manager.asset.manager.RoomHubManager;
import com.quantatw.roomhub.utils.RoomHubDef;
import com.quantatw.sls.api.DeviceTypeConvertApi;
import com.quantatw.sls.device.CloudDevice;
import com.quantatw.sls.object.IRCodeNumData;

import java.util.ArrayList;

/**
 * Created by erin on 10/13/15.
 */
public class IRLearningActivity extends FragmentActivity implements RoomHubChangeListener {

    private final String TAG=IRLearningActivity.class.getSimpleName();

    private Context mContext;
    private IRLearningStartupFragment mStartupFragment;
    private RoomHubManager mRoomHubManager;
    private IRController mIRController;
    private String mUuid;
    private String mAssetUuid;
    private int mAssetType;
    private Fragment mCurrentFragment;

    private ProgressDialog mProgressDialog;

    private final int MESSAGE_GET_IR_LEARNING_RESULTS = 100;
    private final int MESSAGE_GET_IR_LEARNING_RESULTS_TIMEOUT = 101;
    private final int MESSAGE_GET_IR_AUTOSCAN_RESULTS_TIMEOUT = 102;
    private final int MESSAGE_GET_IR_LEARNING_RESULTS_FAIL = 103;
    private final int MESSAGE_LAUNCH_DEVICE_LIST = 104;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case MESSAGE_GET_IR_LEARNING_RESULTS:
                    removeMessages(MESSAGE_GET_IR_LEARNING_RESULTS_TIMEOUT);
                    //gotoNextFragment((String) msg.obj);
                    break;
                case MESSAGE_GET_IR_LEARNING_RESULTS_FAIL:
                case MESSAGE_GET_IR_AUTOSCAN_RESULTS_TIMEOUT:
                case MESSAGE_GET_IR_LEARNING_RESULTS_TIMEOUT:
                    if(mProgressDialog != null && mProgressDialog.isShowing()) {
                        mProgressDialog.dismiss();
                        mProgressDialog = null;
                    }
                    if(msg.what == MESSAGE_GET_IR_LEARNING_RESULTS_FAIL) {
                        int reason = (int)msg.obj;
                        mIRController.log(TAG, "got MESSAGE_GET_IR_LEARNING_RESULTS_FAIL fail reason="+reason);
                        StringBuilder sb = new StringBuilder("response error: ");
                        if(reason == IRSettingDataValues.IR_LEARNING_TIMEOUT)
                            sb.append("timeout (sent from RoomHub)");
                        else if(reason == IRSettingDataValues.IR_LEARNING_NOT_MATCHED)
                            sb.append("learning results are not found from server");

                        mIRController.log(TAG,sb.toString());
//                        Toast.makeText(mContext,sb.toString(),Toast.LENGTH_SHORT).show();
                    }
                    else if(msg.what == MESSAGE_GET_IR_AUTOSCAN_RESULTS_TIMEOUT) {
                        StringBuilder sb = new StringBuilder();
                        sb.append("got MESSAGE_GET_IR_AUTOSCAN_RESULTS_TIMEOUT ");
                        sb.append(Integer.toString(getResources().getInteger(R.integer.config_ir_auto_scan_timeout))+" milliseconds timeout!");
                        mIRController.log(TAG,sb.toString());
                    }
                    else if(msg.what == MESSAGE_GET_IR_LEARNING_RESULTS_TIMEOUT) {
                        StringBuilder sb = new StringBuilder();
                        sb.append("got MESSAGE_GET_IR_LEARNING_RESULTS_TIMEOUT ");
                        sb.append(Integer.toString(getResources().getInteger(R.integer.config_ir_learning_timeout))+" milliseconds timeout!");
                        mIRController.log(TAG,sb.toString());
                    }
                    gotoFailFragment();
                    break;
                case MESSAGE_LAUNCH_DEVICE_LIST:
                    launchDeviceList();
                    break;
                default:
                    super.handleMessage(msg);
                    break;
            }
        }
    };

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(IRSettingDataValues.ACTION_IR_LEARNING_RESULTS)) {
                mIRController.log(TAG, "got ACTION_IR_LEARNING_RESULTS");
                // Only handle learning results on Startup Fragment:
                if(mCurrentFragment instanceof IRLearningStartupFragment) {
                    String uuid = intent.getStringExtra(IRSettingDataValues.KEY_DATA_ROOM_HUB_DEVICE_UUID);
                    ArrayList<ApIRParingInfo> learningResult = intent.getParcelableArrayListExtra(IRSettingDataValues.KEY_IR_LEARNING_RESULTS);
                    int reason = intent.getIntExtra(IRSettingDataValues.KEY_IR_LEARNING_RESULTS_REASON, IRSettingDataValues.IR_LEARNING_SUCCESS);
                    if(reason != IRSettingDataValues.IR_LEARNING_SUCCESS) {
                        mHandler.removeMessages(MESSAGE_GET_IR_LEARNING_RESULTS_TIMEOUT);
                        mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_GET_IR_LEARNING_RESULTS_FAIL, reason));
                        return;
                    }

                    Intent sendIntent = new Intent(mContext, IRPairingActivity.class);
                    sendIntent.putExtra(IRSettingDataValues.KEY_DATA_IR_SETTING_MODE,IRSettingDataValues.IR_SETTING_MODE_LEARN_CODES);
                    sendIntent.putExtra(IRSettingDataValues.KEY_DATA_ROOM_HUB_DEVICE_UUID, uuid);
                    sendIntent.putExtra(IRSettingDataValues.KEY_ELECTRIC_TYPE, mAssetType);
                    sendIntent.putParcelableArrayListExtra(IRSettingDataValues.KEY_DATA_IR_PARING_INFO, learningResult);
                    startActivityForResult(sendIntent, IRSettingDataValues.REQUEST_CODE_IR_CONFIG_DONE);
                    mHandler.removeMessages(MESSAGE_GET_IR_LEARNING_RESULTS_TIMEOUT);
                }
            }
            else if(intent.getAction().equals(IRSettingDataValues.ACTION_IR_AUTO_SCAN_RESULTS)) {
                mIRController.log(TAG, "got ACTION_IR_AUTO_SCAN_RESULTS");
                mHandler.removeMessages(MESSAGE_GET_IR_AUTOSCAN_RESULTS_TIMEOUT);
                if(mProgressDialog != null && mProgressDialog.isShowing()) {
                    mProgressDialog.dismiss();
                    mProgressDialog = null;
                }

                String uuid = intent.getStringExtra(IRSettingDataValues.KEY_DATA_ROOM_HUB_DEVICE_UUID);
//                ArrayList<ApIRParingInfo> results = intent.getParcelableArrayListExtra(IRSettingDataValues.KEY_IR_AUTO_SCAN_RESULTS);
                int count = intent.getIntExtra(IRSettingDataValues.KEY_DATA_IR_AUTO_SCAN_COUNT,0);
                if(count == 0) {
                    Toast.makeText(mContext,R.string.ir_search_fail,Toast.LENGTH_SHORT).show();
                    return;
                }

                Intent sendIntent = new Intent(mContext, IRPairingActivity.class);
                sendIntent.putExtra(IRSettingDataValues.KEY_DATA_IR_SETTING_MODE,IRSettingDataValues.IR_SETTING_MODE_AUTO_SCAN);
                sendIntent.putExtra(IRSettingDataValues.KEY_DATA_ROOM_HUB_DEVICE_UUID, uuid);
                sendIntent.putExtra(IRSettingDataValues.KEY_ELECTRIC_UUID,mAssetUuid);
                sendIntent.putExtra(IRSettingDataValues.KEY_ELECTRIC_TYPE, mAssetType);
//                sendIntent.putParcelableArrayListExtra(IRSettingDataValues.KEY_DATA_IR_PARING_INFO, results);
                startActivityForResult(sendIntent, IRSettingDataValues.REQUEST_CODE_IR_CONFIG_DONE);
            }
        }
    };

    private void gotoFailFragment() {
        IRLearningFailFragment fragment = new IRLearningFailFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.contentFragment, fragment)
                .commit();

    }

    private void clearFragments() {
        for(int i=0;i<getSupportFragmentManager().getBackStackEntryCount();i++) {
            getSupportFragmentManager().popBackStack();
        }
    }

    public void startupIRLearning(IRController.OnSignalLearningCallback onSignalLearningCallback) {
        mIRController.learning(mUuid, mAssetUuid, onSignalLearningCallback);
        mHandler.sendEmptyMessageDelayed(MESSAGE_GET_IR_LEARNING_RESULTS_TIMEOUT,
                getResources().getInteger(R.integer.config_ir_learning_timeout));
    }

    public boolean checkIRData(IRCodeNumData irData) {
        return false;
        //return mRoomHubManager.checkIRData(mUuid, irData.getIrData());
    }

    public void finishIRLearning(IRCodeNumData irData) {
        clearFragments();
        Intent intent = new Intent();
        intent.putExtra(IRSettingDataValues.KEY_DATA_IR_CODENUM_DATA, (Parcelable) irData);
        setResult(RESULT_OK, intent);
        finish();
    }

    public void autoScan(Fragment currentFragment) {
        mProgressDialog = ProgressDialog.show(mContext, "", getString(R.string.ir_pairing_do_autoscan), true);
        mIRController.autoScan(mUuid,mAssetUuid,mAssetType);
        mHandler.sendEmptyMessageDelayed(MESSAGE_GET_IR_AUTOSCAN_RESULTS_TIMEOUT,
                getResources().getInteger(R.integer.config_ir_auto_scan_timeout));
    }

    public void doRetry(Fragment currentFragment) {
        getSupportFragmentManager().beginTransaction().remove(currentFragment).commit();
        getSupportFragmentManager().beginTransaction().add(
                R.id.contentFragment, mStartupFragment).commit();
    }

    /*
    public void setLed() {
        mRoomHubManager.setLed(mUuid, RoomHubDef.LED_COLOR_GREEN,RoomHubDef.LED_FLASH,1000,0,1);
    }

    public void blinkLed() {
        // TODO:
        // blink RED led every 0.5 secs for 10 times
    }
    */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ir_learning);

        mContext = this;
        mRoomHubManager = ((RoomHubApplication) getApplication()).getRoomHubManager();
        mRoomHubManager.registerRoomHubChange(this);
        mIRController = ((RoomHubApplication) getApplication()).getIRController();

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
        TextView txtTitle = (TextView) backView.findViewById(R.id.txtTitle);
        txtTitle.setVisibility(View.GONE);
        /*
        txtTitle.setText(getString(R.string.back));
        txtTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        RelativeLayout.LayoutParams layoutParams =
                (RelativeLayout.LayoutParams)txtTitle.getLayoutParams();
        layoutParams.addRule(RelativeLayout.RIGHT_OF, 0);
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        txtTitle.setLayoutParams(layoutParams);
        */

        mUuid = getIntent().getStringExtra(IRSettingDataValues.KEY_DATA_ROOM_HUB_DEVICE_UUID);
        mAssetType = getIntent().getIntExtra(IRSettingDataValues.KEY_ELECTRIC_TYPE, DeviceTypeConvertApi.TYPE_ROOMHUB.AC);
        mAssetUuid = getIntent().getStringExtra(IRSettingDataValues.KEY_ELECTRIC_UUID);

        if(findViewById(R.id.contentFragment) != null) {
            mStartupFragment = new IRLearningStartupFragment();
            Bundle extras = new Bundle();
            extras.putString(IRSettingDataValues.KEY_DATA_ROOM_HUB_DEVICE_UUID,mUuid);
            mStartupFragment.setArguments(extras);
            getSupportFragmentManager().beginTransaction().add(
                    R.id.contentFragment, mStartupFragment).commit();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction(IRSettingDataValues.ACTION_IR_LEARNING_RESULTS);
        filter.addAction(IRSettingDataValues.ACTION_IR_AUTO_SCAN_RESULTS);
        registerReceiver(mReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mHandler.removeMessages(MESSAGE_GET_IR_LEARNING_RESULTS_TIMEOUT);
        unregisterReceiver(mReceiver);
    }

    @Override
    protected void onDestroy() {
        mRoomHubManager.unRegisterRoomHubChange(this);
        clearFragments();
        super.onDestroy();
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);
        mCurrentFragment = fragment;
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra(IRSettingDataValues.KEY_DATA_ROOM_HUB_DEVICE_UUID,mUuid);
        intent.putExtra(IRSettingDataValues.KEY_ELECTRIC_TYPE,mAssetType);
        setResult(RESULT_CANCELED, intent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        setResult(resultCode,data);
        finish();
//        if(resultCode == RESULT_OK && requestCode == IRSettingDataValues.REQUEST_CODE_IR_CONFIG_DONE) {
//            setResult(RESULT_OK);
//            finish();
//        }
    }

    private void launchDeviceList() {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setClass(this, RoomHubMainPage.class);
        startActivity(intent);
    }

    @Override
    public void addDevice(RoomHubData data) {

    }

    @Override
    public void removeDevice(RoomHubData data) {
        if(data!=null) {
            if(data.getUuid().equals(mUuid)) {
                mHandler.sendEmptyMessage(MESSAGE_LAUNCH_DEVICE_LIST);
            }
        }
    }

    @Override
    public void UpdateRoomHubData(int type, RoomHubData data) {
        if(data != null) {
            if ((type == RoomHubManager.UPDATE_ROOMHUB_DATA)) {
                if (data.getUuid().equals(mUuid) && !data.IsOnLine()) {
                    mHandler.sendEmptyMessage(MESSAGE_LAUNCH_DEVICE_LIST);
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
        if(uuid.equals(mUuid) && (is_upgrade == true)){
            mHandler.sendEmptyMessage(MESSAGE_LAUNCH_DEVICE_LIST);
        }
    }
}
