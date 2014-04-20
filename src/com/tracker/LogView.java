package com.tracker;


import java.util.ArrayList;

import com.google.gson.Gson;

import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/**
 * The activity in which you can view all the logs
 * @author Wilson Yan
 *
 */
public class LogView extends ListActivity{
	ListView listView;
	ArrayList<Log> logs;
	Database database;
	Gson gson = new Gson();
	SharedPreferences sharedPrefs;

	/**
	 * Called when the activity is created
	 * Makes the adapter and sets the format/ look of the activity
	 */
	public void onCreate(Bundle bundle){
		super.onCreate(bundle);
		database = new Database(this);
		logs = database.getLogs();

		setListAdapter(new ArrayAdapter<Log>(this, R.layout.log_list,R.id.log, logs));
		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Log log = (Log)l.getItemAtPosition(position);

		String logString = gson.toJson(log);
		Intent intent;

		intent = new Intent(this, WalkViewer.class);


		intent.putExtra("log", logString);
		intent.putExtra("position", log.getId());

		startActivity(intent);
	}

	/**
	 * Creates and Inflates the options menu
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
