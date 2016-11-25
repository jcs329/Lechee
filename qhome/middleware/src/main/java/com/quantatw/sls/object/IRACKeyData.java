package com.quantatw.sls.object;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;


public class IRACKeyData  implements Serializable, Parcelable {
    private static final long serialVersionUID = 3168960377749136386L;

    private String keyId;
    private String stPower;
    private String stMode;
    private int stTemp;
    private String stFan;
    private String stSwing;
    private String irData;

    public String getKeyId() {
        return keyId;
    }
    public void setKeyId(String keyId) {
        this.keyId = keyId;
    }

    public String getStPower() {
        return stPower;
    }
    public void setStPower(String stPower) {
        this.stPower = stPower;
    }

    public String getStMode() {
        return stMode;
    }
    public void setStMode(String stMode) {
        this.stMode = stMode;
    }

    public int getStTemp() {
        return stTemp;
    }
    public void setStTemp(int stTemp) {
        this.stTemp = stTemp;
    }

    public String getStFan() {
        return stFan;
    }
    public void setStFan(String stFan) {
        this.stFan = stFan;
    }

    public String getStSwing() {
        return stSwing;
    }
    public void setStSwing(String stSwing) {
        this.stSwing = stSwing;
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
    public static final Creator<IRACKeyData> CREATOR = new Parcelable.Creator<IRACKeyData>() {

        /**
         * Read the serialized concrete strategy from the parcel.
         *
         * @param in
         *            The parcel to read from
         * @return An AbstractStrategy
         */
        public IRACKeyData createFromParcel(Parcel in) {
            // Read serialized concrete strategy from parcel
            return (IRACKeyData) in.readSerializable();
        }

        /**
         * Required by Creator
         */
        public IRACKeyData[] newArray(int size) {
            return new IRACKeyData[size];
        }
    };
}
