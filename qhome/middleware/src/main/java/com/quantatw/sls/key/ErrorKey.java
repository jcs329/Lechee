package com.quantatw.sls.key;

public class ErrorKey {
	// H60 Status Code List (http://h60service.cloudapp.net/V1)
	public static int Success 					= 0;

	public static int UserAccountFormatError 	= -1;
	public static int UserPWFormatError 		= -2;
	public static int UserPWError 				= -3;
	public static int FBUserIDFormatError 		= -4;
	public static int ConnectionIdFormatError 	= -5;
	public static int SaveError 				= -6;
	public static int FBAccessTokenFormatError 	= -7;
	public static int UserNameFormatError 		= -8;
	public static int SociaMediaNotSupport 		= -9;
	public static int GetNameInfoError			= -10;
	public static int NoStatusInfo	 			= -11;
	public static int NoDeviceInfo 				= -12;
	public static int NotDeviceSettingInfo		= -13;
	public static int User_IDFormatError 		= -14;
	public static int UUIDFormatError 			= -15;
	public static int StatusFormatError 		= -16;
	public static int AccountNotFound 			= -17;
	public static int AccountExist		 		= -18;
	public static int NoMinValue				= -19;
	public static int NoMaxValue				= -20;
	public static int NoNotifyInfo				= -21;
	public static int NoGroupName				= -22;
	public static int NoGroupId					= -23;
	public static int TokenInvalid				= -24;
	public static int TokenUsed					= -25;
	public static int TokenExpired				= -26;
	public static int NoXmppVerifyCode			= -27;
	public static int NoPermission				= -28;
	public static int EmailFormatError			= -29;
	public static int EmailExist				= -30;
	public static int DeviceHasOwnerAlready 	= -31;
	public static int EmailNotAuthorized 		= -32;
	public static int PageBetween0To50 			= -33;
	public static int FailToGetStartTime		= -34;
	public static int FailToGetIntervalTime		= -35;
	public static int FriendAccountExist		= -36;
	public static int FriendAccountNotExist		= -37;
	public static int AccountEmailAuthorized	= -38;

	public static int BroadcastUpdateError		= -72;

	public static int JsonError					= -998;
	public static int UnknowError 				= -999;
	public static int ConnectionError 			= -1000;

	// RoomHub Error Code defined in Android ( error code : 8000 ~ 9000)
	/* System error code：8000 ~ 8049 */
	public static int MQTT_CONNECT_ERROR		= -8000;
	public static int CLOUDAPI_NOT_ALLOWED		= -8001;
	public static int ONBOARDING_FAILURE		= -8002;
	public static int FUNCTION_NOT_SUPPORTED	= -8003;
	public static int START_UPGRADE_FAILURE		= -8004;
	public static int OTA_UPDATE_FAILURE		= -8005;
	public static int GET_WIFI_BRIDGE_FAILURE	= -8006;
	public static int AUTO_WIFI_BRIDGE_FAILURE	= -8007;

	/* IR error code ：8050 ~ 8059 */
	public static int IR_LEARNING_FAILURE			= -8050;
	public static int IR_CHECK_DATA_FAILURE			= -8051;
	public static int IR_CLEAR_CONTROL_DATA_FAILURE	= -8052;
	public static int IR_ADD_CONTROL_DATA_FAILURE	= -8053;
	public static int IR_PAIR_FAILURE				= -8054;

	/* BLE Pair error code ：8060 ~ 8069 */
	public static int BLE_PAIR_FAILURE				= -8060;
	public static int BLE_PAIR_SCAN_TIME_OUT		= -8061;
	public static int BLE_PAIR_SCAN_NO_DATA			= -8062;
	public static int BLE_PAIR_ADD_ASSET_FAILURE	= -8063;
	public static int BLE_PAIR_RENAME_FAILURE		= -8064;

	/* Room Hub error code：8070 ~ 8089 */
	public static int ROOMHUB_DATA_NOT_FOUND		= -8070;
	public static int ROOMHUB_LED_CONTROL_FAILURE	= -8071;
	public static int ROOMHUB_RENAME_FAILURE		= -8072;
	public static int ROOMHUB_REBOOT_FAILURE		= -8073;
	public static int SENSOR_TEMPERATURE_INVALID	= -8074;
	public static int SENSOR_HUMIDITY_INVALID		= -8075;
	public static int FAIL_RECOVER_NOT_SET			= -8076;
	public static int SCAN_ASSET_FAILURE			= -8077;
	public static int SCAN_ASSET_NOT_SUPPORTED		= -8078;

	/* Asset error code：8090 ~ 8109 */
	public static int ASSET_INFO_NOT_SET			= -8090;
	public static int ADD_APPLIANCES_FAILURE		= -8091;
	public static int APPLIANCES_ALREADY_EXISTS		= -8092;
	public static int DELETE_APPLIANCES_FAILURE		= -8093;
	public static int SCAN_APPLIANCES_FAILURE		= -8094;
	public static int ASSET_PROFILE_NOT_SET			= -8095;
	public static int ASSET_PROFILE_INVALID			= -8096;
	public static int ASSET_REGISTER_TYPE_ERROR		= -8097;
	public static int ASSET_REGISTER_TYPE_DUPLICATE	= -8098;

	/* AC error code：8110 ~ 8129 */
	public static int AC_DATA_NOT_FOUND					= -8110;
	public static int AC_ASSET_INFO_INVALID				= -8111;
	public static int AC_ABILITY_INVALID				= -8112;
	public static int AC_COMMAND_FAILURE				= -8113;
	public static int AC_COMMAND_TIMEOUT				= -8114;
	public static int AC_ADD_SCHEDULE_FAILURE			= -8115;
	public static int AC_MODIFY_SCHEDULE_FAILURE		= -8116;
	public static int AC_REMOVE_SCHEDULE_FAILURE		= -8117;
	public static int AC_REMOVE_ALL_SCHEDULE_FAILURE	= -8118;

	/* FAN error code：8130 ~ 8149 */
	public static int FAN_DATA_NOT_FOUND				= -8130;
	public static int FAN_ASSET_INFO_INVALID			= -8131;
	public static int FAN_ABILITY_INVALID				= -8132;
	public static int FAN_COMMAND_FAILURE				= -8133;
	public static int FAN_COMMAND_TIMEOUT				= -8134;

	/* Air purifier error code：8150 ~ 8169 */
	public static int AP_DATA_NOT_FOUND					= -8150;
	public static int AP_ASSET_INFO_INVAILD				= -8151;
	public static int AP_ABILITY_INVALID				= -8152;
	public static int AP_COMMAND_FAILURE				= -8153;
	public static int AP_COMMAND_TIMEOUT				= -8154;

	/* Bulb error coe : 8170 ~ 8179 */
	public static int BULB_DATA_NOT_FOUND 				= -8170;
	public static int BULB_ASSET_INFO_INVAILD 			= -8171;
	public static int BULB_COMMAND_FAILURE 				= -8172;
	public static int BULB_COMMAND_TIMEOUT 				= -8173;
	public static int BULB_ADD_SCHEDULE_FAILURE			= -8174;
	public static int BULB_MODIFY_SCHEDULE_FAILURE		= -8175;
	public static int BULB_REMOVE_SCHEDULE_FAILURE		= -8176;
	public static int BULB_REMOVE_ALL_SCHEDULE_FAILURE	= -8177;
	public static int BULB_GET_ALL_SCHEDULE_FAILURE		= -8178;

	/* PM2.5 error coe : 8180 ~ 8189 */
	public static int PM_ASSET_INFO_INVAILD				= -8180;
	public static int PM_RELOAD_TIMEOUT					= -8181;
	public static int PM_COMMAND_FAILURE					= -8182;

	/* TV error code：8190 ~ 8209 */
	public static int TV_DATA_NOT_FOUND					= -8190;
	public static int TV_ASSET_INFO_INVALID				= -8191;
	public static int TV_ABILITY_INVALID				= -8192;
	public static int TV_COMMAND_FAILURE				= -8193;
	public static int TV_COMMAND_TIMEOUT				= -8194;

	/* Healthcare device manager error code: 8500 ~ 8600 */
	public static int HEALTHCARE_NOT_SUPPORT			= -8500;
	public static int HEALTHCARE_REGISTER_TYPE_ERROR	= -8501;
	public static int HEALTHCARE_REGISTER_TYPE_DUPLICATE	= -8502;
	public static int HEALTHCARE_REG_DEVICE_FAIL		= -8503;
	public static int HEALTHCARE_PARSE_HISTORY_FAIL		= -8504;
}
