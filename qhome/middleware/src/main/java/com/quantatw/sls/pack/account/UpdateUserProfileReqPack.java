package com.quantatw.sls.pack.account;

import com.quantatw.sls.pack.base.BaseReqPack;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class UpdateUserProfileReqPack extends BaseReqPack {
    private static final long serialVersionUID = 7218919990471165284L;

    private String userName;
    public String getUserName() {
        return userName;
    }
    public void setUserName(String userName) {
        this.userName = userName;
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
    public static final Creator<UpdateUserProfileReqPack> CREATOR = new Parcelable.Creator<UpdateUserProfileReqPack>() {

        /**
         * Read the serialized concrete strategy from the parcel.
         *
         * @param in
         *            The parcel to read from
         * @return An AbstractStrategy
         */
        public UpdateUserProfileReqPack createFromParcel(Parcel in) {
            // Read serialized concrete strategy from parcel
            return (UpdateUserProfileReqPack) in.readSerializable();
        }

        /**
         * Required by Creator
         */
        public UpdateUserProfileReqPack[] newArray(int size) {
            return new UpdateUserProfileReqPack[size];
        }
    };
}
