package com.quantatw.sls.pack.roomhub.sensor;

import android.os.Parcel;
import android.os.Parcelable;
import com.quantatw.sls.pack.base.BaseResPack;
import java.util.ArrayList;

public class SensorDataResPack extends BaseResPack {
    private static final long serialVersionUID = 1739369010988402405L;

    private double minValue;
    private double maxValue;
    private double avgValue;
    private double nowValue;
    private String  alertHighValue;
    private String  alertLowValue;
    private ArrayList<Record> recordList;

    public double getMinValue() { return this.minValue; }
    public void setMinValue(double minValue) {
        this.minValue = minValue;
    }

    public double getMaxValue() { return this.maxValue; }
    public void setMaxValue(double maxValue) {
        this.maxValue = maxValue;
    }

    public double getAvgValue() { return this.avgValue; }
    public void setAvgValue(double avgValue) {
        this.avgValue = avgValue;
    }

    public double getNowValue() { return this.nowValue; }
    public void setNowValue(double nowValue) {
        this.nowValue = nowValue;
    }


    public ArrayList<Record> getRecordList() {
        return recordList;
    }
    public void setRecordList(ArrayList<Record> recordList) {
        this.recordList = recordList;
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
    public static final Creator<SensorDataResPack> CREATOR = new Parcelable.Creator<SensorDataResPack>() {

        /**
         * Read the serialized concrete strategy from the parcel.
         *
         * @param in
         *            The parcel to read from
         * @return An AbstractStrategy
         */
        public SensorDataResPack createFromParcel(Parcel in) {
            // Read serialized concrete strategy from parcel
            return (SensorDataResPack) in.readSerializable();
        }

        /**
         * Required by Creator
         */
        public SensorDataResPack[] newArray(int size) {
            return new SensorDataResPack[size];
        }
    };
}

