package com.juborajsarker.mylocation.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.juborajsarker.mylocation.R;
import com.juborajsarker.mylocation.java_class.DataParser;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SIngleMapActivity extends FragmentActivity implements OnMapReadyCallback {


    double lat, lng, currentLat, currentLng;
    String address, name, nearby;
    Marker marker, marker2;
    LatLng location, location2, fromLocation;
    private GoogleMap mMap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_map);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Intent intent = getIntent();

        lat = intent.getDoubleExtra("lat", 0);
        lng = intent.getDoubleExtra("lng", 0);

        currentLat = intent.getDoubleExtra("currentLat", 0);
        currentLng = intent.getDoubleExtra("currentLng", 0);

        address = intent.getStringExtra("address");
        name = intent.getStringExtra("name");
        nearby = intent.getStringExtra("nearby");


        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {


                LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());

                marker.remove();
                marker = mMap.addMarker(new MarkerOptions().position(currentLocation)
                        .title("Your Location")
                        .alpha(1f));


                //  mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));
                mMap.getUiSettings().setCompassEnabled(true);
                mMap.getUiSettings().setZoomControlsEnabled(true);
                //   mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location2, mMap.getCameraPosition().zoom));


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


        location2 = new LatLng(lat, lng);
        marker2 = mMap.addMarker(new MarkerOptions().position(location2)
                .title(name)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                .snippet(address)
                .alpha(1f));

        marker2.showInfoWindow();
        mMap.moveCamera(CameraUpdateFactory.newLatLng(location2));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location2, 15.0f));
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);


        location = new LatLng(currentLat, currentLng);
        fromLocation = new LatLng(currentLat, currentLng);
        marker = mMap.addMarker(new MarkerOptions().position(location).
                title("Your Location")
                .alpha(1f));

        marker.showInfoWindow();

        mMap.moveCamera(CameraUpdateFactory.newLatLng(location));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 15.0f));
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);


        String url = getUrl(location, location2);
        Log.d("onMapClick", url.toString());
        FetchUrl FetchUrl = new FetchUrl();

        FetchUrl.execute(url);
        //move map camera
        mMap.moveCamera(CameraUpdateFactory.newLatLng(location));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(16.5f));


        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {

                marker.showInfoWindow();

                return false;
            }
        });


    }

    private String getUrl(LatLng origin, LatLng dest) {

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;


        // Sensor enabled
        String sensor = "sensor=false";

        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;


        return url;
    }

    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();
            Log.d("downloadUrl", data.toString());
            br.close();

        } catch (Exception e) {
            Log.d("Exception", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    // Fetches data from url passed
    private class FetchUrl extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try {
                // Fetching the data from web service
                data = downloadUrl(url[0]);
                Log.d("Background Task data", data.toString());
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);

        }
    }

    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                Log.d("ParserTask", jsonData[0].toString());
                DataParser parser = new DataParser();
                Log.d("ParserTask", parser.toString());

                // Starts parsing data
                routes = parser.parse(jObject);
                Log.d("ParserTask", "Executing routes");
                Log.d("ParserTask", routes.toString());

            } catch (Exception e) {
                Log.d("ParserTask", e.toString());
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points;
            PolylineOptions lineOptions = null;

            // Traversing through all the routes
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(10);
                lineOptions.color(Color.RED);

                Log.d("onPostExecute", "onPostExecute lineoptions decoded");

            }

            // Drawing polyline in the Google Map for the i-th route
            if (lineOptions != null) {
                mMap.addPolyline(lineOptions);
            } else {
                Log.d("onPostExecute", "without Polylines drawn");
            }
        }
    }


}
