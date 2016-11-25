package com.quantatw.sls.pack.homeAppliance;

import android.os.Parcel;
import android.os.Parcelable;

import com.quantatw.sls.pack.base.BaseAssetResPack;

public class GetFanAssetInfoResPack extends BaseAssetResPack {
    private static final long serialVersionUID = -4305156145178389007L;

    private int power;
    private int swing;
    private int speed;
    private int ION;
    private int humidification;
    private int savePower;
    private int mode;

    public int getPower() {
        return power;
    }

    public void setPower(int power) {
        this.power = power;
    }

    public int getSwing() {
        return swing;
    }

    public void setSwing(int swing) {
        this.swing = swing;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public int getION() {
        return ION;
    }

    public void setION(int ION) {
        this.ION = ION;
    }

    public int getHumidification() {
        return humidification;
    }

    public void setHumidification(int humidification) {
        this.humidification = humidification;
    }

    public int getSavePower() {
        return savePower;
    }

    public void setSavePower(int savePower) {
        this.savePower = savePower;
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
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
    public static final Creator<GetFanAssetInfoResPack> CREATOR = new Parcelable.Creator<GetFanAssetInfoResPack>() {

        /**
         * Read the serialized concrete strategy from the parcel.
         *
         * @param in
         *            The parcel to read from
         * @return An AbstractStrategy
         */
        public GetFanAssetInfoResPack createFromParcel(Parcel in) {
            // Read serialized concrete strategy from parcel
            return (GetFanAssetInfoResPack) in.readSerializable();
        }

        /**
         * Required by Creator
         */
        public GetFanAssetInfoResPack[] newArray(int size) {
            return new GetFanAssetInfoResPack[size];
        }
    };
}
