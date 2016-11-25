package com.quantatw.roomhub.manager;

import android.os.Parcel;
import android.os.Parcelable;
import java.io.Serializable;

/**
 * Created by 95011613 on 2016/1/12.
 */
public class NoticeSetting implements Serializable,Parcelable{
    private int mSwitchOnOff;
    private int mNoticeTime ;
    private int mNoticeDelta;
    private int mIsDefaultTime;
    private int mIsDefaultDelta;

    public NoticeSetting(int switch_on_off, int time,int delta){
        this.mSwitchOnOff=switch_on_off;
        this.mNoticeTime=time;
        this.mNoticeDelta=delta;
    }

    public static final Creator<NoticeSetting> CREATOR = new Creator<NoticeSetting>() {
        @Override
        public NoticeSetting createFromParcel(Parcel in) {
            return (NoticeSetting)in.readSerializable();
        }

        @Override
        public NoticeSetting[] newArray(int size) {
            return new NoticeSetting[size];
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

    public int getSwitchOnOff() {
        return mSwitchOnOff;
    }

    public void setSwitchOnOff(int on_off) {
        mSwitchOnOff=on_off;
    }

    public int getNoticeTime() {
        return mNoticeTime;
    }

    public void setNoticeTime(int time) {
        mNoticeTime=time;
    }

    public int getNoticeDelta() {
        return mNoticeDelta;
    }

    public void setNoticeDelta(int delta) {
        mNoticeDelta=delta;
    }

    public int getIsDefaultTime() {
        return mIsDefaultTime;
    }

    public void setIsDefaultTime(int is_default_time) {
        mIsDefaultTime=is_default_time;
    }

    public int getIsDefaultDelta() {
        return mIsDefaultDelta;
    }

    public void setIsDefaultDelta(int is_default_delta) {
        mIsDefaultDelta=is_default_delta;
    }
}
