package com.quantatw.roomhub.utils;

/**
 * Created by 95011613 on 2015/9/30.
 */
public class ACDef {
    public static final int POWER_OFF       =   0;
    public static final int POWER_ON        =   1;
    public static final int POWER_TOGGLE    =   2;

    public static final int FUN_MODE_AUTO   =   0;
    public static final int FUN_MODE_COOL   =   1;
    public static final int FUN_MODE_DRY    =   2;
    public static final int FUN_MODE_FAN    =   3;
    public static final int FUN_MODE_HEAT   =   4;
    public static final int FUN_MODE_SWITCH =   5;

    public static final int FAN_AUTO    =   0;
    public static final int FAN_HIGH    =   1;
    public static final int FAN_MEDIUM  =   2;
    public static final int FAN_LOW     =   3;
    public static final int FAN_SWITCH  =   4;

    public static final int SWING_FIX   =   0;
    public static final int SWING_AUTO  =   1;
    public static final int SWING_SWITCH    =   2;

    public static final int ACONOFF_LAST_ACTION_BOOT  =   0;
    public static final int ACONOFF_LAST_ACTION_ON    =   1;
    public static final int ACONOFF_LAST_ACTION_OFF   =   2;

    public static final int NOTIFICATION_OFF   =   0;
    public static final int NOTIFICATION_ON    =   1;

    public static final int AC_ABILITY_FAN_AUTO     = 1 << 0;
    public static final int AC_ABILITY_FAN_HIGH     = 1 << 1;
    public static final int AC_ABILITY_FAN_MEDIUM   = 1 << 2;
    public static final int AC_ABILITY_FAN_LOW      = 1 << 3;

    public static final int AC_ABILITY_SWING_FIX = 1 << 0;
    public static final int AC_ABILITY_SWING_AUTO = 1 << 1;

    public static final int AC_FAIL_RECOVER_TURN_ON_FAIL    = 0;
    public static final int AC_FAIL_RECOVER_TURN_OFF_FAIL   = 1;
    public static final int AC_FAIL_RECOVER_REPEAT_CONTROL  = 2;
    public static final int AC_FAIL_RECOVER_TEMP_TOO_HIGH   = 3;

    public static final int AC_SUBTYPE_SPLIT_TYPE           = 0;
    public static final int AC_SUBTYPE_TOGGLE_TYPE          = 1;
    public static final int AC_SUBTYPE_WINDOW_TYPE          = 2;

    public static final int WINDOW_TYPE_KEY_ID_POWER_TOGGLE     =   IRACDef.IR_AC_KEYID_POWER_TOGGLE;
    public static final int WINDOW_TYPE_KEY_ID_POWER_ON         =   IRACDef.IR_AC_KEYID_POWER_ON;
    public static final int WINDOW_TYPE_KEY_ID_POWER_OFF        =   IRACDef.IR_AC_KEYID_SWING_OFF;
    public static final int WINDOW_TYPE_KEY_ID_MODE_AUTO        =   IRACDef.IR_AC_KEYID_MODE_AUTO;
    public static final int WINDOW_TYPE_KEY_ID_MODE_COOL        =   IRACDef.IR_AC_KEYID_MODE_COOL;
    public static final int WINDOW_TYPE_KEY_ID_MODE_DRY         =   IRACDef.IR_AC_KEYID_MODE_DRY;
    public static final int WINDOW_TYPE_KEY_ID_MODE_FAN         =   IRACDef.IR_AC_KEYID_MODE_FAN;
    public static final int WINDOW_TYPE_KEY_ID_SLEEP            =   IRACDef.IR_AC_KEYID_SLEEP;
    public static final int WINDOW_TYPE_KEY_ID_FAN_AUTO         =   IRACDef.IR_AC_KEYID_FAN_SPD_AUTO;
    public static final int WINDOW_TYPE_KEY_ID_FAN_HIGH         =   IRACDef.IR_AC_KEYID_FAN_SPD_HIGH;
    public static final int WINDOW_TYPE_KEY_ID_FAN_MIDDLE       =   IRACDef.IR_AC_KEYID_FAN_SPD_MID;
    public static final int WINDOW_TYPE_KEY_ID_FAN_LOW          =   IRACDef.IR_AC_KEYID_FAN_SPD_LOW;
    public static final int WINDOW_TYPE_KEY_ID_FAN_SWITCH       =   IRACDef.IR_AC_KEYID_FAN_SPEED;
    public static final int WINDOW_TYPE_KEY_ID_TEMP_INCREASE    =   IRACDef.IR_AC_KEYID_TEMP_UP;
    public static final int WINDOW_TYPE_KEY_ID_TEMP_DECREASE    =   IRACDef.IR_AC_KEYID_TEMP_DOWN;
    public static final int WINDOW_TYPE_KEY_ID_TIMER            =   IRACDef.IR_AC_KEYID_TIMER;
    public static final int WINDOW_TYPE_KEY_ID_SWING_ON         =   IRACDef.IR_AC_KEYID_SWING_ON;
    public static final int WINDOW_TYPE_KEY_ID_SWING_OFF        =   IRACDef.IR_AC_KEYID_SWING_OFF;
    public static final int WINDOW_TYPE_KEY_ID_SWING_SWITCH     =   IRACDef.IR_AC_KEYID_SWING_TOGGLE;
}
