package com.quantatw.sls.pack.roomhub.ir;

import android.os.Parcel;
import android.os.Parcelable;

import com.quantatw.sls.object.IRCodeNumData;
import com.quantatw.sls.pack.base.BaseResPack;

import java.util.ArrayList;

public class IRCodeNumListResPack extends BaseResPack {
    private static final long serialVersionUID = -4397171843135078033L;
    private ArrayList<IRCodeNumData> data;

    public ArrayList<IRCodeNumData> getCodeNumDataList() {
        return data;
    }
    public void setCodeNumDataList(ArrayList<IRCodeNumData> data) {
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
    public static final Creator<IRCodeNumListResPack> CREATOR = new Parcelable.Creator<IRCodeNumListResPack>() {

        /**
         * Read the serialized concrete strategy from the parcel.
         *
         * @param in
         *            The parcel to read from
         * @return An AbstractStrategy
         */
        public IRCodeNumListResPack createFromParcel(Parcel in) {
            // Read serialized concrete strategy from parcel
            return (IRCodeNumListResPack) in.readSerializable();
        }

        /**
         * Required by Creator
         */
        public IRCodeNumListResPack[] newArray(int size) {
            return new IRCodeNumListResPack[size];
        }
    };
}
