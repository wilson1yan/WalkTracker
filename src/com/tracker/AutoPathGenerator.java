package com.tracker;

import java.util.Random;

import android.location.Location;

public class AutoPathGenerator {
	private static final String PROVIDER = "TEST";
	public static final double LATITUDE = 37.775292;
	public static final double LONGITUDE = -121.897062;
	public static final double TRAVEL_DISTANCE = 0.000025;
	private static final int MIN_DIRECTION_CHANGE = 70;
	
	private Location prevLocation;
	
	private int counter;
	private int max;
	
	private Random random;
	
	private double direction;
	private double direction_radians;
	
	public AutoPathGenerator(){
		random = new Random();
		
		counter = 0;
		max = random.nextInt(10)+1;
		direction = random.nextInt(360);
		direction_radians = Math.toRadians(direction);
	}
	
	public void setPrevLocation(Location location){
		prevLocation = location;
	}
	
	public Location getNextLocation(){
		Location location = new Location(PROVIDER);
		
		if(prevLocation != null){
			if(counter == max){
				counter = 0;
				max = random.nextInt(10)+1;
				
				direction = random.nextInt(MIN_DIRECTION_CHANGE*2)-MIN_DIRECTION_CHANGE;
				direction_radians = Math.toRadians(direction);
			}
			
			location.setLatitude(prevLocation.getLatitude()+TRAVEL_DISTANCE*Math.sin(direction_radians));
			location.setLongitude(prevLocation.getLongitude()+TRAVEL_DISTANCE*Math.cos(direction_radians));
			
			counter++;
			
		}else{
			location.setLatitude(LATITUDE);
			location.setLongitude(LONGITUDE);
		}
		
		return location;
	}
	
	public void reset(){
		counter = 0;
		
		prevLocation = null;
	}
}
