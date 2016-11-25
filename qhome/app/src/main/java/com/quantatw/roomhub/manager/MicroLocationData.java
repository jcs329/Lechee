package com.quantatw.roomhub.manager;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Created by jungle on 2015/10/6.
 */
public class MicroLocationData implements Serializable,Parcelable {

    private String uuid =""; //Alljoyn
    private double distance = 99999999;  //unit centimeter, -1 表示不可測或無距離
    private int proximity = 0 ; // PROXIMITY_IMMEDIATE:1, PROXIMITY_NEAR:2, PROXIMITY_FAR:3, PROXIMITY_UNKNOWN:0
    private int sequence = 99999999;

    public static final Creator<RoomHubProfile> CREATOR = new Creator<RoomHubProfile>() {
        @Override
        public RoomHubProfile createFromParcel(Parcel in) {
            //return new RoomHubProfile(in);
            return (RoomHubProfile) in.readSerializable();
        }

        @Override
        public RoomHubProfile[] newArray(int size) {
            return new RoomHubProfile[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeSerializable(this);
    }

    public String getUuid() {
        return uuid;
    }

    public int getSequence() {
        return sequence;
    }

    public int getProximity() {
        return proximity;
    }

    public double getDistance() { return distance; }

    public void setUuid(String uid) { uuid = uid; }

    public void setSequence(int sequences) { sequence = sequences; }

    public void setProximity(int proxim) { proximity = proxim; }

    public void setDistance(double dist) { distance = dist; }

}
