package com.quantatw.roomhub.listener;

import com.quantatw.roomhub.ir.ApIRParingInfo;

/**
 * Created by erin on 10/27/15.
 */
public interface IRControllerCallback {
    public void onPairingProgress(ApIRParingInfo currentTarget, int handleCount);
    public void onPairingResult(ApIRParingInfo currentTarget, boolean result);
}
