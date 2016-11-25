package com.quantatw.roomhub.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.quantatw.roomhub.utils.Utils;

/**
 * Created by erin on 12/3/15.
 */
public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(final Context context, Intent intent) {
        if(Utils.isNotificationOn(context)) {
            Intent serviceIntent = new Intent(context, RoomHubService.class);
            context.startService(serviceIntent);
        }
    }
}
