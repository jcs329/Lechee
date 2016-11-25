package com.quantatw.sls.pack.healthcare;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.quantatw.sls.pack.base.BaseStatusPack;

import java.io.Serializable;

/**
 * Created by 95011613 on 2016/3/17.
 */
public class BPMDataInfo extends BaseStatusPack implements Serializable,Parcelable{
    private final String TAG=BPMDataInfo.class.getSimpleName();

    private boolean DEBUG = true;

    private String userId;
    private String userAccount;
    private String deviceName;
    private String measureDate;
    private int maxBloodPressure;
    private int minBloodPressure;
    private int heartRate;

    public BPMDataInfo(){
    }

    public static final Creator<BPMDataInfo> CREATOR = new Creator<BPMDataInfo>() {
        @Override
        public BPMDataInfo createFromParcel(Parcel in) {
            return (BPMDataInfo)in.readSerializable();
        }

        @Override
        public BPMDataInfo[] newArray(int size) {
            return new BPMDataInfo[size];
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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserAccount() {
        return userAccount;
    }

    public void setUserAccount(String userAccount) {
        this.userAccount = userAccount;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getMeasureDate() {
        return measureDate;
    }

    public void setMeasureDate(String measureDate) {
        this.measureDate = measureDate;
    }

    public int getMaxBloodPressure() {
        return maxBloodPressure;
    }

    public void setMaxBloodPressure(int maxBloodPressure) {
        this.maxBloodPressure = maxBloodPressure;
    }

    public int getMinBloodPressure() {
        return minBloodPressure;
    }

    public void setMinBloodPressure(int minBloodPressure) {
        this.minBloodPressure = minBloodPressure;
    }

    public int getHeartRate() {
        return heartRate;
    }

    public void setHeartRate(int heartRate) {
        this.heartRate = heartRate;
    }

    private void log(String msg) {
        if(DEBUG)
            Log.d(TAG,msg);
    }
}
