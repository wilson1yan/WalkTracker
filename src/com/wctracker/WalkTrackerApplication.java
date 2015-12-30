package com.wctracker;

import java.util.ArrayList;
import java.util.Date;

import com.google.android.gms.maps.model.LatLng;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.location.Location;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class WalkTrackerApplication extends Application implements
		OnSharedPreferenceChangeListener {
	private SharedPreferences sharedPreferences;
	private Database database;
	private ArrayList<Location> currentWalkPath;
	private ArrayList<LatLng> currentWalkPathLatLng;
	// private boolean isTest;
	private boolean center;
	private boolean zoom;

	@Override
	public void onCreate() {
		super.onCreate();
		this.sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(this);
		this.sharedPreferences.registerOnSharedPreferenceChangeListener(this);

		this.database = new Database(this);

		this.currentWalkPath = getDatabase().getCurrentWalkPath();
		this.currentWalkPathLatLng = convertToLatLng(getCurrentWalkPath());

		updateValuesFromPreferences();
	}

	private ArrayList<LatLng> convertToLatLng(ArrayList<Location> path) {
		ArrayList<LatLng> convert = new ArrayList<LatLng>();
		for (Location location : path) {
			convert.add(new LatLng(location.getLatitude(), location
					.getLongitude()));
		}

		return convert;
	}

	@Override
	public void onTerminate() {
		super.onTerminate();
	}

	private void updateValuesFromPreferences() {
		// this.isTest = sharedPreferences.getBoolean(Settings.TEST, true);
		this.center = sharedPreferences.getBoolean(Settings.CENTER, true);
		this.zoom = sharedPreferences.getBoolean(Settings.ZOOM, true);
		Calculator.measurementUnit = sharedPreferences.getString(
				Settings.MEASUREMENT, "m");
	}

	public void onSharedPreferenceChanged(SharedPreferences pref, String key) {
		if (key.equalsIgnoreCase(Settings.WEIGHT)) {
			String wt = pref.getString(key, "150");
			try {
				Double.parseDouble(wt);
			} catch (NumberFormatException e) {
				Toast.makeText(this, "Please Input a Valid Number",
						Toast.LENGTH_LONG).show();
				pref.edit().putString(Settings.WEIGHT, "150").commit();
			}
		}

		updateValuesFromPreferences();
	}

	public boolean isTest() {
		// return this.isTest;
		return false;
	}

	public boolean isZoom() {
		return this.zoom;
	}

	public boolean isCenter() {
		return this.center;
	}

	public SharedPreferences getSharedPreferences() {
		return sharedPreferences;
	}

	public void saveLog(ArrayList<Location> walkPathToSave, double calories,
			double distance, String measurement) {
		long walkId = getDatabase().getMaxId(DbHelper.WALKING_GEOPOINT,
				Database.MAX_WALK_ID);

		Date date = new Date();

		int time = (int) ((System.currentTimeMillis() - Calculator.startTime) / 1000);
		Log log = new Log(date, time, calories, distance, measurement, walkId);

		database.updateDatabaseLog(log);
	}

	public void updateLocationToDatabase(Location location, boolean forReset) {
		getDatabase().updateDatabasePoint(location, forReset);
	}

	public Database getDatabase() {
		return this.database;
	}

	public ArrayList<Location> getCurrentWalkPath() {
		return this.currentWalkPath;
	}

	public ArrayList<LatLng> getCurrentWalkPathLatLng() {
		return this.currentWalkPathLatLng;
	}

}
