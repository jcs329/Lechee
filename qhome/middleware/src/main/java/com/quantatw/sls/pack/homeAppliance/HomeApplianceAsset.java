package com.quantatw.sls.pack.homeAppliance;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.ArrayList;

public class HomeApplianceAsset implements Serializable , Parcelable{
	private static final long serialVersionUID = -9197623481200571575L;

	protected int assetType;
	protected ArrayList<String> uuid;

	public int getAssetType() {
		return assetType;
	}

	public void setAssetType(int assetType) {
		this.assetType = assetType;
	}

	public ArrayList<String> getUuid() {
		return uuid;
	}

	public void setUuid(ArrayList<String> uuid) {
		this.uuid = uuid;
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
	public static final Creator<HomeApplianceAsset> CREATOR = new Creator<HomeApplianceAsset>() {

		/**
		 * Read the serialized concrete strategy from the parcel.
		 * 
		 * @param in
		 *            The parcel to read from
		 * @return An AbstractStrategy
		 */
		public HomeApplianceAsset createFromParcel(Parcel in) {
			// Read serialized concrete strategy from parcel
			return (HomeApplianceAsset) in.readSerializable();
		}

		/**
		 * Required by Creator
		 */
		public HomeApplianceAsset[] newArray(int size) {
			return new HomeApplianceAsset[size];
		}
	};
}
