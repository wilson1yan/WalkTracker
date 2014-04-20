package com.tracker;

import java.util.ArrayList;

import android.graphics.Color;
import android.location.Location;

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
		
		if(lastPosition != null){
			lastPosition.remove();
		}
		
		lastPosition = createMarker(lastLocation);
		
		if(path.size()>=2){
			addToPath(path.get(path.size()-1), path.get(path.size()-2));
		}

		getMap().animateCamera(CameraUpdateFactory.newLatLngZoom(lastPosition.getPosition(), 18f));
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
		for(int i=1; i<walkPath.size(); i++){
			LatLng src = new LatLng(walkPath.get(i-1).getLatitude(), walkPath.get(i-1).getLongitude());
			LatLng dest = new LatLng(walkPath.get(i).getLatitude(), walkPath.get(i).getLongitude());
			
			getMap().addPolyline(new PolylineOptions().add(src, dest).width(8).color(Color.RED).geodesic(true));
		}
	}
	
	public void clearMap(){
		getMap().clear();
		
	}
}
