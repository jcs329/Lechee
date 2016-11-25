package com.quantatw.sls.pack.device;

import android.os.Parcel;
import android.os.Parcelable;

import com.quantatw.sls.pack.base.BaseReqPack;

public class AddDeviceUserReqPack extends BaseReqPack {
    private static final long serialVersionUID = 2331179184136733995L;

    public static final String ROLE_OWNER = "Owner";
    public static final String ROLE_USER = "User";

    private String userId;
    private String roleName;

    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getRoleName() {
        return roleName;
    }
    public void setRoleName(String roleName) {
        this.roleName = roleName;
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
    public static final Parcelable.Creator<AddDeviceUserReqPack> CREATOR = new Parcelable.Creator<AddDeviceUserReqPack>() {

        /**
         * Read the serialized concrete strategy from the parcel.
         *
         * @param in
         *            The parcel to read from
         * @return An AbstractStrategy
         */
        public AddDeviceUserReqPack createFromParcel(Parcel in) {
            // Read serialized concrete strategy from parcel
            return (AddDeviceUserReqPack) in.readSerializable();
        }

        /**
         * Required by Creator
         */
        public AddDeviceUserReqPack[] newArray(int size) {
            return new AddDeviceUserReqPack[size];
        }
    };
}
