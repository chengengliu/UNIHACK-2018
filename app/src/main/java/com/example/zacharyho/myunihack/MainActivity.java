package com.example.zacharyho.myunihack;

import android.annotation.SuppressLint;
import android.graphics.Point;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.zacharyho.myunihack.routing.AStar;
import com.example.zacharyho.myunihack.routing.DataParser;
import com.example.zacharyho.myunihack.routing.helperobjects.Coordinate;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineListener;
import com.mapbox.android.core.location.LocationEnginePriority;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.api.directions.v5.DirectionsCriteria;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerPlugin;
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.CameraMode;
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.RenderMode;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncher;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncherOptions;
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute;
import com.mapbox.services.android.navigation.v5.navigation.MapboxNavigationOptions;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.ContentValues.TAG;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, LocationEngineListener, PermissionsListener
        , MapboxMap.OnMapClickListener, SeekBar.OnSeekBarChangeListener {

    private MapView mapView;
    private MapboxMap map;

    private PermissionsManager permissionsManager;
    private LocationEngine locationEngine;
    private LocationLayerPlugin locationLayerPlugin;
    private Location originLocation;
    private com.mapbox.geojson.Point originPosition;
    private com.mapbox.geojson.Point destinationPosition;
    private Marker destinationMarker;

    private Button startButton;
    private SeekBar dangerSlider;
    private int progress = 0;

    private NavigationMapRoute navigationMapRoute;
    private static final String TAG = "MainActivity";


    @Override
    protected void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        Mapbox.getInstance(this, getString(R.string.access_token));
        setContentView(R.layout.activity_main);
        mapView = (MapView) findViewById(R.id.mapView);
        startButton = (Button) findViewById(R.id.startButton);
        dangerSlider = (SeekBar) findViewById(R.id.seekBar);
        mapView.onCreate(saveInstanceState);
        mapView.getMapAsync(this);

        dangerSlider.setMax(100000);
        dangerSlider.setProgress(progress);

        dangerSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                progress = i;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                // Launch Navigation UI
//                NavigationLauncherOptions options = NavigationLauncherOptions.builder().origin(originPosition)
//                        .destination(destinationPosition)
//                        .shouldSimulateRoute(true)
//                        .build();
//                NavigationLauncher.startNavigation(MainActivity.this, options);
                // Server call for waypoints



                Coordinate[] map = AStar.getBestPath((new Coordinate(originPosition)), (new Coordinate(destinationPosition)), dangerSlider.getProgress());

                ArrayList<com.mapbox.geojson.Point> waypoints = new ArrayList<>();

                for (Coordinate coordinate : map) {
                    Log.d("Debug", coordinate.toString());
                    waypoints.add(DataParser.convertCoordToPoint(coordinate));
                }



                // convert to list of geojson points

                getRoute(originPosition, destinationPosition, waypoints);
            }
        });
    }

    @Override
    public void onMapReady(MapboxMap mapboxMap) {
        map = mapboxMap;
        map.addOnMapClickListener(this);
        enableLocation();
    }

    private void enableLocation() {
        if(PermissionsManager.areLocationPermissionsGranted(this)){
            initalizeLocationEngine();
            initalizeLocationLayer();
        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }
    }

    private void initalizeLocationEngine() {
        locationEngine = new LocationEngineProvider(this).obtainBestLocationEngineAvailable();
        locationEngine.setPriority(LocationEnginePriority.HIGH_ACCURACY);
        locationEngine.activate();

        @SuppressLint("MissingPermission") Location lastlocation = locationEngine.getLastLocation();
        if (lastlocation != null) {
            originLocation = lastlocation;
        } else {
            locationEngine.addLocationEngineListener(this);
        }
    }

    @SuppressLint("MissingPermission")
    private void initalizeLocationLayer() {
        locationLayerPlugin = new LocationLayerPlugin(mapView, map, locationEngine);
        locationLayerPlugin.setLocationLayerEnabled(true);
        locationLayerPlugin.setCameraMode(CameraMode.TRACKING);
        locationLayerPlugin.setRenderMode(RenderMode.NORMAL);
    }

    private void setCameraPosition(Location location) {
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(),
                location.getLongitude()),13.0));
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onConnected() {
        locationEngine.requestLocationUpdates();
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            originLocation = location;
            setCameraPosition(location);
        }
        else {
            Log.d("Debug", "This is null");
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        // Present toast or dialogue
    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            enableLocation();
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    protected  void onStart() {
        super.onStart();
        if (locationEngine != null) {
            locationEngine.requestLocationUpdates();
        }
        if (locationLayerPlugin != null) {
            locationLayerPlugin.onStart();
        }
        mapView.onStart();


        ValueEventListener postListener = new ValueEventListener() {
            //            Log.d("Listening", "Listening!");
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                DataParser.readData(dataSnapshot);
//                String[] testPath = AStar.getBestPath(
////                        new Coordinate(-37.800449, 144.963938),
////                        new Coordinate(-37.802693, 144.973985)
//                        new Coordinate(-37.800, 144.963),
//                        new Coordinate(-37.802, 144.973)
//                );
//
//                for (String c : testPath) {
//                    System.out.printf("Next: %s)\n", c);
//                }
//
//                Coordinate[] testPath = AStar.getBestPath(
//                        new Coordinate(-37.800, 144.963),
//                        new Coordinate(-37.802, 144.973)
//
//                );
                Log.d("asd", "asd");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                // ...
            }
        };

        FirebaseAdapter.myRef.addValueEventListener(postListener);

    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (locationEngine != null) {
            locationEngine.removeLocationUpdates();
        }
        if (locationLayerPlugin != null) {
            locationLayerPlugin.onStop();
        }
        mapView.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (locationEngine != null) {
            locationEngine.deactivate();
        }
        mapView.onDestroy();
    }


    @Override
    public void onMapClick(@NonNull LatLng point) {
        if (destinationMarker != null) {
            map.removeMarker(destinationMarker);
        }

        destinationMarker = map.addMarker(new MarkerOptions().position(point));

        destinationPosition = com.mapbox.geojson.Point.fromLngLat(point.getLongitude(), point.getLatitude());
        originPosition = com.mapbox.geojson.Point.fromLngLat(originLocation.getLongitude(), originLocation.getLatitude());

        startButton.setEnabled(true);
        startButton.setBackgroundResource(R.color.colorPrimary);
    }

    private void getRoute(com.mapbox.geojson.Point origin, com.mapbox.geojson.Point destination, ArrayList<com.mapbox.geojson.Point> waypoints) {
//        NavigationRoute.builder()
//                .accessToken(Mapbox.getAccessToken())
//                .origin(origin)
//                .destination(destination)
//                .profile(DirectionsCriteria.PROFILE_WALKING)
//                .build()
//                .getRoute(new Callback<DirectionsResponse>() {
//                    @Override
//                    public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
//                        if (response.body() == null) {
//                            Log.e(TAG, "No routes found, check right user and access token");
//                            return;
//                        } else if (response.body().routes().size() == 0 ) {
//                            Log.e(TAG, "No routes found");
//                            return;
//                        }
//
//                        DirectionsRoute currRoute = response.body().routes().get(0);
//
//                        if (navigationMapRoute != null) {
//                            navigationMapRoute.removeRoute();
//                        } else {
//                            navigationMapRoute = new NavigationMapRoute(null, mapView, map);
//
//                        }
//
//                        navigationMapRoute.addRoute(currRoute);
//
//                    }
//
//                    @Override
//                    public void onFailure(Call<DirectionsResponse> call, Throwable t) {
//                        Log.e(TAG, "Error:" + t.getMessage());
//                    }
//                });
        NavigationRoute.Builder builder = NavigationRoute.builder(this)
                .accessToken(Mapbox.getAccessToken())
                .profile(DirectionsCriteria.PROFILE_WALKING)
                .origin(origin)
                .destination(destination);

        for (com.mapbox.geojson.Point waypoint : waypoints) {
            builder.addWaypoint(waypoint);
        }

        Log.d("SizeOfWaypoint", ((Integer) waypoints.size()).toString());

        builder.build()
            .getRoute(new Callback<DirectionsResponse>() {
                @Override
                public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {

                    if (response.body() == null) {
                        Log.e(TAG, "No routes found, check right user and access token");
                        return;
                    } else if (response.body().routes().size() == 0 ) {
                        Log.e(TAG, "No routes found");
                        return;
                    }

                    DirectionsRoute currRoute = response.body().routes().get(0);

                    if (navigationMapRoute != null) {
                        navigationMapRoute.removeRoute();
                    } else {
                        navigationMapRoute = new NavigationMapRoute(null, mapView, map);

                    }

                    navigationMapRoute.addRoute(currRoute);

                }

                @Override
                public void onFailure(Call<DirectionsResponse> call, Throwable t) {
                    Log.e(TAG, "Error:" + t.getMessage());
                }
            });
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
