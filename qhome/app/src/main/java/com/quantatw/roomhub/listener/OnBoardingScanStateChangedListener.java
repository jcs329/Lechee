package com.quantatw.roomhub.listener;

import com.quantatw.roomhub.manager.OnBoardee;

/**
 * Created by erin on 9/23/15.
 */
public interface OnBoardingScanStateChangedListener {
    public void onScanStart();
    public void onScanStop(int errorCode, OnBoardee[] clients);
}
