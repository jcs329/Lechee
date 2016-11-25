package com.quantatw.roomhub.manager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.quantatw.myapplication.R;
import com.quantatw.roomhub.ir.ApIRBrandData;
import com.quantatw.roomhub.ir.ApIRModelData;
import com.quantatw.roomhub.ir.ApIRParingInfo;
import com.quantatw.roomhub.listener.IRControllerCallback;
import com.quantatw.roomhub.listener.IRLearningResultCallback;
import com.quantatw.roomhub.listener.IRParingActionCallback;
import com.quantatw.roomhub.listener.IRParingStateChangedListener;
import com.quantatw.roomhub.ui.IRSettingDataValues;
import com.quantatw.roomhub.ui.RoomHubMainPage;
import com.quantatw.roomhub.utils.ACDef;
import com.quantatw.roomhub.utils.AssetDef;
import com.quantatw.roomhub.utils.IRACDef;
import com.quantatw.roomhub.utils.IRAVDef;
import com.quantatw.roomhub.utils.Utils;
import com.quantatw.sls.api.DeviceTypeConvertApi;
import com.quantatw.sls.api.MiddlewareApi;
import com.quantatw.sls.key.ErrorKey;
import com.quantatw.sls.key.IrDeviceType;
import com.quantatw.sls.key.LanguageType;
import com.quantatw.sls.object.IRACData;
import com.quantatw.sls.object.IRACKeyData;
import com.quantatw.sls.object.IRBrandAndModelData;
import com.quantatw.sls.object.IRBrandData;
import com.quantatw.sls.object.IRCodeNumByKeywordData;
import com.quantatw.sls.object.IRCodeNumData;
import com.quantatw.sls.object.IRData;
import com.quantatw.sls.object.IRModelData;
import com.quantatw.sls.pack.roomhub.ir.IRACAutoScanResPack;
import com.quantatw.sls.pack.roomhub.ir.IRACKeyDataResPack;
import com.quantatw.sls.pack.roomhub.ir.IRBrandAndModelDataResPack;
import com.quantatw.sls.pack.roomhub.ir.IRBrandListResPack;
import com.quantatw.sls.pack.roomhub.ir.IRCodeNumByKeywordResPack;
import com.quantatw.sls.pack.roomhub.ir.IRCodeNumListResPack;
import com.quantatw.sls.pack.roomhub.ir.IRModelListResPack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Set;

/**
 * Created by erin on 10/20/15.
 */
public class IRController extends Handler {
    private final String TAG=IRController.class.getSimpleName();

    private boolean DEBUG = true;

    private Context mContext;
    private MiddlewareApi mApi;
    private boolean mHasIrDatabase;
    private boolean mIsOnParing = false;

    //private int mType;

    private ArrayList<IRParingStateChangedListener> mParingStateChangedListener = new ArrayList<IRParingStateChangedListener>();
    private HashMap<String, HashMap<String,IRParingActionCallback>> mParingActionCallback = new HashMap<>();
//    private HashMap<String,IRParingActionCallback> mParingActionCallback = new HashMap<>();

    private final int MESSAGE_IR_LEARNING = 100;
    private final int MESSAGE_HANDLE_IR_LEARNING_RESULTS = 101;
    private final int MESSAGE_HANDLE_IR_LEARNING_RESULTS_SUCCESS = 102;
    private final int MESSAGE_HANDLE_IR_LEARNING_RESULTS_FAIL = 103;
    private final int MESSAGE_PARING_DONE = 104;

    private final int MESSAGE_IR_PAIRING_TEST = 105;
    private final int MESSAGE_IR_PAIRING_START = 106;

    private final int MESSAGE_IR_AUTOSCAN = 110;

    private final int MESSAGE_SEARCH_BY_KEYWORD = 111;

    public interface OnSignalLearningCallback {
        void onSignalLearning();
        void onFound();
    }

    public static final int IR_TEST_BUTTON_POWER = 1000;
    public static final int IR_TEST_BUTTON_MODE = 200;
    public static final int IR_TEST_BUTTON_TEMP_INC = 300;
    public static final int IR_TEST_BUTTON_TEMP_DEC = 400;
    public static final int IR_TEST_BUTTON_MODE_COOL = 500;
    public static final int IR_TEST_BUTTON_MODE_HEAT = 501;
    public static final int IR_TEST_BUTTON_MODE_DRY = 502;
    public static final int IR_TEST_BUTTON_MODE_FAN = 503;
    public static final int IR_TEST_BUTTON_MODE_AUTO = 504;
    //TV
    public static final int IR_TEST_BUTTON_TV_POWER = IRAVDef.IR_AV_KEYID_POWER;
    public static final int IR_TEST_BUTTON_VOLUME_INC = IRAVDef.IR_AV_KEYID_VOL_UP;
    public static final int IR_TEST_BUTTON_VOLUME_DEC = IRAVDef.IR_AV_KEYID_VOL_DOWN;
    public static final int IR_TEST_BUTTON_CHANNEL_INC = IRAVDef.IR_AV_KEYID_CH_UP;
    public static final int IR_TEST_BUTTON_CHANNEL_DEC = IRAVDef.IR_AV_KEYID_CH_DOWN;
    public static final int IR_TEST_BUTTON_MUTE = IRAVDef.IR_AV_KEYID_MUTE;
    public static final int IR_TEST_BUTTON_MENU = IRAVDef.IR_AV_KEYID_MENU;
    public static final int IR_TEST_BUTTON_DIGIT_1 = IRAVDef.IR_AV_KEYID_DIGIT_1;
    //AIR_PURIFIER
    // TODO KeyID need check
    public static final int IR_TEST_BUTTON_WIND = IRACDef.IR_AC_KEYID_FAN_SPEED;
    public static final int IR_TEST_BUTTON_TIMER = IRACDef.IR_AC_KEYID_TIMER;

    public static String[] AC_MODES = {"COOL","HEAT","DRY","FAN","AUTO"};
    public static String[] AC_MODES_KEYID = {"4","7","5","6","3"};
    public static String IR_AC_KEYID_POWER_ON = "1";
    public static String IR_AC_KEYID_MODE_AUTO = "3";
    public static String IR_AC_KEYID_MODE_COOL = "4";
    public static String IR_AC_KEYID_MODE_DRY = "5";
    public static String IR_AC_KEYID_MODE_FAN = "6";
    public static String IR_AC_KEYID_MODE_HEAT = "7";
    public static String IR_AC_KEYID_TEMPERATURE_UP = "13";
    public static String IR_AC_KEYID_TEMPERATURE_DOWN = "14";
    public static String IR_AC_KEYID_TOGGLE_POWER = "21";
    public static int[] AC_MODES_STRING_RES_ID = {R.string.cooler, R.string.heater, R.string.dehumidifier, R.string.fan,R.string.auto};

    private OnSignalLearningCallback mOnSignalLearningCallback;
    private ArrayList<ApIRParingInfo> mIRAutoScanResults;

    private final String DATA_ROOMHUB_UUID = "DATA_ROOMHUB_UUID";
    private final String DATA_ASSET_TYPE = "DATA_ASSET_TYPE";
    private final String DATA_ASSET_UUID = "DATA_ASSET_UUID";

    // use codelib:
    private Object mBrandService, mCodeService;

    public static enum PAIR_MODE {
        LIST_BY_BRAND,
        LEARN_CODES,
        AUTO_SCAN
    }

    private class LearningResult {
        String uuid;
        int assetType;
        String assetUuid;
        int s0,s1,s2;
        String s3;
        IRSettingDataValues.IR_LEARNING_CHECK_TYPE checkType;

        LearningResult(String uuid, int assetType, String assetUuid, int s0, int s1, int s2, String s3, IRSettingDataValues.IR_LEARNING_CHECK_TYPE type) {
            this.uuid = uuid;
            this.assetType = assetType;
            this.assetUuid = assetUuid;
            this.s0 = s0;
            this.s1 = s1;
            this.s2 = s2;
            this.s3 = s3;
            this.checkType = type;
        }
    }

    private class PairingResult {
        ApIRParingInfo info;
        boolean result;

        PairingResult(ApIRParingInfo info, boolean result) {
            this.info = info;
            this.result = result;
        }
    }

    @Override
    public void handleMessage(Message msg) {
        switch(msg.what) {
            case MESSAGE_IR_LEARNING:
                startupLearning(msg.getData());
            break;
            case MESSAGE_HANDLE_IR_LEARNING_RESULTS:
                handleLearningResults((LearningResult)msg.obj);
                break;
            case MESSAGE_HANDLE_IR_LEARNING_RESULTS_SUCCESS:
                Intent sendIntent = (Intent)msg.obj;
                mContext.sendBroadcast(sendIntent);
                break;
            case MESSAGE_HANDLE_IR_LEARNING_RESULTS_FAIL:
                handleLearningResultsFail((String) msg.obj);
                break;
            case MESSAGE_PARING_DONE:
                handlePairingResults((PairingResult) msg.obj);
                break;
            case MESSAGE_IR_PAIRING_TEST:
                handleIRPairingTest((ApIRParingInfo) msg.obj);
                break;
            case MESSAGE_IR_PAIRING_START:;
                handleIRPairingStart((ApIRParingInfo) msg.obj);
                break;
            case MESSAGE_IR_AUTOSCAN:
                handleIRAutoScan(msg.getData());
                break;
            case MESSAGE_SEARCH_BY_KEYWORD:
                handleSearchByKeyword(msg.getData());
                break;
            default:
                super.handleMessage(msg);
                break;
        }
    }

    public IRController(Looper looper, Context context, MiddlewareApi api) {
        super(looper);
        mContext = context;
        mApi = api;

        /*
        mHasIrDatabase = Utils.hasIrDatabase(mContext);
        if(mHasIrDatabase) {
            mBrandService = (BrandService)new BrandService(mContext);
            mCodeService = (CodeService)new CodeService(mContext);
        }
        */
    }

    public void configIR(String deviceUuid, String assetUuid,
                         int assetType, IRParingActionCallback actionCallback) {
        log(TAG,"configIR deviceUuid="+deviceUuid+",assetUuid="+assetUuid+",assetType="+assetType);
        HashMap<String,IRParingActionCallback> assetActionCallback = mParingActionCallback.get(deviceUuid);
        if(assetActionCallback == null) {
            assetActionCallback = new HashMap<>();
        }
        assetActionCallback.put(assetUuid,actionCallback);
        mParingActionCallback.put(deviceUuid,assetActionCallback);
    }

    public void irParingTest(ApIRParingInfo currentTarget) {
        sendMessage(obtainMessage(MESSAGE_IR_PAIRING_TEST, currentTarget));
    }

    public void irParingStart(ApIRParingInfo currentTarget) {
        sendMessage(obtainMessage(MESSAGE_IR_PAIRING_START,currentTarget));
    }

    public void registerIRParingStateChangedListener(IRParingStateChangedListener listener) {
        if(findListenerIndex(mParingStateChangedListener,listener) < 0) {
            mParingStateChangedListener.add(listener);
        }
    }

    public void unregisterIRParingStateChangedListener(IRParingStateChangedListener listener) {
        int index = findListenerIndex(mParingStateChangedListener,listener);
        if(index >=0 ) {
            mParingStateChangedListener.remove(index);
        }
    }

//    public void setIRParingAction(int type,IRParingActionCallback action) {
//        mParingActionCallback.put(type, action);
//    }

    public ArrayList<ApIRBrandData> getBrandList(int assetType) {
        String locale = Locale.getDefault().getLanguage();
        if(locale.equals("zh"))
            locale = LanguageType.TW;

        return getBrandList(assetType, locale);
    }

    public ArrayList<ApIRBrandData> getBrandList(int assetType, String language) {
        if(mHasIrDatabase) {
            /*
           //ArrayList<Brand> list = ((BrandService)mBrandService).getBrand(Constant.CATEGORY_AC, LanguageType.CN);
            ArrayList<Brand> list = ((BrandService)mBrandService).getBrand(Integer.toString(deviceId), Utils.getIrDatabaseRegion());
            if(list != null && list.size() > 0) {
                return transferToApIRBrandData(list);
            }
            */
        }

        //IRBrandListResPack resPack = mApi.GetIRBrandList(IrDeviceType.AIR_CONDITIONER, language);
        ArrayList<ApIRBrandData> resultList = new ArrayList<ApIRBrandData>();
        IRBrandListResPack resPack;
        if(assetType == DeviceTypeConvertApi.TYPE_ROOMHUB.TV){
            resPack = mApi.GetAVBrandList(getIRDeviceId(assetType), language);
        }else {
            resPack = mApi.GetIRBrandList(getIRDeviceId(assetType), language);
        }
        if(resPack.getStatus_code()== ErrorKey.Success) {
            resultList = transferCloudToApIRBrandData(assetType, resPack.getBrandList());
        }

        //For window AC
        if(assetType == DeviceTypeConvertApi.TYPE_ROOMHUB.AC){
            resPack = mApi.GetIRBrandList(IrDeviceType.WINDOW_AC, language);
            if(resPack.getStatus_code() == ErrorKey.Success) {
                ArrayList<ApIRBrandData> resultList2 = transferCloudToApIRBrandData(assetType, resPack.getBrandList());
                log(TAG,"getBrandList list2 size ="+resultList2.size());
                for (ApIRBrandData data: resultList2
                        ) {
                    boolean found = false;
                    for (ApIRBrandData data2 : resultList
                            ) {
                        if (data.getBrandId() == data2.getBrandId()) {
                            found = true;
                            break;
                        }
                    }
                    if (!found){
                        log(TAG,"getBrandList add data BrandId="+data.getBrandId());
                        resultList.add(data);
                    }
                }
                Collections.sort(resultList, new Comparator<ApIRBrandData>() {
                    @Override
                    public int compare(ApIRBrandData lhs, ApIRBrandData rhs) {
                        return lhs.getBrandName().compareTo(rhs.getBrandName());
                    }
                });
            }
        }

        return resultList;
    }

//    public ApIRModelData getFirstModel(int brandId) {
//        IRModelResPack resPack = mApi.GetIRFirstModel(IrDeviceType.AIR_CONDITIONER, brandId);
//        if(resPack.getStatus_code() == ErrorKey.Success) {
//            return transferCloudToApIRModelData(resPack.getModel());
//        }
//        return null;
//    }

    public ArrayList<ApIRParingInfo> getPairingListByBrandData(int assetType, String uuid, String assetUuid, ApIRBrandData brandData) {
        ArrayList<ApIRModelData> modelList = getModelListByBrandId(assetType, brandData.getBrandId());

        /* for checking records is duplicated */
        HashMap<String, ApIRParingInfo> modelNumList = new HashMap<>();

        ArrayList<ApIRParingInfo> paringInfos = new ArrayList<ApIRParingInfo>();
        for(ApIRModelData modelData: modelList) {
            /* for fan type, check this device model number has been added before */
            if(assetType == DeviceTypeConvertApi.TYPE_ROOMHUB.FAN) {
                boolean found = false;
                for (ApIRParingInfo irParingInfo : paringInfos) {
                    if (irParingInfo.getDevModelNumber().trim().equals(modelData.getDevModelNum().trim())) {
                        found = true;
                        break;
                    }
                }
                if (found) {
                    continue;
                }
            }

            /* for other type, check this remote model number has been added before */
            if(assetType != DeviceTypeConvertApi.TYPE_ROOMHUB.FAN && modelNumList.get(modelData.getRemoteModelNum()) != null)
                continue;


            /* this remote model number has been added before */
            if(modelNumList.get(modelData.getRemoteModelNum()) != null)
                continue;

            ApIRParingInfo info = new ApIRParingInfo();

            info.setUuid(uuid);
            info.setAssetUuid(assetUuid);
            info.setAssetType(assetType);
            info.setBrandName(brandData.getBrandName());
            info.setCodeNum(Integer.parseInt(modelData.getCodeNum()));
            info.setDevModelNumber(modelData.getDevModelNum());
            info.setRemoteModelNum(modelData.getRemoteModelNum());
            info.setBrandId(brandData.getBrandId());
            info.setModelId(modelData.getModelId());
            info.setSubType(modelData.getSubtype());

            paringInfos.add(info);
            modelNumList.put(modelData.getRemoteModelNum(),info);
        }

        return paringInfos;
    }

    public ArrayList<ApIRModelData> getModelListByBrandId(int assetType, int brandId) {
        if(mHasIrDatabase) {
            /*
            ArrayList<String> list = ((CodeService)mCodeService).queryCodeNum(
                    Utils.getIrDatabaseRegion(), Integer.toString(deviceId), Integer.toString(brandId));
            if(list != null && list.size() > 0) {
                return transferToIRModelData();
            }
            */
        }
        ArrayList<ApIRModelData> resultList = new ArrayList<ApIRModelData>();
        IRModelListResPack resPack;
        if(assetType == DeviceTypeConvertApi.TYPE_ROOMHUB.TV){
            resPack = mApi.GetAVModelList(getIRDeviceId(assetType), brandId);
        }else{
            resPack = mApi.GetIRModelList(getIRDeviceId(assetType), brandId);
        }
        if (resPack.getStatus_code() == ErrorKey.Success) {
            resultList = transferCloudToApIRModelDataList(resPack.getModelList(), 0);
        }
        //For window AC
        if(assetType == DeviceTypeConvertApi.TYPE_ROOMHUB.AC){
            resPack = mApi.GetIRModelList(IrDeviceType.WINDOW_AC, brandId);
            if(resPack.getStatus_code() == ErrorKey.Success) {
                ArrayList<ApIRModelData> resultList2 = transferCloudToApIRModelDataList(resPack.getModelList(),ACDef.AC_SUBTYPE_WINDOW_TYPE);
                log(TAG,"getModelListByBrandId add list size = "+resultList2.size());
                resultList.addAll(resultList2);
            }
        }

        return resultList;
    }

    public void learning(String uuid, String assetUuid, IRController.OnSignalLearningCallback onSignalLearningCallback) {
        mOnSignalLearningCallback = onSignalLearningCallback;
        Message message = obtainMessage(MESSAGE_IR_LEARNING);
        Bundle bundle = new Bundle();
        bundle.putString(DATA_ROOMHUB_UUID, uuid);
        bundle.putString(DATA_ASSET_UUID, assetUuid);
        message.setData(bundle);
        sendMessage(message);
    }

    public void autoScan(String uuid, String assetUuid, int assetType) {
        Message message = obtainMessage(MESSAGE_IR_AUTOSCAN);
        Bundle bundle = new Bundle();
        bundle.putString(DATA_ROOMHUB_UUID, uuid);
        bundle.putString(DATA_ASSET_UUID, assetUuid);
        bundle.putInt(DATA_ASSET_TYPE, assetType);
        message.setData(bundle);
        sendMessage(message);
    }

    public IRBrandAndModelData getIRBrandAndModelName(int assetType, int codeNum, String brandName) {
        IRBrandAndModelDataResPack resPack = getIRBrandAndModelResPack(getIRDeviceId(assetType),codeNum);
        if(resPack.getStatus_code() == ErrorKey.Success) {
            ArrayList<IRBrandAndModelData> list = resPack.getBrandAndModelDataList();
            if(!TextUtils.isEmpty(brandName)) {
                for (IRBrandAndModelData modelData : list) {
                    if (modelData.getBrandName().equalsIgnoreCase(brandName)) {
                        return modelData;
                    }
                }
            }
            if(list != null && list.size() > 0) {
                // TODO: Always get the first node:
                return list.get(0);
            }
        }
        if (assetType == DeviceTypeConvertApi.TYPE_ROOMHUB.AC){
            resPack = getIRBrandAndModelResPack(IrDeviceType.WINDOW_AC,codeNum);
            if(resPack.getStatus_code() == ErrorKey.Success) {
                ArrayList<IRBrandAndModelData> list = resPack.getBrandAndModelDataList();
                if(!TextUtils.isEmpty(brandName)) {
                    for (IRBrandAndModelData modelData : list) {
                        if (modelData.getBrandName().equalsIgnoreCase(brandName)) {
                            return modelData;
                        }
                    }
                }
                if(list != null && list.size() > 0) {
                    // TODO: Always get the first node:
                    return list.get(0);
                }
            }
        }
        return null;
    }

    public void searchByKeyword(int assetType, String roomHubUuid, String assetUuid, String keyword) {
        Message message = obtainMessage(MESSAGE_SEARCH_BY_KEYWORD);
        Bundle bundle = new Bundle();
        bundle.putInt(IRSettingDataValues.KEY_ELECTRIC_TYPE,assetType);
        bundle.putString(IRSettingDataValues.KEY_DATA_ROOM_HUB_DEVICE_UUID, roomHubUuid);
        bundle.putString(IRSettingDataValues.KEY_ELECTRIC_UUID, assetUuid);
        bundle.putString("KEYWORD", keyword);
        message.setData(bundle);
        sendMessage(message);
    }

    public ArrayList<ApIRParingInfo> getAutoScanResults() {
        return mIRAutoScanResults;
    }

    public boolean isIROnParing() {
        return mIsOnParing;
    }

    public void log(String className, String msg) {
        if(DEBUG)
            Log.d(TAG,"["+className+"]:"+msg);
    }

    /******************************************************************************************/
    /******************************************************************************************/
    /******************************************************************************************/

    private ArrayList<IRACKeyData> getIRKeyDataListByCodeNumber(int codeNum,int assetType) {
        if(mHasIrDatabase) {

        }
        IRACKeyDataResPack resPack;
        if(assetType == DeviceTypeConvertApi.TYPE_ROOMHUB.TV){
            resPack = mApi.GetAVKeyData(codeNum);
        }else {
            resPack = mApi.GetACKeyData(codeNum);
        }
        return resPack.getACKeyList();
    }

    private IRParingActionCallback getIRActionCallback(String uuid, String assetUuid) {
            return mParingActionCallback.get(uuid).get(assetUuid);
    }

    private void startupLearning(Bundle data) {
        String uuid = data.getString(DATA_ROOMHUB_UUID);
        String assetUuid = data.getString(DATA_ASSET_UUID);
        try{
            getIRActionCallback(uuid,assetUuid).onLearning(uuid,assetUuid, mIRLearningResultCallback);
        }catch (Exception e){
            Toast.makeText(mContext,R.string.ir_pairing_fail,Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(mContext, RoomHubMainPage.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);
        }
    }

    /*
    * IR Pairing result callback
     */
    private IRControllerCallback mIRPairingResultCallback = new IRControllerCallback() {
        @Override
        public void onPairingProgress(ApIRParingInfo currentTarget, int handleCount) {
            if(mParingStateChangedListener != null) {
                for(IRParingStateChangedListener listener: mParingStateChangedListener) {
                    listener.onPairingProgress(currentTarget, handleCount);
                }
            }
        }

        @Override
        public void onPairingResult(ApIRParingInfo currentTarget, boolean result) {
            mIsOnParing = false;
            sendMessage(obtainMessage(MESSAGE_PARING_DONE,new PairingResult(currentTarget, result)));
        }
    };

    /*
    * IR learning result callback
     */
    private IRLearningResultCallback mIRLearningResultCallback = new IRLearningResultCallback() {
        @Override
        public void onLoadResultsSuccess(String uuid, int assetType, String assetUuid, int s0, int s1, int s2, String s3, IRSettingDataValues.IR_LEARNING_CHECK_TYPE checkType) {
            log(TAG,"onLoadResultsSuccess uuid="+uuid);
            sendMessage(obtainMessage(MESSAGE_HANDLE_IR_LEARNING_RESULTS, new LearningResult(uuid, assetType, assetUuid, s0, s1, s2, s3, checkType)));
        }

        @Override
        public void onLoadResultsFail(String uuid) {
            log(TAG,"onLoadResultsFail uuid="+uuid);
            sendMessage(obtainMessage(MESSAGE_HANDLE_IR_LEARNING_RESULTS_FAIL, uuid));
        }
    };

    private void handleLearningResults(LearningResult result) {
        ArrayList<IRCodeNumData> list = null;
        log(TAG, "handleLearningResults enter ");
        log(TAG, "handleLearningResults s0="+result.s0+",s1="+result.s1+",s3="+result.s3);
        if(mOnSignalLearningCallback != null)
            mOnSignalLearningCallback.onSignalLearning();

        if(result.checkType == IRSettingDataValues.IR_LEARNING_CHECK_TYPE.IR_AC_CHECK_TYPE) {
            //list = GetCodeNumByLearningSearch(result.s0,result.s3);
//            list = GetCodeNumByLearningSearch(result.s1, result.s3);
            list = GetCodeNumByLearningSearch(result.assetType, result.s0, result.s1, result.s3);
        }

        log(TAG, "handleLearningResults list="+list);
        // notify IRLearningActivity
        Intent intent = new Intent(IRSettingDataValues.ACTION_IR_LEARNING_RESULTS);
        intent.putExtra(IRSettingDataValues.KEY_DATA_ROOM_HUB_DEVICE_UUID, result.uuid);
        if(list != null && list.size() > 0) {
            if(mOnSignalLearningCallback != null)
                mOnSignalLearningCallback.onFound();
            log(TAG, "handleLearningResults list size=" + list.size());
            ArrayList<ApIRParingInfo> learningResultList = getLearningResultList(result.uuid, result.assetUuid, list);
            intent.putExtra(IRSettingDataValues.KEY_IR_LEARNING_RESULTS_REASON, IRSettingDataValues.IR_LEARNING_SUCCESS);
            intent.putParcelableArrayListExtra(IRSettingDataValues.KEY_IR_LEARNING_RESULTS, learningResultList);
        } else {
            intent.putExtra(IRSettingDataValues.KEY_IR_LEARNING_RESULTS_REASON, IRSettingDataValues.IR_LEARNING_NOT_MATCHED);
        }

        // delay to show next page for staying on page longer
        sendMessageDelayed(obtainMessage(MESSAGE_HANDLE_IR_LEARNING_RESULTS_SUCCESS, intent), 2000);
        //mContext.sendBroadcast(intent);

    }

    private void handleLearningResultsFail(String uuid) {
        // notify IRLearningActivity
        log(TAG, "handleLearningResultsFail enter ");
        Intent intent = new Intent(IRSettingDataValues.ACTION_IR_LEARNING_RESULTS);
        intent.putExtra(IRSettingDataValues.KEY_DATA_ROOM_HUB_DEVICE_UUID, uuid);
        intent.putParcelableArrayListExtra(IRSettingDataValues.KEY_IR_LEARNING_RESULTS, null);
        intent.putExtra(IRSettingDataValues.KEY_IR_LEARNING_RESULTS_REASON, IRSettingDataValues.IR_LEARNING_TIMEOUT);
        mContext.sendBroadcast(intent);
    }

    private ArrayList<ApIRParingInfo> getLearningResultList(String uuid, String assetUuid, ArrayList<IRCodeNumData> list) {
        ArrayList<ApIRParingInfo> resultList = new ArrayList<ApIRParingInfo>();
        Set<Integer> codeNumSet = new HashSet<>();
        for(IRCodeNumData data: list) {
            if (codeNumSet.contains(data.getCodeNum())){
                continue;
            }else{
                codeNumSet.add(data.getCodeNum());
            }
            /*
             * For the latest spec, no need to display model number on screen,
             * leave it when ir pairing process is finished.
             * It could also speed up UI response
              * RoomHubManager: IRParingActionCallback/onStart()
              */
            //IRBrandAndModelData brandAndModelData = getIRBrandAndModelName(data.getCodeNum());
            ApIRParingInfo val = new ApIRParingInfo();

            val.setUuid(uuid);
            val.setAssetUuid(assetUuid);
            /*
            if(brandAndModelData != null) {
                val.setBrandName(brandAndModelData.getBrandName());
                val.setDevModelNumber(brandAndModelData.getDevModelNum());
            } else {
                val.setBrandName(Integer.toString(data.getCodeNum()));
                val.setDevModelNumber(Integer.toString(data.getCodeNum()));
            }
             */
            val.setCodeNum(data.getCodeNum());
            resultList.add(val);
        }
        return resultList;
    }

//    private LinkedHashMap<String, LinkedHashMap<Integer,IRACKeyData>> obtainTestKeyDataAc(int assetType, ArrayList<IRACKeyData> iracKeyDataArrayList) {
//
//        LinkedHashMap<String, LinkedHashMap<Integer,IRACKeyData>> keyDataByMode = new LinkedHashMap<>();
//
//        for(String mode:AC_MODES) {
//            keyDataByMode.put(mode,new LinkedHashMap<Integer, IRACKeyData>());
//        }
//        for(String mode:AC_MODES) {
//            LinkedHashMap<Integer,IRACKeyData> map = keyDataByMode.get(mode);
//            for (IRACKeyData iracKeyData : iracKeyDataArrayList) {
//                if(mode.equalsIgnoreCase(iracKeyData.getStMode()) &&
//                        "ON".equalsIgnoreCase(iracKeyData.getStPower())) {
//                    map.put(iracKeyData.getStTemp(),iracKeyData);
//                }
//            }
//            keyDataByMode.put(mode,map);
//        }
//        return keyDataByMode;
//    }

    //Resort arraylist by mode
    private LinkedHashMap<String, ArrayList<IRACKeyData>> obtainTestKeyDataAc(int assetType, ArrayList<IRACKeyData> iracKeyDataArrayList) {

        LinkedHashMap<String, ArrayList<IRACKeyData>> keyDataByMode = new LinkedHashMap<>();

        for(String mode:AC_MODES) {
            keyDataByMode.put(mode,new ArrayList<IRACKeyData>());
        }
        for(String mode:AC_MODES) {
            ArrayList<IRACKeyData> specifiedModeList = keyDataByMode.get(mode);
            for (IRACKeyData data : iracKeyDataArrayList) {
                if(mode.equals(data.getStMode()))
                    specifiedModeList.add(data);
            }
            keyDataByMode.put(mode,specifiedModeList);
        }
        return keyDataByMode;
    }

    private void handlePairingResults(PairingResult pairingResult) {
        notifyPairingDone(pairingResult.info, pairingResult.result);
    }

    //compare irkeydata with keyid 1 & 2 have same irdata
    private boolean isACTogglePower(ArrayList<IRACKeyData> list)
    {
        for (IRACKeyData data: list) {
            if(IR_AC_KEYID_TOGGLE_POWER.equals(data.getKeyId())){
                return true;
            }
        }
        return false;
    }

    private void handleIRPairingTest(ApIRParingInfo currentTarget) {
        String brandName = currentTarget.getBrandName();
        int codeNum = currentTarget.getCodeNum();
        String devModelNum = currentTarget.getDevModelNumber();
        ArrayList<IRACKeyData> keyDataArrayList = currentTarget.getIracKeyDataList();
        if(keyDataArrayList == null) {
            keyDataArrayList = getIRKeyDataListByCodeNumber(codeNum,currentTarget.getAssetType());
            currentTarget.setIracKeyDataList(keyDataArrayList);
        }

        log(TAG, "handleIRPairingTest enter codeNum=" + codeNum + ",keyDataArrayList=" + keyDataArrayList);
        String irData = null;
        currentTarget.setConnectionType(AssetDef.CONNECTION_TYPE_IR);
        if(currentTarget.getAssetType()==DeviceTypeConvertApi.TYPE_ROOMHUB.FAN) {
            irData = getPowerIrData(keyDataArrayList);

            if(TextUtils.isEmpty(irData)) {
                log(TAG, "handleIRPairingTest can't find keyId=1 or 21, send the first one");
               irData = keyDataArrayList.get(0).getIrData();
            }
        }else if(currentTarget.getAssetType()==DeviceTypeConvertApi.TYPE_ROOMHUB.TV) {
            //TODO not check
            for(IRACKeyData iracKeyData: keyDataArrayList) {
                int keyId = Integer.parseInt(iracKeyData.getKeyId());
                if(currentTarget.getTestButton() == keyId) {
                    irData = iracKeyData.getIrData();
                    break;
                }
            }
        }else if(currentTarget.getAssetType()==DeviceTypeConvertApi.TYPE_ROOMHUB.AIR_PURIFIER) {
            if (currentTarget.getTestButton() == IR_TEST_BUTTON_POWER) {
                irData = getPowerIrData(keyDataArrayList);
            }else {
                for (IRACKeyData iracKeyData : keyDataArrayList) {
                    int keyId = Integer.parseInt(iracKeyData.getKeyId());
                    if (currentTarget.getTestButton() == keyId) {
                        irData = iracKeyData.getIrData();
                        break;
                    }
                }
            }
        }
        else {
            LinkedHashMap<String, ArrayList<IRACKeyData>> testKeyMap = obtainTestKeyDataAc(currentTarget.getAssetType(), keyDataArrayList);
            log(TAG, "handleIRPairingTest test button=" + currentTarget.getTestButton());
            if (currentTarget.getTestButton() == IR_TEST_BUTTON_POWER) {
                /* COOL, ON, TEMP=25 */
                ArrayList<IRACKeyData> coolModeList = testKeyMap.get("COOL");
                IRACKeyData iracKeyData = null;
                for (IRACKeyData keydata: coolModeList) {
                    if((IR_AC_KEYID_POWER_ON.equals(keydata.getKeyId()))
                            && keydata.getStTemp() == 25){
                        iracKeyData = keydata;
                        log(TAG,"test power on ir command KeyID: "+iracKeyData.getKeyId() + " PWR: " + iracKeyData.getStPower()
                                + " Mode: "+ iracKeyData.getStMode() + " Temp: " + iracKeyData.getStTemp());
                        break;
                    }
                }
                if (iracKeyData== null) {
                    for (IRACKeyData keydata : coolModeList) {
                        if (IR_AC_KEYID_TOGGLE_POWER.equals(keydata.getKeyId())
                                && keydata.getStTemp() == 25) {
                            iracKeyData = keydata;
                            log(TAG, "test power on ir command KeyID: " + iracKeyData.getKeyId() + " PWR: " + iracKeyData.getStPower()
                                    + " Mode: " + iracKeyData.getStMode() + " Temp: " + iracKeyData.getStTemp());
                            break;
                        }
                    }
                }
                if (iracKeyData== null) {
                    log(TAG, "test power on not found remote model number: " + currentTarget.getRemoteModelNum());
                    iracKeyData = keyDataArrayList.get(0);
                }

                irData = iracKeyData.getIrData();

                if (currentTarget.getAssetType() == DeviceTypeConvertApi.TYPE_ROOMHUB.AC){
                    if(isACTogglePower(keyDataArrayList))
                    {
                        if (currentTarget.getSubType()== ACDef.AC_SUBTYPE_SPLIT_TYPE) {
                            currentTarget.setSubType(ACDef.AC_SUBTYPE_TOGGLE_TYPE);
                        }
                        currentTarget.setCheckIrDataFailed(true);
                        //notifyPairingTest(currentTarget);
                        //Toast.makeText(mContext,R.string.ir_learning_fail_wrong_data,Toast.LENGTH_LONG).show();
                    }
                }


                // reset mode to 0, temp to 25
                currentTarget.setTestCurrentMode(0);
                currentTarget.setTestCurrentTemp((int)Utils.getTemp(mContext,(double) 25));
                log(TAG, "handleIRPairingTest POWER: COOL/25");
            }
            else {
                IRACKeyData iracKeyData = null;
                switch (currentTarget.getTestButton()) {
                    case IR_TEST_BUTTON_MODE_COOL:
                    case IR_TEST_BUTTON_MODE_HEAT:
                    case IR_TEST_BUTTON_MODE_DRY:
                    case IR_TEST_BUTTON_MODE_FAN:
                    case IR_TEST_BUTTON_MODE_AUTO:
                        iracKeyData = getSupportIRACKeyData(currentTarget);
                        if (iracKeyData == null)
                            log(TAG,"test mode change not found remote model number: " + currentTarget.getRemoteModelNum());
                        break;
                    case IR_TEST_BUTTON_MODE: {
                        int mode = currentTarget.getTestCurrentMode();
                        int nextMode = mode + 1;
                        if (nextMode >= AC_MODES.length)
                            nextMode = 0;

                        boolean found = false;
                        String nextModeKeyId = IR_AC_KEYID_MODE_COOL;
                        for (int i = 0; i < AC_MODES.length; i++) {
                            if (i == nextMode) {
                                if(testKeyMap.get(AC_MODES[i]).size() > 0) {
                                    found = true;
                                    currentTarget.setTestCurrentMode(nextMode);
                                    nextModeKeyId = AC_MODES_KEYID[i];
                                    break;
                                }
                                else {
                                    // find next mode
                                    nextMode++;
                                }
                            }
                        }
                        // rollback
                        if(!found) {
                            currentTarget.setTestCurrentMode(0);
                        }
                        //get current mode irkeydatalist
                        ArrayList<IRACKeyData> specifiedModeList = testKeyMap.get(AC_MODES[currentTarget.getTestCurrentMode()]);
                        boolean foundData = false;
                        for (IRACKeyData modeData: specifiedModeList){
                            if(nextModeKeyId.equals(modeData.getKeyId())){
                                currentTarget.setTestCurrentTemp(modeData.getStTemp());
                                iracKeyData = modeData;
                                foundData = true;
                                log(TAG,"test mode change ir command KeyID: "+iracKeyData.getKeyId() + " PWR: " + iracKeyData.getStPower()
                                        + " Mode: "+ iracKeyData.getStMode() + " Temp: " + iracKeyData.getStTemp());
                                break;
                            }
                        }
                        if (!foundData)
                            log(TAG,"test mode change not found remote model number: " + currentTarget.getRemoteModelNum());
                    }
                        break;
                    case IR_TEST_BUTTON_TEMP_INC:
                        iracKeyData = getTemperatureUp(currentTarget);
                        break;
                    case IR_TEST_BUTTON_TEMP_DEC:
                        iracKeyData = getTemperatureDown(currentTarget);
                        break;
                    default:
                        break;
                }
                if(iracKeyData != null)
                    irData = iracKeyData.getIrData();
                else
                    log(TAG,"handleIRPairingTest can't find any ackeyData!!!");
            }
        }
        if(!TextUtils.isEmpty(irData)) {
            try {
                getIRActionCallback(currentTarget.getUuid(), currentTarget.getAssetUuid())
                        .onTest(currentTarget.getUuid(), irData);
            }catch (Exception e){
                Toast.makeText(mContext,R.string.ir_pairing_fail,Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(mContext, RoomHubMainPage.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);
            }
        }

        notifyPairingTest(currentTarget);
    }

    private void handleIRPairingStart(ApIRParingInfo currentTarget) {
        ArrayList<IRACKeyData> keyDataArrayList = getIRKeyDataListByCodeNumber(currentTarget.getCodeNum(),currentTarget.getAssetType());
        currentTarget.setIracKeyDataList(keyDataArrayList);
        final ApIRParingInfo currentInfo = currentTarget;

        mIsOnParing = true;
        notifyPairingStart(currentInfo);
        try {
            getIRActionCallback(currentTarget.getUuid(), currentTarget.getAssetUuid())
                    .onStart(currentInfo, mIRPairingResultCallback);
        }catch (Exception e){
            Toast.makeText(mContext,R.string.ir_pairing_fail,Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(mContext, RoomHubMainPage.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);
        }
    }

    private void handleIRAutoScan(Bundle bundle) {
        log(TAG, "handleIRAutoScan enter");

        String uuid = bundle.getString(DATA_ROOMHUB_UUID);
        String assetUuid = bundle.getString(DATA_ASSET_UUID);
        int assetType = bundle.getInt(DATA_ASSET_TYPE);
        ArrayList<ApIRParingInfo> list = getAutoScanList(uuid,assetUuid,assetType);
        int count = 0;
        if(list != null && list.size() > 0) {
            count = list.size();
            // notify IRLearningActivity
            log(TAG, "handleIRAutoScan get auto scan list size="+list.size());

            mIRAutoScanResults = list;
        }
        else
            mIRAutoScanResults = null;

        Intent intent = new Intent(IRSettingDataValues.ACTION_IR_AUTO_SCAN_RESULTS);
        intent.putExtra(IRSettingDataValues.KEY_DATA_ROOM_HUB_DEVICE_UUID, uuid);
        intent.putExtra(IRSettingDataValues.KEY_DATA_IR_AUTO_SCAN_COUNT, count);
        /* Can't pass large size of list here due to FAILED BINDER TRANSACTION */
//        intent.putParcelableArrayListExtra(IRSettingDataValues.KEY_IR_AUTO_SCAN_RESULTS, list);
        mContext.sendBroadcast(intent);

    }

    private void handleSearchByKeyword(Bundle bundle) {
        int assetType = bundle.getInt(IRSettingDataValues.KEY_ELECTRIC_TYPE);
        String roomHubUuid = bundle.getString(IRSettingDataValues.KEY_DATA_ROOM_HUB_DEVICE_UUID);
        String assetUuid = bundle.getString(IRSettingDataValues.KEY_ELECTRIC_UUID);
        String keyword = bundle.getString("KEYWORD");
        IRCodeNumByKeywordResPack resPack;
        if (assetType == DeviceTypeConvertApi.TYPE_ROOMHUB.TV){
            resPack = mApi.GetAVCodeNumByKeyword(getIRDeviceId(assetType), keyword.toUpperCase());
        }else {
            resPack = mApi.GetCodeNumByKeyword(getIRDeviceId(assetType), keyword.toUpperCase());
        }
        ArrayList<ApIRParingInfo> list = new ArrayList<>();
        if(resPack != null && resPack.getStatus_code()==ErrorKey.Success) {
            list = getSearchResult(assetType, roomHubUuid, assetUuid, resPack, 0);
        }
        if (assetType == DeviceTypeConvertApi.TYPE_ROOMHUB.AC) {
            resPack = mApi.GetCodeNumByKeyword(IrDeviceType.WINDOW_AC, keyword.toUpperCase());
            if (resPack != null && resPack.getStatus_code() == ErrorKey.Success) {
                ArrayList<ApIRParingInfo> list2 = getSearchResult(assetType, roomHubUuid, assetUuid, resPack, ACDef.AC_SUBTYPE_WINDOW_TYPE);
                list.addAll(list2);
                log(TAG,"handleSearchByKeyword add list size = "+list2.size());
            }
        }

        Intent intent = new Intent(IRSettingDataValues.ACTION_IR_SEARCH_RESULTS);
        intent.putExtra(IRSettingDataValues.KEY_DATA_ROOM_HUB_DEVICE_UUID, resPack.getUuid());
        if(list != null && list.size() > 0) {
            // notify IRSettingActivity
            log(TAG, "handleSearchByKeyword get search result list size=" + list.size());
            intent.putParcelableArrayListExtra(IRSettingDataValues.KEY_IR_SEARCH_RESULTS, list);
        }
        mContext.sendBroadcast(intent);
    }

    private ArrayList<ApIRParingInfo> getSearchResult(int assetType,
                                                      String roomHubUuid,
                                                      String assetUuid,
                                                      IRCodeNumByKeywordResPack resPack,
                                                      int subtype) {
        ArrayList<ApIRParingInfo> list = new ArrayList<ApIRParingInfo>();
        Set<Integer> codeNumSet = new HashSet<>();
        for(IRCodeNumByKeywordData data:resPack.getData()) {
            for(IRData irData: data.getIrDataList()) {
                if (codeNumSet.contains(irData.getCodeNum())){
                    continue;
                }else{
                    codeNumSet.add(irData.getCodeNum());
                }
                ApIRParingInfo val = new ApIRParingInfo();
                val.setAssetType(assetType);
                val.setUuid(roomHubUuid);
                val.setAssetUuid(assetUuid);
                val.setCodeNum(irData.getCodeNum());
                val.setBrandName(data.getBrandName());
                val.setRemoteModelNum(irData.getRemoterName());
                val.setModelId(irData.getModelId());
                val.setBrandId(irData.getBrandId());
                val.setSubType(subtype);
                //val.setIrDataFromSearch(irData.getIrData());
                list.add(val);
            }
        }
        return list;
    }

    private IRBrandAndModelDataResPack getIRBrandAndModelResPack(int deviceId,int codeNum) {
        return mApi.GetBrandAndModelData(deviceId, codeNum, Locale.getDefault().getLanguage());
    }

    private ArrayList<IRCodeNumData> GetCodeNumByLearningSearch(int assetType, int s0, int s1, String s3) {
        IRCodeNumListResPack resPack;
        ArrayList<IRCodeNumData> list = new ArrayList<>();
        if(assetType == DeviceTypeConvertApi.TYPE_ROOMHUB.TV){
            resPack = mApi.AVIrLearning(getIRDeviceId(assetType), s0, s1, s3);
        }else{
            resPack = mApi.ACIrLearning(getIRDeviceId(assetType), s0, s1, s3);
        }
        if(resPack.getStatus_code()==ErrorKey.Success) {
            list = resPack.getCodeNumDataList();
            if(assetType == DeviceTypeConvertApi.TYPE_ROOMHUB.AC) {
                for (IRCodeNumData data : list
                        ) {
                    data.setSubtype(ACDef.AC_SUBTYPE_SPLIT_TYPE);
                }
            }
        }
        if(assetType == DeviceTypeConvertApi.TYPE_ROOMHUB.AC) {
            resPack = mApi.ACIrLearning(IrDeviceType.WINDOW_AC, s0, s1, s3);
            if(resPack.getStatus_code()==ErrorKey.Success) {
                ArrayList<IRCodeNumData> list2 = resPack.getCodeNumDataList();
                for (IRCodeNumData data : list2
                        ) {
                    data.setSubtype(ACDef.AC_SUBTYPE_WINDOW_TYPE);
                }
                log(TAG,"GetCodeNumByLearningSearch add list size"+list2.size());
                list.addAll(list2);
            }
        }
        return list;
    }

    private ArrayList<IRCodeNumData> GetCodeNumByLearningSearch(String signature) {
        IRCodeNumListResPack resPack = mApi.GetCodeNumByLearningSearch(signature);
        if(resPack.getStatus_code()==ErrorKey.Success) {
            return resPack.getCodeNumDataList();
        }
        return null;
    }

    private ArrayList<ApIRParingInfo> getAutoScanList(String uuid, String assetUuid, int assetType) {
        IRACAutoScanResPack resPack;
        ArrayList<ApIRParingInfo> list = new ArrayList<>();
        if(assetType == DeviceTypeConvertApi.TYPE_ROOMHUB.TV){
            resPack = mApi.AVAutoScan(getIRDeviceId(assetType));
        }else {
            resPack = mApi.ACAutoScan(getIRDeviceId(assetType));
        }
        if(resPack.getStatus_code() == ErrorKey.Success) {
            list = getAutoScanPairingList(uuid, assetUuid, resPack.getACAutoScanList(),0);
        }
        if(assetType == DeviceTypeConvertApi.TYPE_ROOMHUB.AC) {
            resPack = mApi.ACAutoScan(IrDeviceType.WINDOW_AC);
            if(resPack.getStatus_code() == ErrorKey.Success) {
                ArrayList<ApIRParingInfo> list2 = getAutoScanPairingList(uuid, assetUuid, resPack.getACAutoScanList(),0);
                list.addAll(list2);
                log(TAG,"getAutoScanList add list size = "+list2.size());
            }
        }
        return list;
    }

    private ArrayList<ApIRParingInfo> getAutoScanPairingList(String uuid, String assetUuid, ArrayList<IRACData> list, int subtype) {
        ArrayList<ApIRParingInfo> resultList = new ArrayList<ApIRParingInfo>();
        Set<Integer> codeNumSet = new HashSet<>();
        for(IRACData data: list) {
            if (codeNumSet.contains(data.getCodeNum())){
                continue;
            }else{
                codeNumSet.add(data.getCodeNum());
            }
            /*
             * For the latest spec, no need to display model number on screen,
             * leave it when ir pairing process is finished.
             * It could also speed up UI response
              * RoomHubManager: IRParingActionCallback/onStart()
              */
            //IRBrandAndModelData brandAndModelData = getIRBrandAndModelName(data.getCodeNum());
            ApIRParingInfo val = new ApIRParingInfo();

            val.setUuid(uuid);
            val.setAssetUuid(assetUuid);
            /*
            if(brandAndModelData != null) {
                val.setBrandName(brandAndModelData.getBrandName());
                val.setDevModelNumber(brandAndModelData.getDevModelNum());
            }
            else {
                val.setBrandName(Integer.toString(data.getCodeNum()));
                val.setDevModelNumber(Integer.toString(data.getCodeNum()));
            }
            */
            val.setCodeNum(data.getCodeNum());
            val.setAutoScanIrData(data.getIrData());
            val.setSubType(subtype);
            resultList.add(val);
        }
        return resultList;
    }

    private void notifyPairingTest(ApIRParingInfo currentTarget) {
        if(mParingStateChangedListener != null) {
            for(IRParingStateChangedListener listener: mParingStateChangedListener) {
                listener.onPairingTest(currentTarget);
            }
        }
    }

    private void notifyPairingStart(ApIRParingInfo currentTarget) {
        if(mParingStateChangedListener != null) {
            for (IRParingStateChangedListener listener : mParingStateChangedListener) {
                listener.onPairingStart(currentTarget);
            }
        }
    }

    private void notifyPairingDone(ApIRParingInfo currentTarget, boolean result) {
        if(mParingStateChangedListener != null) {
            for(IRParingStateChangedListener listener: mParingStateChangedListener) {
                listener.onPairingDone(currentTarget, result);
            }
        }
    }

    private int findListenerIndex(ArrayList list, Object listener) {
        for(int i=0;i<list.size();i++) {
            if(list.get(i).equals(listener))
                return i;
        }
        return -1;
    }

    private ArrayList<ApIRModelData> transferCloudToApIRModelDataList(ArrayList<IRModelData> list,int subtype) {
        ArrayList<ApIRModelData> resultList = new ArrayList<ApIRModelData>();

        for(IRModelData item: list) {
            ApIRModelData node = new ApIRModelData();
            node.setCodeNum(item.getCodeNum());
            node.setDevModelNum(item.getDevModelNum());
            node.setRate(item.getRate());
            node.setRemoteModelNum(item.getRemoteModelNum());
            node.setModelId(item.getModelId());
            node.setSubtype(subtype);
            resultList.add(node);
        }

        return resultList;
    }

    private ApIRModelData transferCloudToApIRModelData(IRModelData data) {
        ApIRModelData node = new ApIRModelData();
        node.setCodeNum(data.getCodeNum());
        node.setDevModelNum(data.getDevModelNum());
        node.setRate(data.getRate());
        node.setRemoteModelNum(data.getRemoteModelNum());

        return node;
    }

    private ArrayList<ApIRBrandData> transferCloudToApIRBrandData(int assetType, ArrayList<IRBrandData> list) {
        ArrayList<ApIRBrandData> resultList = new ArrayList<ApIRBrandData>();

        for(IRBrandData item: list) {
            ApIRBrandData node = new ApIRBrandData();
            node.setBrandId(item.getBrandId());
            node.setBrandName(item.getBrandName());
            node.setAssetType(assetType);
            resultList.add(node);
        }

        return resultList;
    }

    private ArrayList<IRModelData> transferToIRModelData(ArrayList<String> list) {
        ArrayList<IRModelData> resultList = new ArrayList<IRModelData>();

        for(String item: list) {
            IRModelData node = new IRModelData();
            node.setCodeNum(item);
            resultList.add(node);
        }

        return resultList;
    }

    private int getIRDeviceId(int assetType) {
        int deviceId = IrDeviceType.AIR_CONDITIONER;
        if (assetType == DeviceTypeConvertApi.TYPE_ROOMHUB.FAN) {
            deviceId = IrDeviceType.FAN;
        } else if (assetType == DeviceTypeConvertApi.TYPE_ROOMHUB.TV) {
            deviceId = IrDeviceType.TV;
        }else if (assetType == DeviceTypeConvertApi.TYPE_ROOMHUB.AIR_PURIFIER) {
            deviceId = IrDeviceType.AIR_PURIFIER;
        }
        return deviceId;
    }

    public IRACKeyData getTemperatureUp(ApIRParingInfo currentTarget) {
        IRACKeyData iracKeyData = null;
        if (currentTarget.getSubType() != ACDef.AC_SUBTYPE_WINDOW_TYPE) {
            int mode = currentTarget.getTestCurrentMode();
            int temp = currentTarget.getTestCurrentTemp() + 1;
            LinkedHashMap<String, ArrayList<IRACKeyData>> testKeyMap = obtainTestKeyDataAc(currentTarget.getAssetType(), currentTarget.getIracKeyDataList());
            ArrayList<IRACKeyData> specifiedModeList = testKeyMap.get(AC_MODES[mode]);
            for (IRACKeyData modeData : specifiedModeList) {
                if (IR_AC_KEYID_TEMPERATURE_UP.equals(modeData.getKeyId()) &&
                        ((int) Utils.getTempToCelsius(mContext, temp)) == modeData.getStTemp()) {
                    iracKeyData = modeData;
                    log(TAG, "test TEMPERATURE INC ir command KeyID: " + iracKeyData.getKeyId() + " PWR: " + iracKeyData.getStPower()
                            + " Mode: " + iracKeyData.getStMode() + " Temp: " + iracKeyData.getStTemp());
                    break;
                }
            }
            if (iracKeyData != null) {
                currentTarget.setTestCurrentTemp(temp);
                log(TAG, "handleIRPairingTest TEMP INC: " + AC_MODES[currentTarget.getTestCurrentMode()] + "/" + temp);
            } else {
                log(TAG, "test TEMPERATURE INC not found remote model number: " + currentTarget.getRemoteModelNum());
            }
        } else {
            for (IRACKeyData modeData : currentTarget.getIracKeyDataList()) {
                if (IR_AC_KEYID_TEMPERATURE_UP.equals(modeData.getKeyId())) {
                    iracKeyData = modeData;
                    log(TAG, "test TEMPERATURE INC ir command KeyID: " + iracKeyData.getKeyId());
                    break;
                }
            }
            if (iracKeyData == null) {
                log(TAG, "test TEMPERATURE INC not found remote model number: " + currentTarget.getRemoteModelNum());
            }
        }
        return iracKeyData;
    }
    public IRACKeyData getTemperatureDown(ApIRParingInfo currentTarget) {
        IRACKeyData iracKeyData = null;
        if (currentTarget.getSubType() != ACDef.AC_SUBTYPE_WINDOW_TYPE) {
            int mode = currentTarget.getTestCurrentMode();
            int temp = currentTarget.getTestCurrentTemp() - 1;
            LinkedHashMap<String, ArrayList<IRACKeyData>> testKeyMap = obtainTestKeyDataAc(currentTarget.getAssetType(), currentTarget.getIracKeyDataList());
            ArrayList<IRACKeyData> specifiedModeList = testKeyMap.get(AC_MODES[mode]);
            for (IRACKeyData modeData : specifiedModeList) {
                if (IR_AC_KEYID_TEMPERATURE_DOWN.equals(modeData.getKeyId()) &&
                        ((int) Utils.getTempToCelsius(mContext, temp)) == modeData.getStTemp()) {
                    iracKeyData = modeData;
                    log(TAG, "test TEMPERATURE INC ir command KeyID: " + iracKeyData.getKeyId() + " PWR: " + iracKeyData.getStPower()
                            + " Mode: " + iracKeyData.getStMode() + " Temp: " + iracKeyData.getStTemp());
                    break;
                }
            }
            if (iracKeyData != null) {
                currentTarget.setTestCurrentTemp(temp);
                log(TAG, "handleIRPairingTest TEMP INC: " + AC_MODES[currentTarget.getTestCurrentMode()] + "/" + temp);
            } else {
                log(TAG, "test TEMPERATURE INC not found remote model number: " + currentTarget.getRemoteModelNum());
            }
        } else {
            for (IRACKeyData modeData : currentTarget.getIracKeyDataList()) {
                if (IR_AC_KEYID_TEMPERATURE_DOWN.equals(modeData.getKeyId())) {
                    iracKeyData = modeData;
                    log(TAG, "test TEMPERATURE INC ir command KeyID: " + iracKeyData.getKeyId());
                    break;
                }
            }
            if (iracKeyData == null) {
                log(TAG, "test TEMPERATURE INC not found remote model number: " + currentTarget.getRemoteModelNum());
            }
        }
        return iracKeyData;
    }

    public IRACKeyData getSupportIRACKeyData(ApIRParingInfo currentTarget){
        IRACKeyData iracKeyData = null;
        LinkedHashMap<String, ArrayList<IRACKeyData>> testKeyMap = obtainTestKeyDataAc(currentTarget.getAssetType(), currentTarget.getIracKeyDataList());
        int mode = 0;
        switch (currentTarget.getTestButton()){
            case IR_TEST_BUTTON_MODE_COOL:
                mode = 0;
                break;
            case IR_TEST_BUTTON_MODE_HEAT:
                mode = 1;
                break;
            case IR_TEST_BUTTON_MODE_DRY:
                mode = 2;
                break;
            case IR_TEST_BUTTON_MODE_FAN:
                mode = 3;
                break;
            case IR_TEST_BUTTON_MODE_AUTO:
                mode = 4;
                break;
        }
        currentTarget.setTestCurrentMode(mode);
        String modeKeyId = AC_MODES_KEYID[mode];
        ArrayList<IRACKeyData> specifiedModeList = testKeyMap.get(AC_MODES[mode]);
        if (specifiedModeList.size() > 0 ){
            for (IRACKeyData modeData: specifiedModeList){
                if(modeKeyId.equals(modeData.getKeyId())){
                    currentTarget.setTestCurrentTemp(modeData.getStTemp());
                    iracKeyData = modeData;
                    log(TAG,"test mode change ir command KeyID: "+iracKeyData.getKeyId() + " PWR: " + iracKeyData.getStPower()
                            + " Mode: "+ iracKeyData.getStMode() + " Temp: " + iracKeyData.getStTemp());
                    break;
                }
            }
        }
        return iracKeyData;
    }

    private String getPowerIrData(ArrayList<IRACKeyData> keyDataArrayList){
        for(IRACKeyData iracKeyData: keyDataArrayList) {
            int keyId = Integer.parseInt(iracKeyData.getKeyId());
            if(IRACDef.IR_AC_KEYID_POWER_ON == keyId) {
                return iracKeyData.getIrData();
            }
        }
        for(IRACKeyData iracKeyData: keyDataArrayList) {
            int keyId = Integer.parseInt(iracKeyData.getKeyId());
            if(IRACDef.IR_AC_KEYID_POWER_TOGGLE == keyId) {
                return iracKeyData.getIrData();
            }
        }
        return null;
    }
}
