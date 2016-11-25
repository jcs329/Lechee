package com.quantatw.roomhub.ui;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Parcelable;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.quantatw.myapplication.BuildConfig;
import com.quantatw.myapplication.R;
import com.quantatw.roomhub.blepair.BLEPairController;
import com.quantatw.roomhub.manager.asset.manager.ACNoticeManager;
import com.quantatw.roomhub.manager.AccountManager;
import com.quantatw.roomhub.manager.IRController;
import com.quantatw.roomhub.manager.OTAManager;
import com.quantatw.roomhub.manager.OnBoardingManager;
import com.quantatw.roomhub.manager.RoomHubDBHelper;
import com.quantatw.roomhub.manager.asset.manager.AssetInfoData;
import com.quantatw.roomhub.manager.asset.manager.BaseAssetData;
import com.quantatw.roomhub.manager.asset.manager.BaseAssetManager;
import com.quantatw.roomhub.manager.asset.manager.RoomHubData;
import com.quantatw.roomhub.manager.asset.manager.RoomHubManager;
import com.quantatw.roomhub.manager.health.manager.HealthDeviceManager;
import com.quantatw.roomhub.utils.ACDef;
import com.quantatw.roomhub.utils.AssetDef;
import com.quantatw.roomhub.utils.GlobalDef;
import com.quantatw.roomhub.utils.PreferenceEditor;
import com.quantatw.roomhub.utils.Utils;
import com.quantatw.sls.api.DeviceTypeConvertApi;
import com.quantatw.sls.key.ErrorKey;
import com.quantatw.sls.pack.roomhub.VersionCheckUpdateResPack;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;

public class AbstractRoomHubActivity extends FragmentActivity implements ActivityInterface {
    private final String TAG=AbstractRoomHubActivity.class.getSimpleName();
    private ProgressDialog mProgressDialog;
    private Bundle mMsgData;
    private int mShowTime, mResult;
    private boolean mIsMenu=false;
    private PopupMenu menu_settings=null;
    private Context mContext;
    private int menuRes;
    private HandlerThread mBackgroundThread;
    private BackgroundHandler mBackgroundHandler;
    private Fragment mCurrentFragment;
    private int mTabChoose;

    private final int MESSAGE_CHECK_APP_VERSION = 100;
    private final int MESSAGE_CHECK_APP_VERSION_DONE = 200;
    private final int MESSAGE_CHECK_APP_VERSION_TIMEOUT = 300;
    private final int MESSAGE_SHOW_APP_UPDATE_DIALOG = 400;

    private final int MESSAGE_REDIRECT_URL = 1000;

    protected static final int TAB_APPLIANCE = 1;
    protected static final int TAB_HEALTH = 2;

    private boolean mCheckVersionDone = false;
    private Message mReplyStartupMessage;
    private Message mReplyTimeoutMessage;

    /* app version update flags */
    private final int APP_NEED_UPDATE = 0x1;
    private final int APP_UPDATE_FORCE = 0x2;

    private String mAppCustomRedirectUrl;

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            String errString;
            switch (msg.what) {
                case DIALOG_SHOW_NOTHING:
                    break;
                case DIALOG_SHOW_LOGIN_SUCCESS:
                    break;
                case DIALOG_SHOW_LOGING_FAIL:
                case DIALOG_SHOW_SIGNUP_LOGIN_FAIL:
                    errString = Utils.getErrorCodeString(AbstractRoomHubActivity.this, msg.getData().getInt("err_code"));
                    Toast.makeText(getApplicationContext(), errString, Toast.LENGTH_SHORT).show();
                    break;
                case DIALOG_SHOW_SIGNUP_FAIL:
                    errString = Utils.getErrorCodeString(AbstractRoomHubActivity.this, msg.getData().getInt("err_code"));
                    Toast.makeText(getApplicationContext(), errString, Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_SHOW_APP_UPDATE_DIALOG:
                    showUpdateAppDialog((int) msg.obj);
                    break;
                case MESSAGE_CHECK_APP_VERSION_TIMEOUT:
                    Log.d(TAG,"MESSAGE_CHECK_APP_VERSION_TIMEOUT continue startup");
                    mCheckVersionDone = true;
                    if(mReplyTimeoutMessage != null) {
                        mReplyTimeoutMessage.sendToTarget();
                        mReplyTimeoutMessage = null;
                    }
                    mReplyStartupMessage = null;
                    break;
            }
            super.handleMessage(msg);
        }
    };

    private final class BackgroundHandler extends Handler {
        public BackgroundHandler (Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_CHECK_APP_VERSION:
                    checkVersion();
                    break;
                case MESSAGE_CHECK_APP_VERSION_DONE:
                    mHandler.removeMessages(MESSAGE_CHECK_APP_VERSION_TIMEOUT);
                    mCheckVersionDone = true;
                    int forceUpdate = (int)msg.obj;
                    if(forceUpdate > 0)
                        mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_SHOW_APP_UPDATE_DIALOG, forceUpdate));
                    else {
                        if(mReplyStartupMessage != null)
                            mReplyStartupMessage.sendToTarget();
                    }

                    break;
                case MESSAGE_REDIRECT_URL:
                    Log.d(TAG,"MESSAGE_REDIRECT_URL");
                    if(Utils.isRoomHubAppForeground(mContext)) {
                        String url = (String) msg.obj;
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    }
                    break;
            }
        }
    }

    private boolean checkVersion() {
        int forceUpdate = isAppForceUpdate();
        /*
        try {
            Thread.sleep(15000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        */
        mBackgroundHandler.sendMessage(mBackgroundHandler.obtainMessage(MESSAGE_CHECK_APP_VERSION_DONE,forceUpdate));
        return (forceUpdate>0?true:false);
    }

    private int isAppForceUpdate() {
        if(getResources().getBoolean(R.bool.config_skip_app_check_version) == true)
            return 0;

        VersionCheckUpdateResPack versionCheckUpdateResPack =
                ((RoomHubApplication)getApplication()).checkAppVersion();
        int forceUpdate = 0;
        if(versionCheckUpdateResPack.getStatus_code() == ErrorKey.Success &&
                versionCheckUpdateResPack.getData() != null) {
            String version = versionCheckUpdateResPack.getData().getVersion();
            String md5 = versionCheckUpdateResPack.getData().getMd5();
            String url = versionCheckUpdateResPack.getData().getUrl();
            String current_version = BuildConfig.VERSION_NAME;
            if(BuildConfig.BUILD_TYPE.equals("debug")) {
                int pos = BuildConfig.VERSION_NAME.indexOf(' ');
                if(pos > 0)
                    current_version = BuildConfig.VERSION_NAME.substring(0,pos);
            }

//            //test
//            current_version = "1.1.1.0";
//            //test
//            url = "https://play.google.com/store/apps/details?id=com.ccasd.cmp";

            if(Utils.checkFirmwareVersion(version, current_version,true) == false) {
                return forceUpdate; // no need to update
            }

            forceUpdate |= APP_NEED_UPDATE; // need update

            if(!url.equalsIgnoreCase(getString(R.string.config_app_update_url)))
                mAppCustomRedirectUrl = url;

            String latest_version = current_version;
            if(!TextUtils.isEmpty(md5)) {
                latest_version = md5.substring(new String("upgver=").length(),md5.length());
            }

//            //test
//            latest_version = "1.1.0.0";

            /*
            * if current version <= latest version: force update
             */
            if(Utils.checkFirmwareVersion(latest_version, current_version,true) == true) {
                forceUpdate |= APP_UPDATE_FORCE; // need update
            }
        }
        return forceUpdate;
    }

    protected void checkAppVersion(Message startupMessage, Message timeoutMessage) {
        mCheckVersionDone = false;
        mReplyStartupMessage = startupMessage;
        mReplyTimeoutMessage = timeoutMessage;
        mBackgroundHandler.sendEmptyMessage(MESSAGE_CHECK_APP_VERSION);
        mHandler.sendEmptyMessageDelayed(MESSAGE_CHECK_APP_VERSION_TIMEOUT,5000);
    }

    protected synchronized boolean isCheckAppVersionDone() {
        return mCheckVersionDone;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy =
                    new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        mContext=this;

        mBackgroundThread=new HandlerThread("AbstractRoomHubActivity");
        mBackgroundThread.start();
        mBackgroundHandler = new BackgroundHandler(mBackgroundThread.getLooper());
    }

    @Override
    protected void onPause() {
        super.onPause();

        if(menu_settings!=null){
            menu_settings.dismiss();
            menu_settings=null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Utils.isWifiConnectWithPrefix(mContext)==true) {
            Utils.showWifiConnectionAlertDialog(mContext);
        }
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);
        mCurrentFragment = fragment;
    }

    @Override
    public RoomHubManager getRoomHubManager () {
        return ((RoomHubApplication) getApplication()).getRoomHubManager();
    }

    @Override
    public ACNoticeManager getACNoticeManager() {
        return ((RoomHubApplication) getApplication()).getACNoticeManager();
    }

    @Override
    public AccountManager getAccountManager () {
        return ((RoomHubApplication) getApplication()).getAccountManager();
    }

    @Override
    public OnBoardingManager getOnBoardingManager () {
        return ((RoomHubApplication) getApplication()).getOnBoardingManager();
    }
/*
    @Override
    public MicroLocationManager getLocationManager () {
        return ((RoomHubApplication) getApplication()).getMicroLocationManager();
    }
*/
    @Override
    public IRController getIRController() {
        return ((RoomHubApplication) getApplication()).getIRController();
    }

    @Override
    public RoomHubDBHelper getRoomHubDBHelper() {
        return ((RoomHubApplication) getApplication()).getRoomHubDBHelper();
    }

    @Override
    public OTAManager getOTAManager() {
        return ((RoomHubApplication) getApplication()).getOTAManager();
    }

    @Override
    public HealthDeviceManager getHealthDeviceManager() {
        return ((RoomHubApplication) getApplication()).getHealthDeviceManager();
    }

    @Override
    public BLEPairController getBLEController() {
        return ((RoomHubApplication) getApplication()).getBLEController();
    }

    @Override
    public void showProgressDialog(String title, String message) {
        if(!isShowing()) {
            mProgressDialog = ProgressDialog.show(AbstractRoomHubActivity.this, title, message, true);
        }
    }

    @Override
    public void dismissProgressDialog(int showtime, int ret, int error_code) {
        mShowTime = showtime;
        mResult = ret;
        if(ret != 0) {
            mMsgData = new Bundle();
            mMsgData.putInt("err_code", error_code);
        } else {
            mMsgData = null;
        }
        new Thread() {
            public void run() {
                try {
                    sleep(mShowTime);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    mProgressDialog.dismiss();
                    Message msg = new Message();
                    msg.what = mResult;
                    if (mMsgData != null) {
                        msg.setData(mMsgData);
                    }
                    mHandler.sendMessage(msg);
                }
            }
        }.start();
    }

    @Override
    public void dismissProgressDialog() {
        if(mProgressDialog!=null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
            mProgressDialog=null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        if(mIsMenu) {
            MenuInflater inflater = getMenuInflater();

            inflater.inflate(menuRes, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.menu_settings) {
            OpenMeneuSettings(findViewById(R.id.menu_settings));
        }
        return super.onOptionsItemSelected(item);
    }

    public void TabChoose(int num){
        mTabChoose = num;
    }

    public void OpenMeneuSettings(View v){
        ContextThemeWrapper wrapper=new ContextThemeWrapper(this,R.style.MenuSettings);
        menu_settings = new PopupMenu(wrapper, v);

        int menu_resource = R.menu.submenu_settings;
        if(mCurrentFragment instanceof HealthcareTabFrag)
            menu_resource = R.menu.health_submenu_settings;
        else if(mCurrentFragment instanceof AppliancesTabFrag)
            menu_resource = R.menu.submenu_settings;
        menu_settings.getMenuInflater().inflate(menu_resource, menu_settings.getMenu());

        menu_settings.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
//                    case R.id.account: {
//                        Intent intent = new Intent(mContext, EditProfileActivity.class);
//                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                        intent.addCategory(Intent.CATEGORY_HOME);
//                        startActivity(intent);
//                    }
//                    break;
                    case R.id.add_device: {
                        addHub();
                        /*
                        if(isWifiConnection()) {
                            if (!getAccountManager().isLogin()) {
                                Utils.ShowLoginActivity(mContext, RoomHubMainPage.class);
                            } else {
                                loadSetupWifiPage();
                            }
                        }
                        else
                            showConnectToWifiDialog();
                        */
                    }
                        break;
                    case R.id.ap_transfer: {
                        showAPTransferDialog();
                    }
                        break;
//                    case R.id.settings: {
//                        Intent intent = new Intent(mContext, SettingsActivity.class);
//                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                        startActivity(intent);
//                    }
//                    break;
//                    case R.id.notification: {
//                        Intent intent = new Intent(mContext, NotificationCenterActivity.class);
//                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                        startActivity(intent);
//                    }
//                        break;
//                    case R.id.feedback: {
//                                Intent intent = new Intent(Intent.ACTION_VIEW);
//                        intent.setData(Uri.parse(getString(R.string.config_feedback_url).toLowerCase()));
//                        startActivity(intent);
//                    }
//                        break;
                    case R.id.close_all_device:
                        ((RoomHubMainPage) mContext).CloseAllDevices();
                        //Toast.makeText(mContext, mContext.getResources().getString(R.string.coming_soon), Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.health_add_device: {
                        if (!getAccountManager().isLogin()) {
                            Utils.ShowLoginActivity(mContext, RoomHubMainPage.class);
                            return true;
                        }

                        if(Utils.isAllowToAddHealthcareDevice(mContext)) {
                            Intent intent = new Intent(mContext, AddHealthcareActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }
                    }
                        break;
                    case R.id.health_manage_all_devices: {
                        if (!getAccountManager().isLogin()) {
                            Utils.ShowLoginActivity(mContext, RoomHubMainPage.class);
                            return true;
                        }

                        Intent intent = new Intent(mContext, ElectricMgrActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra(GlobalDef.KEY_DEVICE_CATEGORY, DeviceTypeConvertApi.CATEGORY.HEALTH);
                        startActivity(intent);
                    }
                        break;
                }
                return true;
            }
        });
        /*
         * 20160601, comment out to not showing menu icon for new requirement
         * Enable it if want to show menu icon
          */
        /*
        * show menu icon
         */
        /*
        try {
            Field[] fields = menu_settings.getClass().getDeclaredFields();
            for (Field field : fields) {
                if ("mPopup".equals(field.getName())) {
                    field.setAccessible(true);
                    Object menuPopupHelper = field.get(menu_settings);
                    Class<?> classPopupHelper = Class.forName(menuPopupHelper
                            .getClass().getName());
                    Method setForceIcons = classPopupHelper.getMethod(
                            "setForceShowIcon", boolean.class);
                    setForceIcons.invoke(menuPopupHelper, true);
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        */
        menu_settings.show();
    }

    private void showAPTransferDialog() {
        final Dialog dialog = new Dialog(this,R.style.CustomDialog);

        dialog.setContentView(R.layout.custom_dialog);

        TextView txt_msg=(TextView)dialog.findViewById(R.id.txt_message);
        txt_msg.setText(getString(R.string.change_wifi_confirm_dialog));

        Button btn_yes = (Button)dialog.findViewById(R.id.btn_yes);
        btn_yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                Intent intent = new Intent(mContext, APTransferActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
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

    private boolean isWifiConnection() {
        ConnectivityManager mConnectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = mConnectivityManager.getActiveNetworkInfo();
        if(networkInfo != null && networkInfo.isConnected()) {
            if(networkInfo.getType() == ConnectivityManager.TYPE_WIFI)
                return true;
        }
        return false;
    }

    private void showConnectToWifiDialog() {
        final Dialog dialog = new Dialog(this,R.style.CustomDialog);

        dialog.setContentView(R.layout.custom_dialog);

        TextView txt_msg=(TextView)dialog.findViewById(R.id.txt_message);
        txt_msg.setText(getString(R.string.roomhub_warning_msg));

        Button btn_yes = (Button)dialog.findViewById(R.id.btn_yes);
        btn_yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        Button btn_no = (Button)dialog.findViewById(R.id.btn_no);
        btn_no.setVisibility(View.GONE);

        dialog.show();

    }

    private void showOnBoardingConfirmDialog() {
        PreferenceEditor pref = new PreferenceEditor(this, GlobalDef.ROOMHUB_SETTINGS_PREFERENCE_NAME);
        StringBuilder sb = new StringBuilder();
        sb.append(getString(R.string.onboarding_prompt_msg1, pref.getStringValue(GlobalDef.ROOMHUB_SETTINGS_WIFI_SSID)));
        sb.append("\n");
        sb.append(getString(R.string.onboarding_prompt_msg2));

        final Dialog dialog = new Dialog(this,R.style.CustomDialog);

        dialog.setContentView(R.layout.custom_dialog_onboarding_comfirm);

        TextView txt_msg=(TextView)dialog.findViewById(R.id.txt_message);
        txt_msg.setText(sb.toString());

        Button btn_yes = (Button)dialog.findViewById(R.id.btn_yes);
        btn_yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                loadSetupWifiPage();
//                loadOnBoardingPage();
            }
        });

        Button btn_no = (Button)dialog.findViewById(R.id.btn_no);
        btn_no.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        //dialog.setCancelable(false);
        dialog.show();
    }

    private void loadPreOnboardingPage() {
        Intent intent = new Intent(this, PreOnBoardingActivity.class);
        startActivity(intent);
    }

    private void loadSetupWifiPage() {
        Intent intent = new Intent(this, SetupWifiActivity.class);
        intent.setAction("android.intent.action.CHANGE_WIFI");
        startActivity(intent);
    }

    private void loadOnBoardingPage() {
        PreferenceEditor pref = new PreferenceEditor(this, GlobalDef.ROOMHUB_SETTINGS_PREFERENCE_NAME);
        Intent intent = new Intent(this, OnBoardingActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addCategory(Intent.CATEGORY_HOME);
        Bundle bundle = new Bundle();
        bundle.putString(GlobalDef.WIFI_AP_SSID, pref.getStringValue(GlobalDef.ROOMHUB_SETTINGS_WIFI_SSID));
        bundle.putInt(GlobalDef.WIFI_AP_SECURITY, pref.getIntValue(GlobalDef.ROOMHUB_SETTINGS_WIFI_SECURITY));
        bundle.putSerializable(GlobalDef.WIFI_AP_PSKTYPE, GlobalDef.getPskType(pref.getIntValue(GlobalDef.ROOMHUB_SETTINGS_WIFI_SECURITY_PSK)));
        bundle.putString(GlobalDef.WIFI_AP_PASSWORD, pref.getStringValue(GlobalDef.ROOMHUB_SETTINGS_WIFI_PASSWORD));
        intent.putExtras(bundle);
        startActivity(intent);
    }

    public void OpenMenu(Context contxt,int menu_res){
        //mContext=contxt;
        mIsMenu=true;
        menuRes=menu_res;
    }

    public boolean isShowing(){
        if(mProgressDialog == null)
            return false;

        if(mProgressDialog.isShowing())
            return true;

        return false;
    }

    public void launchDeviceList() {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setClass(this, RoomHubMainPage.class);
        startActivity(intent);
    }

    public void addHub(){
        if(isWifiConnection()) {
            if (!getAccountManager().isLogin()) {
                Utils.ShowLoginActivity(mContext, RoomHubMainPage.class);
            } else {
                loadPreOnboardingPage();
            }
        }
        else
            showConnectToWifiDialog();
    }

    private void showUpdateAppDialog(final int forceUpdate) {
        final Dialog dialog = new Dialog(this,R.style.CustomDialog);
        dialog.setContentView(R.layout.custom_dialog);
        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);

        TextView txt_msg=(TextView)dialog.findViewById(R.id.txt_message);
        txt_msg.setText(getString(R.string.app_update_message));

        Button btn_yes = (Button)dialog.findViewById(R.id.btn_yes);
        btn_yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                String url = mAppCustomRedirectUrl == null
                        ? "market://details?id=" + getPackageName() : mAppCustomRedirectUrl;
                mBackgroundHandler.sendMessage(mBackgroundHandler.obtainMessage(MESSAGE_REDIRECT_URL, url));

                if(Utils.getCurrentActivityName(mContext).contains("MainActivity")) {
                    System.exit(0);
                    return;
                }
                Intent intent = new Intent(mContext, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra(GlobalDef.FORCE_TERMINATE, true);
                startActivity(intent);
            }
        });

        Button btn_no = (Button)dialog.findViewById(R.id.btn_no);
        if((forceUpdate & APP_UPDATE_FORCE) == APP_UPDATE_FORCE)
            btn_no.setVisibility(View.GONE);
        else {
            btn_no.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    if(mReplyStartupMessage != null)
                        mReplyStartupMessage.sendToTarget();
                }
            });
        }
        dialog.setCancelable(false);
        dialog.show();
    }

    public void OpenElectricMenu(View v, final RoomHubData roomhub_data){
        if((v == null) || (roomhub_data == null)) return;

        final String uuid=roomhub_data.getUuid();
        ContextThemeWrapper wrapper=new ContextThemeWrapper(this,R.style.MenuSettings);
        menu_settings = new PopupMenu(wrapper, v);

        ArrayList<AssetInfoData> asset_list = roomhub_data.getAssetListNoSameType();
        if(asset_list.size() <= 0 ) return;

        MenuItem menu_item = null;
        Intent intent = null;

        for (Iterator<AssetInfoData> it = asset_list.iterator(); it.hasNext(); ) {
            BaseAssetData asset_data = (BaseAssetData)it.next();

            menu_item=menu_settings.getMenu().add(Menu.NONE, asset_data.getAssetType(), Menu.NONE, asset_data.getAssetName());
            int asset_type = asset_data.getAssetType();
            if(asset_type == DeviceTypeConvertApi.TYPE_ROOMHUB.AC) {
                menu_item.setIcon(getResources().getDrawable(R.drawable.icon_ac_green));
                if(asset_data.getSubType() == ACDef.AC_SUBTYPE_WINDOW_TYPE)
                    intent = new Intent(mContext, WindowTypeACController.class);
                else
                    intent = new Intent(mContext, RoomHubControllerFlipper.class);
            }else if(asset_type == DeviceTypeConvertApi.TYPE_ROOMHUB.FAN) {
                menu_item.setIcon(getResources().getDrawable(R.drawable.icon_fan_green));
                intent = new Intent(mContext, FANControllerV2.class);
            }else if(asset_type == DeviceTypeConvertApi.TYPE_ROOMHUB.PM25){
                menu_item.setIcon(getResources().getDrawable(R.drawable.btn_pm_green));
                intent = new Intent(mContext, PMActivity.class);
            }else if(asset_type == DeviceTypeConvertApi.TYPE_ROOMHUB.AIR_PURIFIER){
                menu_item.setIcon(getResources().getDrawable(R.drawable.btn_ion_green));
                if(asset_data.getConnectionType() == AssetDef.CONNECTION_TYPE_BT)
                    intent = new Intent(mContext, AirPurifierBTActivity.class);
                else
                    intent = new Intent(mContext, AirPurifierIRActivity.class);
            }else if(asset_type == DeviceTypeConvertApi.TYPE_ROOMHUB.TV){
                menu_item.setIcon(getResources().getDrawable(R.drawable.icon_dropmenu_tv));
                intent = new Intent(mContext, TVController.class);
            }else if(asset_type == DeviceTypeConvertApi.TYPE_ROOMHUB.BULB){
                menu_item.setIcon(getResources().getDrawable(R.drawable.btn_lamp_green));
                intent = new Intent(mContext, BulbController.class);
            }
            //   intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //  intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra(BaseAssetManager.KEY_ASSET_INFO_DATA, (Parcelable) asset_data);
            menu_item.setIntent(intent);
        }

        menu_settings.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                Intent intent = item.getIntent();
                //String uuid=intent.getStringExtra(RoomHubManager.KEY_UUID);
                AssetInfoData data = intent.getParcelableExtra(BaseAssetManager.KEY_ASSET_INFO_DATA);

                if (!data.IsIRPair()) {
                    Toast.makeText(mContext, R.string.device_not_ir_pairing, Toast.LENGTH_SHORT).show();
                } else {
                    if (!Utils.getCurrentActivityName(mContext).equals(intent.getComponent().getClassName())) {
                        Intent intent_action = new Intent();
                        ComponentName cn = new ComponentName(mContext, intent.getComponent().getClassName());
                        intent_action.setComponent(cn);

                        intent_action.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent_action.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent_action.putExtra(BaseAssetManager.KEY_UUID, data.getRoomHubUuid());
                        intent_action.putExtra(BaseAssetManager.KEY_ASSET_UUID, data.getAssetUuid());
                        startActivity(intent_action);
                        finish();
                    }
                }
                return true;
            }
        });
        try {
            Field[] fields = menu_settings.getClass().getDeclaredFields();
            for (Field field : fields) {
                if ("mPopup".equals(field.getName())) {
                    field.setAccessible(true);
                    Object menuPopupHelper = field.get(menu_settings);
                    Class<?> classPopupHelper = Class.forName(menuPopupHelper
                            .getClass().getName());
                    Method setForceIcons = classPopupHelper.getMethod(
                            "setForceShowIcon", boolean.class);
                    setForceIcons.invoke(menuPopupHelper, true);
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        menu_settings.show();
    }
}
