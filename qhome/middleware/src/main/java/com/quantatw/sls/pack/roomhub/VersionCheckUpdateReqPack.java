package com.quantatw.sls.pack.roomhub;

import android.os.Parcel;

import com.quantatw.sls.pack.base.BaseReqPack;

public class VersionCheckUpdateReqPack extends BaseReqPack {
    private static final long serialVersionUID = -7838118904233160668L;

    private String version;
    private String uuid;

    public String getVersion() {
        return version;
    }
    public void setVersion(String version) {
        this.version = version;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
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
    public static final Creator<VersionCheckUpdateReqPack> CREATOR = new Creator<VersionCheckUpdateReqPack>() {

        /**
         * Read the serialized concrete strategy from the parcel.
         *
         * @param in
         *            The parcel to read from
         * @return An AbstractStrategy
         */
        public VersionCheckUpdateReqPack createFromParcel(Parcel in) {
            // Read serialized concrete strategy from parcel
            return (VersionCheckUpdateReqPack) in.readSerializable();
        }

        /**
         * Required by Creator
         */
        public VersionCheckUpdateReqPack[] newArray(int size) {
            return new VersionCheckUpdateReqPack[size];
        }
    };
}
