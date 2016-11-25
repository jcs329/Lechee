package com.quantatw.sls.device;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class IRFANProperty implements Serializable , Parcelable{
	private static final long serialVersionUID = -1063766259605527785L;

	private int swing;

	public void setSwing(int swing) { this.swing = swing; }

	public int getSwing() { return this.swing; }

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
	public static final Creator<IRFANProperty> CREATOR = new Creator<IRFANProperty>() {

		/**
		 * Read the serialized concrete strategy from the parcel.
		 *
		 * @param in
		 *            The parcel to read from
		 * @return An AbstractStrategy
		 */
		public IRFANProperty createFromParcel(Parcel in) {
			// Read serialized concrete strategy from parcel
			return (IRFANProperty) in.readSerializable();
		}

		/**
		 * Required by Creator
		 */
		public IRFANProperty[] newArray(int size) {
			return new IRFANProperty[size];
		}
	};
}
