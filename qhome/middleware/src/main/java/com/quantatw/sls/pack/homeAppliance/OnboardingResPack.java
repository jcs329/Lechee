package com.quantatw.sls.pack.homeAppliance;

import android.os.Parcel;
import android.os.Parcelable;

import com.quantatw.sls.pack.base.BaseResPack;

/**
 * Created by 10110012 on 2016/3/28.
 */
public class OnboardingResPack extends BaseResPack {
    private static final long serialVersionUID = -1589123497640258326L;

    private int method;
    private int result;

    public int getMethod() {
        return method;
    }

    public void setMethod(int method) {
        this.method = method;
    }

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
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
    public static final Parcelable.Creator<OnboardingResPack> CREATOR = new Parcelable.Creator<OnboardingResPack>() {

        /**
         * Read the serialized concrete strategy from the parcel.
         *
         * @param in
         *            The parcel to read from
         * @return An AbstractStrategy
         */
        public OnboardingResPack createFromParcel(Parcel in) {
            // Read serialized concrete strategy from parcel
            return (OnboardingResPack) in.readSerializable();
        }

        /**
         * Required by Creator
         */
        public OnboardingResPack[] newArray(int size) {
            return new OnboardingResPack[size];
        }
    };
}
