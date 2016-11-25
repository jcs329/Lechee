package com.quantatw.sls.pack.roomhub.sensor;

import android.os.Parcel;
import android.os.Parcelable;
import java.io.Serializable;


public class Record implements Serializable,Parcelable {
    private static final long serialVersionUID = -1320964783332192534L;
    private String uuid;
    private long timeStamp;
    private double recordValue;
    private String recordTime;

    public String getUuid() {
        return uuid;
    }
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
    public long getTimeStamp() {
        return timeStamp;
    }
    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }
    public double getRecordValue() {
        return recordValue;
    }
    public void setRecordValue(double recordValue) {
        this.recordValue = recordValue;
    }
    public String getRecordTime() {
        return recordTime;
    }
    public void setRecordTime(String recordTime) {
        this.recordTime = recordTime;
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
    public static final Creator<Record> CREATOR = new Parcelable.Creator<Record>() {

        /**
         * Read the serialized concrete strategy from the parcel.
         *
         * @param in
         *            The parcel to read from
         * @return An AbstractStrategy
         */
        public Record createFromParcel(Parcel in) {
            // Read serialized concrete strategy from parcel
            return (Record) in.readSerializable();
        }

        /**
         * Required by Creator
         */
        public Record[] newArray(int size) {
            return new Record[size];
        }
    };
}
