package model.core.service;

import model.data.Graph;
import model.data.Journey;
import model.data.Point;
import model.data.Tour;

import java.util.List;

public class GraphService {


    public Point findNearestPoint(final List<Point> pointList, final double longitude,
                                  final double latitude) {
        final Point nearestPoint;
        long nearestId = 0;
        double nearestLong = 0.0, nearestLat = 0.0;
        double differenceLong = 100.0, differenceLat = 100.0;
        for (final Point p : pointList) {
            if ((Math.abs(p.getLatitude() - latitude) < differenceLat) &&
                    (Math.abs(p.getLongitude() - longitude) < differenceLong)) {
                differenceLat = Math.abs(p.getLatitude() - latitude);
                differenceLong = Math.abs(p.getLongitude() - longitude);
                nearestLat = p.getLatitude();
                nearestLong = p.getLongitude();
                nearestId = p.getId();
            }
        }
        /*
        System.out.println("nearestLong : " + nearestLong + " nearestLat : "
        + nearestLat);
        System.out.println("nearestID : " + nearestId);
        System.out.println("differenceLong : "+differenceLong+" differenceLat"
        +differenceLat);
        */
        nearestPoint = new Point(nearestId, nearestLat, nearestLong);
        return nearestPoint;
    }

    public static Tour calculateTour(final Tour tour) {
        return null;
    }

    public static boolean isInMap(final Point newPoint) {
        //todo
        return false;
    }


    public static Journey shortestPath(final Graph graph, final Point point1, final Point point2) {
        //todo
        return null;
    }
}