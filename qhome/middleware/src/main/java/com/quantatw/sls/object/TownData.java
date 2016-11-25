package com.quantatw.sls.object;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;


public class TownData  implements Serializable, Parcelable {
    private static final long serialVersionUID = -5954594064127814749L;
    private String townName;
    private String townId;

    public String getTownName() {
        return townName;
    }
    public void setTownName(String townName) {
        this.townName = townName;
    }

    public String getTownId() {
        return townId;
    }
    public void setTownId(String townId) {
        this.townId = townId;
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
    public static final Creator<TownData> CREATOR = new Parcelable.Creator<TownData>() {

        /**
         * Read the serialized concrete strategy from the parcel.
         *
         * @param in
         *            The parcel to read from
         * @return An AbstractStrategy
         */
        public TownData createFromParcel(Parcel in) {
            // Read serialized concrete strategy from parcel
            return (TownData) in.readSerializable();
        }

        /**
         * Required by Creator
         */
        public TownData[] newArray(int size) {
            return new TownData[size];
        }
    };
}
