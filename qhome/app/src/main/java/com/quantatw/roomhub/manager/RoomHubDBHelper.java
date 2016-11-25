package com.quantatw.roomhub.manager;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;

import com.quantatw.roomhub.utils.Utils;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by 95011613 on 2015/9/24.
 */
public class RoomHubDBHelper extends SQLiteOpenHelper {
    private static final String TAG = "RoomHubDBHelper";
    private final static String DATABASE_NAME = "roomhub.db";
    private static final int DATABASE_VERSION=9;

    private Context mContext;

    private static final HashSet<String> mValidTables = new HashSet<String>();

    private static final String TABLE_ACCOUNTS= "accounts";
    private static final String TABLE_PROFILES = "profiles";
    private static final String TABLE_NOTIFICATION = "notification";
    private static final String TABLE_NOTICE_SETTING = "notice_setting";
    private static final String TABLE_BLOOD_PRESSURE = "blood_pressure";
    private static final String TABLE_AC_TOGGLE = "ac_toggle";

    public static final String ACTION_NOTIFICATOIN_CONTENT_CHANGED = "andorid.intent.action.notification_content_changed";

    static {
        mValidTables.add(TABLE_ACCOUNTS);
        mValidTables.add(TABLE_PROFILES);
        mValidTables.add(TABLE_NOTIFICATION);
    }
    private static SQLiteDatabase database;

    public RoomHubDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
    }
/*
    public static SQLiteDatabase getDatabase(Context context) {

        if (database == null || !database.isOpen()) {
            database = new RoomHubDBHelper(context, DATABASE_NAME,null, DATABASE_VERSION).getWritableDatabase();
        }

        return database;
    }
*/

    private void createAccountsTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_ACCOUNTS + "(" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "account TEXT," +
                "name TEXT" +
                ");");
    }

    private void createProfilesTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_PROFILES + "(" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "account_id INTEGER," +
                "uuid TEXT," +
                "function_mode INTEGER," +
                "temperature INTEGER," +
                "swing INTEGER," +
                "fan INTEGER," +
                "selected INTEGER" +
                ");");
    }

    public static final class Notification {
        public static final String UUID = "uuid";
        public static final String MESSAGE_ID = "message_id";
        public static final String MESSAGE = "message";
        public static final String SENDER_ID = "sender_id";
        public static final String TIMESTAMP = "timestamp";
        public static final String EXPIRE_TIMESTAMP= "expire_timestamp";
        public static final String BUTTON1_TYPE = "button1_type";
        public static final String BUTTON1_LABEL = "button1_label";
        public static final String BUTTON1_HANDLEID = "button1_handleId";
        public static final String BUTTON2_TYPE = "button2_type";
        public static final String BUTTON2_LABEL = "button2_label";
        public static final String BUTTON2_HANDLEID = "button2_handleId";
        public static final String BUTTON3_TYPE = "button3_type";
        public static final String BUTTON3_LABEL = "button3_label";
        public static final String BUTTON3_HANDLEID = "button3_handleId";
        public static final String MINOR_MESSAGE = "minor_message";
        public static final String SUGGESTION = "suggestion";
        public static final String ASSET_UUID = "assetUuid";
        public static final String GCM_MESSAGE_TYPE = "msgType";
    }

    private void createNotificationTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NOTIFICATION + "(" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                Notification.UUID + " TEXT," +
                Notification.MESSAGE_ID + " INTEGER," +
                Notification.MESSAGE + " TEXT," +
                Notification.SENDER_ID + " INTEGER," +
                Notification.TIMESTAMP + " DATETIME," +
                Notification.EXPIRE_TIMESTAMP + " DATETIME," +
                Notification.BUTTON1_TYPE + " INTEGER," +
                Notification.BUTTON1_LABEL + " TEXT," +
                Notification.BUTTON1_HANDLEID + " INTEGER," +
                Notification.BUTTON2_TYPE + " INTEGER," +
                Notification.BUTTON2_LABEL + " TEXT," +
                Notification.BUTTON2_HANDLEID + " INTEGER," +
                Notification.BUTTON3_TYPE + " INTEGER," +
                Notification.BUTTON3_LABEL + " TEXT," +
                Notification.BUTTON3_HANDLEID + " INTEGER," +
                Notification.MINOR_MESSAGE + " TEXT," +
                Notification.SUGGESTION + " TEXT" +
                ");");

    }

    private void createNoticeSetting(SQLiteDatabase db) {

        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NOTICE_SETTING + "(" +
                RoomHubDB.Notice_Setting._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                RoomHubDB.Notice_Setting.UUID + " TEXT," +
                RoomHubDB.Notice_Setting.SWITCH_ON_OFF + " INTEGER," +
                RoomHubDB.Notice_Setting.TIME + " INTEGER," +
                RoomHubDB.Notice_Setting.DELTA + " INTEGER," +
                RoomHubDB.Notice_Setting.IS_DEFAULT_TIME + " INTEGER DEFAULT 1," +
                RoomHubDB.Notice_Setting.IS_DEFAULT_DELTA + " INTEGER DEFAULT 1" +
                ");");
    }

    /*
    private void createBloodPressure(SQLiteDatabase db) {

        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_BLOOD_PRESSURE + "(" +
                RoomHubDB.Blood_Pressure._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                RoomHubDB.Blood_Pressure.UUID + " TEXT," +
                RoomHubDB.Blood_Pressure.MEASURE_TIME + " INTEGER," +
                RoomHubDB.Blood_Pressure.MAX_BLOOD_PRESSURE + " INTEGER," +
                RoomHubDB.Blood_Pressure.MIN_BLOOD_PRESSURE + " INTEGER," +
                RoomHubDB.Blood_Pressure.HEART_RATE + " INTEGER" +
                ");");
    }
    */
    private void createACToggle(SQLiteDatabase db) {

        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_AC_TOGGLE + "(" +
                RoomHubDB.ACToggle._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                RoomHubDB.ACToggle.UUID + " TEXT," +
                RoomHubDB.ACToggle.BRAND_ID + " INTEGER," +
                RoomHubDB.ACToggle.MODEL_ID + " TEXT" +
                ");");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createAccountsTable(db);
        createProfilesTable(db);
        createNotificationTable(db);
        createNoticeSetting(db);
//        createBloodPressure(db);
        createACToggle(db);

        InsertAccount(db, RoomHubDB.Account.DEFAULT_ACCOUNT, RoomHubDB.Account.DEFAULT_ACCOUNT);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if(newVersion > oldVersion) {
            boolean success = false;
            db.beginTransaction();
            switch(oldVersion){
                case 1:
                    db.execSQL("ALTER TABLE " + TABLE_PROFILES+" ADD COLUMN selected INTEGER DEFAULT 0");
                    success=true;
                    break;
                case 2:
                    createNotificationTable(db);
                    success=true;
                    break;
                case 3:
                    createNoticeSetting(db);
                    success=true;
                    break;
                case 4:
                //    createBloodPressure(db);
                    success=true;
                    break;
                case 5:
                    db.execSQL("ALTER TABLE "+TABLE_NOTICE_SETTING+" ADD COLUMN "+RoomHubDB.Notice_Setting.IS_DEFAULT_TIME+" INTEGER DEFAULT 1");
                    db.execSQL("ALTER TABLE "+TABLE_NOTICE_SETTING+" ADD COLUMN "+RoomHubDB.Notice_Setting.IS_DEFAULT_DELTA+" INTEGER DEFAULT 1");
                    success=true;
                    break;
                case 6:
                    createACToggle(db);
                    success=true;
                    break;
                case 7:
                    db.execSQL("DROP TABLE IF EXISTS " + TABLE_BLOOD_PRESSURE );
                    success=true;
                    break;
                case 8:
                    db.execSQL("ALTER TABLE "+TABLE_NOTIFICATION+" ADD COLUMN "+Notification.ASSET_UUID+" TEXT");
                    db.execSQL("ALTER TABLE "+TABLE_NOTIFICATION+" ADD COLUMN "+ Notification.GCM_MESSAGE_TYPE+" TEXT");
                    success=true;
                    break;
            }
            if (success) {
                db.setTransactionSuccessful();
            }
            db.endTransaction();
           // db.execSQL("DROP TABLE IF EXISTS accounts");
            //db.execSQL("DROP TABLE IF EXISTS profiles");
        }else
            onCreate(db);
    }

    public boolean IsExistAccount(SQLiteDatabase db,String account){
        String sql_str="SELECT count(*) FROM accounts WHERE account=?";

        Cursor cursor = db.rawQuery(sql_str, new String[]{account});
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();

        if(count>0)
            return true;

        return false;
    }

    public void InsertAccount(SQLiteDatabase db,String account,String name){

        if(!IsExistAccount(db,account)) {
            db.beginTransaction();

            ContentValues values = new ContentValues();

            values.put(RoomHubDB.Account.ACCOUNT, account);
            values.put(RoomHubDB.Account.NAME, name);

            try {
                db.insert(TABLE_ACCOUNTS, null, values);
                db.setTransactionSuccessful();
            } catch (Exception e) {
            } finally {
                db.endTransaction();
              //  db.close();
            }
        }
    }

    private int getAccountId(SQLiteDatabase db,String account){
        String sql_str="SELECT _id FROM accounts WHERE account='"+account+"'";
        Cursor c = db.rawQuery(sql_str,null);
        int value=-1;
        c.moveToFirst();

        if ((c != null) && (c.getCount() > 0)) {
            value=c.getInt(0);
        }

        c.close();
        return value;
    }

    public ArrayList<RoomHubProfile> getProfileList(SQLiteDatabase db,String uuid,String account){
        ArrayList<RoomHubProfile> profile_list=new ArrayList<RoomHubProfile>();

        String sql_str="SELECT a._id as account_id,b.uuid,b.function_mode,b.temperature,b.swing,b.fan " +
                "FROM accounts a INNER JOIN profiles b ON a._id = b.account_id " +
                "WHERE (((a.account)=?) AND ((b.uuid)=?)) order by b.function_mode";

        Cursor c = db.rawQuery(sql_str,new String[] {account, uuid });
        while (c.moveToNext()) {
            RoomHubProfile profile=new RoomHubProfile();
            profile.setAccountId(c.getInt(0));
            profile.setUuid(c.getString(1));
            profile.setFunMode(c.getInt(2));
            profile.setTemp(c.getInt(3));
            profile.setSwing(c.getInt(4));
            profile.setFan(c.getInt(5));
            profile_list.add(profile);
        }
        c.close();
        return profile_list;
    }

    public RoomHubProfile getProfileByFunMode(SQLiteDatabase db,String uuid,String account,int fun_mode){
        String sql_str="SELECT a._id as account_id,b.uuid,b.function_mode,b.temperature,b.swing,b.fan " +
                "FROM accounts a INNER JOIN profiles b ON a._id = b.account_id " +
                "WHERE (((a.account)=?) AND ((b.uuid)=?) AND ((b.function_mode)=?))";

        Cursor c = db.rawQuery(sql_str,new String[] {account, uuid,String.valueOf(fun_mode)});
        RoomHubProfile profile=null;
        if ((c != null) && (c.getCount() > 0)) {
            c.moveToFirst();
            profile=new RoomHubProfile();
            profile.setAccountId(c.getInt(0));
            profile.setUuid(c.getString(1));
            profile.setFunMode(c.getInt(2));
            profile.setTemp(c.getInt(3));
            profile.setSwing(c.getInt(4));
            profile.setFan(c.getInt(5));
        }

        c.close();
        return profile;
    }

    public RoomHubProfile getProfileBySelected(SQLiteDatabase db,String uuid,String account){
        String sql_str="SELECT a._id as account_id,b.uuid,b.function_mode,b.temperature,b.swing,b.fan " +
                "FROM accounts a INNER JOIN profiles b ON a._id = b.account_id " +
                "WHERE (((a.account)=?) AND ((b.uuid)=?) AND ((b.selected)=1))";

        Cursor c = db.rawQuery(sql_str,new String[] {account, uuid});
        RoomHubProfile profile=null;
        if ((c != null) && (c.getCount() > 0)) {
            c.moveToFirst();
            profile=new RoomHubProfile();
            profile.setAccountId(c.getInt(0));
            profile.setUuid(c.getString(1));
            profile.setFunMode(c.getInt(2));
            profile.setTemp(c.getInt(3));
            profile.setSwing(c.getInt(4));
            profile.setFan(c.getInt(5));
        }

        c.close();
        return profile;
    }

    public void UpdateProfileData(SQLiteDatabase db,String uuid,String account,int fun_mode,String[] field,int[] field_value){
        RoomHubProfile profile=getProfileByFunMode(db,uuid,account,fun_mode);

        db.execSQL("update profiles set selected=0 where uuid='" + uuid + "' and function_mode <> " + fun_mode);
        ContentValues cv = new ContentValues();
        cv.put(RoomHubDB.Profile.SELECTED, 1);

        for(int i=0;i<field.length;i++) {
            cv.put(field[i], field_value[i]);
        }
        if(profile!=null){
            String whereClause="uuid='"+uuid+"' and function_mode="+fun_mode+
                    " and account_id IN (SELECT _id FROM accounts WHERE account='"+account+"')";

            db.update(TABLE_PROFILES,cv,whereClause,null);
        }else{
            int account_id=getAccountId(db, account);
            if(account_id>0) {
                db.beginTransaction();
              //  ContentValues cv = new ContentValues();
                cv.put(RoomHubDB.Profile.UUID, uuid);
                cv.put(RoomHubDB.Profile.ACCOUNT_ID, account_id);
                cv.put(RoomHubDB.Profile.FUN_MODE, fun_mode);
                //cv.put(RoomHubDB.Profile.SELECTED,1);
                /*
                for(int i=0;i<field.length;i++) {
                    cv.put(field[i], field_value[i]);
                }
                */
                try {
                    db.insert(TABLE_PROFILES, null, cv);
                    db.setTransactionSuccessful();
                } catch (Exception e) {
                } finally {
                    db.endTransaction();
                }
            }
//            db.close();
        }

    }

    /*
     * Notification
      */
    public Cursor notificationQuery(SQLiteDatabase db) {
        notificationDeleteExpiredMsg(db);

        String sql_str="SELECT * from "+TABLE_NOTIFICATION+" ORDER BY "+Notification.TIMESTAMP+" DESC";

        Cursor c = db.rawQuery(sql_str,null);
        return c;
    }

    public void notificationInsert(SQLiteDatabase db, ContentValues contentValues) {
        db.beginTransaction();

        try {
            if(updateRecordIfExist(db, contentValues) == 0) {
                db.insert(TABLE_NOTIFICATION, null, contentValues);
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
            notificationContentChanged();
            //  db.close();
        }
    }

    public void notificationUpdate(SQLiteDatabase db, ContentValues contentValues) {

    }

    public void notificationDelete(SQLiteDatabase db, int index) {
        db.beginTransaction();
        try {
            db.delete(TABLE_NOTIFICATION, "_id=?", new String[]{Integer.toString(index)});
            db.setTransactionSuccessful();
        } catch (Exception e) {
        } finally {
            db.endTransaction();
            notificationContentChanged();
            //  db.close();
        }
    }

    public void notificationDeleteAll(SQLiteDatabase db) {
        db.beginTransaction();
        try {
            db.delete(TABLE_NOTIFICATION, null, null);
            db.setTransactionSuccessful();
        } catch (Exception e) {
        } finally {
            db.endTransaction();
            notificationContentChanged();
            //  db.close();
        }
    }

    private void notificationDeleteExpiredMsg(SQLiteDatabase db) {
        final long currentTime = System.currentTimeMillis();
        Log.d(TAG, "notificationDeleteExpiredMsg getTime currentTime=" + currentTime);
        /* ignore gcm message: expire timestamp -999 */
        String where = Notification.EXPIRE_TIMESTAMP +">0 and "+ currentTime+">"+Notification.EXPIRE_TIMESTAMP;
        db.beginTransaction();
        try {
            int ret = db.delete(TABLE_NOTIFICATION,where,null);
            Log.d(TAG, "notificationDeleteExpiredMsg getTime ret=" + ret);
            db.setTransactionSuccessful();
        } catch (Exception e) {
        } finally {
            db.endTransaction();
        }
    }

    private int updateRecordIfExist(SQLiteDatabase db, ContentValues values) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Notification.MESSAGE, values.getAsString(Notification.MESSAGE));
        contentValues.put(RoomHubDBHelper.Notification.SENDER_ID, values.getAsInteger(Notification.SENDER_ID));
        contentValues.put(RoomHubDBHelper.Notification.TIMESTAMP, values.getAsLong(Notification.TIMESTAMP));
        contentValues.put(RoomHubDBHelper.Notification.EXPIRE_TIMESTAMP, values.getAsLong(Notification.EXPIRE_TIMESTAMP));
        contentValues.put(RoomHubDBHelper.Notification.BUTTON1_TYPE, values.getAsInteger(Notification.BUTTON1_TYPE));
        contentValues.put(RoomHubDBHelper.Notification.BUTTON1_LABEL,
                values.getAsString(Notification.BUTTON1_LABEL));
        contentValues.put(RoomHubDBHelper.Notification.BUTTON1_HANDLEID, values.getAsInteger(Notification.BUTTON1_HANDLEID));
        contentValues.put(RoomHubDBHelper.Notification.BUTTON2_TYPE, values.getAsInteger(Notification.BUTTON2_TYPE));
        contentValues.put(RoomHubDBHelper.Notification.BUTTON2_LABEL,
                values.getAsString(Notification.BUTTON2_LABEL));
        contentValues.put(RoomHubDBHelper.Notification.BUTTON2_HANDLEID, values.getAsInteger(Notification.BUTTON2_HANDLEID));
        contentValues.put(RoomHubDBHelper.Notification.BUTTON3_TYPE, values.getAsInteger(Notification.BUTTON3_TYPE));
        contentValues.put(RoomHubDBHelper.Notification.BUTTON3_LABEL,
                values.getAsString(Notification.BUTTON3_LABEL));
        contentValues.put(RoomHubDBHelper.Notification.BUTTON3_HANDLEID, values.getAsInteger(Notification.BUTTON3_HANDLEID));
        contentValues.put(Notification.MINOR_MESSAGE, values.getAsString(Notification.MINOR_MESSAGE));
        contentValues.put(Notification.SUGGESTION, values.getAsString(Notification.SUGGESTION));

        final String uuid = values.getAsString(Notification.UUID);
        final int messageId = values.getAsInteger(Notification.MESSAGE_ID);
        final String gcmMessageType = values.getAsString(Notification.GCM_MESSAGE_TYPE);
        int ret=0;

        /* No need to overwrite GCM message */
        if(Utils.isGcmMessageId(messageId))
            return ret;

        if(TextUtils.isEmpty(uuid)) {
            ret = db.update(TABLE_NOTIFICATION, contentValues,
                    "("+Notification.UUID + " is null or "
                    + Notification.UUID + "='')"
                    + " AND " + Notification.MESSAGE_ID + "=" + messageId, null);
        }
        else if(!TextUtils.isEmpty(gcmMessageType)) {
            ret = db.update(TABLE_NOTIFICATION, contentValues, Notification.UUID + "='"
                    + uuid + "' AND " + Notification.MESSAGE_ID + "=" + messageId
                    + "' AND " + Notification.GCM_MESSAGE_TYPE + "=" + gcmMessageType, null);
        }
        else {
            ret = db.update(TABLE_NOTIFICATION, contentValues, Notification.UUID + "='"
                    + uuid + "' AND " + Notification.MESSAGE_ID + "=" + messageId, null);
        }
        Log.d(TAG,"updateRecordIfExist ret="+ret);
        return ret;
    }

    private void notificationContentChanged() {
        Intent intent = new Intent(ACTION_NOTIFICATOIN_CONTENT_CHANGED);
        mContext.sendBroadcast(intent);
    }


    private boolean IsExistNoticeSetting(SQLiteDatabase db,String uuid){
        String sql_str="SELECT count(*) FROM "+TABLE_NOTICE_SETTING+" WHERE uuid=?";

        Cursor cursor = db.rawQuery(sql_str, new String[]{uuid});
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();

        if(count>0)
            return true;

        return false;
    }

    public void InsertNoticeSetting(SQLiteDatabase db,String uuid,NoticeSetting notice_setting){

        ContentValues contentValues = new ContentValues();
        contentValues.put(RoomHubDB.Notice_Setting.UUID, uuid);
        contentValues.put(RoomHubDB.Notice_Setting.SWITCH_ON_OFF, notice_setting.getSwitchOnOff());
        contentValues.put(RoomHubDB.Notice_Setting.TIME, notice_setting.getNoticeTime());
        contentValues.put(RoomHubDB.Notice_Setting.DELTA, notice_setting.getNoticeDelta());
        contentValues.put(RoomHubDB.Notice_Setting.IS_DEFAULT_TIME, notice_setting.getIsDefaultTime());
        contentValues.put(RoomHubDB.Notice_Setting.IS_DEFAULT_DELTA, notice_setting.getIsDefaultDelta());


        Log.d(TAG, "InsertNoticeSetting uuid=" + uuid + " switch_on_off=" + contentValues.getAsInteger(RoomHubDB.Notice_Setting.SWITCH_ON_OFF) +
                " time=" + contentValues.getAsInteger(RoomHubDB.Notice_Setting.TIME) + " delta=" + contentValues.getAsInteger(RoomHubDB.Notice_Setting.DELTA) +
                " is_default_time=" + contentValues.getAsInteger(RoomHubDB.Notice_Setting.IS_DEFAULT_TIME) +
                " is_default_delta="+contentValues.getAsInteger(RoomHubDB.Notice_Setting.IS_DEFAULT_DELTA));

        db.beginTransaction();

        try {
            if(!IsExistNoticeSetting(db,uuid)) {
                long rowId = db.insert(TABLE_NOTICE_SETTING, null, contentValues);
            }else{
                String whereClause="uuid='"+uuid+"'";

                db.update(TABLE_NOTICE_SETTING, contentValues, whereClause,null);
            }

            db.setTransactionSuccessful();
        } catch (Exception e) {
        } finally {
            db.endTransaction();
        }
    }

    public NoticeSetting QueryNoticeSetting(SQLiteDatabase db,String uuid) {

        String sql_str = "SELECT * from " + TABLE_NOTICE_SETTING + " where " + RoomHubDB.Notice_Setting.UUID + "='" + uuid + "'";

        Cursor c = db.rawQuery(sql_str, null);
        NoticeSetting notice_setting = null;
        if ((c != null) && (c.getCount() > 0)) {
            c.moveToFirst();
            Log.d(TAG, "QueryNoticeSetting uuid=" + uuid + " switch_on_off=" + c.getInt(2) + " time=" + c.getInt(3) + " delta=" + c.getInt(4));
            notice_setting = new NoticeSetting(c.getInt(2),c.getInt(3),c.getInt(4));
            notice_setting.setIsDefaultTime(c.getInt(5));
            notice_setting.setIsDefaultDelta(c.getInt(6));
        }

        c.close();
        return notice_setting;
    }
/*
    public Cursor QueryBloodPressure(SQLiteDatabase db,String uuid) {

        String sql_str = "SELECT * from " + TABLE_BLOOD_PRESSURE + " where " + RoomHubDB.Blood_Pressure.UUID + "='" + uuid + "'";

        Cursor c = db.rawQuery(sql_str, null);

        //c.close();
        return c;
    }
*/
    public void InserACToggle(SQLiteDatabase db,String uuid,int brand_id,String model_id){

        Log.d(TAG, "InserACToggle uuid=" + uuid + " brand_id=" + brand_id + " model_id=" + model_id);

        ContentValues contentValues = new ContentValues();
        contentValues.put(RoomHubDB.ACToggle.UUID, uuid);
        contentValues.put(RoomHubDB.ACToggle.BRAND_ID,brand_id);
        contentValues.put(RoomHubDB.ACToggle.MODEL_ID, model_id);

        db.beginTransaction();

        try {
            long rowId = db.insert(TABLE_AC_TOGGLE, null, contentValues);
            Log.d(TAG,"InserACToggle rowId="+rowId);

            db.setTransactionSuccessful();
        } catch (Exception e) {
        } finally {
            db.endTransaction();
        }
    }

    public Cursor QueryACToggle(SQLiteDatabase db,String uuid) {

        String sql_str = "SELECT * from " + TABLE_AC_TOGGLE + " where " + RoomHubDB.ACToggle.UUID + "='" + uuid + "'";

        Cursor c = db.rawQuery(sql_str, null);

        //c.close();
        return c;
    }

    public void DeleteACToggle(SQLiteDatabase db, String uuid) {
        db.beginTransaction();
        try {
            db.delete(TABLE_AC_TOGGLE, "uuid=?", new String[]{uuid});
            db.setTransactionSuccessful();
        } catch (Exception e) {
        } finally {
            db.endTransaction();
            //  db.close();
        }
    }
}
