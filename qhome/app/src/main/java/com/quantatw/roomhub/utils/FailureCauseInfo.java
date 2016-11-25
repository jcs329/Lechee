
package com.quantatw.roomhub.utils;

import android.content.Context;
import android.os.Message;

import com.quantatw.myapplication.R;

/**
 * Created by erin on 12/25/15.
 */
public class FailureCauseInfo{

    protected int mId = 0;
    protected int mIndex = 0;
    protected CategoryNode mCategory;
    protected int mItem = 0;
    protected int mLevel = 0;
    protected String mCause;
    protected int mSuggestion1 = 0;
    protected int mSuggestion2 = 0;
    protected int mStyle = 0;
    protected ButtonAction mActionButton1Message;   // predefined: OK Button
    protected ButtonAction mActionButton2Message;   // predefined: Notify Later Button
    protected ButtonAction mActionButton3Message;   // predefined: Go&Check Button
    protected boolean mInUse = false;
    protected int mExpireTime; // Unit: secs. ex: 60 secs.
    /*
     * Once
     * User config: defined in config
     * Specific: define specific time in 'mNoticeRepeatSpecific'
     */
    protected int mNoticeRepeatTime;
    protected int mNoticeRepeatSpecific;    // Unit: mins.
    protected int mNoticeRole;

    public FailureCauseInfo() {}

    public interface NoticeRepeatTime {
        public static final int ONCE = 0x1;
        public static final int USER_CONFIG = 0x2;
        public static final int SPECIFIC = 0x4;
    }

    public interface Role {
        public static final int OWNER = 1 << 0;
        public static final int USER = 1 << 1;
        public static final int OPERATOR = 1 << 2;
        public static final int ANYONE = 1 << 3;
        public static final int ALL = OWNER|USER|OPERATOR|ANYONE;
    }

    public interface FailureButton {
        public static final int BUTTON_OK = 1;
        public static final int BUTTON_NOTIFY_LATER = 2;
        public static final int BUTTON_GO_CHECK = 3;
        public static final int BUTTON_CUSTOM = 4;
    }

    public static interface LaunchActionType {
        public static final int DO_NOTHING = 1;
        public static final int LAUNCH_APP = 2;
        public static final int LAUNCH_CONTROLLER = 3;
        public static final int LAUNCH_WIFI = 4;
        public static final int LAUNCH_MOBILE_NETWORK = 5;
        public static final int LAUNCH_CUSTOM = 6;
    }

    public static class ButtonAction {
        /*
        * buttonType: BUTTON_OK, BUTTON_NOTIFY_LATER, BUTTON_GO_CHECK
        * BUTTON_CUSTOM: customize button's text, use setCustomButtonLabel() to define it.
         */
        private int buttonType = FailureButton.BUTTON_OK;

        /*
        * customButtonLabel: button's text
         */
        private String customButtonLabel;

        /*
        * launchActionType: DO_NOTHING, LAUNCH_APP, LAUNCH_CONTROLLER, LAUNCH_WIFI, LAUNCH_MOBILE_NETWORK
        * LAUNCH_CUSTOM: customize button's launch action. use setReplyMessage() to define it
         */
        private int launchActionType = LaunchActionType.DO_NOTHING;
        private Message replyMessage;

        public ButtonAction() {}

        public void setButtonType(int type) {this.buttonType = type; }
        public int getButtonType() { return this.buttonType; }
        public void setLaunchActionType(int actionType) { this.launchActionType = actionType; }
        public int getLaunchActionType() { return this.launchActionType; }

        public void setCustomButtonLabel(String customButtonLabel) {
            this.buttonType = FailureButton.BUTTON_CUSTOM;
            this.customButtonLabel = customButtonLabel;
        }

        public String getCustomButtonLabel() { return this.customButtonLabel; }

        public void setReplyMessage(Message message) {
            this.launchActionType = LaunchActionType.LAUNCH_CUSTOM;
            this.replyMessage = message;
        }

        public Message getReplyMessage() { return this.replyMessage; }
    }

    public static final class CategoryNode {
        int id;
        int icon_res;
        int title_res;

        CategoryNode(int id, int icon_res, int title_res) {
            this.id = id;
            this.icon_res = icon_res;
            this.title_res = title_res;
        }

        public int getId() { return this.id; }
        public int getIcon_res() { return this.icon_res; }
        public int getTitle_res() { return this.title_res; }
        public String getTitle(Context context) {
            if(title_res > 0)
                return context.getString(title_res);
            return "";
        }
    }

    public static final class Category {
        public static final int Temperature = 100;
        public static final int Network = 200;
        public static final int Control = 300;
        public static final int Share = 400;
        public static final int Account = 500;
        public static final int System = 600;
        public static final int Phone = 700;
        public static final int Device = 800;
        public static final int Notice = 900;
    }

    public static final class TemperaryItem {
        public static final int Exception = 101;
    }

    public static final class NetworkItem {
        public static final int DeviceLost = 201;
        public static final int DeviceOffline = 202;
    }

    public static final class ControlItem {
        public static final int INVALID_INDOOR = 301;
        public static final int INVALID_OUTDOOR = 302;
        public static final int INVALID_INDOOR_ON = 303;
        public static final int INVALID_INDOOR_OFF = 304;
        public static final int INVALID_OUTDOOR_ON = 305;
        public static final int INVALID_OUTDOOR_OFF = 306;
        public static final int CONTROL_CONFLICT = 307;
        public static final int SCHEDULE_CONFLICT = 308;
        public static final int CONTROL_NOT_EXPECTED = 309;
        public static final int TEMP_NOT_EXPECTED = 310;
    }

    public static final class ShareItem {
        public static final int ADD_DEVICE = 401;
        public static final int REMOVE_DEVICE = 402;
        public static final int EXIT_DEVICE = 403;
    }

    public static final class AccountItem {
        public static final int ERROR = 501;
    }

    public static final class SystemItem {
        public static final int MAINTENANCE = 601;
        public static final int BLACKOUT = 602;
        public static final int STORM = 603;
    }

    public static final class PhoneItem {
        public static final int NETWORK_DISCONNECT = 701;
        public static final int NETWORK_SWITCH = 702;
        public static final int NETWORK_OFTEN_SWITCH = 703;
        public static final int ROOMHUB_NOT_FOUND = 704;
    }

    public static final class DeviceItem {
        public static final int ORPHAN_ISSUE = 801;
        public static final int FIRMWARE_UPGRADE = 802;
    }

    public static final class NoticeItem {
        public static final int GENERAL_NOTICE = 901;
    }

    public static final class DisplayStyle {
        public static final int TOAST = 0x1;
        public static final int DIALOG = 0x2;
    }

    public static final class Level {
        public static final int INFORMATION = 100;
        public static final int NOTICE = 101;
        public static final int WARNING = 102;
        public static final int EMERGENCY = 103;
    }

    public int getId() { return mId; }
    public CategoryNode getCategory() { return mCategory; }
    public int getItem() { return mItem; }
    public int getLevel() { return mLevel; }
    public String getCause() { return mCause; }
    public int getSuggestion1() { return mSuggestion1; }
    public int getSuggestion2() { return mSuggestion2; }
    public int getStyle() { return mStyle; }

    public ButtonAction getActionButton1Message() { return mActionButton1Message; }
    public ButtonAction getActionButton2Message() { return mActionButton2Message; }
    public ButtonAction getActionButton3Message() { return mActionButton3Message; }

    public boolean isInUse() { return this.mInUse; }

    public int getIndex() { return this.mIndex; }

    public int getExpireTime() { return this.mExpireTime; } //Unit: secs

    public int getNoticeRepeatTime() { return this.mNoticeRepeatTime; }

    public int getNoticeRepeatSpecific() { return this.mNoticeRepeatSpecific; }

    public int getNoticeRole() { return this.mNoticeRole; }

    public String getLevelString(Context context, int level) {
        switch(level) {
            case Level.EMERGENCY:
                return context.getString(R.string.fail_level_emergency);
            case Level.WARNING:
                return context.getString(R.string.fail_level_warning);
            case Level.NOTICE:
                return context.getString(R.string.fail_level_notice);
            case Level.INFORMATION:
                return context.getString(R.string.fail_level_infomation);
        }
        return "";
    }

    public String getSuggestion1String(Context context) {
        if(mSuggestion1 > 0)
            return context.getString(mSuggestion1);
        else
            return "";
    }

    public String getSuggestion2String(Context context) {
        if(mSuggestion2 > 0)
            return context.getString(mSuggestion2);
        else
            return "";
    }

    public void setCause(String cause) { this.mCause = cause; }

    public void setActionButton1Message(ButtonAction message) { this.mActionButton1Message = message; }
    public void setActionButton2Message(ButtonAction message) { this.mActionButton2Message = message; }
    public void setActionButton3Message(ButtonAction message) { this.mActionButton3Message = message; }

    public void setInUse(boolean inUse) { this.mInUse = inUse; }

    public void setIndex(int index) { this.mIndex = index; }

    public void setExpireTime(int expireTime) { this.mExpireTime = expireTime; }

    public boolean hasButtons() {
        int count = 0;
        if(mActionButton1Message != null)
            count++;
        if(mActionButton2Message != null)
            count++;
        if(mActionButton3Message != null)
            count++;
        return (count > 0)?true:false;
    }

    public FailureCauseInfo copy(FailureCauseInfo source) {
        FailureCauseInfo failureCauseInfo = new FailureCauseInfo();
        failureCauseInfo.mId = source.mId;
        failureCauseInfo.mIndex = source.mIndex;
        failureCauseInfo.mCategory = source.mCategory;
        failureCauseInfo.mItem = source.mItem;
        failureCauseInfo.mLevel = source.mLevel;
        failureCauseInfo.mCause = source.mCause;
        failureCauseInfo.mSuggestion1 = source.mSuggestion1;
        failureCauseInfo.mSuggestion2 = source.mSuggestion2;
        failureCauseInfo.mStyle = source.mStyle;
        failureCauseInfo.mActionButton1Message = source.mActionButton1Message;
        failureCauseInfo.mActionButton2Message = source.mActionButton2Message;
        failureCauseInfo.mActionButton3Message = source.mActionButton3Message;
        failureCauseInfo.mInUse = source.mInUse;
        failureCauseInfo.mExpireTime = source.mExpireTime;
        failureCauseInfo.mNoticeRepeatTime = source.mNoticeRepeatTime;
        failureCauseInfo.mNoticeRepeatSpecific = source.mNoticeRepeatSpecific;
        failureCauseInfo.mNoticeRole = source.mNoticeRole;
        return failureCauseInfo;
    }

    public FailureCauseInfo clone() {
        FailureCauseInfo failureCauseInfo = new FailureCauseInfo();
        failureCauseInfo.mId = this.mId;
        failureCauseInfo.mCategory = this.mCategory;
        failureCauseInfo.mItem = this.mItem;
        failureCauseInfo.mLevel = this.mLevel;
        failureCauseInfo.mCause = this.mCause;
        failureCauseInfo.mSuggestion1 = this.mSuggestion1;
        failureCauseInfo.mSuggestion2 = this.mSuggestion2;
        failureCauseInfo.mStyle = this.mStyle;
        failureCauseInfo.mActionButton1Message = this.mActionButton1Message;
        failureCauseInfo.mActionButton2Message = this.mActionButton2Message;
        failureCauseInfo.mActionButton3Message = this.mActionButton3Message;
        failureCauseInfo.mInUse = this.mInUse;
        failureCauseInfo.mExpireTime = this.mExpireTime;
        failureCauseInfo.mNoticeRepeatTime = this.mNoticeRepeatTime;
        failureCauseInfo.mNoticeRepeatSpecific = this.mNoticeRepeatSpecific;
        failureCauseInfo.mNoticeRole = this.mNoticeRole;
        return failureCauseInfo;
    }

    public FailureCauseInfo cloneMandatory() {
        FailureCauseInfo failureCauseInfo = new FailureCauseInfo();
        failureCauseInfo.mId = this.mId;
        failureCauseInfo.mCategory = this.mCategory;
        failureCauseInfo.mItem = this.mItem;
        failureCauseInfo.mLevel = this.mLevel;
        failureCauseInfo.mSuggestion1 = this.mSuggestion1;
        failureCauseInfo.mSuggestion2 = this.mSuggestion2;
        failureCauseInfo.mStyle = this.mStyle;
        failureCauseInfo.mActionButton1Message = null;
        failureCauseInfo.mActionButton2Message = null;
        failureCauseInfo.mActionButton3Message = null;
        failureCauseInfo.mInUse = false;
        failureCauseInfo.mExpireTime = this.mExpireTime;
        failureCauseInfo.mNoticeRepeatTime = this.mNoticeRepeatTime;
        failureCauseInfo.mNoticeRepeatSpecific = this.mNoticeRepeatSpecific;
        failureCauseInfo.mNoticeRole = this.mNoticeRole;
        return failureCauseInfo;
    }

}
