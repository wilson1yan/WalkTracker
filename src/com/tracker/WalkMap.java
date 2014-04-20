package com.tracker;


import java.util.ArrayList;

import junit.framework.Test;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.location.Location;
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
	
	public static boolean isRunning = false;
	
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
		switch(item.getItemId()){
		case R.id.itemPrefs:
			startActivity(new Intent(this, Settings.class));

			break;
		case R.id.reset:
			System.out.println();
			
			if(isRunning){
				Toast.makeText(this, "Stopped", Toast.LENGTH_LONG).show();
				mapHandler.clearMap();
				
				item.setTitle("Start Walk");
				
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
				isRunning = false;
			}else{
				startService(new Intent(this, PathManager.class));
				
				item.setTitle("Stop Walk");
				isRunning = true;
			}
			break;
		case R.id.viewLog:
			startActivity(new Intent(this, LogView.class));
			break;
		}
		return true;
	}
	
	public void notifyServiceResetAndOrSaveLog(boolean saveLog){
		Intent intent = new Intent(STOP_WALK_UDPATE);
		intent.putExtra(SAVE_KEY, saveLog);
		sendBroadcast(intent);
	}
	
	public void notifyServiceUpdatePreferences(){
		Intent intent = new Intent(PREFERENCE_UPDATE);
		sendBroadcast(intent);
	}
	
	public void updateMapWithNewLocation(Location location){
		mapHandler.updateMap(walktracker.getCurrentWalkPath());
	}
	
	public class LocationReceiver extends BroadcastReceiver{
		@Override
		public void onReceive(Context context, Intent intent){
			updateMapWithNewLocation(walktracker.getCurrentWalkPath().get(walktracker.getCurrentWalkPath().size()-1));
		}
	}
}


