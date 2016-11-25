package com.quantatw.sls.pack.device;

import android.os.Parcel;

import com.quantatw.sls.pack.base.BaseReqPack;

public class IRCommandReqPack extends BaseReqPack {

	/**
	 *
	 */
	private static final long serialVersionUID = 1171010290955255311L;
	private int power;	// power status
	private int temp;	// Temperature value
	private int mode;	// working mode
	private int swing;	// swing angle
	private int fan;	// fan level
	private int timerOn;	// timing on
	private int timerOff;	// timing off
	private String userId;	//user unique id

 	public void setPower(int power) { this.power = power; }

	public int getPower() { return this.power; }

	public void setTemp(int temp) { this.temp = temp; }

	public int getTemp() { return this.temp; }

	public void setMode(int mode) { this.mode = mode; }

	public int getMode() { return this.mode; }

	public void setSwing(int swing) { this.swing = swing; }

	public int getSwing() { return this.swing; }

	public void setFan(int fan) { this.fan = fan; }

	public int getFan() { return this.fan; }

	public void setTimeOn(int timerOn) { this.timerOn = timerOn; }

	public int getTimeOn() { return this.timerOn; }

	public void setTimeOff(int timerOff) { this.timerOff = timerOff; }

	public int getTimeOff() { return this.timerOff; }

	public void setUserId(String userId) { this.userId = userId; }

	public String getUserId() { return this.userId; }

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
	public static final Creator<IRCommandReqPack> CREATOR = new Creator<IRCommandReqPack>() {

		/**
		 * Read the serialized concrete strategy from the parcel.
		 * 
		 * @param in
		 *            The parcel to read from
		 * @return An AbstractStrategy
		 */
		public IRCommandReqPack createFromParcel(Parcel in) {
			// Read serialized concrete strategy from parcel
			return (IRCommandReqPack) in.readSerializable();
		}

		/**
		 * Required by Creator
		 */
		public IRCommandReqPack[] newArray(int size) {
			return new IRCommandReqPack[size];
		}
	};
}
