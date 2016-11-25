package com.quantatw.sls.pack.homeAppliance;

import android.os.Parcel;

import com.quantatw.sls.pack.base.BaseResPack;

public class GetTVAssetInfoCommandPack extends BaseResPack {

    private static final long serialVersionUID = 5026700471329065706L;

    private GetTVAssetInfoResPack data;

    public GetTVAssetInfoResPack getData() {
        return data;
    }

    public void setData(GetTVAssetInfoResPack data) {
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
    public static final Creator<GetTVAssetInfoCommandPack> CREATOR = new Creator<GetTVAssetInfoCommandPack>() {

        /**
         * Read the serialized concrete strategy from the parcel.
         *
         * @param in
         *            The parcel to read from
         * @return An AbstractStrategy
         */
        public GetTVAssetInfoCommandPack createFromParcel(Parcel in) {
            // Read serialized concrete strategy from parcel
            return (GetTVAssetInfoCommandPack) in.readSerializable();
        }

        /**
         * Required by Creator
         */
        public GetTVAssetInfoCommandPack[] newArray(int size) {
            return new GetTVAssetInfoCommandPack[size];
        }
    };
}
