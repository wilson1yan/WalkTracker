package com.tracker;

import java.util.ArrayList;
import java.util.Date;

import com.google.android.maps.GeoPoint;

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
	DbHelper dbHelper;
	String[] MAX_ID = {"max(_id)"};
	String[] MAX_WALK_ID = {"max(walk_id)"};
	
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
			String points = cursor.getString(cursor.getColumnIndex(DbHelper.POINTS));
			long id = cursor.getLong(cursor.getColumnIndex(DbHelper.ID_NUM));
			
			logs.add(new Log(date, time, caloriesBurned, distance, measurement, points, id));
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
				String points = cursor.getString(cursor.getColumnIndex(DbHelper.POINTS));
				long id = cursor.getLong(cursor.getColumnIndex(DbHelper.ID_NUM));
				
				db.close();
				
				return new Log(date, time, caloriesBurned, distance, measurement, points, id);
			}else{
				cursor.moveToNext();
			}
		}
		
		db.close();
		return null;
	}
	
	public String debugTable(String tableName){
		String records="";
		
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		//distance int,calories int,time int,date long,measurement text
		Cursor cursor = db.query(tableName, null , null, null, null, null, null);
		cursor.moveToLast();
		records = records+"walk_id=" + cursor.getLong(cursor.getColumnIndex(DbHelper.ID_NUM)) + "\n";
		records = records+"id=" + cursor.getLong(cursor.getColumnIndex(DbHelper.ID));
		db.close();
		return records;
	}
	
	/**
	 * Retrieves a list of all the geopoints in the table by using a query and cursor
	 * @return An Arraylist of geo points
	 */
	public ArrayList<GeoPoint> getPoints(){
		ArrayList<GeoPoint> geoPoints = new ArrayList<GeoPoint>();
		
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		Cursor cursor = db.query(DbHelper.WALKING_GEOPOINT, null, null, null, null, null, null);
		long id = getMaxId(DbHelper.WALKING_GEOPOINT, MAX_WALK_ID);
		cursor.moveToNext();
		
		while(!cursor.isAfterLast()){
			if(cursor.getInt(cursor.getColumnIndex(DbHelper.WALK_ID)) == id){
				int latitude = cursor.getInt(cursor.getColumnIndex(DbHelper.LATITUDE));
				int longitude = cursor.getInt(cursor.getColumnIndex(DbHelper.LONGITUDE));
				
				geoPoints.add(new GeoPoint(latitude, longitude));
			}
			
			cursor.moveToNext();
		}
		db.close();
		return geoPoints;
	}
	
	public ArrayList<Location> getLocations(){
		ArrayList<Location> locations = new ArrayList<Location>();
		ArrayList<GeoPoint> geoPoints = getPoints();
		
		for(int i=0; i<geoPoints.size(); i++){
			double lat = geoPoints.get(i).getLatitudeE6()/1000000.0;
			double lon = geoPoints.get(i).getLongitudeE6()/1000000.0;
			
			Location location = new Location(LocationManager.GPS_PROVIDER);
			location.setLatitude(lat);
			location.setLongitude(lon);
			
			locations.add(location);
		}
		
		return locations;
	}
	
	public ArrayList<GeoPoint> getWalkPoints(long id){
		ArrayList<GeoPoint> geoPoints = new ArrayList<GeoPoint>();
		
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		Cursor cursor = db.query(DbHelper.WALKING_GEOPOINT, null, null, null, null, null, null);
		cursor.moveToNext();
		
		while(!cursor.isAfterLast()){
			if(cursor.getInt(cursor.getColumnIndex(DbHelper.WALK_ID))==id){
				int latitude = cursor.getInt(cursor.getColumnIndex(DbHelper.LATITUDE));
				int longitude = cursor.getInt(cursor.getColumnIndex(DbHelper.LONGITUDE));
				
				geoPoints.add(new GeoPoint(latitude, longitude));
			}
			cursor.moveToNext();
		}
		db.close();
		return geoPoints;
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
	public void updateDatabasePoint(GeoPoint geoPoint, boolean forReset){
		long id = getMaxId(DbHelper.WALKING_GEOPOINT, MAX_ID);
		id++;
		long walkId = getMaxId(DbHelper.WALKING_GEOPOINT, MAX_WALK_ID);
		if(forReset){
			walkId++;
		}
		
		ContentValues contentValues = new ContentValues();
		contentValues.put(DbHelper.ID, id);
		contentValues.put(DbHelper.LATITUDE, geoPoint.getLatitudeE6());
		contentValues.put(DbHelper.LONGITUDE, geoPoint.getLongitudeE6());
		contentValues.put(DbHelper.WALK_ID, walkId);
		insertOrThrow(DbHelper.WALKING_GEOPOINT, contentValues);
	}
	
	public ArrayList<GeoPoint> getGeoPointsForDraw(){
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		String[] columns = {"max(walk_id)"};
		Cursor cursor = db.query(DbHelper.WALKING_GEOPOINT, columns, null, null, null, null, null);
		long id = cursor.moveToNext() ? cursor.getLong(0) : Long.MIN_VALUE;
		return getWalkPoints(id);
	}
}
