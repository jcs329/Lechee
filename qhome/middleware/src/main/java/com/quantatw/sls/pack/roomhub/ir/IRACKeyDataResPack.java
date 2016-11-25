package com.quantatw.sls.pack.roomhub.ir;

import android.os.Parcel;
import android.os.Parcelable;

import com.quantatw.sls.object.IRACKeyData;
import com.quantatw.sls.pack.base.BaseResPack;

import java.util.ArrayList;

public class IRACKeyDataResPack extends BaseResPack {
    private static final long serialVersionUID = 7108479504252826342L;
    private ArrayList<IRACKeyData> data;

    public ArrayList<IRACKeyData> getACKeyList() {
        return data;
    }
    public void setACKeyList(ArrayList<IRACKeyData> data) {
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
    public static final Creator<IRACKeyDataResPack> CREATOR = new Parcelable.Creator<IRACKeyDataResPack>() {

        /**
         * Read the serialized concrete strategy from the parcel.
         *
         * @param in
         *            The parcel to read from
         * @return An AbstractStrategy
         */
        public IRACKeyDataResPack createFromParcel(Parcel in) {
            // Read serialized concrete strategy from parcel
            return (IRACKeyDataResPack) in.readSerializable();
        }

        /**
         * Required by Creator
         */
        public IRACKeyDataResPack[] newArray(int size) {
            return new IRACKeyDataResPack[size];
        }
    };
}
