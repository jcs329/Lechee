package com.quantatw.sls.device;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.ArrayList;

public class IRDeviceAbilityData implements Serializable , Parcelable{
	/**
	 *
	 */
	private static final long serialVersionUID = -3864996291206328985L;

	private ArrayList<IRDeviceAbility> abilitys;

	public ArrayList<IRDeviceAbility> getAbilitys() {
		return abilitys;
	}

	public void setAbilitys(ArrayList<IRDeviceAbility> abilities) {
		this.abilitys = abilities;
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
	public static final Creator<IRDeviceAbilityData> CREATOR = new Creator<IRDeviceAbilityData>() {

		/**
		 * Read the serialized concrete strategy from the parcel.
		 * 
		 * @param in
		 *            The parcel to read from
		 * @return An AbstractStrategy
		 */
		public IRDeviceAbilityData createFromParcel(Parcel in) {
			// Read serialized concrete strategy from parcel
			return (IRDeviceAbilityData) in.readSerializable();
		}

		/**
		 * Required by Creator
		 */
		public IRDeviceAbilityData[] newArray(int size) {
			return new IRDeviceAbilityData[size];
		}
	};
}
