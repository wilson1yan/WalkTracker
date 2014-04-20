package com.tracker;

import java.util.ArrayList;
import java.util.Date;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationManager;

/**
 * This is the main class where you can access the database and use it 
 * with the DbHelper
 * Helps with inserting and deleting
 * @author Wilson Yan
 *
 */
public class Database{
	public static final String[] MAX_ID = {"max(_id)"};
	public static final String[] MAX_WALK_ID = {"max(walk_id)"};
	
	DbHelper dbHelper;

	/**
	 * Constructor
	 * @param context
	 */
	public Database(Context context){
		dbHelper = new DbHelper(context);
	}
	
	/**
	 * Inserts the contents value into the specifed table (string)
	 * @param value
	 * @param contentValues
	 */
	public void insertOrThrow(String value, ContentValues contentValues){
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		try{
			db.insertOrThrow(value, null, contentValues);
		}catch(Exception e){
		}
		db.close();
	}
	
	/**
	 * Retrieves the logs from the database using a query and cursor
	 * @return An Arraylist of Logs
	 */
	public ArrayList<Log> getLogs(){
		ArrayList<Log> logs = new ArrayList<Log>();
		
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		//distance int,calories int,time int,date long,measurement text
		Cursor cursor = db.query(DbHelper.LOGS_TABLE, null, null, null, null, null, null);
		cursor.moveToNext();
		
		while(!cursor.isAfterLast()){
			long milliseconds = cursor.getLong(cursor.getColumnIndex(DbHelper.DATE));
			Date date = new Date(milliseconds);
			int distance = cursor.getInt(cursor.getColumnIndex(DbHelper.DISTANCE));
			int caloriesBurned = cursor.getInt(cursor.getColumnIndex(DbHelper.CALORIES_BURNED));
			int time = cursor.getInt(cursor.getColumnIndex(DbHelper.TIME));
			String measurement = cursor.getString(cursor.getColumnIndex(DbHelper.MEASUREMENT));
			long id = cursor.getLong(cursor.getColumnIndex(DbHelper.ID_NUM));
			
			logs.add(new Log(date, time, caloriesBurned, distance, measurement, id));
			cursor.moveToNext();
		}
		db.close();
		return logs;
	}
	
	public Log getLog(long walkId){
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cursor = db.query(DbHelper.LOGS_TABLE, null, null, null, null, null, null);
		cursor.moveToNext();
		
		while(!cursor.isAfterLast()){
			if(cursor.getLong(cursor.getColumnIndex(DbHelper.ID_NUM)) == walkId){
				long milliseconds = cursor.getLong(cursor.getColumnIndex(DbHelper.DATE));
				Date date = new Date(milliseconds);
				int distance = cursor.getInt(cursor.getColumnIndex(DbHelper.DISTANCE));
				int caloriesBurned = cursor.getInt(cursor.getColumnIndex(DbHelper.CALORIES_BURNED));
				int time = cursor.getInt(cursor.getColumnIndex(DbHelper.TIME));
				String measurement = cursor.getString(cursor.getColumnIndex(DbHelper.MEASUREMENT));
				long id = cursor.getLong(cursor.getColumnIndex(DbHelper.ID_NUM));
				
				db.close();
				
				return new Log(date, time, caloriesBurned, distance, measurement, id);
			}else{
				cursor.moveToNext();
			}
		}
		
		db.close();
		return null;
	}
	
	/**
	 * Retrieves a list of all the geopoints in the table by using a query and cursor
	 * @return An Arraylist of geo points
	 */
	
	public ArrayList<Location> getCurrentWalkPath(){		
		ArrayList<Location> walkPath = new ArrayList<Location>();
		
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		Cursor cursor = db.query(DbHelper.WALKING_GEOPOINT, null, null, null, null, null, null);
		long id = getMaxId(DbHelper.WALKING_GEOPOINT, MAX_WALK_ID);
		cursor.moveToNext();
		
		while(!cursor.isAfterLast()){
			if(cursor.getInt(cursor.getColumnIndex(DbHelper.WALK_ID)) == id){
				double latitude = cursor.getDouble(cursor.getColumnIndex(DbHelper.LATITUDE));
				double longitude = cursor.getDouble(cursor.getColumnIndex(DbHelper.LONGITUDE));
				
				Location location = new Location(LocationManager.GPS_PROVIDER);
				location.setLatitude(latitude);
				location.setLongitude(longitude);
				walkPath.add(location);
			}
			
			cursor.moveToNext();
		}
		db.close();
		
		return walkPath;
	}
	
	public ArrayList<Location> getWalkPoints(long id){
		ArrayList<Location> walkPath = new ArrayList<Location>();
		
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		Cursor cursor = db.query(DbHelper.WALKING_GEOPOINT, null, null, null, null, null, null);
		cursor.moveToNext();
		
		while(!cursor.isAfterLast()){
			if(cursor.getInt(cursor.getColumnIndex(DbHelper.WALK_ID))==id){
				double latitude = cursor.getDouble(cursor.getColumnIndex(DbHelper.LATITUDE));
				double longitude = cursor.getDouble(cursor.getColumnIndex(DbHelper.LONGITUDE));
				
				Location location = new Location(LocationManager.GPS_PROVIDER);
				location.setLatitude(latitude);
				location.setLongitude(longitude);
				
				walkPath.add(location);
			}
			cursor.moveToNext();
		}
		db.close();
		
		return walkPath;
	}
	
	/**
	 * To keep away from duplicate ids (increments by 1 for each row)
	 * @param table
	 * @return The current max id number for the table
	 */
	public long getMaxId(String table, String[] col){
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		
		Cursor cursor = db.query(table, col, null, null, null, null, null);
				
		return cursor.moveToNext() ? cursor.getLong(0) : Long.MIN_VALUE;
	}
	
	/**
	 * Adds a new log to the database
	 * @param log
	 */
	public void updateDatabaseLog(Log log) {
		long id = getMaxId(DbHelper.LOGS_TABLE, MAX_ID);
		id++;
		ContentValues contentValues = new ContentValues();
		contentValues.put(DbHelper.ID, id);
		contentValues.put(DbHelper.DISTANCE, (int) log.getDistance());
		contentValues.put(DbHelper.CALORIES_BURNED, (int) log.getCalories());
		contentValues.put(DbHelper.TIME,  (int)log.getTime());
        contentValues.put(DbHelper.DATE, log.getDate().getTime());
		contentValues.put(DbHelper.MEASUREMENT, log.getMeasurement());
		contentValues.put(DbHelper.WALK_ID, log.getId());
		insertOrThrow(DbHelper.LOGS_TABLE, contentValues);
	}

	/**
	 * Adds a new geo point to the database
	 * @param geoPoint
	 */
	public void updateDatabasePoint(Location location, boolean forReset){
		long id = getMaxId(DbHelper.WALKING_GEOPOINT, MAX_ID);
		id++;
		long walkId = getMaxId(DbHelper.WALKING_GEOPOINT, MAX_WALK_ID);
		if(forReset){
			walkId++;
		}
		
		ContentValues contentValues = new ContentValues();
		contentValues.put(DbHelper.ID, id);
		contentValues.put(DbHelper.LATITUDE, location.getLatitude());
		contentValues.put(DbHelper.LONGITUDE, location.getLongitude());
		contentValues.put(DbHelper.WALK_ID, walkId);
		insertOrThrow(DbHelper.WALKING_GEOPOINT, contentValues);
	}
	
	public ArrayList<Location> getWalkPathForDraw(){
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		String[] columns = {"max(walk_id)"};
		Cursor cursor = db.query(DbHelper.WALKING_GEOPOINT, columns, null, null, null, null, null);
		long id = cursor.moveToNext() ? cursor.getLong(0) : Long.MIN_VALUE;
		return getWalkPoints(id);
	}
}
