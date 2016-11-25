package com.quantatw.roomhub.manager.asset.listener;

import com.quantatw.roomhub.manager.asset.manager.RoomHubData;
import com.quantatw.roomhub.manager.asset.manager.AssetInfoData;

/**
 * Created by cherry on 2016/6/20.
 */
public interface AssetListener {
    public void addAssetDevice(AssetInfoData asset_info_data, RoomHubData data, int result);
    public void removeAssetDevice(AssetInfoData asset_info_data, RoomHubData data);
    public void updateAssetDevice(AssetInfoData asset_info_data, RoomHubData data);
    public void onAssetResult(String uuid,String asset_uuid, int result);
}
