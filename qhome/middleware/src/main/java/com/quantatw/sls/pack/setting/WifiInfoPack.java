package com.quantatw.sls.pack.setting;


import java.util.ArrayList;

import com.quantatw.sls.pack.base.BaseResPack;
import com.quantatw.sls.wificonfig.SSIDInfo;

import android.os.Parcel;
import android.os.Parcelable;

public class WifiInfoPack extends BaseResPack {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5342640829921878508L;
	private ArrayList<SSIDInfo> wifiList;
	

	public ArrayList<SSIDInfo> getWifiList() {
		return wifiList;
	}

	public void setWifiList(ArrayList<SSIDInfo> wifiList) {
		this.wifiList = wifiList;
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
	public static final Creator<WifiInfoPack> CREATOR = new Parcelable.Creator<WifiInfoPack>() {

		/**
		 * Read the serialized concrete strategy from the parcel.
		 * 
		 * @param in
		 *            The parcel to read from
		 * @return An AbstractStrategy
		 */
		public WifiInfoPack createFromParcel(Parcel in) {
			// Read serialized concrete strategy from parcel
			return (WifiInfoPack) in.readSerializable();
		}

		/**
		 * Required by Creator
		 */
		public WifiInfoPack[] newArray(int size) {
			return new WifiInfoPack[size];
		}
	};


}
