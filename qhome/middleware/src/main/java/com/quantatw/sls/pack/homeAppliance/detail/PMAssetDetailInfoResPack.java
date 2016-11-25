package com.quantatw.sls.pack.homeAppliance.detail;

import android.os.Parcel;

import com.quantatw.sls.pack.base.BaseAssetDetailInfoResPack;

public class PMAssetDetailInfoResPack extends BaseAssetDetailInfoResPack {
    private static final long serialVersionUID = -711481459606563176L;

    private int value;   /* pm2.5 sensor value */
    private int capacity;  /* battery capacity: 5: Full 4: Mid 3: Average 2: Low 1: Empty */
    private int adapter;  /* 1 means the adpater is inserted. otherwise means adapter is not inserted */
    private int time; //uint: second; 0: disable
    private int notifyValue; //unit: μg/m3
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
    public static final Creator<PMAssetDetailInfoResPack> CREATOR = new Creator<PMAssetDetailInfoResPack>() {

        /**
         * Read the serialized concrete strategy from the parcel.
         *
         * @param in
         *            The parcel to read from
         * @return An AbstractStrategy
         */
        public PMAssetDetailInfoResPack createFromParcel(Parcel in) {
            // Read serialized concrete strategy from parcel
            return (PMAssetDetailInfoResPack) in.readSerializable();
        }

        /**
         * Required by Creator
         */
        public PMAssetDetailInfoResPack[] newArray(int size) {
            return new PMAssetDetailInfoResPack[size];
        }
    };
}
