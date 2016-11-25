package com.quantatw.roomhub.blepair;

import com.quantatw.roomhub.utils.BLEPairDef;

import java.util.ArrayList;

/**
 * Created by cherry on 2016/05/20.
 */
public interface BLEPairChangeListener {
    public void onScanAssetResult(ArrayList<ScanAssetResult> scan_asset_list,int result);
    public void onAddResult(ScanAssetResult scan_asset,int result);
}
