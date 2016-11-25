package com.quantatw.sls.pack.account;

import android.os.Parcel;
import android.os.Parcelable;

import com.quantatw.sls.pack.base.BaseReqPack;

public class AccountInfoCheckReqPack extends BaseReqPack {
    private static final long serialVersionUID = 8463942866013176995L;

    public final static String TYPE_USER_ACCOUNT = "userAccount";
    public final static String TYPE_EMAIL = "email";

    private String type;
    private String userAccount;
    private String email;

    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }

    public String getUserAccount() {
        return userAccount;
    }
    public void setUserAccount(String userAccount) {
        this.userAccount = userAccount;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
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
    public static final Creator<AccountInfoCheckReqPack> CREATOR = new Parcelable.Creator<AccountInfoCheckReqPack>() {

        /**
         * Read the serialized concrete strategy from the parcel.
         *
         * @param in
         *            The parcel to read from
         * @return An AbstractStrategy
         */
        public AccountInfoCheckReqPack createFromParcel(Parcel in) {
            // Read serialized concrete strategy from parcel
            return (AccountInfoCheckReqPack) in.readSerializable();
        }

        /**
         * Required by Creator
         */
        public AccountInfoCheckReqPack[] newArray(int size) {
            return new AccountInfoCheckReqPack[size];
        }
    };
}
