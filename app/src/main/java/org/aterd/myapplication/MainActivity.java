package org.aterd.myapplication;

import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.DirectionsApi;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DirectionsLeg;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.DirectionsStep;
import com.google.maps.model.EncodedPolyline;
import com.google.maps.model.TravelMode;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback {
    private GoogleMap mMap;

    public LocationManager locationManager;
    private boolean isGPSEnabled, isNetworkProviderEnabled;
    private Object latitude, longitude;
    private Location location;
    private long lastTouchTime = -1;

    public static final int LOCATION_UPDATE_MIN_DISTANCE = 10;
    public static final int LOCATION_UPDATE_MIN_TIME = 5000;

    private LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            if (location != null) {
                drawMarkerLocation(location,true, true);
                locationManager.removeUpdates(mLocationListener);
            } else {
                Log.d("map", "Location is null");
            }
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
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    public LatLng from, to;
    public int step=0;
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        googleMap.setMyLocationEnabled(true);
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setZoomGesturesEnabled(false);
//        googleMap.getUiSettings().setMyLocationButtonEnabled(true);
//        googleMap.getUiSettings().setCompassEnabled(true);
//        googleMap.getUiSettings().setRotateGesturesEnabled(true);
//        getCurrentLocation();
        from=new LatLng(33.9815489,51.3425419);
        drawMarkerLatLng(from, false, true);

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(final LatLng coordinates){
                Toast.makeText(getApplicationContext(), "tapped", Toast.LENGTH_SHORT).show();
                if(step == 0) {
                    from = coordinates;
                    mMap.clear();
                    drawMarkerLatLng(from, true, true);
                    step=1;
                }
                else if(step == 1) {
                    to = coordinates;
                    drawMarkerLatLng(to, false, true);
                    step=2;
                }
                if(step == 2) {
//                    LatLng zaragoza = new LatLng(41.648823,-0.889085);
                    List<LatLng> path = new ArrayList();
                    GeoApiContext context = new GeoApiContext.Builder()
                            .apiKey("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx")
                            .build();
                    DirectionsApiRequest req = DirectionsApi.getDirections(context, from.latitude + ", " + from.longitude,to.latitude + ", " + to.longitude);
                    req.mode(TravelMode.DRIVING);
//                    req.departureTime(now);
                    try {
                        DirectionsResult res = req.await();
                        if (res.routes != null && res.routes.length > 0) {
                            DirectionsRoute route = res.routes[0];
                            if (route.legs !=null) {
                                for(int i=0; i<route.legs.length; i++) {
                                    DirectionsLeg leg = route.legs[i];
                                    if (leg.steps != null) {
                                        for (int j=0; j<leg.steps.length;j++){
                                            DirectionsStep step = leg.steps[j];
                                            if (step.steps != null && step.steps.length >0) {
                                                for (int k=0; k<step.steps.length;k++){
                                                    DirectionsStep step1 = step.steps[k];
                                                    EncodedPolyline points1 = step1.polyline;
                                                    if (points1 != null) {
                                                        //Decode polyline and add points to list of route coordinates
                                                        List<com.google.maps.model.LatLng> coords1 = points1.decodePath();
                                                        for (com.google.maps.model.LatLng coord1 : coords1) {
                                                            path.add(new LatLng(coord1.lat, coord1.lng));
                                                        }
                                                    }
                                                }
                                            } else {
                                                EncodedPolyline points = step.polyline;
                                                if (points != null) {
                                                    //Decode polyline and add points to list of route coordinates
                                                    List<com.google.maps.model.LatLng> coords = points.decodePath();
                                                    for (com.google.maps.model.LatLng coord : coords) {
                                                        path.add(new LatLng(coord.lat, coord.lng));
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } catch(Exception ex) {
                        Log.e("map", ex.getLocalizedMessage());
                    }
                    if (path.size() > 0) {
                        PolylineOptions opts = new PolylineOptions().addAll(path).color(Color.parseColor("#4797e2")).width(4);
                        mMap.addPolyline(opts);
                    }
//                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(from, 6));
                }
            }
        });
//        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
//            @Override
//            public void onMapClick(LatLng point) {
////                allPoints.add(point);
////                mMap.clear();
//                mMap.addMarker(new MarkerOptions().position(point));
//            }
//        });

    }

    private void getCurrentLocation() {
        boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        Location location = null;
        if (!(isGPSEnabled || isNetworkEnabled))
            Toast.makeText(getApplicationContext(), "error_location_provider", Toast.LENGTH_SHORT).show();
        else {
            if (isNetworkEnabled) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, LOCATION_UPDATE_MIN_TIME, LOCATION_UPDATE_MIN_DISTANCE, mLocationListener);
                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
            if (isGPSEnabled) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_UPDATE_MIN_TIME, LOCATION_UPDATE_MIN_DISTANCE, mLocationListener);
                location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }
        }
        if (location != null) {
            drawMarkerLocation(location,true, true);
        }
    }

    private void drawMarkerLocation(Location location, boolean clear, boolean goTo) {
        LatLng gps = new LatLng(location.getLatitude(), location.getLongitude());
        drawMarkerLatLng(gps, clear, goTo);
    }
    private void drawMarkerLatLng(LatLng latlng, boolean clear, boolean goTo) {
        drawMarker(latlng.latitude, latlng.longitude, clear, goTo);
    }

    private void drawMarker(double lat, double lng, boolean clear, boolean goTo) {
        if (mMap != null) {
            if(clear) {
                mMap.clear();
            }
            LatLng latlng = new LatLng(lat, lng);
            mMap.addMarker(new MarkerOptions().position(latlng).title("Current location"));
            if(goTo) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng, 11));
//                mMap.moveCamera(CameraUpdateFactory.newLatLng(gps));
            }
        }
    }
}
