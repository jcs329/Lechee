package com.quantatw.sls.pack.device;

import android.os.Parcel;

import com.quantatw.sls.pack.base.BaseReqPack;

public class ScanAssetReqPack extends BaseReqPack {
	private static final long serialVersionUID = 1768558761189221213L;

	private int assetType;	// asset type
	private int connectionType;	// 1: BT, 2: WiFi, 10000: All Type
	private String prefixName;	//"EQL_PM2.5_",
	private int expireTime;	// expire time

 	public void setAssetType(int assetType) { this.assetType = assetType; }

	public int getAssetType() { return this.assetType; }

	public void setConnectionType(int connectionType) { this.connectionType = connectionType; }

	public int getConnectionType() { return this.connectionType; }

	public void setPrefixName(String prefixName) { this.prefixName = prefixName; }

	public String getPrefixName() { return this.prefixName; }

	public void setExpireTime(int expireTime) { this.expireTime = expireTime; }

	public int getExpireTime() { return this.expireTime; }
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
	public static final Creator<ScanAssetReqPack> CREATOR = new Creator<ScanAssetReqPack>() {

		/**
		 * Read the serialized concrete strategy from the parcel.
		 * 
		 * @param in
		 *            The parcel to read from
		 * @return An AbstractStrategy
		 */
		public ScanAssetReqPack createFromParcel(Parcel in) {
			// Read serialized concrete strategy from parcel
			return (ScanAssetReqPack) in.readSerializable();
		}

		/**
		 * Required by Creator
		 */
		public ScanAssetReqPack[] newArray(int size) {
			return new ScanAssetReqPack[size];
		}
	};
}
