package com.quantatw.sls.object;

import android.os.Parcel;
import android.os.Parcelable;

import com.quantatw.sls.pack.base.BaseResPack;

import java.util.ArrayList;

public class IRCodeNumByKeywordData extends BaseResPack {
    private static final long serialVersionUID = -4325752739965936204L;

    private String brandName;
    private int type;
    private ArrayList<IRData> irDataList;

    public String getBrandName() {
        return this.brandName;
    }
    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }

    public int getType() {
        return this.type;
    }
    public void setType(int type) {
        this.type = type;
    }

    public ArrayList<IRData> getIrDataList() {
        return this.irDataList;
    }
    public void setIrDataList(ArrayList<IRData> irDataList) {
        this.irDataList = irDataList;
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
    public static final Creator<IRCodeNumByKeywordData> CREATOR = new Parcelable.Creator<IRCodeNumByKeywordData>() {

        /**
         * Read the serialized concrete strategy from the parcel.
         *
         * @param in
         *            The parcel to read from
         * @return An AbstractStrategy
         */
        public IRCodeNumByKeywordData createFromParcel(Parcel in) {
            // Read serialized concrete strategy from parcel
            return (IRCodeNumByKeywordData) in.readSerializable();
        }

        /**
         * Required by Creator
         */
        public IRCodeNumByKeywordData[] newArray(int size) {
            return new IRCodeNumByKeywordData[size];
        }
    };
}
