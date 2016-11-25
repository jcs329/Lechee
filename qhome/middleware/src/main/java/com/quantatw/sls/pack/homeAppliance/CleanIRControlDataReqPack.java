package com.quantatw.sls.pack.homeAppliance;

import android.os.Parcel;

import com.quantatw.sls.pack.base.BaseReqPack;

public class CleanIRControlDataReqPack extends BaseReqPack {
    private static final long serialVersionUID = 2056242479550330766L;

    private int assetType;
    private String uuid;

    public int getAssetType() {
        return assetType;
    }

    public void setAssetType(int assetType) {
        this.assetType = assetType;
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
    public static final Creator<CleanIRControlDataReqPack> CREATOR = new Creator<CleanIRControlDataReqPack>() {

        /**
         * Read the serialized concrete strategy from the parcel.
         *
         * @param in
         *            The parcel to read from
         * @return An AbstractStrategy
         */
        public CleanIRControlDataReqPack createFromParcel(Parcel in) {
            // Read serialized concrete strategy from parcel
            return (CleanIRControlDataReqPack) in.readSerializable();
        }

        /**
         * Required by Creator
         */
        public CleanIRControlDataReqPack[] newArray(int size) {
            return new CleanIRControlDataReqPack[size];
        }
    };
}
