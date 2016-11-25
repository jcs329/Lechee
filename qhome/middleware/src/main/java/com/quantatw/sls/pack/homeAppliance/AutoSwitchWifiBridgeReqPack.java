package com.quantatw.sls.pack.homeAppliance;

import android.os.Parcel;

import com.quantatw.sls.pack.base.BaseReqPack;

public class AutoSwitchWifiBridgeReqPack extends BaseReqPack {
    private static final long serialVersionUID = 998688810739598570L;

    private int enable; // 0: disable 1: enable

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
    public static final Creator<AutoSwitchWifiBridgeReqPack> CREATOR = new Creator<AutoSwitchWifiBridgeReqPack>() {

        /**
         * Read the serialized concrete strategy from the parcel.
         *
         * @param in
         *            The parcel to read from
         * @return An AbstractStrategy
         */
        public AutoSwitchWifiBridgeReqPack createFromParcel(Parcel in) {
            // Read serialized concrete strategy from parcel
            return (AutoSwitchWifiBridgeReqPack) in.readSerializable();
        }

        /**
         * Required by Creator
         */
        public AutoSwitchWifiBridgeReqPack[] newArray(int size) {
            return new AutoSwitchWifiBridgeReqPack[size];
        }
    };
}
