package com.quantatw.sls.pack.homeAppliance;

import android.os.Parcel;
import android.os.Parcelable;

import com.quantatw.sls.pack.base.BaseReqPack;

public class RemoveHomeApplianceReqPack extends BaseReqPack {
    private static final long serialVersionUID = -2019604181547168585L;

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
    public static final Creator<RemoveHomeApplianceReqPack> CREATOR = new Parcelable.Creator<RemoveHomeApplianceReqPack>() {

        /**
         * Read the serialized concrete strategy from the parcel.
         *
         * @param in
         *            The parcel to read from
         * @return An AbstractStrategy
         */
        public RemoveHomeApplianceReqPack createFromParcel(Parcel in) {
            // Read serialized concrete strategy from parcel
            return (RemoveHomeApplianceReqPack) in.readSerializable();
        }

        /**
         * Required by Creator
         */
        public RemoveHomeApplianceReqPack[] newArray(int size) {
            return new RemoveHomeApplianceReqPack[size];
        }
    };
}
