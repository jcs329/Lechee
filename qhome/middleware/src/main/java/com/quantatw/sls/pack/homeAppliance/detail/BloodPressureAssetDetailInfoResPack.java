package com.quantatw.sls.pack.homeAppliance.detail;

import android.os.Parcel;

import com.quantatw.sls.pack.base.BaseAssetDetailInfoResPack;

public class BloodPressureAssetDetailInfoResPack extends BaseAssetDetailInfoResPack {
    private static final long serialVersionUID = -3468449646574282257L;

    private int maxBloodPressure;
    private int minBloodPressure;
    private int heartRate;

    public int getMaxBloodPressure() {
        return maxBloodPressure;
    }

    public void setMaxBloodPressure(int max_blood_pressure) {
        this.maxBloodPressure = max_blood_pressure;
    }

    public int getMinBloodPressure() {
        return minBloodPressure;
    }

    public void setMinBloodPressure(int min_blood_pressure) {
        this.minBloodPressure = min_blood_pressure;
    }

    public int getHeartRate() {
        return heartRate;
    }

    public void setHeartRate(int heart_rate) {
        this.heartRate = heart_rate;
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
    public static final Creator<BloodPressureAssetDetailInfoResPack> CREATOR = new Creator<BloodPressureAssetDetailInfoResPack>() {

        /**
         * Read the serialized concrete strategy from the parcel.
         *
         * @param in
         *            The parcel to read from
         * @return An AbstractStrategy
         */
        public BloodPressureAssetDetailInfoResPack createFromParcel(Parcel in) {
            // Read serialized concrete strategy from parcel
            return (BloodPressureAssetDetailInfoResPack) in.readSerializable();
        }

        /**
         * Required by Creator
         */
        public BloodPressureAssetDetailInfoResPack[] newArray(int size) {
            return new BloodPressureAssetDetailInfoResPack[size];
        }
    };
}
