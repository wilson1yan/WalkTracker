package com.tracker;

import java.util.ArrayList;

import com.google.android.maps.GeoPoint;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.location.Location;
import android.preference.PreferenceManager;

public class WalkTrackerApplication extends Application implements OnSharedPreferenceChangeListener{	
	private SharedPreferences sharedPreferences;
	private Database database;
	
	@Override
	public void onCreate(){
		super.onCreate();
		this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		this.sharedPreferences.registerOnSharedPreferenceChangeListener(this);
		
		this.database = new Database(this);
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
	
	public ArrayList<Location> getLocations(){
		return getDatabase().getLocations();
	}
	
	public void updateLocationToDatabase(Location location, boolean forReset){
		GeoPoint geoPoint = new GeoPoint((int)(location.getLatitude()*1E6), (int)(location.getLongitude()*1E6));
		getDatabase().updateDatabasePoint(geoPoint, forReset);
	}
	
	public Database getDatabase(){
		return this.database;
	}
}
