package com.tracker;

import java.text.DecimalFormat;
import java.util.Date;

/**
 * This is the basic Log that is displayed onto the LogView
 * @author Wilson Yan
 *
 */
public class Log {
	private Date date;
	private double time;
	private double calories;
	private double distance;
	private String measurement;
	private long id;

	/**
	 * Constructor
	 * @param date
	 * @param time
	 * @param calories
	 * @param distance
	 * @param measurement
	 */
	public Log(Date date, double time, double calories, double distance, String measurement, long id){
		this.date = date;
		this.time = time;
        this.calories = calories;
		this.distance = distance;
		this.measurement = measurement;
		this.id = id;
	}

	/**
	 * This overriden method is used when the activity displays the list of logs
	 */
	@Override
	public String toString(){
		String string = date.toString() + "\n" + (int)distance;
		if(measurement.equalsIgnoreCase(Settings.MILE)){
			string += "mi ";
		}else if(measurement.equalsIgnoreCase(Settings.KILOMETER)){
			string += "km ";
		}else{
			string += "m ";
		}
		string += (int)calories + "cal ";
		double min = time/60;
		DecimalFormat decimalFormat = new DecimalFormat("#.##");
		min = Double.valueOf(decimalFormat.format(min));

		string += min + "min\n";
		return string;
	}

	/**
	 * get the date
	 * @return Date
	 */
	public Date getDate(){
		return date;
	}

	public double getTime(){
		return time;
	}

	/**
	 * the number of burned calories
	 * @return double
	 */
	public double getCalories(){
		return calories;
	}

	/**
	 * the distance traveled
	 * @return double
	 */
	public double getDistance(){
		return distance;
	}

	/**
	 * the measurement that was used
	 * @return String
	 */
	public String getMeasurement(){
		return measurement;
	}
	
	public long getId(){
		return id;
	}
}
