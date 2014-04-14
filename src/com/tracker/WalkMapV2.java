package com.tracker;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Configuration;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.maps.GeoPoint;
import com.google.gson.Gson;


public class WalkMapV2 extends FragmentActivity implements LocationSource, LocationListener, OnSharedPreferenceChangeListener{
	public static final double LBS_TO_KG_CONVERSION = 1/2.2046;
	public static final HashMap<String, Double> METRIC_CONVERSION = new HashMap<String, Double>(){{put("mi", 0.000621371); put("m", 1.0); put("km", 0.001); put("kg", 1/2.2046);}};
	public static final double MAX_WALKING_SPEED =10;

	
	static boolean isRunning = false;

	
	GoogleMap mMap;
	LocationManager manager;
	ArrayList<Location> locations;
	
	Database database;
	SharedPreferences sharedPreferences;
	OnLocationChangedListener listener;
	Marker currentLocation;
	Location lastLocation;
	
	Dialog dialog;
	EditText editText;
	Gson gson;
	
	public static double calories, convertedDistance;
	String calorieType;
	static String measurement;
	double weight, distance;
	int x = 0;

	static long startTime;
	long shortStartTime;
	boolean test = false;
	
	MarkerOptions markerOptions;
	BitmapDescriptor bitmapDescriptor;
	
	Handler handler;
	AutoPathGenerator generator;
	boolean zoom, center;
	
	public void onCreate(Bundle bundle){
		super.onCreate(bundle);
		setContentView(R.layout.main_v2);
		
		handler = new Handler();
		generator = new AutoPathGenerator();
		
		manager = (LocationManager)getSystemService(LOCATION_SERVICE);
		initMap();
		
		database = new Database(this);
		gson = new Gson();
        locations = database.getLocations();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        bitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.person);

        updatePrefs();
                
        drawAllLines();
	}
	
	Runnable mStatusChecker = new Runnable() {
		
		public void run() {
			Location location = generator.getNextLocation();
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
	
	private void initMap(){
		mMap = ((SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
		mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
	}
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	@Override
    public void onConfigurationChanged(Configuration newConfig)
    { 
		super.onConfigurationChanged(newConfig);
		
		for(int i=1; i<locations.size(); i++){
        	LatLng src = new LatLng(locations.get(i).getLatitude(), locations.get(i).getLongitude());
        	LatLng dest = new LatLng(locations.get(i-1).getLatitude(), locations.get(i-1).getLongitude());
        	mMap.addPolyline(new PolylineOptions().add(src, dest).width(8).color(Color.RED).geodesic(true));
        }
		
    }
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		switch(item.getItemId()){
		case R.id.itemPrefs:
			startActivity(new Intent(this, Settings.class));

			break;
		case R.id.reset:
			System.out.println();
			
			if(isRunning){
				Toast.makeText(this, "Stopped", Toast.LENGTH_LONG).show();
				if(test){
					stopTask();
				}else{
					deactivate();
				}
				item.setTitle("Start Walk");
				
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				String question = "Would You Like to Save Your Walk?";
				builder.setTitle(question);
				String[] items = {"Yes", "No"};
				builder.setItems(items, new DialogInterface.OnClickListener() {
					
					public void onClick(DialogInterface dialog, int which) {
						long walkId = database.getMaxId(DbHelper.WALKING_GEOPOINT, database.MAX_WALK_ID);
						
						if(which == 0){
							saveLog(walkId);
						}
						reset();
					}
				});
				
				builder.show();
			}else{
				isRunning = true;
				Toast.makeText(this, "Started", Toast.LENGTH_LONG).show();
				startTime = System.currentTimeMillis();
				shortStartTime = System.currentTimeMillis();
				item.setTitle("Stop Walk");
				if(test){
					startTask();
				}else{
					activate(listener);
				}
			}
			break;
		case R.id.viewLog:
			startActivity(new Intent(this, LogView.class));
			break;
		}
		return true;
	}

	public void saveLog(long walkId){
		Date date = new Date();
		
		String points = gson.toJson(locationsToGeo(locations));
		int time = (int)((System.currentTimeMillis()-WalkMapV2.startTime)/1000);
		Log log = new Log(date, time, calories, distance, measurement, points, walkId);
		
		database.updateDatabaseLog(log);
	}
	
	public void reset(){
		GeoPoint geoPoint;
		if(!test){
			geoPoint = new GeoPoint((int)(manager.getLastKnownLocation(LocationManager.GPS_PROVIDER).getLatitude()*1E6), (int)(manager.getLastKnownLocation(LocationManager.GPS_PROVIDER).getLongitude()*1E6));
		}else{
			geoPoint = new GeoPoint((int)(lastLocation.getLatitude()*1E6), (int)(lastLocation.getLongitude()*1E6));
		}
			
		database.updateDatabasePoint(geoPoint, true);

		
		sharedPreferences.edit().putInt(Settings.CURRENT_CALORIE_KEY, 0).commit();
		sharedPreferences.edit().putInt(Settings.CURRENT_DISTANCE_KEY, 0).commit();
		sharedPreferences.edit().putLong(Settings.CURRENT_START_KEY, 0).commit();
		
		locations.clear();
		
		calories = 0;
		distance = 0;
		convertedDistance = 0;

		startTime = System.currentTimeMillis();
		
		mMap.clear();

		if(currentLocation != null){
			currentLocation = mMap.addMarker(new MarkerOptions().icon(bitmapDescriptor).position(currentLocation.getPosition()));
		}
		
		generator.reset();
		isRunning = false;

	}
	
	public void update(Location location){
		if(lastLocation != null){

			
			double currentDistance = location.distanceTo(lastLocation);
			double seconds = findSecondsPassed(shortStartTime);
			shortStartTime = System.currentTimeMillis();

			double speed = findSpeed(currentDistance, seconds/3600, measurement);
			distance += currentDistance;

			if(speed<=MAX_WALKING_SPEED){
				calories += calculateCalories(currentDistance, seconds, weight, measurement, calorieType);
				
			}
			
			convertedDistance = (distance*((Double)METRIC_CONVERSION.get(measurement)).doubleValue());
		

		}else{
			lastLocation = location;
			mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 18f));
		}
		
		if(currentLocation != null){
			currentLocation.remove();
		}
		
		Marker marker = createMarker(location);
        locations.add(location);
        currentLocation = marker;
        
        LatLng latLng = marker.getPosition();

        zoom(zoom, center, latLng);
        
        
        drawLines(location);
        
		lastLocation = location;
		
		setCurrentInfo();
        database.updateDatabasePoint(new GeoPoint((int)(location.getLatitude()*1E6), (int)(location.getLongitude()*1E6)), false);
        
        //deactivate();
        
        generator.setPrevLocation(lastLocation);

	}
	
	public void zoom(boolean zoom, boolean center, LatLng latlng){
		if(zoom && center){
			mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng, 18f));
		}else if(zoom){
			 mMap.animateCamera(CameraUpdateFactory.zoomTo(18f));
		}else if(center){
			mMap.animateCamera(CameraUpdateFactory.newLatLng(latlng));
		}
	}
	
	public void onLocationChanged(Location location) {
		update(location);
	}
	
	private double findSpeed(double distance, double hours, String measurement){
		double km = 0;
		km = (distance/(Double)METRIC_CONVERSION.get(measurement))/1000; 
		return km/hours;
	}
	
	private double calculateCalories(double distance, double timeSec, double weight, String measurement, String calorieType){
		double hours = timeSec/3600;
		double kph = findSpeed(distance, hours, measurement);
		double kg = weight*LBS_TO_KG_CONVERSION;
		double calories;
		
		if(calorieType.equalsIgnoreCase("Gross")){
			calories = (0.0215*Math.pow(kph, 3) - 0.1765*Math.pow(kph, 2) + 0.8710*kph + 1.4577)*kg*hours;
		}else{
			calories = (0.0215*Math.pow(kph, 3) - 0.1765*Math.pow(kph, 2) + 0.8710*kph)*kg*hours;
		}
		
		return calories;
	}
	
	private void drawAllLines(){
		for(int i=1; i<locations.size(); i++){
			LatLng src = new LatLng(locations.get(i-1).getLatitude(), locations.get(i-1).getLongitude());
			LatLng dest = new LatLng(locations.get(i).getLatitude(), locations.get(i).getLongitude());
			
			mMap.addPolyline(new PolylineOptions().add(src, dest).width(8).color(Color.RED).geodesic(true));
		}
	}

	public void onProviderDisabled(String provider) {
		
	}

	public void onProviderEnabled(String provider) {
		
	}

	public void onStatusChanged(String provider, int status, Bundle extras) {
		
	}

	public double findSecondsPassed(long start){
		return (System.currentTimeMillis()-start)/1000.0;
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
	
	private void drawLines(Location location){
		if(lastLocation != null){
			LatLng src = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
			LatLng dest = new LatLng(location.getLatitude(), location.getLongitude());
			
			
			mMap.addPolyline(new PolylineOptions().add(src, dest).width(8).color(Color.RED).geodesic(true));
		}
	}
	
	private Marker createMarker(Location location){
		markerOptions = new MarkerOptions();
        markerOptions.icon(bitmapDescriptor);
                
        markerOptions.position(new LatLng(location.getLatitude(), location.getLongitude()));
        
        return mMap.addMarker(markerOptions);
	}
	
	private void updatePrefs(){
		try{
			weight = Integer.parseInt(sharedPreferences.getString(Settings.WEIGHT, "0"));
			if(weight == 0){
				Toast.makeText(this, "Please enter a weight in the settings below\nDefault to 150lbs", Toast.LENGTH_LONG).show();
				openWeightDialog();
				sharedPreferences.edit().putString(Settings.WEIGHT, "150").commit();
				weight = 150;
			}
		}catch(NumberFormatException nfe){
			Toast.makeText(this, "Please Enter a Correct Weight", Toast.LENGTH_LONG).show();
		}
		
		calorieType = sharedPreferences.getString(Settings.CALORIE_BURN, "Gross");
	}

	/**
	 * This method is called when share preferences and altered and it updates the local values
	 */
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		updatePrefs();
	}
	
	private void getCurrentInfo(){
		measurement = sharedPreferences.getString(Settings.MEASUREMENT, "m");

		
		calories = sharedPreferences.getInt(Settings.CURRENT_CALORIE_KEY, 0);
		distance = sharedPreferences.getInt(Settings.CURRENT_DISTANCE_KEY, 0);
		
		convertedDistance = (distance*((Double)METRIC_CONVERSION.get(measurement)).doubleValue());
		startTime = sharedPreferences.getLong(Settings.CURRENT_START_KEY, 0);
		
		test = sharedPreferences.getBoolean(Settings.TEST, true);
		zoom = sharedPreferences.getBoolean(Settings.ZOOM, true);
		center = sharedPreferences.getBoolean(Settings.CENTER, true);
		
		if(!test){
			mMap.setMyLocationEnabled(true);
			mMap.setLocationSource(this);
		}else{
			mMap.setMyLocationEnabled(false);
			mMap.setLocationSource(null);
		}
	}
	
	private void openWeightDialog(){
	    dialog = new Dialog(this);
		dialog.setContentView(R.layout.weight_dialog);
		dialog.setTitle("Input Weight");
		
		editText = (EditText) dialog.findViewById(R.id.editText);
		Button button = (Button) dialog.findViewById(R.id.weight_button);
		
		button.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				setWeight();
			}
		});
		
		//dialog.show();

	}
	
	private void setWeight(){
		String weight = editText.getText().toString();
		try{
			WalkMapV2.this.weight = Double.parseDouble(weight);
		}catch(NumberFormatException e){
			
		}
		
		sharedPreferences.edit().putString(Settings.WEIGHT, weight).commit();
		dialog.dismiss();
	}
	
	@Override
	public void onResume(){
		super.onResume();
		getCurrentInfo();

	}
	
	public void setCurrentInfo(){
		sharedPreferences.edit().putInt(Settings.CURRENT_CALORIE_KEY, (int)calories).commit();
		sharedPreferences.edit().putInt(Settings.CURRENT_DISTANCE_KEY, (int)distance).commit();
		sharedPreferences.edit().putLong(Settings.CURRENT_START_KEY, startTime).commit();

	}
	
	private ArrayList<GeoPoint> locationsToGeo(ArrayList<Location> locations){
		ArrayList<GeoPoint> geoPoints = new ArrayList<GeoPoint>();
		for(int i=0; i<locations.size(); i++){
			geoPoints.add(new GeoPoint((int)(locations.get(i).getLatitude()*1E6), (int)(locations.get(i).getLongitude()*1E6)));
		}
		
		return geoPoints;
	}
	
	@Override
	public void onPause(){
		super.onPause();
		
		setCurrentInfo();
	}
	
	@Override
	public void onStart(){
		super.onStart();
		drawAllLines();
	}
	
	@Override
	public void onDestroy(){
		super.onDestroy();
	}
}
