package com.quantatw.sls.pack.homeAppliance.detail;

import android.os.Parcel;

import com.quantatw.sls.pack.base.BaseResPack;

public class AssetResPack extends BaseResPack {
    private static final long serialVersionUID = 6379520402767660327L;

    private String roomHubUUID;
    private int assetType;
    private String assetUuid;

    private int brandId;
    private String modelId;
    private String brandName;
    private String modelName;
    private int power;

    public String getRoomHubUUID() {
        return roomHubUUID;
    }

    public void setRoomHubUUID(String roomHubUUID) {
        this.roomHubUUID = roomHubUUID;
    }

    public int getAssetType() {
        return assetType;
    }

    public void setAssetType(int assetType) {
        this.assetType = assetType;
    }

    public String getAssetUuid() {
        return assetUuid;
    }

    public void setAssetUuid(String assetUuid) {
        this.assetUuid = assetUuid;
    }

    public int getBrandId() {
        return brandId;
    }

    public void setBrandId(int brandId) {
        this.brandId = brandId;
    }

    public String getModelId() {
        return modelId;
    }

    public void setModelId(String modelId) {
        this.modelId = modelId;
    }

    public String getBrandName() {
        return brandName;
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public int isPower() {
        return power;
    }

    public void setPower(int power) {
        this.power = power;
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
    public static final Creator<AssetResPack> CREATOR = new Creator<AssetResPack>() {

        /**
         * Read the serialized concrete strategy from the parcel.
         *
         * @param in
         *            The parcel to read from
         * @return An AbstractStrategy
         */
        public AssetResPack createFromParcel(Parcel in) {
            // Read serialized concrete strategy from parcel
            return (AssetResPack) in.readSerializable();
        }

        /**
         * Required by Creator
         */
        public AssetResPack[] newArray(int size) {
            return new AssetResPack[size];
        }
    };
}
