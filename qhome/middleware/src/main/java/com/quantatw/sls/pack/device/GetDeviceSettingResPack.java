package com.quantatw.sls.pack.device;

import android.os.Parcel;

import com.quantatw.sls.device.DeviceSetting;
import com.quantatw.sls.device.IRDeviceStatus;
import com.quantatw.sls.pack.base.BaseResPack;

public class GetDeviceSettingResPack extends BaseResPack{

	/**
	 *
	 */
	private static final long serialVersionUID = 4179568444704599246L;

	protected int deviceType;
	protected String deviceSetting;

	public void setDeviceType(int deviceType) { this.deviceType = deviceType; }

	public int getDeviceType() { return this.deviceType; }

	public void setDeviceSetting(String deviceSetting) { this.deviceSetting = deviceSetting; }

	public String getDeviceSetting() { return this.deviceSetting; }

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
	public static final Creator<GetDeviceSettingResPack> CREATOR = new Creator<GetDeviceSettingResPack>() {

		/**
		 * Read the serialized concrete strategy from the parcel.
		 * 
		 * @param in
		 *            The parcel to read from
		 * @return An AbstractStrategy
		 */
		public GetDeviceSettingResPack createFromParcel(Parcel in) {
			// Read serialized concrete strategy from parcel
			return (GetDeviceSettingResPack) in.readSerializable();
		}

		/**
		 * Required by Creator
		 */
		public GetDeviceSettingResPack[] newArray(int size) {
			return new GetDeviceSettingResPack[size];
		}
	};
	
}
