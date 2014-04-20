package com.tracker;

import java.util.ArrayList;
import java.util.Date;


import android.app.Application;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.location.Location;
import android.preference.PreferenceManager;

public class WalkTrackerApplication extends Application implements OnSharedPreferenceChangeListener{	
	private SharedPreferences sharedPreferences;
	private Database database;
	private ArrayList<Location> currentWalkPath;
	private boolean isTest;
	
	public int test = 0;
	
	@Override
	public void onCreate(){
		super.onCreate();
		this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		this.sharedPreferences.registerOnSharedPreferenceChangeListener(this);
		
		this.database = new Database(this);
		
		this.currentWalkPath = getDatabase().getCurrentWalkPath();
		this.isTest = sharedPreferences.getBoolean(Settings.TEST, true);
	}
	
	@Override
	public void onTerminate(){
		super.onTerminate();
	}
	
	public void onSharedPreferenceChanged(SharedPreferences arg0, String arg1) {
		
	}
	
	public boolean isTest(){
		return this.isTest;
	}
	
	public SharedPreferences getSharedPreferences(){
		return sharedPreferences;
	}
	
	public void saveLog(ArrayList<Location> walkPathToSave, double calories, double distance, String measurement){
		long walkId = getDatabase().getMaxId(DbHelper.WALKING_GEOPOINT, Database.MAX_WALK_ID);
		
		Date date = new Date();
		
		int time = (int)((System.currentTimeMillis()-Calculator.startTime)/1000);
		Log log = new Log(date, time, calories, distance, measurement, walkId);
		
		database.updateDatabaseLog(log);
	}
	
	public void updateLocationToDatabase(Location location, boolean forReset){
		getDatabase().updateDatabasePoint(location, forReset);
	}
	
	public Database getDatabase(){
		return this.database;
	}
	
	public ArrayList<Location> getCurrentWalkPath(){
		return this.currentWalkPath;
	}
}
