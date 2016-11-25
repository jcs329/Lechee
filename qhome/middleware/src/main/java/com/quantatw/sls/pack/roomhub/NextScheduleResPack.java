package com.quantatw.sls.pack.roomhub;

import android.os.Parcel;
import android.os.Parcelable;

import com.quantatw.sls.alljoyn.RoomHubInterface;
import com.quantatw.sls.pack.base.BaseResPack;

public class NextScheduleResPack extends BaseResPack {
    private static final long serialVersionUID = 1464922831766293231L;

    private int modeType;
    private int value;
    private String startTime;
    private boolean powerOnOff;

    public int getModeType() {
        return modeType;
    }
    public void setModeType(int modeType) {
        this.modeType = modeType;
    }
    public int getValue() {
        return value;
    }
    public void setValue(int value) {
        this.value = value;
    }
    public String getStartTime() {
        return startTime;
    }
    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }
    public boolean getPowerOnOff() {
        return powerOnOff;
    }
    public void setPowerOnOff(boolean powerOnOff) {
        this.powerOnOff = powerOnOff;
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
    public static final Creator<NextScheduleResPack> CREATOR = new Parcelable.Creator<NextScheduleResPack>() {

        /**
         * Read the serialized concrete strategy from the parcel.
         *
         * @param in
         *            The parcel to read from
         * @return An AbstractStrategy
         */
        public NextScheduleResPack createFromParcel(Parcel in) {
            // Read serialized concrete strategy from parcel
            return (NextScheduleResPack) in.readSerializable();
        }

        /**
         * Required by Creator
         */
        public NextScheduleResPack[] newArray(int size) {
            return new NextScheduleResPack[size];
        }
    };
}