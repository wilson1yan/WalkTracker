package com.wctracker;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

public class MapHandler {
	GoogleMap map;
	BitmapDescriptor bitmapDescriptor;
	MarkerOptions markerOptions;
	Marker lastPosition;
	PolylineOptions walkPathLine;
	
	public int centerCounter = 10;
	
	public MapHandler(GoogleMap map){
		this.map = map;
		this.bitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.person);
		
		map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
		walkPathLine = new PolylineOptions().width(8).color(Color.RED);
	}
	
	public void updateMap(ArrayList<Location> path){		
		Location loc1 = path.get(path.size()-2);
		Location loc2 = path.get(path.size()-1);
		
		
		if(path.size()>=3){
			addToPath(new LatLng(loc1.getLatitude(), loc1.getLongitude()), new LatLng(loc2.getLatitude(), loc2.getLongitude()));
		}
		updateWalkerLoc(path.get(path.size()-1));
	}
	
	public void updateWalkerLoc(Location location){
		if(lastPosition != null){
			lastPosition.remove();
		}
		
		lastPosition = createMarker(location);
	}
	
	public void animateCamera(WalkTrackerApplication walktracker, Context context){
		if (walktracker.getCurrentWalkPath().size() <= 2
				|| (walktracker.isCenter() && walktracker.isZoom())) {
			getMap().animateCamera(
					CameraUpdateFactory.newLatLngZoom(
							lastPosition.getPosition(), 18f));
		} else if (walktracker.isZoom() && !walktracker.isCenter()) {
			getMap().animateCamera(CameraUpdateFactory.zoomTo(18f));
		} else if (!walktracker.isZoom() && walktracker.isCenter()) {
			getMap().animateCamera(
					CameraUpdateFactory.newLatLng(lastPosition.getPosition()));
		}
	}
	
	private Marker createMarker(Location location){
		markerOptions = new MarkerOptions();
        markerOptions.icon(bitmapDescriptor);
                
        markerOptions.position(new LatLng(location.getLatitude(), location.getLongitude()));
        
        return getMap().addMarker(markerOptions);
	}
	
	private void addToPath(LatLng current, LatLng last){
		getMap().addPolyline(new PolylineOptions().color(Color.RED).width(8).add(current).add(last));
		walkPathLine.add(current);
	}
	
	public GoogleMap getMap(){
		return this.map;
	}
	
	public void drawCurrentPath(ArrayList<Location> walkPath){
		Location lastloc = null;
		if(walkPath.size()>0){
			lastloc = walkPath.get(walkPath.size()-1);
		}
		if(lastloc!=null){
			if(lastPosition != null) lastPosition.remove();
			lastPosition = createMarker(lastloc);
		}
		getMap().addPolyline(walkPathLine);
		
	}
	
	public void clearMap(){
		getMap().clear();	
	}
	
	public void reset(){
		walkPathLine = new PolylineOptions().width(8).color(Color.RED);
	}
	
	public ArrayList<PolylineOptions> findLinesWithinBounds(ArrayList<LatLng> path){
		ArrayList<PolylineOptions> visible = new ArrayList<PolylineOptions>();
		LatLngBounds bounds = getMap().getProjection().getVisibleRegion().latLngBounds;
		PolylineOptions line = null;
		boolean first,next;
		
		for(int i=1; i<path.size()-1; i++){
			first = bounds.contains(path.get(i));
			next = bounds.contains(path.get(i+1));
			
			if(!first && next){
				line = null;
				line = new PolylineOptions().color(Color.RED).width(8).add(path.get(i)).add(path.get(i+1));
			}else if(first && next){
				if(line == null){
					line = new PolylineOptions().color(Color.RED).width(8).add(path.get(i)).add(path.get(i+1));
				}else{
					line.add(path.get(i+1));
				}
			}else if(first && !next){
				line.add(path.get(i+1));
				visible.add(line);
				line = null;
			}
		}
		
		if(line != null) visible.add(line);
		
		return visible;
	}
	
	public void redraw(ArrayList<PolylineOptions> lines){
		getMap().clear();
		for(int i=0; i<lines.size(); i++){
			getMap().addPolyline(lines.get(i));
		}
	}
	
	public void updateBounds(ArrayList<LatLng> path){
		ArrayList<PolylineOptions> lines = findLinesWithinBounds(path);
		redraw(lines);
		
		if(path.size()>1){
			Location loc = new Location(LocationManager.GPS_PROVIDER);
			loc.setLatitude(path.get(path.size()-1).latitude);
			loc.setLongitude(path.get(path.size()-1).longitude);
			
			if(lastPosition != null) lastPosition.remove();
			lastPosition = createMarker(loc);
		}
	}
}
