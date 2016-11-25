package com.quantatw.roomhub.manager;

import android.os.Parcel;
import android.os.Parcelable;

import org.alljoyn.onboarding.OnboardingService;

import java.io.Serializable;

/**
 * Created by erin on 9/23/15.
 */
public class OnBoardee implements Serializable,Parcelable {

    protected String ssid;
    protected OnboardingService.AuthType authType;
    protected String password;

    public String getSsid() {
        return ssid;
    }
    
    public static final Creator<OnBoardee> CREATOR = new Parcelable.Creator<OnBoardee>() {

        /**
         * Read the serialized concrete strategy from the parcel.
         *
         * @param in
         *            The parcel to read from
         * @return An AbstractStrategy
         */
        public OnBoardee createFromParcel(Parcel in) {
            // Read serialized concrete strategy from parcel
            return (OnBoardee) in.readSerializable();
        }

        /**
         * Required by Creator
         */
        public OnBoardee[] newArray(int size) {
            return new OnBoardee[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeSerializable(this);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public String toString() {
        return "SSID="+ssid+",authType="+authType+",pass="+password;
    }

}
