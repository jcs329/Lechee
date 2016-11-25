package com.quantatw.roomhub.manager;

import android.os.Parcel;
import android.os.Parcelable;

import com.quantatw.roomhub.utils.ACDef;
import com.quantatw.roomhub.utils.RoomHubDef;

import java.io.Serializable;

/**
 * Created by 95011613 on 2016/1/12.
 */
public class NoticeStatus implements Serializable,Parcelable{

    private int mFunMode=0;
    private int mTargetTemp=0;
    private int mOriginTemp=0;
    private int mNowTemp=0;
    private int mOutdorTemp=0;
    private int mLastAction= ACDef.ACONOFF_LAST_ACTION_BOOT;
    private String mUserId="";
    private long mTimeStamp;
    private int mAvgCnt;
    private int mAvgTemp;
    private long mSendCmdTime;

    public NoticeStatus(){
    }

    public static final Creator<NoticeStatus> CREATOR = new Creator<NoticeStatus>() {
        @Override
        public NoticeStatus createFromParcel(Parcel in) {
            return (NoticeStatus)in.readSerializable();
        }

        @Override
        public NoticeStatus[] newArray(int size) {
            return new NoticeStatus[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeSerializable(this);
    }

    public int getFunMode() {
        return mFunMode;
    }

    public void setFunMode(int fun_mode) {
        mFunMode=fun_mode;
    }

    public int getTargetTemp() {
        return mTargetTemp;
    }

    public void setTargetTemp(int target_temp) {
        mTargetTemp=target_temp;
    }

    public int getOriginTemp() {
        return mOriginTemp;
    }

    public void setOriginTemp(int origin_temp) {
        mOriginTemp=origin_temp;
    }

    public int getNowTemp() {
        return mNowTemp;
    }

    public void setNowTemp(int now_temp) {
        mNowTemp=now_temp;
    }

    public int getOutdorTemp() {
        return mOutdorTemp;
    }

    public void setOutdorTemp(int outdor_temp) {
        mOutdorTemp=outdor_temp;
    }

    public int getLastAction() {
        return mLastAction;
    }

    public void setLastAction(int last_action) {
        mLastAction=last_action;
    }

    public String getUserId() {
        return mUserId;
    }

    public void setUserId(String user_Id) {
        mUserId=user_Id;
    }

    public long getTimeStamp() {
        return mTimeStamp;
    }

    public void setTimeStamp(long time_stamp) {
        mTimeStamp=time_stamp;
    }

    public int getAvgCnt() {
        return mAvgCnt;
    }

    public void setAvgCnt(int avg_cnt) {
        mAvgCnt=avg_cnt;
    }

    public int getAvgTemp() {
        return mAvgTemp;
    }

    public void setAvgTemp(int avg_temp) {
        mAvgTemp=avg_temp;
    }

    public long getSendCmdTime() {
        return mSendCmdTime;
    }

    public void setSendCmdTime(long cmd_time) {
        mSendCmdTime=cmd_time;
    }
}
