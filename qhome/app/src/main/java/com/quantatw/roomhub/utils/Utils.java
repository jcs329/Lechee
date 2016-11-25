package com.quantatw.roomhub.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Message;
import android.os.Parcelable;
import android.os.PowerManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.quantatw.myapplication.R;
import com.quantatw.roomhub.manager.ReminderData;
import com.quantatw.roomhub.manager.asset.manager.RoomHubData;
import com.quantatw.roomhub.ui.MainActivity;
import com.quantatw.roomhub.ui.RoomHubApplication;
import com.quantatw.sls.api.DeviceTypeConvertApi;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by 95010915 on 2015/9/9.
 */
public class Utils {
    private static final String TAG=Utils.class.getSimpleName();
    private static final String EMAIL_STR = "@";
    private static final String EMAIL_STR1 = ".";
    private static final int MIN_PHONE_NUMBER_LEN = 7;

    public static final String KEY_SKIP_AUTO_LOGIN = "KEY_SKIP_AUTO_LOGIN";

    public static LinkedHashMap<String, PowerManager.WakeLock> mWakeLocks = new LinkedHashMap<>();

    public static boolean CheckEmailFormat(String account) {
        if(account.contains(EMAIL_STR) && account.contains(EMAIL_STR1))
            return true;

        // phone number check
        /*if(isValidInteger(account) == true && account.length() > MIN_PHONE_NUMBER_LEN)
            return true;*/

        return false;
    }

    public static String trimSpace(String string) {
        if(!TextUtils.isEmpty(string))
            return string.replaceAll("\\s","");
        return string;
    }

    public static boolean CheckPasswordAvailable(String password) {
        //String regex = "^[a-z0-9A-Z]*";

        if(password.length() < 4)
            return false;
        /*
        if(password.matches(regex)) {
            return true;
        }
        */
        return true;
    }

    public static boolean isValidInteger(String value) {
        try {
            Integer val = Integer.valueOf(value);
            if (val != null)
                return true;
            else
                return false;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean CheckConfirmPwd(String pwd1, String pwd2) {
        if(pwd1.isEmpty() || pwd2.isEmpty()) return false;
        if(pwd1.length() == 0 || pwd2.length() == 0) return false;

        if(pwd2.equals(pwd1))
            return true;
        else
            return false;
    }

    public static String getErrorCodeString(Context context, int err_code) {
        String[] err_number, err_string;
        int index = -1;

        err_number = context.getResources().getStringArray(R.array.error_code_number);
        err_string = context.getResources().getStringArray(R.array.error_code_string);

        for(int i = 0; i < err_number.length; i++) {
            if(Integer.parseInt(err_number[i]) == err_code) {
                index = i;
                break;
            }
        }

        if(index == -1) return getUnknownMessage(context);

        return err_string[index];
    }

    private static String getUnknownMessage(Context context) {
        String[] err_number, err_string;

        err_number = context.getResources().getStringArray(R.array.error_code_number);
        err_string = context.getResources().getStringArray(R.array.error_code_string);

        for(int i = 0; i < err_number.length; i++) {
            if (Integer.parseInt(err_number[i]) == -999) {
                return err_string[i];
            }
        }
        return null;
    }

    public static int getProvision(Context context) {
        PreferenceEditor pref = new PreferenceEditor(context, GlobalDef.ROOMHUB_SETTINGS_PREFERENCE_NAME);
        return pref.getIntValue(GlobalDef.ROOMHUB_SETTINGS_PROVISION);
    }

    public static boolean isProvisioned(Context context) {
        if(getProvision(context) == GlobalDef.PROVISION_SET)
            return true;
        return false;
    }

    public static boolean isShowWelcome(Context context) {
        PreferenceEditor pref = new PreferenceEditor(context, GlobalDef.ROOMHUB_SETTINGS_PREFERENCE_NAME);
        int val = pref.getIntValue(GlobalDef.ROOMHUB_SETTINGS_SHOW_WELCOME);
        return (pref.getIntValue(GlobalDef.ROOMHUB_SETTINGS_SHOW_WELCOME)==0)?false:true;
    }

    public static void setShowWelcome(Context context) {
        PreferenceEditor pref = new PreferenceEditor(context, GlobalDef.ROOMHUB_SETTINGS_PREFERENCE_NAME);
        pref.setIntValue(GlobalDef.ROOMHUB_SETTINGS_SHOW_WELCOME,1);
    }

    public static String getLevelString(Context context, int level) {
        String[] levels = context.getResources().getStringArray(R.array.wifi_signal);
        return levels[level];
    }

    public static String getSecurityString(Context context, boolean concise, int security, GlobalDef.PskType pskType) {
        switch(security) {
            case GlobalDef.SECURITY_EAP:
                return concise ? context.getString(R.string.wifi_security_short_eap) :
                        context.getString(R.string.wifi_security_eap);
            case GlobalDef.SECURITY_PSK:
                switch (pskType) {
                    case WPA:
                        return concise ? context.getString(R.string.wifi_security_short_wpa) :
                                context.getString(R.string.wifi_security_wpa);
                    case WPA2:
                        return concise ? context.getString(R.string.wifi_security_short_wpa2) :
                                context.getString(R.string.wifi_security_wpa2);
                    case WPA_WPA2:
                        return concise ? context.getString(R.string.wifi_security_short_wpa_wpa2) :
                                context.getString(R.string.wifi_security_wpa_wpa2);
                    case UNKNOWN:
                    default:
                        return concise ? context.getString(R.string.wifi_security_short_psk_generic)
                                : context.getString(R.string.wifi_security_psk_generic);
                }
            case GlobalDef.SECURITY_WEP:
                return concise ? context.getString(R.string.wifi_security_short_wep) :
                        context.getString(R.string.wifi_security_wep);
            case GlobalDef.SECURITY_NONE:
            default:
                return concise ? "" : context.getString(R.string.wifi_security_none);
        }
    }

    public static void ShowLoginActivity(final Context context, final Class<?> nextActivityClass) {
        final Dialog dialog = new Dialog(context,R.style.CustomDialog);//指定自定義樣式

        dialog.setContentView(R.layout.custom_dialog);

        TextView txt_msg=(TextView)dialog.findViewById(R.id.txt_message);
        txt_msg.setText(context.getString(R.string.need_login_msg));

        Button btn_yes = (Button)dialog.findViewById(R.id.btn_yes);
        btn_yes.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                ((Activity)context).finish();

                Intent intent = new Intent(context, MainActivity.class);
                intent.addCategory(Intent.CATEGORY_LAUNCHER);
                intent.putExtra(KEY_SKIP_AUTO_LOGIN,true);
//                Intent intent = new Intent(context, MainActivity.class);
//                intent.putExtra(KEY_SKIP_AUTO_LOGIN,true);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                /*
                Intent intent = new Intent(context, LoginActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("Activity", nextActivityClass);
                intent.putExtras(bundle);
                */
                context.startActivity(intent);
                dialog.dismiss();
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

    public static boolean isHexString(String str) {
        if (str == null) {
            return false;
        }
        return Pattern.matches("^[0-9a-fA-F]++$", str);
    }

    public static int getCurrentTempUnit(Context context) {
        PreferenceEditor editor = new PreferenceEditor(context, GlobalDef.ROOMHUB_SETTINGS_PREFERENCE_NAME);
        return editor.getIntValue(GlobalDef.ROOMHUB_SETTINGS_TEMP_UNIT);
    }

    public static double toFahrenheit(double celsius) {
        return (celsius*9/5+32)+0.5;
    }

    public static double toCelsius(double fahrenheit) {
        return ((fahrenheit-32)*5/9)+0.5;
    }

    public static boolean isNotificationOn(Context context) {
        PreferenceEditor editor = new PreferenceEditor(context, GlobalDef.ROOMHUB_SETTINGS_PREFERENCE_NAME);
        String str = editor.getStringValue(GlobalDef.ROOMHUB_SETTINGS_NOTIFICATION);
        if(TextUtils.isEmpty(str))
            editor.setStringValue(GlobalDef.ROOMHUB_SETTINGS_NOTIFICATION,Boolean.toString(true));
        return Boolean.valueOf(editor.getStringValue(GlobalDef.ROOMHUB_SETTINGS_NOTIFICATION));
    }

    public static int getNotificationTime(Context context) {
        PreferenceEditor editor = new PreferenceEditor(context, GlobalDef.ROOMHUB_SETTINGS_PREFERENCE_NAME);
        return editor.getIntValue(GlobalDef.ROOMHUB_SETTINGS_NOTIFICATION_TIME);
    }

    public static int getNotificationDelta(Context context) {
        PreferenceEditor editor = new PreferenceEditor(context, GlobalDef.ROOMHUB_SETTINGS_PREFERENCE_NAME);
        return editor.getIntValue(GlobalDef.ROOMHUB_SETTINGS_NOTIFICATION_DELTA);
    }
    public static boolean isLocateMeOn(Context context) {
        PreferenceEditor editor = new PreferenceEditor(context, GlobalDef.ROOMHUB_SETTINGS_PREFERENCE_NAME);
        String str = editor.getStringValue(GlobalDef.ROOMHUB_SETTINGS_LOCATE_ME);
        if(TextUtils.isEmpty(str))
            editor.setStringValue(GlobalDef.ROOMHUB_SETTINGS_LOCATE_ME,Boolean.toString(false));
        return Boolean.valueOf(editor.getStringValue(GlobalDef.ROOMHUB_SETTINGS_LOCATE_ME));
    }

    public static boolean isNewsletterOn(Context context) {
        PreferenceEditor editor =
                new PreferenceEditor(context, GlobalDef.ROOMHUB_SETTINGS_PREFERENCE_NAME);
        String str = editor.getStringValue(GlobalDef.ROOMHUB_SETTINGS_NEWSLETTER);
        if(TextUtils.isEmpty(str))
            editor.setStringValue(GlobalDef.ROOMHUB_SETTINGS_NEWSLETTER,Boolean.toString(true));
        return Boolean.valueOf(editor.getStringValue(GlobalDef.ROOMHUB_SETTINGS_NEWSLETTER));
    }

    public static boolean isCelsius(Context context) {
        if(getCurrentTempUnit(context) == GlobalDef.TEMP_UNIT_CELSIUS)
            return true;
        return false;
    }

    public static boolean isFahrenheit(Context context) {
        if(getCurrentTempUnit(context) == GlobalDef.TEMP_UNIT_FRHRENHEIT)
            return true;
        return false;
    }

    public static double getTemp(Context context,double celsius){
        double temp=celsius;

        if(getCurrentTempUnit(context) == GlobalDef.TEMP_UNIT_FRHRENHEIT)
            temp=toFahrenheit(temp);

        return temp;
    }

    public static double getTempToCelsius(Context context,double fahrenheit){
        double temp=fahrenheit;

        if(getCurrentTempUnit(context) == GlobalDef.TEMP_UNIT_FRHRENHEIT)
            temp=toCelsius(temp);

        return temp;
    }

    public static byte[] intToByteArray(int a)
    {
        byte[] ret = new byte[4];
        ret[3] = (byte) (a & 0xFF);
        ret[2] = (byte) ((a >> 8) & 0xFF);
        ret[1] = (byte) ((a >> 16) & 0xFF);
        ret[0] = (byte) ((a >> 24) & 0xFF);
        return ret;
    }

    public static void sendReminderMessage(Context context, ReminderData reminderData) {
        Intent intent = new Intent(GlobalDef.ACTION_REMINDER);
        intent.putExtra(GlobalDef.REMINDER_MESSAGE, (Parcelable)reminderData);
        context.sendBroadcast(intent);
    }

    public static boolean isRoomHubAppForeground(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            final int PROCESS_STATE_TOP = 2;
            ActivityManager.RunningAppProcessInfo currentInfo = null;
            Field field = null;
            try {
                field = ActivityManager.RunningAppProcessInfo.class.getDeclaredField("processState");
            } catch (Exception ignored) {
            }

            List<ActivityManager.RunningAppProcessInfo> appList = am.getRunningAppProcesses();
            if(appList != null) {
                for (ActivityManager.RunningAppProcessInfo app : appList) {
                    if (app.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
                            && app.importanceReasonCode == ActivityManager.RunningAppProcessInfo.REASON_UNKNOWN) {
                        Integer state = null;
                        try {
                            state = field.getInt(app);
                        } catch (Exception e) {
                        }
                        if (state != null && state == PROCESS_STATE_TOP) {
                            currentInfo = app;
                            break;
                        }
                    }
                }
            }
            if(currentInfo != null) {
                String processName = currentInfo.processName;
                //log("processName="+processName+",packageName="+getPackageName());
                if(processName.equals(context.getPackageName()))
                    return true;
            }
        }
        else {

            try {
                ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
                // The first in the list of RunningTasks is always the foreground
                // task.
                List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
                if (taskInfo != null && taskInfo.size() >= 1) {
                    ActivityManager.RunningTaskInfo foregroundTaskInfo = taskInfo.get(0);
                    String foregroundTaskPackageName = foregroundTaskInfo.topActivity
                            .getPackageName();// get the top fore ground activity
                    PackageManager pm = context.getPackageManager();
                    PackageInfo foregroundAppPackageInfo = pm.getPackageInfo(
                            foregroundTaskPackageName, 0);

                    if (foregroundAppPackageInfo != null) {
//                        log("foregroundAppPackageInfo=" + foregroundAppPackageInfo + ",applicationInfo=" +
//                                        foregroundAppPackageInfo.applicationInfo
//                        );
                        if (foregroundAppPackageInfo.applicationInfo != null) {
                            String foregroundTaskAppName = foregroundAppPackageInfo.applicationInfo
                                    .loadLabel(pm).toString();
//                            log("foregroundTaskAppName=" + foregroundTaskAppName);
                            if (!TextUtils.isEmpty(foregroundTaskAppName) &&
                                    foregroundTaskAppName.equals(context.getResources().getString(R.string.app_name))) {
                                return true;
                            }
                        }
                    }
                }
            } catch (PackageManager.NameNotFoundException e) {
                return false;
                //e.printStackTrace();
            }

        }
        return false;
    }

    public static void setIsOnBoarding(Context context, boolean isOnBoarding) {
        PreferenceEditor pref = new PreferenceEditor(context, GlobalDef.ROOMHUB_SETTINGS_PREFERENCE_NAME);
        pref.setIntValue(GlobalDef.ROOMHUB_IS_ONBOARDING, isOnBoarding == true ? 1 : 0);
    }

    public static boolean isRoomHubDoingOnBoarding(Context context) {
        PreferenceEditor pref = new PreferenceEditor(context, GlobalDef.ROOMHUB_SETTINGS_PREFERENCE_NAME);
        int val = pref.getIntValue(GlobalDef.ROOMHUB_IS_ONBOARDING);
        return val==1?true:false;
    }

    public static boolean needToBindWifiConnection(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        if (wifiManager.isWifiEnabled()) {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            String ssid = wifiInfo.getSSID();
            ssid = ssid.replaceAll("\"", "");
            if (TextUtils.isEmpty(ssid) ||
                    ssid.equals("<unknown ssid>") || ssid.equals("0x")) {
                // notify user to connect Wifi
                return true;
            }
        }

        return false;
    }

    public static boolean isWifiConnectWithPrefix(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        WifiManager wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        if (activeNetworkInfo != null
                && activeNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI
                && activeNetworkInfo.isConnected()) {

            WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifi.getConnectionInfo();
            String ssid;

            if (wifiInfo == null)
                return false;

            ssid = wifiInfo.getSSID();
            if (!TextUtils.isEmpty(ssid) && (ssid.indexOf(context.getString(R.string.config_onboardee_ssid_prefix)) > 0)) {
                return true;
            }
        }
        else {
            if(Build.VERSION.SDK_INT >= 21) {
                Network[] networks = connectivityManager.getAllNetworks();
                for (Network network : networks) {
                    NetworkInfo networkInfo = connectivityManager.getNetworkInfo(network);
                    if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                        String ssid = networkInfo.getExtraInfo();
                        if (networkInfo.isConnected() && !TextUtils.isEmpty(ssid)) {
                            ssid = ssid.replaceAll("\"", "");
                            if (!TextUtils.isEmpty(ssid) && ssid.startsWith(context.getString(R.string.config_onboardee_ssid_prefix))) {
                                return true;
                            }
                        }
                        break;
                    }
                }
            }
        }

        return false;
    }

    public static boolean isWiFiInternetAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if(Build.VERSION.SDK_INT >= 21) {
            Network[] networks = connectivityManager.getAllNetworks();
            for (Network network : networks) {
                NetworkInfo networkInfo = connectivityManager.getNetworkInfo(network);
                if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                    NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(network);
                    if (networkCapabilities != null && networkCapabilities.hasCapability(networkCapabilities.NET_CAPABILITY_VALIDATED)) {
                        return true;
                        // detect by opening connection may cause ANR
//                        return hasActiveInternetConnection(context);

                    }
                }
            }
        }
        return false;
    }

    public static void showWifiConnectionAlertDialog(final Context context) {
        final Dialog dialog = new Dialog(context,R.style.CustomDialog);

        dialog.setContentView(R.layout.custom_dialog_onboarding_comfirm);

        TextView txt_msg=(TextView)dialog.findViewById(R.id.txt_message);
        txt_msg.setText(context.getString(R.string.wrong_wifi_connected_msg));

        Button btn_yes = (Button)dialog.findViewById(R.id.btn_yes);
        btn_yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                /*
                * add moveTaskToBack could prevent from crashed in previous Activity onCreate
                 */
                ((Activity)context).moveTaskToBack(true);
                System.exit(0);
            }
        });

        Button btn_no = (Button)dialog.findViewById(R.id.btn_no);
        btn_no.setVisibility(View.GONE);
        dialog.setCancelable(false);
        dialog.show();
    }

    public static String getCurrentActivityName(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Activity.ACTIVITY_SERVICE);

        // get the info from the currently running task
        List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);

        ComponentName componentInfo = taskInfo.get(0).topActivity;
        return componentInfo.getClassName();
    }

    public static boolean isVoiceCapable(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        boolean voiceCapable = false;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            voiceCapable = telephonyManager.isVoiceCapable();
        else
            voiceCapable = context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_TELEPHONY);
        return voiceCapable;
    }

    public static boolean hasIccCard(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        return telephonyManager.hasIccCard();
    }

    public static boolean isMobileDataEnabled(Context context) {
        boolean enable = false;
        boolean voiceCapable = isVoiceCapable(context);
        if(voiceCapable && hasIccCard(context)) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
                enable = Settings.Global.getInt(context.getContentResolver(), "mobile_data", 1) == 1;
            } else {
                enable = Settings.Secure.getInt(context.getContentResolver(), "mobile_data", 1) == 1;
            }
        }
        return enable;
    }

    public static void setPromptDisableMobileData(Context context, boolean enable) {
        PreferenceEditor pref = new PreferenceEditor(context, GlobalDef.ROOMHUB_SETTINGS_PREFERENCE_NAME);
        pref.setIntValue(GlobalDef.ROOMHUB_PROMPT_DISABLE_MOBILE_DATA, enable == true ? 1 : 0);
    }

    public static boolean hasPromptDisableMobileData(Context context) {
        // for Android 6.0 Marshmallow
        if(Build.VERSION.SDK_INT >= 23) {
            PreferenceEditor pref = new PreferenceEditor(context, GlobalDef.ROOMHUB_SETTINGS_PREFERENCE_NAME);
            return pref.getIntValue(GlobalDef.ROOMHUB_PROMPT_DISABLE_MOBILE_DATA) == 1 ? true : false;
        }
        return false;
    }

    public static void acquireWakeLock(Context context, String tag) {
        PowerManager.WakeLock wakeLock = mWakeLocks.get(tag);
        if(wakeLock != null && wakeLock.isHeld()) {
            Log.d(TAG,"acquireWakeLock() this Tag("+tag+") WakeLock is Held!!!");
            return;
        }
        Log.d(TAG, "acquireWakeLock() ["+tag+"]");
        PowerManager powerManager = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,tag);
        wakeLock.acquire();
        mWakeLocks.put(tag, wakeLock);
    }

    public static void releaseWakeLock(String tag) {
        PowerManager.WakeLock wakeLock = mWakeLocks.get(tag);
        if(wakeLock == null) {
            Log.d(TAG,"releaseWakeLock() this WakeLock("+tag+") is not found is list");
            return;
        }
        if(wakeLock.isHeld())
            wakeLock.release();
        Log.d(TAG, "releaseWakeLock() [" + tag + "]");
        mWakeLocks.remove(tag);
    }

    public static void acquireIRPairingWakeLock(Context context) {
        acquireWakeLock(context, GlobalDef.WAKELOCK_IR_PAIRING_TAG);
    }

    public static void releaseIRPairingWakeLock() {
        releaseWakeLock(GlobalDef.WAKELOCK_IR_PAIRING_TAG);
    }

    public static void acquireOnBoardingWakeLock(Context context) {
        acquireWakeLock(context, GlobalDef.WAKELOCK_ONBOARDING_TAG);
    }

    public static void releaseOnBoardingWakeLock() {
        releaseWakeLock(GlobalDef.WAKELOCK_ONBOARDING_TAG);
    }

    /*
    * ignoreSameVersion:
    * if true means return false when compared two same version
     */
    public static boolean checkFirmwareVersion(String versionNow,String versionCompare,boolean ignoreSameVersion) {

        if(TextUtils.isEmpty(versionNow) || TextUtils.isEmpty(versionCompare))
            return false;

        if(versionNow.equals(versionCompare)) {
            if(ignoreSameVersion)
                return false;
            return true;
        }

        String[] versionArray  = validateVersionFormat(versionNow);
        String[] compareArray = validateVersionFormat(versionCompare);

        if (Integer.parseInt(versionArray[0]) > Integer.parseInt(compareArray[0])) {
            return true;
        }

        if (Integer.parseInt(versionArray[0]) < Integer.parseInt(compareArray[0])) {
            return false;
        }

        for(int i=0;i<versionArray.length;i++) {
            if(getVersion(versionArray[i]) > getVersion(compareArray[i]))
                return true;
            else {
                if(getVersion(versionArray[i]) < getVersion(compareArray[i]))
                    return false;
            }

            /*
            if (Float.parseFloat("0." + versionArray[i]) > Float.parseFloat("0." + compareArray[i])) {
                return true;
            }
            if (Float.parseFloat("0." + versionArray[i]) < Float.parseFloat("0." + compareArray[i])) {
                return false;
            }
            */
        }

        return false;
    }

    public static void setGcmRegistration(Context context, boolean registration) {
        PreferenceEditor preferenceEditor = new PreferenceEditor(context, GlobalDef.ROOMHUB_SETTINGS_PREFERENCE_NAME);
        preferenceEditor.setIntValue(GlobalDef.ROOMHUB_SETTINGS_GCM_REGISTRATION, registration == true ? 1 : 0);
        Log.d(TAG,"setGcmRegistration : "+registration);
    }

    public static boolean isGcmRegistered(Context context) {
        PreferenceEditor preferenceEditor = new PreferenceEditor(context, GlobalDef.ROOMHUB_SETTINGS_PREFERENCE_NAME);
        return preferenceEditor.getIntValue(GlobalDef.ROOMHUB_SETTINGS_GCM_REGISTRATION)==1?true:false;
    }

    public static boolean isGcmMessageId(int id) {
        if(id == GlobalDef.GCM_MESSAGE_ID || id == GlobalDef.GCM_MESSAGE_ID_SYSTEM)
            return true;
        return false;
    }

    public static int getGcmTitle(int id) {
        if(id == GlobalDef.GCM_MESSAGE_ID)
            return R.string.gcm_message_title;
        if(id == GlobalDef.GCM_MESSAGE_ID_SYSTEM)
            return R.string.sys_gcm_message_title;
        return 0;
    }

    public static boolean bindProcessToWiFiNetwork(Context context, boolean bind) {
        boolean connected = false;
        if(Build.VERSION.SDK_INT >= 23)
//        if(false)
        {
//            Log.d(TAG, "bindProcessToNetwork enter");
            ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

            if(bind) {
                Network[] net_all = connMgr.getAllNetworks();
                for (Network network : net_all) {
                    NetworkInfo net_info = connMgr.getNetworkInfo(network);
                    if (net_info != null && net_info.getType() == ConnectivityManager.TYPE_WIFI) {
                        connected = connMgr.bindProcessToNetwork(network);
                        Log.d(TAG, "bindProcessToNetwork is " + (connected ? "TRUE" : "FALSE"));
                        break;
                    }
                }
            }
            else {
//                Network boundNetwork = connMgr.getBoundNetworkForProcess();
                connMgr.bindProcessToNetwork(null);
                Log.d(TAG, "bindProcessToNetwork unbind");
            }
        }
        return connected;
    }

    public static void showConnectWifiDialog(final Context context, Message replyMessage) {
        Message message = Message.obtain(null,GlobalDef.PROMPT_USER_CONNECT_WIFI, replyMessage);
        ((RoomHubApplication) context.getApplicationContext()).sendToServiceHandler(message);
    }

    public static int getCategoryStringResource(int category) {
        switch (category) {
            case DeviceTypeConvertApi.CATEGORY.ROOMHUB:
                return R.string.manager_device;
            case DeviceTypeConvertApi.CATEGORY.SECURITY:
            case DeviceTypeConvertApi.CATEGORY.ENVIRONMENT:
            case DeviceTypeConvertApi.CATEGORY.HEALTH:
                return R.string.manage_all_devices;
        }
        return -1;
    }

    public static String getCategoryTitleString(Context context, int category) {
        int string_resource = getCategoryStringResource(category);
        if(string_resource > 0) {
            switch (category) {
                case DeviceTypeConvertApi.CATEGORY.HEALTH:
                    return context.getString(string_resource,context.getString(R.string.healthcare));
                case DeviceTypeConvertApi.CATEGORY.SECURITY:
                case DeviceTypeConvertApi.CATEGORY.ENVIRONMENT:
                    // TODO: security/environment
                    return context.getString(R.string.manager_device);
            }
        }
        return "";
    }

    public static boolean isAllowToAddHealthcareDevice(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        if(activeNetworkInfo != null) {
            if(activeNetworkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                Toast.makeText(context, R.string.roomhub_warning_msg, Toast.LENGTH_SHORT).show();
                return false;
            }
            else if(activeNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                if(hasAllJoynDevices(context)) {
                    return true;
                }
                else {
                    Toast.makeText(context, R.string.add_healthcare_device_warning, Toast.LENGTH_SHORT).show();
                    return false;
                }
            }
        }
        return false;
    }

    public static boolean hasAllJoynDevices(Context context) {
        ArrayList<RoomHubData> list = ((RoomHubApplication)context.getApplicationContext()).getRoomHubManager().getRoomHubDataList(false);
        if(list != null && list.size() > 0) {
            return true;
        }
        return false;
    }

    private static boolean hasActiveInternetConnection(Context context) {
        try {
            HttpURLConnection urlc = (HttpURLConnection) (new URL("http://www.google.com").openConnection());
            urlc.setRequestProperty("User-Agent", "Test");
            urlc.setRequestProperty("Connection", "close");
            urlc.setConnectTimeout(1500);
            urlc.connect();
            return (urlc.getResponseCode() == 200);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            return false;
        }
    }

    private static String[] validateVersionFormat(String source) {
        String[] versions = source.split("\\.");
        if(versions.length < 4) {
            int appendNum = 4-versions.length;
            for(int i=0;i<appendNum;i++) {
                source=source+".0";
            }
            versions = source.split("\\.");
        }
        return versions;
    }

    private static float getVersion(String string) {
        float version;
        if(string.length() > 1 && Integer.parseInt(string) >= 10)
            version = (float)((Integer.parseInt(string))*0.1);
        else
            version = Float.parseFloat("0." + string);
        return version;
    }
}
