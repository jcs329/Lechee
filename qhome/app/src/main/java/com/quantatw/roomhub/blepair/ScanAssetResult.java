package com.quantatw.roomhub.blepair;

import android.os.Parcel;
import android.os.Parcelable;

import com.quantatw.sls.device.ScanAsset;

import java.io.Serializable;

/**
 * Created by cherry on 2016/05/20.
 */
public class ScanAssetResult implements Serializable,Parcelable{

    private String roomHubUUID;
    private ScanAsset scanAsset;
    private int result;

    public String getRoomHubUuid() {
        return roomHubUUID;
    }

    public void setRoomHubUuid(String roomHubUUID) {
        this.roomHubUUID = roomHubUUID;
    }

    public ScanAsset getScanAsset() {
        return scanAsset;
    }

    public void setScanAsset(ScanAsset scanAsset) {
        this.scanAsset = scanAsset;
    }

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    /**
     * The creator that MUST be defined and named "CREATOR" so that the service
     * generated from AIDL can recreate AbstractStrategys after IPC.
     */
    public static final Creator<ScanAssetResult> CREATOR = new Creator<ScanAssetResult>() {

        /**
         * Read the serialized concrete strategy from the parcel.
         *
         * @param in
         *            The parcel to read from
         * @return An AbstractStrategy
         */
        public ScanAssetResult createFromParcel(Parcel in) {
            // Read serialized concrete strategy from parcel
            return (ScanAssetResult) in.readSerializable();
        }

        /**
         * Required by Creator
         */
        public ScanAssetResult[] newArray(int size) {
            return new ScanAssetResult[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeSerializable(this);
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
