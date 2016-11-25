package com.quantatw.roomhub.gcm;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.io.Serializable;

/**
 * Created by erin on 12/29/15.
 */
public class GcmDetailTag {
    private static final String TAG="GcmDetailTag";

    public static class DetailTag_Base {
        void dump(String msg) {
            Log.d(TAG, msg);
        }
    }

    public static class DetailTag_AlertSensor extends DetailTag_Base{
        String sensorDataType;
        String deviceName;
        void dump() {
            dump("AlertSensor[sensorDataType="+sensorDataType+",deviceName="+deviceName+"]");
        }
    }

    public static class DetailTag_MotionDetect extends DetailTag_Base {

    }

    public static class DetailTag_IRCmdSensor extends DetailTag_Base implements Serializable,Parcelable {
        String uuid;
        int oriTemp;
        int tarTemp;
        int nowTemp;
        int timeDiff;
        int lastNoti;
        int mode;
        String userId;

        public void setUuid(String uuid) {
            this.uuid = uuid;
        }

        public String getUuid() {
            return this.uuid;
        }

        public void setOriTemp(int oriTemp) {
            this.oriTemp = oriTemp;
        }

        public int getOriTemp() {
            return this.oriTemp;
        }

        public void setTarTemp(int tarTemp) {
            this.tarTemp = tarTemp;
        }

        public int getTarTemp() {
            return this.tarTemp;
        }

        public void setNowTemp(int nowTemp) {
            this.nowTemp = nowTemp;
        }

        public int getNowTemp() {
            return this.nowTemp;
        }

        public void setTimeDiff(int timeDiff) {
            this.timeDiff = timeDiff;
        }

        public int getTimeDiff() {
            return this.timeDiff;
        }

        public void setLastNoti(int lastNoti) {
            this.lastNoti = lastNoti;
        }

        public int getLastNoti() {
            return this.lastNoti;
        }

        public void setMode(int mode) { this.mode = mode; }

        public int getMode() { return this.mode; }

        public void setUserId(String userId) { this.userId = userId; }

        public String getUserId() { return this.userId; }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeSerializable(this);
        }

        /**
         * The creator that MUST be defined and named "CREATOR" so that the service
         * generated from AIDL can recreate AbstractStrategys after IPC.
         */
        public static final Creator<DetailTag_IRCmdSensor> CREATOR = new Creator<DetailTag_IRCmdSensor>() {

            /**
             * Read the serialized concrete strategy from the parcel.
             *
             * @param in
             *            The parcel to read from
             * @return An AbstractStrategy
             */
            public DetailTag_IRCmdSensor createFromParcel(Parcel in) {
                // Read serialized concrete strategy from parcel
                return (DetailTag_IRCmdSensor) in.readSerializable();
            }

            /**
             * Required by Creator
             */
            public DetailTag_IRCmdSensor[] newArray(int size) {
                return new DetailTag_IRCmdSensor[size];
            }
        };

        void dump() {
            dump("IRCmdSensor[uuid="+uuid+",oriTemp="+oriTemp+",tarTemp="+tarTemp
                    +",nowTemp="+nowTemp+",timeDiff="+timeDiff+",lastNoti="
                    +lastNoti+",mode="+mode+",userId="+userId+"]");
        }
    }

}
