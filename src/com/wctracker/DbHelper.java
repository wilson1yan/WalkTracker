package com.wctracker;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * The class that helps open up and use the SQLite Database
 * @author Wilson Yan
 *
 */
public class DbHelper extends SQLiteOpenHelper{
	public static final String DATABASE_NAME = "walking_database.db";
	public static int DATABASE_VERSION = 1;
	
	public static final String LATITUDE = "latitude";
	public static final String LONGITUDE = "longitude";
	
	public static final String LOGS_TABLE = "logs";
	public static final String CALORIES_BURNED = "calories";
	public static final String DISTANCE = "distance";
	public static final String TIME = "time";
	public static final String DATE = "date";
	public static final String MEASUREMENT = "measurement";
	public static final String ID = "_id";
	public static final String POINTS = "points";
	public static final String ID_NUM = "walk_id";
	
	public static final String WALKING_GEOPOINT = "walkgeopoints";
	public static final String WALK_ID = "walk_id";
	Context mContext;
	/**
	 * Constructor
	 * @param context
	 */
	public DbHelper(Context context){
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		this.mContext = context;
	}
	
	/**
	 * Overrided onCreate(), called when database crated for the first time
	 * creates Geo Point Table and Log Table
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {		
		db.execSQL("create table " + LOGS_TABLE + " ( _id int primary key,distance int,calories int,time int,date long,measurement text,points text,walk_id long)");
		db.execSQL("create table " + WALKING_GEOPOINT + " ( _id int primary key,walk_id int, latitude double,longitude double)");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}

}
