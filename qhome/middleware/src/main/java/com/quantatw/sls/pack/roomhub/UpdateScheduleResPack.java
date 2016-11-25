package com.quantatw.sls.pack.roomhub;

import android.os.Parcel;
import android.os.Parcelable;

import com.quantatw.sls.alljoyn.RoomHubInterface;
import com.quantatw.sls.pack.base.BaseResPack;

public class UpdateScheduleResPack extends BaseResPack {
    private static final long serialVersionUID = 2708092193504169174L;

    private int index;
    private int modeType;
    private int value;
    private String startTime;
    private String endTime;
    private boolean repeat;
    private boolean state;
    private RoomHubInterface.weekDay_i[] weekday;


    public int getIndex() {
        return index;
    }
    public void setIndex(int index) {
        this.index = index;
    }
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
    public String getEndTime() {
        return endTime;
    }
    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }
    public boolean getRepeat() {
        return repeat;
    }
    public void setRepeat(boolean repeat) {
        this.repeat = repeat;
    }
    public boolean getState() {
        return state;
    }
    public void setState(boolean state) {
        this.state = state;
    }
    public RoomHubInterface.weekDay_i[] getWeekday() {
        return weekday;
    }
    public void setWeekday(RoomHubInterface.weekDay_i[] weekday) {
        this.weekday = weekday;
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
    public static final Creator<UpdateScheduleResPack> CREATOR = new Parcelable.Creator<UpdateScheduleResPack>() {

        /**
         * Read the serialized concrete strategy from the parcel.
         *
         * @param in
         *            The parcel to read from
         * @return An AbstractStrategy
         */
        public UpdateScheduleResPack createFromParcel(Parcel in) {
            // Read serialized concrete strategy from parcel
            return (UpdateScheduleResPack) in.readSerializable();
        }

        /**
         * Required by Creator
         */
        public UpdateScheduleResPack[] newArray(int size) {
            return new UpdateScheduleResPack[size];
        }
    };
}
