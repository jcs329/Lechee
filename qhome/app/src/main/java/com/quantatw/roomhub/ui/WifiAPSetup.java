package com.quantatw.roomhub.ui;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.quantatw.myapplication.R;
import com.quantatw.roomhub.manager.asset.manager.RoomHubData;
import com.quantatw.roomhub.manager.asset.manager.RoomHubManager;
import com.quantatw.roomhub.utils.GlobalDef;
import com.quantatw.roomhub.utils.PreferenceEditor;
import com.quantatw.roomhub.utils.Utils;
import com.quantatw.sls.key.ErrorKey;
import com.quantatw.sls.pack.homeAppliance.OnboardingReqPack;
import com.quantatw.sls.pack.homeAppliance.OnboardingResPack;

import java.util.List;

/**
 * Created by 95010915 on 2015/9/25.
 */
public class WifiAPSetup extends AbstractRoomHubActivity {
    private final String TAG=WifiAPSetup.class.getSimpleName();
    private String mSSID;
    private int mLevel;
    private String mPassword;
    private int mSecurity;
    private GlobalDef.PskType mPskType;
    private GlobalDef.WPA_WAP2_SUB_TYPE mSubType;

    private EditText txtPassword;
    private Context mContext;

    private int mType = GlobalDef.TYPE_DEFAULT;
    private String mCurUuid;

    private int mPasswordWPAMinLen = 8;
    private int mPasswordWPAMaxLen = 64;
    private int mPasswordWEP64Len = 5;
    private int mPasswordWEP128Len = 10;

    private int[] mPasswordWEPASCIILen = {5,13,16};

    private WifiManager mWifiManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wifi_ap_setup);

        mContext = this;

        mWifiTimeout = getResources().getInteger(R.integer.config_wifi_ap_connect_timeout);

        mWifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
        if (mType == GlobalDef.TYPE_AP_TRANSFER) {
            setTitle(R.string.change_wifi_now);
        } else {
            setTitle(R.string.wifi_list_title);
        }
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        mCurUuid = intent.getStringExtra(RoomHubManager.KEY_UUID);
        mType = intent.getIntExtra(GlobalDef.USE_TYPE, GlobalDef.TYPE_DEFAULT);
        processData(bundle);
    }

    private View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            finish();
        }
    };

    private void processData(Bundle bundle) {
        mSSID = bundle.getString(GlobalDef.WIFI_AP_SSID);
        mLevel = bundle.getInt(GlobalDef.WIFI_AP_LEVEL);
        mSecurity = bundle.getInt(GlobalDef.WIFI_AP_SECURITY);
        mPskType = (GlobalDef.PskType)bundle.getSerializable(GlobalDef.WIFI_AP_PSKTYPE);
        mSubType = (GlobalDef.WPA_WAP2_SUB_TYPE)bundle.getSerializable(GlobalDef.WIFI_AP_PSKSUBTYPE);

        TextView txtSSID = (TextView) findViewById(R.id.txtSSID);
        txtSSID.setText(mSSID);

        TextView txtTitleSignal = (TextView) findViewById(R.id.txtTitleSignal);

        TextView txtSignal = (TextView) findViewById(R.id.txtSignal);

        txtSignal.setText(Utils.getLevelString(this, mLevel));

        TextView txtTitleSecurity = (TextView) findViewById(R.id.txtTitleSecurity);

        TextView txtSecurity = (TextView) findViewById(R.id.txtSecurity);

        txtSecurity.setText(Utils.getSecurityString(this, false, mSecurity, mPskType));

        TextView txtTitlePass = (TextView) findViewById(R.id.txtTitlePass);
        CheckBox chkShowPass = (CheckBox) findViewById(R.id.chkShowPass);

        txtPassword = (EditText) findViewById(R.id.wifiPwd);
        if(mSecurity == GlobalDef.SECURITY_NONE) {
            txtPassword.setVisibility(View.INVISIBLE);
            txtTitlePass.setVisibility(View.INVISIBLE);
            chkShowPass.setVisibility(View.INVISIBLE);
        } else {
            txtPassword.setVisibility(View.VISIBLE);
            txtTitlePass.setVisibility(View.VISIBLE);
            chkShowPass.setVisibility(View.VISIBLE);
        }

        chkShowPass.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked == true) {
                    txtPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
                } else {
                    txtPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
                }
                txtPassword.setSelection(txtPassword.getText().length());
            }
        });

        Button btnConfirm = (Button) findViewById(R.id.btnConfirm);
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(txtPassword.getVisibility() == View.VISIBLE) {
                    if(CheckPassword() == false)
                        return;
                }
                if(mType == GlobalDef.TYPE_AP_TRANSFER){
                    showConfirmChangeWiFiDialog();
//                    mWifiApHandler.sendEmptyMessage(MSG_CHANGE_WIFI);
                }else {
                    Message msg = new Message();
                    msg.what = MSG_DO_CONNECT;
                    mWifiApHandler.sendMessage(msg);
                }
            }
        });

        if(mType == GlobalDef.TYPE_AP_TRANSFER) {
            Button btnCancel = (Button)findViewById(R.id.btnCancel);
            btnCancel.setVisibility(View.VISIBLE);
            btnCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }
    }

    private boolean CheckPassword() {
        int len = txtPassword.getText().length();
        if(len == 0) {
            Toast.makeText(mContext, getString(R.string.wifi_password_empty_wrong_msg), Toast.LENGTH_SHORT).show();
            return false;
        }

        if(len > 64) {
            Toast.makeText(mContext,R.string.wifi_password_max_length_msg,Toast.LENGTH_SHORT).show();
            return false;
        }

        /*
        if(mSecurity == GlobalDef.SECURITY_PSK) { // WPA, WPA2
            if (len < mPasswordWPAMinLen) {
                Toast.makeText(mContext, getString(R.string.wifi_password_len_wrong_msg), Toast.LENGTH_SHORT).show();
                return false;
            }

            if (len > mPasswordWPAMaxLen) {
                Toast.makeText(mContext, getString(R.string.wifi_password_len_wrong_msg), Toast.LENGTH_SHORT).show();
                txtPassword.getText().delete(len - 1, len);
                return false;
            }
        } else if(mSecurity == GlobalDef.SECURITY_WEP) {
            int count = 0;
            for(int i:mPasswordWEPASCIILen) {
                if(len == i)
                    count++;
            }
            if(count == 0) {
            //if(len != mPasswordWEP64Len || len != mPasswordWEP128Len) {
                Toast.makeText(mContext, getString(R.string.wifi_password_wep_len_wrong_msg), Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        */
        return true;
    }

    private final static int MSG_DO_CONNECT = 1030001;
    private final static int MSG_DO_CHECK_CONNECTED = 1030002;
    private final static int MSG_DO_NEXT_ACTIVITY = 1030003;
    private final static int MSG_DO_CONNECT_FAIL = 1030004;
    private final static int MSG_CHANGE_WIFI = 1030005;

    private int mWifiTimeout;

    private Handler mWifiApHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_DO_CONNECT:
                    showProgressDialog("", getString(R.string.process_str));
                    if(checkWifiAP() == false) {
                        doConnectWifiAP();
                    }
                    break;
                case MSG_DO_CHECK_CONNECTED:
                    waitWifiConnected();
                    break;
                case MSG_DO_CONNECT_FAIL:
                    dismissProgressDialog(10, DIALOG_SHOW_NOTHING, ErrorKey.Success);
                    showConnectErrorDialog();
                    break;
                case MSG_DO_NEXT_ACTIVITY:
                    setWifiApData();
                    dismissProgressDialog(10, DIALOG_SHOW_NOTHING, ErrorKey.Success);
                    /*
                    * Note: Redirect to SettingActivity(Caller) and skip on boarding process.
                     */
                    /* 20160215: no need to go back to SettingActivity since wifi setup has been removed from Setting
                    if(Utils.getProvision(mContext) == GlobalDef.PROVISION_SET) {
                        Intent sendIntent = new Intent(mContext,SettingsActivity.class);
                        sendIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                        startActivity(sendIntent);
                        finish();
                        return;
                    }
                    */
                    LoadOnboardingPage();
                    break;
                case MSG_CHANGE_WIFI:
                    showProgressDialog("", getString(R.string.process_str));
                    OnboardingReqPack reqPack = new OnboardingReqPack();
                    reqPack.setSsid(mSSID);
                    reqPack.setPassword(mPassword);
                    //TODO unknown mSecurity mPskType is not ready
                    reqPack.setAuthorise(mPskType.ordinal());
                    reqPack.setEncrypt(mSecurity);
                    RoomHubData mData =getRoomHubManager().getRoomHubDataByUuid(mCurUuid);
                    OnboardingResPack resPack = mData.getRoomHubDevice().setOnboarding(reqPack);
                    if(resPack != null) {
                        if(resPack.getStatus_code() == ErrorKey.Success && resPack.getResult() >= 0) {
                            finish();
                        }
                        else if(resPack.getStatus_code() == -1 || resPack.getResult() == -1) {
                            showChangeFailDialog();
                        }
                    }
                    dismissProgressDialog(10, DIALOG_SHOW_NOTHING, ErrorKey.Success);

                    break;
            }
        }
    };

    private boolean checkWifiAP() {
        if(Build.VERSION.SDK_INT >= 23) {
            showConfirmWiFiDialog();
            return true;
        }
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (networkInfo.isConnected()) {
            WifiInfo connectionInfo = mWifiManager.getConnectionInfo();
            if (connectionInfo != null) {
                String ssid = connectionInfo.getSSID();
                ssid = ssid.replaceAll("\"", "");
                if (mSSID.equals(ssid)) {
                    showSameWiFiDialog();
                    return true;
                }
            }
        }
        return false;
    }

    private void waitWifiConnected() {
        final int sec = mWifiTimeout / 1000;

        new Thread() {
            public void run() {
                int cnt = 0;
                boolean bConnect = false;
                while (cnt < sec) {
                    ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo networkInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                    if (networkInfo.isConnected()) {
                        WifiInfo connectionInfo = mWifiManager.getConnectionInfo();
                        if (connectionInfo != null) {
                            String ssid = connectionInfo.getSSID();
                            ssid = ssid.replaceAll("\"", "");
                            if (mSSID.equals(ssid)) {
                                Message msg = new Message();
                                msg.what = MSG_DO_NEXT_ACTIVITY;
                                mWifiApHandler.sendMessage(msg);
                                bConnect = true;
                                break;
                            }
                        }
                    }
                    try {
                        sleep(1000);
                        cnt++;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if(!bConnect) {
                    Message msg = new Message();
                    msg.what = MSG_DO_CONNECT_FAIL;
                    mWifiApHandler.sendMessage(msg);
                }
            }
        }.start();
    }

    private void doConnectWifiAP() {
        boolean shouldUpdate = false;

        WifiConfiguration wfc = findConfiguration(mSSID);
        if(wfc != null)
            shouldUpdate = true;
        else
            wfc = new WifiConfiguration();

        wfc.SSID = "\"".concat(mSSID).concat("\"");
        wfc.status = WifiConfiguration.Status.DISABLED;
        wfc.priority = 40;

        if(mSecurity == GlobalDef.SECURITY_NONE) {
            wfc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            wfc.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
            wfc.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            wfc.allowedAuthAlgorithms.clear();
            wfc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            wfc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
            wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
        } else if(mSecurity == GlobalDef.SECURITY_WEP) {
            wfc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            wfc.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
            wfc.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            wfc.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            wfc.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
            wfc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            wfc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
            wfc.wepTxKeyIndex = 0;

            String password = txtPassword.getText().toString();
            // check the validity of a WEP password
            Pair<Boolean, Boolean> wepCheckResult = checkWEPPassword(password);
            if (!wepCheckResult.first) {
                Log.i(TAG, "lollipop_connectToWifiAP  auth type = WEP: password " + password + " invalid length or charecters");
                return;
            }
            Log.i(TAG, "lollipop_connectToWifiAP [WEP] using " + (!wepCheckResult.second ? "ASCII" : "HEX"));
            if (!wepCheckResult.second) {
                wfc.wepKeys[0] = "\"" + password + "\"";
            } else {
                wfc.wepKeys[0] = password;
            }
        } else if(mSecurity == GlobalDef.SECURITY_PSK) {
            wfc.status = WifiConfiguration.Status.ENABLED;
            wfc.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
            wfc.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            wfc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            wfc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            wfc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
//            wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
//            wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
            wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);

            wfc.preSharedKey = "\"".concat(txtPassword.getText().toString()).concat("\"");
        }

        int networkId = 0;
        if(shouldUpdate)
            networkId = mWifiManager.updateNetwork(wfc);
        else
            networkId = mWifiManager.addNetwork(wfc);
        if (networkId == -1) {
            Message msg = new Message();
            msg.what = MSG_DO_CONNECT_FAIL;
            mWifiApHandler.sendMessage(msg);
        } else {
            boolean res = mWifiManager.saveConfiguration();
            List<WifiConfiguration> list = mWifiManager.getConfiguredNetworks();
            for( WifiConfiguration i : list ) {
                if(i.SSID != null && i.SSID.equals("\"" + mSSID + "\"")) {
                    mWifiManager.disconnect();
                    mWifiManager.enableNetwork(i.networkId, true);
                    mWifiManager.reconnect();

                    Message msg = new Message();
                    msg.what = MSG_DO_CHECK_CONNECTED;
                    mWifiApHandler.sendMessage(msg);

                    break;
                }
            }
        }
    }

    private final String WEP_HEX_PATTERN = "[\\dA-Fa-f]+";
    private Pair<Boolean, Boolean> checkWEPPassword(String password) {
//        Log.d(TAG, "checkWEPPassword");
        if (password == null || password.isEmpty()) {
            Log.w(TAG, "checkWEPPassword empty password");
            return new Pair<Boolean, Boolean>(false, false);
        }

        int length = password.length();
        switch (length) {
            // valid ASCII keys length
            case 5:
            case 13:
            case 16:
            case 29:
                Log.d(TAG, "checkWEPPassword valid WEP ASCII password");
                return new Pair<Boolean, Boolean>(true, false);
            // valid hex keys length
            case 10:
            case 26:
            case 32:
            case 58:
                if (password.matches(WEP_HEX_PATTERN)) {
                    Log.d(TAG, "checkWEPPassword valid WEP password length, and HEX pattern match");
                    return new Pair<Boolean, Boolean>(true, true);
                }
                Log.w(TAG, "checkWEPPassword valid WEP password length, but HEX pattern matching failed: " + WEP_HEX_PATTERN);
                return new Pair<Boolean, Boolean>(false, false);
            default:
                Log.w(TAG, "checkWEPPassword invalid WEP password length: " + length);
                return new Pair<Boolean, Boolean>(false, false);
        }
    }

    private LinearLayout.LayoutParams setMargin(int left, int right, int top, int bottom, LinearLayout.LayoutParams parms) {
        LinearLayout.LayoutParams marginParms = parms;
        marginParms.setMargins(left, top, right, bottom);

        return marginParms;
    }

    private RelativeLayout.LayoutParams setMargin(int left, int right, int top, int bottom, RelativeLayout.LayoutParams parms) {
        RelativeLayout.LayoutParams marginParms = parms;
        marginParms.setMargins(left, top, right, bottom);

        return marginParms;
    }

    private void LoadOnboardingPage() {
        Intent intent = new Intent(WifiAPSetup.this, OnBoardingActivity.class);
//        Intent intent = new Intent(WifiAPSetup.this, PreOnBoardingActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addCategory(Intent.CATEGORY_HOME);
        Bundle bundle = new Bundle();
        bundle.putString(GlobalDef.WIFI_AP_SSID, mSSID);
        bundle.putInt(GlobalDef.WIFI_AP_SECURITY, mSecurity);
        bundle.putSerializable(GlobalDef.WIFI_AP_PSKTYPE, mPskType);
        bundle.putString(GlobalDef.WIFI_AP_PASSWORD, txtPassword.getText().toString());
        intent.putExtras(bundle);
        startActivity(intent);
        //System.exit(0);
    }

    private void setWifiApData() {
        PreferenceEditor pref = new PreferenceEditor(this, GlobalDef.ROOMHUB_SETTINGS_PREFERENCE_NAME);
        pref.setStringValue(GlobalDef.ROOMHUB_SETTINGS_WIFI_SSID, mSSID);
        pref.setIntValue(GlobalDef.ROOMHUB_SETTINGS_WIFI_SECURITY, mSecurity);
        pref.setIntValue(GlobalDef.ROOMHUB_SETTINGS_WIFI_SECURITY_PSK, mPskType.ordinal());
        pref.setIntValue(GlobalDef.ROOMHUB_SETTINGS_WIFI_SECURITY_SUB_TYPE, mSubType.ordinal());
        pref.setStringValue(GlobalDef.ROOMHUB_SETTINGS_WIFI_PASSWORD, txtPassword.getText().toString());
    }

    private WifiConfiguration findConfiguration(String ssid) {
        // the configured Wi-Fi networks
        final List<WifiConfiguration> wifiConfigs = mWifiManager.getConfiguredNetworks();

        // for debugging purposes only log the list
        StringBuffer buff = new StringBuffer();
        for (WifiConfiguration w : wifiConfigs) {
            if (w!= null && w.SSID != null) {
                w.SSID = normalizeSSID(w.SSID);
                if (w.SSID.length() > 1) {
                    buff.append(w.SSID).append(",");
                }
            }
        }
//        Log.i(TAG, "connectToWifiAP ConfiguredNetworks " + (buff.length() > 0 ? buff.toString().substring(0, buff.length() - 1) : " empty"));

        // find any existing WifiConfiguration that has the same SSID as the
        // supplied one and return it if found
        for (WifiConfiguration w : wifiConfigs) {
            if (w != null && w.SSID != null && isSsidEquals(w.SSID, ssid)) {
//                Log.i(TAG, "connectToWifiAP found " + ssid + " in ConfiguredNetworks. networkId = " + w.networkId);
                return w;
            }
        }

        return null;
    }

    private String normalizeSSID(String ssid) {
        if (ssid != null && ssid.length() > 2 && ssid.startsWith("\"") && ssid.endsWith("\"")) {
            ssid = ssid.substring(1, ssid.length() -1);
        }
        return ssid;
    }

    private boolean isSsidEquals(String ssid1, String ssid2) {
        if (ssid1 == null || ssid1.length() == 0 || ssid2 == null || ssid2.length() == 0) {
            return false;
        }
        return normalizeSSID(ssid1).equals(normalizeSSID(ssid2));
    }

    private void showConnectErrorDialog() {
        final Dialog dialog = new Dialog(this,R.style.CustomDialog);

        dialog.setContentView(R.layout.custom_dialog);

        TextView txt_msg=(TextView)dialog.findViewById(R.id.txt_message);
        txt_msg.setText(getString(R.string.wifi_connect_fail));

        Button btn_yes = (Button)dialog.findViewById(R.id.btn_yes);
        btn_yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Message msg = new Message();
                msg.what = MSG_DO_NEXT_ACTIVITY;
                mWifiApHandler.sendMessage(msg);
            }
        });

        Button btn_no = (Button)dialog.findViewById(R.id.btn_no);
        btn_no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        dialog.setCancelable(false);
        dialog.show();
    }

    private void showSameWiFiDialog() {
        final Dialog dialog = new Dialog(this,R.style.CustomDialog);

        dialog.setContentView(R.layout.custom_dialog);

        TextView txt_msg=(TextView)dialog.findViewById(R.id.txt_message);
        txt_msg.setText(getString(R.string.wifi_connect_same_wifi));

        Button btn_yes = (Button)dialog.findViewById(R.id.btn_yes);
        btn_yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Message msg = new Message();
                msg.what = MSG_DO_NEXT_ACTIVITY;
                mWifiApHandler.sendMessage(msg);
            }
        });

        Button btn_no = (Button)dialog.findViewById(R.id.btn_no);
        btn_no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        dialog.setCancelable(false);
        dialog.show();
    }


    private void showConfirmWiFiDialog() {
        final Dialog dialog = new Dialog(this,R.style.CustomDialog);

        dialog.setContentView(R.layout.custom_dialog);

        TextView txt_msg=(TextView)dialog.findViewById(R.id.txt_message);
        txt_msg.setText(getString(R.string.wifi_prompt_skip_connect));

        Button btn_yes = (Button)dialog.findViewById(R.id.btn_yes);
        btn_yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                Message msg = new Message();
                msg.what = MSG_DO_NEXT_ACTIVITY;
                mWifiApHandler.sendMessage(msg);
            }
        });

        Button btn_no = (Button)dialog.findViewById(R.id.btn_no);
        btn_no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        dialog.setCancelable(false);
        dialog.show();
    }

    private void showConfirmChangeWiFiDialog() {
        final Dialog dialog = new Dialog(this,R.style.CustomDialog);

        dialog.setContentView(R.layout.custom_dialog);

        TextView txt_msg=(TextView)dialog.findViewById(R.id.txt_message);
        txt_msg.setText(getString(R.string.change_wifi_ap_confirm));

        Button btn_yes = (Button)dialog.findViewById(R.id.btn_yes);
        btn_yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                mPassword = txtPassword.getText().toString();
                setWifiApData();
                setResult(RESULT_OK);
                finish();
            }
        });

        Button btn_no = (Button)dialog.findViewById(R.id.btn_no);
        btn_no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        dialog.setCancelable(false);
        dialog.show();
    }

    private void showChangeFailDialog() {
        final Dialog dialog = new Dialog(this,R.style.CustomDialog);

        dialog.setContentView(R.layout.custom_dialog_center);

        TextView txt_msg=(TextView)dialog.findViewById(R.id.txt_message);
        txt_msg.setText(R.string.change_wifi_fail);

        Button btn_yes = (Button)dialog.findViewById(R.id.btn_yes);
        btn_yes.setText(R.string.ok);
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
}
