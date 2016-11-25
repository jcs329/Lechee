package com.quantatw.sls.pack.base;

import android.os.Parcel;
import android.os.Parcelable;

public class BaseAlljoynResPack extends  BaseResPack {
    private static final long serialVersionUID = -760218950142606757L;

    private int method;

    public int getMethod() {
        return method;
    }

    public void setMethod(int method) {
        this.method = method;
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
    public static final Creator<BaseAlljoynResPack> CREATOR = new Parcelable.Creator<BaseAlljoynResPack>() {

        /**
         * Read the serialized concrete strategy from the parcel.
         *
         * @param in
         *            The parcel to read from
         * @return An AbstractStrategy
         */
        public BaseAlljoynResPack createFromParcel(Parcel in) {
            // Read serialized concrete strategy from parcel
            return (BaseAlljoynResPack) in.readSerializable();
        }

        /**
         * Required by Creator
         */
        public BaseAlljoynResPack[] newArray(int size) {
            return new BaseAlljoynResPack[size];
        }
    };
}
