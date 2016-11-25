package com.quantatw.roomhub.manager;

import com.quantatw.roomhub.listener.OTAStateChangeListener;
import com.quantatw.roomhub.utils.Utils;
import com.quantatw.sls.pack.roomhub.VersionCheckUpdateReqPack;

import java.util.LinkedHashSet;

/**
 * Created by erin on 1/20/16.
 */
public class OTADevice {
    private final String TAG=OTADevice.class.getSimpleName();
    public static class NewVersionInfo {
        private String driverName;
        private String version;
        private String url;
        private String md5;

        NewVersionInfo() {}

        public String getDriverName() {
            return driverName;
        }

        public String getVersion() {
            return version;
        }

        public String getUrl() {
            return url;
        }

        public String getMd5() {
            return md5;
        }

        public void setDriverName(String driverName) {
            this.driverName = driverName;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public void setMd5(String md5) {
            this.md5 = md5;
        }

        public String toString() {
            return("NewVersionInfo: [version="+version+",url="+url+"]");
        }
    }

    public static class UpgradeStateInfo {
        int state;
        String msg;
        UpgradeStateInfo(int state, String msg) {
            this.state = state;
            this.msg = msg;
        }

        public void update(int state, String msg) {
            this.state = state;
            this.msg = msg;
        }
    }

    private enum DeviceType {
        ROOMHUB,
        FAN,
        MANOMETER
    }

    private String uuid;
    private DeviceType deviceType = DeviceType.ROOMHUB;
    private String name;
    private String currentVersion;
    private OTAState currentState = OTAState.IDLE;
    private NewVersionInfo newVersionInfo;
    private UpgradeStateInfo upgradeStateInfo;
    //private int upgradeState = 0;
    private long checkTimestamp;    //check version timestamp
    private long updateTimestamp;   //update timestamp
    private boolean doUpgrade = false;
    private boolean isOwner = false;
    private boolean isAllJoyn = false;
    private boolean userConfirmAutoUpdate = false;
    private boolean pendingUpdate = false;
    private boolean forceUpdate = false;
    private boolean mIsOnLine=false;

    private OTAManager.OTAManagerCallback otaManagerCallback;
    private LinkedHashSet<OTAStateChangeListener> stateChangeListeners = new LinkedHashSet<>();

    OTADevice(OTAManager.OTAManagerCallback callback) {
        this.otaManagerCallback = callback;
    }

    public boolean isOnLine() {
        return mIsOnLine;
    }

    public void setIsOnLine(boolean mIsOnLine) {
        this.mIsOnLine = mIsOnLine;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getUuid() {
        return uuid;
    }

    public synchronized String getName() {
        return name;
    }

    public synchronized void setName(String name) {
        this.name = name;
    }

    public synchronized String getCurrentVersion() {
        return currentVersion;
    }

    public synchronized void setCurrentVersion(String currentVersion) {
        this.currentVersion = currentVersion;
    }

    public synchronized OTAState getCurrentState() {
        return currentState;
    }

    public synchronized void setCurrentState(OTAState currentState) {
        this.currentState = currentState;
    }

    public synchronized NewVersionInfo getNewVersionInfo() {
        return newVersionInfo;
    }

    public synchronized void setNewVersionInfo(NewVersionInfo newVersionInfo) {
        this.newVersionInfo = newVersionInfo;
    }

    public UpgradeStateInfo getUpgradeStateInfo() {
        return upgradeStateInfo;
    }

    public void setUpgradeStateInfo(UpgradeStateInfo upgradeStateInfo) {
        this.upgradeStateInfo = upgradeStateInfo;
        notifyStateChange();
    }

    public synchronized long getCheckTimestamp() {
        return checkTimestamp;
    }

    public synchronized void setCheckTimestamp(long checkTimestamp) {
        this.checkTimestamp = checkTimestamp;
    }

    public long getUpdateTimestamp() {
        return updateTimestamp;
    }

    public void setUpdateTimestamp(long updateTimestamp) {
        this.updateTimestamp = updateTimestamp;
    }

    public synchronized boolean isDoUpgrade() {
        return doUpgrade;
    }

    public synchronized void setDoUpgrade(boolean doUpgrade) {
        this.doUpgrade = doUpgrade;
    }

    public synchronized boolean isOwner() {
        return isOwner;
    }

    public synchronized void setIsOwner(boolean isOwner) {
        this.isOwner = isOwner;
    }

    public boolean isAllJoyn() {
        return isAllJoyn;
    }

    public void setIsAllJoyn(boolean isAllJoyn) {
        this.isAllJoyn = isAllJoyn;
    }

    public synchronized boolean isUserConfirmAutoUpdate() {
        return userConfirmAutoUpdate;
    }

    public synchronized void setUserConfirmAutoUpdate(boolean userConfirmAutoUpdate) {
        this.userConfirmAutoUpdate = userConfirmAutoUpdate;
    }

    public synchronized boolean isPendingUpdate() {
        return pendingUpdate;
    }

    public synchronized void setPendingUpdate(boolean pendingUpdate) {
        this.pendingUpdate = pendingUpdate;
    }

    public boolean isForceUpdate() {
        return forceUpdate;
    }

    public void setForceUpdate(boolean forceUpdate) {
        this.forceUpdate = forceUpdate;
    }

    public void upgradeDone() {
        // TODO: notify state change?
        setCurrentState(OTAState.READY);
        if(this.newVersionInfo != null)
            setCurrentVersion(this.newVersionInfo.getVersion());
        setNewVersionInfo(null);
        setCheckTimestamp(0);
        setDoUpgrade(false);
    }

    public void upgradeFail() {
        // TODO: state roll back to READY?
        setCurrentState(OTAState.READY);
        setDoUpgrade(false);
    }

    public boolean hasExpired(long currentTime) {
        if(currentTime > checkTimestamp)
            return true;
        return false;
    }

    public synchronized boolean isUpgrading() {
        return doUpgrade;
    }

    public synchronized boolean isChecking() {
        return currentState==OTAState.VERIFY;
    }

    public void checkVersion() {
        notifyCheckVersionStart();
        setCheckTimestamp(System.currentTimeMillis());
        setCurrentState(OTAState.VERIFY);
        VersionCheckUpdateReqPack versionCheckUpdateReqPack = new VersionCheckUpdateReqPack();
        versionCheckUpdateReqPack.setUuid(uuid);
        versionCheckUpdateReqPack.setVersion(currentVersion);
        otaManagerCallback.checkVersion(this, versionCheckUpdateReqPack);
        notifyCheckVersionDone();
    }

    public void doUpgrade() {
        setCurrentState(OTAState.VERIFY_DONE);
        if(this.upgradeStateInfo == null)
            this.upgradeStateInfo = new UpgradeStateInfo(0,"");
        else
            this.upgradeStateInfo.state = 0;
        setDoUpgrade(true);

        /* version >= 1.1.09 use newer function */
        if(Utils.checkFirmwareVersion(getCurrentVersion(),"1.1.09", false)==true)
            otaManagerCallback.upgrade(uuid, newVersionInfo.getUrl(), newVersionInfo.getMd5());
        else
            otaManagerCallback.upgrade(uuid, newVersionInfo.getUrl());
    }

    public void handleUpgradeTimeout() {
        setDoUpgrade(false);
        setCurrentState(OTAState.READY);
        notifyStateChangeTimeout();
    }

    public void registerStateChangeListener(OTAStateChangeListener listener) {
        synchronized (stateChangeListeners) {
            stateChangeListeners.add(listener);
        }
    }

    public void unregisterStateChangeListener(OTAStateChangeListener listener) {
        synchronized (stateChangeListeners) {
            stateChangeListeners.remove(listener);
        }
    }

    private void notifyCheckVersionStart() {
        for(OTAStateChangeListener listener: stateChangeListeners) {
            listener.checkVersionStart();
        }
    }

    private void notifyCheckVersionDone() {
        for(OTAStateChangeListener listener: stateChangeListeners) {
            listener.checkVersionDone(getNewVersionInfo());
        }
    }

    private void notifyStateChange() {
        for(OTAStateChangeListener listener: stateChangeListeners) {
            listener.upgradeStateChange(this.upgradeStateInfo.state);
        }
    }

    private void notifyStateChangeTimeout() {
        for(OTAStateChangeListener listener: stateChangeListeners) {
            listener.upgradeStateChangeTimeout(this.upgradeStateInfo.state);
        }
    }

}
