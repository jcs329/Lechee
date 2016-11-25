package com.quantatw.roomhub.manager;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Created by 95011613 on 2016/1/12.
 */
public class FailureData implements Serializable,Parcelable{
    private int mFailureId;
    private long mLastSendTime;
    private int mLastTemp;
    private int mOKBtnCnt;
    private int mLaterBtnCnt;
    private int mCheckBtnCnt;
    private int mNoNotifyBtnCnt;
    private int mSwitchOnOff;
    private int mCheckTime;

    public FailureData(){

    }

    public static final Creator<FailureData> CREATOR = new Creator<FailureData>() {
        @Override
        public FailureData createFromParcel(Parcel in) {
            return (FailureData)in.readSerializable();
        }

        @Override
        public FailureData[] newArray(int size) {
            return new FailureData[size];
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

    public int getFailureId() {
        return mFailureId;
    }

    public void setFailureId(int failure_id) {
        mFailureId=failure_id;
    }

    public long getLastSendTime() {
        return mLastSendTime;
    }

    public void setLastSendTime(long last_send_time) {
        mLastSendTime=last_send_time;
    }

    public int getLastTemp() {
        return mLastTemp;
    }

    public void setLastTemp(int last_temp) {
        mLastTemp=last_temp;
    }

    public int getOKBtnCnt() {
        return mOKBtnCnt;
    }

    public void setOKBtnCnt(int ok_btn_cnt) {
        mOKBtnCnt=ok_btn_cnt;
    }

    public int getLaterBtnCnt() {
        return mLaterBtnCnt;
    }

    public void setLaterBtnCnt(int later_btn_cnt) {
        mLaterBtnCnt=later_btn_cnt;
    }

    public int getCheckBtnCnt() {
        return mCheckBtnCnt;
    }

    public void setCheckBtnCnt(int check_btn_cnt) {
        mCheckBtnCnt=check_btn_cnt;
    }

    public int getNoNotifyBtnCnt() {
        return mNoNotifyBtnCnt;
    }

    public void setNoNotifyBtnCnt(int no_notify_btn_cnt) {
        mNoNotifyBtnCnt=no_notify_btn_cnt;
    }

    public int getSwitchOnOff() {
        return mSwitchOnOff;
    }

    public void setSwitchOnOff(int on_off) {
        mSwitchOnOff=on_off;
    }

    public int getCheckTime() {
        return mCheckTime;
    }

    public void setCheckTime(int check_time) {
        mCheckTime=check_time;
    }
}
