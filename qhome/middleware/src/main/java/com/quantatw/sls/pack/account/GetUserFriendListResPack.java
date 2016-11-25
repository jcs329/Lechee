package com.quantatw.sls.pack.account;

import android.os.Parcel;

import com.quantatw.sls.device.AlljoynDevice;
import com.quantatw.sls.device.FriendData;
import com.quantatw.sls.pack.base.BaseResPack;

import java.util.ArrayList;

public class GetUserFriendListResPack extends BaseResPack{

	/**
	 *
	 */
	private static final long serialVersionUID = 4179568444704599246L;

	private ArrayList<FriendData> list;

	public ArrayList<FriendData> getList() {
		return list;
	}

	public void setList(ArrayList<FriendData> devices) {
		this.list = devices;
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
	public static final Creator<GetUserFriendListResPack> CREATOR = new Creator<GetUserFriendListResPack>() {

		/**
		 * Read the serialized concrete strategy from the parcel.
		 * 
		 * @param in
		 *            The parcel to read from
		 * @return An AbstractStrategy
		 */
		public GetUserFriendListResPack createFromParcel(Parcel in) {
			// Read serialized concrete strategy from parcel
			return (GetUserFriendListResPack) in.readSerializable();
		}

		/**
		 * Required by Creator
		 */
		public GetUserFriendListResPack[] newArray(int size) {
			return new GetUserFriendListResPack[size];
		}
	};
	
}
