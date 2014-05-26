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
	
	public MapHandler(GoogleMap map){
		this.map = map;
		this.bitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.person);
		
		map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
		
	}
	
	public void updateMap(ArrayList<Location> path){
		Location lastLocation = path.get(path.size()-1);
		
		updateWalkerLoc(lastLocation);
		
		if(path.size()>=3){
			addToPath(path.get(path.size()-1), path.get(path.size()-2));
		}
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
	
	private void addToPath(Location prev, Location current){
		LatLng src = new LatLng(prev.getLatitude(), prev.getLongitude());
		LatLng dest = new LatLng(current.getLatitude(), current.getLongitude());
		
		getMap().addPolyline(new PolylineOptions().add(src, dest).width(8).color(Color.RED).geodesic(true));
	}
	
	public GoogleMap getMap(){
		return this.map;
	}
	
	public void drawCurrentPath(ArrayList<Location> walkPath){
		PolylineOptions polylineOptions = new PolylineOptions().width(8).color(Color.RED).geodesic(true);
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
}
