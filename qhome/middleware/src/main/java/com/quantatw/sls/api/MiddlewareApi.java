package com.quantatw.sls.api;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.HandlerThread;
import android.provider.Settings.Secure;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.quantatw.sls.alljoyn.RoomHubAllJoynDef;
import com.quantatw.sls.cloudapi.CloudApi;
import com.quantatw.sls.device.CloudDevice;
import com.quantatw.sls.device.CloudDeviceManagement;
import com.quantatw.sls.device.DeviceSignal;
import com.quantatw.sls.device.IRDeviceStatus;
import com.quantatw.sls.device.IRFANProperty;
import com.quantatw.sls.device.IRFANStatus;
import com.quantatw.sls.device.RoomHubDevice;
import com.quantatw.sls.device.Schedule;
import com.quantatw.sls.handler.RoomHubBusHandler;
import com.quantatw.sls.key.ErrorKey;
import com.quantatw.sls.key.ReasonType;
import com.quantatw.sls.key.SourceType;
import com.quantatw.sls.listener.CloudDeviceListener;
import com.quantatw.sls.listener.DeviceDefaultUserListener;
import com.quantatw.sls.listener.HomeApplianceSignalListener;
import com.quantatw.sls.listener.OTACloudStateUpdateListener;
import com.quantatw.sls.listener.RoomHubDeviceListener;
import com.quantatw.sls.listener.RoomHubSignalListener;
import com.quantatw.sls.listener.ShareUserListener;
import com.quantatw.sls.listener.UserFriendListener;
import com.quantatw.sls.pack.Weather.CityListResPack;
import com.quantatw.sls.pack.Weather.TownListResPack;
import com.quantatw.sls.pack.Weather.WeatherDataResPack;
import com.quantatw.sls.pack.account.AccountInfoCheckReqPack;
import com.quantatw.sls.pack.account.AccountLoginReqPack;
import com.quantatw.sls.pack.account.AccountNotificationTokenReqPack;
import com.quantatw.sls.pack.account.AccountRegisterReqPack;
import com.quantatw.sls.pack.account.AccountResPack;
import com.quantatw.sls.pack.account.AddUserFriendReqPack;
import com.quantatw.sls.pack.account.ChangePasswordReqPack;
import com.quantatw.sls.pack.account.FBAccountReqPack;
import com.quantatw.sls.pack.account.ForgetPasswordReqPack;
import com.quantatw.sls.pack.account.GetUserFriendListResPack;
import com.quantatw.sls.pack.account.JwtPayloadResPack;
import com.quantatw.sls.pack.account.ModifyNickNameReqPack;
import com.quantatw.sls.pack.account.RemoveUserFriendReqPack;
import com.quantatw.sls.pack.account.UserSharedDataResPack;
import com.quantatw.sls.pack.account.SendAuthorizeEmailReqPack;
import com.quantatw.sls.pack.account.UpdateUserProfileReqPack;
import com.quantatw.sls.pack.account.UserFriendResPack;
import com.quantatw.sls.pack.base.BaseResPack;
import com.quantatw.sls.pack.device.AddCloudDeviceResPack;
import com.quantatw.sls.pack.device.AddDeviceReqPack;
import com.quantatw.sls.pack.device.AddDeviceUserReqPack;
import com.quantatw.sls.pack.device.DeviceDefaultUserResPack;
import com.quantatw.sls.pack.device.DeleteDeviceReqPack;
import com.quantatw.sls.pack.device.DeviceUserReqPack;
import com.quantatw.sls.pack.device.GetCloudDeviceStatusResPack;
import com.quantatw.sls.pack.device.GetCloudDevicesResPack;
import com.quantatw.sls.pack.device.GetDeviceSettingResPack;
import com.quantatw.sls.pack.device.GetDeviceStatusReqPack;
import com.quantatw.sls.pack.device.GetDevicesReqPack;
import com.quantatw.sls.pack.device.GetScheduleListResPack;
import com.quantatw.sls.pack.device.ModifyDeviceNameReqPack;
import com.quantatw.sls.pack.device.ScanAssetResultResPack;
import com.quantatw.sls.pack.homeAppliance.AcFailRecoverResPack;
import com.quantatw.sls.pack.homeAppliance.AssetProfile;
import com.quantatw.sls.pack.homeAppliance.FirmwareUpdateStateResPack;
import com.quantatw.sls.pack.homeAppliance.SignalDeleteSchedulePack;
import com.quantatw.sls.pack.homeAppliance.SignalUpdateSchedulePack;
import com.quantatw.sls.pack.homeAppliance.detail.AcAssetDetailInfoResPack;
import com.quantatw.sls.pack.homeAppliance.detail.AirPurifierAssetDetailInfoResPack;
import com.quantatw.sls.pack.homeAppliance.detail.AssetResPack;
import com.quantatw.sls.pack.homeAppliance.detail.BulbAssetDetailInfoResPack;
import com.quantatw.sls.pack.homeAppliance.detail.FanAssetDetailInfoResPack;
import com.quantatw.sls.pack.homeAppliance.detail.PMAssetDetailInfoResPack;
import com.quantatw.sls.pack.homeAppliance.detail.TVAssetDetailInfoResPack;
import com.quantatw.sls.pack.mqtt.AssetRecvPack;
import com.quantatw.sls.pack.mqtt.MessageRecvPack;
import com.quantatw.sls.pack.roomhub.AcOnOffStatusResPack;
import com.quantatw.sls.pack.roomhub.DeleteScheduleResPack;
import com.quantatw.sls.pack.roomhub.DeviceFirmwareUpdateStateResPack;
import com.quantatw.sls.pack.roomhub.DeviceInfoChangeResPack;
import com.quantatw.sls.pack.roomhub.LearningResultResPack;
import com.quantatw.sls.pack.roomhub.NameChangeResPack;
import com.quantatw.sls.pack.roomhub.NextScheduleResPack;
import com.quantatw.sls.pack.roomhub.RoomHubDataResPack;
import com.quantatw.sls.pack.roomhub.UpdateScheduleResPack;
import com.quantatw.sls.pack.roomhub.VersionCheckUpdateReqPack;
import com.quantatw.sls.pack.roomhub.VersionCheckUpdateResPack;
import com.quantatw.sls.pack.roomhub.ir.IRACAutoScanResPack;
import com.quantatw.sls.pack.roomhub.ir.IRACKeyDataResPack;
import com.quantatw.sls.pack.roomhub.ir.IRBrandAndModelDataResPack;
import com.quantatw.sls.pack.roomhub.ir.IRBrandListResPack;
import com.quantatw.sls.pack.roomhub.ir.IRCodeNumByKeywordResPack;
import com.quantatw.sls.pack.roomhub.ir.IRCodeNumListResPack;
import com.quantatw.sls.pack.roomhub.ir.IRModeResPack;
import com.quantatw.sls.pack.roomhub.ir.IRModelListResPack;
import com.quantatw.sls.pack.roomhub.ir.IRModelResPack;
import com.quantatw.sls.pack.roomhub.sensor.SensorDataResPack;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

public class MiddlewareApi implements RoomHubDeviceListener, RoomHubSignalListener, HomeApplianceSignalListener {
	private String TAG = "MiddlewareApi";
	static MiddlewareApi instance = null;

	public static final String CONNECT_ROOMHUB_ALLJOYN_ACTION = "com.quantatw.roomhub.CONNECT_ALLJOYN";
	public static final String DISCONNECT_ROOMHUB_ALLJOYN_ACTION = "com.quantatw.roomhub.DISCONNECT_ALLJOYN";
	public static final String ONBOARDING_START_ACTION = "com.quantatw.roomhub.ONBOARDING_START";
	public static final String ONBOARDING_STOP_ACTION = "com.quantatw.roomhub.ONBOARDING_STOP";

	public static final String CLOUD_DONE_ACTION = "com.quantatw.roomhub.CLOUD_DONE";

	private Context mContext;
	private RoomHubBusHandler roomHubBusHandler;

	private AccountResPack mResPack;
	
	private ApiConfig mApiConfig;
	//remote control 
//	private String mqttServer = "tcp://60.250.232.249:1883";
	private String mqttServer = "tcp://10.0.8.101:1883";
	private MqttClient client = null;
	private int mMqttTimeout = -1;
	private CloudApi mCloudApi ;
	private CloudDeviceManagement mCloudDeviceManagement;
	//wifi info check
	private String nowWifi;
	private WifiManager wifi;

	private Gson gson;
	
	
	//DeviceListener
	private LinkedHashSet<CloudDeviceListener> cdListener = null;
	private LinkedHashMap<Integer, LinkedHashSet<RoomHubDeviceListener>> categoryDeviceListener = null;

	// SignalListener
	private LinkedHashSet<RoomHubSignalListener> rhListener = null;
	// new SignalListener
	private LinkedHashSet<HomeApplianceSignalListener> homeApplianceSignalListeners = null;

	private LinkedHashSet<UserFriendListener> ufListener = null;

	private LinkedHashSet<OTACloudStateUpdateListener> otaStateChangeListener = null;

	private LinkedHashSet<ShareUserListener> suListener = null;

	private LinkedHashSet<DeviceDefaultUserListener> dfuListener = null;

	MqttCallback mqttCallback = new MqttCallback() {

		public void messageArrived(String topic, MqttMessage message) throws Exception {
			// TODO Auto-generated method stub
			String msgData = message.toString();
			Log.d(TAG, "MQTT: " + msgData);

			try {
				final MessageRecvPack pack = gson.fromJson(msgData, MessageRecvPack.class);
				Log.d(TAG, "MQTT: Recv Type : " + pack.getType());
				switch (pack.getType()) {
					case 1:

						break;
					case 10100: { //Add Device


						CloudDevice dev = gson.fromJson(pack.getData(), CloudDevice.class);
						mCloudDeviceManagement.AddDevice(dev);
						if (cdListener != null) {
							for (CloudDeviceListener listener : cdListener) {
								listener.addDevice(dev);
							}
						}

						dispatchCloudAddDevice(dev,ReasonType.CLOUD);
						CheckTypeDevice(dev);

					}

					break;
					case 10200: { //Remove Device


						CloudDevice dev = gson.fromJson(pack.getData(), CloudDevice.class);
						mCloudDeviceManagement.Remove(dev);
						if (cdListener != null) {
							for (CloudDeviceListener listener : cdListener) {
								listener.removeDevice(dev);
							}
						}

						dispatchCloudRemoveDevice(dev,ReasonType.CLOUD);
					}
					break;
					case 10300:
					case 10400: { //Update Device
						// 10300: Device Name update
						// 10400: Device online status update

						CloudDevice dev = gson.fromJson(pack.getData(), CloudDevice.class);
						mCloudDeviceManagement.UpdateDevice(dev);

						dispatchCloudUpdateDevice(dev);
						// TODO: How to Update Device ??
						CheckTypeDevice(dev);

					}
					break;
					case 10500: {// Device info update
						IRDeviceStatus irDeviceStatus = gson.fromJson(pack.getData(), IRDeviceStatus.class);

						AcAssetDetailInfoResPack resPack = new AcAssetDetailInfoResPack();
						resPack.setRoomHubUUID(irDeviceStatus.getRoomHubUUID());
						resPack.setUuid(irDeviceStatus.getUuid());
						resPack.setBrand(irDeviceStatus.getBrand());
						resPack.setDevice(irDeviceStatus.getDevice());
						resPack.setPower(irDeviceStatus.getPower());
						resPack.setTemp(irDeviceStatus.getTemp());
						resPack.setMode(irDeviceStatus.getMode());
						resPack.setSwing(irDeviceStatus.getSwing());
						resPack.setFan(irDeviceStatus.getFan());
						resPack.setTimerOn(irDeviceStatus.getTimeOn());
						resPack.setTimerOff(irDeviceStatus.getTimeOff());
						resPack.setSubType(irDeviceStatus.getSubType());
						resPack.setConnectionType(irDeviceStatus.getConnectionType());
						resPack.setBrandId(irDeviceStatus.getBrandId());
						resPack.setModelId(irDeviceStatus.getModelId());
						resPack.setOnLineStatus(irDeviceStatus.getOnLineStatus());

						if (homeApplianceSignalListeners != null) {
							for (HomeApplianceSignalListener listener : homeApplianceSignalListeners) {
								listener.AssetInfoChange(RoomHubAllJoynDef.assetType.ASSET_TYPE_AC, resPack, SourceType.CLOUD);
							}
						}
					}
						break;
					case 10600: //Device share user change notification
						CloudDevice share_device = gson.fromJson(pack.getData(), CloudDevice.class);
						Log.d(TAG,"MQTT Device share user targetUser="+share_device.getTagetUser()+" user_id="+share_device.getUser_id());
						share_device.setRoleName(CloudDevice.ROLE_USER);
						share_device.setUser_id(share_device.getTagetUser());

						if(share_device.getType().equals("add")){
							Log.d(TAG,"MQTT Device share user add targetUser="+share_device.getTagetUser()+" user_id="+share_device.getUser_id());

							dispatchCloudAddDevice(share_device, ReasonType.USERSHARE);

							if(suListener != null){
								for(ShareUserListener listener: suListener){
									listener.addShareUser(share_device);
								}
							}
						}else{
							Log.d(TAG,"MQTT Device share user remove targetUser="+share_device.getTagetUser()+" user_id="+share_device.getUser_id());

							dispatchCloudRemoveDevice(share_device, ReasonType.USERSHARE);

							if(suListener != null){
								for(ShareUserListener listener: suListener){
									listener.removeShareUser(share_device);
								}
							}
						}
						break;
					case 10700: { //Device firmware update state
						DeviceFirmwareUpdateStateResPack resPack = gson.fromJson(pack.getData(), DeviceFirmwareUpdateStateResPack.class);
						if (otaStateChangeListener != null) {
							for (OTACloudStateUpdateListener listener : otaStateChangeListener) {
								listener.stateChange(resPack);
							}
						}
						if (rhListener != null) {
							for (RoomHubSignalListener listener : rhListener) {
								listener.RoomHubOTAUpgradeStateChangeUpdate(resPack);
							}
						}
					}
						break;
					case 10800: { // Add device asset
						AssetResPack resPack = gson.fromJson(pack.getData(), AssetResPack.class);
						if(resPack != null) {
							if(TextUtils.isEmpty(resPack.getAssetUuid()))
								resPack.setAssetUuid(resPack.getUuid());
							resPack.setUuid(resPack.getRoomHubUUID());
							if(homeApplianceSignalListeners != null) {
								for (HomeApplianceSignalListener listener : homeApplianceSignalListeners) {
									listener.addAsset(resPack,SourceType.CLOUD);
								}
							}
						}
					}
						break;
					case 10900: { // Update device asset
						AssetResPack resPack = gson.fromJson(pack.getData(), AssetResPack.class);
						if(resPack != null) {
							if(TextUtils.isEmpty(resPack.getAssetUuid()))
								resPack.setAssetUuid(resPack.getUuid());
							resPack.setUuid(resPack.getRoomHubUUID());
							if(homeApplianceSignalListeners != null) {
								for (HomeApplianceSignalListener listener : homeApplianceSignalListeners) {
									listener.updateAsset(resPack,SourceType.CLOUD);
								}
							}
						}
					}
						break;
					case 11000: { // Delete device asset
						AssetResPack resPack = gson.fromJson(pack.getData(), AssetResPack.class);
						if(resPack != null) {
							if(TextUtils.isEmpty(resPack.getAssetUuid()))
								resPack.setAssetUuid(resPack.getUuid());
							resPack.setUuid(resPack.getRoomHubUUID());
							if(homeApplianceSignalListeners != null) {
								for (HomeApplianceSignalListener listener : homeApplianceSignalListeners) {
									listener.removeAsset(resPack,SourceType.CLOUD);
								}
							}
						}
					}
						break;
					case 11100: {// Send IR Asset Status
						IRFANStatus irFanStatus = gson.fromJson(pack.getData(), IRFANStatus.class);

						FanAssetDetailInfoResPack resPack = new FanAssetDetailInfoResPack();
						resPack.setRoomHubUUID(irFanStatus.getRoomHubUUID());
						resPack.setUuid(irFanStatus.getUuid());
						resPack.setBrand(irFanStatus.getBrand());
						resPack.setDevice(irFanStatus.getDevice());
						resPack.setPower(irFanStatus.getPower());
						IRFANProperty irfanProperty = gson.fromJson(irFanStatus.getProperty(), IRFANProperty.class);
						resPack.setSwing(irfanProperty.getSwing());

						if (homeApplianceSignalListeners != null) {
							for (HomeApplianceSignalListener listener : homeApplianceSignalListeners) {
								listener.AssetInfoChange(RoomHubAllJoynDef.assetType.ASSET_TYPE_FAN, resPack, SourceType.CLOUD);
							}
						}
					}
						break;
					case 11200: //Device signal
						DeviceSignal deviceSignal = gson.fromJson(pack.getData(), DeviceSignal.class);
						AssetRecvPack assetPack = gson.fromJson(deviceSignal.getSignal(), AssetRecvPack.class);
						switch (assetPack.getAssetType()) {
							case RoomHubAllJoynDef.assetType.ASSET_TYPE_AIRPURIFIER:
								AirPurifierAssetDetailInfoResPack airPurifierAssetDetailInfoResPack =
										gson.fromJson(deviceSignal.getSignal(), AirPurifierAssetDetailInfoResPack.class);
								airPurifierAssetDetailInfoResPack.setRoomHubUUID(deviceSignal.getRoomHubUUID());
								if (homeApplianceSignalListeners != null) {
									for (HomeApplianceSignalListener listener : homeApplianceSignalListeners) {
										listener.AssetInfoChange(RoomHubAllJoynDef.assetType.ASSET_TYPE_AIRPURIFIER, airPurifierAssetDetailInfoResPack, SourceType.CLOUD);
									}
								}
								break;
							case RoomHubAllJoynDef.assetType.ASSET_TYPE_PM25:
								PMAssetDetailInfoResPack pmAssetDetailInfoResPack =
										gson.fromJson(deviceSignal.getSignal(), PMAssetDetailInfoResPack.class);
								pmAssetDetailInfoResPack.setRoomHubUUID(deviceSignal.getRoomHubUUID());
								if (homeApplianceSignalListeners != null) {
									for (HomeApplianceSignalListener listener : homeApplianceSignalListeners) {
										listener.AssetInfoChange(RoomHubAllJoynDef.assetType.ASSET_TYPE_PM25, pmAssetDetailInfoResPack, SourceType.CLOUD);
									}
								}
								break;
							case RoomHubAllJoynDef.assetType.ASSET_TYPE_BULB:
								BulbAssetDetailInfoResPack bulbAssetDetailInfoResPack =
										gson.fromJson(deviceSignal.getSignal(), BulbAssetDetailInfoResPack.class);
								bulbAssetDetailInfoResPack.setRoomHubUUID(deviceSignal.getRoomHubUUID());
								if (homeApplianceSignalListeners != null) {
									for (HomeApplianceSignalListener listener : homeApplianceSignalListeners) {
										listener.AssetInfoChange(RoomHubAllJoynDef.assetType.ASSET_TYPE_BULB, bulbAssetDetailInfoResPack, SourceType.CLOUD);
									}
								}
								break;
							case RoomHubAllJoynDef.assetType.ASSET_TYPE_TV:
								TVAssetDetailInfoResPack tvAssetDetailInfoResPack =
										gson.fromJson(deviceSignal.getSignal(), TVAssetDetailInfoResPack.class);
								tvAssetDetailInfoResPack.setRoomHubUUID(deviceSignal.getRoomHubUUID());
								if (homeApplianceSignalListeners != null) {
									for (HomeApplianceSignalListener listener : homeApplianceSignalListeners) {
										listener.AssetInfoChange(RoomHubAllJoynDef.assetType.ASSET_TYPE_TV, tvAssetDetailInfoResPack, SourceType.CLOUD);
									}
								}
								break;
							default:
								break;
						}
						break;
					case 11400: {	// Device default user
						DeviceDefaultUserResPack res = gson.fromJson(pack.getData(), DeviceDefaultUserResPack.class);
						if(dfuListener != null) {
							for(DeviceDefaultUserListener listener:dfuListener) {
								listener.defaultUserChange(res);
							}
						}
					}
						break;
					case 20200:
						RoomHubDataResPack res = gson.fromJson(pack.getData(), RoomHubDataResPack.class);
						if (rhListener != null) {
							for (RoomHubSignalListener listener : rhListener) {
								listener.RoomHubDataUpdate(res, SourceType.CLOUD);
							}
						}
					break;
					case 20300: {
						Log.d(TAG,"MQTT BPM notification received");
						/*
						BloodPressureAssetDetailInfoResPack resPack = gson.fromJson(pack.getData(), BloodPressureAssetDetailInfoResPack.class);
						if (homeApplianceSignalListeners != null) {
							for (HomeApplianceSignalListener listener : homeApplianceSignalListeners) {
//								listener.AssetInfoChange(RoomHubAllJoynDef.assetType.ASSET_TYPE_SPHYGMOMETER, resPack, SourceType.CLOUD);
								// convert AllJoyn type to App type then send to upper layer
								DeviceTypeConvertApi.AppDeviceCategoryType categoryType =
										DeviceTypeConvertApi.ConvertType_AllJoynToApp(RoomHubAllJoynDef.assetType.ASSET_TYPE_SPHYGMOMETER);
								listener.AssetInfoChange(categoryType.getType(), resPack, SourceType.CLOUD);
							}
						}
						*/
						}
						break;
					case 30200: {	// Update all schedule
						GetDeviceSettingResPack getDeviceSettingResPack = gson.fromJson(pack.getData(), GetDeviceSettingResPack.class);
						if(getDeviceSettingResPack.getStatus_code() == 0 &&
								!TextUtils.isEmpty(getDeviceSettingResPack.getDeviceSetting())) {
							GetScheduleListResPack getScheduleListResPack = gson.fromJson(getDeviceSettingResPack.getDeviceSetting(), GetScheduleListResPack.class);
							if(getScheduleListResPack.getStatus_code()==0 &&
									getScheduleListResPack.getScheduleList() != null) {
								if (rhListener != null) {
									for (RoomHubSignalListener listener : rhListener) {
										listener.RoomHubUpdateAllSchedule(pack.getUuid(),getScheduleListResPack.getScheduleList());
									}
								}
							}
						}
					}
					break;
					case 90100: { //Add friend
							UserFriendResPack friend = gson.fromJson(pack.getData(), UserFriendResPack.class);
							if (ufListener != null) {
								for (UserFriendListener listener : ufListener) {
									listener.addFriend(friend);
								}
							}
						}
						break;
					case 90200: { //Update friend
							UserFriendResPack friend = gson.fromJson(pack.getData(), UserFriendResPack.class);
							if (ufListener != null) {
								for (UserFriendListener listener : ufListener) {
									listener.updateFriend(friend);
								}
							}
						}
						break;
					case 90300: { //Remove friend
							UserFriendResPack friend = gson.fromJson(pack.getData(), UserFriendResPack.class);
							if (ufListener != null) {
								for (UserFriendListener listener : ufListener) {
									listener.removeFriend(friend);
								}
							}
						}
						break;
					case 90400: { //Save User Shared Data
							UserSharedDataResPack user_share_data = gson.fromJson(pack.getData(), UserSharedDataResPack.class);
							if(user_share_data != null) {
								DeviceTypeConvertApi.AppDeviceCategoryType appDeviceCategoryType
										= DeviceTypeConvertApi.ConvertType_CloudToApp(user_share_data.getDeviceType());
								// convert to app device type
								if(appDeviceCategoryType != null)
									user_share_data.setDeviceType(appDeviceCategoryType.getType());
							}
							if (suListener != null) {
								for (ShareUserListener listener : suListener) {
									listener.UserSharedData(user_share_data);
								}
							}
						}
						break;
					default:
						break;
				}
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
		}


		public void deliveryComplete(IMqttDeliveryToken token) {
			// TODO Auto-generated method stub

		}


		public void connectionLost(Throwable e) {
			// TODO Auto-generated method stub

			Log.d(TAG, "MQTT: connectionLost!!" + e);

			if (client != null) {

				if (mResPack != null && mResPack.getMqttTopic() != null) {
					client.setCallback(null);
				}
				client = null;
				Log.d(TAG, "MQTT: disconnect!!");

			}

		}
	};


	BroadcastReceiver WifiBroadcastReceiver = new BroadcastReceiver() {
		boolean connectionStatus = false;
		private Task mTask;
		boolean mOnBoardingRunning = false;

		@Override
		public void onReceive(Context context, Intent intent) {
			if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent.getAction())) {
				if (mTask != null && mTask.getStatus() != AsyncTask.Status.FINISHED) {
					return;
				}
				mTask = new Task();
				mTask.execute();
			} else if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(intent.getAction())) {

			} else if (ONBOARDING_START_ACTION.equals(intent.getAction())) {
				mOnBoardingRunning = true;
				roomHubBusHandler.sendEmptyMessage(RoomHubBusHandler.DISCONNECT);
			} else if (ONBOARDING_STOP_ACTION.equals(intent.getAction())) {
				mOnBoardingRunning = false;
				roomHubBusHandler.sendEmptyMessage(RoomHubBusHandler.CONNECT);
			} else if (CONNECT_ROOMHUB_ALLJOYN_ACTION.equals(intent.getAction())) {
				Log.d(TAG, "--==[ Connect roomhub alljoyn ]==--");
				roomHubBusHandler.sendEmptyMessage(RoomHubBusHandler.CONNECT);
			} else if (DISCONNECT_ROOMHUB_ALLJOYN_ACTION.equals(intent.getAction())) {
				Log.d(TAG, "--==[ disconnect roomhub alljoyn ]==--");
				roomHubBusHandler.sendEmptyMessage(RoomHubBusHandler.DISCONNECT);
			}
		}

		class Task extends AsyncTask<Void, Void, Integer> {
			@Override
			protected Integer doInBackground(Void... params) {
				Log.d(TAG, "---- WifiBroadcastReceiver:doInBackground asynctask ----");
				ConnectivityManager connMgr = (ConnectivityManager) mContext
						.getSystemService(Context.CONNECTIVITY_SERVICE);

				NetworkInfo wifiInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
				WifiInfo info = wifi.getConnectionInfo();
				String ssid = info.getSSID();
				nowWifi = ssid;
				Log.d(TAG,
						"WifiName:  " + "SSID " + ssid + " IP :" + info.getIpAddress() + " " + wifiInfo.isAvailable()
								+ " " + wifiInfo.isConnected() + " " + wifiInfo.isConnectedOrConnecting());

				if (ssid.equals("<unknown ssid>") || ssid.equals("0x")) {
					connectionStatus = true;
					Log.d(TAG, "Alljoyn Unknow WifiName:  " + "SSID " + ssid);

					if(!mOnBoardingRunning) {
						new Thread() {
							@Override
							public void run() {
								switchNetwork(false);
							}
						}.start();
						// delay 3 secs to disconnect from alljoyn
						roomHubBusHandler.sendEmptyMessageDelayed(RoomHubBusHandler.DISCONNECT, 3000);
					}
				}
				else {
					if (connectionStatus && info.getIpAddress() != 0) {

						connectionStatus = false;
						Log.d(TAG, "Alljoyn WifiName:  " + "SSID " + ssid);

						if (!mOnBoardingRunning) {
							// delay 3 secs to connect AllJoyn
							new Thread() {
								@Override
								public void run() {
									switchNetwork(true);
								}
							}.start();
//							roomHubBusHandler.sendEmptyMessage(RoomHubBusHandler.CONNECT);
							roomHubBusHandler.sendEmptyMessageDelayed(RoomHubBusHandler.CONNECT, 3000);
						}
					}
				}

				Log.d(TAG, "---- WifiBroadcastReceiver:doInBackground [finished]----");
				return null;
			}

			@Override
			protected void onPostExecute(Integer integer) {
				super.onPostExecute(integer);
			}

		}
	};

	BroadcastReceiver connectionBroadcastReceiver = new BroadcastReceiver() {
		private Task mTask;
		boolean connectionStatus = false;

		@Override
		public void onReceive(Context context, Intent intent) {
			if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
				if (mTask != null && mTask.getStatus() != AsyncTask.Status.FINISHED) {
					return;
				}
				mTask = new Task();
				mTask.execute();
			}
		}

		class Task extends AsyncTask<Void, Void, Integer> {
			@Override
			protected Integer doInBackground(Void... params) {
				Log.d(TAG, "---- connectionBroadcastReceiver:doInBackground asynctask ----");
				ConnectivityManager connMgr = (ConnectivityManager) mContext
						.getSystemService(Context.CONNECTIVITY_SERVICE);
				NetworkInfo activeNetworkInfo = connMgr.getActiveNetworkInfo();
				Log.d(TAG, "activeNetworkInfo="+activeNetworkInfo);
				if(activeNetworkInfo != null) {
					Log.d(TAG, "activeNetworkInfo type="+activeNetworkInfo.getTypeName());
					if(activeNetworkInfo.isConnected()) {
						if(activeNetworkInfo.getType()==ConnectivityManager.TYPE_WIFI) {
							NetworkInfo wifiInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
							WifiInfo info = wifi.getConnectionInfo();
							String ssid = info.getSSID();
							nowWifi = ssid;
							if (!nowWifi.contains("H60AJ_")) {
								connectMQTT();
							}
						}
						else
							connectMQTT();
					}
					else {
						disconnectMQTT();
					}
				}
				Log.d(TAG, "---- connectionBroadcastReceiver:doInBackground [finished]----");
				return null;
			}

			@Override
			protected void onPostExecute(Integer integer) {
				super.onPostExecute(integer);
			}
		}

		private void connectMQTT() {
			Log.d(TAG, "connectMQTT() enter");
			while (!connectionStatus) {
				try {
					if (mResPack != null && mResPack.getMqttTopic() != null) {

						if (client != null) {
							client.setCallback(null);
							client = null;
						}
						if (client == null) {
							Log.d(TAG, "MQTT: Try Client!!!!" + mResPack.getMqttTopic());
							String id = Secure.getString(mContext.getContentResolver(), Secure.ANDROID_ID);
							client = new MqttClient(mqttServer, id, new MemoryPersistence());
							// client = new
							// MqttAndroidClient(getApplicationContext(),
							// "tcp://10.0.8.100:1883", id);
							client.setCallback(mqttCallback);

							// client.connect(null,
							// mqttListener);
							MqttConnectOptions options = new MqttConnectOptions();
							options.setKeepAliveInterval(
									MqttConnectOptions.KEEP_ALIVE_INTERVAL_DEFAULT);
							options.setConnectionTimeout(0);

							MqttConnectOptions conOpts = new MqttConnectOptions();
							conOpts.setKeepAliveInterval(30);
							conOpts.setWill(client.getTopic("Error"),
									"something bad happened".getBytes(), 1, true);

							// client.connect(options, null,
							// mqttListener);
							client.setTimeToWait(mMqttTimeout);
							client.connect(conOpts);
							client.subscribe(mResPack.getMqttTopic());
							Log.d(TAG, "Reconnect MQTT success!!");

						}
						break;
					} else {
						break;
					}

				} catch (MqttException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				try {
					Thread.sleep(1500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}

		private void disconnectMQTT() {
			Log.d(TAG, "disconnectMQTT() enter");
			connectionStatus = false;
			if (client != null) {
				try {
					if (mResPack != null && mResPack.getMqttTopic() != null)

					{
						client.setCallback(null);
						// client.unregisterResources();
						client.unsubscribe(mResPack.getMqttTopic());
						client.disconnect();
					}

					// client.close();
					client = null;
					Log.d(TAG, "MQTT: disconnect!!");
				} catch (MqttException e) {
					client = null;
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	};

	public static class ApiConfig {
		private String mMqttServer;
		private String mCloudAddress;

		public ApiConfig(String mqttServer, String cloudAddress) {
			if(mqttServer != null) this.mMqttServer = mqttServer;
			if(cloudAddress != null) this.mCloudAddress = cloudAddress;
		}
	}

	private MiddlewareApi(Context mContext) {
		this(mContext, null);
	}

	private MiddlewareApi(Context mContext, ApiConfig mApiConfig)
	{
		this.mContext = mContext;

		cdListener = new LinkedHashSet<CloudDeviceListener>();

		// RoomHub Device & Signal Listener
		categoryDeviceListener = new LinkedHashMap<>();

		rhListener = new LinkedHashSet<RoomHubSignalListener>();

		ufListener = new LinkedHashSet<UserFriendListener>();

		homeApplianceSignalListeners = new LinkedHashSet<HomeApplianceSignalListener>();

		otaStateChangeListener = new LinkedHashSet<>();

		suListener =new LinkedHashSet<ShareUserListener>();

		dfuListener = new LinkedHashSet<>();

		HandlerThread busThread = new HandlerThread("RoomHubBusHandler");
		busThread.start();
		roomHubBusHandler = new RoomHubBusHandler(busThread.getLooper(), mContext);
		roomHubBusHandler.sendEmptyMessage(RoomHubBusHandler.CONNECT);
		roomHubBusHandler.setRoomHubDeviceListener(this);
		roomHubBusHandler.setRoomHubSignalListener(this);
		roomHubBusHandler.setHomeApplianceSignalListener(this);

		this.mApiConfig = mApiConfig;
		if(this.mApiConfig != null && this.mApiConfig.mMqttServer != null) {
			mqttServer = this.mApiConfig.mMqttServer;
		}
		if(this.mApiConfig != null && this.mApiConfig.mCloudAddress != null) {
			mCloudApi = CloudApi.getInstance(this.mApiConfig.mCloudAddress, getClientId());
		} else {
			mCloudApi = CloudApi.getInstance();
		}

		wifi = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);

		mContext.registerReceiver(WifiBroadcastReceiver,
				new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION));
		mContext.registerReceiver(WifiBroadcastReceiver, new IntentFilter(CONNECT_ROOMHUB_ALLJOYN_ACTION));
		mContext.registerReceiver(WifiBroadcastReceiver, new IntentFilter(DISCONNECT_ROOMHUB_ALLJOYN_ACTION));
		mContext.registerReceiver(WifiBroadcastReceiver, new IntentFilter(ONBOARDING_START_ACTION));
		mContext.registerReceiver(WifiBroadcastReceiver, new IntentFilter(ONBOARDING_STOP_ACTION));

		/* for connect/disconnect MQTT */
		mContext.registerReceiver(connectionBroadcastReceiver,
				new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

		gson = new GsonBuilder().create();
		
		mCloudDeviceManagement = new CloudDeviceManagement();

		int resId = mContext.getResources().getIdentifier("config_mqtt_timeout", "integer", "com.quantatw.myapplication");
		mMqttTimeout = mContext.getResources().getInteger(resId);
		if(mMqttTimeout < 0) {
			// millisecond
			mMqttTimeout = 30000;
		}

	}

	public static MiddlewareApi getInstance(Context mContext)
	{
		if(instance == null)
		{
			instance = new MiddlewareApi(mContext);
		}

		return instance;
	}

	public static MiddlewareApi getInstance(Context mContext, ApiConfig mApiConfig)
	{
		if(instance == null)
		{
			instance = new MiddlewareApi(mContext, mApiConfig);
		}
			
		
		return instance;
	}

	private String getClientId() {
//		WifiManager wifiMan = (WifiManager) mContext.getSystemService(android.content.Context.WIFI_SERVICE);
//		WifiInfo wifiInf = wifiMan.getConnectionInfo();
//		String clientId = wifiInf.getMacAddress();
		String clientId = Secure.getString(mContext.getContentResolver(), Secure.ANDROID_ID);
		clientId = "Android-" + clientId.replaceAll(":", "");

		return clientId;
	}

//	public CloudDeviceManagement getCloudDeviceManagement() {
//		return mCloudDeviceManagement;
//	}
//
//	public void setCloudDeviceManagement(CloudDeviceManagement mCloudDeviceManagement) {
//		this.mCloudDeviceManagement = mCloudDeviceManagement;
//	}

	/* User ------------------------------------------------------------------------------------ */
	public AccountResPack userRegister(final AccountRegisterReqPack _AccountRegisterReqPack, String language) {
		// TODO Auto-generated method stub
		if(_AccountRegisterReqPack.getClientId() == null) {
			_AccountRegisterReqPack.setClientId(getClientId());
		}
		_AccountRegisterReqPack.setUserPw(md5(_AccountRegisterReqPack.getUserPw()));
		mResPack = mCloudApi.UserRegisterREQ(_AccountRegisterReqPack, language);
		/*
		if (mResPack.getStatus_code() == 0) {
			// GetDevicesReqPack req = new GetDevicesReqPack();
			// req.setUser_id(mResPack.getUser_id());
			// GetCloudDevicesResPack re =
			// mCloudApi.GetDevicesREQ(req);
			// if(re.getStatus_code() >= 0)
			// {
			// for(int x = 0;x < re.getDevices().size() ;x++)
			// {
			//
			// }
			// }

			try {
				String id = Secure.getString(mContext.getContentResolver(), Secure.ANDROID_ID);
				client = new MqttClient(mqttServer, id, new MemoryPersistence());
				// client = new
				// MqttAndroidClient(getApplicationContext(),
				// "tcp://10.0.8.100:1883", id);
				client.setCallback(mqttCallback);

				// client.connect(null, mqttListener);
				MqttConnectOptions options = new MqttConnectOptions();
				options.setKeepAliveInterval(MqttConnectOptions.KEEP_ALIVE_INTERVAL_DEFAULT);
				options.setConnectionTimeout(0);

				MqttConnectOptions conOpts = new MqttConnectOptions();
				conOpts.setKeepAliveInterval(30);
				conOpts.setWill(client.getTopic("Error"), "something bad happened".getBytes(), 1, true);

				// client.connect(options, null, mqttListener);
				client.setTimeToWait(mMqttTimeout);
				client.connectWithResult(conOpts);
				client.subscribe(mResPack.getMqttTopic());
				Log.d(TAG, "MQTT:  Connect Success!!!");
			} catch (MqttException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				mResPack.setStatus_code(ErrorKey.MQTT_CONNECT_ERROR);

				return mResPack;
			}

			GetDevicesReqPack req = new GetDevicesReqPack();
			GetCloudDevicesResPack re = mCloudApi.GetDevicesREQ(req);
			if (re.getStatus_code() == 0) {
				CheckTypeDevices(re);
			} else {
				mResPack.setStatus_code(ErrorKey.ConnectionError);
			}
		}
		*/
		return mResPack;
	}


	public AccountResPack userLogin(final AccountLoginReqPack _AccountLoginReqPack, boolean silent) {
		if(_AccountLoginReqPack.getClientId() == null) {
			_AccountLoginReqPack.setClientId(getClientId());
		}
		_AccountLoginReqPack.setUserPw(md5(_AccountLoginReqPack.getUserPw()));
		//_AccountLoginReqPack.setUserPw(_AccountLoginReqPack.getUserPw());
		mResPack = mCloudApi.UserLoginREQ(_AccountLoginReqPack);
		if(silent) {
			return mResPack;
		}

		if (mResPack.getStatus_code() == 0) {
			try {
				String id = Secure.getString(mContext.getContentResolver(), Secure.ANDROID_ID);
				if(client != null)
					client.disconnect();
				client = new MqttClient(mqttServer, id, new MemoryPersistence());
				// client = new
				// MqttAndroidClient(getApplicationContext(),
				// "tcp://10.0.8.100:1883", id);
				client.setCallback(mqttCallback);

				// client.connect(null, mqttListener);
				MqttConnectOptions options = new MqttConnectOptions();
				options.setKeepAliveInterval(MqttConnectOptions.KEEP_ALIVE_INTERVAL_DEFAULT);
				options.setConnectionTimeout(0);

				MqttConnectOptions conOpts = new MqttConnectOptions();
				conOpts.setKeepAliveInterval(30);
				conOpts.setWill(client.getTopic("Error"), "something bad happened".getBytes(), 1, true);

				// client.connect(options, null, mqttListener);
				client.setTimeToWait(mMqttTimeout);
				client.connect(conOpts);

				client.subscribe(mResPack.getMqttTopic());
				Log.d(TAG, "MQTT:Connect success!!!" + mResPack.getMqttTopic());
			} catch (MqttException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				// MQTT Connect error
				mResPack.setStatus_code(ErrorKey.MQTT_CONNECT_ERROR);
				Log.d(TAG, "mqtt exception: " + ErrorKey.MQTT_CONNECT_ERROR);

				return mResPack;
			}

			GetDevicesReqPack req = new GetDevicesReqPack();
			GetCloudDevicesResPack re = mCloudApi.GetDevicesREQ(req);
			if (re.getStatus_code() == 0) {
				CheckTypeDevices(re);
			} else {
				// FIXME: Add Get device list error code
				mResPack.setStatus_code(ErrorKey.ConnectionError);
			}

		}

		// MqttAndroidClient client = new
		// MqttAndroidClient(getApplicationContext(), "10.0.8.100",
		// "");
		// client.swindsno
		return mResPack;
	}


	public AccountResPack userRegister(final FBAccountReqPack _FBAccountReqPack) {
		// TODO Auto-generated method stub


		mResPack = mCloudApi.FBUserLoginREQ(_FBAccountReqPack);
		if (mResPack.getStatus_code() == 0) {

			try {
				String id = Secure.getString(mContext.getContentResolver(), Secure.ANDROID_ID);
				client = new MqttClient(mqttServer, id, new MemoryPersistence());
				// client = new
				// MqttAndroidClient(getApplicationContext(),
				// "tcp://10.0.8.100:1883", id);
				client.setCallback(mqttCallback);

				// client.connect(null, mqttListener);
				MqttConnectOptions options = new MqttConnectOptions();
				options.setKeepAliveInterval(MqttConnectOptions.KEEP_ALIVE_INTERVAL_DEFAULT);
				options.setConnectionTimeout(0);

				MqttConnectOptions conOpts = new MqttConnectOptions();
				conOpts.setKeepAliveInterval(30);
				conOpts.setWill(client.getTopic("Error"), "something bad happened".getBytes(), 1, true);

				// client.connect(options, null, mqttListener);
				client.setTimeToWait(mMqttTimeout);
				client.connect(conOpts);
				client.subscribe(mResPack.getMqttTopic());
				Log.d(TAG, "MQTT: Connect Success!!!");
			} catch (MqttException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				// MQTT Connect error
				mResPack.setStatus_code(ErrorKey.MQTT_CONNECT_ERROR);

				return mResPack;
			}

			GetDevicesReqPack req = new GetDevicesReqPack();
			GetCloudDevicesResPack re = mCloudApi.GetDevicesREQ(req);
			if (re.getStatus_code() == 0) {
				CheckTypeDevices(re);
			} else {
				mResPack.setStatus_code(ErrorKey.ConnectionError);
			}

		}
		return mResPack;
	}


	public BaseResPack forgetPassword(final ForgetPasswordReqPack reqPack, String language) {
		// TODO Auto-generated method stub

		BaseResPack resPack = mCloudApi.ForgetPasswordREQ(reqPack, language);

		return resPack;
	}

	
	public BaseResPack changePassword(final ChangePasswordReqPack reqPack)  {
		// TODO Auto-generated method stub
		reqPack.setOldPassword(md5(reqPack.getOldPassword()));
		reqPack.setNewPassword(md5(reqPack.getNewPassword()));
		BaseResPack resPack = mCloudApi.ChangePasswordREQ(reqPack);
		return resPack;
			
	}

	public void userLogout()  {
		// TODO Auto-generated method stub
//		if (client != null && client.isConnected()) {
//			try {
//				client.disconnect();
//			} catch (MqttException e) {
//				client = null;
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			client = null;
//		}
		if (client != null) {
			try {
				if (mResPack != null && mResPack.getMqttTopic() != null)

				{
					client.setCallback(null);
					// client.unregisterResources();
					client.unsubscribe(mResPack.getMqttTopic());
					client.disconnect();
				}
			} catch (MqttException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			finally {
				client = null;
				mResPack = null;
				Log.d(TAG, "MQTT: disconnect!!");
			}
		}

		// Send remove device signal
		ArrayList<CloudDevice> cloudDevList = mCloudDeviceManagement.GetClone();
		if(cloudDevList != null) {
			for (CloudDevice dev : cloudDevList) {
				dispatchCloudRemoveDevice(dev, ReasonType.LOGOUT);
			}
		}
		mCloudDeviceManagement.RemoveAll();

	}

	public BaseResPack UpdateUserProfile(UpdateUserProfileReqPack userProfileReqPack) {
		BaseResPack resPack = mCloudApi.UpdateUserProfile(userProfileReqPack);
		return resPack;
	}

	public BaseResPack SendAuthorizeEmail(SendAuthorizeEmailReqPack sendAuthorizeEmailReqPack) {
		BaseResPack resPack = mCloudApi.SendAuthorizeEmail(sendAuthorizeEmailReqPack);
		return resPack;
	}

	public BaseResPack CheckAccountOrEmailExist(AccountInfoCheckReqPack accountInfoCheckReqPack) {
		BaseResPack resPack = mCloudApi.CheckAccountOrEmailExist(accountInfoCheckReqPack);
		return resPack;
	}

	public JwtPayloadResPack GetJwtPayload() {
		JwtPayloadResPack resPack = mCloudApi.GetJwtPayloadREQ();
		return resPack;
	}

	public GetUserFriendListResPack GetUserFriendListREQ() {
		GetUserFriendListResPack resPack = mCloudApi.GetUserFriendListREQ();
		return resPack;
	}

	public BaseResPack AddUserFriend(AddUserFriendReqPack addUserFriendReqPack) {
		BaseResPack resPack = mCloudApi.AddUserFriend(addUserFriendReqPack);
		return resPack;
	}

	public BaseResPack RemoveUserFriend(RemoveUserFriendReqPack removeUserFriendReqPack) {
		BaseResPack resPack = mCloudApi.RemoveUserFriend(removeUserFriendReqPack);
		return resPack;
	}

	public BaseResPack modifyUserFriendNickName(ModifyNickNameReqPack modifyNickNameReqPack) {
		BaseResPack resPack = mCloudApi.modifyNickName(modifyNickNameReqPack);
		return resPack;
	}
	/* User ------------------------------------------------------------------------------------ */

	/* Device----------------------------------------------------------------------------------- */
	public AddCloudDeviceResPack addDevice(final AddDeviceReqPack _AddDeviceReqPack)  {
		// TODO Auto-generated method stub

		// TODO: type transfer
		int convertType = DeviceTypeConvertApi.ConvertType_AllJoynToCloud(_AddDeviceReqPack.getDeviceType());

		if(convertType == DeviceTypeConvertApi.TYPE_NOT_FOUND)
			convertType = DeviceTypeConvertApi.ConvertType_AppToCloud(
					new DeviceTypeConvertApi.AppDeviceCategoryType(_AddDeviceReqPack.getCategory(),_AddDeviceReqPack.getDeviceType()));

		if(convertType != DeviceTypeConvertApi.TYPE_NOT_FOUND)
			_AddDeviceReqPack.setDeviceType(convertType);

		AddCloudDeviceResPack resPack = mCloudApi.AddDeviceREQ(_AddDeviceReqPack);

		return resPack;
	}

	
	public BaseResPack deleteDevice(final DeleteDeviceReqPack _DeleteDeviceReqPack)  {
		// TODO Auto-generated method stub


		BaseResPack resPack = mCloudApi.DeleteDeviceREQ(_DeleteDeviceReqPack);
		return resPack;
	}

	public BaseResPack modifyDeviceName(String uuid, final ModifyDeviceNameReqPack _ModifyDeviceNameReqPack)  {
		// TODO Auto-generated method stub


		BaseResPack resPack = mCloudApi.ModifyDeviceNameREQ(uuid, _ModifyDeviceNameReqPack);
		return resPack;
	}
	
	public BaseResPack setNotificationTokenREQ(final AccountNotificationTokenReqPack reqPack, String language) {
		// TODO Auto-generated method stub

		BaseResPack resPack = mCloudApi.SetNotificationTokenREQ(reqPack,language);
		return resPack;

	}

	public GetCloudDeviceStatusResPack GetDeviceInfo(GetDeviceStatusReqPack _GetDeviceStatusReqPack) {
		// TODO Auto-generated method stub

		GetCloudDeviceStatusResPack resPack = mCloudApi.GetDeviceStatusREQ(_GetDeviceStatusReqPack);

		return resPack;
	}
	
	private GetCloudDevicesResPack getAllDeviceList()  {
		GetDevicesReqPack req = new GetDevicesReqPack();
		GetCloudDevicesResPack re = mCloudApi.GetDevicesREQ(req);

		if (re.getStatus_code() == 0) {
			CheckTypeDevices(re);
		} else {
			re.setStatus_code(ErrorKey.ConnectionError);
		}

		return re;
	}

//	public RoomHubDevice changeCloudDeviceToRoomHubDevice(CloudDevice device)
//	{
//		if(device.getDeviceType() == DeviceType.RoomHubDevice)
//		{
//			RoomHubDevice dev  = roomHubBusHandler.getDeviceManagment().getAlljoynDeviceFromCloudDevice(device);
//			if(dev == null)
//			{
//				dev = new RoomHubDevice();
//				dev.setUuid(device.getUuid());
//				dev.setName(device.getDevice_name());
//				dev.setExtraInfo(device);
//			}
//
//			return dev;
//		}else {
//			return null;
//		}
//	}

	private void CheckTypeDevices(GetCloudDevicesResPack re)
	{
		int x =0;
		mCloudDeviceManagement.RemoveAll();
		ArrayList<CloudDevice> cloudList = re.getDevices();
		
		for(x=0;x<cloudList.size() ;x++) {
			CloudDevice dev = cloudList.get(x);
			mCloudDeviceManagement.AddDevice(dev);

			// TODO: move to BaseManager
//			if(dev.getDeviceType() == DeviceType.RoomHubDevice)
//			{
//				if(roomHubBusHandler.getDeviceManagment().CheckCloudDeviceInAlljoyn(dev))
//				{
//					dev.setOnlineStatus(true);
//					dev.setLocal(true);
//				}
//			}

			dispatchCloudAddDevice(dev,ReasonType.CLOUD);
			/*
			if (dev.getDeviceType() == DeviceType.RoomHubDevice) {
				if (rhDeviceListener != null) {
					RoomHubDevice roomHubDevice = RoomHubDevice.createDevice(dev, SourceType.CLOUD);
					roomHubDevice.setUuid(dev.getUuid());

					for (RoomHubDeviceListener listener : rhDeviceListener) {
						listener.addDevice(roomHubDevice,ReasonType.CLOUD);
					}
				}
			}*/

			// TODO: move to BaseManager
//			if(dev.isOnlineStatus())
//			{
//				if(cdListener != null) {
//					for (CloudDeviceListener listener : cdListener) {
//						listener.onlineDevice(dev);
//					}
//				}
//			}
//			else
//			{
//				if(cdListener != null) {
//					for (CloudDeviceListener listener : cdListener) {
//						listener.offlineDevice(dev);
//					}
//				}
//			}
			
		}

		if(cloudList.size() > 0){
			mContext.sendBroadcast(new Intent(MiddlewareApi.CLOUD_DONE_ACTION));
		}
	}

	private void dispatchCloudAddDevice(CloudDevice cloudDevice, ReasonType reasonType) {
		int category = DeviceTypeConvertApi.ConvertType_GetCategoryByCloudDeviceType(cloudDevice.getDeviceType());
		LinkedHashSet<RoomHubDeviceListener> deviceListenerLinkedHashSet =
				getDeviceListenerByCategory(category);

		if(deviceListenerLinkedHashSet != null) {
			RoomHubDevice roomHubDevice = RoomHubDevice.createDevice(cloudDevice, SourceType.CLOUD);
			roomHubDevice.setUuid(cloudDevice.getUuid());
			for(RoomHubDeviceListener listener: deviceListenerLinkedHashSet) {
				listener.addDevice(roomHubDevice, reasonType);
			}
		}
	}

	private void dispatchCloudRemoveDevice(CloudDevice cloudDevice, ReasonType reasonType) {
		int category = DeviceTypeConvertApi.ConvertType_GetCategoryByCloudDeviceType(cloudDevice.getDeviceType());
		LinkedHashSet<RoomHubDeviceListener> deviceListenerLinkedHashSet =
				getDeviceListenerByCategory(category);

		RoomHubDevice roomHubDevice = RoomHubDevice.createDevice(cloudDevice, SourceType.CLOUD);
		roomHubDevice.setUuid(cloudDevice.getUuid());

		if(deviceListenerLinkedHashSet != null) {
			for(RoomHubDeviceListener listener: deviceListenerLinkedHashSet) {
				listener.removeDevice(roomHubDevice, reasonType);
			}
		}
		else {
			// notify all
			for(int keyCategory: categoryDeviceListener.keySet()) {
				LinkedHashSet<RoomHubDeviceListener> listenerByCategory =
						getDeviceListenerByCategory(keyCategory);
				for(RoomHubDeviceListener listener:listenerByCategory) {
					listener.removeDevice(roomHubDevice, reasonType);
				}
			}
		}
	}

	private void dispatchCloudUpdateDevice(CloudDevice cloudDevice) {
		int category = DeviceTypeConvertApi.ConvertType_GetCategoryByCloudDeviceType(cloudDevice.getDeviceType());
		LinkedHashSet<RoomHubDeviceListener> deviceListenerLinkedHashSet =
				getDeviceListenerByCategory(category);

		RoomHubDevice roomHubDevice = RoomHubDevice.createDevice(cloudDevice, SourceType.CLOUD);
		roomHubDevice.setUuid(cloudDevice.getUuid());

		if(deviceListenerLinkedHashSet != null) {
			for(RoomHubDeviceListener listener: deviceListenerLinkedHashSet) {
				listener.updateDevice(roomHubDevice);
			}
		}
		else {
			// notify all
			for(int keyCategory: categoryDeviceListener.keySet()) {
				LinkedHashSet<RoomHubDeviceListener> listenerByCategory =
						getDeviceListenerByCategory(keyCategory);
				for(RoomHubDeviceListener listener:listenerByCategory) {
					listener.updateDevice(roomHubDevice);
				}
			}
		}
	}

	private void CheckTypeDevice(CloudDevice dev)
	{

		// TODO: move to BaseManager
//		if (dev.getDeviceType() == DeviceType.RoomHubDevice) {
//			if (roomHubBusHandler.getDeviceManagment().CheckCloudDeviceInAlljoyn(dev)) {
//				dev.setOnlineStatus(true);
//				dev.setLocal(true);
//			}
//		}
//
//		if (dev.isOnlineStatus()) {
//			if (cdListener != null) {
//				for (CloudDeviceListener listener : cdListener) {
//					listener.onlineDevice(dev);
//				}
//			}
//		} else {
//			if (cdListener != null) {
//				for (CloudDeviceListener listener : cdListener) {
//					listener.offlineDevice(dev);
//				}
//			}
//		}

	}

	private LinkedHashSet<RoomHubDeviceListener> getDeviceListenerByCategory(int category) {
		synchronized (categoryDeviceListener) {
			return categoryDeviceListener.get(category);
		}
	}

	public GetCloudDevicesResPack GetDeviceUsersREQ(String uuid) {

		GetCloudDevicesResPack resPack = mCloudApi.GetDeviceUsersREQ(uuid);

		return resPack;
	}

	public BaseResPack AddDeviceUserREQ(String uuid, AddDeviceUserReqPack addDeviceUserReqPack){
		BaseResPack resPack = mCloudApi.AddDeviceUserREQ(uuid, addDeviceUserReqPack);
		return resPack;
	}

	public BaseResPack DeleteDeviceUserREQ(String uuid, DeviceUserReqPack deleteDeviceUserReqPack) {
		BaseResPack resPack = mCloudApi.DeleteDeviceUserREQ(uuid, deleteDeviceUserReqPack);
		return resPack;
	}

	/* Device----------------------------------------------------------------------------------- */



	private String md5(String s) {
	    try {
	        // Create MD5 Hash
	        MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
	        digest.update(s.getBytes());
	        byte messageDigest[] = digest.digest();

	        // Create Hex String
	        StringBuffer hexString = new StringBuffer();
	        for (int i=0; i<messageDigest.length; i++)
	            hexString.append(String.format("%02x", 0xFF & messageDigest[i]));
	        return hexString.toString();

	    } catch (NoSuchAlgorithmException e) {
	        e.printStackTrace();
	    }
	    return "";
	}

	/* IR -------------------------------------------------------------------------------------- */
	@Deprecated
	public IRCodeNumListResPack GetCodeNumByLearningSearch(String signature) {
		IRCodeNumListResPack resPack = mCloudApi.GetCodeNumByLearningSearch(signature);
		return resPack;
	}
	public IRCodeNumListResPack ACIrLearning(int deviceId, int s0, int s1, String s3) {
		IRCodeNumListResPack resPack = mCloudApi.ACIrLearning(deviceId, s0, s1, s3);
		return resPack;
	}
	public IRCodeNumListResPack AVIrLearning(int deviceId, int s0, int s1, String s3) {
		IRCodeNumListResPack resPack = mCloudApi.AVIrLearning(deviceId, s0, s1, s3);
		return resPack;
	}
	public IRBrandListResPack GetIRBrandList(int deviceId, String language) {
		IRBrandListResPack resPack = mCloudApi.GetIRBrandList(deviceId, language);
		return resPack;
	}
	public IRBrandListResPack GetAVBrandList(int deviceId, String language) {
		IRBrandListResPack resPack = mCloudApi.GetAVBrandList(deviceId, language);
		return resPack;
	}
	public IRModelResPack GetIRFirstModel(int deviceId, int brandId) {
		IRModelResPack resPack = mCloudApi.GetIRFirstModel(deviceId, brandId);
		return resPack;
	}
	public IRModelResPack GetAVFirstModel(int deviceId, int brandId) {
		IRModelResPack resPack = mCloudApi.GetAVFirstModel(deviceId, brandId);
		return resPack;
	}
	public IRModelListResPack GetIRModelList(int deviceId, int brandId) {
		IRModelListResPack resPack = mCloudApi.GetIRModelList(deviceId, brandId);
		return resPack;
	}
	public IRModelListResPack GetAVModelList(int deviceId, int brandId) {
		IRModelListResPack resPack = mCloudApi.GetAVModelList(deviceId, brandId);
		return resPack;
	}
	public IRACAutoScanResPack ACAutoScan(int deviceId) {
		IRACAutoScanResPack resPack = mCloudApi.ACAutoScan(deviceId);
		return resPack;
	}
	public IRACAutoScanResPack AVAutoScan(int deviceId) {
		IRACAutoScanResPack resPack = mCloudApi.AVAutoScan(deviceId);
		return resPack;
	}
	public IRACKeyDataResPack GetACKeyData(int codeNum) {
		IRACKeyDataResPack resPack = mCloudApi.GetACKeyData(codeNum);
		return resPack;
	}
	public IRACKeyDataResPack GetAVKeyData(int codeNum) {
		IRACKeyDataResPack resPack = mCloudApi.GetAVKeyData(codeNum);
		return resPack;
	}
	public IRBrandAndModelDataResPack GetBrandAndModelData(int deviceId,int codeNum, String language) {
		IRBrandAndModelDataResPack resPack = mCloudApi.GetBrandAndModelData(deviceId,codeNum, language);
		return resPack;
	}
	public IRModeResPack GetIRModes(int codeNum) {
		IRModeResPack resPack = mCloudApi.GetIRModes(codeNum);
		return resPack;
	}
	public IRCodeNumByKeywordResPack GetCodeNumByKeyword(int deviceId, String keyword) {
		IRCodeNumByKeywordResPack resPack = mCloudApi.GetCodeNumByKeyword(deviceId, keyword);
		return resPack;
	}
	public IRCodeNumByKeywordResPack GetAVCodeNumByKeyword(int deviceId, String keyword) {
		IRCodeNumByKeywordResPack resPack = mCloudApi.GetAVCodeNumByKeyword(deviceId, keyword);
		return resPack;
	}

	/* IR -------------------------------------------------------------------------------------- */

	// Sensor --------------------------------------------------------------------------------

	/**
	 *
	 * @param uuid
	 * @param sensorType SensorTypeKey.SENSOR_TEMPERATURE or SensorTypeKey.SENSOR_HUMIDITY
	 * @param queryDate For example: Oct 16 => "10-16"
	 * @return
	 */
	public SensorDataResPack GetSensorDailyData(String uuid, String sensorType, String queryDate) {
		SensorDataResPack resPack = mCloudApi.GetSensorDailyData(uuid, sensorType, queryDate);
		return resPack;
	}
	// Sensor --------------------------------------------------------------------------------

	/* Weather --------------------------------------------------------------------------------- */
	public CityListResPack GetCityList(String language) {
		CityListResPack resPack = mCloudApi.GetCityListREQ(language);
		return resPack;
	}
	public TownListResPack GetTownList(int cityId, String language) {
		TownListResPack resPack = mCloudApi.GetTownListREQ(cityId, language);
		return resPack;
	}
	public WeatherDataResPack GetWeatherData(String townId, String language) {
		WeatherDataResPack resPack = mCloudApi.GetWeatherDataREQ(townId, language);
		return resPack;
	}
	/* Weather --------------------------------------------------------------------------------- */

	/* RoomHub Version ------------------------------------------------------------------------- */
	public VersionCheckUpdateResPack CheckVersion(VersionCheckUpdateReqPack reqPack) {
		VersionCheckUpdateResPack resPack = mCloudApi.CheckVersion(reqPack);
		return resPack;
	}
	/* RoomHub Version ------------------------------------------------------------------------- */

	/* APP Latest Version ------------------------------------------------------------------------- */
	public VersionCheckUpdateResPack AppCheckVersion() {
		return mCloudApi.AppCheckVersion();
	}
	/* APP Latest Version ------------------------------------------------------------------------- */

	/* RoomHubDeviceListener ------------------------------------------------------------------- */
	public void registerRoomHubDeviceListener(int category, RoomHubDeviceListener deviceListener) {
		synchronized (categoryDeviceListener) {
			LinkedHashSet<RoomHubDeviceListener> deviceListenerLinkedHashSet =
					categoryDeviceListener.get(category);
			if(deviceListenerLinkedHashSet == null)
				deviceListenerLinkedHashSet = new LinkedHashSet<>();
			deviceListenerLinkedHashSet.add(deviceListener);
			categoryDeviceListener.put(category,deviceListenerLinkedHashSet);
		}
	}

	public void unregisterRoomHubDeviceListener(int category, RoomHubDeviceListener deviceListener) {
		synchronized (categoryDeviceListener) {
			LinkedHashSet<RoomHubDeviceListener> deviceListenerLinkedHashSet =
					categoryDeviceListener.get(category);
			if(deviceListenerLinkedHashSet != null)
				deviceListenerLinkedHashSet.remove(deviceListener);
		}
	}

	@Override
	public void addDevice(RoomHubDevice device,ReasonType reason) {
		for(int keyCategory: categoryDeviceListener.keySet()) {
			LinkedHashSet<RoomHubDeviceListener> listenerByCategory =
					getDeviceListenerByCategory(keyCategory);
			for(RoomHubDeviceListener listener:listenerByCategory) {
				listener.addDevice(device, reason);
			}
		}
	}

	@Override
	public void removeDevice(RoomHubDevice device,ReasonType reason) {
		for(int keyCategory: categoryDeviceListener.keySet()) {
			LinkedHashSet<RoomHubDeviceListener> listenerByCategory =
					getDeviceListenerByCategory(keyCategory);
			for(RoomHubDeviceListener listener:listenerByCategory) {
				listener.removeDevice(device, reason);
			}
		}
	}

	@Override
	public void updateDevice(RoomHubDevice device) {
		for(int keyCategory: categoryDeviceListener.keySet()) {
			LinkedHashSet<RoomHubDeviceListener> listenerByCategory =
					getDeviceListenerByCategory(keyCategory);
			for(RoomHubDeviceListener listener:listenerByCategory) {
				listener.updateDevice(device);
			}
		}
	}

	@Override
	public void switchNetwork(boolean connected) {
		for(int keyCategory: categoryDeviceListener.keySet()) {
			LinkedHashSet<RoomHubDeviceListener> listenerByCategory =
					getDeviceListenerByCategory(keyCategory);
			for(RoomHubDeviceListener listener:listenerByCategory) {
				listener.switchNetwork(connected);
			}
		}
	}

	/* RoomHubDeviceListener ------------------------------------------------------------------- */

	/* RoomHubSignalListener ------------------------------------------------------------------- */
	public void registerRoomHubSignalListener(int category, RoomHubSignalListener roomhubListener) {
		synchronized (rhListener) {
			rhListener.add(roomhubListener);
		}
	}

	public void unregisterRoomHubSignalListener(RoomHubSignalListener roomhubListener) {
		synchronized (rhListener) {
			rhListener.remove(roomhubListener);
		}
	}

	/* HomeApplianceSignlListener -------------------------------------------------------------- */
	public void registerHomeApplianceSignalListeners(int category, HomeApplianceSignalListener signalListener) {
		synchronized (homeApplianceSignalListeners) {
			homeApplianceSignalListeners.add(signalListener);
		}
	}

	public void unregisterHomeApplianceSignalListeners(HomeApplianceSignalListener signalListener) {
		synchronized (homeApplianceSignalListeners) {
			homeApplianceSignalListeners.remove(signalListener);
		}
	}

	/* UserFriendListener ------------------------------------------------------------------- */
	public void registerUserFrinedListener(UserFriendListener userfriendListener) {
		synchronized (ufListener) {
			ufListener.add(userfriendListener);
		}
	}

	public void unregisterUserFriendListener(UserFriendListener userfriendListener) {
		synchronized (ufListener) {
			ufListener.remove(userfriendListener);
		}
	}

	/* ShareUserListener ------------------------------------------------------------------- */
	public void registerShareUserListener(ShareUserListener shareUserListener) {
		synchronized (suListener) {
			suListener.add(shareUserListener);
		}
	}

	public void unregisterShareUserListener(ShareUserListener shareUserListener) {
		synchronized (suListener) {
			suListener.remove(shareUserListener);
		}
	}

	/* DeviceDefaultUserListener ------------------------------------------------------------------- */
	public void registerDeviceDefaultUserChangeListener(int category, DeviceDefaultUserListener deviceDefaultUserListener) {
		synchronized (dfuListener) {
			dfuListener.add(deviceDefaultUserListener);
		}
	}

	public void unregisterDeviceDefaultUserChangeListener(int category, DeviceDefaultUserListener deviceDefaultUserListener) {
		synchronized (dfuListener) {
			dfuListener.remove(deviceDefaultUserListener);
		}
	}

	@Override
	public void RoomHubDataUpdate(RoomHubDataResPack dataResPack, SourceType sourceType) {
		if(rhListener == null)
			return;

		for(RoomHubSignalListener listener: rhListener) {
			listener.RoomHubDataUpdate(dataResPack, sourceType);
		}
	}

	@Override
	public void RoomHubLearningResultUpdate(LearningResultResPack learningResultResPack) {
		if(rhListener == null)
			return;

		for(RoomHubSignalListener listener: rhListener) {
			listener.RoomHubLearningResultUpdate(learningResultResPack);
		}
	}

	@Override
	public void RoomHubDeviceInfoChangeUpdate(DeviceInfoChangeResPack deviceInfoChangeResPack, SourceType sourceType) {
		if(rhListener == null)
			return;

		for(RoomHubSignalListener listener: rhListener) {
			listener.RoomHubDeviceInfoChangeUpdate(deviceInfoChangeResPack, sourceType);
		}
	}

	@Override
	public void RoomHubNameChangeUpdate(NameChangeResPack nameResPack) {
		if(rhListener == null)
			return;

		for(RoomHubSignalListener listener: rhListener) {
			listener.RoomHubNameChangeUpdate(nameResPack);
		}
	}

	@Override
	public void RoomHubSyncTime() {
		if(rhListener == null)
			return;

		for(RoomHubSignalListener listener: rhListener) {
			listener.RoomHubSyncTime();
		}
	}

	@Override
	public void RoomHubAcOnOffStatusUpdate(AcOnOffStatusResPack resPack) {
		if(rhListener == null)
			return;

		for(RoomHubSignalListener listener: rhListener) {
			listener.RoomHubAcOnOffStatusUpdate(resPack);
		}
	}

	@Override
	public void RoomHubUpdateSchedule(UpdateScheduleResPack resPack) {
		if(rhListener == null)
			return;

		for(RoomHubSignalListener listener: rhListener) {
			listener.RoomHubUpdateSchedule(resPack);
		}
	}

	@Override
	public void RoomHubUpdateAllSchedule(String uuid,ArrayList<Schedule> schedule_lst) {
		if(rhListener == null)
			return;

		for(RoomHubSignalListener listener: rhListener) {
			listener.RoomHubUpdateAllSchedule(uuid, schedule_lst);
		}
	}

	@Override
	public void RoomHubDeleteSchedule(DeleteScheduleResPack resPack) {
		if(rhListener == null)
			return;

		for(RoomHubSignalListener listener: rhListener) {
			listener.RoomHubDeleteSchedule(resPack);
		}
	}

	@Override
	public void RoomHubNextSchedule(NextScheduleResPack resPack) {
		if(rhListener == null)
			return;

		for(RoomHubSignalListener listener: rhListener) {
			listener.RoomHubNextSchedule(resPack);
		}
	}

	@Override
	public void RoomHubOTAUpgradeStateChangeUpdate(DeviceFirmwareUpdateStateResPack resPack) {
		if(rhListener == null)
			return;

		for(RoomHubSignalListener listener: rhListener) {
			listener.RoomHubOTAUpgradeStateChangeUpdate(resPack);
		}
	}

	/* RoomHubSignalListener ------------------------------------------------------------------- */
	/* OTACloudStateUpdateListener ------------------------------------------------------------------ */
	public void registerOTAStateChange(OTACloudStateUpdateListener listener) {
		synchronized (otaStateChangeListener) {
			otaStateChangeListener.add(listener);
		}
	}

	public void unregisterOTAStateChange(OTACloudStateUpdateListener listener) {
		synchronized (otaStateChangeListener) {
			otaStateChangeListener.remove(listener);
		}
	}
	/* OTACloudStateUpdateListener ------------------------------------------------------------------ */

	@Override
	public void addAsset(AssetResPack assetResPack,SourceType sourceType) {
		if(homeApplianceSignalListeners == null)
			return;

		for(HomeApplianceSignalListener listener: homeApplianceSignalListeners) {
			listener.addAsset(assetResPack, sourceType);
		}
	}

	@Override
	public void removeAsset(AssetResPack assetResPack,SourceType sourceType) {
		if(homeApplianceSignalListeners == null)
			return;

		for(HomeApplianceSignalListener listener: homeApplianceSignalListeners) {
			listener.removeAsset(assetResPack, sourceType);
		}
	}

	@Override
	public void updateAsset(AssetResPack assetResPack,SourceType sourceType) {
		if(homeApplianceSignalListeners == null)
			return;

		for(HomeApplianceSignalListener listener: homeApplianceSignalListeners) {
			listener.updateAsset(assetResPack, sourceType);
		}
	}

	@Override
	public void AssetInfoChange(int assetType, Object assetDetailInfoResPack, SourceType sourceType) {
		if(homeApplianceSignalListeners == null)
			return;

		for(HomeApplianceSignalListener listener: homeApplianceSignalListeners) {
			listener.AssetInfoChange(assetType,assetDetailInfoResPack, sourceType);
		}
	}

	@Override
	public void FirmwareUpdateStateChange(FirmwareUpdateStateResPack firmwareUpdateStateResPack) {
		// pass state via previous listeners (otaStateChangeListener & rhListener
		if(otaStateChangeListener != null) {
			for(OTACloudStateUpdateListener otaCloudStateUpdateListener: otaStateChangeListener) {
				DeviceFirmwareUpdateStateResPack deviceFirmwareUpdateStateResPack = new DeviceFirmwareUpdateStateResPack();
				deviceFirmwareUpdateStateResPack.setUuid(firmwareUpdateStateResPack.getHubUUID());
				deviceFirmwareUpdateStateResPack.setState(firmwareUpdateStateResPack.getState());
				deviceFirmwareUpdateStateResPack.setStatus_code(0);
				otaCloudStateUpdateListener.stateChange(deviceFirmwareUpdateStateResPack);
			}
		}

		if(rhListener != null) {
			for (RoomHubSignalListener listener : rhListener) {
				DeviceFirmwareUpdateStateResPack deviceFirmwareUpdateStateResPack = new DeviceFirmwareUpdateStateResPack();
				deviceFirmwareUpdateStateResPack.setUuid(firmwareUpdateStateResPack.getHubUUID());
				deviceFirmwareUpdateStateResPack.setState(firmwareUpdateStateResPack.getState());
				deviceFirmwareUpdateStateResPack.setStatus_code(0);
				listener.RoomHubOTAUpgradeStateChangeUpdate(deviceFirmwareUpdateStateResPack);
			}
		}

		/*
		if(homeApplianceSignalListeners == null)
			return;

		for(HomeApplianceSignalListener listener: homeApplianceSignalListeners) {
			listener.FirmwareUpdateStateChange(firmwareUpdateStateResPack);
		}
		*/
	}

	@Override
	public void AcFailRecover(AcFailRecoverResPack failRecoverResPack, SourceType sourceType) {
		if(homeApplianceSignalListeners == null)
			return;

		for(HomeApplianceSignalListener listener: homeApplianceSignalListeners) {
			listener.AcFailRecover(failRecoverResPack, sourceType);
		}
	}

	@Override
	public void ScanAssetResult(ScanAssetResultResPack scanAssetResPack) {
		if(homeApplianceSignalListeners == null)
			return;

		for(HomeApplianceSignalListener listener: homeApplianceSignalListeners) {
			listener.ScanAssetResult(scanAssetResPack);
		}
	}

	@Override
	public void UpdateSchedule(SignalUpdateSchedulePack updateSchedulePack) {
		if(homeApplianceSignalListeners == null)
			return;

		for(HomeApplianceSignalListener listener: homeApplianceSignalListeners) {
			listener.UpdateSchedule(updateSchedulePack);
		}

	}

	@Override
	public void DeleteSchedule(SignalDeleteSchedulePack deleteSchedulePack) {
		if(homeApplianceSignalListeners == null)
			return;

		for(HomeApplianceSignalListener listener: homeApplianceSignalListeners) {
			listener.DeleteSchedule(deleteSchedulePack);
		}
	}

	@Override
	public void AssetProfileChange(AssetProfile profile) {
		if(homeApplianceSignalListeners == null)
			return;

		for(HomeApplianceSignalListener listener: homeApplianceSignalListeners) {
			listener.AssetProfileChange(profile);
		}
	}
}
