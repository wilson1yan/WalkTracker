package com.tracker;

import java.util.ArrayList;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class WalkViewer extends FragmentActivity{
	ArrayList<Location> walkPath;
	Database database;
	BitmapDescriptor bitmapDescriptor;
	GoogleMap mMap;
	private static Log log;
	
	public void onCreate(Bundle bundle){
		super.onCreate(bundle);
		setContentView(R.layout.simple_mapv2);
		
		database = new Database(this);
		long pos = getIntent().getExtras().getLong("position");
		walkPath = database.getWalkPoints(pos);
		log = database.getLog(pos);
		
		mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
		mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
		bitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.person);
		
		Location lastPos = new Location(LocationManager.GPS_PROVIDER);
		lastPos.setLatitude(walkPath.get(walkPath.size()-1).getLatitude());
		lastPos.setLongitude(walkPath.get(walkPath.size()-1).getLongitude());
	
		drawLines();
		
		
	}
	
	public static Log getLog(){
		return log;
	}
	
	public Marker createMarker(Location location){
		return mMap.addMarker(new MarkerOptions().icon(bitmapDescriptor).position(new LatLng(location.getLatitude(), location.getLongitude())));
	}
	
	public void drawLines(){
		for(int i=1; i<walkPath.size(); i++){
			LatLng src = new LatLng(walkPath.get(i-1).getLatitude(), walkPath.get(i-1).getLongitude());
			LatLng dest = new LatLng(walkPath.get(i).getLatitude(), walkPath.get(i).getLongitude());
			
	        
			mMap.addPolyline(new PolylineOptions().add(src, dest).width(8).color(Color.RED).geodesic(true));
		}
		
		Location last = walkPath.get(walkPath.size()-1);
		LatLng latLng = new LatLng(last.getLatitude(), last.getLongitude());
		mMap.addMarker(new MarkerOptions().position(latLng).icon(bitmapDescriptor));
		
		mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 19f));
		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.home, menu);
		return true;
	}
	
	/**
	 * Called when an item on the options menu is selected
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		switch(item.getItemId()){
		case R.id.home:
			finish();
			break;
		}
		return true;
	}
}
