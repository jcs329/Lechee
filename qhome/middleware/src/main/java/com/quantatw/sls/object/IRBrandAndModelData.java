package com.quantatw.sls.object;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;


public class IRBrandAndModelData  implements Serializable, Parcelable {
    private static final long serialVersionUID = 5708867288085745059L;

    private String modelId;
    private String brandName;
    private int brandId;
    private int codeNum;
    private String devModelNum;
    private String remoteModelNum;
    private int Rate;

    public String getModelId() {
        return modelId;
    }
    public void setModelId(String modelId) {
        this.modelId = modelId;
    }

    public String getBrandName() {
        return brandName;
    }
    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }

    public int getBrandId() {
        return brandId;
    }
    public void setBrandId(int brandId) {
        this.brandId = brandId;
    }

    public int getCodeNum() {
        return codeNum;
    }
    public void setCodeNum(int codeNum) {
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
    public static final Creator<IRBrandAndModelData> CREATOR = new Parcelable.Creator<IRBrandAndModelData>() {

        /**
         * Read the serialized concrete strategy from the parcel.
         *
         * @param in
         *            The parcel to read from
         * @return An AbstractStrategy
         */
        public IRBrandAndModelData createFromParcel(Parcel in) {
            // Read serialized concrete strategy from parcel
            return (IRBrandAndModelData) in.readSerializable();
        }

        /**
         * Required by Creator
         */
        public IRBrandAndModelData[] newArray(int size) {
            return new IRBrandAndModelData[size];
        }
    };
}
