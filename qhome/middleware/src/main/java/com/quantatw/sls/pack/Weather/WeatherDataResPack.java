package com.quantatw.sls.pack.Weather;

import android.os.Parcel;
import android.os.Parcelable;

import com.quantatw.sls.object.WeatherRecord;
import com.quantatw.sls.object.WeatherToday;
import com.quantatw.sls.pack.base.BaseResPack;

import java.util.ArrayList;

public class WeatherDataResPack extends BaseResPack {
    private static final long serialVersionUID = -1405801861488609900L;

    private WeatherToday todayWeather;
    private ArrayList<WeatherRecord> list;

    public WeatherToday getTodayWeather() {
        return todayWeather;
    }
    public void setTodayWeather(WeatherToday todayWeather) {
        this.todayWeather = todayWeather;
    }

    public ArrayList<WeatherRecord> getWeatherList() {
        return list;
    }
    public void setWeatherList(ArrayList<WeatherRecord> list) {
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
    public static final Creator<WeatherDataResPack> CREATOR = new Parcelable.Creator<WeatherDataResPack>() {

        /**
         * Read the serialized concrete strategy from the parcel.
         *
         * @param in
         *            The parcel to read from
         * @return An AbstractStrategy
         */
        public WeatherDataResPack createFromParcel(Parcel in) {
            // Read serialized concrete strategy from parcel
            return (WeatherDataResPack) in.readSerializable();
        }

        /**
         * Required by Creator
         */
        public WeatherDataResPack[] newArray(int size) {
            return new WeatherDataResPack[size];
        }
    };
}
