package com.quantatw.roomhub.listener;

import com.quantatw.roomhub.manager.OnBoardee;

import org.alljoyn.onboarding.sdk.OnboardingManager;

/**
 * Created by erin on 9/23/15.
 */
public interface OnBoardingStateChangedListener {
    public void onBoardingStart();
    public void onBoardingProgress(OnBoardee client, int state);
    public void onBoardingStop();
    public void onClientConnect(int errorCode, OnBoardee client);
    public void onClientJoined(int errorCode, OnboardingManager.OnboardingErrorType detailError, OnBoardee client);
    public void onClientJoinedEnd(OnBoardee client);
}
