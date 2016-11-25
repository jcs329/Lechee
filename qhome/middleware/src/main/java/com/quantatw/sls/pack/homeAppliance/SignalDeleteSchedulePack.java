package com.quantatw.sls.pack.homeAppliance;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Created by 10110012 on 2016/6/22.
 */
public class SignalDeleteSchedulePack implements Serializable,Parcelable {

    private static final long serialVersionUID = 5971491954534659542L;
    private  int type;
    private int assetType;
    private String uuid;
    private int value; // schedule index

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getAssetType() {
        return assetType;
    }

    public void setAssetType(int assetType) {
        this.assetType = assetType;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
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
    public static final Creator<SignalDeleteSchedulePack> CREATOR = new Creator<SignalDeleteSchedulePack>() {

        /**
         * Read the serialized concrete strategy from the parcel.
         *
         * @param in
         *            The parcel to read from
         * @return An AbstractStrategy
         */
        public SignalDeleteSchedulePack createFromParcel(Parcel in) {
            // Read serialized concrete strategy from parcel
            return (SignalDeleteSchedulePack) in.readSerializable();
        }

        /**
         * Required by Creator
         */
        public SignalDeleteSchedulePack[] newArray(int size) {
            return new SignalDeleteSchedulePack[size];
        }
    };
}
