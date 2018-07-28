package com.example.zacharyho.myunihack;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DataParser {
    private static final String MAP_KEY = "map";
    private static final String POINTS_KEY = "points";
    private static final String ROADS_KEY = "roads";
    private static final String LATITUDE_KEY = "lat";
    private static final String LONGITUDE_KEY = "long";
    private static final String NEIGHBORS_KEY = "neighbours";
    private static final String DEBUG_NUM_KEY = "debug-num";

    private static final String CRIME_KEY  = "crime";
    private static final String ELEVATION_KEY  = "elevation";
    private static final String LIGHTS_KEY  = "lights";
    private static final String PARKS_KEY  = "parks";
    private static final String NICENESS_KEY  = "usr-NICE";
    private static final String SAFETY_KEY  = "usr-SAFE";


    public static Map<String, Intersection> intersections;
    public static Map<String, RoadInfo> roads;

    private DataParser() {
    }

    public static void readData(DataSnapshot snapshot) {
        readIntersections(snapshot);
        readRoads(snapshot);
        Log.d("Finished:", "Reading data");
    }

    private static void readIntersections(DataSnapshot snapshot) {
        intersections = new HashMap<>();

        // Grab list of points from snapshot.
        Iterable<DataSnapshot> points = snapshot.child(MAP_KEY).child(POINTS_KEY).getChildren();

        // Iterate over points/intersections.
        int count = 0;
        while (points.iterator().hasNext()) {
            DataSnapshot nextIntersection = points.iterator().next();
            final String latitude = (String) nextIntersection.child(LATITUDE_KEY).getValue();
            final String  longitude = (String) nextIntersection.child(LONGITUDE_KEY).getValue();

            Intersection newIntesection = new Intersection(latitude, longitude);

            // For debugging.
            newIntesection.debugNum = ((Long) nextIntersection.child(DEBUG_NUM_KEY).getValue()).toString();

            // Get list of neighbors.
            ArrayList<String> neigbors = (ArrayList<String>) nextIntersection.child(NEIGHBORS_KEY).getValue();
            for (String neighborId : neigbors) {

                if (intersections.containsKey(neighborId)) {
                    // We've already read in this neighbor and have an object for it. Let's add these
                    // two points as neighbors of one another.
                    intersections.get(neighborId).addNeighbor(newIntesection.getId(), newIntesection);
                    newIntesection.addNeighbor(neighborId, intersections.get(neighborId));
                }
            }

            intersections.put(newIntesection.getId(), newIntesection);
            count++;
            Log.d("Num of points read in:", Integer.toString(count));
        }

        Log.d("Finished:", "Reading intersections");
    }

    private static void readRoads(DataSnapshot snapshot) {
        roads = new HashMap<>();

        // Grab list of roads from snapshot.
        Iterable<DataSnapshot> roadList = snapshot.child(MAP_KEY).child(ROADS_KEY).getChildren();

        // Iterate over roads.
        int count = 0;
        while (roadList.iterator().hasNext()) {
            DataSnapshot nextRoad = roadList.iterator().next();
            final float crime = Float.parseFloat((String) nextRoad.child(CRIME_KEY).getValue());
            final float elevation = Float.parseFloat((String) nextRoad.child(ELEVATION_KEY).getValue());
            final float lights = Float.parseFloat((String) nextRoad.child(LIGHTS_KEY).getValue());
            final float parks = Float.parseFloat((String) nextRoad.child(PARKS_KEY).getValue());
            final float niceness = Float.parseFloat((String) nextRoad.child(NICENESS_KEY).getValue());
            final float safety = Float.parseFloat((String) nextRoad.child(SAFETY_KEY).getValue());

            final RoadInfo newRoadInfo = new RoadInfo(crime, elevation, lights, parks, niceness, safety);

            roads.put(nextRoad.getKey(), newRoadInfo);
            count++;
            Log.d("Num of roads read in:", Integer.toString(count));
        }
        Log.d("Finished:", "Reading roads");
    }
}
