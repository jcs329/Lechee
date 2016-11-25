package com.quantatw.sls.pack.homeAppliance;

import android.os.Parcel;

import com.quantatw.sls.pack.base.BaseResPack;

public class CheckIrDataResPack extends BaseResPack {
    private static final long serialVersionUID = -4305156145178389007L;

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
    public static final Creator<CheckIrDataResPack> CREATOR = new Creator<CheckIrDataResPack>() {

        /**
         * Read the serialized concrete strategy from the parcel.
         *
         * @param in
         *            The parcel to read from
         * @return An AbstractStrategy
         */
        public CheckIrDataResPack createFromParcel(Parcel in) {
            // Read serialized concrete strategy from parcel
            return (CheckIrDataResPack) in.readSerializable();
        }

        /**
         * Required by Creator
         */
        public CheckIrDataResPack[] newArray(int size) {
            return new CheckIrDataResPack[size];
        }
    };
}
