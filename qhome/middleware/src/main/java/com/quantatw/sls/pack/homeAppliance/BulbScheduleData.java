package com.quantatw.sls.pack.homeAppliance;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Created by 10110012 on 2016/6/22.
 */
public class BulbScheduleData implements Serializable, Parcelable {

    private static final long serialVersionUID = -6341283564753425415L;
    private String startTime;
    private String endTime;
    private int repeat;
    private int state;
    private int[] weekday;
    private int groupId;
    private int power;
    private int luminance;
    private int index;

    public BulbScheduleData(SignalUpdateSchedulePack updateSchedulePack){
        setStartTime(updateSchedulePack.getStartTime());
        setEndTime(updateSchedulePack.getEndTime());
        setRepeat(updateSchedulePack.getRepeat());
        setState(updateSchedulePack.getState());
        setWeekday(updateSchedulePack.getWeekday());
        setGroupId(updateSchedulePack.getGroupId());
        setPower(updateSchedulePack.getPower());
        setLuminance(updateSchedulePack.getLuminance());
        setIndex(updateSchedulePack.getIndex());
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
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

    public int getRepeat() {
        return repeat;
    }

    public void setRepeat(int repeat) {
        this.repeat = repeat;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int[] getWeekday() {
        return weekday;
    }

    public void setWeekday(int[] weekday) {
        this.weekday = weekday;
    }


    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public int getPower() {
        return power;
    }

    public void setPower(int power) {
        this.power = power;
    }

    public int getLuminance() {
        return luminance;
    }

    public void setLuminance(int luminance) {
        this.luminance = luminance;
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
    public static final Creator<BulbScheduleData> CREATOR = new Creator<BulbScheduleData>() {

        /**
         * Read the serialized concrete strategy from the parcel.
         *
         * @param in
         *            The parcel to read from
         * @return An AbstractStrategy
         */
        public BulbScheduleData createFromParcel(Parcel in) {
            // Read serialized concrete strategy from parcel
            return (BulbScheduleData) in.readSerializable();
        }

        /**
         * Required by Creator
         */
        public BulbScheduleData[] newArray(int size) {
            return new BulbScheduleData[size];
        }
    };
}
