package com.quantatw.sls.pack.device;

import android.os.Parcel;

import com.quantatw.sls.pack.base.BaseReqPack;

public class IRDeviceScheduleReqPack extends BaseReqPack {

	/**
	 *
	 */
	private static final long serialVersionUID = 1171010290955255311L;
	private int action;	// action, 0 : add / 1 : update / 2 : delete
	private int index;	// schedule index
	private int mode;	// working mode
	private int value;
	private String startTime;	// start time
	private String endTime;	// end time
	private int repeat;	// repeat status
	private int state;
	private int[] weekday;	// weekday

	public void setAction(int action) { this.action = action; }

	public int getAction() { return this.action; }

	public void setIndex(int index) { this.index = index; }

	public int getIndex() { return this.index; }

	public void setMode(int mode) { this.mode = mode; }

	public int getMode() { return this.mode; }

	public void setValue(int value) { this.value = value; }

	public int getValue() { return this.value; }

	public void setStartTime(String startTime) { this.startTime= startTime; }

	public String getStartTime() { return this.startTime; }

	public void setEndTime(String endTime) { this.endTime = endTime; }

	public String getEndTime() { return this.endTime; }

	public void setRepeat(int repeat) { this.repeat = repeat; }

	public int getRepeat() { return this.repeat; }

	public void setState(int state) { this.state = state; }

	public int getState() { return this.state; }

	public void setWeekday(int[] weekday) { this.weekday = weekday; }

	public int[] getWeekday() { return this.weekday; }
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
	public static final Creator<IRDeviceScheduleReqPack> CREATOR = new Creator<IRDeviceScheduleReqPack>() {

		/**
		 * Read the serialized concrete strategy from the parcel.
		 * 
		 * @param in
		 *            The parcel to read from
		 * @return An AbstractStrategy
		 */
		public IRDeviceScheduleReqPack createFromParcel(Parcel in) {
			// Read serialized concrete strategy from parcel
			return (IRDeviceScheduleReqPack) in.readSerializable();
		}

		/**
		 * Required by Creator
		 */
		public IRDeviceScheduleReqPack[] newArray(int size) {
			return new IRDeviceScheduleReqPack[size];
		}
	};
}
