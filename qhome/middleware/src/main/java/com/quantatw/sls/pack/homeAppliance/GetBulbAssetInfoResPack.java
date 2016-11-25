package com.quantatw.sls.pack.homeAppliance;

import android.os.Parcel;

import com.quantatw.sls.pack.base.BaseAssetResPack;

public class GetBulbAssetInfoResPack extends BaseAssetResPack {
    private static final long serialVersionUID = 1941604590184040693L;

    private int power;
    private int luminance;

    public int getPower() {
        return power;
    }

    public void setPower(int power) {
        this.power = power;
    }

    public int getLuminance() {
        return luminance;
    }

    public void setLuminance(int luminance) {
        this.luminance = luminance;
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
    public static final Creator<GetBulbAssetInfoResPack> CREATOR = new Creator<GetBulbAssetInfoResPack>() {

        /**
         * Read the serialized concrete strategy from the parcel.
         *
         * @param in
         *            The parcel to read from
         * @return An AbstractStrategy
         */
        public GetBulbAssetInfoResPack createFromParcel(Parcel in) {
            // Read serialized concrete strategy from parcel
            return (GetBulbAssetInfoResPack) in.readSerializable();
        }

        /**
         * Required by Creator
         */
        public GetBulbAssetInfoResPack[] newArray(int size) {
            return new GetBulbAssetInfoResPack[size];
        }
    };
}
