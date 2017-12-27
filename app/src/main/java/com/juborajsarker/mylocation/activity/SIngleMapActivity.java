package com.juborajsarker.mylocation.activity;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.util.Property;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.juborajsarker.mylocation.R;
import com.juborajsarker.mylocation.java_class.CartesianCoordinates;
import com.juborajsarker.mylocation.java_class.DataParser;
import com.juborajsarker.mylocation.java_class.LatLngInterpolator;

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

public class SIngleMapActivity extends FragmentActivity implements OnMapReadyCallback, LocationSource.OnLocationChangedListener {

    public static final long DURATION = 5000;
    public static final LatLngInterpolator latLngInterpolator = new LatLngInterpolator.Linear();
    public static final Property<Marker, LatLng> property = Property.of(Marker.class, LatLng.class, "position");
    public static final TypeEvaluator<LatLng> typeEvaluator = new TypeEvaluator<LatLng>() {
        @Override
        public LatLng evaluate(float fraction, LatLng startValue, LatLng endValue) {
            return latLngInterpolator.interpolate(fraction, startValue, endValue);
        }
    };
    double lat, lng, currentLat, currentLng;
    String address, name, nearby;
    Marker marker, marker2;
    LatLng location, location2, fromLocation;
    private GoogleMap mMap;

    private static void animateMarker(final Marker marker, final int current, final LatLng[] line) {
        if (line == null || line.length == 0 || current >= line.length) {
            return;
        }

        ObjectAnimator animator = ObjectAnimator.ofObject(marker, property, typeEvaluator, line[current]);
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                animateMarker(marker, current + 1, line);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        animator.setDuration(DURATION);
        animator.start();
    }

    private static LatLng[] bezier(LatLng p1, LatLng p2, double arcHeight, double skew, boolean up) {
        ArrayList<LatLng> list = new ArrayList<>();
        try {
            if (p1.longitude > p2.longitude) {
                LatLng tmp = p1;
                p1 = p2;
                p2 = tmp;
            }

            LatLng c = new LatLng((p1.latitude + p2.latitude) / 2, (p1.longitude + p2.longitude) / 2);

            double cLat = c.latitude;
            double cLon = c.longitude;

            //add skew and arcHeight to move the midPoint
            if (Math.abs(p1.longitude - p2.longitude) < 0.0001) {
                if (up) {
                    cLon -= arcHeight;
                } else {
                    cLon += arcHeight;
                    cLat += skew;
                }
            } else {
                if (up) {
                    cLat += arcHeight;
                } else {
                    cLat -= arcHeight;
                    cLon += skew;
                }
            }

            list.add(p1);
            //calculating points for bezier
            double tDelta = 1.0 / 10;
            CartesianCoordinates cart1 = new CartesianCoordinates(p1);
            CartesianCoordinates cart2 = new CartesianCoordinates(p2);
            CartesianCoordinates cart3 = new CartesianCoordinates(cLat, cLon);

            for (double t = 0; t <= 1.0; t += tDelta) {
                double oneMinusT = (1.0 - t);
                double t2 = Math.pow(t, 2);

                double y = oneMinusT * oneMinusT * cart1.y + 2 * t * oneMinusT * cart3.y + t2 * cart2.y;
                double x = oneMinusT * oneMinusT * cart1.x + 2 * t * oneMinusT * cart3.x + t2 * cart2.x;
                double z = oneMinusT * oneMinusT * cart1.z + 2 * t * oneMinusT * cart3.z + t2 * cart2.z;
                LatLng control = CartesianCoordinates.toLatLng(x, y, z);
                list.add(control);
            }

            list.add(p2);
        } catch (Exception e) {

        }

        LatLng[] result = new LatLng[list.size()];
        result = list.toArray(result);

        return result;
    }

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
    public void onLocationChanged(Location location) {


        LatLng toLocation = new LatLng(location.getLatitude(), location.getLongitude());

        if (fromLocation != null) {

            marker.setPosition(fromLocation);

            LatLng[] line = bezier(fromLocation, toLocation, 0, 0, true);
            Marker marker = mMap.addMarker(new MarkerOptions().position(fromLocation));
            animateMarker(marker, 0, line);

        }

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
