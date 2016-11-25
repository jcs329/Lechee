package com.quantatw.sls.pack.roomhub.ir;


import android.os.Parcel;
import android.os.Parcelable;

import com.quantatw.sls.pack.base.BaseResPack;

import java.util.ArrayList;

public class IRModeResPack extends BaseResPack {
    private static final long serialVersionUID = 1477158961537718059L;

    private ArrayList<String> data;

    public ArrayList<String> getData() {
        return data;
    }
    public void setData(ArrayList<String> data) {
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
    public static final Creator<IRModeResPack> CREATOR = new Parcelable.Creator<IRModeResPack>() {

        /**
         * Read the serialized concrete strategy from the parcel.
         *
         * @param in
         *            The parcel to read from
         * @return An AbstractStrategy
         */
        public IRModeResPack createFromParcel(Parcel in) {
            // Read serialized concrete strategy from parcel
            return (IRModeResPack) in.readSerializable();
        }

        /**
         * Required by Creator
         */
        public IRModeResPack[] newArray(int size) {
            return new IRModeResPack[size];
        }
    };
}
