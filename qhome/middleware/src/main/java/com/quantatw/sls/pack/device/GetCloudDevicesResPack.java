package com.quantatw.sls.pack.device;

import java.util.ArrayList;

import android.os.Parcel;
import android.os.Parcelable;

import com.quantatw.sls.device.AlljoynDevice;
import com.quantatw.sls.device.CloudDevice;
import com.quantatw.sls.pack.base.BaseResPack;

public class GetCloudDevicesResPack extends BaseResPack{

	/**
	 * 
	 */
	private static final long serialVersionUID = 4179568444704599246L;

	private ArrayList<CloudDevice> devices;
	private String mqttTopic;

	public ArrayList<CloudDevice> getDevices() {
		return devices;
	}

	public void setDevices(ArrayList<CloudDevice> devices) {
		this.devices = devices;
	}

	public String getMqttTopic() {
		return mqttTopic;
	}

	public void setMqttTopic(String mqttTopic) {
		this.mqttTopic = mqttTopic;
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
	public static final Creator<GetCloudDevicesResPack> CREATOR = new Parcelable.Creator<GetCloudDevicesResPack>() {

		/**
		 * Read the serialized concrete strategy from the parcel.
		 * 
		 * @param in
		 *            The parcel to read from
		 * @return An AbstractStrategy
		 */
		public GetCloudDevicesResPack createFromParcel(Parcel in) {
			// Read serialized concrete strategy from parcel
			return (GetCloudDevicesResPack) in.readSerializable();
		}

		/**
		 * Required by Creator
		 */
		public GetCloudDevicesResPack[] newArray(int size) {
			return new GetCloudDevicesResPack[size];
		}
	};
	
}
