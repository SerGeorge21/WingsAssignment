package com.example.wingsassignment;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleMap.OnCameraMoveStartedListener,
        GoogleMap.OnCameraMoveListener,
        GoogleMap.OnCameraMoveCanceledListener,
        GoogleMap.OnCameraIdleListener,
        android.app.LoaderManager.LoaderCallbacks<ArrayList<Venue>>{

    private GoogleMap mMap;

    LocationManager locationManager;
    LocationListener locationListener;
    LatLng latLng;
    boolean isPermissionGranted = false; // to use for API search request

    TextView addressTextView;
    LinearLayout infoLayout;
    TextView infoTextView;

    //actual data
    private static final int VENUE_LOADER_ID = 1;
    private static final String TYPE_OF_VENUE = "bakery";
    private static final String FOURSQUARE_REQUEST_URL = "https://api.foursquare.com/v2/venues/explore";
    private static final String CLIENT_ID = "JKSLSSUYQ4OFHUHSVBDHAOLLYASXWWVMGJWG1O3BBTSFGQGT";
    private static final String CLIENT_SECRET = "KCICUDFUH1UA4AJRDFVIU4KZT4XVHREZZDJEHZSJCD5ATABS";
    ArrayList<Venue> venues;


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == 1){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    isPermissionGranted = true;

                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

                    mMap.setMyLocationEnabled(true);

                    Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if(location ==null /*&& location.getTime() > Calendar.getInstance().getTimeInMillis() - 2*60*1000*/){
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000,0,locationListener);
                    }
                    if(location != null){
                        //locationManager.removeUpdates(locationListener);


                        latLng = new LatLng(location.getLatitude(), location.getLongitude()); //TODO THE LAT LONG FOR THE API CALL
                        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng,15);
                        mMap.animateCamera(cameraUpdate);
                        locationManager.removeUpdates(locationListener);
                    }
                    showCurrentAddress(location); //geocoder
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        addressTextView = (TextView) findViewById(R.id.address);
        infoLayout = (LinearLayout) findViewById(R.id.infoLayout);
        infoTextView = (TextView) findViewById(R.id.infoTextView);

        venues = new ArrayList<Venue>();
        //I dont need adapter here

        //TODO: MIGHT WANT TO MAKE LAYOUT CLICKABLE/UNCLICKABLE
        //TODO:ANIMATE LAYOUT IN AND OUT OF FRAME
        //Redirect to other page when user clicks Info Layout
        infoLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), VenueInfoActivity.class);
                intent.putExtra("VENUE_INFO", infoTextView.getText().toString());
                startActivity(intent);
            }
        });

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnCameraMoveListener(this);
        mMap.setOnCameraMoveStartedListener(this);
        mMap.setOnCameraIdleListener(this);
        mMap.setOnCameraMoveCanceledListener(this);



        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                locationManager.removeUpdates(this);

            }
        };

        if (Build.VERSION.SDK_INT < 23) {
            locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER, 0, 0, locationListener);
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            } else {
                isPermissionGranted = true;
                locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER, 0, 0, locationListener);
                Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                mMap.clear();

                //THIS IS SUPPOSED TO GIVE ME BLUE DOT OF MY LOCATION??
                mMap.setMyLocationEnabled(true);

                /*//TODO: U NEED TO REMOVE MARKER FOR USER LOCATION
                LatLng userLocation = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                mMap.addMarker(new MarkerOptions().position(userLocation).title("Your Location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(userLocation)); */

                //CODE FOR MAP CENTERING THE USER POSITION???
                Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if(location ==null /*&& location.getTime() > Calendar.getInstance().getTimeInMillis() - 2*60*1000*/){
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000,0,locationListener);
                }
                if(location != null){
                    //locationManager.removeUpdates(locationListener);


                latLng = new LatLng(location.getLatitude(), location.getLongitude()); //TODO THE LAT LONG FOR THE API CALL
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng,15);
                mMap.animateCamera(cameraUpdate);
                locationManager.removeUpdates(locationListener);
                }

                showCurrentAddress(location); //geocoder


            }
        }
    }

    public void showCurrentAddress(Location location){
        // add geocoder
        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        try{
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(),1);
            String address = addresses.get(0).getAddressLine(0);
            String fullAddress = address;
            addressTextView.setText(fullAddress);
        }catch(Exception e){
            e.printStackTrace();
        }

    }



    @Override
    public void onCameraIdle() {
        Toast.makeText(this, "Camera stopped Moving" , Toast.LENGTH_SHORT).show();
        if(isPermissionGranted) {
            //API CALL HERE
            startLoader();
        }

    }

    private void startLoader(){
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        /*NetworkInfo class seems to be depricated but I also checked
        https://developer.android.com/training/monitoring-device-state/connectivity-status-type
         */
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if(networkInfo != null && networkInfo.isConnected()){
            LoaderManager loaderManager = getLoaderManager();

            loaderManager.initLoader(VENUE_LOADER_ID, null, this);

        }else{
            Toast.makeText(this, "No Internet Connection", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onCameraMoveCanceled() {

    }

    @Override
    public void onCameraMove() {
        infoLayout.setVisibility(View.INVISIBLE);

    }

    @Override
    public void onCameraMoveStarted(int i) {

    }

    @Override
    public Loader<ArrayList<Venue>> onCreateLoader(int id, Bundle args) {
        String lat = String.valueOf(latLng.latitude);
        String lng = String.valueOf(latLng.longitude);
        String latAndLng = lat + ", " + lng;
        Uri baseUri = Uri.parse(FOURSQUARE_REQUEST_URL);

        Uri.Builder uriBuilder = baseUri.buildUpon();

        uriBuilder.appendQueryParameter("ll", latAndLng); //TODO:put latLong by gps
        uriBuilder.appendQueryParameter("radius", "700");
        uriBuilder.appendQueryParameter("v", "20201220"); //YYYYMMDD
        uriBuilder.appendQueryParameter("query", TYPE_OF_VENUE);
        uriBuilder.appendQueryParameter("client_id", CLIENT_ID);
        uriBuilder.appendQueryParameter("client_secret", CLIENT_SECRET);

        return new VenueLoader(this, uriBuilder.toString());
    }


    @Override
    public void onLoadFinished(Loader<ArrayList<Venue>> loader, ArrayList<Venue> v) {
        if(v != null && !v.isEmpty()){
            //replace contents in venues with fresh fetched data/venues
            venues.removeAll(venues);
            venues.addAll(v);
        }
        addVenueMarkers();

    }

    @Override
    public void onLoaderReset(Loader<ArrayList<Venue>> loader) {

    }

    private void addVenueMarkers(){
        mMap.clear();
        for(int i=0; i<venues.size(); i++){
            LatLng vLatLng = new LatLng(Double.parseDouble(venues.get(i).getLatitude()), Double.parseDouble(venues.get(i).getLongitude()));
            mMap.addMarker(new MarkerOptions().position(vLatLng).title(venues.get(i).getName()).snippet(String.valueOf(i))); //I have the position of the venue in the array inside the snippet
        }

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                //TODO: try making an InfoWindow if you have time
                String markerInfo = venues.get(Integer.parseInt(marker.getSnippet())).toString();
                infoLayout.setVisibility(View.VISIBLE);
                infoTextView.setText(markerInfo);
                return true; //true so that the camera does not move to center to Marker, when said marker is clicked
            }
        });

    }
}