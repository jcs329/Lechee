/**
 * Copyright 2015 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.quantatw.roomhub.gcm;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import  com.quantatw.myapplication.R;
import com.quantatw.roomhub.manager.ReminderData;
import com.quantatw.roomhub.ui.MainActivity;
import com.quantatw.roomhub.utils.GlobalDef;
import com.quantatw.sls.pack.homeAppliance.AcFailRecoverResPack;

import org.json.JSONException;
import org.json.JSONObject;

public class MyGcmListenerService extends GcmListenerService {

    private static final String TAG = "MyGcmListenerService";

    private final String CLOUD_PARAMETER_TYPE = "type";
    private final String CLOUD_PARAMETER_DATA = "data";
    private final String CLOUD_PARAMETER_DETAIL = "detail";
    private final String CLOUD_PARAMETER_URI = "uri";

    private final String NOTIFICATION_TYPE_ALERT_SENSOR = "N0001";
    private final String NOTIFICATION_TYPE_MOTION_DETECT = "N0002";
    private final String NOTIFICATION_TYPE_IR_CMD_SENSOR = "N0003";
    private final String NOTIFICATION_TYPE_CUSTOM = "N0004";
    private final String NOTIFICATION_TYPE_SYSTEM = "N0005";
    private final String NOTIFICATION_TYPE_ROOMHUB_NOTICE = "N0006";

    private final String DETAIL_TAG_SENSOR_DATA_TYPE = "sensorDataType";
    private final String DETAIL_TAG_DEVICE_NAME = "deviceName";
    private final String DETAIL_TAG_UUID = "uuid";
    private final String DETAIL_TAG_STARTTS = "startTs";
    private final String DETAIL_TAG_VIDEOURL = "videoUrl";
    private final String DETAIL_TAG_ORITEMP = "oriTemp";
    private final String DETAIL_TAG_TARTEMP = "tarTemp";
    private final String DETAIL_TAG_NOWTEMP = "nowTemp";
    private final String DETAIL_TAG_TIMEDIFF = "timeDiff";
    private final String DETAIL_TAG_LASTNOTI = "lastNoti";
    private final String DETAIL_TAG_MODE     = "mode";
    private final String DETAIL_TAG_USERID     = "userId";

    private GcmDetailTag.DetailTag_Base getDetailTagByType(JSONObject detail, String type) {
        if(type.equals(NOTIFICATION_TYPE_ALERT_SENSOR)) {
            GcmDetailTag.DetailTag_AlertSensor tag = new GcmDetailTag.DetailTag_AlertSensor();
            try {
                tag.sensorDataType = detail.getString(DETAIL_TAG_SENSOR_DATA_TYPE);
                tag.deviceName = detail.getString(DETAIL_TAG_DEVICE_NAME);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return tag;
        }
        else if(type.equals(NOTIFICATION_TYPE_MOTION_DETECT)) {

        }
        else if(type.equals(NOTIFICATION_TYPE_IR_CMD_SENSOR)) {
            GcmDetailTag.DetailTag_IRCmdSensor tag = new GcmDetailTag.DetailTag_IRCmdSensor();
            try {
                tag.uuid = detail.getString(DETAIL_TAG_UUID);
                tag.oriTemp = detail.getInt(DETAIL_TAG_ORITEMP);
                tag.tarTemp = detail.getInt(DETAIL_TAG_TARTEMP);
                tag.nowTemp = detail.getInt(DETAIL_TAG_NOWTEMP);
                tag.timeDiff = detail.getInt(DETAIL_TAG_TIMEDIFF);
                tag.lastNoti = detail.getInt(DETAIL_TAG_LASTNOTI);
                tag.mode = detail.getInt(DETAIL_TAG_MODE);
                tag.userId = detail.getString(DETAIL_TAG_USERID);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return tag;
        }
        return null;
    }

    /**
     * Called when message is received.
     *
     * @param from SenderID of the sender.
     * @param data Data bundle containing message data as key/value pairs.
     *             For Set of keys use data.keySet().
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(String from, Bundle data) {
        String type = data.getString(CLOUD_PARAMETER_TYPE);
        //String message = data.getString("message");
        // Our tag is "data"
        String message = data.getString(CLOUD_PARAMETER_DATA);
        String detail = data.getString(CLOUD_PARAMETER_DETAIL);
        String uri = data.getString(CLOUD_PARAMETER_URI);

        Log.d(TAG, "From: " + from);
        Log.d(TAG, "Type: " + type);
        Log.d(TAG, "Message: " + message);
        Log.d(TAG, "Detail: " + detail);
        Log.d(TAG, "Uri: " + uri);

        if(type.equals(NOTIFICATION_TYPE_CUSTOM) || type.equals(NOTIFICATION_TYPE_SYSTEM)) {
            StringBuilder sb = new StringBuilder();
            sb.append(message);
            if(!TextUtils.isEmpty(uri)) {
                sb.append("<br />");
                sb.append("<a href=\"" + uri + "\">");
                sb.append(uri);
                sb.append("</a>");
            }
            Intent intent = new Intent(GlobalDef.ACTION_GCM_MESSAGE);
            Bundle bundle = new Bundle();
            bundle.putInt(GlobalDef.GCM_MESSAGE_TYPE_ID,getGcmMessageIdByType(type));
            bundle.putString(GlobalDef.GCM_MESSAGE,message+"\n"+uri);
            bundle.putString(GlobalDef.GCM_HTML_MESSAGE,sb.toString());
            intent.putExtra(GlobalDef.GCM_MESSAGE,bundle);
            sendBroadcast(intent);
        }
        else if(type.equals(NOTIFICATION_TYPE_ROOMHUB_NOTICE)) {
            Gson gson = new GsonBuilder().create();
            AcFailRecoverResPack acFailRecoverResPack = gson.fromJson(message, AcFailRecoverResPack.class);
//            if(acFailRecoverResPack != null && acFailRecoverResPack.getStatus_code()==GlobalDef.STATUS_CODE_SUCCESS) {
//                Intent intent = new Intent(GlobalDef.ACTION_GCM_MESSAGE);
//                Bundle bundle = new Bundle();
//                bundle.putInt(GlobalDef.GCM_MESSAGE_TYPE_ID, getGcmMessageIdByType(type));
//                bundle.putParcelable(GlobalDef.GCM_MESSAGE_ROOMHUB_NOTICE, acFailRecoverResPack);
//                intent.putExtra(GlobalDef.GCM_MESSAGE,bundle);
//                sendBroadcast(intent);
//            }
        }

        /*
        try {
            JSONObject detailData = new JSONObject(data.getString(CLOUD_PARAMETER_DETAIL));

            GcmDetailTag.DetailTag_Base tagBase = getDetailTagByType(detailData, type);
            if(type.equals(NOTIFICATION_TYPE_ALERT_SENSOR)) {
                GcmDetailTag.DetailTag_AlertSensor tag = (GcmDetailTag.DetailTag_AlertSensor)tagBase;
                tag.dump();
                //sendNotification(tag);
            }
            else if(type.equals(NOTIFICATION_TYPE_MOTION_DETECT)) {
                GcmDetailTag.DetailTag_MotionDetect tag = (GcmDetailTag.DetailTag_MotionDetect)tagBase;
                //tag.dump();
            }
            else if(type.equals(NOTIFICATION_TYPE_IR_CMD_SENSOR)) {
                GcmDetailTag.DetailTag_IRCmdSensor tag = (GcmDetailTag.DetailTag_IRCmdSensor)tagBase;
                tag.dump();
                ReminderData reminderData = new ReminderData();
                reminderData.setGcmDetailTag(tag);
                Intent intent = new Intent(GlobalDef.ACTION_GCM_MESSAGE);
                intent.putExtra(GlobalDef.REMINDER_MESSAGE, (Parcelable)reminderData);
                sendBroadcast(intent);
            }
            else if(type.equals(NOTIFICATION_TYPE_CUSTOM)) {

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        */

        if (from.startsWith("/topics/")) {
            // message received from some topic.
        } else {
            // normal downstream message.
        }

        // [START_EXCLUDE]
        /**
         * Production applications would usually process the message here.
         * Eg: - Syncing with server.
         *     - Store message in local database.
         *     - Update UI.
         */

        /**
         * In some cases it may be useful to show a notification indicating to the user
         * that a message was received.
         */
        //sendNotification(message);
        // [END_EXCLUDE]
    }
    // [END receive_message]

    /**
     * Create and show a simple notification containing the received GCM message.
     *
     * @param message GCM message received.
     */
    private void sendNotification(String message) {
        Log.d(TAG, "got message:" + message);
        Intent intent = new Intent(this, MainActivity.class);
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.putExtra(GlobalDef.REMINDER_MESSAGE, (Parcelable) getReminderData());
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0  /*Request code*/ , intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_stat_ic_notification)
                .setContentTitle(getString(R.string.app_name)+" "+getString(R.string.app_notification_title))
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0  /*ID of notification*/, notificationBuilder.build());
    }

    private ReminderData getReminderData() {
        ReminderData data = new ReminderData();
        /*
        data.setReminderMessageType(GlobalDef.ReminderMessageType.REDIRECT_TARGET_UUID);
        data.setUuid("RoomHub-207c8fed2a30");
        */

        data.setReminderMessageType(ReminderData.ReminderMessageType.REDIRECT_LAUNCH_APP);
        return data;

    }

    private int getGcmMessageIdByType(String type) {
        if(type.equals(NOTIFICATION_TYPE_CUSTOM))
            return GlobalDef.GCM_MESSAGE_ID;
        if(type.equals(NOTIFICATION_TYPE_SYSTEM))
            return GlobalDef.GCM_MESSAGE_ID_SYSTEM;
        if(type.equals(NOTIFICATION_TYPE_ROOMHUB_NOTICE))
            return GlobalDef.GCM_MESSAGE_ID_ROOMHUB_NOTICE;
        return 0;
    }
}
