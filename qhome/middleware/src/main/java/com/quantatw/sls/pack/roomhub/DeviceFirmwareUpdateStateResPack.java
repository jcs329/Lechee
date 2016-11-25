package com.quantatw.sls.pack.roomhub;

import android.os.Parcel;

import com.quantatw.sls.pack.base.BaseReqPack;
import com.quantatw.sls.pack.base.BaseResPack;

public class DeviceFirmwareUpdateStateResPack extends BaseResPack {
    private static final long serialVersionUID = -667915931589919726L;

    private int state;
    private String stateMsg;

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getStateMsg() {
        return stateMsg;
    }

    public void setStateMsg(String stateMsg) {
        this.stateMsg = stateMsg;
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
    public static final Creator<DeviceFirmwareUpdateStateResPack> CREATOR = new Creator<DeviceFirmwareUpdateStateResPack>() {

        /**
         * Read the serialized concrete strategy from the parcel.
         *
         * @param in
         *            The parcel to read from
         * @return An AbstractStrategy
         */
        public DeviceFirmwareUpdateStateResPack createFromParcel(Parcel in) {
            // Read serialized concrete strategy from parcel
            return (DeviceFirmwareUpdateStateResPack) in.readSerializable();
        }

        /**
         * Required by Creator
         */
        public DeviceFirmwareUpdateStateResPack[] newArray(int size) {
            return new DeviceFirmwareUpdateStateResPack[size];
        }
    };
}
