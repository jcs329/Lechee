package com.quantatw.roomhub.manager.health.bpm;

import android.content.ContentUris;
import android.graphics.Color;
import android.text.TextUtils;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by erin on 5/18/16.
 */
public class BPMUtils {
    public static void setSystolicValue(int systolicValue, TextView textView) {
        textView.setText(Integer.toString(systolicValue));
        if(systolicValue > 130)
            textView.setTextColor(Color.RED);
        else if(systolicValue < 99)
            textView.setTextColor(Color.GREEN);
        else
            textView.setTextColor(Color.WHITE);
    }

    public static void setDialstolicValue(int dialstolic, TextView textView) {
        textView.setText(Integer.toString(dialstolic));
        if(dialstolic > 90)
            textView.setTextColor(Color.RED);
        else if(dialstolic < 65)
            textView.setTextColor(Color.GREEN);
        else
            textView.setTextColor(Color.WHITE);
    }

    public static void setHeartRateValue(int heartRate, TextView textView) {
        textView.setText(Integer.toString(heartRate));
        if(heartRate > 96)
            textView.setTextColor(Color.RED);
        else if(heartRate < 64)
            textView.setTextColor(Color.GREEN);
        else
            textView.setTextColor(Color.WHITE);
    }

    public static Date getDateBeforeOrAfter(Date curDate, int iDate) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(curDate);
        cal.add(Calendar.DAY_OF_MONTH, iDate);
        return cal.getTime();
    }

    public static boolean isToday(Date date){
        boolean b = false;
        Date today = new Date();
        if(date != null){
            SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd");
            String nowDate = DATE_FORMAT.format(today);
            String timeDate = DATE_FORMAT.format(date);
            if(nowDate.equals(timeDate)){
                b = true;
            }
        }
        return b;
    }

    private static Date getFormatDate(String currDate, String format) {
        final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
        if (currDate == null) {
            return null;
        }
        SimpleDateFormat dtFormatdB = null;
        try {
            dtFormatdB = new SimpleDateFormat(format);
            return dtFormatdB.parse(currDate);
        } catch (Exception e) {
            dtFormatdB = new SimpleDateFormat(DATE_FORMAT);
            try {
                return dtFormatdB.parse(currDate);
            } catch (Exception ex) {
            }
        }
        return null;
    }

    private static String getFormatDate(Date currDate, String format) {
        final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
        if (currDate == null) {
            return "";
        }
        SimpleDateFormat dtFormatdB = null;
        try {
            dtFormatdB = new SimpleDateFormat(format);
            return dtFormatdB.format(currDate);
        } catch (Exception e) {
            dtFormatdB = new SimpleDateFormat(DATE_FORMAT);
            try {
                return dtFormatdB.format(currDate);
            } catch (Exception ex) {
            }
        }
        return null;
    }

    private static Date getFormatDateTime(String currDate, String format) {
        final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
        if (currDate == null) {
            return null;
        }
        SimpleDateFormat dtFormatdB = null;
        try {
            dtFormatdB = new SimpleDateFormat(format);
            return dtFormatdB.parse(currDate);
        } catch (Exception e) {
            dtFormatdB = new SimpleDateFormat(DATE_FORMAT);
            try {
                return dtFormatdB.parse(currDate);
            } catch (Exception ex) {
            }
        }
        return null;
    }

    private static int getPeriod(String dateTimeString, Date dateTime, String format) {
        final int TIME_DAY_MILLISECOND = 86400000;

        Date d1 = getFormatDateTime(dateTimeString, format);
        Date d2 = getFormatDateTime(getFormatDate(dateTime, format), format);

        if(d1 != null && d2 != null) {
            Long mils = (d2.getTime() - d1.getTime()) / (TIME_DAY_MILLISECOND);
            return mils.intValue();
        }
        return -1;
    }

    public static String getDateString(String currDate, String format) {
        final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

        if (TextUtils.isEmpty(currDate)) {
            return null;
        }

        Date d1 = getFormatDateTime(currDate, DATE_FORMAT);

        SimpleDateFormat dtFormatdB = null;
        try {
            dtFormatdB = new SimpleDateFormat(format);
            return dtFormatdB.format(d1);
        } catch (Exception e) {
            return "";
        }
    }

    public static int getDaysBetweenDates(String dateTimeString, Date dateTime) {
        final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
        final String T_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";

        int days = getPeriod(dateTimeString,dateTime,DATE_FORMAT);
        if(days < 0) {
            days = getPeriod(dateTimeString,dateTime,T_DATE_FORMAT);
        }

        return days;
    }
}
