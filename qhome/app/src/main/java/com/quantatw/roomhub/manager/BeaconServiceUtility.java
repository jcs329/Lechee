package com.radiusnetworks.ibeacon;

import java.util.Calendar;
import java.util.List;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import com.radiusnetworks.ibeacon.IBeaconConsumer;
import com.radiusnetworks.ibeacon.IBeaconManager;
import com.radiusnetworks.ibeacon.BeaconDetactorService;

public class BeaconServiceUtility {

	private Context context;
	private PendingIntent pintent;
	private AlarmManager alarm;
	private Intent iService;

	public BeaconServiceUtility(Context context) {
		super();
		this.context = context;
		iService = new Intent(context, BeaconDetactorService.class);
		alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		pintent = PendingIntent.getService(context, 0, iService, 0);
	}

	/***
	 * Android L (lollipop, API 21) introduced a new problem when trying to invoke implicit intent,
	 * "java.lang.IllegalArgumentException: Service Intent must be explicit"
	 *
	 * If you are using an implicit intent, and know only 1 target would answer this intent,
	 * This method will help you turn the implicit intent into the explicit form.
	 *
	 * Inspired from SO answer: http://stackoverflow.com/a/26318757/1446466
	 * @param context
	 * @param implicitIntent - The original implicit intent
	 * @return Explicit Intent created from the implicit original intent
	 */
	public static Intent createExplicitFromImplicitIntent(Context context, Intent implicitIntent, String packageName, String className) {
		// Retrieve all services that can match the given intent
		ComponentName component = new ComponentName(packageName, className);

		// Create a new intent. Use the old one for extras and such reuse
		Intent explicitIntent = new Intent(implicitIntent);

		// Set the component to be explicit
		explicitIntent.setComponent(component);

		return explicitIntent;
	}

	public void onStart(IBeaconManager iBeaconManager, IBeaconConsumer consumer) {

		stopBackgroundScan();
		iBeaconManager.bind(consumer);

	}

	public void onStop(IBeaconManager iBeaconManager, IBeaconConsumer consumer) {

		iBeaconManager.unBind(consumer);
		startBackgroundScan();

	}

	private void stopBackgroundScan() {

		alarm.cancel(pintent);
		context.stopService(iService);
	}

	private void startBackgroundScan() {

		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.SECOND, 2);
		alarm.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), 360000, pintent); // 6*60 * 1000
		context.startService(iService);
	}

}
