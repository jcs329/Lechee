package com.quantatw.sls.pack.base;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by erin on 5/27/16.
 */
public class BaseStatusPack extends BaseResPack {
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
    public static final Creator<BaseStatusPack> CREATOR = new Parcelable.Creator<BaseStatusPack>() {

        /**
         * Read the serialized concrete strategy from the parcel.
         *
         * @param in
         *            The parcel to read from
         * @return An AbstractStrategy
         */
        public BaseStatusPack createFromParcel(Parcel in) {
            // Read serialized concrete strategy from parcel
            return (BaseStatusPack) in.readSerializable();
        }

        /**
         * Required by Creator
         */
        public BaseStatusPack[] newArray(int size) {
            return new BaseStatusPack[size];
        }
    };
}
