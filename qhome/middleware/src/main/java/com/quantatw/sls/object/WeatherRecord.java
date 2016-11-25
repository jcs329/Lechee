package com.quantatw.sls.object;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;


public class WeatherRecord  implements Serializable, Parcelable {
    private static final long serialVersionUID = 8077779743615868757L;

    private int cityId;
    private String cityName;

    private String date;

    private double minTemp;
    private double maxTemp;
    private String weatherStatus;


    public int getCityId() {
        return cityId;
    }
    public void setCityId(int cityId) {
        this.cityId = cityId;
    }

    public String getCityName() {
        return cityName;
    }
    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getDate() {
        return date;
    }
    public void setDate(String date) {
        this.date = date;
    }

    public double getMinTemp() {
        return minTemp;
    }
    public void setMinTemp(double minTemp) {
        this.minTemp = minTemp;
    }

    public double getMaxTemp() {
        return maxTemp;
    }
    public void setMaxTemp(double maxTemp) {
        this.maxTemp = maxTemp;
    }

    public String getWeatherStatus() {
        return weatherStatus;
    }
    public void setWeatherStatus(String weatherStatus) {
        this.weatherStatus = weatherStatus;
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
    public static final Creator<WeatherRecord> CREATOR = new Parcelable.Creator<WeatherRecord>() {

        /**
         * Read the serialized concrete strategy from the parcel.
         *
         * @param in
         *            The parcel to read from
         * @return An AbstractStrategy
         */
        public WeatherRecord createFromParcel(Parcel in) {
            // Read serialized concrete strategy from parcel
            return (WeatherRecord) in.readSerializable();
        }

        /**
         * Required by Creator
         */
        public WeatherRecord[] newArray(int size) {
            return new WeatherRecord[size];
        }
    };
}
