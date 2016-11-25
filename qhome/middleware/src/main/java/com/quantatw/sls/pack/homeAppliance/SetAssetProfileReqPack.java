package com.quantatw.sls.pack.homeAppliance;

import android.os.Parcel;

import com.quantatw.sls.pack.base.BaseReqPack;

/**
 * Created by 10110012 on 2016/6/22.
 */
public class SetAssetProfileReqPack extends BaseReqPack {

    private static final long serialVersionUID = 529867729554914884L;
    private int assetType;
    private String uuid;
    private String name;

    public int getAssetType() {
        return assetType;
    }

    public void setAssetType(int assetType) {
        this.assetType = assetType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
    public static final Creator<SetAssetProfileReqPack> CREATOR = new Creator<SetAssetProfileReqPack>() {

        /**
         * Read the serialized concrete strategy from the parcel.
         *
         * @param in
         *            The parcel to read from
         * @return An AbstractStrategy
         */
        public SetAssetProfileReqPack createFromParcel(Parcel in) {
            // Read serialized concrete strategy from parcel
            return (SetAssetProfileReqPack) in.readSerializable();
        }

        /**
         * Required by Creator
         */
        public SetAssetProfileReqPack[] newArray(int size) {
            return new SetAssetProfileReqPack[size];
        }
    };
}
