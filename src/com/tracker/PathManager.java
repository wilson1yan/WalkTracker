package com.tracker;

import com.google.android.gms.maps.LocationSource.OnLocationChangedListener;
import com.tracker.WalkMap.LocationReceiver;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class PathManager extends Service implements LocationListener{
	public static final String LOCATION_UPDATE = "com.tracker.PathManager.LOCATION_UPDATE";
	public static final String LATITUDE = "com.tracker.PathManager.LATITUDE";
	public static final String LONGITUDE = "com.tracker.PathManager.LONGITUDE";
	
	public static final int DELAY_STRING = 1000;
	private boolean isRunning;
	private OnLocationChangedListener listener;
	
	LocationManager manager;
	Calculator calculator;
	WalkTrackerApplication walktracker;
	
	Location prevLocation;
	
	private long shortTimeStart;

	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public void onCreate(){
		super.onCreate();
		
		manager = (LocationManager)getSystemService(LOCATION_SERVICE);
		walktracker = (WalkTrackerApplication) getApplication();

	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId){
		super.onStartCommand(intent, flags, startId);
		
		this.isRunning = true;
		this.shortTimeStart = System.currentTimeMillis();

		initCalculator();

		activate(listener);
				
		return START_STICKY;
	}
	
	private void initCalculator(){		
		double distance = walktracker.getSharedPreferences().getInt(Settings.CURRENT_DISTANCE_KEY, 0);
		double calories = walktracker.getSharedPreferences().getInt(Settings.CURRENT_CALORIE_KEY, 0);
		long startTime = walktracker.getSharedPreferences().getLong(Settings.CURRENT_START_KEY, System.currentTimeMillis());
		
		double weight = Integer.parseInt(walktracker.getSharedPreferences().getString(Settings.WEIGHT, "150"));
		String unit = walktracker.getSharedPreferences().getString(Settings.MEASUREMENT, "m");
		String calorieType = walktracker.getSharedPreferences().getString(Settings.CALORIE_BURN, "Gross");
		
		calculator = new Calculator(distance, calories, System.currentTimeMillis(), weight, unit, calorieType);
	}
	
	@Override
	public void onDestroy(){
		super.onDestroy();
		
		
	}

	public void activate(OnLocationChangedListener listener) {
		this.listener = listener;
		LocationProvider gpsProvider = manager.getProvider(LocationManager.GPS_PROVIDER);
		if(gpsProvider != null){
			manager.requestLocationUpdates(gpsProvider.getName(), 1000, 1, this);
		}
		
	}

	public void deactivate() {
		manager.removeUpdates(this);
	}

	public void onLocationChanged(Location location) {
		if(prevLocation != null){
			double timeLength = (System.currentTimeMillis()-this.shortTimeStart)/1000;
			calculator.calculate(location.distanceTo(prevLocation), timeLength);
			
			shortTimeStart = System.currentTimeMillis();
		}else{
			prevLocation = location;
		}
		
		sendLocationInfoToActivity(location);
		//sendInfoToCanvas();
		
		updatePreferences();
		walktracker.updateLocationToDatabase(location, false);
	}
	
	private void updatePreferences(){
		calculator.updateInfo(walktracker.getSharedPreferences());
	}
	
	private void sendLocationInfoToActivity(Location location){
		double latitude = location.getLatitude();
		double longitude = location.getLongitude();
		
		Intent intent = new Intent(LOCATION_UPDATE);
		
		intent.putExtra(LATITUDE, latitude);
		intent.putExtra(LONGITUDE, longitude);
		
		sendBroadcast(intent);
	}
	
	private void sendInfoToCanvas(){
		Intent intent = calculator.packageInfoForCanvas();
		sendBroadcast(intent);
	}
	
	public boolean isRunning(){
		return this.isRunning;
	}

	
	public void onProviderDisabled(String provider) {
		
	}

	public void onProviderEnabled(String provider) {
		
	}

	public void onStatusChanged(String provider, int status, Bundle extras) {
		
	}
}
