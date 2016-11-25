package com.quantatw.sls.pack.device;

import com.quantatw.sls.pack.base.BaseReqPack;

import android.os.Parcel;
import android.os.Parcelable;

public class ModifyDeviceNameReqPack extends BaseReqPack {

    /**
     *
     */
    private static final long serialVersionUID = -8376122139542826465L;

    private String deviceName;

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
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
    public static final Creator<ModifyDeviceNameReqPack> CREATOR = new Parcelable.Creator<ModifyDeviceNameReqPack>() {

        /**
         * Read the serialized concrete strategy from the parcel.
         *
         * @param in
         *            The parcel to read from
         * @return An AbstractStrategy
         */
        public ModifyDeviceNameReqPack createFromParcel(Parcel in) {
            // Read serialized concrete strategy from parcel
            return (ModifyDeviceNameReqPack) in.readSerializable();
        }

        /**
         * Required by Creator
         */
        public ModifyDeviceNameReqPack[] newArray(int size) {
            return new ModifyDeviceNameReqPack[size];
        }
    };
}
