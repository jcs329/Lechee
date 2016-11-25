package com.quantatw.sls.pack.account;

import java.io.Serializable;

import com.quantatw.sls.pack.base.BaseReqPack;

import android.os.Parcel;
import android.os.Parcelable;


public class FBAccountReqPack extends BaseReqPack implements Serializable,Parcelable{


	/**
	 * 
	 */
	private static final long serialVersionUID = -5343787697970990218L;
	private String user_account;
	private String fb_user_id;
	private String fb_app_id;
	private String fb_AppSecret;
	private String fb_accessToken;
	private String user_name;
	public String getUser_account() {
		return user_account;
	}
	public void setUser_account(String user_account) {
		this.user_account = user_account;
	}
	public String getFb_user_id() {
		return fb_user_id;
	}
	public void setFb_user_id(String fb_user_id) {
		this.fb_user_id = fb_user_id;
	}
	public String getFb_app_id() {
		return fb_app_id;
	}
	public void setFb_app_id(String fb_app_id) {
		this.fb_app_id = fb_app_id;
	}
	public String getFb_AppSecret() {
		return fb_AppSecret;
	}
	public void setFb_AppSecret(String fb_AppSecret) {
		this.fb_AppSecret = fb_AppSecret;
	}
	public String getFb_accessToken() {
		return fb_accessToken;
	}
	public void setFb_accessToken(String fb_accessToken) {
		this.fb_accessToken = fb_accessToken;
	}
	public String getUser_name() {
		return user_name;
	}
	public void setUser_name(String user_name) {
		this.user_name = user_name;
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
	public static final Creator<FBAccountReqPack> CREATOR = new Parcelable.Creator<FBAccountReqPack>() {

		/**
		 * Read the serialized concrete strategy from the parcel.
		 * 
		 * @param in
		 *            The parcel to read from
		 * @return An AbstractStrategy
		 */
		public FBAccountReqPack createFromParcel(Parcel in) {
			// Read serialized concrete strategy from parcel
			return (FBAccountReqPack) in.readSerializable();
		}

		/**
		 * Required by Creator
		 */
		public FBAccountReqPack[] newArray(int size) {
			return new FBAccountReqPack[size];
		}
	};
	
}
