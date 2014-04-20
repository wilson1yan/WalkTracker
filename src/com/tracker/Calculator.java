package com.tracker;

import java.util.HashMap;

import android.content.Intent;
import android.content.SharedPreferences;


public class Calculator {
	public static final HashMap<String, Double> METRIC_CONVERSION = new HashMap<String, Double>(){{put("mi", 0.000621371); put("m", 1.0); put("km", 0.001); put("kg", 1/2.2046);}};
	public static final double LBS_TO_KG_CONVERSION = 1/2.2046;
	public static final double MAX_WALKING_SPEED = 10;
	
	public static final String DISTANCE = "DISTANCE_TO_CANVAS";
	public static final String CALORIES = "CALORIES_TO_CANVAS";
	public static final String START_TIME = "START_TIME_TO_CANVAS";
	public static final String CANVAS_UPDATE = "CANVAS_UPDATE";

	public static double totalDistance, totalCalories, convertedDistance;
	public static long startTime;
	private double weight;
	
	public static String measurementUnit, calorieType;
	
	public Calculator(double totalDistance, double totalCalories, long startTime, double weight, String measurementUnit, String calorieType){
		super();
		
		this.totalDistance = totalDistance;
		this.totalCalories = totalCalories;
		this.weight = weight;
		this.startTime = startTime;
		
		this.measurementUnit = measurementUnit;
		this.calorieType = calorieType;
		
		this.convertedDistance = (totalDistance*((Double)METRIC_CONVERSION.get(measurementUnit)).doubleValue());
	}
	
	public void reset(){
		Calculator.totalDistance = 0;
		this.totalCalories = 0;
		this.totalDistance = 0;
		this.convertedDistance = 0;
		this.startTime = -1;
	}
	
	public void begin(){
		this.startTime = System.currentTimeMillis();
	}
	
	public void calculate(double distance, double timeLength){
		double speed = findSpeed(distance, timeLength/3600);
		this.totalDistance += distance;

		if(speed<=MAX_WALKING_SPEED){
			totalCalories += calculateCalories(distance, timeLength, weight, this.calorieType);
			
		}
		
		convertedDistance = (totalDistance*((Double)METRIC_CONVERSION.get(this.measurementUnit)).doubleValue());
	
	}
	
	private double findSpeed(double distance, double hours){
		double km = 0;
		km = (distance/(Double)METRIC_CONVERSION.get(this.measurementUnit))/1000; 
		return km/hours;
	}
	
	private double calculateCalories(double distance, double timeSec, double weight, String calorieType){
		double hours = timeSec/3600;
		double kph = findSpeed(distance, hours);
		double kg = weight*LBS_TO_KG_CONVERSION;
		double calories;
		
		if(calorieType.equalsIgnoreCase("Gross")){
			calories = (0.0215*Math.pow(kph, 3) - 0.1765*Math.pow(kph, 2) + 0.8710*kph + 1.4577)*kg*hours;
		}else{
			calories = (0.0215*Math.pow(kph, 3) - 0.1765*Math.pow(kph, 2) + 0.8710*kph)*kg*hours;
		}
		
		return calories;
	}
	
	public void updateInfo(SharedPreferences sharedPreferences){
		sharedPreferences.edit().putInt(Settings.CURRENT_DISTANCE_KEY, (int)this.totalDistance).commit();
		sharedPreferences.edit().putInt(Settings.CURRENT_CALORIE_KEY, (int)this.totalCalories).commit();
		sharedPreferences.edit().putLong(Settings.CURRENT_START_KEY, startTime).commit();
	}
	
	public Intent packageInfoForCanvas(){
		Intent packagedInfo = new Intent(CANVAS_UPDATE);
		
		packagedInfo.putExtra(DISTANCE, this.totalDistance);
		packagedInfo.putExtra(CALORIES, this.totalCalories);
		packagedInfo.putExtra(START_TIME, this.startTime);
		
		return packagedInfo;
	}
}
