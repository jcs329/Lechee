package com.quantatw.roomhub.listener;

import com.quantatw.roomhub.ir.ApIRParingInfo;

/**
 * Created by erin on 10/20/15.
 */
public interface IRParingStateChangedListener {
    public void onPairingTest(ApIRParingInfo currentInfo);
    public void onPairingStart(ApIRParingInfo currentInfo);
    public void onPairingProgress(ApIRParingInfo currentInfo, int handleCount);
    public void onPairingDone(ApIRParingInfo currentInfo, boolean result);
}
