package com.quantatw.roomhub.blepair;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.quantatw.myapplication.R;
import com.quantatw.roomhub.manager.asset.manager.RoomHubData;
import com.quantatw.roomhub.manager.asset.manager.RoomHubManager;
import com.quantatw.roomhub.manager.asset.manager.AssetInfoData;
import com.quantatw.roomhub.manager.asset.listener.AssetListener;
import com.quantatw.roomhub.ui.BLEPairingActivity;
import com.quantatw.roomhub.ui.RoomHubService;
import com.quantatw.roomhub.utils.AssetDef;
import com.quantatw.roomhub.utils.BLEPairDef;
import com.quantatw.sls.api.MiddlewareApi;
import com.quantatw.sls.device.ScanAsset;
import com.quantatw.sls.key.ErrorKey;
import com.quantatw.sls.key.SourceType;
import com.quantatw.sls.listener.HomeApplianceSignalListener;
import com.quantatw.sls.pack.device.ScanAssetReqPack;
import com.quantatw.sls.pack.device.ScanAssetResultResPack;
import com.quantatw.sls.pack.homeAppliance.AcFailRecoverResPack;
import com.quantatw.sls.pack.homeAppliance.AssetProfile;
import com.quantatw.sls.pack.homeAppliance.CommandResPack;
import com.quantatw.sls.pack.homeAppliance.FirmwareUpdateStateResPack;
import com.quantatw.sls.pack.homeAppliance.SignalDeleteSchedulePack;
import com.quantatw.sls.pack.homeAppliance.SignalUpdateSchedulePack;
import com.quantatw.sls.pack.homeAppliance.detail.AssetResPack;

import java.util.ArrayList;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;

/**
 * Created by cherry on 2016/5/18.
 */
public class BLEPairController extends Handler implements HomeApplianceSignalListener,AssetListener {
    private final String TAG=BLEPairController.class.getSimpleName();

    private boolean DEBUG = true;

    private Context mContext;
    private MiddlewareApi mApi;

    //private HashMap<String, BLEPairReqPack> mBLEPairData = new HashMap<>();
    private LinkedHashSet<BLEPairChangeListener> mListener = new LinkedHashSet<BLEPairChangeListener>();

    private BLEPairReqPack mBLEPairData;
    private ArrayList<RoomHubData> mRoomHubDataList=new ArrayList<RoomHubData>();
    private ArrayList<ScanAssetResult> mScanAssetList=new ArrayList<ScanAssetResult>();
    private HashMap<String,ScanAssetResult> mAddAssetList=new HashMap<String, ScanAssetResult>();;
    private boolean mScanTimeout=false;

    private final int MESSAGE_SCAN_ASSET        = 100;
    private final int MESSAGE_SCAN_ASSET_RESULT = 101;
    private final int MESSAGE_ADD_ASSET         = 102;
    private final int MESSAGE_SCAN_TIMEOUT      = 103;
    private final int MESSAGE_ADD_ASSET_TIMEOUT = 104;

    @Override
    public void handleMessage(Message msg) {
        log("message what=" + msg.what);
        switch (msg.what) {
            case MESSAGE_SCAN_ASSET:
                doScanAsset();
                break;
            case MESSAGE_SCAN_ASSET_RESULT:
                handleScanAssetResult((ScanAssetResultResPack)msg.obj);
                break;
            case MESSAGE_ADD_ASSET:
                handleAddAsset((AssetResPack)msg.obj);
                break;
            case MESSAGE_SCAN_TIMEOUT:
                log("scan asset timeout (60 secs)");
                mScanTimeout=true;
                mRoomHubDataList.clear();
                int ret=ErrorKey.Success;
                if(mScanAssetList.size() == 0)
                    ret=ErrorKey.BLE_PAIR_SCAN_TIME_OUT;

                ScanAssetResultListener(ret);
                break;
            case MESSAGE_ADD_ASSET_TIMEOUT:
                log("add asset timeout (60 secs)");
                AddAssetListener((ScanAssetResult)msg.obj,ErrorKey.BLE_PAIR_ADD_ASSET_FAILURE);
                break;
            default:
                super.handleMessage(msg);
                break;
        }
    }

    public BLEPairController(Looper looper, Context context, MiddlewareApi api) {
        super(looper);
        mContext = context;
        mApi = api;
    }

    public void init(BLEPairReqPack req_pack){
        mBLEPairData=req_pack;

        mApi.registerHomeApplianceSignalListeners(req_pack.getCatetory(), this);
        ((RoomHubService)mContext).getRoomHubManager().registerAssetListener(this);

        log("BLEPairController roomHubUuid=" + req_pack.getRoomHubUuid() + ",assetUuid=" + req_pack.getAssetUuid() + ",catetory=" + req_pack.getCatetory() +
                ",assetType=" + req_pack.getAssetType() + ",prefixName=" + req_pack.getPrefixName() + ",expire_time=" + req_pack.getExpireTime() +
                ",showRename=" + req_pack.IsShowRename() + ", assetName=" + req_pack.getAssetName());
        if(req_pack.IsUseDefault()) {
            Intent intent = new Intent(mContext, BLEPairingActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(BLEPairDef.BLE_STATUS, BLEPairDef.STATUS.START);
            mContext.startActivity(intent);
        }
    }

    public void finish(){
        log("finish");
        mBLEPairData=null;
        mAddAssetList.clear();
        mScanAssetList.clear();
        mApi.unregisterHomeApplianceSignalListeners(this);
        ((RoomHubService)mContext).getRoomHubManager().unRegisterAssetListener(this);
    }

    public void scanAsset() {
        sendEmptyMessage(MESSAGE_SCAN_ASSET);
    }

    private void doScanAsset(){
       // BLEPairReqPack data=mBLEPairData.get(assetUuid);
        mScanAssetList.clear();
        mScanTimeout=false;

        if(mBLEPairData != null){
            RoomHubManager roomhub_mgr=((RoomHubService) mContext).getRoomHubManager();
            String roomhub_uuid=mBLEPairData.getRoomHubUuid();
            RoomHubData roomhub_data;
            int retval;

            if(roomhub_uuid == null){
                //ArrayList<RoomHubData> roomhub_data_list=roomhub_mgr.getRoomHubDataList(false);
                mRoomHubDataList=roomhub_mgr.getRoomHubDataList(false);
                if(mRoomHubDataList != null){
                    for (Iterator<RoomHubData> it = mRoomHubDataList.iterator(); it.hasNext(); ) {
                        roomhub_data = it.next();

                        retval=ScanAssetCommand(roomhub_data);
                        if(retval < ErrorKey.Success) {
                            it.remove();
                        }
                        log("doScanAsset roomHubUuid="+roomhub_data.getUuid()+",retval="+retval);
                    }
                }
            }else {
                roomhub_data = roomhub_mgr.getRoomHubDataByUuid(roomhub_uuid);
                if (roomhub_data != null) {
                    retval= ScanAssetCommand(roomhub_data);
                    if(retval == ErrorKey.Success) {
                        mRoomHubDataList.add(roomhub_data);
                    }
                    log("doScanAsset roomHubUuid="+roomhub_data.getUuid()+",retval="+retval);
                }
            }
            if(mRoomHubDataList.size() == 0){
                ScanAssetResultListener(ErrorKey.BLE_PAIR_SCAN_NO_DATA);
            }else {
                log("doScanAsset mRoomHubCnt=" + mRoomHubDataList.size());
                int timeout = (int) (mContext.getResources().getInteger(R.integer.config_ble_pair_expire_time) * 2 * 1000);
                sendEmptyMessageDelayed(MESSAGE_SCAN_TIMEOUT, timeout);
            }
        }
    }

    private int ScanAssetCommand(RoomHubData roomhub_data){
        if(mBLEPairData != null) {
            ScanAssetReqPack req_pack = new ScanAssetReqPack();
            req_pack.setAssetType(mBLEPairData.getAssetType());
            req_pack.setConnectionType(AssetDef.CONNECTION_TYPE_BT);
            req_pack.setPrefixName(mBLEPairData.getPrefixName());
            req_pack.setExpireTime(mBLEPairData.getExpireTime());

            CommandResPack res_pack = roomhub_data.getRoomHubDevice().ScanAsset(req_pack);
            return res_pack.getStatus_code();
        }
        return ErrorKey.Success;
    }

    private void handleScanAssetResult(ScanAssetResultResPack scanAssetResPack){
        if(mScanTimeout) return;
        if(scanAssetResPack == null) return;

        String uuid=scanAssetResPack.getUuid();
        RoomHubData roomhub_data=IsRoomHubData(uuid);
        if(roomhub_data == null) return;

        if(scanAssetResPack.getStatus_code() == ErrorKey.Success){
            log("handleScanAssetResult roomhub_uuid=" + uuid + " ret="+scanAssetResPack.getStatus_code());
            ArrayList<ScanAsset> scan_asset_list=scanAssetResPack.getScanAssetList();
            if(scan_asset_list != null) {
                log("handleScanAssetResult scan asset size=" + scan_asset_list.size());
                for (Iterator<ScanAsset> it = scan_asset_list.iterator(); it.hasNext(); ) {
                    ScanAsset new_asset = it.next();

                    log("handleScanAssetResult asset_uuid=" + new_asset.getUuid() + " device_name="+new_asset.getDeviceName());
                    addToScanList(uuid, new_asset);
                }
            }
        }
        mRoomHubDataList.remove(roomhub_data);

        log("handleScanAssetResult mRoomHubCnt=" + mRoomHubDataList.size());
        if(mRoomHubDataList.size() == 0){
            removeMessages(MESSAGE_SCAN_TIMEOUT);
            ScanAssetResultListener(ErrorKey.Success);
        }
    }

    private RoomHubData IsRoomHubData(String uuid){
        if (mRoomHubDataList != null){
            for (Iterator<RoomHubData> it = mRoomHubDataList.iterator(); it.hasNext(); ) {
                RoomHubData roomhub_data = it.next();
                if(uuid.equalsIgnoreCase(roomhub_data.getUuid())){
                    return roomhub_data;
                }
            }
        }
        return null;
    }

    private void handleAddAsset(AssetResPack assetResPack){
        int ret=assetResPack.getStatus_code();
        String asset_uuid=assetResPack.getAssetUuid();
        log("handleAddAsset asset_uuid="+asset_uuid+" asset_type="+assetResPack.getAssetType()+" ret=" + ret);
        if(ret == ErrorKey.Success){
            ScanAssetResult add_asset=mAddAssetList.get(asset_uuid);

            ret=AddDeviceStep(BLEPairDef.ADD_STEP.SET_ASSET_INFO,add_asset);
            if(ret == ErrorKey.Success) {
                AddDeviceStep(BLEPairDef.ADD_STEP.SET_NAME,add_asset);
                ret = AddDeviceStep(BLEPairDef.ADD_STEP.REG_TO_CLOUD, add_asset);
                log("handleAddAsset REG_TO_CLOUD ret="+ret);
                if(ret == ErrorKey.Success){
                    ret = AddDeviceStep(BLEPairDef.ADD_STEP.SET_DEFAULT_USER,add_asset);
                    log("handleAddAsset SET_DEFAULT_USER ret="+ret);
                }
            }
        }
        log("handleAddAsset ret="+ret);
        removeMessages(MESSAGE_ADD_ASSET_TIMEOUT);
        AddAssetListener(mAddAssetList.get(asset_uuid),ret);
    }

    private void addToScanList(String roomhubUuid,ScanAsset new_asset){
        boolean add=false;

        if(mScanAssetList.size() == 0){
            add=true;
        }else{
            boolean is_exist=false;
            for (Iterator<ScanAssetResult> it = mScanAssetList.iterator(); it.hasNext(); ) {
                ScanAssetResult old_asset = it.next();

                if (old_asset.getScanAsset().getUuid().equalsIgnoreCase(new_asset.getUuid())) {
                    is_exist=true;
                    break;
                }
            }
            if(!is_exist)
                add=true;
        }

        if(add) {
            ScanAssetResult scan_asset_result=new ScanAssetResult();
            scan_asset_result.setRoomHubUuid(roomhubUuid);
            scan_asset_result.setScanAsset(new_asset);

            setBrandAndModel(scan_asset_result);

            log("addToScanList roomhubUuid=" + roomhubUuid+" asset_uuid="+new_asset.getUuid()+" assetType="+new_asset.getAssetType()
                    +" deviceName="+new_asset.getDeviceName() +" brandName="+new_asset.getBrand()+" device="+new_asset.getDevice());
            mScanAssetList.add(scan_asset_result);
        }
    }

    private void setBrandAndModel(ScanAssetResult scan_asset_result){
        ScanAsset new_asset=scan_asset_result.getScanAsset();

        String device_name=new_asset.getDeviceName();
        String[] name=device_name.split("-");
        if(name.length == 0)
            name=device_name.split(" ");

        if(name.length >= 2 ){
            new_asset.setBrand(name[0]);
            new_asset.setDevice(name[1]);
        }else{
            new_asset.setBrand(device_name);
            new_asset.setDevice(device_name);
        }
    }

    private void ScanAssetResultListener(int ret){
        if (mListener != null) {
            synchronized (mListener) {
                for (Iterator<BLEPairChangeListener> it = mListener.iterator(); it.hasNext(); ) {
                    BLEPairChangeListener listener = it.next();
                    listener.onScanAssetResult(mScanAssetList, ret);
                }
            }
        }
    }

    private void AddAssetListener(ScanAssetResult add_asset,int ret){
        if (mListener != null) {
            synchronized (mListener) {
                for (Iterator<BLEPairChangeListener> it = mListener.iterator(); it.hasNext(); ) {
                    BLEPairChangeListener listener = it.next();
                    listener.onAddResult(add_asset, ret);
                }
            }
        }
        if(ret < ErrorKey.Success)
            mAddAssetList.remove(add_asset.getScanAsset().getUuid());

    }

    private int AddDeviceStep(BLEPairDef.ADD_STEP add_step,ScanAssetResult scanAsset){
        //BLEPairReqPack data=mBLEPairData.get(scanAsset.getUuid());
        if(mBLEPairData == null) return ErrorKey.BLE_PAIR_FAILURE;

        int ret=ErrorKey.BLE_PAIR_FAILURE;
        log("AddBLEDevice add_step=" + add_step);

        switch (add_step){
            case ADD_ASSET:
                mAddAssetList.put(scanAsset.getScanAsset().getUuid(),scanAsset);
                ret= mBLEPairData.getCallback().onAdd(BLEPairDef.ADD_STEP.ADD_ASSET, scanAsset);
                break;
            case SET_ASSET_INFO:
                ret=mBLEPairData.getCallback().onAdd(BLEPairDef.ADD_STEP.SET_ASSET_INFO,scanAsset);
                if(ret != ErrorKey.Success)
                    RemoveBLEDevice(scanAsset);
                break;
            case SET_NAME:
                ret=mBLEPairData.getCallback().onAdd(BLEPairDef.ADD_STEP.SET_NAME,scanAsset);
                break;
            case REG_TO_CLOUD:
                ret=mBLEPairData.getCallback().onAdd(BLEPairDef.ADD_STEP.REG_TO_CLOUD,scanAsset);
                break;
            case SET_DEFAULT_USER:
                ret=mBLEPairData.getCallback().onAdd(BLEPairDef.ADD_STEP.SET_DEFAULT_USER,scanAsset);
                break;
        }

        log("AddBLEDevice asset_uuid=" + scanAsset.getScanAsset().getUuid() + " ret="+ret);
        return ret;
    }

    public BLEPairReqPack getBLEPairData(){
        return mBLEPairData;
    }

    public ArrayList<ScanAssetResult> getAddAsset(){
        ArrayList<ScanAssetResult> scan_asset_list=new ArrayList<ScanAssetResult>(mAddAssetList.values());

        return scan_asset_list;
    }

    public void registerBLEPairChange(BLEPairChangeListener listener) {
        synchronized (mListener) {
            mListener.add(listener);
        }
    }

    public void unRegisterBLEPairChange(BLEPairChangeListener listener) {
        synchronized(mListener) {
            mListener.remove(listener);
        }
    }

    public int AddBLEDevice(ScanAssetResult scanAsset){
        int ret;
        ret=AddDeviceStep(BLEPairDef.ADD_STEP.ADD_ASSET, scanAsset);

        int timeout= (int) (mContext.getResources().getInteger(R.integer.config_ble_pair_expire_time) * 2 * 1000);
        //sendEmptyMessageDelayed(MESSAGE_ADD_ASSET_TIMEOUT, timeout);
        sendMessageDelayed(obtainMessage(MESSAGE_ADD_ASSET_TIMEOUT,scanAsset),timeout);
        return ret;
    }

    public void RemoveBLEDevice(ScanAssetResult scanAsset){
        if(mBLEPairData != null){
            if(scanAsset != null) {
                log("RemoveBLEDevice roomhub_uuid=" + scanAsset.getRoomHubUuid() + " asset_uuid=" + scanAsset.getScanAsset().getUuid());
                mBLEPairData.getCallback().onRemove(scanAsset.getRoomHubUuid(),scanAsset.getScanAsset().getUuid(), scanAsset.getScanAsset().getAssetType());
                mAddAssetList.remove(scanAsset.getScanAsset().getUuid());
            }
        }
    }

    public int Rename(String uuid,String new_name){
        if(mBLEPairData != null){
            return mBLEPairData.getCallback().onRename(uuid,new_name);
        }

        return ErrorKey.BLE_PAIR_RENAME_FAILURE;
    }

    @Override
    public void addAsset(AssetResPack assetResPack, SourceType sourceType) {
        if(assetResPack == null) return;
        String asset_uuid=assetResPack.getAssetUuid();
        if(asset_uuid == null) return;
        log("addAsset res_pack asset_uuid="+asset_uuid+" asset_type="+assetResPack.getAssetType()+" source_type="+sourceType);
        if(sourceType == SourceType.CLOUD)
            return;
        ScanAssetResult add_asset=mAddAssetList.get(asset_uuid);

        log("addAsset res_pack asset_uuid="+add_asset.getScanAsset().getUuid()+" asset_type="+add_asset.getScanAsset().getAssetType());
        if(add_asset != null) {
            log("addAsset MESSAGE_ADD_ASSET");
            sendMessage(obtainMessage(MESSAGE_ADD_ASSET, assetResPack));
        }
    }

    @Override
    public void removeAsset(AssetResPack assetResPack, SourceType sourceType) {

    }

    @Override
    public void updateAsset(AssetResPack assetResPack, SourceType sourceType) {

    }

    @Override
    public void AssetInfoChange(int assetType, Object assetDetailInfoResPack, SourceType sourceType) {

    }

    @Override
    public void FirmwareUpdateStateChange(FirmwareUpdateStateResPack firmwareUpdateStateResPack) {

    }

    @Override
    public void AcFailRecover(AcFailRecoverResPack failRecoverResPack, SourceType sourceType) {

    }

    @Override
    public void ScanAssetResult(ScanAssetResultResPack scanAssetResPack) {
        sendMessage(obtainMessage(MESSAGE_SCAN_ASSET_RESULT, scanAssetResPack));
    }

    @Override
    public void UpdateSchedule(SignalUpdateSchedulePack updateSchedulePack) {

    }

    @Override
    public void DeleteSchedule(SignalDeleteSchedulePack deleteSchedulePack) {

    }

    @Override
    public void AssetProfileChange(AssetProfile profile) {

    }

    public void log(String msg) {
        if(DEBUG)
            Log.d(TAG,msg);
    }

    @Override
    public void addAssetDevice(AssetInfoData asset_info_data, RoomHubData data, int result) {

    }

    @Override
    public void removeAssetDevice(AssetInfoData asset_info_data, RoomHubData data) {

    }

    @Override
    public void updateAssetDevice(AssetInfoData asset_info_data, RoomHubData data) {

    }

    @Override
    public void onAssetResult(String uuid, String asset_uuid,int result) {
        log("onAssetResult uuid=" + uuid + " asset_uuid=" + asset_uuid + " result=" + result);
        if((mAddAssetList.size() > 0) && (result < ErrorKey.Success)){
            ScanAssetResult add_asset=mAddAssetList.get(asset_uuid);
            if(add_asset != null){
                AddAssetListener(add_asset,result);
            }
        }
    }
}
