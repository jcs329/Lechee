package com.quantatw.roomhub.utils;

import android.content.Context;

import com.quantatw.myapplication.R;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by erin on 12/25/15.
 */
public class RoomHubFailureCause {
    private static RoomHubFailureCause mInstance;
    private static final Object mInstanceSync = new Object();
    private static HashMap<Integer, ArrayList<FailureCauseInfo>> mFailureCauseList;

    public static interface ID {
        static final int H60Failure_Temp_001 = 100;

        static final int H60Failure_Net_001 = 200;
        static final int H60Failure_Net_002 = 201;

        static final int H60Failure_Control_002 = 302;
        static final int H60Failure_Control_003 = 303;
        static final int H60Failure_Control_007 = 307;
        static final int H60Failure_Control_009 = 309;
        static final int H60Failure_Control_010 = 310;

        static final int H60Failure_Account_001 = 501;

        static final int H60Failure_Phone_001 = 701;
        static final int H60Failure_Phone_002 = 702;

        static final int H60Failure_Phone_004 = 704;

        static final int H60Failure_Device_001 = 801;
        static final int H60Failure_Device_002 = 802;

        static final int H60Failure_GCM_Notice = 900;

    }

    public static RoomHubFailureCause getInstance(Context context) {
        synchronized (mInstanceSync) {
            if (mInstance == null) {
                mInstance = new RoomHubFailureCause();
            }
        }
        return mInstance;
    }

    public static FailureCauseInfo obtainFailCause(int id) {
        FailureCauseInfo returnValue = null;
        synchronized (mInstanceSync) {
            ArrayList<FailureCauseInfo> arrayList = mFailureCauseList.get(id);
            for (FailureCauseInfo failureCauseInfo : arrayList) {
                if (!failureCauseInfo.isInUse()) {
                    failureCauseInfo.setInUse(true);
                    returnValue = failureCauseInfo;
                }
            }
            if(returnValue == null) {
                returnValue =
                        mFailureCauseList.get(id).get(0).cloneMandatory();
                returnValue.setIndex(arrayList.size());
                arrayList.add(returnValue);
            }
        }
        return returnValue;
    }

    public static void discardFailCause(int id, int extra_index) {
        FailureCauseInfo failureCauseInfo = getFailCause(id, extra_index);
        synchronized (mInstanceSync) {
            failureCauseInfo.setActionButton1Message(null);
            failureCauseInfo.setActionButton2Message(null);
            failureCauseInfo.setActionButton3Message(null);
            failureCauseInfo.setInUse(false);
        }
    }

    public static FailureCauseInfo getFailCause(int id, int extra_index) {
        FailureCauseInfo returnValue = null;
        synchronized (mInstanceSync) {
            ArrayList<FailureCauseInfo> arrayList = mFailureCauseList.get(id);
            for (FailureCauseInfo failureCauseInfo : arrayList) {
                if (failureCauseInfo.mIndex == extra_index) {
                    returnValue = failureCauseInfo;
                    break;
                }
            }
        }
        return returnValue;
    }

    private RoomHubFailureCause() {
        mFailureCauseList = new HashMap<Integer,ArrayList<FailureCauseInfo>>();

        mFailureCauseList.put(ID.H60Failure_Temp_001, obtainNewList(new H60FailureTemp001()));

        mFailureCauseList.put(ID.H60Failure_Net_001, obtainNewList(new H60FailureNet001()));
        mFailureCauseList.put(ID.H60Failure_Net_002, obtainNewList(new H60FailureNet002()));

        mFailureCauseList.put(ID.H60Failure_Control_002, obtainNewList(new H60FailureControl002()));
        mFailureCauseList.put(ID.H60Failure_Control_003, obtainNewList(new H60FailureControl003()));
        mFailureCauseList.put(ID.H60Failure_Control_007, obtainNewList(new H60FailureControl007()));
        mFailureCauseList.put(ID.H60Failure_Control_009, obtainNewList(new H60FailureControl009()));
        mFailureCauseList.put(ID.H60Failure_Control_010, obtainNewList(new H60FailureControl010()));

        mFailureCauseList.put(ID.H60Failure_Account_001, obtainNewList(new H60FailureAccount001()));

        mFailureCauseList.put(ID.H60Failure_Phone_001, obtainNewList(new H60FailurePhone001()));
        mFailureCauseList.put(ID.H60Failure_Phone_002, obtainNewList(new H60FailurePhone002()));

        mFailureCauseList.put(ID.H60Failure_Phone_004, obtainNewList(new H60FailurePhone004()));

        mFailureCauseList.put(ID.H60Failure_Device_001, obtainNewList(new H60FailureDevice001()));
        mFailureCauseList.put(ID.H60Failure_Device_002, obtainNewList(new H60FailureDevice002()));

        mFailureCauseList.put(ID.H60Failure_GCM_Notice, obtainNewList(new H60FailureGCMNotice001()));

    }

    private ArrayList<FailureCauseInfo> obtainNewList(FailureCauseInfo failureCauseInfo) {
        ArrayList<FailureCauseInfo> list = new ArrayList<>();
        list.add(failureCauseInfo);
        return list;
    }

    private static class H60FailureTemp001 extends FailureCauseInfo {
        H60FailureTemp001() {
            mId = ID.H60Failure_Temp_001;
            mCategory =
                    new CategoryNode(Category.Temperature, R.drawable.icon_temperror, R.string.fail_category_title_temp);
            mItem = TemperaryItem.Exception;
            mLevel = Level.EMERGENCY;
            mSuggestion1 = R.string.fail_suggestion_temp_001_1;
            mSuggestion2 = R.string.fail_suggestion_temp_001_2;
            mStyle = DisplayStyle.DIALOG;
            mExpireTime = 24*60*60;
            mNoticeRepeatTime = NoticeRepeatTime.ONCE;
            mNoticeRole = Role.ALL;
        }
    }

    private static class H60FailureNet001 extends FailureCauseInfo {
        H60FailureNet001() {
            mId = ID.H60Failure_Net_001;
            mCategory =
                    new CategoryNode(Category.Network, R.drawable.icon_connectionerror, R.string.fail_category_title_network);
            mItem = NetworkItem.DeviceLost;
            mLevel = Level.WARNING;
            mSuggestion1 = R.string.fail_suggestion_net_001_1;
            mSuggestion2 = R.string.fail_suggestion_net_001_2;
            mStyle = DisplayStyle.DIALOG;
            mExpireTime = 24*60*60;
            mNoticeRepeatTime = NoticeRepeatTime.ONCE;
            mNoticeRole = Role.OWNER|Role.USER;
        }
    }

    private static class H60FailureNet002 extends FailureCauseInfo {
        H60FailureNet002() {
            mId = ID.H60Failure_Net_002;
            mCategory =
                    new CategoryNode(Category.Network, R.drawable.icon_connectionerror, R.string.fail_category_title_network);
            mItem = NetworkItem.DeviceOffline;
            mLevel = Level.WARNING;
            mSuggestion1 = R.string.fail_suggestion_net_002_1;
            mSuggestion2 = R.string.fail_suggestion_net_002_2;
            mStyle = DisplayStyle.DIALOG;
            mExpireTime = 24*60*60;
            mNoticeRepeatTime = NoticeRepeatTime.ONCE;
            mNoticeRole = Role.OWNER|Role.USER;
        }
    }

    private static class H60FailureControl002 extends FailureCauseInfo {
        H60FailureControl002() {
            mId = ID.H60Failure_Control_002;
            // TODO: Control item drawable and title
            mCategory =
                    //new CategoryNode(Category.Control, 0, 0);
                    new CategoryNode(Category.Control, R.drawable.icon_temperror, R.string.fail_category_title_control);
            //mCategory = Category.Control;
            mItem = ControlItem.INVALID_INDOOR_ON;
            mLevel = Level.WARNING;
            mSuggestion1 = R.string.fail_suggestion_control_002_1;
            mSuggestion2 = R.string.fail_suggestion_control_002_2;
            mStyle = DisplayStyle.DIALOG;
            mExpireTime = 24*60*60;
            mNoticeRepeatTime = NoticeRepeatTime.ONCE | NoticeRepeatTime.USER_CONFIG;
            mNoticeRole = Role.OPERATOR;
        }
    }

    private static class H60FailureControl003 extends FailureCauseInfo {
        H60FailureControl003() {
            mId = ID.H60Failure_Control_003;
            // TODO: Control item drawable and title
            mCategory =
                    new CategoryNode(Category.Control, R.drawable.icon_temperror, R.string.fail_category_title_control);
            mItem = ControlItem.INVALID_INDOOR_OFF;
            mLevel = Level.WARNING;
            mSuggestion1 = R.string.fail_suggestion_control_003_1;
            mSuggestion2 = R.string.fail_suggestion_control_003_2;
            mStyle = DisplayStyle.DIALOG;
            mExpireTime = 24*60*60;
            mNoticeRepeatTime = NoticeRepeatTime.ONCE | NoticeRepeatTime.USER_CONFIG;
            mNoticeRole = Role.OPERATOR;
        }
    }

    private static class H60FailureControl007 extends FailureCauseInfo {
        H60FailureControl007() {
            mId = ID.H60Failure_Control_007;
            // TODO: Control item drawable and title
            mCategory =
                    new CategoryNode(Category.Control, R.drawable.icon_temperror, R.string.fail_category_title_control);
            mItem = ControlItem.CONTROL_CONFLICT;
            mLevel = Level.WARNING;
            mSuggestion1 = R.string.fail_suggestion_control_007_1;
            mSuggestion2 = R.string.fail_suggestion_control_007_2;
            mStyle = DisplayStyle.DIALOG;
            mExpireTime = 24*60*60;
            mNoticeRepeatTime = NoticeRepeatTime.SPECIFIC;
            mNoticeRepeatSpecific = 30;
            mNoticeRole = Role.OPERATOR;
        }
    }

    private static class H60FailureControl009 extends FailureCauseInfo {
        H60FailureControl009() {
            mId = ID.H60Failure_Control_009;
            // TODO: Control item drawable and title
            mCategory =
                    new CategoryNode(Category.Control, R.drawable.icon_temperror, R.string.fail_category_title_control);
            mItem = ControlItem.CONTROL_NOT_EXPECTED;
            mLevel = Level.NOTICE;
            mSuggestion1 = R.string.fail_suggestion_control_009_1;
            mSuggestion2 = R.string.fail_suggestion_control_009_2;
            mStyle = DisplayStyle.DIALOG;
            mExpireTime = 24*60*60;
            mNoticeRepeatTime = NoticeRepeatTime.SPECIFIC;
            mNoticeRepeatSpecific = 30;
            mNoticeRole = Role.OPERATOR;
        }
    }

    private static class H60FailureControl010 extends FailureCauseInfo {
        H60FailureControl010() {
            mId = ID.H60Failure_Control_010;
            // TODO: Control item drawable and title
            mCategory =
                    new CategoryNode(Category.Control, R.drawable.icon_temperror, R.string.fail_category_title_control);
            mItem = ControlItem.TEMP_NOT_EXPECTED;
            mLevel = Level.WARNING;
            mSuggestion1 = R.string.fail_suggestion_control_009_1;
            mSuggestion2 = R.string.fail_suggestion_control_009_2;
            mStyle = DisplayStyle.DIALOG;
            mExpireTime = 24*60*60;
            mNoticeRepeatTime = NoticeRepeatTime.SPECIFIC;
            mNoticeRepeatSpecific = 30;
            mNoticeRole = Role.OWNER;
        }
    }

    private static class H60FailureAccount001 extends FailureCauseInfo {
        H60FailureAccount001() {
            mId = ID.H60Failure_Account_001;
            // TODO: Account item drawable and title
            mCategory =
                    new CategoryNode(Category.Account, R.drawable.icon_connectionerror, R.string.fail_category_title_account);
            mItem = AccountItem.ERROR;
            mLevel = Level.WARNING;
            mStyle = DisplayStyle.TOAST;
            mExpireTime = 24*60*60;
            mNoticeRepeatTime = NoticeRepeatTime.ONCE;
            mNoticeRole = Role.ALL;
        }
    }

    private static class H60FailurePhone001 extends FailureCauseInfo {
        H60FailurePhone001() {
            mId = ID.H60Failure_Phone_001;
            // TODO: Phone item drawable and title
            mCategory =
                    new CategoryNode(Category.Phone, R.drawable.icon_connectionerror, R.string.fail_category_title_phone);
            mItem = PhoneItem.NETWORK_DISCONNECT;
            mLevel = Level.INFORMATION;
            mStyle = DisplayStyle.TOAST;
            mExpireTime = 24*60*60;
            mNoticeRepeatTime = NoticeRepeatTime.ONCE;
            mNoticeRole = Role.ALL;
        }
    }

    private static class H60FailurePhone002 extends FailureCauseInfo {
        H60FailurePhone002() {
            mId = ID.H60Failure_Phone_002;
            // TODO: Phone item drawable and title
            mCategory =
                    new CategoryNode(Category.Phone, R.drawable.icon_connectionerror, R.string.fail_category_title_phone);
            mItem = PhoneItem.NETWORK_SWITCH;   //PhoneItem.NETWORK_OFTEN_SWITCH;
            mLevel = Level.INFORMATION;
            mStyle = DisplayStyle.TOAST;
            mExpireTime = 24*60*60;
            mNoticeRepeatTime = NoticeRepeatTime.ONCE;
            mNoticeRole = Role.ALL;
        }
    }

    private static class H60FailurePhone004 extends FailureCauseInfo {
        H60FailurePhone004() {
            mId = ID.H60Failure_Phone_004;
            // TODO: Phone item drawable and title
            mCategory =
                    new CategoryNode(Category.Phone, R.drawable.icon_connectionerror, R.string.fail_category_title_phone);
            mItem = PhoneItem.ROOMHUB_NOT_FOUND;
            mLevel = Level.INFORMATION;
            mSuggestion1 = R.string.fail_suggestion_phone_004_1;
            mSuggestion2 = R.string.fail_suggestion_phone_004_2;
            mStyle = DisplayStyle.DIALOG;
            mExpireTime = 24*60*60;
            mNoticeRepeatTime = NoticeRepeatTime.ONCE;
            mNoticeRole = Role.ALL;
        }
    }

    private static class H60FailureDevice001 extends FailureCauseInfo {
        H60FailureDevice001() {
            mId = ID.H60Failure_Device_001;
            // TODO: Phone item drawable and title
            mCategory =
                    new CategoryNode(Category.Device, R.drawable.icon_connectionerror, R.string.fail_category_title_device);
            mItem = DeviceItem.ORPHAN_ISSUE;
            mLevel = Level.WARNING;
            mSuggestion1 = R.string.fail_suggestion_device_001_1;
            mSuggestion2 = R.string.fail_suggestion_device_001_2;
            mStyle = DisplayStyle.DIALOG;
            mExpireTime = 24*60*60;
            mNoticeRepeatTime = NoticeRepeatTime.ONCE;
            mNoticeRole = Role.OWNER;
        }
    }

    private static class H60FailureDevice002 extends FailureCauseInfo {
        H60FailureDevice002() {
            mId = ID.H60Failure_Device_002;
            // TODO: Phone item drawable and title
            mCategory =
                    new CategoryNode(Category.Device, R.drawable.icon_connectionerror, R.string.fail_category_title_device);
            mItem = DeviceItem.FIRMWARE_UPGRADE;
            mLevel = Level.WARNING;
            mSuggestion1 = 0;
            mSuggestion2 = 0;
            mStyle = DisplayStyle.DIALOG;
            mExpireTime = 24*60*60;
            mNoticeRepeatTime = NoticeRepeatTime.ONCE;
            mNoticeRole = Role.OWNER;
        }
    }

    private static class H60FailureGCMNotice001 extends FailureCauseInfo {
        H60FailureGCMNotice001() {
            mId = ID.H60Failure_GCM_Notice;
            // TODO: Phone item drawable and title
            mCategory =
                    new CategoryNode(Category.Notice, R.drawable.icon_connectionerror, R.string.fail_category_title_device);
            mItem = NoticeItem.GENERAL_NOTICE;
            mLevel = Level.WARNING;
            mSuggestion1 = 0;
            mSuggestion2 = 0;
            mStyle = DisplayStyle.DIALOG;
            mExpireTime = 24*60*60;
            mNoticeRepeatTime = NoticeRepeatTime.ONCE;
            mNoticeRole = Role.OWNER;
        }
    }

}
