package com.quantatw.sls.object;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;


public class IRACData  implements Serializable, Parcelable {
    private static final long serialVersionUID = -258139156773634795L;

    private int dataFrom;
    private int codeNum;
    private String irData;

    public int getDataFrom() {
        return dataFrom;
    }
    public void setDataFrom(int dataFrom) {
        this.dataFrom = dataFrom;
    }

    public int getCodeNum() {
        return codeNum;
    }
    public void setCodeNum(int codeNum) {
        this.codeNum = codeNum;
    }

    public String getIrData() {
        return irData;
    }
    public void setIrData(String irData) {
        this.irData = irData;
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
    public static final Creator<IRACData> CREATOR = new Parcelable.Creator<IRACData>() {

        /**
         * Read the serialized concrete strategy from the parcel.
         *
         * @param in
         *            The parcel to read from
         * @return An AbstractStrategy
         */
        public IRACData createFromParcel(Parcel in) {
            // Read serialized concrete strategy from parcel
            return (IRACData) in.readSerializable();
        }

        /**
         * Required by Creator
         */
        public IRACData[] newArray(int size) {
            return new IRACData[size];
        }
    };
}
