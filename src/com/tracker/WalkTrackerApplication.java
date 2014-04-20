package com.tracker;

import java.util.ArrayList;
import java.util.Date;

import com.google.android.maps.GeoPoint;
import com.google.gson.Gson;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.location.Location;
import android.preference.PreferenceManager;

public class WalkTrackerApplication extends Application implements OnSharedPreferenceChangeListener{	
	private SharedPreferences sharedPreferences;
	private Database database;
	private Gson gson;
	private ArrayList<Location> currentWalkPath;
	
	public int test = 0;
	
	@Override
	public void onCreate(){
		super.onCreate();
		this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		this.sharedPreferences.registerOnSharedPreferenceChangeListener(this);
		
		this.database = new Database(this);
		this.gson = new Gson();
		
		this.currentWalkPath = getDatabase().getCurrentWalkPath();
	}
	
	@Override
	public void onTerminate(){
		super.onTerminate();
	}
	
	public void onSharedPreferenceChanged(SharedPreferences arg0, String arg1) {
		
	}
	
	public SharedPreferences getSharedPreferences(){
		return sharedPreferences;
	}
	
	public void saveLog(ArrayList<Location> walkPathToSave, double calories, double distance, String measurement){
		long walkId = getDatabase().getMaxId(DbHelper.WALKING_GEOPOINT, Database.MAX_WALK_ID);
		
		Date date = new Date();
		
		String points = gson.toJson(locationsToGeo(walkPathToSave));
		int time = (int)((System.currentTimeMillis()-Calculator.startTime)/1000);
		Log log = new Log(date, time, calories, distance, measurement, points, walkId);
		
		database.updateDatabaseLog(log);
	}
	
	private ArrayList<GeoPoint> locationsToGeo(ArrayList<Location> locations){
		ArrayList<GeoPoint> geoPoints = new ArrayList<GeoPoint>();
		for(int i=0; i<locations.size(); i++){
			geoPoints.add(new GeoPoint((int)(locations.get(i).getLatitude()*1E6), (int)(locations.get(i).getLongitude()*1E6)));
		}
		
		return geoPoints;
	}
	
	public void updateLocationToDatabase(Location location, boolean forReset){
		GeoPoint geoPoint = new GeoPoint((int)(location.getLatitude()*1E6), (int)(location.getLongitude()*1E6));
		getDatabase().updateDatabasePoint(geoPoint, forReset);
	}
	
	public Database getDatabase(){
		return this.database;
	}
	
	public ArrayList<Location> getCurrentWalkPath(){
		return this.currentWalkPath;
	}
}
