package com.quantatw.roomhub.manager.health.manager;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;

import com.quantatw.myapplication.R;
import com.quantatw.roomhub.listener.ShareUserChangedListener;
import com.quantatw.roomhub.manager.health.bpm.BPMData;
import com.quantatw.roomhub.manager.health.data.HealthData;
import com.quantatw.roomhub.manager.health.listener.HealthDeviceChangeListener;
import com.quantatw.roomhub.manager.health.listener.HealthDeviceListener;
import com.quantatw.roomhub.manager.health.listener.ShareHealthDataListener;
import com.quantatw.sls.key.ErrorKey;
import com.quantatw.sls.pack.base.BaseResPack;

import java.util.HashMap;

/**
 * Created by erin on 4/29/16.
 */
public class HealthDeviceController implements HealthDeviceListener,ShareHealthDataListener {
    private final String TAG=HealthDeviceController.class.getSimpleName();
    protected Context mContext;
    private String mTag;
    protected int mType;
    protected int mTitleStringResourceId;
    protected int mDrawableResourceId;
    protected HandlerThread mBackgroundThread;
    protected Handler mBackgroundHandler;
    protected HealthDeviceView mHealthDeviceView;
    protected NotifyCallback mNotifyCallback;

    protected interface NotifyCallback {
        void onResult(int result, HealthData healthData);
    }

    public HealthDeviceController(Context context, int type, String tag, int title_string_resource, int drawable_resource) {
        mContext = context;
        mType = type;
        mTag = tag;
        mTitleStringResourceId = title_string_resource;
        mDrawableResourceId = drawable_resource;

        mBackgroundThread=new HandlerThread(mTag);
        mBackgroundThread.start();

    }

    protected void startup() {
        log(TAG,"startup");
    }

    protected void terminate() {
    }

    protected HealthData newHealthData() {
        return null;
    }

    protected int parseDataPack(HealthData healthData, int action, String jsonString) {
        return ErrorKey.HEALTHCARE_PARSE_HISTORY_FAIL;
    }

    protected int registerHealthDeviceChangeListener(HealthDeviceChangeListener listener) {
        return ErrorKey.Success;
    }

    protected int unregisterHealthDeviceChangeListener(HealthDeviceChangeListener listener) {
        return ErrorKey.Success;
    }

    protected void log(String msg) {
        log("",msg);
    }

    protected void log(String tag, String msg) {
        HealthDeviceManager.traceLog(tag, msg);
    }

    protected void addShareHealthData(HealthData healthData, NotifyCallback callback) {
        mNotifyCallback = callback;
    }

    public int getType() {
        return mType;
    }

    public void setType(int mType) {
        this.mType = mType;
    }

    public HealthDeviceView getViewController() {
        return this.mHealthDeviceView;
    }

    public int getDrawableResourceId() {
        return mDrawableResourceId;
    }

    public void setDrawableResourceId(int drawableResourceId) {
        this.mDrawableResourceId = mDrawableResourceId;
    }

    public int getTitleStringResourceId() {
        return mTitleStringResourceId;
    }

    public void setTitleStringResourceId(int titleStringResourceId) {
        this.mTitleStringResourceId = mTitleStringResourceId;
    }

    public void startBLEPairing() {
    }

    @Override
    public void addDeivce(HealthData device) {
    }

    @Override
    public void removeDevice(HealthData device) {
    }

    @Override
    public void updateDevice(int type, HealthData device) {
    }

    @Override
    public void contentChange(HealthData device, BaseResPack updateResPack) {
    }

    @Override
    public void addShareHealthData(HealthData healthData) {
    }

    @Override
    public void removeHealthData(HealthData healthData) {

    }

    @Override
    public void updateHealthData(HealthData healthData) {

    }
}
