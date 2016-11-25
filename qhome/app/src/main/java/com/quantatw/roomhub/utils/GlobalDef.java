package com.quantatw.roomhub.utils;

/**
 * Created by 95010915 on 2015/9/23.
 */
public class GlobalDef {
    public final static int PROVISION_SET = 1;
    public final static int PROVISION_NOT_SET = 0;

    public final static String ROOMHUB_SETTINGS_PREFERENCE_NAME = "roomhub_settings";
    // preference keys
    public final static String ROOMHUB_SETTINGS_WIFI_SSID = "wifi_ssid";
    public final static String ROOMHUB_SETTINGS_WIFI_SECURITY = "wifi_security";
    public final static String ROOMHUB_SETTINGS_WIFI_SECURITY_PSK = "wifi_security_psk";
    public final static String ROOMHUB_SETTINGS_WIFI_SECURITY_SUB_TYPE = "wifi_security_subtype";
    public final static String ROOMHUB_SETTINGS_WIFI_PASSWORD = "wifi_password";
    public final static String ROOMHUB_SETTINGS_PROVISION = "provision"; // 0->settup not complete, 1->settup complete

    public final static String ROOMHUB_SETTINGS_TEMP_UNIT = "temp_unit";
    public final static String ROOMHUB_SETTINGS_NOTIFICATION = "notification";
    public final static String ROOMHUB_SETTINGS_NOTIFICATION_TIME = "notification_time";
    public final static String ROOMHUB_SETTINGS_NOTIFICATION_DELTA = "notification_delta";
    public final static String ROOMHUB_SETTINGS_LOCATE_ME = "locate_me";
    public final static String ROOMHUB_SETTINGS_NEWSLETTER = "newsletter";

    public final static String ROOMHUB_SETTINGS_GCM_REGISTRATION = "gcm_registration";

    public final static String ROOMHUB_SETTINGS_SHOW_WELCOME = "show_welcome";

    public final static String ROOMHUB_IS_ONBOARDING = "isOnBoarding";

    public final static String ROOMHUB_PROMPT_DISABLE_MOBILE_DATA = "hasPromptDisableMobileData";

    // broadcast
    public final static String INTENT_NEW_ONBOARDESS_FOUND = "android.intent.action.NEW_ONBOARDEES_FOUND";
    public static final int SECURITY_NONE = 0;
    public static final int SECURITY_WEP = 1;
    public static final int SECURITY_PSK = 2;
    public static final int SECURITY_EAP = 3;

    public final static String ACTION_REMINDER = "android.intent.action.reminder";
    public final static String ACTION_GCM_MESSAGE = "android.intent.action.gcm_message";
    public final static String REMINDER_MESSAGE = "reminder_message";
    public final static String GCM_MESSAGE = "gcm_message";
    public final static String GCM_HTML_MESSAGE = "gcm_html_message";
    public final static String GCM_MESSAGE_TYPE_ID = "gcm_message_type_id";
    public final static String GCM_MESSAGE_ROOMHUB_NOTICE = "gcm_message_roomhub_notice";
    public final static String GCM_MESSAGE_GENERAL = "gcm_message_general";

    public final static String BP_MESSAGE = "bp_message";
    public final static String BP_UUID_MESSAGE = "uuid";
    public final static String BP_USERID_MESSAGE = "userId";
    public final static String BP_DATA_MESSAGE = "bp_data";

    public final static String ACTION_NO_CLOUD_IDENTIFY = "android.intent.action.no_cloud_identify_message";
    public final static String ROOMHUB_UUID_MESSAGE = "roomhub_uuid";
    public final static String ROOMHUB_DEVNAME_MESSAGE = "roomhub_devname";

    public final static String ACTION_WAKE_UP = "android.intent.action.wake_up";

    public enum PskType {
        UNKNOWN,
        WPA,
        WPA2,
        WPA_WPA2
    }

    public enum WPA_WAP2_SUB_TYPE {
        UNKNOWN,
        AUTO, // CCMP+TKIP
        CCMP,
        TKIP
    }

    public final static String WIFI_AP_SSID = "SSID";
    public final static String WIFI_AP_LEVEL = "LEVEL";
    public final static String WIFI_AP_SECURITY = "SECURITY";
    public final static String WIFI_AP_PSKTYPE = "PSKTYPE";
    public final static String WIFI_AP_PSKSUBTYPE = "PSKSUBTYPE";
    public final static String WIFI_AP_PASSWORD = "PASSWORD";

    public static GlobalDef.PskType getPskType(int value) {
        switch(value) {
            case 0:
                return PskType.UNKNOWN;
            case 1:
                return PskType.WPA;
            case 2:
                return PskType.WPA2;
            case 3:
                return PskType.WPA_WPA2;
        }
        return PskType.UNKNOWN;
    }

    public final static String ACTION_SETTINGS_TEMP_UNIT_CHANGED = "ACTION_SETTINGS_TEMP_UNIT_CHANGED";
    public final static String ACTION_SETTINGS_NOTIFICATION_CHANGED = "ACTION_SETTINGS_NOTIFICATION_CHANGED";
    public final static String ACTION_SETTINGS_LOCATE_ME_CHANGED = "ACTION_SETTINGS_LOCATE_ME_CHANGED";
    public final static String ACTION_SETTINGS_NEWSLETTER_CHANGED = "ACTION_SETTINGS_NEWSLETTER_CHANGED";

    public final static String KEY_SETTINGS_VALUE = "KEY_SETTINGS_VALUE";

    public static final int TEMP_UNIT_CELSIUS = 0;
    public static final int TEMP_UNIT_FRHRENHEIT = 1;

    public static final int SIGNUP_REQUEST_CODE_DONE = 100;
    public static final int SIGNUP_REQUEST_CODE_CONFIRM = 200;

    // Network state broadcast
    public static final String ACTION_NETWORK_STATE_CHAGNED = "android.intent.action.network_state_changed";
    public static final String ACTION_NETWORK_TYPE_SWITCHED = "android.intent.action.network_type_switched";
    public static final String EXTRA_NETWORK_STATE = "extra_network_state";

    public static final String KEY_IS_LOGOUT = "KEY_IS_LOGOUT";

    public static final String ACTION_REDIRECT_NOTIFICATION = "com.quantatw.roomhub.redirect.notification";

    public static final String WAKELOCK_ONBOARDING_TAG="OnBoarding";
    public static final String WAKELOCK_IR_PAIRING_TAG="IRPairing";

    public static final int GCM_MESSAGE_ID = 54321; // Ads from customer
    public static final int GCM_MESSAGE_ID_SYSTEM = 54322;  // System maintenance
    public static final int GCM_MESSAGE_ID_ROOMHUB_NOTICE = 54323;  // RoomHub notice
    public static final int GCM_MESSAGE_ID_GENERAL = 54324;

    public static final String FORCE_TERMINATE="TERMINATE";

    public static final String USE_TYPE = "use_type";
    public static final int TYPE_DEFAULT = 0;
    public static final int TYPE_AP_TRANSFER = 1;
    public static final String AP_TRANSFER_LIST = "AP_TRANSFER_LIST";

    public static final int PROMPT_USER_CONNECT_WIFI = 1000;

    public static final String KEY_DEVICE_CATEGORY = "device_category";
    public static final String KEY_DEVICE_TYPE = "device_type";
    public static final String KEY_DEVICE_DATA = "device_data";

    // for BPM GCM
    public static final String ACTION_BPM_NOTICE = "com.quantatw.roomhub.bpm.notification";
}
