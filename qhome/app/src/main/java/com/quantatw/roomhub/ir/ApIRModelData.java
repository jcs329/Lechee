package com.quantatw.roomhub.ir;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;


public class ApIRModelData implements Serializable, Parcelable {
    private static final long serialVersionUID = -5019881900618728243L;

    private String codeNum;
    private String devModelNum;
    private String remoteModelNum;
    private int Rate;
    private String modelId;
    private int subtype;

    public int getSubtype() {
        return subtype;
    }

    public void setSubtype(int subtype) {
        this.subtype = subtype;
    }

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
    public static final Creator<ApIRModelData> CREATOR = new Creator<ApIRModelData>() {

        /**
         * Read the serialized concrete strategy from the parcel.
         *
         * @param in
         *            The parcel to read from
         * @return An AbstractStrategy
         */
        public ApIRModelData createFromParcel(Parcel in) {
            // Read serialized concrete strategy from parcel
            return (ApIRModelData) in.readSerializable();
        }

        /**
         * Required by Creator
         */
        public ApIRModelData[] newArray(int size) {
            return new ApIRModelData[size];
        }
    };
}
