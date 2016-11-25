package com.quantatw.sls.pack.healthcare;

import android.os.Parcel;

import com.quantatw.sls.pack.base.BaseResPack;

import java.util.ArrayList;

public class BPMLastHistoryResPack extends BaseResPack {
    private static final long serialVersionUID = 8145655493238350149L;

    private BPMDataInfo data;

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
    public static final Creator<BPMLastHistoryResPack> CREATOR = new Creator<BPMLastHistoryResPack>() {

        /**
         * Read the serialized concrete strategy from the parcel.
         *
         * @param in
         *            The parcel to read from
         * @return An AbstractStrategy
         */
        public BPMLastHistoryResPack createFromParcel(Parcel in) {
            // Read serialized concrete strategy from parcel
            return (BPMLastHistoryResPack) in.readSerializable();
        }

        /**
         * Required by Creator
         */
        public BPMLastHistoryResPack[] newArray(int size) {
            return new BPMLastHistoryResPack[size];
        }
    };

    public BPMDataInfo getData() {
        return data;
    }
}
