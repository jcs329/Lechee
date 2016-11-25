package com.quantatw.sls.device;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class IRDeviceAbility implements Serializable , Parcelable{
	/**
	 *
	 */
	private static final long serialVersionUID = -3864996291206328985L;

	protected int mode;
	protected int maxValue;
	protected int minValue;

	public void setMode(int mode) { this.mode = mode; }

	public int getMode() { return this.mode; }

	public void setMaxValue(int maxValue) { this.maxValue = maxValue; }

	public int getMaxValue() { return this.maxValue; }

	public void setMinValue(int minValue) { this.minValue = minValue; }

	public int getMinValue() { return this.minValue; }

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
	public static final Creator<IRDeviceAbility> CREATOR = new Creator<IRDeviceAbility>() {

		/**
		 * Read the serialized concrete strategy from the parcel.
		 * 
		 * @param in
		 *            The parcel to read from
		 * @return An AbstractStrategy
		 */
		public IRDeviceAbility createFromParcel(Parcel in) {
			// Read serialized concrete strategy from parcel
			return (IRDeviceAbility) in.readSerializable();
		}

		/**
		 * Required by Creator
		 */
		public IRDeviceAbility[] newArray(int size) {
			return new IRDeviceAbility[size];
		}
	};
}
