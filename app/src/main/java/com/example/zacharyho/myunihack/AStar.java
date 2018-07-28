package com.example.zacharyho.myunihack;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class AStar {

    /** Reasonable min/max for this scalar between 0 - 2000. **/
    private static final double UNSAFETY_SCALAR = 1500;
    private static final double HCOST_CONSTANT = 100;
    private static final double EARTH_RADIUS_M = 6371000;

    private static Intersection start;
    private static Intersection goal;

    private static ArrayList<Intersection> evaluatedNodes;
    private static Map<Intersection, Double> unevaluatedNodes;
    private static Map<Intersection, Intersection> cameFrom;
    private static Map<Intersection, Double> gCosts;

    /**
     * Given a starting and ending goal, returns an ordered list of coordinates that provides
     * the "best" route.
     * @param start
     * @param goal
     * @return
     */
//    public static Coordinate[] getBestPath(Coordinate start, Coordinate goal) {
    public static String[] getBestPath(Coordinate start, Coordinate goal) {
        AStar.start = getClosestIntersection(start);
        AStar.goal = getClosestIntersection(goal);

        // Initialize data structures.
        evaluatedNodes = new ArrayList<>();
        unevaluatedNodes = new HashMap<>();
        unevaluatedNodes.put(AStar.start, getHCost(AStar.start));
        cameFrom = new HashMap<>();
        gCosts = new HashMap<>();
        gCosts.put(AStar.start, 0.0);

        while (!unevaluatedNodes.isEmpty()) {
            Intersection current = getLowestFCostCoordinate();

            if (current.getId().equals(AStar.goal.getId())) {
                return reconstructPath(current);
            }

            unevaluatedNodes.remove(current);
            evaluatedNodes.add(current);

            Intersection[] neighbors = current.getNeighbors();
            for (Intersection neighbor : neighbors) {
                if (evaluatedNodes.contains(neighbor)) {
                    continue;
                }

                if (!unevaluatedNodes.containsKey(neighbor)) {
                    unevaluatedNodes.put(neighbor, Double.MAX_VALUE);
                }

                final double gCost = gCosts.get(current) + getGCost(current, neighbor);
                if (gCosts.containsKey(neighbor)) {
                    if (gCost >= gCosts.get(neighbor)) {
                        continue;
                    }
                }

                // This path to 'neighbor' is the best so far. Record it.
                cameFrom.put(neighbor, current);
                gCosts.put(neighbor, gCost);
                final double fCost = gCost + getHCost(neighbor);
                unevaluatedNodes.put(neighbor, fCost);
            }
        }

        // Was unable to find a path. Return null.
        return null;
    }

    private static double getGCost(Intersection from, Intersection to) {

        RoadInfo roadInfo = DataParser.roads.get(generateRoadID(from, to));

        double dist = getDistanceAsMetres(from.getCoordinate(), to.getCoordinate());
        return Math.max(dist + UNSAFETY_SCALAR * (roadInfo.crime + roadInfo.parks - roadInfo.safety - roadInfo.lights), 0);

    }

    private static double getHCost(Intersection i1) {
        return getDistanceAsMetres(i1.getCoordinate(), AStar.goal.getCoordinate());
    }

    /**
     * Returns the coordinate with the lowest fCost from unexploredKnownNodes. Does not remove it.
     * @return the coordinate with the lowest fCost from unexploredKnownNodes.
     */
    private static Intersection getLowestFCostCoordinate() {
        Intersection lowestIntersection = null;
        double lowestFCost = Double.MAX_VALUE;

        for (Intersection currIntersection : unevaluatedNodes.keySet()) {
            double currFCost = unevaluatedNodes.get(currIntersection);
            if (currFCost < lowestFCost) {
                lowestFCost = currFCost;
                lowestIntersection = currIntersection;
            }
        }

        return lowestIntersection;
    }

//    /**
//     * Given an ending coordinate, reconstructs the path from 'start' to it, returning the shortest path.
//     * @param end is the ending coordinate.
//     * @return a list of sequential coordinates leading from 'start' to 'end'.
//     */
//    private static Coordinate[] reconstructPath(Intersection end) {
//        ArrayList<Coordinate> path = new ArrayList<>();
//        path.add(AStar.goal.getCoordinate());
//        Intersection current = goal;
//        while (cameFrom.containsKey(current)) {
//            current = cameFrom.get(current);
//            path.add(current.getCoordinate());
//        }
//
//        Collections.reverse(path);
//        return path.toArray(new Coordinate[0]);
//    }


//     * Given an ending coordinate, reconstructs the path from 'start' to it, returning the shortest path.
//     * @param end is the ending coordinate.
//     * @return a list of sequential coordinates leading from 'start' to 'end'.
//     */
    private static String[] reconstructPath(Intersection end) {
        ArrayList<String> path = new ArrayList<>();
        path.add(AStar.goal.debugNum);
        Intersection current = goal;
        while (cameFrom.containsKey(current)) {
            current = cameFrom.get(current);
            path.add(current.debugNum);
        }

        Collections.reverse(path);
        return path.toArray(new String[0]);
    }

    /**
     * Generate our RoadID from two intersects
     * @param i1
     * @param i2
     * @return
     */
    private static String generateRoadID(Intersection i1, Intersection i2) {

        return (i1.getId().compareTo(i2.getId()) > 0 ? i2.getId() + i1.getId() : i1.getId() + i2.getId());

    }

    /**
     * Finds the closest intersection to the provided coordinate
     * @param coord
     * @return
     */
    private static Intersection getClosestIntersection(Coordinate coord) {

        double shortest = Double.MAX_VALUE;
        Intersection closestIntersection = null;

        for (Intersection intersection : DataParser.intersections.values()) {
            double dist = getDistanceAsMetres(intersection.getCoordinate(), coord);
            if (dist < shortest) {
                closestIntersection = intersection;
                shortest = dist;
            }
        }

        return closestIntersection;
    }

    /**
     * Calculates the raw euclidean distance between two coordinates in Metres
     * @return
     */
    private static double getDistanceAsMetres(Coordinate p1, Coordinate p2) {

        double dLat = degToRad(p2.getLatitude()-p1.getLatitude());
        double dLon = degToRad(p2.getLongitude()-p1.getLongitude());

        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                    Math.sin(dLon/2) * Math.sin(dLon/2) *
                    Math.cos(degToRad(p1.getLatitude())) * Math.cos(degToRad(p2.getLatitude()));

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

        return EARTH_RADIUS_M * c;

    }

    private static double degToRad(double degrees) {
        return degrees * Math.PI / 180;
    }
}
