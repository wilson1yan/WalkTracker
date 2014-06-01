package com.tracker;

import java.util.ArrayList;

import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

public class MapHandler {
	GoogleMap map;
	BitmapDescriptor bitmapDescriptor;
	MarkerOptions markerOptions;
	Marker lastPosition;
	PolylineOptions walkPathLine;
	
	private int counter = 0;
	
	public MapHandler(GoogleMap map){
		this.map = map;
		this.bitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.person);
		
		map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
		walkPathLine = new PolylineOptions().width(8).color(Color.RED);
	}
	
	public void updateMap(ArrayList<Location> path){		
		Location loc1 = path.get(path.size()-2);
		Location loc2 = path.get(path.size()-1);
		
		
		if(path.size()>=2){
			addToPath(new LatLng(loc1.getLatitude(), loc1.getLongitude()), new LatLng(loc2.getLatitude(), loc2.getLongitude()));
			counter++;
		}
		updateWalkerLoc(path.get(path.size()-1));
	}
	
	public void updateWalkerLoc(Location location){
		if(lastPosition != null){
			lastPosition.remove();
		}
		
		lastPosition = createMarker(location);
	}
	
	public void animateCamera(WalkTrackerApplication walktracker){
		if(walktracker.getCurrentWalkPath().size() <= 2 || (walktracker.isCenter() && walktracker.isZoom())){
			getMap().animateCamera(CameraUpdateFactory.newLatLngZoom(lastPosition.getPosition(), 18f));
		}else if(walktracker.isZoom() && !walktracker.isCenter()){
			getMap().animateCamera(CameraUpdateFactory.zoomTo(18f));
		}else if(!walktracker.isZoom() && walktracker.isCenter()){
			getMap().animateCamera(CameraUpdateFactory.newLatLng(lastPosition.getPosition()));
		}
	}
	
	private Marker createMarker(Location location){
		markerOptions = new MarkerOptions();
        markerOptions.icon(bitmapDescriptor);
                
        markerOptions.position(new LatLng(location.getLatitude(), location.getLongitude()));
        
        return getMap().addMarker(markerOptions);
	}
	
	private void addToPath(LatLng current, LatLng last){
		if(counter == 20){
			getMap().clear();
			getMap().addPolyline(walkPathLine);
			counter = 0;
		}else{
			getMap().addPolyline(new PolylineOptions().color(Color.RED).width(8).add(current).add(last));
			walkPathLine.add(current);
		}
	}
	
	public GoogleMap getMap(){
		return this.map;
	}
	
	public void drawCurrentPath(ArrayList<Location> walkPath){
		PolylineOptions polylineOptions = new PolylineOptions().width(8).color(Color.RED);
		Location lastLoc = new Location(LocationManager.GPS_PROVIDER);
		boolean foundLoc = false;
		for(int i=1; i<walkPath.size(); i++){
			LatLng current = new LatLng(walkPath.get(i).getLatitude(), walkPath.get(i).getLongitude());			
			polylineOptions.add(current);
			lastLoc = walkPath.get(i);
			foundLoc = true;
		}
		getMap().clear();
		if(foundLoc) lastPosition = createMarker(lastLoc);
		getMap().addPolyline(polylineOptions);
		
	}
	
	public void clearMap(){
		getMap().clear();	
	}
	
	public void reset(){
		walkPathLine = new PolylineOptions().width(8).color(Color.RED);
	}
}
