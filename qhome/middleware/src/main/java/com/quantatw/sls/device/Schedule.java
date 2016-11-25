package com.quantatw.sls.device;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class Schedule implements Serializable , Parcelable{
	/**
	 *
	 */
	private static final long serialVersionUID = -3864996291206328985L;

	protected int index;
	protected int mode;
	protected int value;
	protected boolean repeat;
	protected boolean state;
	protected String startTime;
	protected String endTime;
	protected int[] weekday;

	public void setIndex(int index) { this.index = index; }

	public int getIndex() { return this.index; }

	public void setType(int type) { this.mode = type; }

	public int getType() { return this.mode; }

	public void setRepeat(boolean repeat) { this.repeat = repeat; }

	public boolean getRepeat() { return this.repeat; }

	public void setValue(int value) { this.value = value; }

	public int getValue() { return this.value;}

	public void setEnable(boolean enable) { this.state = enable; }

	public boolean getEnable() { return this.state; }

	public void setStartTime(String startTime) { this.startTime = startTime; }

	public String getStartTime() { return this.startTime; }

	public void setEndTime(String endTime) { this.endTime = endTime; }

	public String getEndTime() { return this.endTime; }

	public void setWeek(int[] week) { this.weekday = week; }

	public int[] getWeek() { return this.weekday; }

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
	public static final Creator<Schedule> CREATOR = new Creator<Schedule>() {

		/**
		 * Read the serialized concrete strategy from the parcel.
		 * 
		 * @param in
		 *            The parcel to read from
		 * @return An AbstractStrategy
		 */
		public Schedule createFromParcel(Parcel in) {
			// Read serialized concrete strategy from parcel
			return (Schedule) in.readSerializable();
		}

		/**
		 * Required by Creator
		 */
		public Schedule[] newArray(int size) {
			return new Schedule[size];
		}
	};
}
