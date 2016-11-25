package com.quantatw.sls.pack.account;

import android.os.Parcel;
import android.os.Parcelable;

import com.quantatw.sls.pack.base.BaseResPack;

import java.io.Serializable;

public class JwtPayloadResPack extends BaseResPack {
    private static final long serialVersionUID = 7248383896177948256L;

    private String unique_name;
    private String userName;
    private String userId;

    public String getUniqueName() {
        return unique_name;
    }
    public void setUniqueName(String unique_name) {
        this.unique_name = unique_name;
    }

    public String getUserName() {
        return userName;
    }
    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
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
    public static final Creator<JwtPayloadResPack> CREATOR = new Parcelable.Creator<JwtPayloadResPack>() {

        /**
         * Read the serialized concrete strategy from the parcel.
         *
         * @param in
         *            The parcel to read from
         * @return An AbstractStrategy
         */
        public JwtPayloadResPack createFromParcel(Parcel in) {
            // Read serialized concrete strategy from parcel
            return (JwtPayloadResPack) in.readSerializable();
        }

        /**
         * Required by Creator
         */
        public JwtPayloadResPack[] newArray(int size) {
            return new JwtPayloadResPack[size];
        }
    };
}
