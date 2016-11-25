package com.quantatw.roomhub.utils;

/**
 * Created by erin on 6/4/16.
 */
public class SupportVersion {

    private static final String ROOMHUB_SUPPORT_MIN_VERSION = "1.1.30.0";
    private static final String OTA_SUPPORT_MIN_VERSION = "1.1.16.3";

    private static class VerifyVersion {
        String version;

        VerifyVersion(String version) {
            this.version = version;
        }

        boolean check(String compareVersion) {
            return Utils.checkFirmwareVersion(compareVersion,version,false);
        }
    }

    public static class RoomHubVer extends VerifyVersion{
        private static RoomHubVer mInstance;

        private RoomHubVer() {
            super(ROOMHUB_SUPPORT_MIN_VERSION);
        }

        public static boolean isValid(String compareVersion) {
            if(mInstance == null)
                mInstance = new RoomHubVer();
            return mInstance.check(compareVersion);
        }
    }

    public static class OTAVer extends VerifyVersion{
        private static OTAVer mInstance;

        private OTAVer() {
            super(OTA_SUPPORT_MIN_VERSION);
        }

        public static boolean isValid(String compareVersion) {
            if(mInstance == null)
                mInstance = new OTAVer();
            return mInstance.check(compareVersion);
        }
    }

}
