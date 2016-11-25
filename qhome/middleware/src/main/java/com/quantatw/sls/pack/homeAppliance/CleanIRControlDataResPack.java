package com.quantatw.sls.pack.homeAppliance;

import android.os.Parcel;

import com.quantatw.sls.pack.base.BaseResPack;

public class CleanIRControlDataResPack extends BaseResPack {
    private static final long serialVersionUID = -4305156145178389007L;

    private int method;
    private int assetType;

    public int getMethod() {
        return method;
    }

    public void setMethod(int method) {
        this.method = method;
    }

    public int getAssetType() {
        return assetType;
    }

    public void setAssetType(int assetType) {
        this.assetType = assetType;
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
    public static final Creator<CleanIRControlDataResPack> CREATOR = new Creator<CleanIRControlDataResPack>() {

        /**
         * Read the serialized concrete strategy from the parcel.
         *
         * @param in
         *            The parcel to read from
         * @return An AbstractStrategy
         */
        public CleanIRControlDataResPack createFromParcel(Parcel in) {
            // Read serialized concrete strategy from parcel
            return (CleanIRControlDataResPack) in.readSerializable();
        }

        /**
         * Required by Creator
         */
        public CleanIRControlDataResPack[] newArray(int size) {
            return new CleanIRControlDataResPack[size];
        }
    };
}
