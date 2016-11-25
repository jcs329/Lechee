package com.quantatw.roomhub.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by 95010915 on 2015/9/23.
 */
public class PreferenceEditor {
    SharedPreferences mPref;
    Context mContext;

    public PreferenceEditor(Context context, String prefName) {
        mContext = context;
        mPref = context.getSharedPreferences(prefName, Context.MODE_PRIVATE);
    }

    public void setStringValue(String key, String value) {
        SharedPreferences.Editor editor = mPref.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public String getStringValue(String key) {
        String value = mPref.getString(key, "");
        return value;
    }

    public void setIntValue(String key, int value) {
        SharedPreferences.Editor editor = mPref.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    public int getIntValue(String key) {
        int value = mPref.getInt(key, 0);
        return value;
    }
}
