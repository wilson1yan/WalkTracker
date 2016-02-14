package com.wctracker;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * This is the basic Log that is displayed onto the LogView
 * 
 * @author Wilson Yan
 * 
 */
public class Log {
	private static final String[] DAYS_OF_WEEK = { "Sun", "Mon", "Tues", "Wed",
			"Thurs", "Fri", "Sat" };
	private static final String[] MONTHS = { "Jan", "Feb", "Mar", "Apr", "May",
			"June", "July", "Aug", "Sept", "Oct", "Nov", "Dec" };

	private Calendar calendar;
	private Date date;
	private int timeW;
	private double calories;
	private double distance;
	private String measurement;
	private long id;
	private double convertedDistance;

	/**
	 * Constructor
	 * 
	 * @param date
	 * @param time
	 * @param calories
	 * @param distance
	 * @param measurement
	 */
	public Log(Date date, int time, double calories, double distance,
			String measurement, long id) {
		this.calendar = Calendar.getInstance();
		this.calendar.setTime(date);
		this.date = date;
		this.timeW = time;
		this.calories = calories;
		this.distance = distance;
		this.measurement = measurement;
		this.id = id;
		this.convertedDistance = getDistance()
				* ((Double) Calculator.METRIC_CONVERSION
						.get(Calculator.measurementUnit)).doubleValue();
	}

	/**
	 * This overriden method is used when the activity displays the list of logs
	 */
	@Override
	public String toString() {
		String num = "Walk #" + (id + 1);
		String date = "Date: "
				+ DAYS_OF_WEEK[calendar.get(Calendar.DAY_OF_WEEK) - 1] + " "
				+ MONTHS[calendar.get(Calendar.MONTH) - 1] + " "
				+ calendar.get(Calendar.DAY_OF_MONTH) + ", "
				+ calendar.get(Calendar.YEAR);
		String st = "Start time: " + calendar.get(Calendar.HOUR_OF_DAY) + ":"
				+ calendar.get(Calendar.MINUTE) + ":"
				+ calendar.get(Calendar.SECOND);
		String dist = "Distance: "
				+ new DecimalFormat("#.##").format(this.convertedDistance)
				+ measurement;
		String wt = "Walk time: " + getTime(timeW);
		String cb = "Calories burned: " + (int) calories + "cal";

		String string = num + "\n" + date + "\n" + st + "\n" + dist + "\n" + wt
				+ "\n" + cb;
		return string;
	}

	private String getTime(int time) {
		String text = "";
		int remainder;
		int hours, minutes, seconds;

		if (time >= 3600) {
			hours = (int) (time / 3600.0);
			minutes = time % 3600;

			text += hours + "h ";

			remainder = minutes % 60;
			minutes = (int) (minutes / 60.0);

			text += minutes + "m " + remainder + "s";
		} else if (time >= 60) {
			seconds = time % 60;
			minutes = (int) (time / 60.0);

			text += minutes + "m " + seconds + "s";
		} else {
			text += time + "s";
		}

		return text;
	}

	public String getShortString() {

		return "Walk #" + (id + 1);
	}

	/**
	 * get the date
	 * 
	 * @return Date
	 */
	public Date getDate() {
		return date;
	}

	public double getTime() {
		return timeW;
	}

	/**
	 * the number of burned calories
	 * 
	 * @return double
	 */
	public double getCalories() {
		return calories;
	}

	/**
	 * the distance traveled
	 * 
	 * @return double
	 */
	public double getDistance() {
		return distance;
	}

	public double getConvertedDistance() {
		return convertedDistance;
	}

	/**
	 * the measurement that was used
	 * 
	 * @return String
	 */
	public String getMeasurement() {
		return measurement;
	}

	public long getId() {
		return id;
	}
}
