package com.quantatw.sls.device;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.ArrayList;

public class DeviceSetting implements Serializable , Parcelable{
	/**
	 *
	 */
	private static final long serialVersionUID = -3864996291206328985L;

	//protected ArrayList<Schedule> scheduleList;
	protected String scheduleList;

//	public void setScheduleList(ArrayList<Schedule> list) { this.scheduleList = list; }
//
//	public ArrayList<Schedule> getScheduleList() { return this.scheduleList; }

	public void setScheduleList(String list) { this.scheduleList = list; }

	public String getScheduleList() { return this.scheduleList; }


	/**
	 * Flags for special marshaling
	 */
	public int describeContents() {
		return 0;
	}

	/**
	 * Write the concrete strategy to the Parcel.
	 */
	public void writeToParcel(Parcel out, int flags) {
		// Serialize "this", so that we can get it back after IPC
		out.writeSerializable(this);
	}

	/**
	 * The creator that MUST be defined and named "CREATOR" so that the service
	 * generated from AIDL can recreate AbstractStrategys after IPC.
	 */
	public static final Creator<DeviceSetting> CREATOR = new Creator<DeviceSetting>() {

		/**
		 * Read the serialized concrete strategy from the parcel.
		 * 
		 * @param in
		 *            The parcel to read from
		 * @return An AbstractStrategy
		 */
		public DeviceSetting createFromParcel(Parcel in) {
			// Read serialized concrete strategy from parcel
			return (DeviceSetting) in.readSerializable();
		}

		/**
		 * Required by Creator
		 */
		public DeviceSetting[] newArray(int size) {
			return new DeviceSetting[size];
		}
	};
}
