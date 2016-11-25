package com.quantatw.sls.pack.roomhub;

import android.os.Parcel;
import android.os.Parcelable;

import com.quantatw.sls.pack.base.BaseResPack;

public class RoomHubDataResPack extends BaseResPack {
    private static final long serialVersionUID = 7706621665263337876L;

    private String sensorDataType;
    private int sensorDataTypeSeq;
    private double value;

    public String getSensorDataType() {
        return sensorDataType;
    }
    public void setSensorDataType(String sensorDataType) {
        this.sensorDataType = sensorDataType;
    }

    public int getSensorDataTypeSeq() {
        return sensorDataTypeSeq;
    }
    public void setSensorDataTypeSeq(int sensorDataTypeSeq) {
        this.sensorDataTypeSeq = sensorDataTypeSeq;
    }

    public double getValue() {
        return value;
    }
    public void setValue(double value) {
        this.value = value;
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
    public static final Creator<RoomHubDataResPack> CREATOR = new Parcelable.Creator<RoomHubDataResPack>() {

        /**
         * Read the serialized concrete strategy from the parcel.
         *
         * @param in
         *            The parcel to read from
         * @return An AbstractStrategy
         */
        public RoomHubDataResPack createFromParcel(Parcel in) {
            // Read serialized concrete strategy from parcel
            return (RoomHubDataResPack) in.readSerializable();
        }

        /**
         * Required by Creator
         */
        public RoomHubDataResPack[] newArray(int size) {
            return new RoomHubDataResPack[size];
        }
    };

}
