package com.quantatw.roomhub.manager;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.radiusnetworks.ibeacon.IBeacon;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Comparator;


/**
 * Created by jungle on 2015/10/6.
 */

public class AlljyonIBeacon implements Serializable, Parcelable {
    protected static final String TAG = "AlljyonIBeacon";
    private static boolean DEBUG = true;

    private String mUuid = "";
    private String mBtUuid = "";
    private int mIBeaconMajor = -1;
    private int mIBeaconMinor = -1;
    private IBeacon mIbeacon = null;
    private double mDistance = 99999999;
    private int mProximity = 0;
    private int mSequence = 99999999; //順序由1 開始, 不可重複, 不可漏號
    private int mOldSequence = 99999999; //順序由1 開始, 不可重複, 不可漏號
    private Timestamp mTimestamp;  //最後收到的時間點
    private Boolean mIsAvailable = false;
    private MicroLocationData mLocationData = null;

    public static Comparator<AlljyonIBeacon> getCompByDistance()
    {
        Comparator comp = new Comparator<AlljyonIBeacon>(){
            @Override
            public int compare(AlljyonIBeacon ab1, AlljyonIBeacon ab2)
            {
                if (DEBUG) Log.d(TAG, "addDevice ++, ab1.getDistance()="+ab1.getDistance()+",ab2.getDistance()="+ab2.getDistance());
                if (ab1.getDistance() > ab2.getDistance()){
                    return 1;
                } else {
                    return -1;
                }
            }
        };
        return comp;
    }

    public static final Creator<AlljyonIBeacon> CREATOR = new Creator<AlljyonIBeacon>() {
        @Override
        public AlljyonIBeacon createFromParcel(Parcel in) {
            //return new RoomHubProfile(in);
            return (AlljyonIBeacon) in.readSerializable();
        }

        @Override
        public AlljyonIBeacon[] newArray(int size) {
            return new AlljyonIBeacon[size];
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

    public String getUuid() {
        return mUuid;
    }

    public String getBtUuid() {
        return mBtUuid;
    }

    public int getIBeaconMajor() {
        return mIBeaconMajor;
    }

    public int getIBeaconMinor() {
        return mIBeaconMinor;
    }

    public String getIBeaconUuid() {
        if (mIbeacon == null) return null;
        return mIbeacon.getProximityUuid();
    }

    public IBeacon getIbeacon() {
        if (mIbeacon == null) return null;
        return mIbeacon;
    }

    public int getSeqence() {
        return mSequence;
    }

    public int getProximity() {
        return mProximity;
    }

    public int getOldSeqence() {
        return mOldSequence;
    }


    public String getBtMacAddress() {
        if (!mIsAvailable) return null;
        return mIbeacon.getDeviceMACAddress();
    }

    public Boolean getIsAvailable() { return mIsAvailable; }

    public double getDistance() {
        if (DEBUG) Log.d(TAG, "getDistance ++, mIsAvailable=" + mIsAvailable);
        if (!mIsAvailable) {
            mDistance = 99999999;
        }
        else {

            java.util.Date date = new java.util.Date();
            Timestamp timestampNow = new Timestamp(date.getTime());
            if (DEBUG) Log.d(TAG, "getDistance ++, timestampNow=" + timestampNow+"mTimestamp="
                    +mTimestamp);
            if (DEBUG) Log.d(TAG, "getDistance ++, timestampNow.getTime()=" + timestampNow.getTime()+"mTimestamp.getTime()="+mTimestamp.getTime());
            if (timestampNow.getTime() - mTimestamp.getTime() > 15000) {
                mIsAvailable = false;
                mDistance = 99999999;
            }
        }

        if (DEBUG) Log.d(TAG, "getDistance ++, mDistance=" + mDistance);

        return mDistance;
    }

    public Timestamp getTimeStamp() { return mTimestamp; }

    public int getMajor() {
        if (!mIsAvailable)  return -1;

        return mIbeacon.getMajor();
    }

    public int getMinor() {
        if (!mIsAvailable)  return -1;
        return mIbeacon.getMinor();
    }

    public void setUuid(String uuid) { mUuid = uuid; }

    public void setBtUuid(String btuuid) { mBtUuid = btuuid; }

    public void setIBeaconMajor(int major) { mIBeaconMajor = major; }

    public void setIBeaconMinor(int minor) { mIBeaconMinor = minor; }

    public void setIsAvailable(Boolean available) {
        mIsAvailable = available;
    }

    public void setIBeaconData(IBeacon ibeacon) {
        if (ibeacon == null) return;
        mIbeacon = ibeacon;
    }

    public void setSequence(int sequence) { mSequence = sequence; }

    public void setProximity(int proximity) { mProximity = proximity; }

    public void setOldSequence(int oldsequence) { mOldSequence = oldsequence; }

    public void setDistance(double distance) { mDistance = distance; }

    public void setTimeStamp(Timestamp timestamp) { mTimestamp = timestamp; }

    public MicroLocationData getMicroLocationData(){
        return mLocationData;
    }

    public void setMicroLocationData(MicroLocationData data){
        mLocationData = data;
    }

}
