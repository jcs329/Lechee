package com.quantatw.sls.pack.homeAppliance;

import android.os.Parcel;

import com.quantatw.sls.pack.base.BaseAssetResPack;

public class GetTVAssetInfoResPack extends BaseAssetResPack {

    private static final long serialVersionUID = 3844450456560661478L;

    private int power;

    public int getPower() {
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
    public static final Creator<GetTVAssetInfoResPack> CREATOR = new Creator<GetTVAssetInfoResPack>() {

        /**
         * Read the serialized concrete strategy from the parcel.
         *
         * @param in
         *            The parcel to read from
         * @return An AbstractStrategy
         */
        public GetTVAssetInfoResPack createFromParcel(Parcel in) {
            // Read serialized concrete strategy from parcel
            return (GetTVAssetInfoResPack) in.readSerializable();
        }

        /**
         * Required by Creator
         */
        public GetTVAssetInfoResPack[] newArray(int size) {
            return new GetTVAssetInfoResPack[size];
        }
    };
}
