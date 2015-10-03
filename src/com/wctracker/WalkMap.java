package com.wctracker;




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
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.model.CameraPosition;


public class WalkMap extends AppCompatActivity{
	public static final String PREFERENCE_UPDATE = "PREFERENCE_UPDATE";
	public static final String STOP_WALK_UDPATE = "STOP_WALK";
	public static final String SAVE_KEY = "SAVE";
	public static final String START_WALK_UPDATE = "START_WALK";
	
	GoogleMap mMap;
	MapHandler mapHandler;
		
	LocationReceiver receiver;
	WalkTrackerApplication walktracker;
	LocationManager manager;
			
	@Override
	public void onCreate(Bundle bundle){
		super.onCreate(bundle);
				
		setContentView(R.layout.main_v2);
		
		walktracker = (WalkTrackerApplication) getApplication();
		initMap();
		
		
		mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
		mapHandler = new MapHandler(mMap);
		
		mapHandler.drawCurrentPath(walktracker.getCurrentWalkPath());
		mapHandler.getMap().setOnCameraChangeListener(new OnCameraChangeListener() {
			
			public void onCameraChange(CameraPosition arg0) {
				mapHandler.updateBounds(walktracker.getCurrentWalkPathLatLng());
			}
		});
				
     }
	
	public void checkIfGPSEnabled(){
		 if(manager == null) manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );

		    if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
		        buildAlertMessageNoGps();
		    }
	}
	
	private void buildAlertMessageNoGps() {
	    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
	           .setCancelable(false)
	           .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
	               public void onClick(final DialogInterface dialog, final int id) {
	                   startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
	               }
	           })
	           .setNegativeButton("No", new DialogInterface.OnClickListener() {
	               public void onClick(final DialogInterface dialog, final int id) {
	                    dialog.cancel();
	               }
	           });
	    final AlertDialog alert = builder.create();
	    alert.show();
	}
	
	@Override
	public void onStart(){
		super.onStart();		
		
		if(!PathManager.isWalking && !PathManager.isRunning){
			startService(new Intent(this, PathManager.class));
		}
		
		checkIfGPSEnabled();
	}
	
	@Override
	public void onResume(){
		super.onResume();
		
		IntentFilter locationFilter = new IntentFilter();
		locationFilter.addAction(PathManager.LOCATION_UPDATE);
		locationFilter.addAction(PathManager.CLEAR_MAP);
		locationFilter.addAction(PathManager.PERSON_UPDATE);
		
		if(receiver == null){
			receiver = new LocationReceiver();
			registerReceiver(receiver, locationFilter);
		}
		
		//if(PathManager.isWalking) mapHandler.drawCurrentPath(walktracker.getCurrentWalkPath());
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
		//stopService(new Intent(this, PathManager.class));
		unregisterReceiver(receiver);
	}
	

	private void initMap(){
		mMap = ((SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
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
			startActivity(new Intent(this, LogActivity.class));
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
		if(mapHandler.centerCounter == 10){
			mapHandler.animateCamera(walktracker, this);
			mapHandler.centerCounter = 0;
		}else{
			mapHandler.centerCounter++;
		}
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
				mapHandler.animateCamera(walktracker, WalkMap.this);
			}
		}
	}
}


