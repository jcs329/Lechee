package com.quantatw.sls.object;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;


public class IRBrandData  implements Serializable, Parcelable {
    private static final long serialVersionUID = -5019881900618728243L;

    private int brandId;
    private String brandName;

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
    public static final Creator<IRBrandData> CREATOR = new Parcelable.Creator<IRBrandData>() {

        /**
         * Read the serialized concrete strategy from the parcel.
         *
         * @param in
         *            The parcel to read from
         * @return An AbstractStrategy
         */
        public IRBrandData createFromParcel(Parcel in) {
            // Read serialized concrete strategy from parcel
            return (IRBrandData) in.readSerializable();
        }

        /**
         * Required by Creator
         */
        public IRBrandData[] newArray(int size) {
            return new IRBrandData[size];
        }
    };
}
