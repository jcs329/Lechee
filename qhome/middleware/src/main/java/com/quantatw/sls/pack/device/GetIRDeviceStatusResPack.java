package com.quantatw.sls.pack.device;

import android.os.Parcel;

import com.quantatw.sls.device.IRDeviceStatus;
import com.quantatw.sls.pack.base.BaseResPack;

import java.util.ArrayList;

public class GetIRDeviceStatusResPack extends BaseResPack{

	/**
	 *
	 */
	private static final long serialVersionUID = 4179568444704599246L;

	//private ArrayList<IRDeviceStatus> data;
	private IRDeviceStatus data;

	public void setData(IRDeviceStatus data) { this.data = data; }

	public IRDeviceStatus getData() { return data; }
	/*
	public ArrayList<IRDeviceStatus> getAbilities() {
		return data;
	}

	public void setAbilities(ArrayList<IRDeviceStatus> data) {
		this.data = data;
	}
	*/

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
	public static final Creator<GetIRDeviceStatusResPack> CREATOR = new Creator<GetIRDeviceStatusResPack>() {

		/**
		 * Read the serialized concrete strategy from the parcel.
		 * 
		 * @param in
		 *            The parcel to read from
		 * @return An AbstractStrategy
		 */
		public GetIRDeviceStatusResPack createFromParcel(Parcel in) {
			// Read serialized concrete strategy from parcel
			return (GetIRDeviceStatusResPack) in.readSerializable();
		}

		/**
		 * Required by Creator
		 */
		public GetIRDeviceStatusResPack[] newArray(int size) {
			return new GetIRDeviceStatusResPack[size];
		}
	};
	
}
