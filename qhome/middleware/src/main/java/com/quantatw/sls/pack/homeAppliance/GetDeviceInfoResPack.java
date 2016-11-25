package com.quantatw.sls.pack.homeAppliance;

import android.os.Parcel;
import android.os.Parcelable;

import com.quantatw.sls.pack.base.BaseResPack;

public class GetDeviceInfoResPack extends BaseResPack {
    private static final long serialVersionUID = -6132346806765265287L;

    private int method;
    private String townId;
    private String userId;
    private String token;
    private int WiFiBridge; //0: off 1: on

    public int getMethod() {
        return method;
    }

    public void setMethod(int method) {
        this.method = method;
    }

    public String getTownId() {
        return townId;
    }

    public void setTownId(String townId) {
        this.townId = townId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public int getWiFiBridge() {
        return WiFiBridge;
    }

    public void setWiFiBridge(int WiFiBridge) {
        this.WiFiBridge = WiFiBridge;
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
    public static final Creator<GetDeviceInfoResPack> CREATOR = new Parcelable.Creator<GetDeviceInfoResPack>() {

        /**
         * Read the serialized concrete strategy from the parcel.
         *
         * @param in
         *            The parcel to read from
         * @return An AbstractStrategy
         */
        public GetDeviceInfoResPack createFromParcel(Parcel in) {
            // Read serialized concrete strategy from parcel
            return (GetDeviceInfoResPack) in.readSerializable();
        }

        /**
         * Required by Creator
         */
        public GetDeviceInfoResPack[] newArray(int size) {
            return new GetDeviceInfoResPack[size];
        }
    };
}
