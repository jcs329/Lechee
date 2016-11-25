package com.quantatw.sls.pack.component;

import java.util.ArrayList;

import com.quantatw.sls.pack.base.BaseReqPack;

import android.os.Parcel;
import android.os.Parcelable;


public class ComponentUpdateReqPack extends BaseReqPack {

	/**
	 * 
	 */
	private static final long serialVersionUID = -875720381150959549L;
	private ArrayList<ComponentBasePack> data;
	private String uuid;

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	private ComponentType type;

	public ComponentType getType() {
		return type;
	}

	public void setType(ComponentType type) {
		this.type = type;
	}

	

	public ArrayList<ComponentBasePack> getData() {
		return data;
	}

	public void setData(ArrayList<ComponentBasePack> data) {
		this.data = data;
	}

	public ComponentUpdateReqPack(ArrayList<ComponentBasePack> data, String uuid, ComponentType type) {
		super();
		this.data = data;
		this.uuid = uuid;
		this.type = type;
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
	public static final Creator<ComponentUpdateReqPack> CREATOR = new Parcelable.Creator<ComponentUpdateReqPack>() {

		/**
		 * Read the serialized concrete strategy from the parcel.
		 * 
		 * @param in
		 *            The parcel to read from
		 * @return An AbstractStrategy
		 */
		public ComponentUpdateReqPack createFromParcel(Parcel in) {
			// Read serialized concrete strategy from parcel
			return (ComponentUpdateReqPack) in.readSerializable();
		}

		/**
		 * Required by Creator
		 */
		public ComponentUpdateReqPack[] newArray(int size) {
			return new ComponentUpdateReqPack[size];
		}
	};
}
