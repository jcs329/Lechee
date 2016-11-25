package com.quantatw.sls.pack.homeAppliance;

import android.os.Parcel;

import com.quantatw.sls.pack.base.BaseReqPack;

public class CheckIrDataReqPack extends BaseReqPack {
    private static final long serialVersionUID = 3300018000734096182L;

    private String irData;

    public String getIrData() {
        return irData;
    }

    public void setIrData(String irData) {
        this.irData = irData;
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
    public static final Creator<CheckIrDataReqPack> CREATOR = new Creator<CheckIrDataReqPack>() {

        /**
         * Read the serialized concrete strategy from the parcel.
         *
         * @param in
         *            The parcel to read from
         * @return An AbstractStrategy
         */
        public CheckIrDataReqPack createFromParcel(Parcel in) {
            // Read serialized concrete strategy from parcel
            return (CheckIrDataReqPack) in.readSerializable();
        }

        /**
         * Required by Creator
         */
        public CheckIrDataReqPack[] newArray(int size) {
            return new CheckIrDataReqPack[size];
        }
    };
}
