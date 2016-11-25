package com.quantatw.roomhub.manager;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.quantatw.roomhub.gcm.GcmDetailTag;
import com.quantatw.roomhub.utils.FailureCauseInfo;
import com.quantatw.roomhub.utils.RoomHubFailureCause;

import java.io.Serializable;

/**
 * Created by erin on 12/10/15.
 */
public class ReminderData implements Serializable,Parcelable {

    public static final class ReminderMessageType {
        public static final int REDIRECT_LAUNCH_APP = 0;
        public static final int REDIRECT_TARGET_UUID = 1;
    }

    public ReminderData() {
    }

    public void discard(Context context) {
        RoomHubFailureCause.getInstance(context).discardFailCause(messageId, message_extraIndex);
    }

    private ReminderData(Parcel in) {
        senderId = in.readInt();
        reminderMessageType = in.readInt();
        uuid = in.readString();
        messageId = in.readInt();
        message_extraIndex = in.readInt();
        simpleMessage = in.readString();
        timestamp = in.readLong();
        gcmDetailTag = (GcmDetailTag.DetailTag_IRCmdSensor) in.readSerializable();
        assetUuid = in.readString();
        msgType = in.readString();
    }

    int senderId;
    /* ReminderMessageType */
    int reminderMessageType;
    String uuid;
    /* Failure ID. see FailureCauseInfo/RoomHubFailureCause */
    int messageId;
    int message_extraIndex;
    String simpleMessage;
    long timestamp;
    boolean silent; // only save record and won't show any dialog/toast/notification

    // new GCM message for general
    String assetUuid;
    String msgType;

    GcmDetailTag.DetailTag_IRCmdSensor gcmDetailTag;

    public void setSenderId(int senderId) { this.senderId = senderId; }

    public int getSenderId() { return this.senderId; }

    public void setReminderMessageType(int type) { this.reminderMessageType = type; }

    public int getReminderMessageType() { return this.reminderMessageType; }

    public void setUuid(String uuid) { this.uuid = uuid; }

    public String getUuid() { return this.uuid; }

    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }

    public int getMessageId() {
        return this.messageId;
    }

    public void setMessage_extraIndex(int message_extraIndex) {
        this.message_extraIndex = message_extraIndex;
    }

    public int getMessage_extraIndex() {
        return this.message_extraIndex;
    }

    public void setSimpleMessage(String message) { this.simpleMessage = message; }

    public String getSimpleMessage() { return this.simpleMessage; }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getTimestamp() { return this.timestamp; }

	public void setGcmDetailTag(GcmDetailTag.DetailTag_IRCmdSensor gcmDetailTag) {
        this.gcmDetailTag = gcmDetailTag;
    }

    public GcmDetailTag.DetailTag_IRCmdSensor getGcmDetailTag() {
        return this.gcmDetailTag;
    }

    public boolean isSilent() {
        return silent;
    }

    public void setSilent(boolean silent) {
        this.silent = silent;
    }

    public String getAssetUuid() {
        return assetUuid;
    }

    public void setAssetUuid(String assetUuid) {
        this.assetUuid = assetUuid;
    }

    public String getMsgType() {
        return msgType;
    }

    public void setMsgType(String msgType) {
        this.msgType = msgType;
    }

    /*
            * For Sender:
            * obtain new FailCause to dispatch Reminder message
             */
    public FailureCauseInfo obtainFailureCauseInfo(Context context) {
        FailureCauseInfo failureCauseInfo =
                RoomHubFailureCause.getInstance(context).obtainFailCause(messageId);
        this.message_extraIndex = failureCauseInfo.getIndex();
        return failureCauseInfo;
    }

    /*
    * For Receiver , display clients or any one who wants to lookup data
    *
     */
    public FailureCauseInfo getFailureCauseInfo(Context context) {
        return RoomHubFailureCause.getInstance(context).getFailCause(messageId, message_extraIndex);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(senderId);
        dest.writeInt(reminderMessageType);
        dest.writeString(uuid);
        dest.writeInt(messageId);
        dest.writeInt(message_extraIndex);
        dest.writeString(simpleMessage);
        dest.writeLong(timestamp);
        dest.writeSerializable(gcmDetailTag);
        dest.writeString(assetUuid);
        dest.writeString(msgType);
    }

    /**
     * The creator that MUST be defined and named "CREATOR" so that the service
     * generated from AIDL can recreate AbstractStrategys after IPC.
     */
    public static final Creator<ReminderData> CREATOR = new Creator<ReminderData>() {

        /**
         * Read the serialized concrete strategy from the parcel.
         *
         * @param in
         *            The parcel to read from
         * @return An AbstractStrategy
         */
        public ReminderData createFromParcel(Parcel in) {
            // Read serialized concrete strategy from parcel
            return new ReminderData(in);
        }

        /**
         * Required by Creator
         */
        public ReminderData[] newArray(int size) {
            return new ReminderData[size];
        }
    };

}
