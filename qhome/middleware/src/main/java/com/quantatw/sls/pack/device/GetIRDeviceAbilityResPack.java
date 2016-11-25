package com.quantatw.sls.pack.device;

import android.os.Parcel;

import com.quantatw.sls.device.AlljoynDevice;
import com.quantatw.sls.device.IRDeviceAbility;
import com.quantatw.sls.device.IRDeviceAbilityData;
import com.quantatw.sls.pack.base.BaseResPack;

import java.util.ArrayList;

public class GetIRDeviceAbilityResPack extends BaseResPack{

	/**
	 *
	 */
	private static final long serialVersionUID = 4179568444704599246L;

	private IRDeviceAbilityData data;
	/*
	private ArrayList<IRDeviceAbility> data;

	public ArrayList<IRDeviceAbility> getData() {
		return data;
	}

	public void setData(ArrayList<IRDeviceAbility> data) {
		this.data = data;
	}
	*/

	public IRDeviceAbilityData getData() {
		return data;
	}

	public void setData(IRDeviceAbilityData data) {
		this.data = data;
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
	public static final Creator<GetIRDeviceAbilityResPack> CREATOR = new Creator<GetIRDeviceAbilityResPack>() {

		/**
		 * Read the serialized concrete strategy from the parcel.
		 * 
		 * @param in
		 *            The parcel to read from
		 * @return An AbstractStrategy
		 */
		public GetIRDeviceAbilityResPack createFromParcel(Parcel in) {
			// Read serialized concrete strategy from parcel
			return (GetIRDeviceAbilityResPack) in.readSerializable();
		}

		/**
		 * Required by Creator
		 */
		public GetIRDeviceAbilityResPack[] newArray(int size) {
			return new GetIRDeviceAbilityResPack[size];
		}
	};
	
}
