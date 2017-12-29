package com.juborajsarker.mylocation.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.juborajsarker.mylocation.R;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    double lat, lng;
    LatLng location;
    Marker marker;
    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Intent intent = getIntent();

        lat = intent.getDoubleExtra("lat", 0);
        lng = intent.getDoubleExtra("lng", 0);


        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                (this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            return;
        }


        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

//                Toast.makeText(MapsActivity.this, String.valueOf(location.getLatitude()) + "\n"
//                        + location.getLongitude() , Toast.LENGTH_SHORT).show();


                LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());

                marker.remove();
                marker = mMap.addMarker(new MarkerOptions().position(currentLocation)
                        .title("Your Location")
                        .alpha(1f));
                marker.showInfoWindow();

                mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));
                mMap.getUiSettings().setCompassEnabled(true);
                mMap.getUiSettings().setZoomControlsEnabled(true);
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15.0f));

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {


            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        });


    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in current location and move the camera
        location = new LatLng(lat, lng);
        marker = mMap.addMarker(new MarkerOptions().position(location)
                .title("Your Location")
                .alpha(1f));

        marker.showInfoWindow();

        mMap.moveCamera(CameraUpdateFactory.newLatLng(location));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 15.0f));
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);


    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
