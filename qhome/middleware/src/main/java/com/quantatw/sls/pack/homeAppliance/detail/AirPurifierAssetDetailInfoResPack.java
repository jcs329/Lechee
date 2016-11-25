package com.quantatw.sls.pack.homeAppliance.detail;

import android.os.Parcel;

import com.quantatw.sls.pack.base.BaseAssetDetailInfoResPack;

public class AirPurifierAssetDetailInfoResPack extends BaseAssetDetailInfoResPack {
    private static final long serialVersionUID = 7359793049874812878L;

    private int power; /* [IR] 0: off 1: on 2: switch [BT] 0: off 1: on */
    private int autoOn; /* [IR][BT] 0: off 1:on */
    private int notifyValue; /* [IR][BT] 54 unit: μg/m3 */

    //For IR
    private int swing; //0: off 1: on 2: switch
    private int fanspeed; // 0: decrease 1: increase 2: switch
    private int mode; //0: normal 1: sleep 2: nature wind 3: special 4: save power 5: switch

    //For BT
    private int quality;    //0~500
    private int autoFan;   // 0: off 1: on_green 2: on_yellow 3: on_red
    private int uv; // 0:off, 1: on
    private int anion;  // 0:off, 1: on
    private int speed;  // 1~9
    private int timer;  // Hour
    /*
     * 2 bytes:
     * high byte: 1~5,
     * low byte: 1~3; 1: Clor Green, 2: Yellow, 3: Red
      */
    private int strainer;

    public int getPower() {
        return power;
    }

    public void setPower(int power) {
        this.power = power;
    }

    public int getAutoOn() {
        return autoOn;
    }

    public void setAutoOn(int autoOn) {
        this.autoOn = autoOn;
    }

    public int getNotifyValue() {
        return notifyValue;
    }

    public void setNotifyValue(int notifyValue) {
        this.notifyValue = notifyValue;
    }

    public int getSwing() {
        return swing;
    }

    public void setSwing(int swing) {
        this.swing = swing;
    }

    public int getFanSpeed() {
        return fanspeed;
    }

    public void setFanSpeed(int fanspeed) {
        this.fanspeed = fanspeed;
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public int getQuality() {
        return quality;
    }

    public void setQuality(int quality) {
        this.quality = quality;
    }

    public int getAutoFan() {
        return autoFan;
    }

    public void setAutoFan(int autoFan) {
        this.autoFan = autoFan;
    }

    public int getUv() {
        return uv;
    }

    public void setUv(int uv) {
        this.uv = uv;
    }

    public int getAnion() {
        return anion;
    }

    public void setAnion(int anion) {
        this.anion = anion;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public int getTimer() {
        return timer;
    }

    public void setTimer(int timer) {
        this.timer = timer;
    }

    public int getStrainer() {
        return strainer;
    }

    public void setStrainer(int strainer) {
        this.strainer = strainer;
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
    public static final Creator<AirPurifierAssetDetailInfoResPack> CREATOR = new Creator<AirPurifierAssetDetailInfoResPack>() {

        /**
         * Read the serialized concrete strategy from the parcel.
         *
         * @param in
         *            The parcel to read from
         * @return An AbstractStrategy
         */
        public AirPurifierAssetDetailInfoResPack createFromParcel(Parcel in) {
            // Read serialized concrete strategy from parcel
            return (AirPurifierAssetDetailInfoResPack) in.readSerializable();
        }

        /**
         * Required by Creator
         */
        public AirPurifierAssetDetailInfoResPack[] newArray(int size) {
            return new AirPurifierAssetDetailInfoResPack[size];
        }
    };
}
