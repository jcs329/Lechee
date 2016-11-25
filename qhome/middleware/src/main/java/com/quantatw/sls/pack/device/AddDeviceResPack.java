package com.quantatw.sls.pack.device;

import android.os.Parcel;
import android.os.Parcelable;

import com.quantatw.sls.device.AlljoynDevice;
import com.quantatw.sls.device.CloudDevice;
import com.quantatw.sls.pack.base.BaseResPack;

public class AddDeviceResPack extends BaseResPack {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1814071515332031311L;
	
	private AlljoynDevice device;

	public AlljoynDevice getDevice() {
		return device;
	}

	public void setDevice(AlljoynDevice device) {
		this.device = device;
	}
	
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
	public static final Creator<AddDeviceResPack> CREATOR = new Parcelable.Creator<AddDeviceResPack>() {

		/**
		 * Read the serialized concrete strategy from the parcel.
		 * 
		 * @param in
		 *            The parcel to read from
		 * @return An AbstractStrategy
		 */
		public AddDeviceResPack createFromParcel(Parcel in) {
			// Read serialized concrete strategy from parcel
			return (AddDeviceResPack) in.readSerializable();
		}

		/**
		 * Required by Creator
		 */
		public AddDeviceResPack[] newArray(int size) {
			return new AddDeviceResPack[size];
		}
	};
}
