package com.quantatw.sls.device;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class DeviceSignal implements Serializable , Parcelable{
	private static final long serialVersionUID = -2618503442753567798L;

	private String roomHubUUID;
	private String signal;

	public void setRoomHubUUID(String roomHubUUID) { this.roomHubUUID = roomHubUUID; }

	public String getRoomHubUUID() { return this.roomHubUUID; }

	public void setSignal(String signal) { this.signal = signal; }

	public String getSignal() { return this.signal; }
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
	public static final Creator<DeviceSignal> CREATOR = new Creator<DeviceSignal>() {

		/**
		 * Read the serialized concrete strategy from the parcel.
		 *
		 * @param in
		 *            The parcel to read from
		 * @return An AbstractStrategy
		 */
		public DeviceSignal createFromParcel(Parcel in) {
			// Read serialized concrete strategy from parcel
			return (DeviceSignal) in.readSerializable();
		}

		/**
		 * Required by Creator
		 */
		public DeviceSignal[] newArray(int size) {
			return new DeviceSignal[size];
		}
	};
}
