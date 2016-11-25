package com.quantatw.sls.pack.component;

import com.quantatw.sls.pack.base.BaseReqPack;

import android.os.Parcel;
import android.os.Parcelable;


public class ComponentReqPack extends BaseReqPack {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3027265948843129075L;

	private String uuid;

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	private ComponentType type;

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

	public ComponentType getType() {
		return type;
	}

	public void setType(ComponentType type) {
		this.type = type;
	}

	/**
	 * The creator that MUST be defined and named "CREATOR" so that the service
	 * generated from AIDL can recreate AbstractStrategys after IPC.
	 */
	public static final Creator<ComponentReqPack> CREATOR = new Parcelable.Creator<ComponentReqPack>() {

		/**
		 * Read the serialized concrete strategy from the parcel.
		 * 
		 * @param in
		 *            The parcel to read from
		 * @return An AbstractStrategy
		 */
		public ComponentReqPack createFromParcel(Parcel in) {
			// Read serialized concrete strategy from parcel
			return (ComponentReqPack) in.readSerializable();
		}

		/**
		 * Required by Creator
		 */
		public ComponentReqPack[] newArray(int size) {
			return new ComponentReqPack[size];
		}
	};
}
