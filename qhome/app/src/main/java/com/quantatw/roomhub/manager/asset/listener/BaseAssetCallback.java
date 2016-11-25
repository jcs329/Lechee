package com.quantatw.roomhub.manager.asset.listener;

import android.os.Bundle;

import com.quantatw.roomhub.manager.asset.manager.RoomHubData;
import com.quantatw.roomhub.manager.asset.manager.AssetInfoData;
import com.quantatw.sls.key.SourceType;
import com.quantatw.sls.pack.roomhub.LearningResultResPack;

/**
 * Created by cherry on 2016/6/20.
 */
public interface BaseAssetCallback {
    void BaseAsset_AddDevice(AssetInfoData asset_info_data, RoomHubData room_hub);
    void BaseAsset_RemoveDevice(AssetInfoData asset_info_data,RoomHubData room_hub);
    void BaseAsset_UpdateDevice(AssetInfoData asset_info_data,RoomHubData room_hub);
    void BaseAsset_UpdateRoomHubData(int type,RoomHubData data);
    int BaseAsset_SendCommand(final Bundle bundle);
    void BaseAsset_CommandTimeOut(long thread_id,int error_code);
    void BaseAsset_UpgradeStats(String uuid,boolean is_upgrade);
    void BaseAsset_AssetInfoChange(Object ResPack,SourceType sourceType);
    void BaseAsset_GetAssetInfo(Object data);
    void BaseAsset_GetAbilityLimit(Object data);
    void BaseAsset_LearningResult(LearningResultResPack learningResultResPack);
    void BaseAsset_WakeUp();
}
