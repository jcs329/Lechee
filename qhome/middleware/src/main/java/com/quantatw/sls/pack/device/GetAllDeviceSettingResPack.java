package com.quantatw.sls.pack.device;

import android.os.Parcel;

import com.quantatw.sls.device.Schedule;
import com.quantatw.sls.pack.base.BaseResPack;

import java.util.ArrayList;

public class GetAllDeviceSettingResPack extends BaseResPack{

	/**
	 *
	 */
	private static final long serialVersionUID = 4179568444704599246L;

	protected int deviceType;
	protected ArrayList<Schedule> scheduleList;

	public void setDeviceType(int deviceType) { this.deviceType = deviceType; }

	public int getDeviceType() { return this.deviceType; }

	public void setScheduleList(ArrayList<Schedule> list) { this.scheduleList = list; }

	public ArrayList<Schedule> getScheduleList() { return this.scheduleList; }

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
	public static final Creator<GetAllDeviceSettingResPack> CREATOR = new Creator<GetAllDeviceSettingResPack>() {

		/**
		 * Read the serialized concrete strategy from the parcel.
		 * 
		 * @param in
		 *            The parcel to read from
		 * @return An AbstractStrategy
		 */
		public GetAllDeviceSettingResPack createFromParcel(Parcel in) {
			// Read serialized concrete strategy from parcel
			return (GetAllDeviceSettingResPack) in.readSerializable();
		}

		/**
		 * Required by Creator
		 */
		public GetAllDeviceSettingResPack[] newArray(int size) {
			return new GetAllDeviceSettingResPack[size];
		}
	};
	
}
