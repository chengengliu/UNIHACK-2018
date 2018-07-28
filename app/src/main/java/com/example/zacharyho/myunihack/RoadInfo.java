package com.example.zacharyho.myunihack;

public class RoadInfo {
    public final float crime;
    public final float elevation;
    public final float lights;
    public final float parks;
    public final float niceness;
    public final float safety;


    public RoadInfo(float crime, float elevation, float lights, float parks, float niceness,
                    float safety) {
        this.crime = crime;
        this.elevation = elevation;
        this.lights = lights;
        this.parks = parks;
        this.niceness = niceness;
        this.safety = safety;
    }
}
