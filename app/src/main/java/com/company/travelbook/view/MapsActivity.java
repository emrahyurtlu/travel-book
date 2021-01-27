package com.company.travelbook.view;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.company.travelbook.R;
import com.company.travelbook.model.Place;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener, GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private SQLiteDatabase database;
    private final List<Place> places = new ArrayList<Place>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapLongClickListener(this);
        Intent intent = getIntent();
        String info = intent.getStringExtra("info");

        if (info.matches("new")) {
            locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            locationListener = location -> {
                LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
            };

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            } else {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

                Location lkl = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                if (lkl != null) {
                    LatLng lastLocation = new LatLng(lkl.getLatitude(), lkl.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastLocation, 15));
                }
            }
        } else {
            Place place = (Place) intent.getSerializableExtra("place");
            LatLng userOldLocation = new LatLng(place.getLatitude(), place.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userOldLocation, 15));
            mMap.clear();
            Marker marker = mMap.addMarker(new MarkerOptions().position(userOldLocation).title(place.getName()));
            marker.setTag(place.getId());
        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0 && requestCode == 1) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            } else {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

                Location lkl = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                if (lkl != null) {
                    LatLng lastLocation = new LatLng(lkl.getLatitude(), lkl.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastLocation, 15));
                }
            }
        }
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        String address = "";

        try {
            List<Address> addressList = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);

            if (addressList != null && addressList.size() > 0) {
                Address addr = addressList.get(0);

                address += addr.getThoroughfare();
                address += " " + addr.getSubThoroughfare();
            } else {
                address = "Unknown Place";
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        mMap.clear();

        mMap.addMarker(new MarkerOptions().title(address).position(latLng));

        Double latitude = latLng.latitude;
        Double longitude = latLng.longitude;

        Place place = new Place(address, latitude, longitude);

        AlertDialog.Builder alerDialog = new AlertDialog.Builder(MapsActivity.this);
        alerDialog.setCancelable(false);
        alerDialog.setTitle("Are you sure?");
        alerDialog.setMessage(place.getName());

        alerDialog.setPositiveButton("Yes", (dialogInterface, i) -> {
            try {
                database = MapsActivity.this.openOrCreateDatabase("Places", MODE_PRIVATE, null);
                database.execSQL("CREATE TABLE IF NOT EXISTS places (id INTEGER PRIMARY KEY,name VARCHAR, latitude VARCHAR, longitude VARCHAR)");

                String toCompile = "INSERT INTO places (name, latitude, longitude) VALUES (?, ?, ?)";

                SQLiteStatement sqLiteStatement = database.compileStatement(toCompile);
                sqLiteStatement.bindString(1, place.getName());
                sqLiteStatement.bindString(2, String.valueOf(place.getLatitude()));
                sqLiteStatement.bindString(3, String.valueOf(place.getLongitude()));
                sqLiteStatement.execute();

                Toast.makeText(getApplicationContext(), "Saved!", Toast.LENGTH_LONG).show();

            } catch (Exception e) {
                e.printStackTrace();
            }

        });

        alerDialog.setNegativeButton("No", (dialogInterface, i) -> {
            Toast.makeText(getApplicationContext(), "Calceled!", Toast.LENGTH_LONG).show();
        });

        alerDialog.show();

    }


    @Override
    public boolean onMarkerClick(Marker marker) {
        System.out.println(marker.getTag());
        return true;
    }
}