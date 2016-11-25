package com.quantatw.sls.pack.homeAppliance.detail;

import android.os.Parcel;

import com.quantatw.sls.pack.base.BaseAssetDetailInfoResPack;

public class BulbAssetDetailInfoResPack extends BaseAssetDetailInfoResPack {
    private static final long serialVersionUID = -565524408792725648L;

    private int power;  /* 0: off 1: on */
    private int luminance;  /* 0 ~ 100 */

    public int getPower() {
        return power;
    }

    public void setPower(int power) {
        this.power = power;
    }

    public int getLuminance() {
        return luminance;
    }

    public void setLuminanceg(int luminance) {
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
    public static final Creator<BulbAssetDetailInfoResPack> CREATOR = new Creator<BulbAssetDetailInfoResPack>() {

        /**
         * Read the serialized concrete strategy from the parcel.
         *
         * @param in
         *            The parcel to read from
         * @return An AbstractStrategy
         */
        public BulbAssetDetailInfoResPack createFromParcel(Parcel in) {
            // Read serialized concrete strategy from parcel
            return (BulbAssetDetailInfoResPack) in.readSerializable();
        }

        /**
         * Required by Creator
         */
        public BulbAssetDetailInfoResPack[] newArray(int size) {
            return new BulbAssetDetailInfoResPack[size];
        }
    };
}
