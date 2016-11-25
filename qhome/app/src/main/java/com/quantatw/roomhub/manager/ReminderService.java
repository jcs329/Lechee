package com.quantatw.roomhub.manager;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.widget.Toast;

import com.quantatw.roomhub.utils.GlobalDef;

public class ReminderService extends Service {
    ReminderReceiver mReminderReceiver;

    public ReminderService() {
    }

    @Override
    public void onCreate() {
        mReminderReceiver = new ReminderReceiver();

        IntentFilter filter = new IntentFilter();
        filter.addAction(GlobalDef.ACTION_REMINDER);
        mReminderReceiver = new ReminderReceiver();
        registerReceiver(mReminderReceiver, filter);

        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        this.unregisterReceiver(mReminderReceiver);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public class ReminderReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub

            if(GlobalDef.ACTION_REMINDER.equals(intent.getAction())){
                String msg=intent.getStringExtra(GlobalDef.REMINDER_MESSAGE);
                Toast.makeText(getBaseContext(),msg,
                        Toast.LENGTH_LONG).show();
            }
        }
    }
}
