package com.quantatw.sls.pack.homeAppliance;

import android.os.Parcel;

import com.quantatw.sls.pack.base.BaseReqPack;

public class AddIRControlDataReqPack extends BaseReqPack {
    private static final long serialVersionUID = 6197614668616524974L;

    private int assetType;
    private String uuid;
    private int keyId;
    private String irData;

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

    public int getKeyId() {
        return keyId;
    }

    public void setKeyId(int keyId) {
        this.keyId = keyId;
    }

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
    public static final Creator<AddIRControlDataReqPack> CREATOR = new Creator<AddIRControlDataReqPack>() {

        /**
         * Read the serialized concrete strategy from the parcel.
         *
         * @param in
         *            The parcel to read from
         * @return An AbstractStrategy
         */
        public AddIRControlDataReqPack createFromParcel(Parcel in) {
            // Read serialized concrete strategy from parcel
            return (AddIRControlDataReqPack) in.readSerializable();
        }

        /**
         * Required by Creator
         */
        public AddIRControlDataReqPack[] newArray(int size) {
            return new AddIRControlDataReqPack[size];
        }
    };
}
