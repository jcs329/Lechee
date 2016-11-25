package com.quantatw.sls.pack.device;

import android.os.Parcel;
import android.os.Parcelable;

import com.quantatw.sls.device.CloudDevice;
import com.quantatw.sls.pack.base.BaseResPack;

public class UpdateDeviceOnlineStatusResPack extends BaseResPack {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8412444029569940590L;
	private CloudDevice device;
	private String mqttTopic;

	public CloudDevice getDevice() {
		return device;
	}

	public void setDevice(CloudDevice device) {
		this.device = device;
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
	public static final Creator<UpdateDeviceOnlineStatusResPack> CREATOR = new Parcelable.Creator<UpdateDeviceOnlineStatusResPack>() {

		/**
		 * Read the serialized concrete strategy from the parcel.
		 * 
		 * @param in
		 *            The parcel to read from
		 * @return An AbstractStrategy
		 */
		public UpdateDeviceOnlineStatusResPack createFromParcel(Parcel in) {
			// Read serialized concrete strategy from parcel
			return (UpdateDeviceOnlineStatusResPack) in.readSerializable();
		}

		/**
		 * Required by Creator
		 */
		public UpdateDeviceOnlineStatusResPack[] newArray(int size) {
			return new UpdateDeviceOnlineStatusResPack[size];
		}
	};
	
}
