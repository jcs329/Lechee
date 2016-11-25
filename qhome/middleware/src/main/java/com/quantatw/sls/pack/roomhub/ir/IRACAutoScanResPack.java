package com.quantatw.sls.pack.roomhub.ir;

import android.os.Parcel;
import android.os.Parcelable;

import com.quantatw.sls.object.IRACData;
import com.quantatw.sls.pack.base.BaseResPack;

import java.util.ArrayList;

public class IRACAutoScanResPack extends BaseResPack {
    private static final long serialVersionUID = 7108479504252826342L;
    private ArrayList<IRACData> data;

    public ArrayList<IRACData> getACAutoScanList() {
        return data;
    }
    public void setACAutoScanList(ArrayList<IRACData> data) {
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
    public static final Creator<IRACAutoScanResPack> CREATOR = new Parcelable.Creator<IRACAutoScanResPack>() {

        /**
         * Read the serialized concrete strategy from the parcel.
         *
         * @param in
         *            The parcel to read from
         * @return An AbstractStrategy
         */
        public IRACAutoScanResPack createFromParcel(Parcel in) {
            // Read serialized concrete strategy from parcel
            return (IRACAutoScanResPack) in.readSerializable();
        }

        /**
         * Required by Creator
         */
        public IRACAutoScanResPack[] newArray(int size) {
            return new IRACAutoScanResPack[size];
        }
    };
}
