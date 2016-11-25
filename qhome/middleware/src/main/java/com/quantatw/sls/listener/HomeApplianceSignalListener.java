package com.quantatw.sls.listener;

import com.quantatw.sls.key.SourceType;
import com.quantatw.sls.pack.device.ScanAssetResultResPack;
import com.quantatw.sls.pack.homeAppliance.AcFailRecoverResPack;
import com.quantatw.sls.pack.homeAppliance.AssetProfile;
import com.quantatw.sls.pack.homeAppliance.FirmwareUpdateStateResPack;
import com.quantatw.sls.pack.homeAppliance.SignalDeleteSchedulePack;
import com.quantatw.sls.pack.homeAppliance.SignalUpdateSchedulePack;
import com.quantatw.sls.pack.homeAppliance.detail.AssetResPack;

/**
 * Created by erin on 2/1/16.
 */
public interface HomeApplianceSignalListener {
    void addAsset(AssetResPack assetResPack,SourceType sourceType);
    void removeAsset(AssetResPack assetResPack,SourceType sourceType);
    void updateAsset(AssetResPack assetResPack,SourceType sourceType);
    void AssetInfoChange(int assetType,Object assetDetailInfoResPack,SourceType sourceType);
    void FirmwareUpdateStateChange(FirmwareUpdateStateResPack firmwareUpdateStateResPack);
    void AcFailRecover(AcFailRecoverResPack failRecoverResPack,SourceType sourceType);
    void ScanAssetResult(ScanAssetResultResPack scanAssetResPack);
    void UpdateSchedule(SignalUpdateSchedulePack updateSchedulePack);
    void DeleteSchedule(SignalDeleteSchedulePack deleteSchedulePack);
    void AssetProfileChange(AssetProfile profile);
    /*
    void AcAssetInfoChange(AcAssetDetailInfoResPack acAssetDetailInfoResPack,SourceType sourceType);
    void FanAssetInfoChange(FanAssetDetailInfoResPack fanAssetDetailInfoResPack);
    void AirPurifierInfoChange(AirPurifierAssetDetailInfoResPack airPurfierAssetDetailInfoResPack);
    void PMAssetInfoChange(PMAssetDetailInfoResPack pmAssetDetailInfoResPack);
    void BloodPressureAssetInfoChange(BloodPressureAssetDetailInfoResPack bloodPressureAssetDetailInfoResPack,SourceType sourceType);
    void BulbAssetInfoChange(BulbAssetDetailInfoResPack bulbAssetDetailInfoResPack,SourceType sourceType);
    */
}
