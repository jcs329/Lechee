package com.quantatw.roomhub.ui;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.quantatw.myapplication.R;
import com.quantatw.roomhub.manager.asset.listener.RoomHubChangeListener;
import com.quantatw.roomhub.manager.AccountManager;
import com.quantatw.roomhub.manager.OTAManager;
import com.quantatw.roomhub.manager.asset.manager.RoomHubData;
import com.quantatw.roomhub.manager.asset.manager.RoomHubManager;
import com.quantatw.roomhub.utils.GlobalDef;
import com.quantatw.roomhub.utils.PreferenceEditor;
import com.quantatw.roomhub.utils.SupportVersion;
import com.quantatw.roomhub.utils.Utils;
import com.quantatw.sls.device.CloudDevice;
import com.quantatw.sls.key.ErrorKey;
import com.quantatw.sls.pack.homeAppliance.AutoSwitchWifiBridgeReqPack;
import com.quantatw.sls.pack.homeAppliance.AutoSwitchWifiBridgeResPack;
import com.quantatw.sls.pack.homeAppliance.BaseHomeApplianceResPack;
import com.quantatw.sls.pack.homeAppliance.OnboardingReqPack;
import com.quantatw.sls.pack.homeAppliance.OnboardingResPack;

public class AboutActivity extends AbstractRoomHubActivity implements View.OnClickListener,RoomHubChangeListener {
    private static final String TAG = "AboutActivity";
    private static boolean DEBUG=true;
    private TextView mTxtName;
    private TextView mTxtVersion;
    private AccountManager mAccountMgr;
    private RoomHubManager mRoomHubMgr;
    private Context mContext;

    private RelativeLayout mlayoutChangeWifiSetup;
    private TextView mTxtSSID;
    private int mSupportWIFI = 0; //0:need check, 1:support, -1:not support

    private Button mBtnRename;
    private Button mBtnDel,mBtnCheckNow;
    private Button mBtnReboot;
    private ImageView mAutoWifiBridge;
    private RoomHubData mData;
    private String mCurUuid;
    private boolean mShow;
    private boolean mAutoWifiBridgeState;
    private ProgressDialog mProgressDialog;

    private HandlerThread mBackgroundThread;
    private BackgroundHandler mBackgroundHandler;

    private final int MESSAGE_UPDATE_ROOMHUB_DATA = 100;
    private final int MESSAGE_REFRESH_WIFI_BRIDGE_STATE = 200;
    private final int MESSAGE_TIMEOUT = 300;
    private final int MESSAGE_TOAST = 400;
    private final int MESSAGE_TOAST2 = 401;
    private final int MESSAGE_GOTO_SETWIFI = 402;


    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Log.d(TAG,"message what="+msg.what);
            switch(msg.what) {
                case MESSAGE_UPDATE_ROOMHUB_DATA:
                    RoomHubData data=(RoomHubData)msg.obj;
                    if(data != null && data.getUuid().equals(mCurUuid)) {
                        int type=msg.arg1;
                        if(type == RoomHubManager.UPDATE_ONLINE_STATUS){
                            if(!data.IsOnLine())
                                finish();
                        }else if(type == RoomHubManager.UPDATE_ROOMHUB_NAME){
                            mTxtName.setText(data.getName());
                        }
                        mData=data;
                        mTxtVersion.setText(mData.getVersion());
                    }

                    break;
                case MESSAGE_REFRESH_WIFI_BRIDGE_STATE:
                    boolean onoff = (boolean)msg.obj;
                    mAutoWifiBridgeState = onoff;
                    refreshWifiBridgeState(onoff);
                    break;
                case MESSAGE_TIMEOUT:
                    showProgressDialog(false);
                    Toast.makeText(mContext,R.string.control_timeout,Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_TOAST:
                    Toast.makeText(mContext,R.string.wifi_bridge_not_support,Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_TOAST2:
                    Toast.makeText(mContext,R.string.change_wifi_setup_not_support,Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_GOTO_SETWIFI:
                    changeWIFI();
                    break;
            }
        }
    };

    private final int MESSAGE_GET_WIFI_BRIDGE_STATE = 500;
    private final int MESSAGE_SWITCH_WIFI_BRIDGE_STATE = 600;
    private final int MESSAGE_CHECK_SUPPORT_CHANGE_WIFI = 700;

    private final class BackgroundHandler extends Handler {
        public BackgroundHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case MESSAGE_GET_WIFI_BRIDGE_STATE: {
                    showProgressDialog(true);
                    mHandler.sendEmptyMessageDelayed(MESSAGE_TIMEOUT, 20000);
                    AutoSwitchWifiBridgeResPack resPack = mData.getRoomHubDevice().getWifiBridgeState();
                    if (resPack != null && resPack.getStatus_code() == ErrorKey.Success) {
                        boolean onoff = resPack.getEnable() == 1 ? true : false;
                        mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_REFRESH_WIFI_BRIDGE_STATE, onoff));
                    }
                    mHandler.removeMessages(MESSAGE_TIMEOUT);
                    showProgressDialog(false);
                }
                    break;
                case MESSAGE_SWITCH_WIFI_BRIDGE_STATE: {
                    boolean enable = mAutoWifiBridgeState==true?false:true;
                    showProgressDialog(true);
                    mHandler.sendEmptyMessageDelayed(MESSAGE_TIMEOUT, 20000);
                    AutoSwitchWifiBridgeReqPack reqPack = new AutoSwitchWifiBridgeReqPack();
                    reqPack.setEnable(enable == true ? 1 : 0);
                    BaseHomeApplianceResPack resPack = mData.getRoomHubDevice().switchAutoWifiBridge(reqPack);
                    if(resPack != null) {
                        if(resPack.getStatus_code() == ErrorKey.Success) {
                            sendEmptyMessage(MESSAGE_GET_WIFI_BRIDGE_STATE);
                        }
                        else if(resPack.getStatus_code() == ErrorKey.AUTO_WIFI_BRIDGE_FAILURE)
                            mHandler.sendEmptyMessage(MESSAGE_TOAST);
                    }
                    mHandler.removeMessages(MESSAGE_TIMEOUT);
                    showProgressDialog(false);
                }
                    break;
                case MESSAGE_CHECK_SUPPORT_CHANGE_WIFI:
                    showProgressDialog(true);
                    mHandler.sendEmptyMessageDelayed(MESSAGE_TIMEOUT, 20000);
                    OnboardingReqPack reqPack = new OnboardingReqPack();
                    reqPack.setSsid(null);
                    reqPack.setPassword(null);
                    OnboardingResPack resPack = mData.getRoomHubDevice().setOnboarding(reqPack);
                    if(resPack != null) {
                        if(resPack.getStatus_code() == ErrorKey.Success) {
                            mSupportWIFI = resPack.getStatus_code();
                            mHandler.sendEmptyMessage(MESSAGE_GOTO_SETWIFI);
                        }
                        else if(resPack.getStatus_code() == ErrorKey.ONBOARDING_FAILURE) {
                            mSupportWIFI = resPack.getStatus_code();
                            mHandler.sendEmptyMessage(MESSAGE_TOAST2);
                        }
                    }
                    mHandler.removeMessages(MESSAGE_TIMEOUT);
                    showProgressDialog(false);

                    break;
            }
            super.handleMessage(msg);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.room_hub_about);

        mContext = this;
        mAccountMgr=getAccountManager();
        mRoomHubMgr=getRoomHubManager();
        mRoomHubMgr.registerRoomHubChange(this);

        mBackgroundThread=new HandlerThread("AboutActivity");
        mBackgroundThread.start();
        mBackgroundHandler = new BackgroundHandler(mBackgroundThread.getLooper());

    }

    @Override
    protected void onResume() {
        super.onResume();

        mShow = true;
        mCurUuid=getIntent().getStringExtra(RoomHubManager.KEY_UUID);

        mData=mRoomHubMgr.getRoomHubDataByUuid(mCurUuid);

        mTxtName=(TextView)findViewById(R.id.txt_dev_name);
        mTxtName.setText(mData.getName());

        String str_version=mData.getVersion();
        mTxtVersion=(TextView)findViewById(R.id.txt_version);
        mTxtVersion.setText(str_version);

        if(DEBUG)
            Log.d(TAG,"onResume version="+str_version);

        mlayoutChangeWifiSetup =(RelativeLayout) findViewById(R.id.layout_change_wifi);
        mlayoutChangeWifiSetup.setOnClickListener(this);
        //TODO use cloud to retrieve ssid
        mTxtSSID = (TextView) findViewById(R.id.txt_ssid);
        PreferenceEditor pref = new PreferenceEditor(this, GlobalDef.ROOMHUB_SETTINGS_PREFERENCE_NAME);
        mTxtSSID.setText(pref.getStringValue(GlobalDef.ROOMHUB_SETTINGS_WIFI_SSID));

        mBtnRename=(Button) findViewById(R.id.btn_rename);
        mBtnRename.setOnClickListener(this);

        mBtnDel=(Button)findViewById(R.id.btn_delete);
        mBtnDel.setOnClickListener(this);

        mBtnCheckNow = (Button)findViewById(R.id.btn_checkNewVersion);
        mBtnCheckNow.setOnClickListener(this);

        mAutoWifiBridge = (ImageView)findViewById(R.id.auto_wifi_bridge_switch);
        mAutoWifiBridge.setBackgroundResource(R.drawable.switch_off);
        mAutoWifiBridge.setOnClickListener(this);

        mBackgroundHandler.sendEmptyMessage(MESSAGE_GET_WIFI_BRIDGE_STATE);

        mBtnReboot = (Button)findViewById(R.id.btn_reboot);
        mBtnReboot.setOnClickListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mShow = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRoomHubMgr.unRegisterRoomHubChange(this);

        if(mBackgroundThread != null)
            mBackgroundThread.quit();
    }


    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.btn_delete){
            DeleteDevice();
        }
        else if(v.getId() == R.id.btn_checkNewVersion) {
            OTAManager.OTADeviceLackCapability otaDeviceLackCapability =
                    getOTAManager().checkDevicCapability(mCurUuid);
            Log.d(TAG,"OTA checkDevicCapability="+otaDeviceLackCapability.getValues());
            if(otaDeviceLackCapability == OTAManager.OTADeviceLackCapability.NONE ||
                    otaDeviceLackCapability == OTAManager.OTADeviceLackCapability.UPGRADE) {
                Intent intent = new Intent(this, OTAActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString(OTAManager.OTA_DEVICE_UUID, mCurUuid);
                intent.putExtras(bundle);
                startActivity(intent);
            }
            else if(otaDeviceLackCapability.getValues() ==
                    OTAManager.OTADeviceLackCapability.LOGIN.getValues()) {
                Utils.ShowLoginActivity(this, RoomHubMainPage.class);
            }
            else if(otaDeviceLackCapability.getValues() ==
                    OTAManager.OTADeviceLackCapability.OWNER.getValues()) {
                Toast.makeText(this,otaDeviceLackCapability.getStringResourceId(),Toast.LENGTH_SHORT).show();
            }
            else if(otaDeviceLackCapability.getValues() ==
                    OTAManager.OTADeviceLackCapability.ALLJOYN.getValues()) {
                //leak alljoyn but support remote ota
                if(mContext.getResources().getBoolean(R.bool.config_ota_support_remote)){
                    if(SupportVersion.OTAVer.isValid(mData.getVersion())) {
//                    if (Utils.checkFirmwareVersion(mData.getVersion(),"1.1.16.3",false)){
                        if (mData.IsOnLine() ) {
                            Intent intent = new Intent(this, OTAActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putString(OTAManager.OTA_DEVICE_UUID, mCurUuid);
                            intent.putExtras(bundle);
                            startActivity(intent);
                        }else{
                            Toast.makeText(this, getString(R.string.device_offline), Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        Toast.makeText(this, getString(R.string.ota_device_not_support_remote_ota), Toast.LENGTH_SHORT).show();
                    }

                }else {
                    Toast.makeText(this, otaDeviceLackCapability.getStringResourceId(), Toast.LENGTH_SHORT).show();
                }
            }
        }else if(v.getId() == R.id.btn_rename){
            LaunchReName();
        }
        else if(v.getId() == R.id.auto_wifi_bridge_switch) {
            if(!mData.IsAlljoyn())
                Toast.makeText(this,R.string.roomhub_warning_msg,Toast.LENGTH_SHORT).show();
            else if (!mData.IsOnLine() )
                Toast.makeText(this, getString(R.string.device_offline), Toast.LENGTH_SHORT).show();
            else if(mData.IsUpgrade())
                Toast.makeText(this, getString(R.string.device_upgrade), Toast.LENGTH_SHORT).show();
            else
                showSwitchWifiBridgeDialog();
        }
        else if(v.getId() == R.id.layout_change_wifi) {

            if(!mData.IsAlljoyn()) {
                Toast.makeText(this, R.string.roomhub_warning_msg, Toast.LENGTH_SHORT).show();
            }else if(mSupportWIFI == ErrorKey.Success){
                mBackgroundHandler.sendEmptyMessage(MESSAGE_CHECK_SUPPORT_CHANGE_WIFI);
            }else if(mSupportWIFI == ErrorKey.ONBOARDING_FAILURE){
                Toast.makeText(this,R.string.change_wifi_setup_not_support,Toast.LENGTH_SHORT).show();
            }else{
                changeWIFI();
            }
        }else if(v.getId() == R.id.btn_reboot){
            RebootRoomHubDialog();
        }
    }

    private void showSwitchWifiBridgeDialog() {
        // show dialog when switch ON
        if(mAutoWifiBridgeState == false) {
            final Dialog dialog = new Dialog(this, R.style.CustomDialog);
            dialog.setContentView(R.layout.custom_dialog);
            TextView txt_msg = (TextView) dialog.findViewById(R.id.txt_message);
            txt_msg.setText(getString(R.string.wifi_bridge_confirm_msg));

            Button btn_yes = (Button) dialog.findViewById(R.id.btn_yes);
            btn_yes.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    mBackgroundHandler.sendEmptyMessage(MESSAGE_SWITCH_WIFI_BRIDGE_STATE);
                }
            });

            Button btn_no = (Button) dialog.findViewById(R.id.btn_no);
            btn_no.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });

            dialog.setCancelable(false);
            dialog.show();
        }
        else {
            mBackgroundHandler.sendEmptyMessage(MESSAGE_SWITCH_WIFI_BRIDGE_STATE);
        }
    }

    private void showProgressDialog(boolean show) {
        if(show) {
            if(mProgressDialog!=null && mProgressDialog.isShowing())
                return;
            mProgressDialog = ProgressDialog.show(this, "", getString(R.string.process_str), true);
        }
        else {
            if(mProgressDialog != null)
                mProgressDialog.dismiss();
        }
    }

    private void refreshWifiBridgeState(boolean onoff) {
        mAutoWifiBridge.setBackgroundResource(onoff == true ? R.drawable.switch_on : R.drawable.switch_off);
    }

    private void changeWIFI(){
        Intent intent = new Intent(this, ChangeWIFIActivity.class);
        intent.putExtra(RoomHubManager.KEY_UUID, mCurUuid);
        startActivity(intent);
    }

    private void LaunchReName(){
        if (!mData.IsOnLine() ) {
            Toast.makeText(this, getString(R.string.device_offline), Toast.LENGTH_SHORT).show();
            return;
        }

        if(mData.IsUpgrade()){
            Toast.makeText(this, getString(R.string.device_upgrade), Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, RenameActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(RoomHubManager.KEY_UUID, mCurUuid);
        bundle.putString(RoomHubManager.KEY_DEV_NAME, mData.getName());

        intent.putExtras(bundle);

        startActivity(intent);
    }

    public void DeleteDevice(){
        if(mAccountMgr.isLogin()){
        //    String uuid=mData.getUuid();
            if(!mData.IsOwner()){
                Toast.makeText(this, R.string.only_owner_use, Toast.LENGTH_SHORT).show();
            }else{
                DeleteDeviceDialog();
            }
/*
            if(!mRoomHubMgr.IsCloud(uuid)) {
                //if(mAccountMgr.getUserId().equals(data.getOwnerId()))
                if(mData.IsOwner())
                    DeleteDeviceDialog();
                else
                    Toast.makeText(this, R.string.only_owner_use, Toast.LENGTH_SHORT).show();
            }else {
                if(mRoomHubMgr.IsAlljoyn(uuid))
                    DeleteDeviceDialog();
                else
                    Toast.makeText(this, R.string.roomhub_warning_msg, Toast.LENGTH_SHORT).show();
            }
*/
        }else{
            Utils.ShowLoginActivity(this, RoomHubMainPage.class);
        }
    }

    private void DeleteDeviceDialog(){
        final Dialog dialog = new Dialog(this,R.style.CustomDialog);

        dialog.setContentView(R.layout.custom_delete_dialog);

        TextView txt_msg=(TextView)dialog.findViewById(R.id.txt_message);
        if(!mData.IsAlljoyn() && mData.IsCloud()){
            txt_msg.setGravity(Gravity.CENTER_VERTICAL);
            txt_msg.setText(getString(R.string.cannot_find_roomhub_confirm_delete));
        }else
            txt_msg.setText(getString(R.string.confirm_delete_device));


        Button btn_no = (Button)dialog.findViewById(R.id.btn_no);
        btn_no.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        Button btn_yes = (Button)dialog.findViewById(R.id.btn_yes);
        btn_yes.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();
                showProgressDialog("", getString(R.string.processing_str));
                Thread thread = new Thread() {
                    @Override
                    public void run() {
                        mRoomHubMgr.DeleteDevice(mCurUuid);
                    }
                };
                thread.start();
            }
        });
        dialog.show();
    }

    private void RebootRoomHubDialog(){
        if (!mData.IsOnLine() ) {
            Toast.makeText(this, getString(R.string.device_offline), Toast.LENGTH_SHORT).show();
            return;
        }

        if(mData.IsUpgrade()){
            Toast.makeText(this, getString(R.string.device_upgrade), Toast.LENGTH_SHORT).show();
            return;
        }

        final Dialog dialog = new Dialog(this,R.style.CustomDialog);

        dialog.setContentView(R.layout.custom_dialog);

        TextView txt_msg=(TextView)dialog.findViewById(R.id.txt_message);
        txt_msg.setText(getString(R.string.reboot_confirm_msg));

        Button btn_yes = (Button)dialog.findViewById(R.id.btn_yes);
        btn_yes.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();
                mRoomHubMgr.RebootRoomHub(mCurUuid);
                finish();
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
    public void addDevice(RoomHubData data) {

    }

    @Override
    public void removeDevice(RoomHubData data) {
        dismissProgressDialog();
        if(data!=null) {
            if(data.getUuid().equals(mCurUuid)) {
                finish();
            }
        }
    }

    @Override
    public void UpdateRoomHubData(int type, RoomHubData data) {
        if(data != null) {
            mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_UPDATE_ROOMHUB_DATA,type,0,data));
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
