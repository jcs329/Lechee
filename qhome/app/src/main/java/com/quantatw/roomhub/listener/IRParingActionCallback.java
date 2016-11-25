package com.quantatw.roomhub.listener;

import com.quantatw.roomhub.ir.ApIRParingInfo;

/**
 * Created by erin on 10/27/15.
 */
public interface IRParingActionCallback {
    public void onTest(String uuid, String irData);
    public void onStart(ApIRParingInfo currentTarget, IRControllerCallback controllerCallback);
    public void onLearning(String uuid, String assetUuid, IRLearningResultCallback resultCallback);
}
