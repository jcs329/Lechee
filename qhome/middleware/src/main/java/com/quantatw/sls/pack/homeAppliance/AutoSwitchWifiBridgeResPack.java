package com.quantatw.sls.pack.homeAppliance;

import android.os.Parcel;

import com.quantatw.sls.pack.base.BaseResPack;

public class AutoSwitchWifiBridgeResPack extends BaseResPack {
    private static final long serialVersionUID = -97685346385353172L;

    private int enable; // 0: disable 1: enable
    private int method;
    private int result;

    public int getMethod() {
        return method;
    }

    public void setMethod(int method) {
        this.method = method;
    }

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    public int getEnable() {
        return enable;
    }

    public void setEnable(int enable) {
        this.enable = enable;
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
    public static final Creator<AutoSwitchWifiBridgeResPack> CREATOR = new Creator<AutoSwitchWifiBridgeResPack>() {

        /**
         * Read the serialized concrete strategy from the parcel.
         *
         * @param in
         *            The parcel to read from
         * @return An AbstractStrategy
         */
        public AutoSwitchWifiBridgeResPack createFromParcel(Parcel in) {
            // Read serialized concrete strategy from parcel
            return (AutoSwitchWifiBridgeResPack) in.readSerializable();
        }

        /**
         * Required by Creator
         */
        public AutoSwitchWifiBridgeResPack[] newArray(int size) {
            return new AutoSwitchWifiBridgeResPack[size];
        }
    };
}
