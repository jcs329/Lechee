package com.quantatw.roomhub.listener;

/**
 * Created by 95010915 on 2015/9/23.
 */
public interface AccountLoginStateListener {
    public void onLogin();
    public void onLogout();
    public void onSkipLogin();
}
