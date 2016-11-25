package com.quantatw.roomhub.ui;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.quantatw.myapplication.R;
import com.quantatw.roomhub.ir.ApIRParingInfo;
import com.quantatw.roomhub.listener.IRParingStateChangedListener;
import com.quantatw.roomhub.manager.asset.listener.RoomHubChangeListener;
import com.quantatw.roomhub.manager.IRController;
import com.quantatw.roomhub.manager.asset.manager.RoomHubData;
import com.quantatw.roomhub.manager.asset.manager.RoomHubManager;
import com.quantatw.roomhub.ui.circleprogress.ArcProgress;
import com.quantatw.roomhub.utils.ACDef;
import com.quantatw.roomhub.utils.RoomHubDef;
import com.quantatw.sls.api.DeviceTypeConvertApi;
import com.quantatw.sls.device.CloudDevice;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by erin on 10/22/15.
 */
public class IRPairingActivity extends AbstractRoomHubActivity implements View.OnClickListener,
        IRParingStateChangedListener,
        RoomHubViewFilpper.OnViewFlipperListener,
        RoomHubChangeListener {
    private final String TAG=IRPairingActivity.class.getSimpleName();
    private RoomHubViewFilpper mViewFlipper;
    private Context mContext;
    private View mPairLayout,mProgressLayout;
    private TextView mTitleView;
    private Button mConfirmYesBtn, mConfirmNoBtn;
    private TextView mConnectText;
    private ArcProgress mConnectProgress;

    private int mIRConfigMode = IRSettingDataValues.IR_SETTING_MODE_GET_LIST;
    private String mUuid;
    private String mAssetUuid;
    private int mAssetType;

    private ArrayList<ApIRParingInfo> mParingInfos;
    private ProgressDialog mProgressDialog;
    private boolean mIsPairng = false;

    private int mTotalModelNum;
    private int mCurrentIndex = 1;

    private final int MESSAGE_IR_PARING_DONE = 100;
    private final int MESSAGE_IR_PARING_START = 101;
    private final int MESSAGE_IR_PARING_PROGRESS = 102;
    private final int MESSAGE_IR_PARING_TEST_DONE = 103;
    private final int MESSAGE_LAUNCH_DEVICE_LIST = 104;
    private final int MESSAGE_IR_PARING_DONE_DELAY = 105;

    private RoomHubManager mRoomHubMgr;

    private final String RESULT="RESULT";

    private ButtonTestAdapter buttonTestAdapter;
    private int mTestButton = 0;
    private int mTestButtonSub = 0;

    private HashMap<Integer,Boolean> mTestButtonStateMap = new HashMap<>();

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case MESSAGE_IR_PARING_DONE_DELAY: {
                    Bundle bundle = msg.getData();
                    ApIRParingInfo paringInfo = (ApIRParingInfo) bundle.getParcelable(IRSettingDataValues.KEY_DATA_IR_PARING_INFO);
                    boolean result = bundle.getBoolean(RESULT);
                    Intent intent = new Intent();
                    intent.putExtra(IRSettingDataValues.KEY_DATA_ROOM_HUB_DEVICE_UUID, paringInfo.getUuid());
                    intent.putExtra(IRSettingDataValues.KEY_ELECTRIC_TYPE, paringInfo.getAssetType());
                    //setResult(result ? RESULT_OK : RESULT_CANCELED, intent);
                    setResult(RESULT_OK, intent);
                    finish();
                }
                    break;
                case MESSAGE_IR_PARING_DONE: {
                    Bundle bundle = msg.getData();
                    ApIRParingInfo pairingInfo = (ApIRParingInfo) bundle.getParcelable(IRSettingDataValues.KEY_DATA_IR_PARING_INFO);
                    boolean result = bundle.getBoolean(RESULT);
                    mConnectText.setText(result?R.string.ir_pairing_success:R.string.ir_pairing_fail);

                    getIRController().log(TAG, "MESSAGE_IR_PARING_DONE result=" + result);
                    if (result) {
                        mRoomHubMgr.setLed(mUuid, RoomHubDef.LED_COLOR_GREEN, RoomHubDef.LED_FLASH, 500, 500, 3);
                        mConnectText.setText(R.string.ir_pairing_success);
                        Bundle sendData = new Bundle();
                        sendData.putParcelable(IRSettingDataValues.KEY_DATA_IR_PARING_INFO, pairingInfo);
                        sendData.putBoolean(RESULT, result);
                        Message delayMessage = obtainMessage(MESSAGE_IR_PARING_DONE_DELAY);
                        delayMessage.setData(sendData);
                        sendMessageDelayed(delayMessage, 3000);
                    }
                    else {
                        showPairingFailDialog(pairingInfo);
                    }
                }
                    break;
                case MESSAGE_IR_PARING_START: {
                    getIRController().log(TAG, "MESSAGE_IR_PARING_START");
                    ApIRParingInfo info = (ApIRParingInfo) msg.obj;
                    mConnectText.setText(R.string.ir_pairing_now);
                    mConnectProgress.setMax(info.getIracKeyDataList().size());
                    mRoomHubMgr.setLed(mUuid, RoomHubDef.LED_COLOR_GREEN, RoomHubDef.LED_FLASH, 1000, 1000, 0);
                }
                    break;
                case MESSAGE_IR_PARING_PROGRESS:
                    int progress = (int)msg.obj;
//                    getIRController().log(TAG, "MESSAGE_IR_PARING_PROGRESS progress=" + progress);
                    mConnectProgress.setProgress(progress);
                    break;
                case MESSAGE_IR_PARING_TEST_DONE: {
                    getIRController().log(TAG, "MESSAGE_IR_PARING_TEST_DONE");
                    ApIRParingInfo info = (ApIRParingInfo) msg.obj;

                    getIRController().log(TAG, "MESSAGE_IR_PARING_TEST_DONE testButton=" + info.getTestButton());
                    if(info.isCheckIrDataFailed()) {
                        showCheckIrDataFailDialog();
                        info.setCheckIrDataFailed(false);
                    }
                    refreshTestButtonView(info);
                }
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

    private void showCheckIrDataFailDialog() {
        final Dialog dialog = new Dialog(this,R.style.CustomDialog);

        dialog.setContentView(R.layout.custom_dialog);

        TextView txt_msg=(TextView)dialog.findViewById(R.id.txt_message);
        txt_msg.setText(getString(R.string.ir_learning_fail_wrong_data));

        Button btn_yes = (Button)dialog.findViewById(R.id.btn_yes);
        btn_yes.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        Button btn_no = (Button)dialog.findViewById(R.id.btn_no);
		btn_no.setVisibility(View.GONE);

        dialog.setCancelable(false);
        dialog.show();	  
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ir_paring_v2);
        if (android.os.Build.VERSION.SDK_INT >= 11) {

            getWindow().setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED, WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
        }

        mContext = this;
        mRoomHubMgr=getRoomHubManager();
        mRoomHubMgr.registerRoomHubChange(this);

        Bundle data = getIntent().getExtras();
        if(data != null) {
            mIRConfigMode = data.getInt(IRSettingDataValues.KEY_DATA_IR_SETTING_MODE);

            mUuid = data.getString(IRSettingDataValues.KEY_DATA_ROOM_HUB_DEVICE_UUID);
            mAssetType = data.getInt(IRSettingDataValues.KEY_ELECTRIC_TYPE);
            mAssetUuid = data.getString(IRSettingDataValues.KEY_ELECTRIC_UUID);
            if(mIRConfigMode == IRSettingDataValues.IR_SETTING_MODE_AUTO_SCAN) {
                mParingInfos = getIRController().getAutoScanResults();
                mTotalModelNum = mParingInfos.size();
            }
            else {
                mParingInfos = data.getParcelableArrayList(IRSettingDataValues.KEY_DATA_IR_PARING_INFO);
                mTotalModelNum = mParingInfos.size();
            }
        }

        View backView = findViewById(R.id.backLayout);
        /*
        ImageView imgBack = (ImageView) backView.findViewById(R.id.imgBack);
        imgBack.setOnClickListener(listener);
        */
        mTitleView = (TextView) backView.findViewById(R.id.txtTitle);
        mTitleView.setOnClickListener(listener);
        RelativeLayout.LayoutParams layoutParams =
                (RelativeLayout.LayoutParams)mTitleView.getLayoutParams();
        layoutParams.addRule(RelativeLayout.RIGHT_OF, 0);
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        mTitleView.setLayoutParams(layoutParams);

        if(mIRConfigMode == IRSettingDataValues.IR_SETTING_MODE_GET_LIST ||
                mIRConfigMode == IRSettingDataValues.IR_SETTING_MODE_SEARCH)
            mTitleView.setText(mParingInfos.get(0).getBrandName());
        else if(mIRConfigMode == IRSettingDataValues.IR_SETTING_MODE_LEARN_CODES)
            mTitleView.setText(getString(R.string.ir_learning));
        else
            mTitleView.setText(getString(R.string.ir_learning_auto_scan));

        buttonTestAdapter = new ButtonTestAdapter(this,buttonClickListener);

        mPairLayout = findViewById(R.id.ir_pair_layout);

        mConfirmYesBtn = (Button)findViewById(R.id.ir_pair_confirm_yesBtn);
        mConfirmNoBtn = (Button)findViewById(R.id.ir_pair_confirm_noBtn);
        mConfirmYesBtn.setOnClickListener(this);
        mConfirmNoBtn.setOnClickListener(this);

        mViewFlipper = (RoomHubViewFilpper) findViewById(R.id.ir_viewFlipper);
        mViewFlipper.setOnViewFlipperListener(this);
        mViewFlipper.setLongClickable(true);
        mViewFlipper.addView(createView());

        mProgressLayout = findViewById(R.id.ir_pair_progress_layout);
        mConnectText = (TextView)mProgressLayout.findViewById(R.id.connect_status_text);
        mConnectProgress = (ArcProgress)mProgressLayout.findViewById(R.id.connect_progress);

        getIRController().registerIRParingStateChangedListener(this);


    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getIRController().unregisterIRParingStateChangedListener(this);
        mRoomHubMgr.unRegisterRoomHubChange(this);
    }

    @Override
    public void onBackPressed() {
        if(mIsPairng)
            return;

        Intent intent = new Intent();
        intent.putExtra(IRSettingDataValues.KEY_DATA_ROOM_HUB_DEVICE_UUID, mUuid);
        intent.putExtra(IRSettingDataValues.KEY_ELECTRIC_TYPE, mAssetType);
        setResult(RESULT_CANCELED, intent);
        finish();
    }

    private void refreshTestButtonView(ApIRParingInfo pairingInfo) {
        if (pairingInfo.getAssetType() == DeviceTypeConvertApi.TYPE_ROOMHUB.AC){
            if(pairingInfo.getTestButton() == IRController.IR_TEST_BUTTON_POWER){
                if( buttonTestAdapter.getCount() == 1){
                    //ADD mode
                    int index = mCurrentIndex-1;
                    ApIRParingInfo currentInfo = mParingInfos.get(index);
                    if(TextUtils.isEmpty(currentInfo.getUuid())) {
                        currentInfo.setUuid(mUuid);
                    }
                    if(TextUtils.isEmpty(currentInfo.getAssetUuid()))
                        currentInfo.setAssetUuid(mAssetUuid);
                    currentInfo.setAssetType(mAssetType);
                    currentInfo.setTestButton(IRController.IR_TEST_BUTTON_MODE_COOL);
                    if (getIRController().getSupportIRACKeyData(currentInfo)!=null){
                        buttonTestAdapter.addTestButton(new ButtonEntry(IRController.IR_TEST_BUTTON_MODE_COOL, R.string.ir_pairing_mode_cooler, R.drawable.btn_cooler_selector, false));
                    }
                    if (getIRController().getTemperatureDown(currentInfo)!=null){
                        buttonTestAdapter.addTestButton(new ButtonEntry(IRController.IR_TEST_BUTTON_TEMP_INC,IRController.IR_TEST_BUTTON_TEMP_DEC, R.string.ir_pairing_temperature, R.drawable.btn_higher_s_selector, R.drawable.btn_lower_s_selector, false));
                    }
                    if(pairingInfo.getSubType() == ACDef.AC_SUBTYPE_WINDOW_TYPE) {
                        currentInfo.setTestButton(IRController.IR_TEST_BUTTON_MODE_AUTO);
                        if (getIRController().getSupportIRACKeyData(currentInfo) != null) {
                            buttonTestAdapter.addTestButton(new ButtonEntry(IRController.IR_TEST_BUTTON_MODE_AUTO, R.string.ir_pairing_mode_auto, R.drawable.btn_auto_selector, false));
                        }
                    }
                    currentInfo.setTestButton(IRController.IR_TEST_BUTTON_MODE_DRY);
                    if (getIRController().getSupportIRACKeyData(currentInfo)!=null) {
                        buttonTestAdapter.addTestButton(new ButtonEntry(IRController.IR_TEST_BUTTON_MODE_DRY, R.string.ir_pairing_mode_dry, R.drawable.btn_dry_selector, false));
                    }
                    currentInfo.setTestButton(IRController.IR_TEST_BUTTON_MODE_FAN);
                    if (getIRController().getSupportIRACKeyData(currentInfo)!=null) {
                        buttonTestAdapter.addTestButton(new ButtonEntry(IRController.IR_TEST_BUTTON_MODE_FAN, R.string.ir_pairing_mode_fan, R.drawable.btn_fan_selector, false));
                    }
                    buttonTestAdapter.notifyDataSetChanged();
                }
            }
        }
        buttonTestAdapter.updateButtonSelected(mTestButton, mTestButtonSub);
        if (mTestButton < buttonTestAdapter.getCount() - 1) {
            buttonTestAdapter.updateButtonEnable(mTestButton + 1);
        }
        findViewById(R.id.bottomLayout).setVisibility(View.VISIBLE);

    }

    private View createView() {
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View view = layoutInflater.inflate(R.layout.ir_pair_viewflipper_item_v2, null);

        view.setFocusable(true);
        view.setFocusableInTouchMode(true);

        ApIRParingInfo info = mParingInfos.get(mCurrentIndex - 1);
        if(info != null)
            getIRController().log(TAG,"<"+mCurrentIndex+"> codeNum="+info.getCodeNum());

        TextView model = (TextView)view.findViewById(R.id.ir_modelname);
        // for the fan type, show the device model name, for other type, show the remoter name
        if (mAssetType == DeviceTypeConvertApi.TYPE_ROOMHUB.FAN) {
            if ((!TextUtils.isEmpty(info.getBrandName()) && (!TextUtils.isEmpty(info.getDevModelNumber()))))
                model.setText(info.getBrandName() + "/" + info.getDevModelNumber());
            else if (TextUtils.isEmpty(info.getBrandName()) && !TextUtils.isEmpty(info.getDevModelNumber()))
                model.setText(info.getDevModelNumber());
            else if (!TextUtils.isEmpty(info.getBrandName()) && TextUtils.isEmpty(info.getDevModelNumber()))
                model.setText(info.getBrandName());
            else {
                model.setVisibility(View.GONE);
            }
        } else {
            if ((!TextUtils.isEmpty(info.getBrandName()) && (!TextUtils.isEmpty(info.getRemoteModelNum()))))
                model.setText(info.getBrandName() + "/" + info.getRemoteModelNum());
            else if (TextUtils.isEmpty(info.getBrandName()) && !TextUtils.isEmpty(info.getRemoteModelNum()))
                model.setText(info.getRemoteModelNum());
            else if (!TextUtils.isEmpty(info.getBrandName()) && TextUtils.isEmpty(info.getRemoteModelNum()))
                model.setText(info.getBrandName());
            else {
                model.setVisibility(View.GONE);
            }
        }

        // update Brand name
        if(mIRConfigMode == IRSettingDataValues.IR_SETTING_MODE_SEARCH)
            mTitleView.setText(info.getBrandName());

        //TextView deviceNumber = (TextView)view.findViewById(R.id.ir_pair_device_number);
        TextView deviceCount = (TextView)view.findViewById(R.id.ir_pair_count);
        //deviceNumber.setText(mParingInfos.get(mCurrentIndex - 1).getDevModelNumber());
        deviceCount.setText(Integer.toString(mCurrentIndex) + "/" + Integer.toString(mTotalModelNum));

//        ImageView powerBtn = (ImageView)view.findViewById(R.id.powerBtn);
//        powerBtn.setOnClickListener(this);
        ListView btnList = (ListView) view.findViewById(R.id.listView);
        buttonTestAdapter.clear();
        if (mAssetType == DeviceTypeConvertApi.TYPE_ROOMHUB.AC) {
            //AC default
            buttonTestAdapter.addTestButton(new ButtonEntry(IRController.IR_TEST_BUTTON_POWER, R.string.ir_pairing_open, R.drawable.btn_power_selector, true));
        }else if (mAssetType == DeviceTypeConvertApi.TYPE_ROOMHUB.FAN){
            buttonTestAdapter.addTestButton(new ButtonEntry(IRController.IR_TEST_BUTTON_POWER, R.string.ir_pairing_open, R.drawable.btn_power_selector, true));
        }else if (mAssetType == DeviceTypeConvertApi.TYPE_ROOMHUB.AIR_PURIFIER){
            buttonTestAdapter.addTestButton(new ButtonEntry(IRController.IR_TEST_BUTTON_POWER, R.string.ir_pairing_open, R.drawable.btn_power_selector, true));
            buttonTestAdapter.addTestButton(new ButtonEntry(IRController.IR_TEST_BUTTON_WIND, R.string.ir_pairing_wind_speed, R.drawable.btn_windspeed_selector, false));
            buttonTestAdapter.addTestButton(new ButtonEntry(IRController.IR_TEST_BUTTON_TIMER, R.string.ir_pairing_Timer, R.drawable.btn_timer_selector, false));
        }else if (mAssetType == DeviceTypeConvertApi.TYPE_ROOMHUB.TV){
            buttonTestAdapter.addTestButton(new ButtonEntry(IRController.IR_TEST_BUTTON_TV_POWER, R.string.ir_pairing_open, R.drawable.btn_power_selector, true));
            buttonTestAdapter.addTestButton(new ButtonEntry(IRController.IR_TEST_BUTTON_VOLUME_INC,IRController.IR_TEST_BUTTON_VOLUME_DEC, R.string.ir_pairing_volume, R.drawable.btn_higher_s_selector, R.drawable.btn_lower_s_selector, false));
            buttonTestAdapter.addTestButton(new ButtonEntry(IRController.IR_TEST_BUTTON_CHANNEL_INC,IRController.IR_TEST_BUTTON_CHANNEL_DEC, R.string.ir_pairing_channel, R.drawable.btn_higher_s_selector, R.drawable.btn_lower_s_selector, false));
            buttonTestAdapter.addTestButton(new ButtonEntry(IRController.IR_TEST_BUTTON_MUTE, R.string.ir_pairing_mute, R.drawable.btn_mute_selector, false));
            buttonTestAdapter.addTestButton(new ButtonEntry(IRController.IR_TEST_BUTTON_MENU, R.string.ir_pairing_menu, R.drawable.btn_menu_selector, false));
            buttonTestAdapter.addTestButton(new ButtonEntry(IRController.IR_TEST_BUTTON_DIGIT_1, R.string.ir_pairing_digit_1, R.drawable.btn_number_1_selector, false));
        }
        btnList.setAdapter(buttonTestAdapter);

        findViewById(R.id.bottomLayout).setVisibility(View.GONE);

        ImageView rightBtn = (ImageView) view.findViewById(R.id.rightBtn);
        ImageView leftBtn = (ImageView) view.findViewById(R.id.leftBtn);
        rightBtn.setOnClickListener(this);
        leftBtn.setOnClickListener(this);
        if(mTotalModelNum > 1) {
            mViewFlipper.setOnViewFlipperListener(this);
            if(mCurrentIndex == 1) {
                rightBtn.setVisibility(View.VISIBLE);
                leftBtn.setVisibility(View.INVISIBLE);
            }
            else if(mCurrentIndex == mTotalModelNum) {
                rightBtn.setVisibility(View.INVISIBLE);
                leftBtn.setVisibility(View.VISIBLE);
            }
            else {
                rightBtn.setVisibility(View.VISIBLE);
                leftBtn.setVisibility(View.VISIBLE);
            }
        }
        else {
            mViewFlipper.setOnViewFlipperListener(null);
            rightBtn.setVisibility(View.INVISIBLE);
            leftBtn.setVisibility(View.INVISIBLE);
        }


        return view;
    }

    private ButtonTestAdapter.ButtonClickListener buttonClickListener = new ButtonTestAdapter.ButtonClickListener() {
        @Override
        public void onButtonClick(int position, int id) {
            mTestButton = position;
            if (id == R.id.btn1) {
                mTestButtonSub = 0;
                doTestButton(buttonTestAdapter.getItem(position).testButton1Key);
            } else if (id == R.id.btn2) {
                mTestButtonSub = 1;
                doTestButton(buttonTestAdapter.getItem(position).testButton2Key);
            }
        }
    };

    @Override
    public void onClick(View v) {
        if(v instanceof Button) {
            mViewFlipper.setOnViewFlipperListener(this);
            switch (v.getId()) {
                case R.id.ir_pair_confirm_yesBtn:
                case R.id.ir_pair_confirm_noBtn:
                    if(v.getId()==R.id.ir_pair_confirm_yesBtn) {
                        showConfirmDialog();
                    }
                    else {  // NO:
                        if((mTotalModelNum > 1) && (mCurrentIndex < mTotalModelNum))
                            mViewFlipper.flingToNext();
                    }
                    if(v.getId()==R.id.ir_pair_confirm_noBtn && mTotalModelNum==1)
                        onBackPressed();
                    break;
            }
            /*
            if(v.getId()==R.id.ir_pair_confirm_yesBtn) {
                doIRParingStart();
            }
            else {  // NO:
                if((mTotalModelNum > 1) && (mCurrentIndex < mTotalModelNum))
                    mViewFlipper.flingToNext();
            }
            mConfirmLayout.setVisibility(View.GONE);
            */
        }
        else if(v instanceof ImageView) {
            if(v.getId()==R.id.rightBtn) {
                mViewFlipper.flingToNext();
            }
            else if(v.getId()==R.id.leftBtn) {
                mViewFlipper.flingToPrevious();
            }
        }
    }

    private void doTestButton(int button) {
        mRoomHubMgr.setLed(mUuid, RoomHubDef.LED_COLOR_GREEN, RoomHubDef.LED_FLASH, 1000, 0, 1);
        doIRParingTest(button);
    }

    private void doIRParingStart() {
        int index = mCurrentIndex-1;
        final ApIRParingInfo currentInfo = mParingInfos.get(index);
        Log.v(TAG,"=========doIRParingStart");
        mIsPairng = true;
        mPairLayout.setVisibility(View.GONE);
        mProgressLayout.setVisibility(View.VISIBLE);
        getIRController().irParingStart(currentInfo);
    }

    private void doIRParingTest(int testButton) {
        int index = mCurrentIndex-1;
        ApIRParingInfo currentInfo = mParingInfos.get(index);

        showProgressDialog("", getString(R.string.loading));
        if(TextUtils.isEmpty(currentInfo.getUuid())) {
            currentInfo.setUuid(mUuid);
        }
        if(TextUtils.isEmpty(currentInfo.getAssetUuid()))
            currentInfo.setAssetUuid(mAssetUuid);
        currentInfo.setAssetType(mAssetType);
        currentInfo.setTestButton(testButton);
        getIRController().irParingTest(currentInfo);

    }

    private View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            finish();
        }
    };

    @Override
//    public void onPairingTest(String uuid, String brand, String model) {
    public void onPairingTest(ApIRParingInfo currentTarget) {
        dismissProgressDialog();
        mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_IR_PARING_TEST_DONE, currentTarget));
    }

    @Override
    public void onPairingStart(ApIRParingInfo currentTarget) {
//        dismissProgressDialog();
        mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_IR_PARING_START, currentTarget));
    }

    @Override
    public void onPairingProgress(ApIRParingInfo currentInfo, int handleCount) {
        if(currentInfo.getIracKeyDataList() == null)
            return;
        mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_IR_PARING_PROGRESS, handleCount));
    }

    @Override
    public void onPairingDone(ApIRParingInfo currentTarget, boolean result) {
        Message message = mHandler.obtainMessage(MESSAGE_IR_PARING_DONE);
        Bundle bundle = new Bundle();
        bundle.putParcelable(IRSettingDataValues.KEY_DATA_IR_PARING_INFO, currentTarget);
        bundle.putBoolean(RESULT, result);
        message.setData(bundle);
        mHandler.sendMessage(message);
    }

    @Override
    public View getNextView() {
        //mCurrentIndex = mCurrentIndex == mTotalModelNum ? 1 : mCurrentIndex + 1;
        if(mCurrentIndex >= mTotalModelNum)
            return null;

        mCurrentIndex = mCurrentIndex + 1;
        return createView();
    }

    @Override
    public View getPreviousView() {
        //mCurrentIndex = mCurrentIndex == 1 ?  mTotalModelNum : mCurrentIndex - 1;
        if(mCurrentIndex <= 1)
            return null;

        mCurrentIndex = mCurrentIndex - 1;
        return createView();
    }

    @Override
    public void addDevice(RoomHubData data) {

    }

    @Override
    public void removeDevice(RoomHubData data) {
        if(data!=null) {
            if(data.getUuid().equals(mUuid) && !getIRController().isIROnParing()) {
                mHandler.sendEmptyMessage(MESSAGE_LAUNCH_DEVICE_LIST);
            }
        }
    }

    @Override
    public void UpdateRoomHubData(int type, RoomHubData data) {
        if(data != null) {
            if ((type == RoomHubManager.UPDATE_ROOMHUB_DATA)) {
                if (data.getUuid().equals(mUuid) && !data.IsOnLine()
                        && !getIRController().isIROnParing()) {
                    mHandler.sendEmptyMessage(MESSAGE_LAUNCH_DEVICE_LIST);
                }
            }
        }
    }

    @Override
    public void UpdateDeviceShareUser(CloudDevice device) {

    }

    @Override
    public void UpgradeStatus(String uuid, boolean is_upgrade) {
        if(uuid.equals(mUuid) && (is_upgrade == true)
                && !getIRController().isIROnParing()){
            mHandler.sendEmptyMessage(MESSAGE_LAUNCH_DEVICE_LIST);
        }
    }
    private void showPairingFailDialog(final ApIRParingInfo paringInfo) {
        final Dialog dialog = new Dialog(this,R.style.CustomDialog);

        dialog.setContentView(R.layout.custom_dialog);

        TextView txt_msg=(TextView)dialog.findViewById(R.id.txt_message);
        txt_msg.setText(getString(R.string.ir_pairing_fail));

        Button btn_yes = (Button)dialog.findViewById(R.id.btn_yes);
        btn_yes.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Bundle sendData = new Bundle();
                sendData.putParcelable(IRSettingDataValues.KEY_DATA_IR_PARING_INFO, paringInfo);
                sendData.putBoolean(RESULT, false);
                Message delayMessage = mHandler.obtainMessage(MESSAGE_IR_PARING_DONE_DELAY);
                delayMessage.setData(sendData);
                mHandler.sendMessage(delayMessage);
                dialog.dismiss();
            }
        });

        Button btn_no = (Button)dialog.findViewById(R.id.btn_no);
        btn_no.setVisibility(View.GONE);

        dialog.setCancelable(false);
        dialog.show();
    }

    private void showConfirmDialog() {
        final Dialog dialog = new Dialog(this,R.style.CustomDialog);
        dialog.setContentView(R.layout.custom_dialog_center);
        dialog.setCancelable(false);
        dialog.setTitle("");
        TextView txt_msg=(TextView)dialog.findViewById(R.id.txt_message);
        Button btn_yes = (Button)dialog.findViewById(R.id.btn_yes);
        btn_yes.setVisibility(View.VISIBLE);
        Button btn_no = (Button)dialog.findViewById(R.id.btn_no);
        btn_no.setVisibility(View.VISIBLE);
        txt_msg.setText(R.string.ir_pairing_confirm_warning);
        btn_yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doIRParingStart();
                dialog.dismiss();
            }
        });
        btn_no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

}
