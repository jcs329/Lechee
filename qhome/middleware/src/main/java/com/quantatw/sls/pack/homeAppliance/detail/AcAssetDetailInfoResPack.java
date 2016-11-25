package com.quantatw.sls.pack.homeAppliance.detail;

import android.os.Parcel;

import com.quantatw.sls.pack.base.BaseAssetDetailInfoResPack;

public class AcAssetDetailInfoResPack extends BaseAssetDetailInfoResPack {
    private static final long serialVersionUID = 4104849404343126417L;

    private int power;  /* 0: off 1:on 2: toggle, */
    private int temp;
    private int mode;   /* 0: Auto 1: Cool 2: Dry 3: Fan 4: Heat, */
    private int swing;  /* 0: off 1: on, */
    private int fan;    /* 0: Auto 1:High 2:Low 3: Soft, */
    private int timerOn;    /* high 16 bytes: hour; low 16 bytes: minute, */
    private int timerOff;   /* high 16 bytes: hour; low 16 bytes: minute, */

    public int getPower() {
        return power;
    }

    public void setPower(int power) {
        this.power = power;
    }

    public int getTemp() {
        return temp;
    }

    public void setTemp(int temp) {
        this.temp = temp;
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public int getSwing() {
        return swing;
    }

    public void setSwing(int swing) {
        this.swing = swing;
    }

    public int getFan() {
        return fan;
    }

    public void setFan(int fan) {
        this.fan = fan;
    }

    public int getTimerOn() {
        return timerOn;
    }

    public void setTimerOn(int timerOn) {
        this.timerOn = timerOn;
    }

    public int getTimerOff() {
        return timerOff;
    }

    public void setTimerOff(int timerOff) {
        this.timerOff = timerOff;
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
    public static final Creator<AcAssetDetailInfoResPack> CREATOR = new Creator<AcAssetDetailInfoResPack>() {

        /**
         * Read the serialized concrete strategy from the parcel.
         *
         * @param in
         *            The parcel to read from
         * @return An AbstractStrategy
         */
        public AcAssetDetailInfoResPack createFromParcel(Parcel in) {
            // Read serialized concrete strategy from parcel
            return (AcAssetDetailInfoResPack) in.readSerializable();
        }

        /**
         * Required by Creator
         */
        public AcAssetDetailInfoResPack[] newArray(int size) {
            return new AcAssetDetailInfoResPack[size];
        }
    };
}
