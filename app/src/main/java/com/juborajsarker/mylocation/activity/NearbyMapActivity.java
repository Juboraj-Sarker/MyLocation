package com.juborajsarker.mylocation.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.juborajsarker.mylocation.R;
import com.juborajsarker.mylocation.java_class.Place;
import com.juborajsarker.mylocation.java_class.PlacesList;

public class NearbyMapActivity extends FragmentActivity implements OnMapReadyCallback {

    GeoPoint geoPoint;
    PlacesList nearPlaces;
    MapController mc;
    MapView mapView;
    Marker marker;
    int count = 0;
    double latitude, lat;
    double longitude, lng;
    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearby_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Intent intent = getIntent();
        lat = intent.getDoubleExtra("lat", 0);
        lng = intent.getDoubleExtra("lng", 0);
        nearPlaces = (PlacesList) intent.getSerializableExtra("near_places");



    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng location = new LatLng(lat, lng);


        if (nearPlaces.results != null) {

            for (Place place : nearPlaces.results) {

                count++;

                latitude = place.geometry.location.lat; // latitude
                longitude = place.geometry.location.lng; // longitude

                location = new LatLng(latitude, longitude);

                marker = mMap.addMarker(new MarkerOptions().position(location)
                        .title(place.name)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                        .snippet(place.vicinity)
                        .alpha(1f));
                marker.showInfoWindow();


                mMap.moveCamera(CameraUpdateFactory.newLatLng(location));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 15.0f));
                mMap.getUiSettings().setCompassEnabled(true);
                mMap.getUiSettings().setZoomControlsEnabled(true);

            }

        }


        location = new LatLng(lat, lng);
        mMap.addMarker(new MarkerOptions().position(location).title("Your Location")).showInfoWindow();
        mMap.moveCamera(CameraUpdateFactory.newLatLng(location));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 15.5f));
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);




    }
}
