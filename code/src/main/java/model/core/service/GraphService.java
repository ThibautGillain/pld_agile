package model.core.service;

import model.core.TSP;
import model.core.TSP2;
import model.data.*;
import model.core.Computing;
import org.checkerframework.checker.units.qual.C;

import java.sql.Time;
import java.util.ArrayList;
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

    public static Tour calculateTour(final Tour tour, final Graph graph) {
        Computing computing = new Computing();
        Tour res = tour;
        TSP tsp2 = new TSP2();
        int tpsLimite = Integer.MAX_VALUE;
        List<Journey> journeys = computing.getListJourney(tour, graph, tsp2, tpsLimite);

        List<Point> points = new ArrayList<>();
        for (int i = 1; i < journeys.size(); i++) {
            points.add(journeys.get(i).getStartPoint());
        }
        List<ActionPoint> actionPoints = new ArrayList<>();
        actionPoints.add(new ActionPoint(Time.valueOf("0:0:0"), tour.getBase(), ActionType.BASE));
        for (Point point : points) {
            boolean notFound = true;
            for (DeliveryProcess deliveryProcess :
                    tour.getDeliveryProcesses()) {
                if (deliveryProcess.getDelivery().getLocation() == point) {
                    actionPoints.add(deliveryProcess.getDelivery());
                    notFound = false;
                } else if (deliveryProcess.getPickUP().getLocation() == point) {
                    actionPoints.add(deliveryProcess.getPickUP());
                    notFound = false;
                }
                if (!notFound) break;;
            }
        }
        actionPoints.add(new ActionPoint(Time.valueOf("0:0:0"), tour.getBase(), ActionType.BASE));
        res.setActionPoints(actionPoints);

        //TODO: calculate time
        res.setJourneys(journeys);

        return res;
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
