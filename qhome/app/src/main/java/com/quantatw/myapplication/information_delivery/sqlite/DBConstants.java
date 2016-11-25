package com.quantatw.myapplication.information_delivery.sqlite;

import android.provider.BaseColumns;

public interface DBConstants extends BaseColumns {
	public static final String TABLE_NAME = "infocenter";
	public static final String TABLE_NAME_COUNT = "infocount";
	
	public static final String InfoTopic = "topic";
	public static final String InfoContent = "content";
	public static final String InfoNote = "note";
	public static final String InfoMailto = "mailto";
	public static final String InfoMailfrom = "mailfrom";
	public static final String InfoReceivetime = "receive_time";
	public static final String InfoURL = "URL";
	public static final String MessageCount = "count";
}
