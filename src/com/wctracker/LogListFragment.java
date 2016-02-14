package com.wctracker;

import java.util.ArrayList;

import com.google.gson.Gson;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ListFragment;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class LogListFragment extends ListFragment {
	ArrayList<Log> logs;
	Database database;
	Gson gson = new Gson();
	SharedPreferences sharedPrefs;
	ArrayAdapter<Log> adapter;
	WalkTrackerApplication walktracker;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.log_list_view, container, false);
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		database = new Database(getActivity());
		logs = database.getLogs();
		//Collections.reverse(logs);

		walktracker = (WalkTrackerApplication) getActivity().getApplication();

		adapter = new ArrayAdapter<Log>(getActivity(), R.layout.log_list,
				R.id.log, logs);
		setListAdapter(adapter);
		sharedPrefs = PreferenceManager
				.getDefaultSharedPreferences(getActivity());

		registerForContextMenu(getListView());
	}

	@Override
	public void onCreateContextMenu(ContextMenu contextMenu, View v,
			ContextMenuInfo menuInfo) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
		contextMenu.setHeaderTitle(logs.get(info.position).getShortString());
		String[] menuItems = getResources().getStringArray(R.array.log_options);
		for (int i = 0; i < menuItems.length; i++) {
			contextMenu.add(Menu.NONE, i, i, menuItems[i]);
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item
				.getMenuInfo();
		int menuItemIndex = item.getItemId();

		if (menuItemIndex == 0) {
			walktracker.getDatabase().deleteLog(
					logs.get(info.position).getId(), getActivity());
			adapter.remove(logs.get(info.position));
		}

		return true;
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Log log = (Log) l.getItemAtPosition(position);

		String logString = gson.toJson(log);
		Intent intent;

		intent = new Intent(getActivity(), WalkViewer.class);

		intent.putExtra("log", logString);
		intent.putExtra("position", log.getId());

		startActivity(intent);
	}
}
