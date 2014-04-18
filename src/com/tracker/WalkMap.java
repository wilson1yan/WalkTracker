package com.tracker;


import java.util.ArrayList;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;



public class WalkMap extends FragmentActivity{
	public static final String PREFERENCE_UPDATE = "PREFERENCE_UPDATE";
	
	GoogleMap mMap;
	ArrayList<Location> walkPath = new ArrayList<Location>();
	MapHandler mapHandler;
	
	static boolean running = false;
	
	LocationReceiver receiver;
	WalkTrackerApplication walktracker;
			
	@Override
	public void onCreate(Bundle bundle){
		super.onCreate(bundle);
		setContentView(R.layout.main_v2);
		
		walktracker = (WalkTrackerApplication) getApplication();
		initMap();
		mapHandler = new MapHandler(mMap);
		
     }
	
	@Override
	public void onStart(){
		super.onStart();		
	}
	
	@Override
	public void onResume(){
		super.onResume();
		
		IntentFilter locationFilter = new IntentFilter(PathManager.LOCATION_UPDATE);
		receiver = new LocationReceiver();
		
		registerReceiver(receiver, locationFilter);
	}
	
	@Override
	public void onPause(){
		super.onPause();
		
		notifyServiceUpdatePreferences();
	}
	
	@Override
	public void onStop(){
		super.onStop();
	}
	
	
	@Override
	public void onDestroy(){
		super.onDestroy();
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
    }
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		if(running){
			stopService(new Intent(this, PathManager.class));
		}else{
			startService(new Intent(this, PathManager.class));
		}
		
		running = !running;

		return true;
	}
	
	public void notifyServiceUpdatePreferences(){
		Intent intent = new Intent(PREFERENCE_UPDATE);
		sendBroadcast(intent);
	}
	
	public void updateMapWithNewLocation(Location location){
		walkPath.add(location);
		mapHandler.updateMap(walkPath);
	}
	
	public class LocationReceiver extends BroadcastReceiver{
		@Override
		public void onReceive(Context context, Intent intent){
			double latitude = intent.getDoubleExtra(PathManager.LATITUDE, 0);
			double longitude = intent.getDoubleExtra(PathManager.LONGITUDE, 0);
				
			Location location = new Location("RECEIVED_LOCATION");	
			location.setLatitude(latitude);
			location.setLongitude(longitude);
			
			updateMapWithNewLocation(location);
		}
	}
}


