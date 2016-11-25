package com.quantatw.sls.object;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;


public class CityData  implements Serializable, Parcelable {
    private static final long serialVersionUID = 4791540417393575895L;

    private String cityName;
    private int cityId;

    public String getCityName() {
        return cityName;
    }
    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public int getCityId() {
        return cityId;
    }
    public void setCityId(int cityId) {
        this.cityId = cityId;
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
    public static final Creator<CityData> CREATOR = new Parcelable.Creator<CityData>() {

        /**
         * Read the serialized concrete strategy from the parcel.
         *
         * @param in
         *            The parcel to read from
         * @return An AbstractStrategy
         */
        public CityData createFromParcel(Parcel in) {
            // Read serialized concrete strategy from parcel
            return (CityData) in.readSerializable();
        }

        /**
         * Required by Creator
         */
        public CityData[] newArray(int size) {
            return new CityData[size];
        }
    };
}
