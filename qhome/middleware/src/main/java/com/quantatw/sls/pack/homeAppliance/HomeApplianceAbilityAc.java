package com.quantatw.sls.pack.homeAppliance;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.ArrayList;

public class HomeApplianceAbilityAc implements Serializable , Parcelable{
	private static final long serialVersionUID = -6496365652351642110L;

	protected int mode;
	protected int maxValue;
	protected int minValue;
	protected int swing;
	protected int fan;

	public int getMode() {
		return mode;
	}

	public void setMode(int mode) {
		this.mode = mode;
	}

	public int getMaxValue() {
		return maxValue;
	}

	public void setMaxValue(int maxValue) {
		this.maxValue = maxValue;
	}

	public int getMinValue() {
		return minValue;
	}

	public void setMinValue(int minValue) {
		this.minValue = minValue;
	}

	public int getSwing() {
		return swing;
	}

	public void setSwing(int swing) {
		this.swing = swing;
	}

	public int getFan() {
		return fan;
	}

	public void setFan(int fan) {
		this.fan = fan;
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
	public static final Creator<HomeApplianceAbilityAc> CREATOR = new Creator<HomeApplianceAbilityAc>() {

		/**
		 * Read the serialized concrete strategy from the parcel.
		 * 
		 * @param in
		 *            The parcel to read from
		 * @return An AbstractStrategy
		 */
		public HomeApplianceAbilityAc createFromParcel(Parcel in) {
			// Read serialized concrete strategy from parcel
			return (HomeApplianceAbilityAc) in.readSerializable();
		}

		/**
		 * Required by Creator
		 */
		public HomeApplianceAbilityAc[] newArray(int size) {
			return new HomeApplianceAbilityAc[size];
		}
	};
}
