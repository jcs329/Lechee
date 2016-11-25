package com.quantatw.roomhub.ui;

import android.Manifest;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Parcelable;
import android.provider.Settings;
import android.text.InputType;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.quantatw.myapplication.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.quantatw.roomhub.manager.AccountManager;

import com.quantatw.roomhub.manager.health.data.HealthData;
import com.quantatw.roomhub.ui.bpm.BPMGuideActivity;
import com.quantatw.roomhub.ui.bpm.BPMHistoryActivity;
import com.quantatw.roomhub.utils.GlobalDef;
import com.quantatw.roomhub.utils.Utils;
import com.quantatw.sls.key.ErrorKey;

public class MainActivity extends AbstractRoomHubActivity {
    private final String TAG=MainActivity.class.getSimpleName();
    private Context mContext;
    private LinearLayout ll_start, ll_loading;
    private ImageEditTextWidget txtUserAccount;
    private ImageEditTextWidget txtPassword;
    private Button btnLogin;
    private Button btnCreateAccount;
    private Button btnSkip;

    private ImageView imgCreateAccount, imgSkip;

    private AccountManager mAccountManager;

    private final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    private int mGcmMessageTypeId;
    private String mGcmCustomMessage;
    private boolean mForceSkipAutoLogin;
    private String mLaunchBPMUuid,mLaunchBPMUserId;
    private boolean mIsLogout;
    private boolean mInitService = false;
    private boolean mRegister = false;
    private int mCheckServiceRetryCount = 0;

    private HandlerThread mBackgroundThread;
    private BackgroundHandler mBackgroundHandler;

    private interface StartupConditionState {
        int INIT = 1;
        int CHECK_WIFI_CONNECTION = 2;
        int CHECK_WIFI_INTERNET = 3;
        int DONE = 4;
    }

    private final int REQUEST_ENABLE_BT = 1;
    private final int REQUEST_FORCE_BIND_WIFI_NETWORK = 2;
    private final int REQUEST_ENABLE_LOCATION = 3;
    private final int PERMISSIONS_REQUEST_OVERLAY = 4;
    private final int PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION = 5;

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive INTENT_ROOMHUB_SERVICE_INIT_DONE");
            mRegister = false;
            mOBHandler.removeMessages(PROCESS_FORCE_CHECK_SERVICE_IS_READY);
            unregisterReceiver(this);

            verifyStartupConditions();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.room_hub_main);

        Log.d(TAG,"onCreate");

        mContext = this;
        checkPlayServices();

        Intent intent = getIntent();
        if(intent != null && intent.getBooleanExtra(GlobalDef.FORCE_TERMINATE,false)==true) {
            Log.d(TAG, "got TERMINATE");
            setIntent(null);
            finish();
            System.exit(0);
            return;
        }

        if(intent != null) {
            mGcmMessageTypeId = intent.getIntExtra(GlobalDef.GCM_MESSAGE_TYPE_ID,0);
            mGcmCustomMessage = intent.getStringExtra(GlobalDef.GCM_MESSAGE);
            mForceSkipAutoLogin = intent.getBooleanExtra(Utils.KEY_SKIP_AUTO_LOGIN, false);
            mIsLogout = intent.getBooleanExtra(GlobalDef.KEY_IS_LOGOUT, false);
            if(intent.getAction() != null && intent.getAction().equals(GlobalDef.ACTION_REDIRECT_NOTIFICATION)) {
                mLaunchBPMUuid=intent.getStringExtra(GlobalDef.BP_UUID_MESSAGE);
                mLaunchBPMUserId=intent.getStringExtra(GlobalDef.BP_USERID_MESSAGE);
            }
        }

        ll_start = (LinearLayout) findViewById(R.id.ll_start);
        ll_loading = (LinearLayout) findViewById(R.id.ll_loading);

        mBackgroundThread = new HandlerThread("MainActivity");
        mBackgroundThread.start();
        mBackgroundHandler = new BackgroundHandler(mBackgroundThread.getLooper());

        boolean isServiceReady = ((RoomHubApplication) getApplication()).isServiceReady();
        Log.d(TAG, "onCreate isServiceReady=" + isServiceReady);
        if (!isServiceReady) {
            mRegister = true;
            registerReceiver(mReceiver, new IntentFilter(RoomHubService.INTENT_ROOMHUB_SERVICE_INIT_DONE));
            mOBHandler.sendEmptyMessageDelayed(PROCESS_FORCE_CHECK_SERVICE_IS_READY, 3000);
            return;
        }

        verifyStartupConditions();
    }

    private void verifyStartupConditions() {
        verifyStartupConditions(0);
    }

    private void verifyStartupConditions(int assignState) {
        Log.d(TAG,"verifyStartupConditions assignState="+assignState);
        if(assignState > 0) {
            switch (assignState) {
                case StartupConditionState.INIT:
                    if(checkPermissions())
                        return;
                case StartupConditionState.CHECK_WIFI_CONNECTION:
                    /*
                    * Android 6.0+
                    * Wifi Connection won't be setup or automatically reconnect
                    * if there is no internet accessibility.
                    * Need to prompt user to connect Wifi AP manually:
                     */
                    if(verifyWifiConnection())
                        return;
                case StartupConditionState.CHECK_WIFI_INTERNET:
                    if(!verifyWifiInternetCapability())
                        return;
                case StartupConditionState.DONE:
                    verifyConditions();
                    break;
            }
        }
        else {
            if (checkPermissions())
                return;

        /*
        * Android 6.0+
        * Wifi Connection won't be setup or automatically reconnect
        * if there is no internet accessibility.
        * Need to prompt user to connect Wifi AP manually:
         */
            if (verifyWifiConnection())
                return;

            if (!verifyWifiInternetCapability())
                return;

            verifyConditions();
        }
    }

    private boolean checkPermissions() {
        if(Build.VERSION.SDK_INT >= 23) {
            /*
            * Android 6.0+
            * Special Permissions: SYSTEM_ALERT_WINDOW
             */
            if (!Settings.canDrawOverlays(this)) {
                showOverlayPermissionDialog();
                return true;
            }

            if(!checkLocationService())
                return true;

            /*
            * Android 6.0+
            * Dangerous Permissions
             */
            if(checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION);
                return true;
            }
        }
        return false;
    }

    private boolean checkLocationService() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enable = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!gps_enable) {
            final Dialog dialog = new Dialog(this,R.style.CustomDialog);

            dialog.setContentView(R.layout.custom_dialog_location);

            Button btn_yes = (Button)dialog.findViewById(R.id.btn_yes);
            btn_yes.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    startActivityForResult(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS),REQUEST_ENABLE_LOCATION);
                }
            });

            Button btn_no = (Button)dialog.findViewById(R.id.btn_no);
            btn_no.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    verifyStartupConditions(StartupConditionState.CHECK_WIFI_CONNECTION);
                }
            });

            dialog.setCancelable(false);
            dialog.show();

            return false;
        }
        return true;
    }

    private void showOverlayPermissionDialog() {
        Log.d(TAG, "showOverlayPermissionDialog AbstractRoomHubActivity");
        final Dialog dialog = new Dialog(this,R.style.CustomDialog);

        dialog.setContentView(R.layout.custom_dialog);

        TextView txt_msg=(TextView)dialog.findViewById(R.id.txt_message);
        txt_msg.setText(getString(R.string.prompt_overlay_permissions));

        Button btn_yes = (Button)dialog.findViewById(R.id.btn_yes);
        btn_yes.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
//                startActivity(intent);
                startActivityForResult(intent, PERMISSIONS_REQUEST_OVERLAY);
            }
        });

        Button btn_no = (Button)dialog.findViewById(R.id.btn_no);
        btn_no.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();
                finish();
                System.exit(0);
            }
        });

        dialog.setCancelable(false);
        dialog.show();
    }

    private boolean verifyWifiConnection() {
        if(Build.VERSION.SDK_INT >= 23) {
            if (Utils.needToBindWifiConnection(mContext)) {
                // notify user to connect Wifi
                if (Utils.isRoomHubAppForeground(mContext)) {
                    Utils.showConnectWifiDialog(mContext,
                            mBackgroundHandler.obtainMessage(PROCESS_LAUNCH_CHOOSE_WIFI));
                    return true;
                }
            }
        }
        return false;
    }

    private boolean verifyWifiInternetCapability() {
        WifiManager wifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);

        if(!wifiManager.isWifiEnabled())
            return true;

        if(Build.VERSION.SDK_INT >= 23) {
            if(!Utils.isWiFiInternetAvailable(this)) {
                showWifiNoInternetDialog();
                return false;
            }
        }
        return true;
    }

    private void verifyConditions() {
        /* Verify App version */
        checkAppVersion(mOBHandler.obtainMessage(PROCESS_CHECK_APP_DONE),
                mOBHandler.obtainMessage(PROCESS_CHECK_APP_VERSION_TIMEOUT));
    }

    private void startup() {
        Log.d(TAG,"startup");
        mInitService = true;
        mAccountManager = getAccountManager();
        if(!Utils.isShowWelcome(this))
        {
            setLoadingPage(true);
            Utils.setShowWelcome(this);
            if(mAccountManager.couldAutoLogin())
                mLoaderHandler.postDelayed(runnable, 3000);
            else {
                mLoaderHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        initPage(false, null, null);
                    }
                },3000);
            }
        }
        else {
            if (mAccountManager.couldAutoLogin() && !mForceSkipAutoLogin) {
                setLoadingPage(false);
                mLoaderHandler.postDelayed(runnable, 1000);
            } else
                initPage(false, null, null);
        }
    }

    private void checkBT() {
        if(Utils.isLocateMeOn(this) && !mIsLogout) {
            final BluetoothManager bluetoothManager =
                    (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            BluetoothAdapter mBluetoothAdapter = bluetoothManager.getAdapter();

            // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
            // fire an intent to display a dialog asking the user to grant permission to enable it.
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            } else
                mOBHandler.sendEmptyMessage(PROCESS_APP_STARTUP);
        }
        else
            mOBHandler.sendEmptyMessage(PROCESS_APP_STARTUP);

    }

    private Handler mLoaderHandler = new Handler( );

    private Runnable runnable = new Runnable( ) {
        public void run ( ) {
            doAutoLogin();

            mLoaderHandler.removeCallbacks(runnable);
        }
    };

    private void doAutoLogin() {
        showProgressDialog("", getString(R.string.process_str));

        new Thread() {
            public void run() {
                int ret = ErrorKey.ConnectionError;
                try {
                    ret = mAccountManager.autoLogin();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    Message msgPost = new Message();
                    if(ret == ErrorKey.Success) {
                        msgPost.what = PROCESS_AUTO_LOGIN_SUCCESS;
                    } else {
                        msgPost.what = PROCESS_AUTO_LOGIN_FAIL;
                        Bundle data = new Bundle();
                        data.putInt("error_code", ret);
                        msgPost.setData(data);
                    }
                    mOBHandler.sendMessage(msgPost);
                }
            }
        }.start();
    }

    private void setLoadingPage(boolean firstIn) {
        Log.d(TAG,"setLoadingPage "+firstIn);
        ll_loading.setVisibility(View.VISIBLE);
        ll_start.setVisibility(View.GONE);

        TextView txtLoadingTitle = (TextView) findViewById(R.id.txtLoadingTitle);
      //  txtLoadingTitle.setTextSize(getResources().getDimension(R.dimen.main_page_loading_text_size));
        if (firstIn) {
            txtLoadingTitle.setText(getString(R.string.first_welcome_msg));
            TextView minorTitle = (TextView)findViewById(R.id.txtLoadingTitleMinor);
            minorTitle.setText(getString(R.string.first_welcom_msg_minor));
            minorTitle.setVisibility(View.VISIBLE);
        }

        ImageView imgLoading = (ImageView) findViewById(R.id.imgLoading);
        imgLoading.setLayoutParams(setMargin(0, 0, (int) getResources().getDimension(R.dimen.main_page_loading_margin),
                (int) getResources().getDimension(R.dimen.main_page_loading_margin), (LinearLayout.LayoutParams) imgLoading.getLayoutParams()));

        //showProgressDialog("", getString(R.string.process_str));
        //dismissProgressDialog(2000, DIALOG_SHOW_NOTHING, 0);
    }

    private void LoadRoomHubMainPage() {
        /* 20160218, [0000214]
        * Whether provision is set or not, redirect to device list
         */
        //if(Utils.getProvision(this) == GlobalDef.PROVISION_SET)
        {
            //setLoadingPage();
            Intent intent = new Intent(MainActivity.this, RoomHubMainPage.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra(GlobalDef.GCM_MESSAGE_TYPE_ID, mGcmMessageTypeId);
            intent.putExtra(GlobalDef.GCM_MESSAGE,mGcmCustomMessage);
            intent.putExtra(GlobalDef.BP_UUID_MESSAGE,mLaunchBPMUuid);
            intent.putExtra(GlobalDef.BP_USERID_MESSAGE,mLaunchBPMUserId);
            startActivity(intent);
            //System.exit(0);
        }
        /*
        else {
            Intent intent = new Intent(MainActivity.this, SetupWifiActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            //System.exit(0);
        }
        */
    }

    private void LoadSignupPage() {
        Intent intent = new Intent(this, SignupActivity.class);
        startActivityForResult(intent, GlobalDef.SIGNUP_REQUEST_CODE_DONE);
    }

    private void initPage(boolean bringInUserPass, String userAccount, String userPass) {
        ll_start.setVisibility(View.VISIBLE);
        ll_loading.setVisibility(View.GONE);

        TextView txtTitle = (TextView) findViewById(R.id.txtMainTitle);
        TextView txtDesc = (TextView) findViewById(R.id.txtDesc);

        txtUserAccount = (ImageEditTextWidget) findViewById(R.id.loginAccount);
        txtUserAccount.getLayoutParams().height = (int) getResources().getDimension(R.dimen.main_page_image_edit_height);
        txtUserAccount.setLayoutParams(setMargin(0, 0, 0, (int) getResources().getDimension(R.dimen.main_page_margin_bottom), (LinearLayout.LayoutParams) txtUserAccount.getLayoutParams()));
        txtUserAccount.setEditImage(R.drawable.icon_account);
        txtUserAccount.setEditHint(R.string.user_account_desc);
        txtUserAccount.setEditInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        if(bringInUserPass)
            txtUserAccount.setText(mAccountManager.getCurrentAccount());
        else if(!TextUtils.isEmpty(userAccount))
            txtUserAccount.setText(userAccount);

        txtPassword = (ImageEditTextWidget) findViewById(R.id.loginPwd);
        txtPassword.getLayoutParams().height = (int) getResources().getDimension(R.dimen.main_page_image_edit_height);
        txtPassword.setLayoutParams(setMargin(0, 0, 0, (int) getResources().getDimension(R.dimen.main_page_margin_bottom), (LinearLayout.LayoutParams) txtPassword.getLayoutParams()));
        txtPassword.setEditImage(R.drawable.icon_password);
        txtPassword.setEditHint(R.string.password_desc);
        txtPassword.setEditInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
        txtPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
        if(bringInUserPass)
            txtPassword.setText(mAccountManager.getCurrentAccountPass());
        else if(!TextUtils.isEmpty(userPass))
            txtPassword.setText(userPass);

        btnLogin = (Button) findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if (verifyWifiConnection())
//                    return;
                doLogin();
            }
        });

        View ll_bottom = (View) findViewById(R.id.ll_bottom);
        ll_bottom.setLayoutParams(setMargin((int) getResources().getDimension(R.dimen.main_page_margin_bottom_reg_left),
                (int) getResources().getDimension(R.dimen.main_page_margin_bottom_reg_left),
                        (int) getResources().getDimension(R.dimen.main_page_margin_bottom_reg_top), 0,
                        (LinearLayout.LayoutParams) ll_bottom.getLayoutParams()));

        btnCreateAccount = (Button) findViewById(R.id.btnCreateAccount);
        imgCreateAccount = (ImageView) findViewById(R.id.imgCreateAccount);
        imgCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imgCreateAccount.setImageResource(R.drawable.btn_enter_pressed);
                LoadSignupPage();
            }
        });
        //btnCreateAccount.setTextSize(getResources().getDimension(R.dimen.main_page_process_text_size));
        btnCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imgCreateAccount.setImageResource(R.drawable.btn_enter_pressed);
                LoadSignupPage();
            }
        });

        btnSkip = (Button) findViewById(R.id.btnSkip);
      //  btnSkip.setTextSize(getResources().getDimension(R.dimen.main_page_process_text_size));
        imgSkip = (ImageView) findViewById(R.id.imgSkip);

        View createAccountLayout = (View)ll_bottom.findViewById(R.id.createAccountLayout);
        View skipLayout = (View)ll_bottom.findViewById(R.id.skipLayout);

        /*
        * For Debug:
         */
        boolean forceDisplaySkip = getResources().getBoolean(R.bool.config_force_display_skip_for_debug);

        if(!mAccountManager.loginBefore() && !forceDisplaySkip) {
            createAccountLayout.setVisibility(View.VISIBLE);
            skipLayout.setVisibility(View.INVISIBLE);
            /*RelativeLayout.LayoutParams params =
                    new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.CENTER_HORIZONTAL);
            createAccountLayout.setLayoutParams(params);*/
        }
        else {
            createAccountLayout.setVisibility(View.VISIBLE);
            skipLayout.setVisibility(View.VISIBLE);
            /*RelativeLayout.LayoutParams params =
                    new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            skipLayout.setLayoutParams(params);*/

            imgSkip.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    imgSkip.setImageResource(R.drawable.btn_skip);
                    if(!isMobileNetwork()) {
                        mAccountManager.skipLogin();
                        LoadRoomHubMainPage();
                    }
                    else
                        showUseMobileNetworkConfirmDialog();
                }
            });
            btnSkip.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    imgSkip.setImageResource(R.drawable.btn_skip);
                    if(!isMobileNetwork()) {
                        mAccountManager.skipLogin();
                        LoadRoomHubMainPage();
                    }
                    else
                        showUseMobileNetworkConfirmDialog();
                }
            });
        }

        TextView txtForgetPass = (TextView) findViewById(R.id.txtForgetPassword);
        txtForgetPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext,ForgetPassActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);

                /*
                if(txtUserAccount.getText().toString().length() == 0 ||
                        (!txtUserAccount.getText().toString().contains("@") || !txtUserAccount.getText().toString().contains("."))) {
                    Toast.makeText(getApplicationContext(), getString(R.string.email_prompt), Toast.LENGTH_SHORT).show();
                    txtUserAccount.requestFocus();
                    return;
                }
                showProgressDialog("", getString(R.string.process_str));

                Message msg = new Message();
                msg.what = PROCESS_FORGET_PWD;
                Bundle data = new Bundle();
                data.putString("account", txtUserAccount.getText().toString());
                msg.setData(data);
                mBackgroundHandler.sendMessage(msg);
                */
            }
        });
    }

    private boolean isMobileNetwork() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if(networkInfo != null
                && networkInfo.getType()==ConnectivityManager.TYPE_MOBILE
                && networkInfo.isConnected()) {
            return true;
        }
        return false;
    }

    private void showWifiNoInternetDialog() {
        final Dialog dialog = new Dialog(this,R.style.CustomDialog);

        dialog.setContentView(R.layout.custom_dialog);

        TextView txt_msg=(TextView)dialog.findViewById(R.id.txt_message);

        // true: connected, but has no internet caps
        // false: not connected yet
        final boolean isWifiConnectedWithInternet = Utils.needToBindWifiConnection(this);

        String msg;
        if(!isWifiConnectedWithInternet)
            msg = getString(R.string.wifi_network_no_interet);
        else
            msg = getString(R.string.wifi_network_not_connect);

        txt_msg.setText(msg);

        Button btn_yes = (Button)dialog.findViewById(R.id.btn_yes);
        btn_yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                if(!isWifiConnectedWithInternet) {
                    Utils.bindProcessToWiFiNetwork(mContext, true);
                }
                verifyStartupConditions(StartupConditionState.DONE);
            }
        });

        Button btn_no = (Button)dialog.findViewById(R.id.btn_no);
        btn_no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                mBackgroundHandler.sendMessage(mBackgroundHandler.obtainMessage(PROCESS_LAUNCH_CHOOSE_WIFI,true));
            }
        });

        dialog.setCancelable(false);
        dialog.show();
    }

    private void showUseMobileNetworkConfirmDialog() {
        final Dialog dialog = new Dialog(this,R.style.CustomDialog);

        dialog.setContentView(R.layout.custom_dialog);

        TextView txt_msg=(TextView)dialog.findViewById(R.id.txt_message);
        txt_msg.setText(getString(R.string.skip_login_use_mobile_msg));

        Button btn_yes = (Button)dialog.findViewById(R.id.btn_yes);
        btn_yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                mAccountManager.skipLogin();
                LoadRoomHubMainPage();
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

    private LinearLayout.LayoutParams setMargin(int left, int right, int top, int bottom, LinearLayout.LayoutParams parms) {
        LinearLayout.LayoutParams marginParms = parms;
        marginParms.setMargins(left,top, right, bottom);

        return marginParms;
    }

    private void doLogin() {
        showProgressDialog("", getString(R.string.process_str));
        String account = Utils.trimSpace(txtUserAccount.getText().toString());
        String password = Utils.trimSpace(txtPassword.getText().toString());

        if(TextUtils.isEmpty(account)) {
            Toast.makeText(mContext, getString(R.string.account_empty_error_msg), Toast.LENGTH_SHORT).show();
            txtUserAccount.requestFocus();
            dismissProgressDialog(1500, DIALOG_SHOW_NOTHING, 0);
            return;
        }

        if (TextUtils.isEmpty(password)) {
            txtPassword.requestFocus();
            dismissProgressDialog(1500, DIALOG_SHOW_NOTHING, 0);
            return;
        }

        /*int ret = mAccountManager.Login(txtUserAccount.getText(), txtPassword.getText(), true);
        if(ret == GlobalDef.STATUS_CODE_SUCCESS) {
            dismissProgressDialog();
            LoadRoomHubMainPage();
        } else {
            txtUserAccount.requestFocus();
            dismissProgressDialog(1500, DIALOG_SHOW_LOGING_FAIL, ret);
        }*/
        Message msg = new Message();
        msg.what = PROCESS_DO_LOGIN;
        Bundle data = new Bundle();
        data.putString("account", account);
        data.putString("pass", password);
        msg.setData(data);
        mOBHandler.sendMessage(msg);
    }

    private final int PROCESS_DO_LOGIN = 190011;
    private final int PROCESS_LOGIN_SUCCESS = 190012;
    private final int PROCESS_LOGIN_FAIL = 190013;
    private final int PROCESS_AUTO_LOGIN_SUCCESS = 190014;
    private final int PROCESS_AUTO_LOGIN_FAIL = 190015;
    private final int PROCESS_FORGET_PWD = 190016;
    private final int PROCESS_LAUNCH_CHOOSE_WIFI = 190017;

    /*
    * PROCESS_FORCE_CHECK_SERVICE_IS_READY -> PROCESS_CHECK_BT -> PROCESS_APP_STARTUP
     */
    private final int PROCESS_FORCE_CHECK_SERVICE_IS_READY = 200000;
    private final int PROCESS_CHECK_BT = 210000;
    private final int PROCESS_APP_STARTUP = 220000;
    private final int PROCESS_CHECK_APP_DONE = 230000;
    private final int PROCESS_CHECK_APP_VERSION_TIMEOUT = 240000;

    private Handler mOBHandler = new Handler() {
        public void handleMessage(Message msg) {
            final String account;
            switch (msg.what) {
                case PROCESS_DO_LOGIN:
                    account = msg.getData().getString("account");
                    final String pass = msg.getData().getString("pass");
                    new Thread() {
                        public void run() {
                            int ret = ErrorKey.ConnectionError;
                            try {
                                ret = mAccountManager.Login(account, pass, true);
                            } catch (Exception e) {
                                e.printStackTrace();
                            } finally {
                                Message msgPost = new Message();
                                if(ret == ErrorKey.Success) {
                                    msgPost.what = PROCESS_LOGIN_SUCCESS;
                                } else {
                                    msgPost.what = PROCESS_LOGIN_FAIL;
                                    Bundle data = new Bundle();
                                    data.putInt("error_code", ret);
                                    msgPost.setData(data);
                                }
                                mOBHandler.sendMessage(msgPost);
                            }
                        }
                    }.start();
                    break;
                case PROCESS_LOGIN_SUCCESS:
                    dismissProgressDialog();
                    LoadRoomHubMainPage();
                    break;
                case PROCESS_LOGIN_FAIL:
                    int ret = msg.getData().getInt("error_code");
                    Log.d(TAG,"PROCESS_LOGIN_FAIL errorCode="+ret);
                    if(ret == ErrorKey.EmailNotAuthorized) {
                        dismissProgressDialog();
                        redirectToConfirmPage(null);
                        return;
                    }
                    txtUserAccount.requestFocus();
                    dismissProgressDialog(1500, DIALOG_SHOW_LOGING_FAIL, ret);
                    break;
                case PROCESS_AUTO_LOGIN_SUCCESS:
                    dismissProgressDialog();
                    LoadRoomHubMainPage();
                    break;
                case PROCESS_AUTO_LOGIN_FAIL:
                    if(msg.getData().getInt("error_code") == ErrorKey.EmailNotAuthorized) {
                        dismissProgressDialog();
                        redirectToConfirmPage(null);
                        return;
                    }
                    String errString = Utils.getErrorCodeString(getApplicationContext(), msg.getData().getInt("error_code"));
                    Toast.makeText(mContext, errString, Toast.LENGTH_SHORT).show();
                    dismissProgressDialog(1500, DIALOG_SHOW_LOGING_FAIL, msg.getData().getInt("error_code"));
                    initPage(true, null, null);
                    break;
                case PROCESS_FORCE_CHECK_SERVICE_IS_READY:
                    boolean isReady = ((RoomHubApplication)getApplication()).isServiceReady();
                    Log.d(TAG, "PROCESS_FORCE_CHECK_SERVICE_IS_READY isReady=" + isReady);
                    if(isReady)
                        sendBroadcast(new Intent(RoomHubService.INTENT_ROOMHUB_SERVICE_INIT_DONE));
                    else {
                        if(mCheckServiceRetryCount++ > 3)
                            Log.d(TAG, "!!!!!!!!!!!!!!!!! ERROR! Why the background service still not ready?????");
                        else
                            sendEmptyMessageDelayed(PROCESS_FORCE_CHECK_SERVICE_IS_READY,1000);
                    }
                    break;
                case PROCESS_CHECK_BT:
                    checkBT();
                    break;
                case PROCESS_APP_STARTUP:
                    startup();
                    break;
                case PROCESS_CHECK_APP_DONE:
                    sendEmptyMessage(PROCESS_CHECK_BT);
                    break;
                case PROCESS_CHECK_APP_VERSION_TIMEOUT:
                    sendEmptyMessage(PROCESS_CHECK_BT);
                    break;
            }
        }
    };

    private final class BackgroundHandler extends Handler {
        public BackgroundHandler (Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case PROCESS_FORGET_PWD:
                    final String account;

                    account = msg.getData().getString("account");

                    int ret_val = getAccountManager().forgetPass(account);
                    dismissProgressDialog();
                    if(ret_val != ErrorKey.Success) {
                        Toast.makeText(mContext, Utils.getErrorCodeString(getApplicationContext(), ret_val), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(mContext, getString(R.string.forget_pass_success_str), Toast.LENGTH_SHORT).show();
                    }
                    break;
                case PROCESS_LAUNCH_CHOOSE_WIFI:
                    Log.d(TAG, "--- PROCESS_LAUNCH_CHOOSE_WIFI ---");
                    if(msg.obj != null) {
                        boolean force = (boolean) msg.obj;
                        Log.d(TAG, "force = " + force);
                        if(force) {
                            Intent launchIntent = new Intent(Intent.ACTION_MAIN);
                            launchIntent.setComponent(new ComponentName("com.android.settings",
                                    "com.android.settings.wifi.WifiSettings"));
                            startActivityForResult(launchIntent,REQUEST_FORCE_BIND_WIFI_NETWORK);
                        }
                        else {
                            verifyStartupConditions(StartupConditionState.CHECK_WIFI_INTERNET);
                        }
                    }
                    break;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION) {
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                verifyStartupConditions();
            }
            else {
                finish();
                System.exit(0);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            mOBHandler.sendEmptyMessage(PROCESS_APP_STARTUP);
        }
        else if(requestCode == REQUEST_FORCE_BIND_WIFI_NETWORK) {
            verifyStartupConditions(StartupConditionState.CHECK_WIFI_INTERNET);
        }
        else if(requestCode == REQUEST_ENABLE_LOCATION) {
            verifyStartupConditions(StartupConditionState.CHECK_WIFI_CONNECTION);
        }
        else if(requestCode == PERMISSIONS_REQUEST_OVERLAY) {
            verifyStartupConditions();
        }
        else {
            if (resultCode == RESULT_OK) {
                if (requestCode == GlobalDef.SIGNUP_REQUEST_CODE_DONE) {
                    redirectToConfirmPage(data);
                } else if (requestCode == GlobalDef.SIGNUP_REQUEST_CODE_CONFIRM) {
                    String userAccount = (data != null) ?
                            data.getStringExtra(LoginConfirmActivity.KEY_USER_NAME) : null;
                    String userPass = (data != null) ?
                            data.getStringExtra(LoginConfirmActivity.KEY_USER_PASS) : null;
                    initPage(false, userAccount, userPass);
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        finish();
        System.exit(0);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(!isCheckAppVersionDone())
            setLoadingPage(!Utils.isShowWelcome(this));
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(mRegister)
            unregisterReceiver(mReceiver);

        if (mBackgroundThread != null ) {
            mBackgroundThread.quit();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        }
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Log.d(TAG, "checkPlayServices: This device is not supported.");
            }
            return false;
        }
        return true;
    }

    private void redirectToConfirmPage(Intent data) {
        Intent intent = new Intent(this,LoginConfirmActivity.class);
        String username, account, password;
        if(data != null) {
            username = data.getStringExtra(LoginConfirmActivity.KEY_USER_NAME);
            account = data.getStringExtra(LoginConfirmActivity.KEY_USER_ACCOUNT);
            password = data.getStringExtra(LoginConfirmActivity.KEY_USER_PASS);
        }
        else {
            username = Utils.trimSpace(txtUserAccount.getText().toString());
            account = Utils.trimSpace(txtUserAccount.getText().toString());
            password = Utils.trimSpace(txtPassword.getText().toString());
        }
        intent.putExtra(LoginConfirmActivity.KEY_USER_NAME, username);
        intent.putExtra(LoginConfirmActivity.KEY_USER_ACCOUNT,account);
        intent.putExtra(LoginConfirmActivity.KEY_USER_PASS, password);
        startActivityForResult(intent, GlobalDef.SIGNUP_REQUEST_CODE_CONFIRM);
    }
}
