package com.quantatw.sls.pack.homeAppliance.detail;

import android.os.Parcel;

import com.quantatw.sls.pack.base.BaseAssetDetailInfoResPack;

public class FanAssetDetailInfoResPack extends BaseAssetDetailInfoResPack {
    private static final long serialVersionUID = 4104849404343126417L;

    private int power;  /* 0: off 1: on 2: switch */
    private int swing;  /* 0: off 1: on 2: switch */
    private int speed;  /* 0: decrease 1: increase 2: switch */
    private int ION;    /* 0: off 1: on */
    private int humidification; /* 0: off 1: on */
    private int savePower; /* 0: off 1: on */
    private int mode;   /* 0: normal 1: sleep 2: nature wind 3: special 4: save power */

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
    public static final Creator<FanAssetDetailInfoResPack> CREATOR = new Creator<FanAssetDetailInfoResPack>() {

        /**
         * Read the serialized concrete strategy from the parcel.
         *
         * @param in
         *            The parcel to read from
         * @return An AbstractStrategy
         */
        public FanAssetDetailInfoResPack createFromParcel(Parcel in) {
            // Read serialized concrete strategy from parcel
            return (FanAssetDetailInfoResPack) in.readSerializable();
        }

        /**
         * Required by Creator
         */
        public FanAssetDetailInfoResPack[] newArray(int size) {
            return new FanAssetDetailInfoResPack[size];
        }
    };
}
