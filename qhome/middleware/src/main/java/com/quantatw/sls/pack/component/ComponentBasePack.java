package com.quantatw.sls.pack.component;

import java.io.Serializable;

import android.os.Parcel;
import android.os.Parcelable;

public class ComponentBasePack implements Serializable, Parcelable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4974585798007959771L;
	private String name;
	private int state;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
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
	public static final Creator<ComponentBasePack> CREATOR = new Parcelable.Creator<ComponentBasePack>() {

		/**
		 * Read the serialized concrete strategy from the parcel.
		 * 
		 * @param in
		 *            The parcel to read from
		 * @return An AbstractStrategy
		 */
		public ComponentBasePack createFromParcel(Parcel in) {
			// Read serialized concrete strategy from parcel
			return (ComponentBasePack) in.readSerializable();
		}

		/**
		 * Required by Creator
		 */
		public ComponentBasePack[] newArray(int size) {
			return new ComponentBasePack[size];
		}
	};
}
