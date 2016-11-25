package com.quantatw.roomhub.listener;

import com.quantatw.roomhub.ui.IRSettingDataValues;

/**
 * Created by erin on 10/27/15.
 */
public interface IRLearningResultCallback {
    public void onLoadResultsSuccess(String uuid, int assetType, String assetUuid, int s0, int s1, int s2, String s3, IRSettingDataValues.IR_LEARNING_CHECK_TYPE checkType);
    public void onLoadResultsFail(String uuid);
}
