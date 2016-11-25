package com.quantatw.myapplication;

import android.app.Activity;
import android.content.SharedPreferences;
import android.util.Log;

public class CityPreference {
    public static final String PREFS_NAME = "MyPrefsFile";
     
    SharedPreferences prefs;
     
    public CityPreference(Activity activity){
        prefs = activity.getSharedPreferences(PREFS_NAME, 0);
    }

    // If the user has not chosen a city yet, return
    // Sydney as the default city
    public String getCity(){
        //return prefs.getString("city", "Jerusalem, IS");        
        //return prefs.getString("city", "Sydney, AU");        
        return prefs.getString("city", "Taipei");
    }
     
    void setCity(String city){
        prefs.edit().putString("city", city).commit();
    }

    public String getGuardPhoneNo() {
        return prefs.getString("guard_phone", "q41");
    }

    public String getRTSPip() {
        return prefs.getString("rtsp_ip", "rtsp://184.72.239.149/vod/mp4:BigBuckBunny_115k.mov");
    }

    public void setGuardPhoneNo(String phone) {
        prefs.edit().putString("guard_phone", phone).commit();
    }

    public void setRTSPip(String rtspip) {
        if (rtspip.equals("ip")) rtspip="rtsp://184.72.239.149/vod/mp4:BigBuckBunny_115k.mov";
        prefs.edit().putString("rtsp_ip", rtspip).commit();
    }

    public boolean getSecurityEnabled(){
        return prefs.getBoolean("security", false);
    }

    void setSecurityEnabled(boolean enable){
        prefs.edit().putBoolean("security", enable).commit();
    }

    /* YouJun, 2016/05/24, voice assistant <-- */
    public boolean getVoiceAssistantEnabled() {
        return prefs.getBoolean("voice_assistant", true);
    }

    public void setVoiceAssistantEnabled(boolean enabled) {
        prefs.edit().putBoolean("voice_assistant", enabled).commit();
    }

    public boolean getVoiceAssistantTcpService() {
        return prefs.getBoolean("voice_assistant_tcp", true);
    }

    public void setVoiceAssistantTcpService(boolean enabled) {
        prefs.edit().putBoolean("voice_assistant_tcp", enabled).commit();
    }

    public boolean getVoiceAssistantDebug() {
        return prefs.getBoolean("voice_assistant_debug", false);
    }

    public void setVoiceAssistantDebug(boolean enabled) {
        prefs.edit().putBoolean("voice_assistant_debug", enabled).commit();
    }

    public String getKeywordSensitivity() {
        return prefs.getString("keyword_sensitivity", "29");
    }

    public void setKeywordSensitivity(String sensitivity) {
        prefs.edit().putString("keyword_sensitivity", sensitivity).commit();
    }
    /* YouJun, 2016/05/24, voice assistant --> */
}