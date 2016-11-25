package com.quantatw.sls.pack.homeAppliance;

import android.os.Parcel;
import android.os.Parcelable;

import com.quantatw.sls.pack.base.BaseReqPack;

public class SetAssetInfoReqPack extends BaseReqPack {
    private static final long serialVersionUID = -4676054614440536806L;

    private int assetType;
    private String uuid;
    private int subtype;
    private int connectionType;
    private String brand;
    private String device;
    private int brandId;	// brand ID
    private String modelId;	// model ID

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

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
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
    public static final Creator<SetAssetInfoReqPack> CREATOR = new Parcelable.Creator<SetAssetInfoReqPack>() {

        /**
         * Read the serialized concrete strategy from the parcel.
         *
         * @param in
         *            The parcel to read from
         * @return An AbstractStrategy
         */
        public SetAssetInfoReqPack createFromParcel(Parcel in) {
            // Read serialized concrete strategy from parcel
            return (SetAssetInfoReqPack) in.readSerializable();
        }

        /**
         * Required by Creator
         */
        public SetAssetInfoReqPack[] newArray(int size) {
            return new SetAssetInfoReqPack[size];
        }
    };
}
