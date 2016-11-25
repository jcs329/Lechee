package com.quantatw.sls.pack.Weather;

import android.os.Parcel;
import android.os.Parcelable;

import com.quantatw.sls.object.CityData;
import com.quantatw.sls.pack.base.BaseResPack;

import java.util.ArrayList;

public class CityListResPack extends BaseResPack {
    private static final long serialVersionUID = 2848717064173990495L;

    private ArrayList<CityData> list;

    public ArrayList<CityData> getCityList() {
        return list;
    }
    public void setCityList(ArrayList<CityData> list) {
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
    public static final Creator<CityListResPack> CREATOR = new Parcelable.Creator<CityListResPack>() {

        /**
         * Read the serialized concrete strategy from the parcel.
         *
         * @param in
         *            The parcel to read from
         * @return An AbstractStrategy
         */
        public CityListResPack createFromParcel(Parcel in) {
            // Read serialized concrete strategy from parcel
            return (CityListResPack) in.readSerializable();
        }

        /**
         * Required by Creator
         */
        public CityListResPack[] newArray(int size) {
            return new CityListResPack[size];
        }
    };
}
