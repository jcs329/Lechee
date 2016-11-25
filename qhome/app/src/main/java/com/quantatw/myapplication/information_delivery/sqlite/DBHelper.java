package com.quantatw.myapplication.information_delivery.sqlite;

import static com.quantatw.myapplication.information_delivery.sqlite.DBConstants.TABLE_NAME;
import static com.quantatw.myapplication.information_delivery.sqlite.DBConstants.TABLE_NAME_COUNT;
import static com.quantatw.myapplication.information_delivery.sqlite.DBConstants.MessageCount;
import static android.provider.BaseColumns._ID;
import static com.quantatw.myapplication.information_delivery.sqlite.DBConstants.InfoTopic;
import static com.quantatw.myapplication.information_delivery.sqlite.DBConstants.InfoContent;
import static com.quantatw.myapplication.information_delivery.sqlite.DBConstants.InfoNote;
import static com.quantatw.myapplication.information_delivery.sqlite.DBConstants.InfoMailto;
import static com.quantatw.myapplication.information_delivery.sqlite.DBConstants.InfoMailfrom;
import static com.quantatw.myapplication.information_delivery.sqlite.DBConstants.InfoReceivetime;
import static com.quantatw.myapplication.information_delivery.sqlite.DBConstants.InfoURL;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

public class DBHelper extends SQLiteOpenHelper {
	
	private final static String DATABASE_NAME = "info.db";
	private final static int DATABASE_VERSION = 3;
	private final static String TAG = "SQL_Debug";
	public DBHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		this.getWritableDatabase();
	}

	public void close(){
		this.getWritableDatabase().close();
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.d(TAG,"onCreate");
		final String INIT_TABLE = "CREATE TABLE " + TABLE_NAME + " (" +
								  _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
				InfoTopic + " CHAR, " +
				InfoContent + " CHAR, " +
				InfoNote + " CHAR, " +
				InfoMailto + " CHAR, " +
				InfoMailfrom + " CHAR, " +
				InfoReceivetime + " CHAR, " +
				InfoURL + " CHAR, " +
				"unread" + " INTEGER);";
		db.execSQL(INIT_TABLE);
		final String INIT_TABLE_COUNT = "CREATE TABLE " + TABLE_NAME_COUNT + " (" +
                _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                MessageCount + " INTEGER);";
		db.execSQL(INIT_TABLE_COUNT);
		
		/*String statement="insert into infocenter(topic,content,note)values('infocomm',' Community dance, Welcome','http://219.87.191.181:8080/1.png');";
		db.execSQL(statement);
		statement="insert into infocenter(topic,content,note)values('infocomm',' You have mail from your friend','http://219.87.191.181:8080/2.png');";
		db.execSQL(statement);
		statement="insert into infocenter(topic,content,note)values('infocomm',' Delicious food','http://219.87.191.181:8080/3.png');";
		db.execSQL(statement);
		statement="insert into infocenter(topic,content,note)values('infocomm',' Heavy rain','http://219.87.191.181:8080/4.png');";
		db.execSQL(statement);
		*/
	}

	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		final String DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
		Log.d(TAG,"onUpgrade");
		db.execSQL(DROP_TABLE);
		
		final String DROP_TABLE_COUNT = "DROP TABLE IF EXISTS " + TABLE_NAME_COUNT;
		db.execSQL(DROP_TABLE_COUNT);
		onCreate(db);
	}

	public void add(String infotopic,String infocontent,String infonote, String mailto, String mailfrom, String receive_time, String URL){
		Log.d(TAG,infotopic+":"+ infocontent+":"+infonote);
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		if (infotopic != null)
			values.put(InfoTopic, infotopic);
		if (infocontent != null)
			values.put(InfoContent, infocontent);
		if (infonote != null)
			values.put(InfoNote, infonote);
		if (mailto != null)
			values.put(InfoMailto, mailto);
		if (mailfrom != null)
			values.put(InfoMailfrom, mailfrom);
		if (receive_time != null)
			values.put(InfoReceivetime, receive_time);
		if (URL != null)
			values.put(InfoURL, URL);
		values.put("unread", "1");
		String SqlStatement= "select * from infocenter where URL=" + "'" +URL + "'";
		if ((db.rawQuery(SqlStatement, null).getCount()) == 0) {
   			db.insert(TABLE_NAME, null, values);
		}
	}

	public Cursor getCursor(String infotopic){
		Log.d(TAG, "fragment getCursor " +infotopic);
		SQLiteDatabase db = this.getReadableDatabase();
		String[] columns = {_ID,InfoContent,InfoNote,InfoMailto, InfoMailfrom, "unread", InfoURL};
		String condtion = InfoTopic + "=? ";//+ " and " + InfoNote + "=?";
		String[] conditionequal = {infotopic};
		Cursor cursor = db.query(TABLE_NAME, columns, condtion, conditionequal, null, null, "receive_time desc","8");
		Log.d(TAG, "fragment getCursor");
		return cursor;
	}

	
	public int getTableUnreadCount(){
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.query(TABLE_NAME, null, "unread=1", null, null, null, null);
		int cnt=cursor.getCount();
		return cnt;
	}

	public int getTablePartialUnreadCount(String topic){
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.query(TABLE_NAME, null, "topic=? AND unread=1", new String[]{topic}, null, null, null);
		int cnt=cursor.getCount();
		return cnt;
	}

	public int getTableCount(){
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null, null);
		int cnt=cursor.getCount();
		return cnt;
	}
	
	public ArrayList<ArrayList<String>> get2DimData(String infotopic){
		Log.d(TAG, "fragment getData "+ infotopic);
		ArrayList<ArrayList<String>> infoarray = new ArrayList<>();
		Cursor cursor = getCursor(infotopic);
		ArrayList<String> list1 = new ArrayList<>();
		ArrayList<String> list2 = new ArrayList<>();
		ArrayList<String> list3 = new ArrayList<>();
		ArrayList<String> list4 = new ArrayList<>();
		ArrayList<String> list5 = new ArrayList<>();
		ArrayList<String> list6 = new ArrayList<>();
		ArrayList<String> list7 = new ArrayList<>();
		Log.d(TAG, "fragment getData");
		if (cursor != null) {
			Log.d(TAG, "fragment getData rom unmber = "+ cursor.getCount());
			while (cursor.moveToNext()) {
				String id = cursor.getString(0);
				String content = cursor.getString(1);
				String note = cursor.getString(2);
				String mailto = cursor.getString(3);
				String mailfrom = cursor.getString(4);
				String unread = cursor.getString(5);
				String URL = cursor.getString(6);
				Log.d(TAG, "fragment getData" + content + ", note = " + note);
				list1.add( id);
				list2.add( content);
				list3.add( note);
				list4.add( mailto);
				list5.add( mailfrom);
				list6.add( unread);
				list7.add( URL);
			}
			infoarray.add(0,list1);
			infoarray.add(1,list2);
			infoarray.add(2,list3);
			infoarray.add(3,list4);
			infoarray.add(4,list5);
			infoarray.add(5,list6);
			infoarray.add(6,list7);
			Log.d(TAG, "fragment onclick listview infoarray 0 = "+ infoarray.get(0));
			Log.d(TAG, "fragment onclick listview infoarray 1 = "+ infoarray.get(1));
			Log.d(TAG, "fragment onclick listview infoarray 2 = "+ infoarray.get(2));
		}else{
			Log.d(TAG, "fragment getData null");
			cursor.close();
			return null;
		}
		cursor.close();
		return infoarray;

	}


	public void del(){
		//String Id
		//String id = Id;

		SQLiteDatabase db = this.getWritableDatabase();
		//db.delete(TABLE_NAME, _ID + "=" + id, null);
		db.delete(TABLE_NAME,null,null);
	}

	public void update(String Id){
		String id = Id;

		ContentValues values = new ContentValues();
		values.put("unread", "0");
		

		SQLiteDatabase db = this.getWritableDatabase();
		db.update(TABLE_NAME, values, _ID + "=" + id, null);
	}
    /*SQL*/

}
