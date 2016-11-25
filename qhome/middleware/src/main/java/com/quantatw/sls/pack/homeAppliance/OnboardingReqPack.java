package com.quantatw.sls.pack.homeAppliance;

import android.os.Parcel;
import android.os.Parcelable;

import com.quantatw.sls.pack.base.BaseReqPack;

/**
 * Created by 10110012 on 2016/3/28.
 */
public class OnboardingReqPack extends BaseReqPack{

    private static final long serialVersionUID = -2019604112347168585L;

    private String ssid;
    private String password;
    private int authorise;
    private int encrypt;

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getAuthorise() {
        return authorise;
    }

    public void setAuthorise(int authorise) {
        this.authorise = authorise;
    }

    public int getEncrypt() {
        return encrypt;
    }

    public void setEncrypt(int encrypt) {
        this.encrypt = encrypt;
    }

    /**
     * Flags for special marshaling
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Write the concrete strategy to the Parcel.
     *
     * @param out
     * @param flags
     */
    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeSerializable(this);
    }

    /**
     * The creator that MUST be defined and named "CREATOR" so that the service
     * generated from AIDL can recreate AbstractStrategys after IPC.
     */
    public static final Creator<OnboardingReqPack> CREATOR = new Parcelable.Creator<OnboardingReqPack>() {

        /**
         * Read the serialized concrete strategy from the parcel.
         *
         * @param in
         *            The parcel to read from
         * @return An AbstractStrategy
         */
        public OnboardingReqPack createFromParcel(Parcel in) {
            // Read serialized concrete strategy from parcel
            return (OnboardingReqPack) in.readSerializable();
        }

        /**
         * Required by Creator
         */
        public OnboardingReqPack[] newArray(int size) {
            return new OnboardingReqPack[size];
        }
    };

}
