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
import android.os.AsyncTask;
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
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.juborajsarker.mylocation.R;
import com.juborajsarker.mylocation.activity.LocationDetailsActivity;
import com.juborajsarker.mylocation.activity.NearbyMapActivity;
import com.juborajsarker.mylocation.java_class.AlertDialogManager;
import com.juborajsarker.mylocation.java_class.GooglePlaces;
import com.juborajsarker.mylocation.java_class.Place;
import com.juborajsarker.mylocation.java_class.PlacesList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.content.Context.LOCATION_SERVICE;


public class NearbyFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private final static int ALL_PERMISSIONS_RESULT = 101;
    // KEY Strings
    public static String KEY_REFERENCE = "reference"; // id of the place
    public static String KEY_NAME = "name"; // name of the place
    public static String KEY_VICINITY = "vicinity"; // Place area name
    final String TAG = "GPS";
    View view;
    Spinner spinnerNearbyChoice;
    Button btnSearch, btnShowOnMap;
    ListView lv;
    Double latitude;
    Double longitude;
    ProgressDialog pDialog;
    String types;
    PlacesList nearPlaces;
    GooglePlaces googlePlaces;
    AlertDialogManager alert = new AlertDialogManager();
    ArrayList<HashMap<String, String>> placesListItems = new ArrayList<HashMap<String, String>>();
    Address returnAddress;
    LocationManager locationManager;
    ArrayList<String> permissions = new ArrayList<>();
    ArrayList<String> permissionsToRequest;
    ArrayList<String> permissionsRejected = new ArrayList<>();
    boolean isGPS = false;
    boolean isNetwork = false;
    boolean canGetLocation = true;
    int count = 0;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private double currentLatitude;
    private double currentLongitude;


    public NearbyFragment() {


    }


    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_nearby, container, false);

        spinnerNearbyChoice = (Spinner) view.findViewById(R.id.spinner_nearby_choice);
        btnSearch = (Button) view.findViewById(R.id.btn_search);
        btnShowOnMap = (Button) view.findViewById(R.id.btn_show_on_map);
        lv = (ListView) view.findViewById(R.id.lv_nearby);

        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                execute();
                init();
                count++;



                if (!mGoogleApiClient.isConnected()) {

                    mGoogleApiClient.connect();


                    if (count == 1) {


                        pDialog = new ProgressDialog(view.getContext());
                        pDialog.setMessage(Html.fromHtml("<b>Connecting with GPS</b><br/>Please wait...."));
                        pDialog.setIndeterminate(false);
                        pDialog.setCancelable(false);
                        pDialog.show();

                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            public void run() {
                                pDialog.dismiss();


                                AlertDialog.Builder builder;
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                    builder = new AlertDialog.Builder(getContext(), android.R.style.Theme_Material_Light_Dialog);
                                } else {
                                    builder = new AlertDialog.Builder(getContext());
                                }
                                builder.setTitle("Connected successfully !!!")
                                        .setMessage("Press OK to continue")
                                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                // continue with delete


                                                if (spinnerNearbyChoice.getSelectedItemPosition() > 0) {

                                                    fetchData();
                                                } else {

                                                    Toast.makeText(getContext(), "Please select a valid choice", Toast.LENGTH_SHORT).show();
                                                }


                                            }
                                        }).setCancelable(false)
                                        .show();


                            }
                        }, 2000);
                    }


                }


                if (spinnerNearbyChoice.getSelectedItemPosition() > 0) {

                    fetchData();

                } else {

                    Toast.makeText(getContext(), "Please select a valid choice", Toast.LENGTH_SHORT).show();
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


                if (spinnerNearbyChoice.getSelectedItemPosition() > 0) {

                    fetchDataForMap();
                } else {

                    Toast.makeText(getContext(), "Please select a valid choice !!!", Toast.LENGTH_SHORT).show();
                }


            }
        });


        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // getting values from selected ListItem
                String reference = ((TextView) view.findViewById(R.id.reference)).getText().toString();

                // Starting new intent
                Intent in = new Intent(getActivity().getApplicationContext(), LocationDetailsActivity.class);

                // Sending place refrence id to single place activity
                // place refrence id used to get "Place full details"
                in.putExtra(KEY_REFERENCE, reference);
                in.putExtra("currentLat", latitude);
                in.putExtra("currentLng", longitude);
                startActivity(in);
            }
        });


        return view;
    }

    private void fetchDataForMap() {


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


                            placesListItems.clear();
                            new LoadPlaces().execute();

                            if (nearPlaces.results != null) {

                                Intent intent = new Intent(getContext(), NearbyMapActivity.class);
                                intent.putExtra("lat", latitude);
                                intent.putExtra("lng", longitude);
                                intent.putExtra("near_places", nearPlaces);
                                startActivity(intent);

                            }


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

    private void fetchData() {


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


                            placesListItems.clear();
                            new LoadPlaces().execute();
                            btnShowOnMap.setVisibility(View.VISIBLE);


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
    public void onPause() {
        super.onPause();

        //btnShowOnMap.setVisibility(View.GONE);
    }

    @Override
    public void onStart() {
        super.onStart();


    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mGoogleApiClient.isConnected()) {

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
                .setInterval(10 * 1000)
                .setFastestInterval(1 * 1000);
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
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (location == null) {
//            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest,
            //                (com.google.android.gms.location.LocationListener) this);

        } else {

            currentLatitude = location.getLatitude();
            currentLongitude = location.getLongitude();

        }
    }


    @Override
    public void onConnectionSuspended(int i) {

        mGoogleApiClient.connect();
        Toast.makeText(getActivity(), "Connection Suspended !!! Trying another way.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

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


    class LoadPlaces extends AsyncTask<String, String, String> {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(view.getContext());
            pDialog.setMessage(Html.fromHtml("<b>Search</b><br/>Loading Places..."));
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        /**
         * getting Places JSON
         */
        protected String doInBackground(String... args) {
            // creating Places class object
            googlePlaces = new GooglePlaces();

            try {
                // Separeate your place types by PIPE symbol "|"
                // If you want all types places make it as null
                // Check list of types supported by google
                //

                if (spinnerNearbyChoice.getSelectedItemPosition() > 0) {

                    if (spinnerNearbyChoice.getSelectedItemPosition() == 1) {

                        types = "atm";

                    } else if (spinnerNearbyChoice.getSelectedItemPosition() == 2) {

                        types = "bank";

                    } else if (spinnerNearbyChoice.getSelectedItemPosition() == 3) {

                        types = "hospital";

                    } else if (spinnerNearbyChoice.getSelectedItemPosition() == 4) {

                        types = "local_government_office";

                    } else if (spinnerNearbyChoice.getSelectedItemPosition() == 5) {

                        types = "restaurant";

                    } else if (spinnerNearbyChoice.getSelectedItemPosition() == 6) {

                        types = "cafe";

                    } else if (spinnerNearbyChoice.getSelectedItemPosition() == 7) {

                        types = "school";

                    } else if (spinnerNearbyChoice.getSelectedItemPosition() == 8) {

                        types = "laundry";

                    } else if (spinnerNearbyChoice.getSelectedItemPosition() == 9) {

                        types = "university";

                    } else if (spinnerNearbyChoice.getSelectedItemPosition() == 10) {

                        types = "police";

                    } else if (spinnerNearbyChoice.getSelectedItemPosition() == 11) {

                        types = "mosque";

                    } else if (spinnerNearbyChoice.getSelectedItemPosition() == 12) {

                        types = "gym";

                    } else if (spinnerNearbyChoice.getSelectedItemPosition() == 13) {

                        types = "shopping_mall";

                    } else if (spinnerNearbyChoice.getSelectedItemPosition() == 14) {

                        types = "post_office";

                    } else if (spinnerNearbyChoice.getSelectedItemPosition() == 15) {

                        types = "pharmacy";

                    } else if (spinnerNearbyChoice.getSelectedItemPosition() == 16) {

                        types = "bar";

                    } else if (spinnerNearbyChoice.getSelectedItemPosition() == 17) {

                        types = "park";

                    }

                } else {

                    Toast.makeText(getContext(), "PLease select a valid type", Toast.LENGTH_SHORT).show();
                }
                // Listing places only cafes, restaurants

                // Radius in meters - increase this value if you don't find any places
                double radius = 1000; // 1000 meters

                // get nearest places
                nearPlaces = googlePlaces.search(latitude, longitude, radius, types);


            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog
         * and show the data in UI
         * Always use runOnUiThread(new Runnable()) to update UI from background
         * thread, otherwise you will get error
         **/
        protected void onPostExecute(String file_url) {
            // dismiss the dialog after getting all products
            pDialog.dismiss();
            // updating UI from Background Thread
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    /**
                     * Updating parsed Places into LISTVIEW
                     * */
                    // Get json response status
                    String status = nearPlaces.status;

                    // Check for all possible status
                    if (status.equals("OK")) {
                        // Successfully got places details
                        if (nearPlaces.results != null) {
                            // loop through each place
                            for (Place p : nearPlaces.results) {
                                HashMap<String, String> map = new HashMap<String, String>();

                                // Place reference won't display in listview - it will be hidden
                                // Place reference is used to get "place full details"
                                map.put(KEY_REFERENCE, p.reference);

                                // Place name
                                map.put(KEY_NAME, p.name);


                                // adding HashMap to ArrayList
                                placesListItems.add(map);
                            }
                            // list adapter
                            ListAdapter adapter = new SimpleAdapter(view.getContext(), placesListItems,
                                    R.layout.list_item,
                                    new String[]{KEY_REFERENCE, KEY_NAME}, new int[]{
                                    R.id.reference, R.id.name});

                            // Adding data into listview

                            lv.setAdapter(adapter);

                        }
                    } else if (status.equals("ZERO_RESULTS")) {
                        // Zero results found
                        alert.showAlertDialog(view.getContext(), "Near Places",
                                "Sorry no places found. Try to change the types of places",
                                false);
                    } else if (status.equals("UNKNOWN_ERROR")) {
                        alert.showAlertDialog(view.getContext(), "Places Error",
                                "Sorry unknown error occured.",
                                false);
                    } else if (status.equals("OVER_QUERY_LIMIT")) {
                        alert.showAlertDialog(view.getContext(), "Places Error",
                                "Sorry query limit to google places is reached",
                                false);
                    } else if (status.equals("REQUEST_DENIED")) {
                        alert.showAlertDialog(view.getContext(), "Places Error",
                                "Sorry error occured. Request is denied",
                                false);
                    } else if (status.equals("INVALID_REQUEST")) {
                        alert.showAlertDialog(view.getContext(), "Places Error",
                                "Sorry error occured. Invalid Request",
                                false);
                    } else {
                        alert.showAlertDialog(view.getContext(), "Places Error",
                                "Sorry error occured.",
                                false);
                    }
                }
            });

        }


    }


}
