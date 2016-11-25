package com.quantatw.roomhub.gcm;

import android.os.Parcel;
import android.os.Parcelable;

import com.quantatw.roomhub.blepair.BLEControllerCallback;
import com.quantatw.sls.pack.base.BaseResPack;

import java.io.Serializable;

/**
 * Created by cherry on 2016/05/20.
 */
public class GCMMessageDetail extends BaseResPack implements Serializable,Parcelable{
    private static final long serialVersionUID = -3524214325065670573L;
    private String roomHubUUID;
    private String msgType;
    private String gcmMessage;

    public String getRoomHubUUID() {
        return roomHubUUID;
    }

    public void setRoomHubUUID(String roomHubUUID) {
        this.roomHubUUID = roomHubUUID;
    }

    public String getMsgType() {
        return msgType;
    }

    public void setMsgType(String msgType) {
        this.msgType = msgType;
    }

    public String getGcmMessage() {
        return gcmMessage;
    }

    public void setGcmMessage(String gcmMessage) {
        this.gcmMessage = gcmMessage;
    }

    /**
     * The creator that MUST be defined and named "CREATOR" so that the service
     * generated from AIDL can recreate AbstractStrategys after IPC.
     */
    public static final Creator<GCMMessageDetail> CREATOR = new Creator<GCMMessageDetail>() {

        /**
         * Read the serialized concrete strategy from the parcel.
         *
         * @param in
         *            The parcel to read from
         * @return An AbstractStrategy
         */
        public GCMMessageDetail createFromParcel(Parcel in) {
            // Read serialized concrete strategy from parcel
            return (GCMMessageDetail) in.readSerializable();
        }

        /**
         * Required by Creator
         */
        public GCMMessageDetail[] newArray(int size) {
            return new GCMMessageDetail[size];
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
}
