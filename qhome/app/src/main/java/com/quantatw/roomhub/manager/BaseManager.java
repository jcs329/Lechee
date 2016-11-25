package com.quantatw.roomhub.manager;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Parcelable;

import com.quantatw.roomhub.utils.GlobalDef;

/**
 * Created by erin on 12/9/15.
 */
public class BaseManager {
    private final String TAG=BaseManager.class.getSimpleName();

    public static final int ROOMHUB_MANAGER = 1;
    public static final int ACCOUNT_MANAGER = 2;
    public static final int ONBOARDING_MANAGER = 3;
    public static final int LOCATION_MANAGER = 4;
    public static final int NETWORK_MONITOR = 5;
    public static final int OTA_MANAGER = 6;
    public static final int ACNOTICE_MANAGER = 7;

    public static final int HEALTH_MANAGER = 15;

    private int mCategory;
    private int mId;
    protected Context mContext;
    protected Handler mFailureHandler;

    public BaseManager(Context context, int id) {
        mContext = context;
        mId = id;

    }

    protected void startup() {}
    protected void terminate() {}

    public Handler getFailureHandler() {
        return mFailureHandler;
    }

    int getManagerId() {
        return mId;
    }

    protected void sendReminderMessage(ReminderData reminderData) {
        reminderData.setSenderId(mId);
        Intent intent = new Intent(GlobalDef.ACTION_REMINDER);
        intent.putExtra(GlobalDef.REMINDER_MESSAGE, (Parcelable)reminderData);
        mContext.sendBroadcast(intent);
    }
}
