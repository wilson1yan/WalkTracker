package com.tracker;



import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

/**
 * This is the settings class
 * It helps you be able to personlize it more
 * you can adjust weight, and the measurement unit as well
 * @author Wilson Yan
 *
 */
public class Settings extends PreferenceActivity{
	//All of the keys used in the preferences
	public static final String WEIGHT = "weight";
	public static final String MEASUREMENT = "measurement";
	public static final String CALORIE_BURN = "calorieBurn";
	public static final String TEST = "artificial";
	public static final String ZOOM = "zoom";
	public static final String CENTER = "center";

	public static final String KILOMETER = "km";
	public static final String MILE = "mi";
	public static final String METER = "m";
	public static final String FEET = "ft";

	public static final String CURRENT_DISTANCE_KEY = "current distance";
	public static final String CURRENT_CALORIE_KEY = "current calorie";
	public static final String CURRENT_START_KEY = "current start";




	/**
	 * Called when activity is first created
	 */
	@Override
	protected void onCreate(Bundle bundle){
		super.onCreate(bundle);
		addPreferencesFromResource(R.xml.settings);
	}

	/**
	 * Creates and Inflates option menu
	 */
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
