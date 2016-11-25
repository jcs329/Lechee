package com.quantatw.roomhub.ir;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;


public class ApIRBrandData implements Serializable, Parcelable {
    private static final long serialVersionUID = -5019881900618728243L;

    private int brandId;
    private String brandName;
    private int assetType;

    public int getBrandId() {
        return brandId;
    }
    public void setBrandId(int brandId) {
        this.brandId = brandId;
    }

    public String getBrandName() {
        return brandName;
    }
    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }

    public int getAssetType() {
        return assetType;
    }

    public void setAssetType(int assetType) {
        this.assetType = assetType;
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
    public static final Creator<ApIRBrandData> CREATOR = new Creator<ApIRBrandData>() {

        /**
         * Read the serialized concrete strategy from the parcel.
         *
         * @param in
         *            The parcel to read from
         * @return An AbstractStrategy
         */
        public ApIRBrandData createFromParcel(Parcel in) {
            // Read serialized concrete strategy from parcel
            return (ApIRBrandData) in.readSerializable();
        }

        /**
         * Required by Creator
         */
        public ApIRBrandData[] newArray(int size) {
            return new ApIRBrandData[size];
        }
    };
}
