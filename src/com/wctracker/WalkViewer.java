package com.wctracker;

import java.util.ArrayList;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class WalkViewer extends AppCompatActivity {
	ArrayList<Location> walkPath;
	Database database;
	BitmapDescriptor bitmapDescriptor;
	GoogleMap mMap;
	private static Log log;

	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.simple_mapv2);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		database = new Database(this);
		long pos = getIntent().getExtras().getLong("position");
		walkPath = database.getWalkPoints(pos);
		log = database.getLog(pos);

		mMap = ((SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.map)).getMap();
		mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
		bitmapDescriptor = BitmapDescriptorFactory
				.fromResource(R.drawable.person);

		Location lastPos = new Location(LocationManager.GPS_PROVIDER);
		lastPos.setLatitude(walkPath.get(walkPath.size() - 1).getLatitude());
		lastPos.setLongitude(walkPath.get(walkPath.size() - 1).getLongitude());

		drawLines();

	}

	@Override
	public boolean onSupportNavigateUp() {
		finish();
		return true;
	}

	public static Log getLog() {
		return log;
	}

	public Marker createMarker(Location location) {
		return mMap.addMarker(new MarkerOptions().icon(bitmapDescriptor)
				.position(
						new LatLng(location.getLatitude(), location
								.getLongitude())));
	}

	public void drawLines() {
		LatLng src = new LatLng(walkPath.get(1).getLatitude(), walkPath.get(1)
				.getLongitude());
		LatLng dest = new LatLng(walkPath.get(2).getLatitude(), walkPath.get(2)
				.getLongitude());

		PolylineOptions options = new PolylineOptions().add(src, dest).width(8)
				.color(Color.RED).geodesic(true);
		for (int i = 3; i < walkPath.size(); i++) {
			LatLng next = new LatLng(walkPath.get(i).getLatitude(), walkPath
					.get(i).getLongitude());

			options.add(next);
		}
		mMap.addPolyline(options);

		Location last = walkPath.get(walkPath.size() - 1);
		LatLng latLng = new LatLng(last.getLatitude(), last.getLongitude());
		mMap.addMarker(new MarkerOptions().position(latLng).icon(
				bitmapDescriptor));

		mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 19f));

	}
}
