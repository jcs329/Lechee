package com.quantatw.sls.object;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class IRData implements Serializable, Parcelable {
    private static final long serialVersionUID = 689239250888846402L;

    private String remoterName;
    private int codeNum;
    private String stPower;
    private String stMode;
    private int stTemp;
    private String stFan;
    private String stSwing;
    private String irData;
    private int brandId;	// brand ID
    private String modelId;
    private String devName; // model name

    public String getDevName() {
        return devName;
    }
    public void setDevName(String devName) {
        this.devName = devName;
    }


    public int getBrandId() {
        return brandId;
    }
    public void setBrandId(int brandId) {
        this.brandId = brandId;
    }

    public String getModelId() {
        return modelId;
    }
    public void setModelId(String modelId) {
        this.modelId = modelId;
    }

    public String getRemoterName() {
        return remoterName;
    }
    public void setRemoterName(String remoterName) {
        this.remoterName = remoterName;
    }

    public int getCodeNum() {
        return codeNum;
    }
    public void setCodeNum(int codeNum) {
        this.codeNum = codeNum;
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
    public static final Creator<IRData> CREATOR = new Parcelable.Creator<IRData>() {

        /**
         * Read the serialized concrete strategy from the parcel.
         *
         * @param in
         *            The parcel to read from
         * @return An AbstractStrategy
         */
        public IRData createFromParcel(Parcel in) {
            // Read serialized concrete strategy from parcel
            return (IRData) in.readSerializable();
        }

        /**
         * Required by Creator
         */
        public IRData[] newArray(int size) {
            return new IRData[size];
        }
    };
}
