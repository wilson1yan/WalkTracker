package com.tracker;

import com.google.android.gms.maps.LocationSource.OnLocationChangedListener;
import com.google.android.maps.GeoPoint;
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
import android.os.Handler;
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
		
		this.isRunning = true;
		this.shortTimeStart = System.currentTimeMillis();

		initCalculator();

		startTask();
		//activate(listener);
				
		IntentFilter pathFilter = new IntentFilter(WalkMap.STOP_WALK_UDPATE);
		receiver = new PathManagerReceiver();
		registerReceiver(receiver, pathFilter);
		
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
		
		//manager.removeUpdates(this);
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
		update(location);
	}
		
	private void update(Location location){
		if(prevLocation != null){
			double timeLength = (System.currentTimeMillis()-this.shortTimeStart)/1000;
			calculator.calculate(location.distanceTo(prevLocation), timeLength);
			
			shortTimeStart = System.currentTimeMillis();
		}else{
			prevLocation = location;
		}
		
		walktracker.getCurrentWalkPath().add(location);
		sendLocationInfoToActivity();
		//sendInfoToCanvas();
		
		updatePreferences();
		walktracker.updateLocationToDatabase(location, false);
		
		autoPathGenerator.setPrevLocation(location);
	}
	
	private void updatePreferences(){
		calculator.updateInfo(walktracker.getSharedPreferences());
	}
	
	private void sendLocationInfoToActivity(){
		Intent intent = new Intent(LOCATION_UPDATE);
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
	
	public void reset(){
		calculator.reset();
	}
	
	public class PathManagerReceiver extends BroadcastReceiver{
		
		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent.getAction().equalsIgnoreCase(WalkMap.STOP_WALK_UDPATE)){
				boolean saveLog = intent.getExtras().getBoolean(WalkMap.SAVE_KEY);
				
				if(saveLog){
					walktracker.saveLog(walktracker.getCurrentWalkPath(), calculator.totalCalories, calculator.totalDistance, calculator.measurementUnit);
				}
				
				GeoPoint geoPoint;
				//geoPoint = new GeoPoint((int)(manager.getLastKnownLocation(LocationManager.GPS_PROVIDER).getLatitude()*1E6), (int)(manager.getLastKnownLocation(LocationManager.GPS_PROVIDER).getLongitude()*1E6));
				geoPoint = new GeoPoint((int)(prevLocation.getLatitude()*1E6), (int)(prevLocation.getLongitude()*1E6));
				walktracker.getDatabase().updateDatabasePoint(geoPoint, true);
				
				reset();
				updatePreferences();
				
				stopTask();
				stopSelf();
			}
		}
	}
}
