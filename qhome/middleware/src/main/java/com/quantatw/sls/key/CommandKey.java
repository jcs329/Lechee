package com.quantatw.sls.key;



public enum CommandKey {

	//Sign In
	UserRegister,
	UserLogin,
	FBUserLogin,

	ForgetPassword,
	ChangePassword,
	
	//Command
	AddDevice,
	DeleteDevice,
	AddCloudDevice,
	DeleteCloudDevice,
	GetDevices,
	GetAllDevices,
	
	//Signal
	AddDeviceNotice,
	RemoveDeviceNotice,
	AddCloudDeviceNotice,
	RemoveCloudDeviceNotice,
	UpdateDeviceNotice,
	UpdateCloudDeviceNotice,
	MotionDetectedNotice,
	SnapshotPicNotice,
	VideoUploadResultNotice,
	VideoUploadCloudResultNotice,
	CurrentRecordRTSPNotice,
	
	ControlDeviceNotice,
	DoorLockNotice,
	DoorSensorNotice,
	ZigbeeConnectNotice,
	
	ScheduleEventNotice,
	//Response
	CamList,
	ConnectToCam,
	CamSSIDList,
	SetCamConnectToSSID,
	
	
	//Command Request
	
	//IPCam
	GetBulbCamNameReq,
	SetBulbCamNameReq,
	GetBulbCamStreamingStatusReq,
	SetBulbCamStreamingStatusReq,

	
	GetBulbCamResolutionTypeReq,
	SetBulbCamResolutionTypeReq,
	GetBulbCamZoomTypeReq,
	SetBulbCamZoomTypeReq,
	GetBulbCamMotionDetectionStatusReq,
	SetBulbCamMotionDetectionStatusReq,
	GetBulbCamMotionDetectionControlReq,
	SetBulbCamMotionDetectionControlReq,
	GetBulbCamMotionDetectionTimeReq,
	SetBulbCamMotionDetectionTimeReq,
	GetBulbCamMotionZoneReq,
	SetBulbCamMotionZoneReq,
	GetBulbCamEncryptionTypeReq,
	SetBulbCamEncryptionTypeReq,
	GetBulbCamWifiModeReq,
	GetBulbCamCamCurrentTimeReq,
	GetBulbCamUuidReq,
	SetBulbCamTakeSnapshotOnOffReq,
	GetBulbCamVideoResolutionURLReq,
	

	
	//Command Response
	//IPCam
	GetBulbCamNameRes,
	SetBulbCamNameRes,
	SetBulbFirstCamNameRes,
	GetBulbCamStreamingStatusRes,
	SetBulbCamStreamingStatusRes,

	GetBulbCamResolutionTypeRes,
	SetBulbCamResolutionTypeRes,
	GetBulbCamZoomTypeRes,
	SetBulbCamZoomTypeRes,
	GetBulbCamMotionDetectionStatusRes,
	SetBulbCamMotionDetectionStatusRes,
	GetBulbCamMotionDetectionControlRes,
	SetBulbCamMotionDetectionControlRes,
	GetBulbCamMotionDetectionTimeRes,
	SetBulbCamMotionDetectionTimeRes,
	GetBulbCamMotionZoneRes,
	SetBulbCamMotionZoneRes,
	GetBulbCamEncryptionTypeRes,
	SetBulbCamEncryptionTypeRes,
	GetBulbCamWifiModeRes,
	GetBulbCamCamCurrentTimeRes,
	GetBulbCamUuidRes,
	SetBulbCamTakeSnapshotOnOffRes,
	GetBulbCamVideoResolutionURLRes,

	GetBulbCamTimeStampRes,
	GetBulbCamTakeRecordingRes,
	GetBulbCamControlRes,
	GetBulbCamBirateRes,
	GetBulbCamAudioMuteRes,
	GetBulbCamScheduleRes,
	GetBulbCamRecordListRes,
	GetBulbCamPlayRecordURLRes,
	SetBulbCamTakeRecordingRes,
	SetBulbCamControlRes,
	SetBulbCamBirateRes,
	SetBulbCamAudioMuteRes,
	SetBulbCamScheduleRes,
	SetBulbCamRecordListRes,
	SetBulbCamPlayRecordURLRes,
	SetBulbScheduleSwitchRes,
	SetBulbCammotionZoneAllAreaRes,
	SetBulbCamTimeStampRes,
	SetRotateImageRes,
	GetBulbCamMotionZoneSwitchRes,
	GetBulbCamRotateStatusRes,
	GetBulbCamScheduleSwitchStatusRes,
	GetBulbCamSetTimeRes,
	SetBulbCamSetTimeRes,
	//Search 
	QueryMotiobRecordDaysReq,
	QueryMotionRecordDaysRes,
	QueryMotiobRecordLogReq,
	QueryMotionRecordLogRes,
	QueryMotiobRecordReq,
	QueryMotionRecordRes, SetNotificationTokenRes
}
