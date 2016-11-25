package com.quantatw.sls.pack.homeAppliance;

import android.os.Parcel;

import com.quantatw.sls.pack.base.BaseAssetResPack;

public class GetAcAssetInfoResPack extends BaseAssetResPack {
    private static final long serialVersionUID = -4380292232315651789L;

    private int power;	// power status
    private int temp;	// Temperature value
    private int mode;	// working mode
    private int swing;	// swing angle
    private int fan;	// fan level
    private int timerOn;	// timing on
    private int timerOff;	// timing off

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

    public int getTimeOn() {
        return timerOn;
    }

    public void setTimeOn(int timerOn) {
        this.timerOn = timerOn;
    }

    public int getTimeOff() {
        return timerOff;
    }

    public void setTimeOff(int timerOff) {
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
    public static final Creator<GetAcAssetInfoResPack> CREATOR = new Creator<GetAcAssetInfoResPack>() {

        /**
         * Read the serialized concrete strategy from the parcel.
         *
         * @param in
         *            The parcel to read from
         * @return An AbstractStrategy
         */
        public GetAcAssetInfoResPack createFromParcel(Parcel in) {
            // Read serialized concrete strategy from parcel
            return (GetAcAssetInfoResPack) in.readSerializable();
        }

        /**
         * Required by Creator
         */
        public GetAcAssetInfoResPack[] newArray(int size) {
            return new GetAcAssetInfoResPack[size];
        }
    };
}
