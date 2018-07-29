package com.example.zacharyho.myunihack.routing.helperobjects;

/**
 * This represents a real coordinate.
 */
public class Coordinate {

    private final double latitude;
    private final double longitude;

    public Coordinate(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public Coordinate(com.mapbox.geojson.Point point) {
        this.latitude = point.latitude();
        this.longitude = point.longitude();
    }

    @Override
    public String toString() {
        return "(" + this.getLongitude() + ", " +   this.getLatitude() + ")";
    }
}
