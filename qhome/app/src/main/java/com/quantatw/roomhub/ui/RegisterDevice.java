package com.quantatw.roomhub.ui;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Created by erin on 2/3/16.
 */
public class RegisterDevice implements Serializable,Parcelable {
    String uuid;
    String name;
    String version;

    RegisterDevice(String uuid, String name, String version) {
        this.uuid = uuid;
        this.name = name;
        this.version = version;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
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
    public static final Creator<RegisterDevice> CREATOR = new Creator<RegisterDevice>() {

        /**
         * Read the serialized concrete strategy from the parcel.
         *
         * @param in
         *            The parcel to read from
         * @return An AbstractStrategy
         */
        public RegisterDevice createFromParcel(Parcel in) {
            // Read serialized concrete strategy from parcel
              return (RegisterDevice) in.readSerializable();
        }

        /**
         * Required by Creator
         */
        public RegisterDevice[] newArray(int size) {
            return new RegisterDevice[size];
        }
    };
}
