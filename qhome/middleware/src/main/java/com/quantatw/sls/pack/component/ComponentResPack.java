package com.quantatw.sls.pack.component;

import java.util.ArrayList;

import com.quantatw.sls.pack.base.BaseResPack;

import android.os.Parcel;
import android.os.Parcelable;


public class ComponentResPack extends BaseResPack {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5405899331653141910L;
	private ArrayList<ComponentBasePack> data;
	
	public ArrayList<ComponentBasePack> getData() {
		return data;
	}

	public void setData(ArrayList<ComponentBasePack> data) {
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
	public static final Creator<ComponentResPack> CREATOR = new Parcelable.Creator<ComponentResPack>() {

		/**
		 * Read the serialized concrete strategy from the parcel.
		 * 
		 * @param in
		 *            The parcel to read from
		 * @return An AbstractStrategy
		 */
		public ComponentResPack createFromParcel(Parcel in) {
			// Read serialized concrete strategy from parcel
			return (ComponentResPack) in.readSerializable();
		}

		/**
		 * Required by Creator
		 */
		public ComponentResPack[] newArray(int size) {
			return new ComponentResPack[size];
		}
	};
}
