package com.quantatw.sls.pack.homeAppliance;

import android.os.Parcel;

import com.quantatw.sls.pack.base.BaseResPack;

public class GetBulbAssetInfoCommandPack extends BaseResPack {

    private static final long serialVersionUID = -4591772509221805359L;
    private GetBulbAssetInfoResPack data;

    public GetBulbAssetInfoResPack getData() {
        return data;
    }

    public void setData(GetBulbAssetInfoResPack data) {
        this.data = data;
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
    public static final Creator<GetBulbAssetInfoCommandPack> CREATOR = new Creator<GetBulbAssetInfoCommandPack>() {

        /**
         * Read the serialized concrete strategy from the parcel.
         *
         * @param in
         *            The parcel to read from
         * @return An AbstractStrategy
         */
        public GetBulbAssetInfoCommandPack createFromParcel(Parcel in) {
            // Read serialized concrete strategy from parcel
            return (GetBulbAssetInfoCommandPack) in.readSerializable();
        }

        /**
         * Required by Creator
         */
        public GetBulbAssetInfoCommandPack[] newArray(int size) {
            return new GetBulbAssetInfoCommandPack[size];
        }
    };
}
