package com.quantatw.sls.pack.account;

import android.os.Parcel;
import android.os.Parcelable;

import com.quantatw.sls.pack.base.BaseReqPack;

import java.io.Serializable;

public class AccountLoginReqPack extends BaseReqPack implements Serializable,Parcelable {
    private static final long serialVersionUID = -1586602622230134534L;
    /**
     *
     */
    private String userAccount;
    private String userPw;
    private String clientId;
    public String getUserAccount() {
        return userAccount;
    }
    public void setUserAccount(String userAccount) {
        this.userAccount = userAccount;
    }
    public String getUserPw() {
        return userPw;
    }
    public void setUserPw(String userPw) {
        this.userPw = userPw;
    }

    public String getClientId() {
        return clientId;
    }
    public void setClientId(String clientId) {
        this.clientId = clientId;
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
    public static final Creator<AccountLoginReqPack> CREATOR = new Parcelable.Creator<AccountLoginReqPack>() {

        /**
         * Read the serialized concrete strategy from the parcel.
         *
         * @param in
         *            The parcel to read from
         * @return An AbstractStrategy
         */
        public AccountLoginReqPack createFromParcel(Parcel in) {
            // Read serialized concrete strategy from parcel
            return (AccountLoginReqPack) in.readSerializable();
        }

        /**
         * Required by Creator
         */
        public AccountLoginReqPack[] newArray(int size) {
            return new AccountLoginReqPack[size];
        }
    };

}
