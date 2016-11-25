package com.quantatw.sls.pack.homeAppliance;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class AssetData implements Serializable , Parcelable{
	private static final long serialVersionUID = -48797393615472917L;

	protected int assetType;
	protected int method;
	protected int type;
	protected int result;
	protected String uuid;

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public int getAssetType() {
		return assetType;
	}

	public void setAssetType(int assetType) {
		this.assetType = assetType;
	}

	public int getResult() {
		return result;
	}

	public void setResult(int result) {
		this.result = result;
	}

	public int getMethod() {
		return method;
	}

	public void setMethod(int method) {
		this.method = method;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
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
	public static final Creator<AssetData> CREATOR = new Creator<AssetData>() {

		/**
		 * Read the serialized concrete strategy from the parcel.
		 * 
		 * @param in
		 *            The parcel to read from
		 * @return An AbstractStrategy
		 */
		public AssetData createFromParcel(Parcel in) {
			// Read serialized concrete strategy from parcel
			return (AssetData) in.readSerializable();
		}

		/**
		 * Required by Creator
		 */
		public AssetData[] newArray(int size) {
			return new AssetData[size];
		}
	};
}
