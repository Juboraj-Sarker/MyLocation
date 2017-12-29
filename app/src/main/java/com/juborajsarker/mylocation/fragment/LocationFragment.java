package com.juborajsarker.mylocation.fragment;


import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.juborajsarker.mylocation.R;
import com.juborajsarker.mylocation.activity.MapsActivity;
import com.juborajsarker.mylocation.java_class.CustomAdapter;
import com.juborajsarker.mylocation.java_class.DataModel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.content.Context.LOCATION_SERVICE;


public class LocationFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {


    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private final static int ALL_PERMISSIONS_RESULT = 101;
    private static CustomAdapter adapter;
    final String TAG = "GPS";
    public Handler handler = new Handler();
    public final Runnable runnable = new Runnable() {
        @Override
        public void run() {


            handler.postDelayed(runnable, 1000);
        }


    };
    View view;
    Button btnViewDetails, btnShowOnMap;
    ListView detailsLV;
    ArrayList<DataModel> dataModels;
    int counter = 0;
    Location location;
    Double latitude;
    Double longitude;
    Address returnAddress;
    LocationManager locationManager;
    ArrayList<String> permissions = new ArrayList<>();
    ArrayList<String> permissionsToRequest;
    ArrayList<String> permissionsRejected = new ArrayList<>();
    boolean isGPS = false;
    boolean isNetwork = false;
    boolean canGetLocation = true;
    boolean connected, error = false;
    ProgressDialog pDialog;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private double currentLatitude;
    private double currentLongitude;


    public LocationFragment() {


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_location, container, false);


        execute();
        init();


        detailsLV = (ListView) view.findViewById(R.id.detailsLV);
        dataModels = new ArrayList<>();
        adapter = new CustomAdapter(dataModels, getActivity().getApplicationContext());


        pDialog = new ProgressDialog(view.getContext());
        pDialog.setMessage(Html.fromHtml("<b>Getting GPS ready</b><br/>Please wait...."));
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(false);
        pDialog.show();

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                pDialog.dismiss();

            }
        }, 5000);


        btnViewDetails = (Button) view.findViewById(R.id.btn_ViewDetails);
        btnShowOnMap = (Button) view.findViewById(R.id.btn_showOnMap);
        btnViewDetails.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View v) {

                counter++;
                pDialog = new ProgressDialog(view.getContext());
                pDialog.setMessage(Html.fromHtml("<b>Loading data</b><br/>Please wait...."));
                pDialog.setIndeterminate(false);
                pDialog.setCancelable(false);
                pDialog.show();

                execute();
                init();


                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        pDialog.dismiss();

                        if (counter == 1) {

                            AlertDialog.Builder builder;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                builder = new AlertDialog.Builder(getContext(), android.R.style.Theme_Material_Light_Dialog);
                            } else {
                                builder = new AlertDialog.Builder(getContext());
                            }
                            builder.setTitle("Notice !!!")
                                    .setMessage("For first time while GPS is on there have some difference in location value. So Please wait a while to accurate data. Thank you")
                                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            // continue with delete

                                            dialog.dismiss();


                                        }
                                    }).setCancelable(false)
                                    .show();
                        }

                    }
                }, 2000);


                if (mGoogleApiClient.isConnected()) {


                    runToFetchData();

                } else if (mGoogleApiClient.isConnecting()) {


                } else {

                    mGoogleApiClient.connect();
                    runToFetchData();

                }


            }
        });


        btnShowOnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                execute();
                init();


                if (!mGoogleApiClient.isConnected()) {

                    mGoogleApiClient.connect();
                }


                if (mGoogleApiClient.isConnecting()) {


                }


                runToFetchDataForMap();


            }
        });

        return view;
    }

    private void runToFetchData() {


        if (checkLocationValidity()) {

            Geocoder geocoder = new Geocoder(getContext());

            try {
                List<Address> addresses = geocoder.getFromLocation(currentLatitude, currentLongitude, 1);

                if (geocoder.isPresent()) {

                    StringBuilder stringBuilder = new StringBuilder();

                    if (addresses.size() > 0) {

                        returnAddress = addresses.get(0);

                        latitude = currentLatitude;
                        longitude = currentLongitude;


                        if (latitude != 0 && longitude != 0) {


                            executeData();
                            fetchData();


                        } else {


                            Toast.makeText(getContext(), "Error", Toast.LENGTH_SHORT).show();
                        }


                    }
                } else {

                }
            } catch (IOException e) {
                e.printStackTrace();
            }


        } else if (!checkLocationValidity()) {

            showSettingsAlert();

        }
    }

    private void runToFetchDataForMap() {


        if (checkLocationValidity()) {

            Geocoder geocoder = new Geocoder(getContext());

            try {
                List<Address> addresses = geocoder.getFromLocation(currentLatitude, currentLongitude, 1);
                if (geocoder.isPresent()) {
                    StringBuilder stringBuilder = new StringBuilder();
                    if (addresses.size() > 0) {
                        returnAddress = addresses.get(0);

                        latitude = currentLatitude;
                        longitude = currentLongitude;


                        if (latitude != 0 && longitude != 0) {


                            Intent intent = new Intent(getContext(), MapsActivity.class);
                            intent.putExtra("lat", latitude);
                            intent.putExtra("lng", longitude);
                            startActivity(intent);


                        }


                    }
                } else {

                }
            } catch (IOException e) {
                e.printStackTrace();
            }


        } else if (!checkLocationValidity()) {

            showSettingsAlert();

        }
    }

    @Override
    public void onResume() {
        super.onResume();


        mGoogleApiClient.connect();

    }

    @Override
    public void onPause() {
        super.onPause();


        if (mGoogleApiClient.isConnected()) {
//            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, (com.google.android.gms.location.LocationListener) this);
            mGoogleApiClient.disconnect();
        }


    }

    private void execute() {

        mGoogleApiClient = new GoogleApiClient.Builder(getContext())

                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)

                .addApi(LocationServices.API)
                .build();


        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(0)
                .setFastestInterval(0);


    }

    private void init() {


        locationManager = (LocationManager) getActivity().getSystemService(LOCATION_SERVICE);
        isGPS = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        isNetwork = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        permissionsToRequest = findUnAskedPermissions(permissions);


        if (!isGPS && !isNetwork) {
            Toast.makeText(getContext(), "GPS Off !!!", Toast.LENGTH_SHORT).show();
            showSettingsAlert();
        } else {
            Log.d(TAG, "Connection on");
            // check permissions
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (permissionsToRequest.size() > 0) {
                    requestPermissions(permissionsToRequest.toArray(new String[permissionsToRequest.size()]),
                            ALL_PERMISSIONS_RESULT);
                    Log.d(TAG, "Permission requests");
                    canGetLocation = false;
                }
            }


            //getLocation();
        }
    }

    private ArrayList findUnAskedPermissions(ArrayList<String> wanted) {
        ArrayList result = new ArrayList();

        for (String perm : wanted) {
            if (!hasPermission(perm)) {
                result.add(perm);
            }
        }

        return result;
    }

    private boolean hasPermission(String permission) {
        if (canAskPermission()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return (getContext().checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED);
            }
        }
        return true;
    }

    private boolean canAskPermission() {
        return (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1);
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case ALL_PERMISSIONS_RESULT:
                Log.d(TAG, "onRequestPermissionsResult");
                for (String perms : permissionsToRequest) {
                    if (!hasPermission(perms)) {
                        permissionsRejected.add(perms);
                    }
                }

                if (permissionsRejected.size() > 0) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (shouldShowRequestPermissionRationale(permissionsRejected.get(0))) {
                            showMessageOKCancel("These permissions are mandatory for the application. Please allow access.",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                requestPermissions(permissionsRejected.toArray(
                                                        new String[permissionsRejected.size()]), ALL_PERMISSIONS_RESULT);
                                            }
                                        }
                                    });
                            return;
                        }
                    }
                } else {
                    Log.d(TAG, "No rejected permissions.");
                    canGetLocation = true;
                    //    getLocation();
                }
                break;
        }
    }

    public void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
        alertDialog.setTitle("GPS is not Enabled!");
        alertDialog.setMessage("Do you want to turn on GPS?");
        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {


            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                getActivity().startActivity(intent);
            }
        });

        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        alertDialog.show();
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(getContext())
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    @Override
    public void onConnected(Bundle bundle) {


        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }


        if (checkLocationValidity()) {


            requestForLocation();

            location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        }

        if (location == null) {

            requestForLocation();
            error = true;

            AlertDialog.Builder builder;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                builder = new AlertDialog.Builder(getContext(), android.R.style.Theme_Material_Light_Dialog);
            } else {
                builder = new AlertDialog.Builder(getContext());
            }
            builder.setTitle("Problem in loading data")
                    .setMessage("There is a problem while fetching data from GPS. Better solution is re-connect GPS\nPress OK to continue")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // continue with delete


                            dialog.dismiss();


                        }
                    }).setCancelable(false)
                    .show();


        } else {

            currentLatitude = location.getLatitude();
            currentLongitude = location.getLongitude();
            error = false;

            //  runToFetchData();

        }


        connected = true;

    }

    @Override
    public void onConnectionSuspended(int i) {

        mGoogleApiClient.reconnect();
        Toast.makeText(getActivity(), "Connection Suspended !!! Trying another way.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

        Toast.makeText(getContext(), "Connection Failed. Trying another way.", Toast.LENGTH_SHORT).show();
        mGoogleApiClient.reconnect();


        if (connectionResult.hasResolution()) {
            try {

                connectionResult.startResolutionForResult(getActivity(), CONNECTION_FAILURE_RESOLUTION_REQUEST);

            } catch (IntentSender.SendIntentException e) {

                e.printStackTrace();
            }
        } else {

            Log.e("Error", "Location services connection failed with code " + connectionResult.getErrorCode());
        }
    }

    @Override
    public void onLocationChanged(Location location) {

        currentLatitude = location.getLatitude();
        currentLongitude = location.getLongitude();

        Toast.makeText(getContext(), currentLatitude + " WORKS " + currentLongitude + "", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    public boolean checkLocationValidity() {

        LocationManager locateManager = (LocationManager) getActivity().getSystemService(LOCATION_SERVICE);
        boolean enabled = locateManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (enabled) {
            return true;

        } else {

            Toast.makeText(getContext(), "Location is off !!! \nPlease turn on Location Service !!!", Toast.LENGTH_SHORT).show();
            return false;

        }

    }

    private void executeData() {

        Geocoder geocoder = new Geocoder(getContext());

        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (geocoder.isPresent()) {
                StringBuilder stringBuilder = new StringBuilder();
                if (addresses.size() > 0) {
                    returnAddress = addresses.get(0);
                }
            } else {

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void fetchData() {

        String city = returnAddress.getLocality();
        String address = returnAddress.getFeatureName();
        String subCity = returnAddress.getSubLocality();
        String countryName = returnAddress.getCountryName();
        String countryCode = returnAddress.getCountryCode();
        String zip_Code = returnAddress.getPostalCode();
        String division = returnAddress.getAdminArea();

        String surface = returnAddress.getThoroughfare();
        String subSurface = returnAddress.getSubThoroughfare();
        String premises = returnAddress.getPremises();
        String url = returnAddress.getUrl();


        adapter.clear();
        dataModels.add(new DataModel(address, city, subCity, zip_Code, division, countryName,
                countryCode, String.valueOf(latitude), String.valueOf(longitude)));

        detailsLV.setAdapter(adapter);
        adapter.notifyDataSetChanged();


    }

    private void requestForLocation() {

        if (checkLocationValidity()) {

            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                return;
            }
            location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        }
    }

    private void turnGPSOn() {
        String provider = Settings.Secure.getString(getActivity().getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);

        if (!provider.contains("gps")) { //if gps is disabled
            final Intent poke = new Intent();
            poke.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
            poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
            poke.setData(Uri.parse("3"));
            getActivity().sendBroadcast(poke);
        }
    }

    private void turnGPSOff() {
        String provider = Settings.Secure.getString(getActivity().getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);

        if (provider.contains("gps")) { //if gps is enabled
            final Intent poke = new Intent();
            poke.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
            poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
            poke.setData(Uri.parse("3"));
            getActivity().sendBroadcast(poke);
        }
    }




}
