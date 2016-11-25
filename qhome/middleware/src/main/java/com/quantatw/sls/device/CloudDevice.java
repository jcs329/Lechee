package com.quantatw.sls.device;

import java.io.Serializable;
import java.util.ArrayList;

import org.alljoyn.bus.annotation.BusInterface;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class CloudDevice implements Serializable , Parcelable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7103896594756024553L;

	public static final String ROLE_ADMINISTRATOR = "Administrator";
	public static final String ROLE_OWNER = "Owner";
	public static final String ROLE_USER = "User";

	protected String id;
	protected String uuid;
	protected String userId;
	protected boolean status;
	protected boolean onlineStatus;
	protected String deviceName;
	protected long updateOnlineStatusTime;
	protected String ip;
	protected int port;
	protected String townId;
	protected int deviceType;
	protected String deviceModel;
	protected String brandName;
	protected String modelName;
	protected String roleName;
	protected float favTemp;
	protected String ownerName;
	protected int shareCnt;
	protected String version;
	protected String type; //add or remove
	protected String targetUser; //add/remove user id
	protected boolean isLocal;
	protected String roomHubUUID;
	protected ArrayList<Asset> deviceAssets;

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}

	public String getUuid() {
		return uuid;
	}
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getUser_id() {
		return userId;
	}
	public void setUser_id(String userId) {
		this.userId = userId;
	}

	public boolean getStatus() {
		return status;
	}
	public void setStatus(boolean status) {
		this.status = status;
	}

	public boolean isOnlineStatus() {
		return onlineStatus;
	}
	public void setOnlineStatus(boolean onlineStatus) {
		this.onlineStatus = onlineStatus;
	}

	public String getDevice_name() {
		return deviceName;
	}
	public void setDevice_name(String deviceName) {
		this.deviceName = deviceName;
	}

	public long getUpdateOnlineStatusTime() {
		return updateOnlineStatusTime;
	}
	public void setUpdateOnlineStatusTime(long updateOnlineStatusTime) {
		this.updateOnlineStatusTime = updateOnlineStatusTime;
	}

	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}

	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}

	public String getTownId() {
		return townId;
	}
	public void setTownId(String townId) {
		this.townId = townId;
	}

	public int getDeviceType() {
		return deviceType;
	}
	public void setDeviceType(int deviceType) {
		this.deviceType = deviceType;
	}

	public String getDeviceModel() {
		return deviceModel;
	}
	public void setDeviceModel(String deviceModel) {
		this.deviceModel = deviceModel;
	}

	public String getRoleName() {
		return roleName;
	}
	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}

	public float getFavTemp() {
		return favTemp;
	}
	public void setFavTemp(float favTemp) {
		this.favTemp = favTemp;
	}

	public boolean isLocal() {
		return isLocal;
	}

	public void setLocal(boolean isLocal) {
		this.isLocal = isLocal;
	}


	public String getOwnerName() {
		return ownerName;
	}

	public void setOwnerName(String ownerName) {
		this.ownerName = ownerName;
	}

	public int getShareCnt() {
		return shareCnt;
	}

	public void setShareCnt(int shareCnt) {
		this.shareCnt = shareCnt;
	}


	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getTagetUser() {
		return targetUser;
	}

	public void setTagetUser(String targetUser) {
		this.targetUser = targetUser;
	}

	public String getBrandName() {
		return brandName;
	}

	public void setBrandName(String brandName) {
		this.brandName = brandName;
	}

	public String getModelName() {
		return modelName;
	}

	public void setModelName(String modelName) {
		this.modelName = modelName;
	}

	public String getRoomHubUUID() {
		return roomHubUUID;
	}

	public void setRoomHubUUID(String roomHubUUID) {
		this.roomHubUUID = roomHubUUID;
	}

	public ArrayList<Asset> getDeviceAssets() {
		return deviceAssets;
	}

	public void setDeviceAssets(ArrayList<Asset> deviceAssets) {
		this.deviceAssets = deviceAssets;
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
	public static final Creator<CloudDevice> CREATOR = new Parcelable.Creator<CloudDevice>() {

		/**
		 * Read the serialized concrete strategy from the parcel.
		 * 
		 * @param in
		 *            The parcel to read from
		 * @return An AbstractStrategy
		 */
		public CloudDevice createFromParcel(Parcel in) {
			// Read serialized concrete strategy from parcel
			return (CloudDevice) in.readSerializable();
		}

		/**
		 * Required by Creator
		 */
		public CloudDevice[] newArray(int size) {
			return new CloudDevice[size];
		}
	};



	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((uuid == null) ? 0 : uuid.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CloudDevice other = (CloudDevice) obj;
		if (uuid == null) {
			if (other.uuid != null)
				return false;
		} else if (!uuid.equals(other.uuid))
			return false;
		return true;
	}
	
	
}
