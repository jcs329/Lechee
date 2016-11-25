package com.quantatw.sls.pack.homeAppliance;

import android.os.Parcel;

import com.quantatw.sls.pack.base.BaseReqPack;

public class GetAbilityLimitReqPack extends BaseReqPack {
    private static final long serialVersionUID = -2971395427274181457L;

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
    public static final Creator<GetAbilityLimitReqPack> CREATOR = new Creator<GetAbilityLimitReqPack>() {

        /**
         * Read the serialized concrete strategy from the parcel.
         *
         * @param in
         *            The parcel to read from
         * @return An AbstractStrategy
         */
        public GetAbilityLimitReqPack createFromParcel(Parcel in) {
            // Read serialized concrete strategy from parcel
            return (GetAbilityLimitReqPack) in.readSerializable();
        }

        /**
         * Required by Creator
         */
        public GetAbilityLimitReqPack[] newArray(int size) {
            return new GetAbilityLimitReqPack[size];
        }
    };
}
