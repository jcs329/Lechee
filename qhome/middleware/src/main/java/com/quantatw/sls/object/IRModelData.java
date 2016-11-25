package com.quantatw.sls.object;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;


public class IRModelData  implements Serializable, Parcelable {
    private static final long serialVersionUID = -5019881900618728243L;

    private String codeNum;
    private String devModelNum;
    private String remoteModelNum;
    private int Rate;
    private String modelId;
    public String getModelId() {
        return modelId;
    }

    public void setModelId(String modelId) {
        this.modelId = modelId;
    }

    public String getCodeNum() {
        return codeNum;
    }
    public void setCodeNum(String codeNum) {
        this.codeNum = codeNum;
    }

    public String getDevModelNum() {
        return devModelNum;
    }
    public void setDevModelNum(String devModelNum) {
        this.devModelNum = devModelNum;
    }

    public String getRemoteModelNum() {
        return remoteModelNum;
    }
    public void setRemoteModelNum(String remoteModelNum) {
        this.remoteModelNum = remoteModelNum;
    }

    public int getRate() {
        return Rate;
    }
    public void setRate(int Rate) {
        this.Rate = Rate;
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
    public static final Creator<IRModelData> CREATOR = new Parcelable.Creator<IRModelData>() {

        /**
         * Read the serialized concrete strategy from the parcel.
         *
         * @param in
         *            The parcel to read from
         * @return An AbstractStrategy
         */
        public IRModelData createFromParcel(Parcel in) {
            // Read serialized concrete strategy from parcel
            return (IRModelData) in.readSerializable();
        }

        /**
         * Required by Creator
         */
        public IRModelData[] newArray(int size) {
            return new IRModelData[size];
        }
    };
}
