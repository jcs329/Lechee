package com.quantatw.sls.object;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;


public class WeatherToday  implements Serializable, Parcelable {
    private static final long serialVersionUID = -6121829098787837586L;

    private int cityId;
    private String cityName;

    private String townName;
    private String townId;

    private double temperature;
    private double humidity;
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

    public double getTemperature() {
        return temperature;
    }
    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public double getHumidity() {
        return humidity;
    }
    public void setHumidity(double humidity) {
        this.humidity = humidity;
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
    public static final Creator<WeatherToday> CREATOR = new Parcelable.Creator<WeatherToday>() {

        /**
         * Read the serialized concrete strategy from the parcel.
         *
         * @param in
         *            The parcel to read from
         * @return An AbstractStrategy
         */
        public WeatherToday createFromParcel(Parcel in) {
            // Read serialized concrete strategy from parcel
            return (WeatherToday) in.readSerializable();
        }

        /**
         * Required by Creator
         */
        public WeatherToday[] newArray(int size) {
            return new WeatherToday[size];
        }
    };
}
