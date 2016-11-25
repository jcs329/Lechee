package com.quantatw.roomhub.ui;

import com.quantatw.roomhub.blepair.BLEPairController;
import com.quantatw.roomhub.manager.asset.manager.ACNoticeManager;
import com.quantatw.roomhub.manager.AccountManager;
import com.quantatw.roomhub.manager.IRController;
import com.quantatw.roomhub.manager.OTAManager;
import com.quantatw.roomhub.manager.OnBoardingManager;
import com.quantatw.roomhub.manager.RoomHubDBHelper;
import com.quantatw.roomhub.manager.asset.manager.RoomHubManager;
import com.quantatw.roomhub.manager.health.manager.HealthDeviceManager;

public interface ActivityInterface {
    public final static int DIALOG_SHOW_NOTHING = 0;
    public final static int DIALOG_SHOW_LOGING_FAIL = 1;
    public final static int DIALOG_SHOW_SIGNUP_FAIL = 2;
    public final static int DIALOG_SHOW_LOGIN_SUCCESS = 3;
    public final static int DIALOG_SHOW_SIGNUP_LOGIN_FAIL = 4;
    public final static int DIALOG_SHOW_DIALOG = 5;

    public RoomHubManager getRoomHubManager();
    public ACNoticeManager getACNoticeManager();
    public AccountManager getAccountManager();
    public OnBoardingManager getOnBoardingManager();
    //public MicroLocationManager getLocationManager ();
    public RoomHubDBHelper getRoomHubDBHelper();
    public IRController getIRController();
    public OTAManager getOTAManager();

    public HealthDeviceManager getHealthDeviceManager();
    public BLEPairController getBLEController();

    public void showProgressDialog(String title, String message);
    public void dismissProgressDialog(int showtime, int ret, int error_code);
    public void dismissProgressDialog();    // dismiss right away
    // other manager
}
