package com.quantatw.roomhub.manager.asset.manager;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;

import com.quantatw.roomhub.utils.AssetDef;
import com.quantatw.roomhub.utils.RoomHubDef;
import com.quantatw.sls.api.DeviceTypeConvertApi;
import com.quantatw.sls.device.RoomHubDevice;
import com.quantatw.sls.key.SourceType;
import com.quantatw.sls.object.AlljoynAboutData;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

/**
 * Created by 95011613 on 2015/9/23.
 */
public class RoomHubData implements Serializable,Parcelable,Comparable<RoomHubData>{
    private static final String TAG = RoomHubData.class.getSimpleName();

    private static boolean DEBUG=true;

    public static final int SOURCE_TYPE_ALLJOYN = 1 << 0;
    public static final int SOURCE_TYPE_CLOUD = 1 << 1;

    public enum ACTION
    {
        ADD_DEVICE, REMOVE_DEVICE, REMOVE_DEVICE_ALL
    }

    private RoomHubDevice mDevice;
    private String mUuid;
    private String mName;
    private int mSourceType;
    private double mSensorTemp;
    private double mSensorHumidity;
    private String mOwnerId; //for alljoyn
    private String mOwnerName; //for cloud
    private String mRoleName;
    private String mVersion;

    private boolean mIsUpgrade=false;
    private boolean mIsOnLine=false;

    private ArrayList<AssetInfoData> mAssetList=new ArrayList<AssetInfoData>();

    protected RoomHubData(RoomHubDevice device){
        if(device != null) {
            mDevice=device;

            mUuid=mDevice.getuuid();
            mSensorTemp=mDevice.gettemperature();
            mSensorHumidity=mDevice.gethumidity();

            if(device.getSourceType() == SourceType.ALLJOYN) {
                this.mSourceType = SOURCE_TYPE_ALLJOYN;
            }else if (device.getSourceType() == SourceType.CLOUD){
                if(device.getExtraInfo()!=null) {
                    this.mSourceType = SOURCE_TYPE_CLOUD;
                }
            }
            UpdateRoomHubName(device);
            UpdateOnLineStatus(device);
            mVersion=getFirmwareVersion(device);
        }
    }

    protected void UpdateRoomHubDevice(RoomHubDevice device){
        RoomHubDevice new_device=device;
        SourceType new_source_type=new_device.getSourceType();

        if(new_source_type == SourceType.ALLJOYN){
            if(IsCloud())
                new_device.setExtraInfo(mDevice.getExtraInfo());

            mDevice=new_device;

            if(!IsAlljoyn())
                setSourceType(RoomHubData.SOURCE_TYPE_ALLJOYN);

        }else if(new_source_type == SourceType.CLOUD){
            mDevice.setExtraInfo(new_device.getExtraInfo());

            if(!IsCloud())
                setSourceType(RoomHubData.SOURCE_TYPE_CLOUD);
        }
    }

    protected void UpdateRoomHubData(RoomHubDevice device){
        UpdateRoomHubDevice(device);
        mSensorTemp=mDevice.gettemperature();
        mSensorHumidity=mDevice.gethumidity();
        Log.d(TAG,"UpdateRoomHubData mSensorTemp="+mSensorTemp+" mSensorHumidity="+mSensorHumidity);
        UpdateRoomHubName(device);
        UpdateOnLineStatus(device);
        mVersion=getFirmwareVersion(mDevice);
    }

    protected void RemoveRoomHubData(RoomHubDevice device){
        RoomHubDevice new_device=device;
        SourceType new_source_type=new_device.getSourceType();

        if(new_source_type == SourceType.ALLJOYN){
            if(IsCloud()){
                new_device.setExtraInfo(mDevice.getExtraInfo());
                mDevice=new_device;
            }
            mSourceType &=~RoomHubData.SOURCE_TYPE_ALLJOYN;
        }else if(new_source_type == SourceType.CLOUD){
            mDevice.setExtraInfo(null);
            mSourceType &=~RoomHubData.SOURCE_TYPE_CLOUD;
        }
    }

    public static final Creator<RoomHubData> CREATOR = new Creator<RoomHubData>() {
        @Override
        public RoomHubData createFromParcel(Parcel in) {
            return (RoomHubData)in.readSerializable();
        }

        @Override
        public RoomHubData[] newArray(int size) {
            return new RoomHubData[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeSerializable(this);
    }


    public RoomHubDevice getRoomHubDevice() {
        return mDevice;
    }

    protected void setRoomHubDevice(RoomHubDevice device) {
        this.mDevice = device;
    }

    public String getUuid() {
        return mUuid;
    }

    private String getCurrentName(RoomHubDevice device) {
        String name="";

        if(IsAlljoyn()) {
            name = device.getName();
        }else if(IsCloud()){
            if(mDevice.getExtraInfo() != null)
                name=device.getExtraInfo().getDevice_name();
        }

        return name;
    }

    protected void setName(String name) {
        mName=name;
    }

    public String getName() {
        return mName;
    }

    protected void setSourceType(int type){//SourceType type){
        mSourceType |=type;
    }

    public int getSourceType(){
        return mSourceType;
    }

    public double getSensorTemp() {
        return mSensorTemp;
    }

    protected void setSensorTemp(double temp) {
        this.mSensorTemp = temp;
    }

    public double getSensorHumidity() {
        return mSensorHumidity;
    }

    protected void setSensorHumidity(double humidity) {
        this.mSensorHumidity = humidity;
    }

    public boolean IsAlljoyn(){
        if((mSourceType & RoomHubData.SOURCE_TYPE_ALLJOYN) != 0){
            return true;
        }
        return false;
    }

    public boolean IsCloud(){
        if((mSourceType & RoomHubData.SOURCE_TYPE_CLOUD) != 0){
            return true;
        }
        return false;
    }

    public boolean IsOnLine(){
        return mIsOnLine;
    }

    public String getOwnerId() {
        String owner_id="";
        if(IsAlljoyn())
            owner_id=mDevice.getOwnerId();
        else if(IsCloud()){
            if(mDevice.getExtraInfo() != null)
                owner_id=mDevice.getExtraInfo().getUser_id();
        }

        return owner_id;
    }

    protected void setOwnerId(String owner_id) {
        mOwnerId=owner_id;
    }

    public String getOwnerName(){
        return mOwnerName;
    }

    protected void setOwnerName(String owner_name){
        this.mOwnerName=owner_name;
    }

    public String getVersion(){
        Log.d(TAG,"getVersion uuid="+mUuid+" version="+mVersion);
        return mVersion;
    }

    private String getFirmwareVersion(RoomHubDevice device){
        String version = null;

        if (IsAlljoyn()) {
            AlljoynAboutData about_data=device.getAboutData();
            if(about_data != null)
                version=about_data.getSoftwareVersion();

            log("getVersion ALLJOYN version="+version);
        }else if (IsCloud() && (device.getExtraInfo() != null)) {
            version=device.getExtraInfo().getVersion();
            log("getVersion CLOUD version=" + version);
        }
        return version;
    }

    public boolean IsOwner(){
        if(mRoleName.equals(RoomHubDef.ROLE_OWNER) || mRoleName.equals(RoomHubDef.ROLE_ADMIN))
            return true;

        return false;
    }

    public String getRoleName(){
        return mRoleName;
    }

    protected void setRoleName(String role_name){
        this.mRoleName=role_name;
    }

    public boolean IsFriend(){
        if(mRoleName.equals(RoomHubDef.ROLE_USER))
            return true;

        return false;
    }

    protected void setUpgrade(boolean is_upgrade){
        this.mIsUpgrade=is_upgrade;
    }

    public boolean IsUpgrade(){
        return mIsUpgrade;
    }

    public ArrayList<AssetInfoData> getAssetList(){
        Collections.sort(mAssetList);
        return mAssetList;
    }

    public ArrayList<AssetInfoData> getAssetListNoSameType() {
        ArrayList<AssetInfoData> asset_list = new ArrayList<AssetInfoData>();
        synchronized (mAssetList){
            for (AssetInfoData asset_data : mAssetList ) {
                boolean found = false;
                for (AssetInfoData asset_data1 : asset_list ) {
                    if (asset_data.getAssetType() == asset_data1.getAssetType()) {
                        found = true;
                    }
                }
                if (!found) {
                    asset_list.add(asset_data);
                }
            }
        }
        return asset_list;
    }

    protected AssetInfoData AssetIsExist(String uuid){
        if((mAssetList != null) && (mAssetList.size() > 0)){
            synchronized (mAssetList){
                for (Iterator<AssetInfoData> it = mAssetList.iterator(); it.hasNext(); ) {
                    AssetInfoData asset_data=it.next();
                    if(((asset_data.getAssetUuid().equals(uuid)) || (uuid.equals(mUuid))))
                        return asset_data;
                }
            }
        }

        return null;
    }

    public AssetInfoData AssetIsExist(int asset_type){
        if((mAssetList != null) && (mAssetList.size() > 0)){
            synchronized (mAssetList){
                for (Iterator<AssetInfoData> it = mAssetList.iterator(); it.hasNext(); ) {
                    AssetInfoData asset_data=it.next();
                    if(asset_data.getAssetType() == asset_type)
                        return asset_data;
                }
            }
        }
        return null;
    }

    public int getAssetCount(int asset_type){
        if((mAssetList != null) && (mAssetList.size() > 0)){
            int count = 0;
            synchronized (mAssetList){
                for (Iterator<AssetInfoData> it = mAssetList.iterator(); it.hasNext(); ) {
                    AssetInfoData asset_data=it.next();
                    if(asset_data.getAssetType() == asset_type)
                        count++;
                }
                return count;
            }
        }
        return 0;
    }

    protected boolean checkVersion(String versionNow,String versionCompare) {
        if(TextUtils.isEmpty(versionNow) || TextUtils.isEmpty(versionCompare))
            return false;

        if(versionNow.equals(versionCompare))
            return true;

        String[] versionArray  = versionNow.split("\\.");
        String[] compareArray = versionCompare.split("\\.");

        if (Integer.parseInt(versionArray[0]) > Integer.parseInt(compareArray[0])) {
            return true;
        }

        Float float_ver = Float.parseFloat("0." + versionArray[1]);
        Float float_compare_ver = Float.parseFloat("0." + compareArray[1]);

        if (float_ver > float_compare_ver) {
            return true;
        }

        if (Float.parseFloat("0." + versionArray[2]) > Float.parseFloat("0." + compareArray[2])) {
            return true;
        }

        return false;
    }

    private boolean getOnLineStatus(RoomHubDevice device){
        log("getOnLineStatus mSourceType=" + mSourceType);
        if (IsAlljoyn()) {
            //always is on line if alljoyn is exist
            return true;
        }else if (IsCloud() && (device.getExtraInfo() != null)) {
            //get cloud isOnlineStatus if the alljoyn doesn't exist
            return device.getExtraInfo().isOnlineStatus();
        }
        return false;
    }

    protected boolean UpdateOnLineStatus(RoomHubDevice device){
        boolean new_online=getOnLineStatus(device);
        log("UpdateOnLineStatus uuid=" + mUuid + " mIsOnLine=" + mIsOnLine + " new_online=" + new_online);
        if(mIsOnLine != new_online){
            mIsOnLine=new_online;
            UpdateRoomHubDevice(device);
            return true;
        }

        return false;
    }

    protected boolean UpdateRoomHubName(RoomHubDevice device){
        String new_name=getCurrentName(device);
        if(new_name == null)
            return false;

        if((mName == null) || !mName.equals(new_name)){
            mName=new_name;
            UpdateRoomHubDevice(device);
            return true;
        }

        return false;
    }

    protected void UpdateFirmwareVersion(RoomHubDevice device){
        mVersion=getFirmwareVersion(device);
        Log.d(TAG,"UpdateFirmwareVersion uuid="+mUuid+" version="+mVersion);
    }

    @Override
    public int compareTo(RoomHubData another) {
        return mName.compareToIgnoreCase(another.getName());
    }

    private void log(String msg) {
        if(DEBUG)
            Log.d(TAG,msg);
    }
    public boolean isAnyBulbOnline(){
        for (AssetInfoData data:getAssetList()
             ) {
            if(data.getAssetType() == DeviceTypeConvertApi.TYPE_ROOMHUB.BULB){
                if (data.getOnlineStatus() == AssetDef.ONLINE_STATUS_ONLINE){
                    return true;
                }
            }
        }
        return false;
    }
}
