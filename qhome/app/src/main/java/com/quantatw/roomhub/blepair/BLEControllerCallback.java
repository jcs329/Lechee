package com.quantatw.roomhub.blepair;

import com.quantatw.roomhub.utils.BLEPairDef;

/**
 * Created by erin on 10/27/15.
 */
public interface BLEControllerCallback {
    public int onAdd(BLEPairDef.ADD_STEP add_step,ScanAssetResult scaAsset);
    public int onRename(String uuid,String new_name);
    public int onRemove(String roomhub_uuid,String asset_uuid,int asset_type);
}
