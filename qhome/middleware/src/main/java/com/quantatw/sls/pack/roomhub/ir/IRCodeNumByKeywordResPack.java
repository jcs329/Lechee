package com.quantatw.sls.pack.roomhub.ir;

import android.os.Parcel;
import android.os.Parcelable;

import com.quantatw.sls.object.IRCodeNumByKeywordData;
import com.quantatw.sls.object.IRData;
import com.quantatw.sls.pack.base.BaseResPack;

import java.util.ArrayList;

public class IRCodeNumByKeywordResPack extends BaseResPack {
    private static final long serialVersionUID = -5516002874944261971L;

    private ArrayList<IRCodeNumByKeywordData> data;

    public ArrayList<IRCodeNumByKeywordData> getData() {
        return data;
    }
    public void setData(ArrayList<IRCodeNumByKeywordData> data) {
        this.data = data;
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
    public static final Creator<IRCodeNumByKeywordResPack> CREATOR = new Parcelable.Creator<IRCodeNumByKeywordResPack>() {

        /**
         * Read the serialized concrete strategy from the parcel.
         *
         * @param in
         *            The parcel to read from
         * @return An AbstractStrategy
         */
        public IRCodeNumByKeywordResPack createFromParcel(Parcel in) {
            // Read serialized concrete strategy from parcel
            return (IRCodeNumByKeywordResPack) in.readSerializable();
        }

        /**
         * Required by Creator
         */
        public IRCodeNumByKeywordResPack[] newArray(int size) {
            return new IRCodeNumByKeywordResPack[size];
        }
    };
}
