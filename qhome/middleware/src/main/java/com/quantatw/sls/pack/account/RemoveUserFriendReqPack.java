package com.quantatw.sls.pack.account;

import android.os.Parcel;

import com.quantatw.sls.pack.base.BaseReqPack;

public class RemoveUserFriendReqPack extends BaseReqPack {
    private static final long serialVersionUID = 7218919990471165284L;

    private String userAccount;
    public String getUserAccount() {
        return userAccount;
    }
    public void setUserAccount(String userAccount) {
        this.userAccount = userAccount;
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
    public static final Creator<RemoveUserFriendReqPack> CREATOR = new Creator<RemoveUserFriendReqPack>() {

        /**
         * Read the serialized concrete strategy from the parcel.
         *
         * @param in
         *            The parcel to read from
         * @return An AbstractStrategy
         */
        public RemoveUserFriendReqPack createFromParcel(Parcel in) {
            // Read serialized concrete strategy from parcel
            return (RemoveUserFriendReqPack) in.readSerializable();
        }

        /**
         * Required by Creator
         */
        public RemoveUserFriendReqPack[] newArray(int size) {
            return new RemoveUserFriendReqPack[size];
        }
    };
}
