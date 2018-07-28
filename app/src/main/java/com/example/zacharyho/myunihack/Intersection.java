package com.example.zacharyho.myunihack;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Intersection {
    public String debugNum;

    private final String latitude;
    private final String longitude;
    private Map<String, Intersection> neighbors = new HashMap<>();

    public Intersection(String latitude, String longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitudeAsDouble() {
        return Double.parseDouble(latitude);
    }

    public double getLongitudeAsDouble() {
        return Double.parseDouble(longitude);
    }

    public String getLatitudeAsString() {
        return latitude;
    }

    public String getLongitudeAsString() {
        return longitude;
    }

    public Coordinate getCoordinate() {
        return new Coordinate(this.getLatitudeAsDouble(), this.getLongitudeAsDouble());
    }

    public Intersection getNeighbor(String id) {
        return neighbors.get(id);
    }

    public Boolean hasNeighbor(String id) {
        return neighbors.containsKey(id);
    }

    public String getId() {
        final String alteredLatitude = latitude.replace(".", "");
        final String alteredLongitude = longitude.replace(".", "");
        return String.format(Locale.ENGLISH, "%s%s", alteredLatitude, alteredLongitude);
    }

    public void addNeighbor(String id, Intersection neighbor) {
        neighbors.put(id, neighbor);
    }

    public Intersection[] getNeighbors() {
        return neighbors.values().toArray(new Intersection[0]);
    }
}