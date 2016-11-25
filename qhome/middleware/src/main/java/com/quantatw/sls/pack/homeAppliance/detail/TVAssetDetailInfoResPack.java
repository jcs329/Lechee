package com.quantatw.sls.pack.homeAppliance.detail;

import android.os.Parcel;

import com.quantatw.sls.pack.base.BaseAssetDetailInfoResPack;

public class TVAssetDetailInfoResPack extends BaseAssetDetailInfoResPack {

    private static final long serialVersionUID = 1946184661474807943L;

    private int power;  /* 0: off 1: on 2: switch */

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
    public static final Creator<TVAssetDetailInfoResPack> CREATOR = new Creator<TVAssetDetailInfoResPack>() {

        /**
         * Read the serialized concrete strategy from the parcel.
         *
         * @param in
         *            The parcel to read from
         * @return An AbstractStrategy
         */
        public TVAssetDetailInfoResPack createFromParcel(Parcel in) {
            // Read serialized concrete strategy from parcel
            return (TVAssetDetailInfoResPack) in.readSerializable();
        }

        /**
         * Required by Creator
         */
        public TVAssetDetailInfoResPack[] newArray(int size) {
            return new TVAssetDetailInfoResPack[size];
        }
    };
}
