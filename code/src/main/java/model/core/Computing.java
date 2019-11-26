package model.core;

import model.data.*;
import model.io.XmlToGraph;
import org.apache.commons.lang.Validate;

import javax.swing.event.ListDataListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Computing {
    /**
     * this class contains the previous point index and the distance in the shortest path from the start point to each point
     */
    class tuple {
        private int prev;
        private double dist;
        tuple(int prev, double dist) {
            if (prev < 0) {
                throw new IllegalArgumentException("prev is negative");
            }
            if (dist < 0) {
                throw new IllegalArgumentException("dist is negative");
            }
            this.prev = prev;
            this.dist = dist;
        }

        public int getPrev() {
            return prev;
        }
        public double getDist() {
            return dist;
        }
        public void setPrev(int prev) {
            if (prev < 0) {
                throw new IllegalArgumentException("prev is negative");
            }
            this.prev = prev;
        }
        public void setDist(double dist) {
            if (dist < 0) {
                throw new IllegalArgumentException("dist is negative");
            }
            this.dist = dist;
        }
    }

    /**
     * Dijkstra shortest path
     * the shortest path from start point to other points
     * @param graph Map which contains a list of points with segments connect to each of them
     * @param id_start Id of start point
     * @return List of tuple which contains the previous point index and the distance in the shortest path from the start point to each point
     */
    public List<tuple> dijkstra(final Graph graph, final long id_start) {
        Map<Long,Integer> map = graph.getMap();
        if (map.get(id_start) == null) {
            throw new IllegalArgumentException("id_start not in graph");
        }
        List<tuple> res = new ArrayList<>();

        int nb_points = graph.getNbPoints();
        int start_index = map.get(id_start);
        // flag[i] represents whether we've already got the shortest path from start point to the i-th point
        boolean[] flag = new boolean[nb_points];
        List<Point> points = graph.getPoints();
        Point start_point = points.get(start_index);
        for (int i = 0; i < nb_points; i++) {
            flag[i] = (i == start_index);
            res.add(new tuple(start_index, start_point.getLengthTo(points.get(i).getId())));
        }
        int cur_index = start_index;
        for (int i = 1; i < nb_points; i++) {
            double min = Double.POSITIVE_INFINITY;
            for (int j = 0; j < nb_points; j++) {
                if (!flag[j] && res.get(j).getDist() < min) {
                    min = res.get(j).getDist();
                    cur_index = j;
                }
            }
            flag[cur_index] = true;
            Point cur_point = points.get(cur_index);
            for (Segment s : cur_point.getSegments()) {
                long id_other = s.getId_end();
                int index_other = map.get(id_other);
                if (flag[index_other]) continue;
                double tmp = cur_point.getLengthTo(id_other);
                tmp = (tmp == Double.POSITIVE_INFINITY) ? tmp : tmp + min;
                if (tmp < res.get(index_other).getDist()) {
                    res.get(index_other).setPrev(cur_index);
                    res.get(index_other).setDist(tmp);
                }
            }
        }
        System.out.printf("dijkstra(%d)\n", id_start);
        for (int i = 0; i < nb_points; i++) {
            System.out.printf("  shortest(%d, %d)=%f\n", id_start, points.get(i).getId(), res.get(i).getDist());
        }
        return res;
    }

    /**
     * Create the shortest path from one point to another
     * @param graph Map which contains a list of points with segments connect to each of them
     * @param id_start Id of the start point of the journey
     * @param id_arrive Id of the arrival point of the journey
     * @param res_dijkstra Result of dijkstra(id_start)
     * @return Journey which represents the shortest path from the start point to arrival point
     */
    public Journey getShortestPath(final Graph graph, final long id_start, final long id_arrive, List<tuple> res_dijkstra) {
        Map<Long,Integer> map = graph.getMap();
        if (map.get(id_start) == null) {
            throw new IllegalArgumentException("id_start not in graph");
        }
        if (map.get(id_arrive) == null) {
            throw new IllegalArgumentException("id_arrive not in graph");
        }
        Validate.notNull(res_dijkstra,"res_dijkstra is null");

        List<Point> points = graph.getPoints();
        int start_index = map.get(id_start);
        int arrive_index = map.get(id_arrive);
        List<Long> ids = new ArrayList<>();
        int cur_index = arrive_index;
        double min_length = res_dijkstra.get(arrive_index).getDist();
        if (min_length == Double.POSITIVE_INFINITY) return null;
        while (true) {
            ids.add(points.get(cur_index).getId());
            if (cur_index == start_index) break;
            cur_index = res_dijkstra.get(cur_index).getPrev();
        }
        System.out.printf("Shortest Path from %d to %d in REVERSE order:\n", id_start, id_arrive);
        for (long id : ids) {
            System.out.printf("  %d", id);
        }
        System.out.println();
        return new Journey(id_start,id_arrive,ids,min_length);
    }

    public List<List<tuple>> applyDijkstraToTour(final Tour tour, final Graph graph) {
        List<List<tuple>> res_dijkstra = new ArrayList<>();
        int nb = tour.getDeliveryProcesses().size();
        for (int i = 0; i < 2*nb+1; i++) {
            Point point;
            if (i == 0) {
                point = tour.getBase();
            } else if (i < nb + 1) {
                point = tour.getDeliveryProcesses().get(i - 1).getPickUP().getLocation();
            } else {
                point = tour.getDeliveryProcesses().get(i - nb - 1).getDelivery().getLocation();
            }
            res_dijkstra.add(dijkstra(graph, point.getId()));
        }
        return res_dijkstra;
    }

    public int[][] getCost(final Tour tour, final Graph graph, List<List<tuple>> res_dijkstra) {
        Map<Long,Integer> map = graph.getMap();
        int nb = tour.getDeliveryProcesses().size();
        int[][] cost = new int[2*nb+1][2*nb+1];
        for (int i = 0; i < 2*nb+1; i++) {
            for (int j = 0; j < 2 * nb + 1; j++) {
                Point point;
                if (j==0) {
                    point = tour.getBase();
                } else if (j < nb+1) {
                    point = tour.getDeliveryProcesses().get(j-1).getPickUP().getLocation();
                } else {
                    point = tour.getDeliveryProcesses().get(j-nb-1).getDelivery().getLocation();
                }
                int index = map.get(point.getId());
                cost[i][j] = (int)res_dijkstra.get(i).get(index).getDist();
            }
        }
        return cost;
    }

    public List<Journey> getListJourney(final Tour tour, final Graph graph, TSP tsp, int tpsLimite) {
        List<List<tuple>> res_dijkstra = applyDijkstraToTour(tour, graph);
        int[][] cout = getCost(tour, graph, res_dijkstra);
        int nbSommets = tour.getDeliveryProcesses().size()*2+1;
        int[] duree = new int[nbSommets];
        tsp.chercheSolution(tpsLimite,nbSommets,cout,duree);

        List<Journey> journeys = new ArrayList<>();
        for (int i = 0; i < nbSommets; i++) {
            int index_start_tour = tsp.getMeilleureSolution(i);
            int index_arrive_tour = tsp.getMeilleureSolution((i+1)%nbSommets);
            long id_start;
            if (index_start_tour == 0) {
                id_start = tour.getBase().getId();
            } else if (index_start_tour < nbSommets/2+1) {
                id_start = tour.getDeliveryProcesses().get(index_start_tour-1).getPickUP().getLocation().getId();
            } else {
                id_start = tour.getDeliveryProcesses().get(index_start_tour-1-nbSommets/2).getDelivery().getLocation().getId();
            }
            long id_arrive;
            if (index_arrive_tour == 0) {
                id_arrive = tour.getBase().getId();
            } else if (index_arrive_tour < nbSommets/2+1) {
                id_arrive = tour.getDeliveryProcesses().get(index_arrive_tour-1).getPickUP().getLocation().getId();
            } else {
                id_arrive = tour.getDeliveryProcesses().get(index_arrive_tour-1-nbSommets/2).getDelivery().getLocation().getId();
            }
            Journey journey = getShortestPath(graph,id_start,id_arrive,res_dijkstra.get(index_start_tour));
            journeys.add(journey);
        }
        return journeys;
    }

    public static void main(String[] args) {
//        List<Point> points = new ArrayList<>();
//        Point p1 = new Point(1,0,0);
//        p1.addNeighbour(new Segment(1,2,1,"s12"));
//        p1.addNeighbour(new Segment(1,4,1,"s14"));
//        Point p2 = new Point(2,0,0);
//        p2.addNeighbour(new Segment(2,5,1,"s25"));
//        Point p3 = new Point(3,0,0);
//        p3.addNeighbour(new Segment(3,1,1,"s31"));
//        p3.addNeighbour(new Segment(3,6,1,"s36"));
//        Point p4 = new Point(4,0,0);
//        p4.addNeighbour(new Segment(4,2,1,"s42"));
//        p4.addNeighbour(new Segment(4,6,1,"s46"));
//        Point p5 = new Point(5,0,0);
//        Point p6 = new Point(6,0,0);
//        p6.addNeighbour(new Segment(6,5,1,"s65"));
//        points.add(p1);
//        points.add(p2);
//        points.add(p3);
//        points.add(p4);
//        points.add(p5);
//        points.add(p6);
//        Graph graph = new Graph(points);
//        graph.show_map();
//        List<tuple> res_dijkstra = dijkstra(graph, 5);
//        Journey journey = getShortestPath(graph, 5, 3,res_dijkstra);
//        if (journey == null) System.out.println("journey null");
        Computing computing = new Computing();
        XmlToGraph xmlToGraph = new XmlToGraph();
        List<Point> points = xmlToGraph.getGraphFromXml("moyenPlan.xml");
        Graph graph = new Graph(points);
        graph.show_map();

        TSP tsp1 = new TSP1();
		int tpsLimite = Integer.MAX_VALUE;
		List<DeliveryProcess> deliveryProcesses = new ArrayList<>();
        deliveryProcesses.add(new DeliveryProcess(new ActionPoint(1,new Point(26121686,0,0), ActionType.PICK_UP), new ActionPoint(1,new Point(191134392,0,0),ActionType.DELIVERY)));
        deliveryProcesses.add(new DeliveryProcess(new ActionPoint(1,new Point(55444018,0,0), ActionType.PICK_UP), new ActionPoint(1,new Point(26470086,0,0),ActionType.DELIVERY)));
        deliveryProcesses.add(new DeliveryProcess(new ActionPoint(1,new Point(27362899,0,0), ActionType.PICK_UP), new ActionPoint(1,new Point(505061101,0,0),ActionType.DELIVERY)));
		Tour tour = new Tour(deliveryProcesses, new Point(1349383079,0,0),1);

        computing.getListJourney(tour,graph,tsp1,tpsLimite);
    }
}
