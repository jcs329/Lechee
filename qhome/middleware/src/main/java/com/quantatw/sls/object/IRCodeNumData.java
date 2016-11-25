package com.quantatw.sls.object;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;


public class IRCodeNumData  implements Serializable, Parcelable {
    private static final long serialVersionUID = 6025413534057885168L;

    private int dataFrom;
    private int codeNum;
    private String irData;
    private int subtype;

    public int getSubtype() {
        return subtype;
    }
    public void setSubtype(int subtype) {
        this.subtype = subtype;
    }

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
    public static final Creator<IRCodeNumData> CREATOR = new Parcelable.Creator<IRCodeNumData>() {

        /**
         * Read the serialized concrete strategy from the parcel.
         *
         * @param in
         *            The parcel to read from
         * @return An AbstractStrategy
         */
        public IRCodeNumData createFromParcel(Parcel in) {
            // Read serialized concrete strategy from parcel
            return (IRCodeNumData) in.readSerializable();
        }

        /**
         * Required by Creator
         */
        public IRCodeNumData[] newArray(int size) {
            return new IRCodeNumData[size];
        }
    };
}
