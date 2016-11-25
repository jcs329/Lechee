package com.quantatw.sls.pack.roomhub;

import android.os.Parcel;
import android.os.Parcelable;

import com.quantatw.sls.pack.base.BaseResPack;

public class AcOnOffStatusResPack extends BaseResPack {
    private static final long serialVersionUID = 9151055145278483101L;

    private int functionMode;
    private int targetTemperature;
    private int originTemperature;
    private int nowTemperature;
    private int timeInterval;
    private int lastAction;
    private String userId;

    public int getFunctionMode() {
        return functionMode;
    }
    public void setFunctionMode(int functionMode) {
        this.functionMode = functionMode;
    }

    public int getTargetTemperature() {
        return targetTemperature;
    }
    public void setTargetTemperature(int targetTemperature) {
        this.targetTemperature = targetTemperature;
    }

    public int getOriginTemperature() {
        return originTemperature;
    }
    public void setOriginTemperature(int originTemperature) {
        this.originTemperature = originTemperature;
    }

    public int getNowTemperature() {
        return nowTemperature;
    }
    public void setNowTemperature(int nowTemperature) {
        this.nowTemperature = nowTemperature;
    }

    public int getTimeInterval() {
        return timeInterval;
    }
    public void setTimeInterval(int timeInterval) {
        this.timeInterval = timeInterval;
    }

    public int getLastAction() {
        return lastAction;
    }
    public void setLastAction(int lastAction) {
        this.lastAction = lastAction;
    }

    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
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
    public static final Creator<AcOnOffStatusResPack> CREATOR = new Parcelable.Creator<AcOnOffStatusResPack>() {

        /**
         * Read the serialized concrete strategy from the parcel.
         *
         * @param in
         *            The parcel to read from
         * @return An AbstractStrategy
         */
        public AcOnOffStatusResPack createFromParcel(Parcel in) {
            // Read serialized concrete strategy from parcel
            return (AcOnOffStatusResPack) in.readSerializable();
        }

        /**
         * Required by Creator
         */
        public AcOnOffStatusResPack[] newArray(int size) {
            return new AcOnOffStatusResPack[size];
        }
    };
}
