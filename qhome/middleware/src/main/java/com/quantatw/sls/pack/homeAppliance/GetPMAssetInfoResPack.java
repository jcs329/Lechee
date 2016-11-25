package com.quantatw.sls.pack.homeAppliance;

import android.os.Parcel;

import com.quantatw.sls.pack.base.BaseAssetResPack;

public class GetPMAssetInfoResPack extends BaseAssetResPack {
    private static final long serialVersionUID = -2014388384758620776L;

    private int value;
    private int capacity;
    private int adapter;
    private int time;
    private int notifyValue;
    private int autoOn; // 0: off 1:on

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getAdapter() {
        return adapter;
    }

    public void setAdapter(int adapter) {
        this.adapter = adapter;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public int getNotifyValue() {
        return notifyValue;
    }

    public void setNotifyValue(int notifyValue) {
        this.notifyValue = notifyValue;
    }

    public int getAutoOn() {
        return autoOn;
    }

    public void setAutoOn(int autoOn) {
        this.autoOn = autoOn;
    }

    /**
     * Flags for special marshaling
     */
    public int describeContents() {
        return 0;
    }

    /**
     * Write the concrete strategy to the Parcel.
     */
    public void writeToParcel(Parcel out, int flags) {
        // Serialize "this", so that we can get it back after IPC
        out.writeSerializable(this);
    }

    /**
     * The creator that MUST be defined and named "CREATOR" so that the service
     * generated from AIDL can recreate AbstractStrategys after IPC.
     */
    public static final Creator<GetPMAssetInfoResPack> CREATOR = new Creator<GetPMAssetInfoResPack>() {

        /**
         * Read the serialized concrete strategy from the parcel.
         *
         * @param in
         *            The parcel to read from
         * @return An AbstractStrategy
         */
        public GetPMAssetInfoResPack createFromParcel(Parcel in) {
            // Read serialized concrete strategy from parcel
            return (GetPMAssetInfoResPack) in.readSerializable();
        }

        /**
         * Required by Creator
         */
        public GetPMAssetInfoResPack[] newArray(int size) {
            return new GetPMAssetInfoResPack[size];
        }
    };
}
