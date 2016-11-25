package com.quantatw.sls.pack.device;

import java.util.ArrayList;

import android.os.Parcel;
import android.os.Parcelable;

import com.quantatw.sls.device.AlljoynDevice;
import com.quantatw.sls.device.CloudDevice;
import com.quantatw.sls.pack.base.BaseResPack;

public class GetDevicesResPack extends BaseResPack{

	/**
	 * 
	 */
	private static final long serialVersionUID = 4179568444704599246L;

	private ArrayList<AlljoynDevice> devices;

	public ArrayList<AlljoynDevice> getDevices() {
		return devices;
	}

	public void setDevices(ArrayList<AlljoynDevice> devices) {
		this.devices = devices;
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
	public static final Creator<GetDevicesResPack> CREATOR = new Parcelable.Creator<GetDevicesResPack>() {

		/**
		 * Read the serialized concrete strategy from the parcel.
		 * 
		 * @param in
		 *            The parcel to read from
		 * @return An AbstractStrategy
		 */
		public GetDevicesResPack createFromParcel(Parcel in) {
			// Read serialized concrete strategy from parcel
			return (GetDevicesResPack) in.readSerializable();
		}

		/**
		 * Required by Creator
		 */
		public GetDevicesResPack[] newArray(int size) {
			return new GetDevicesResPack[size];
		}
	};
	
}
