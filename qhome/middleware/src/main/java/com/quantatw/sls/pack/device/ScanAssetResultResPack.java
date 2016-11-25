package com.quantatw.sls.pack.device;

import android.os.Parcel;

import com.quantatw.sls.device.ScanAsset;
import com.quantatw.sls.pack.base.BaseResPack;

import java.util.ArrayList;

public class ScanAssetResultResPack extends BaseResPack{

	private static final long serialVersionUID = -3074685831462725414L;

	private ArrayList<ScanAsset> list;
	private int result; //0: success; -1: error; -2: don't support; -3: timeout
	/*
	 "list": [
       {
         "uuid": "uuid",
         "assetType": 2,
         "deviceName": "Living Room - PM2.5",
         "connectionType": 2
       }
       ,{
         ...
       }
       ...
    ]
	 */
	public int getResult(){
			return result;
	}

	public void setResult(int result){
		this.result = result;
	}

	public ArrayList<ScanAsset> getScanAssetList() {
		return list;
	}

	public void setScanAssetList(ArrayList<ScanAsset> scanAssetList) {
		this.list = scanAssetList;
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
	public static final Creator<ScanAssetResultResPack> CREATOR = new Creator<ScanAssetResultResPack>() {

		/**
		 * Read the serialized concrete strategy from the parcel.
		 * 
		 * @param in
		 *            The parcel to read from
		 * @return An AbstractStrategy
		 */
		public ScanAssetResultResPack createFromParcel(Parcel in) {
			// Read serialized concrete strategy from parcel
			return (ScanAssetResultResPack) in.readSerializable();
		}

		/**
		 * Required by Creator
		 */
		public ScanAssetResultResPack[] newArray(int size) {
			return new ScanAssetResultResPack[size];
		}
	};
	
}
