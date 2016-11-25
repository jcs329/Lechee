package com.quantatw.sls.pack.roomhub;

import android.os.Parcel;
import android.os.Parcelable;

import com.quantatw.sls.pack.base.BaseResPack;

public class NameChangeResPack extends BaseResPack {
    private static final long serialVersionUID = -194360868155166458L;

    private String values;

    public String getName() {
        return values;
    }
    public void setName(String values) {
        this.values = values;
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
    public static final Creator<NameChangeResPack> CREATOR = new Parcelable.Creator<NameChangeResPack>() {

        /**
         * Read the serialized concrete strategy from the parcel.
         *
         * @param in
         *            The parcel to read from
         * @return An AbstractStrategy
         */
        public NameChangeResPack createFromParcel(Parcel in) {
            // Read serialized concrete strategy from parcel
            return (NameChangeResPack) in.readSerializable();
        }

        /**
         * Required by Creator
         */
        public NameChangeResPack[] newArray(int size) {
            return new NameChangeResPack[size];
        }
    };
}
