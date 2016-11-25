package com.quantatw.sls.pack.Weather;

import android.os.Parcel;
import android.os.Parcelable;

import com.quantatw.sls.object.TownData;
import com.quantatw.sls.pack.base.BaseResPack;

import java.util.ArrayList;

public class TownListResPack extends BaseResPack {
    private static final long serialVersionUID = -2295941195139076004L;
    private ArrayList<TownData> list;

    public ArrayList<TownData> getTownList() {
        return list;
    }
    public void setTownList(ArrayList<TownData> list) {
        this.list = list;
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
    public static final Creator<TownListResPack> CREATOR = new Parcelable.Creator<TownListResPack>() {

        /**
         * Read the serialized concrete strategy from the parcel.
         *
         * @param in
         *            The parcel to read from
         * @return An AbstractStrategy
         */
        public TownListResPack createFromParcel(Parcel in) {
            // Read serialized concrete strategy from parcel
            return (TownListResPack) in.readSerializable();
        }

        /**
         * Required by Creator
         */
        public TownListResPack[] newArray(int size) {
            return new TownListResPack[size];
        }
    };
}
