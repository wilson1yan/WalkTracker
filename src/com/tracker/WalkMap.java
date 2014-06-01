package com.tracker;



import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;


public class WalkMap extends FragmentActivity{
	public static final String PREFERENCE_UPDATE = "PREFERENCE_UPDATE";
	public static final String STOP_WALK_UDPATE = "STOP_WALK";
	public static final String SAVE_KEY = "SAVE";
	public static final String START_WALK_UPDATE = "START_WALK";
	
	GoogleMap mMap;
	MapHandler mapHandler;
		
	LocationReceiver receiver;
	WalkTrackerApplication walktracker;
			
	@Override
	public void onCreate(Bundle bundle){
		super.onCreate(bundle);
		setContentView(R.layout.main_v2);
		
		walktracker = (WalkTrackerApplication) getApplication();
		initMap();
		mapHandler = new MapHandler(mMap);
		
		mapHandler.drawCurrentPath(walktracker.getCurrentWalkPath());
     }
	
	@Override
	public void onStart(){
		super.onStart();		
		
		if(!PathManager.isWalking && !PathManager.isRunning){
			startService(new Intent(this, PathManager.class));
		}
	}
	
	@Override
	public void onResume(){
		super.onResume();
		
		IntentFilter locationFilter = new IntentFilter();
		locationFilter.addAction(PathManager.LOCATION_UPDATE);
		locationFilter.addAction(PathManager.CLEAR_MAP);
		locationFilter.addAction(PathManager.PERSON_UPDATE);
		
		receiver = new LocationReceiver();
		registerReceiver(receiver, locationFilter);
		
		if(PathManager.isWalking) mapHandler.drawCurrentPath(walktracker.getCurrentWalkPath());
	}
	
	@Override
	public void onPause(){
		super.onPause();
		
		notifyServiceTo(PREFERENCE_UPDATE);
		
	}
	
	@Override
	public void onStop(){
		super.onStop();
		
		if(!PathManager.isWalking && PathManager.isRunning){
			stopService(new Intent(this, PathManager.class));
		}
	}
	
	
	@Override
	public void onDestroy(){
		super.onDestroy();	
		stopService(new Intent(this, PathManager.class));
		unregisterReceiver(receiver);
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
		switch(item.getItemId()){
		case R.id.itemPrefs:
			startActivity(new Intent(this, Settings.class));

			break;
		case R.id.reset:
			System.out.println();
			
			if(PathManager.isWalking){
				Toast.makeText(this, "Stopped", Toast.LENGTH_LONG).show();
				
				item.setTitle("Start Walk");
				mapHandler.reset();
				buildAndShowResetPrompt();
				
			}else{				
				item.setTitle("Stop Walk");
				notifyServiceTo(START_WALK_UPDATE);
			}
			break;
		case R.id.viewLog:
			startActivity(new Intent(this, LogView.class));
			break;
		}
		return true;
	}
	
	private void buildAndShowResetPrompt(){
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		String question = "Would You Like to Save Your Walk?";
		builder.setTitle(question);
		String[] items = {"Yes", "No"};
		builder.setItems(items, new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {						
				boolean saveLog = (which == 0);	//If "yes" is selected, which == 0 is true
				notifyServiceResetAndOrSaveLog(saveLog);
			}
		});

		builder.show();
	}
	
	
	public void notifyServiceResetAndOrSaveLog(boolean saveLog){
		Intent intent = new Intent(STOP_WALK_UDPATE);
		intent.putExtra(SAVE_KEY, saveLog);
		sendBroadcast(intent);
	}
	
	public void notifyServiceTo(String action){
		Intent intent = new Intent(action);
		sendBroadcast(intent);
	}
	
	public void updateMapWithNewLocation(Location location){
		//mapHandler.updateMap(walktracker.getCurrentWalkPathLatLng(), walktracker.getCurrentWalkPath().get(walktracker.getCurrentWalkPath().size()-1));
		mapHandler.updateMap(walktracker.getCurrentWalkPath());
		mapHandler.animateCamera(walktracker);
	}
	
	public class LocationReceiver extends BroadcastReceiver{
		@Override
		public void onReceive(Context context, Intent intent){
			if(intent.getAction().equalsIgnoreCase(PathManager.LOCATION_UPDATE)){
				updateMapWithNewLocation(walktracker.getCurrentWalkPath().get(walktracker.getCurrentWalkPath().size()-1));
			}else if(intent.getAction().equalsIgnoreCase(PathManager.CLEAR_MAP)){
				mapHandler.clearMap();
			}else if(intent.getAction().equalsIgnoreCase(PathManager.PERSON_UPDATE)){
				double lat = intent.getDoubleExtra(PathManager.LATITUDE, 0);
				double lon = intent.getDoubleExtra(PathManager.LONGITUDE, 0);
				
				Location loc = new Location(LocationManager.GPS_PROVIDER);
				loc.setLatitude(lat);
				loc.setLongitude(lon);
				
				mapHandler.updateWalkerLoc(loc);
				mapHandler.animateCamera(walktracker);
			}
		}
	}
}


