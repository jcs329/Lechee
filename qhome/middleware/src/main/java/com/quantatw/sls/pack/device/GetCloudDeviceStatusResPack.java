package com.quantatw.sls.pack.device;

import android.os.Parcel;
import android.os.Parcelable;

import com.quantatw.sls.device.CloudDevice;
import com.quantatw.sls.pack.base.BaseResPack;

public class GetCloudDeviceStatusResPack extends BaseResPack {

	/**
	 * 
	 */
	private static final long serialVersionUID = -849597845185938779L;
	
	private CloudDevice device;

	public CloudDevice getDevice() {
		return device;
	}

	public void setDevice(CloudDevice device) {
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
	public static final Creator<GetCloudDeviceStatusResPack> CREATOR = new Parcelable.Creator<GetCloudDeviceStatusResPack>() {

		/**
		 * Read the serialized concrete strategy from the parcel.
		 * 
		 * @param in
		 *            The parcel to read from
		 * @return An AbstractStrategy
		 */
		public GetCloudDeviceStatusResPack createFromParcel(Parcel in) {
			// Read serialized concrete strategy from parcel
			return (GetCloudDeviceStatusResPack) in.readSerializable();
		}

		/**
		 * Required by Creator
		 */
		public GetCloudDeviceStatusResPack[] newArray(int size) {
			return new GetCloudDeviceStatusResPack[size];
		}
	};
}
