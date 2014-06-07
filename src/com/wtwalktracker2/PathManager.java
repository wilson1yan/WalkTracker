package com.wtwalktracker2;

import com.google.android.gms.maps.LocationSource.OnLocationChangedListener;
import com.google.android.gms.maps.model.LatLng;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;

public class PathManager extends Service implements LocationListener{
	public static final String LOCATION_UPDATE = "com.tracker.PathManager.LOCATION_UPDATE";
	public static final String CLEAR_MAP = "com.tracker.PathManager.CLEAR_MAP";
	public static final String PERSON_UPDATE = "com.tracker.PathManager.PERSON_UPDATE";
	public static final String LATITUDE = "com.tracker.PathManager.LATITUDE";
	public static final String LONGITUDE = "com.tracker.PathManager.LONGITUDE";
	
	public static final int DELAY_STRING = 1000;
	private OnLocationChangedListener listener;
	public static boolean isRunning = false, isWalking = false;
	
	LocationManager manager;
	Calculator calculator;
	WalkTrackerApplication walktracker;
	PathManagerReceiver receiver;
	
	Location prevLocation;
	
	private long shortTimeStart;

	AutoPathGenerator autoPathGenerator;
	Handler handler;
	
	
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public void onCreate(){
		super.onCreate();
		
		manager = (LocationManager)getSystemService(LOCATION_SERVICE);
		walktracker = (WalkTrackerApplication) getApplication();
		
		handler = new Handler();
		autoPathGenerator = new AutoPathGenerator();
		
		isRunning = true;
	}
	
	Runnable mStatusChecker = new Runnable() {
		
		public void run() {
			Location location = autoPathGenerator.getNextLocation();
			update(location);
			
			handler.postDelayed(mStatusChecker, 1000);
		}
	};
	
	void startTask(){
		mStatusChecker.run();
	}
	
	void stopTask(){
		handler.removeCallbacks(mStatusChecker);
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId){
		super.onStartCommand(intent, flags, startId);	

		initCalculator();
				
		IntentFilter pathFilter = new IntentFilter();
		pathFilter.addAction(WalkMap.START_WALK_UPDATE);
		pathFilter.addAction(WalkMap.STOP_WALK_UDPATE);
		receiver = new PathManagerReceiver();
		registerReceiver(receiver, pathFilter);
		
		activate();
		
		return START_STICKY;
	}
	
	private void initCalculator(){		
		double distance = walktracker.getSharedPreferences().getInt(Settings.CURRENT_DISTANCE_KEY, 0);
		double calories = walktracker.getSharedPreferences().getInt(Settings.CURRENT_CALORIE_KEY, 0);
		long startTime = walktracker.getSharedPreferences().getLong(Settings.CURRENT_START_KEY, System.currentTimeMillis());
		
		if(startTime == -1){
			startTime = System.currentTimeMillis();
		}
		
		double weight = Integer.parseInt(walktracker.getSharedPreferences().getString(Settings.WEIGHT, "150"));
		String unit = walktracker.getSharedPreferences().getString(Settings.MEASUREMENT, "m");
		String calorieType = walktracker.getSharedPreferences().getString(Settings.CALORIE_BURN, "Gross");
		
		calculator = new Calculator(distance, calories, startTime, weight, unit, calorieType);
	}
	
	@Override
	public void onDestroy(){
		super.onDestroy();
		unregisterReceiver(receiver);

		manager.removeUpdates(this);
		isRunning = false;
	}

	public void activate() {
		if(!walktracker.isTest()){
			LocationProvider gpsProvider = manager.getProvider(LocationManager.GPS_PROVIDER);
			if(gpsProvider != null){
				manager.requestLocationUpdates(gpsProvider.getName(), 0, 0, this);
			}
		}
	}

	public void deactivate() {
		manager.removeUpdates(this);
	}

	public void onLocationChanged(Location location) {
		if(isWalking){ update(location); }
		else {
			notifyAcitivtyDrawPerson(location);
		}
	}
		
	private void update(Location location){
		if(prevLocation != null){
			double timeLength = (System.currentTimeMillis()-this.shortTimeStart)/1000;
			calculator.calculate(location.distanceTo(prevLocation), timeLength);
			
			shortTimeStart = System.currentTimeMillis();
			notifyActivityDrawMap();

		}else{
			prevLocation = location;
		}
		
		walktracker.getCurrentWalkPath().add(location);
		walktracker.getCurrentWalkPathLatLng().add(new LatLng(location.getLatitude(), location.getLongitude()));
		
		updatePreferences();
		walktracker.updateLocationToDatabase(location, false);
		
		prevLocation = location;
		autoPathGenerator.setPrevLocation(location);
	}
	
	private void updatePreferences(){
		calculator.updateInfo(walktracker.getSharedPreferences());
	}
	
	private void notifyActivityDrawMap(){
		Intent intent = new Intent(LOCATION_UPDATE);
		sendBroadcast(intent);
	}
	
	private void notifyAcitivtyDrawPerson(Location loc){
		Intent intent = new Intent(PERSON_UPDATE);
		intent.putExtra(LATITUDE, loc.getLatitude());
		intent.putExtra(LONGITUDE, loc.getLongitude());
		
		sendBroadcast(intent);
	}

	
	public void onProviderDisabled(String provider) {
		
	}

	public void onProviderEnabled(String provider) {
		
	}

	public void onStatusChanged(String provider, int status, Bundle extras) {
		
	}
	
	public void reset(){
		calculator.reset();
	}
	
	private void notifyMapToClear(){
		Intent intent = new Intent(CLEAR_MAP);
		sendBroadcast(intent);
	}
	
	private void stopWalk(boolean saveLog){
		if(saveLog){
			walktracker.saveLog(walktracker.getCurrentWalkPath(), Calculator.totalCalories, Calculator.totalDistance, Calculator.measurementUnit);

		}
		walktracker.getCurrentWalkPath().clear();
		walktracker.getCurrentWalkPathLatLng().clear();

		if(prevLocation != null){
			walktracker.getDatabase().updateDatabasePoint(prevLocation, true);
			walktracker.getCurrentWalkPath().add(prevLocation);
			walktracker.getCurrentWalkPathLatLng().add(new LatLng(prevLocation.getLatitude(), prevLocation.getLongitude()));
		}
		
		reset();
		updatePreferences();
		
		if(walktracker.isTest()){
			stopTask();
		}
		
		notifyMapToClear();
		
		this.isWalking = false;
	}
	
	private void startWalk(){
		this.shortTimeStart = System.currentTimeMillis();
		initCalculator();
		
		if(walktracker.isTest()){
			startTask();
		}else{
			activate();
		}
		
		this.isWalking = true;
	}
	
	
	public class PathManagerReceiver extends BroadcastReceiver{
		
		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent.getAction().equalsIgnoreCase(WalkMap.STOP_WALK_UDPATE)){
				boolean saveLog = intent.getExtras().getBoolean(WalkMap.SAVE_KEY);
				stopWalk(saveLog);
			}else if(intent.getAction().equalsIgnoreCase(WalkMap.START_WALK_UPDATE)){
				startWalk();
			}
		}
	}
}
