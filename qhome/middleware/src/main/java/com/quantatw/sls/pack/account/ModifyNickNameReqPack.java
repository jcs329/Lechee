package com.quantatw.sls.pack.account;

import android.os.Parcel;

import com.quantatw.sls.pack.base.BaseReqPack;

public class ModifyNickNameReqPack extends BaseReqPack {

    private static final long serialVersionUID = 6840369900615917990L;

    private String nickName;
    private String userAccount;

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nick_name) {
        this.nickName = nick_name;
    }

    public String getUserAccount() {
        return userAccount;
    }
    public void setUserAccount(String account) {
        this.userAccount = account;
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
    public static final Creator<ModifyNickNameReqPack> CREATOR = new Creator<ModifyNickNameReqPack>() {

        /**
         * Read the serialized concrete strategy from the parcel.
         *
         * @param in
         *            The parcel to read from
         * @return An AbstractStrategy
         */
        public ModifyNickNameReqPack createFromParcel(Parcel in) {
            // Read serialized concrete strategy from parcel
            return (ModifyNickNameReqPack) in.readSerializable();
        }

        /**
         * Required by Creator
         */
        public ModifyNickNameReqPack[] newArray(int size) {
            return new ModifyNickNameReqPack[size];
        }
    };
}
