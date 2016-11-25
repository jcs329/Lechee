package com.quantatw.sls.pack.homeAppliance;

import android.os.Parcel;
import android.os.Parcelable;

import com.quantatw.sls.pack.base.BaseReqPack;

public class AddHomeApplianceReqPack extends BaseReqPack {
    private static final long serialVersionUID = 3877164680753498553L;

    private int assetType;
    private int subtype;
    private int connectionType;
    private String uuid; //If exist and not all zero, then the asset uuid specified by app

    public int getAssetType() {
        return assetType;
    }

    public void setAssetType(int assetType) {
        this.assetType = assetType;
    }

    public int getSubType() {
        return subtype;
    }

    public void setSubType(int subtype) {
        this.subtype = subtype;
    }

    public int getConnectionType() {
        return connectionType;
    }

    public void setConnectionType(int connectionType) {
        this.connectionType = connectionType;
    }

    public String getAssetUuid() {
        return uuid;
    }

    public void setAssetUuid(String uuid) {
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
    public static final Creator<AddHomeApplianceReqPack> CREATOR = new Parcelable.Creator<AddHomeApplianceReqPack>() {

        /**
         * Read the serialized concrete strategy from the parcel.
         *
         * @param in
         *            The parcel to read from
         * @return An AbstractStrategy
         */
        public AddHomeApplianceReqPack createFromParcel(Parcel in) {
            // Read serialized concrete strategy from parcel
            return (AddHomeApplianceReqPack) in.readSerializable();
        }

        /**
         * Required by Creator
         */
        public AddHomeApplianceReqPack[] newArray(int size) {
            return new AddHomeApplianceReqPack[size];
        }
    };
}
