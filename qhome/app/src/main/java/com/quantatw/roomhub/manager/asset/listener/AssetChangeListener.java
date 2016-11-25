package com.quantatw.roomhub.manager.asset.listener;

/**
 * Created by 95010915 on 2016/2/1.
 */
public interface AssetChangeListener {
    public void addDevice(int asset_type,Object data);
    public void removeDevice(int asset_type,Object data);
    public void UpdateAssetData(int asset_type,Object data);
    public void UpdatePageStatus(int asset_type,boolean enabled, Object data);
    public void onCommandResult(int asset_type,String uuid, int result);

}
