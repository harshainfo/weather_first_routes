package com.hpitawela.weatherfirstroute;

import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.common.util.IOUtils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.maps.DirectionsApi;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.GeolocationApi;
import com.google.maps.GeolocationApiRequest;
import com.google.maps.android.PolyUtil;
import com.google.maps.model.DirectionsLeg;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.DirectionsStep;
import com.google.maps.model.Distance;
import com.google.maps.model.TrafficModel;
import com.google.maps.model.TravelMode;
import com.squareup.okhttp.Request;

import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Button btnDirections;
    private EditText etOrigin;
    private EditText etDestination;
    private PlaceAutocompleteFragment originAutocompleteFragment, destAutocompleteFragment;
    private static final String TAG = "MapsActivity";
    private boolean isOriginSelected, isDestSelected, isMapReady;
    private LatLng origin, dest;
    private com.google.maps.model.LatLng originShort, destShort;
    private int padding;
    private XmlPullParserFactory xmlFactoryObject;
    private XmlPullParser myParser;
    private byte[] key;
    private GeoDataClient mGeoDataClient;
    private PlaceDetectionClient mPlaceDetectionClient;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private boolean mLocationPermissionGranted;
    private Object mLastKnownLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        padding = 50;

        originAutocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.origin_autocomplete_fragment);

        originAutocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                Log.i(TAG, "Place: " + place.getName());
                isOriginSelected = true;

                try {
                    Geocoder coder = new Geocoder(MapsActivity.this);
                    List<Address> address;
                    address = coder.getFromLocationName(place.getName().toString(), 1);
                    if (address == null) {
                        Log.i(TAG, "Address: null ");
                    }
                    Address location = address.get(0);
                    double lat = location.getLatitude();
                    double lng = location.getLongitude();

                    origin = new LatLng(lat, lng);
                    originShort = new com.google.maps.model.LatLng(lat, lng);
                    mMap.addMarker(new MarkerOptions()
                            .position(origin)
                            .title("Start")
                            .alpha(0.3f));
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(origin));
                    if (dest != null && isMapReady) {
                        LatLngBounds.Builder builder = new LatLngBounds.Builder();

                        builder.include(new MarkerOptions().position(origin).getPosition());
                        builder.include(new MarkerOptions().position(dest).getPosition());
                        LatLngBounds bounds = builder.build();

                        // offset from edges of the map in pixels
                        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);

                        mMap.moveCamera(cu);
                        mMap.animateCamera(cu);

                        DateTime now = new DateTime();
                        Log.i(TAG, "now: " + now.hourOfDay());
                        DirectionsApiRequest directionsApiRequest = DirectionsApi.newRequest(getGeoContext());
                        directionsApiRequest.mode(TravelMode.DRIVING);
                        directionsApiRequest.origin(originShort);
                        directionsApiRequest.destination(destShort);
                        directionsApiRequest.departureTime(now);
                        directionsApiRequest.alternatives(true);
                        DirectionsResult result = directionsApiRequest.await();
                        Log.i(TAG, "Result.routes.length: " + result.routes[0].legs.length);
                        addPolylines(result, mMap);
                        Log.i(TAG, "result: " + result.toString());

                    } else {

                        CameraUpdate zoom = CameraUpdateFactory.zoomTo(15);
                        mMap.animateCamera(zoom);

                    }

                } catch (Exception e) {

                    Log.i(TAG, "Exception: " + e.getClass());
                }


            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);

            }
        });

        destAutocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.dest_autocomplete_fragment);

        destAutocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                Log.i(TAG, "Place: " + place.getName());
                isDestSelected = true;
                try {
                    Geocoder coder = new Geocoder(MapsActivity.this);
                    List<Address> address;
                    address = coder.getFromLocationName(place.getName().toString(), 1);
                    if (address == null) {
                        Log.i(TAG, "Address: null ");
                    }
                    Address location = address.get(0);
                    double lat = location.getLatitude();
                    double lng = location.getLongitude();

                    dest = new LatLng(lat, lng);
                    destShort = new com.google.maps.model.LatLng(lat, lng);
                    mMap.addMarker(new MarkerOptions()
                            .position(dest)
                            .title("End")
                            .alpha(0.3f));
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(dest));
                    if (origin != null && isMapReady) {
                        LatLngBounds.Builder builder = new LatLngBounds.Builder();

                        builder.include(new MarkerOptions().position(origin).getPosition());
                        builder.include(new MarkerOptions().position(dest).getPosition());
                        LatLngBounds bounds = builder.build();

                        // offset from edges of the map in pixels
                        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);

                        mMap.moveCamera(cu);
                        mMap.animateCamera(cu);

                        DateTime now = new DateTime();
                        Log.i(TAG, "now: " + now.hourOfDay());
                        DirectionsApiRequest directionsApiRequest = DirectionsApi.newRequest(getGeoContext());
                        directionsApiRequest.mode(TravelMode.DRIVING);
                        directionsApiRequest.origin(originShort);
                        directionsApiRequest.destination(destShort);
                        directionsApiRequest.departureTime(now);
                        directionsApiRequest.alternatives(true);
                        directionsApiRequest.trafficModel(TrafficModel.BEST_GUESS);

                        DirectionsResult result = directionsApiRequest.await();

                        Log.i(TAG, "Result.routes.length: " + result.routes[0].legs.length);
                        addPolylines(result, mMap);

                    } else {


                        CameraUpdate zoom = CameraUpdateFactory.zoomTo(15);
                        mMap.animateCamera(zoom);

                    }

                } catch (Exception e) {

                    Log.i(TAG, "Exception: " + e.getClass());
                }


            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });
    }

    private void sendRequest() {
        String origin = etOrigin.getText().toString();
        String destination = etDestination.getText().toString();

        if (origin.isEmpty()) {
            Toast.makeText(this, "Please enter valid origin!", Toast.LENGTH_LONG).show();
            return;
        }
        if (destination.isEmpty()) {
            Toast.makeText(this, "Please enter valid destination!", Toast.LENGTH_LONG).show();
            return;
        }
    }

    private GeoApiContext getGeoContext() {


        GeoApiContext geoApiContext = new GeoApiContext();

        return geoApiContext.setQueryRateLimit(3)
                .setApiKey(getString(R.string.google_maps_key))
                .setConnectTimeout(30, TimeUnit.SECONDS)
                .setReadTimeout(30, TimeUnit.SECONDS)
                .setWriteTimeout(30, TimeUnit.SECONDS);
    }

    private String getEndLocationTitle(DirectionsResult results) {
        return "Time :" + results.routes[0].legs[0].duration.humanReadable +
                " Distance :" + results.routes[0].legs[0].distance.humanReadable;
    }

    private void traverseRoute(DirectionsRoute route) {
        Log.i(TAG, "route.summary: " + route.summary);
        int totalRouteDistance = 0;
        for (DirectionsLeg leg : route.legs) {
            Log.i(TAG, "leg.departureTime: " + leg.departureTime);
            Log.i(TAG, "leg.duration: " + leg.duration);
            Log.i(TAG, "leg.distance: " + leg.distance);

            for (DirectionsStep step : leg.steps) {
                Log.i(TAG, "step.duration: " + step.duration);
                Log.i(TAG, "step.distance: " + step.distance);

            }


        }
    }

    private void addPolylines(DirectionsResult results, final GoogleMap mMap) {
        Log.i(TAG, "result routes: " + results.toString());

        List<List<LatLng>> decodedPaths = null;
        for (DirectionsRoute route : results.routes) {
            int totalHours = 0, totalMins = 0;
            List<LatLng> decodedPath = PolyUtil.decode(route.overviewPolyline.getEncodedPath());
            mMap.addPolyline(new PolylineOptions().addAll(decodedPath));

            for (DirectionsLeg leg : route.legs) {

                for (DirectionsStep step : leg.steps) {
                    String duration = step.duration.humanReadable;


                    if (step.distance.inMeters > 160900) {

                        final com.google.maps.model.LatLng startLocation = step.startLocation;
                        com.android.volley.RequestQueue queue = Volley.newRequestQueue(this);
                        String url = "https://api.openweathermap.org/data/2.5/forecast?lat=" + startLocation.lat + "&lon=" + startLocation.lng + "&cnt=1" + "&mode=xml" + "&appid=" + getString(R.string.openweather_api_key);

                        // Request a string response from the provided URL.
                        StringRequest stringRequest = new StringRequest(com.android.volley.Request.Method.GET, url,
                                new com.android.volley.Response.Listener<String>() {
                                    @Override
                                    public void onResponse(String response) {

                                        try {
                                            xmlFactoryObject = XmlPullParserFactory.newInstance();
                                            myParser = xmlFactoryObject.newPullParser();

                                            myParser.setInput(new ByteArrayInputStream(response.getBytes(StandardCharsets.UTF_8)), null);

                                            int event = myParser.getEventType();

                                            while (event != XmlPullParser.END_DOCUMENT) {
                                                String name = myParser.getName();

                                                String symbolName, symbolVar;

                                                //check the event of parser
                                                switch (event) {
                                                    //check if the beginning of tag
                                                    case XmlPullParser.START_TAG:
                                                        //check if it is the symbol tag
                                                        if (name.equals("symbol")) {
                                                            Marker mIcon;

                                                            symbolName = myParser.getAttributeValue(null, "name");
                                                            symbolVar = myParser.getAttributeValue(null, "var");

                                                            Glide.with(getApplicationContext())
                                                                    .load(getString(R.string.icon_path) + symbolVar + ".png").asBitmap().fitCenter()
                                                                    .override(100, 100)
                                                                    .into(new SimpleTarget<Bitmap>() {
                                                                        @Override
                                                                        public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> glideAnimation) {

                                                                            mMap.addMarker(new MarkerOptions().position(new LatLng(startLocation.lat, startLocation.lng))
                                                                                    .icon(BitmapDescriptorFactory.fromBitmap(bitmap)));

                                                                        }
                                                                    });
                                                        }
                                                        break;

                                                    case XmlPullParser.END_TAG:
                                                        Log.i(TAG, "In XmlPullParser.END_TAG: " + event);

                                                        break;
                                                }
                                                try {
                                                    event = myParser.next();
                                                } catch (IOException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        } catch (XmlPullParserException e) {
                                            e.printStackTrace();
                                        }

                                    }
                                }, new com.android.volley.Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.i(TAG, "An error occurred: " + error.getLocalizedMessage());
                            }
                        });

                        // Add the request to the RequestQueue.
                        queue.add(stringRequest);
                    }
                    int hours = 0, mins = 0, hours_from_minutes = 0;
                    if (duration.contains("min") && duration.contains("hours")) {

                        mins = Integer.parseInt(duration.split("min")[0].split("hours")[1].trim());
                        totalMins = totalMins + mins;
                        hours = Integer.parseInt(duration.split("min")[0].split("hours")[0].trim());
                        if (totalMins > 59) {
                            hours_from_minutes = totalMins / 60;
                            totalMins = totalMins % 60;
                        }
                        totalHours = totalHours + hours + hours_from_minutes;

                    } else if (duration.contains("min") && duration.contains("hour")) {
                        mins = Integer.parseInt(duration.split("min")[0].split("hour")[1].trim());
                        totalMins = totalMins + mins;
                        hours = Integer.parseInt(duration.split("min")[0].split("hour")[0].trim());
                        if (totalMins > 59) {
                            hours_from_minutes = totalMins / 60;
                            totalMins = totalMins % 60;
                        }
                        totalHours = totalHours + hours + hours_from_minutes;

                    } else if (duration.contains("hour")) {
                        totalHours += Integer.parseInt(duration.split("hour")[0].trim());
                    } else if (duration.contains("min")) {
                        mins = Integer.parseInt(duration.split("min")[0].trim());
                        totalMins = totalMins + mins;
                        if (totalMins > 59) {
                            Log.i(TAG, "Inside mins only");
                            Log.i(TAG, "totalHours: " + totalHours + " totalMins: " + totalMins);
                            hours_from_minutes = totalMins / 60;
                            totalMins = totalMins % 60;
                        }
                        totalHours += hours_from_minutes;

                    }
                    Log.i(TAG, "duration: " + duration);
                    Log.i(TAG, "totalHours: " + totalHours + " totalMins: " + totalMins);
                }
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        isMapReady = true;
    }
}