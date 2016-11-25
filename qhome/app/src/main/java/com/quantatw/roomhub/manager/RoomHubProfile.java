package com.quantatw.roomhub.manager;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Created by 95011613 on 2015/9/23.
 */
public class RoomHubProfile implements Serializable,Parcelable{
    protected int account_id;
    protected String uuid;
    protected int fun_mode;
    protected int temp;
    protected int swing;
    protected int fan;

    public int getAccountId() {
        return account_id;
    }

    public void setAccountId(int accountId) {
        this.account_id = accountId;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public int getFunMode() {
        return fun_mode;
    }

    public void setFunMode(int fun_mode) {
        this.fun_mode = fun_mode;
    }

    public int getTemp() {
        return temp;
    }

    public void setTemp(int temp) {
        this.temp = temp;
    }

    public int getSwing() {
        return swing;
    }

    public void setSwing(int swing) {
        this.swing = swing;
    }

    public int getFan() {
        return fan;
    }

    public void setFan(int fan) {
        this.fan = fan;
    }
/*
    protected RoomHubProfile(Parcel in) {
        account_id = in.readInt();
        uuid = in.readString();
        temp = in.readInt();
        swing = in.readInt();
    }
*/
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
}
