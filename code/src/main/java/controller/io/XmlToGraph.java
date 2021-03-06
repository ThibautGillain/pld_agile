package controller.io;

import controller.management.ApplicationManagerImpl;
import model.genData.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.sql.Time;
import java.util.ArrayList;

public class XmlToGraph {
    /**
     * Number of Seconds in a Hour.
     */
    private static final int NB_SEC_IN_HOUR = 3600;
    /**
     * Number of Seconds in a minute.
     */
    private static final int NB_SEC_IN_MIN = 60;
    /**
     * ArrayList that contains nodes that we'll send at the end of the reading.
     * Represents the graph
     */
    private static ArrayList<Point> nodes;
    /**
     * Tour that contains deliveryProcess.
     */
    private static Tour tour;
    /**
     * ArrayList that contains DeliveryProcess that we'll send
     * at the end of the reading.
     */
    private static ArrayList<DeliveryProcess> deliveries;

    /**
     * Empty String.
     */
    public static final String EMPTY_STRING = "";

    /**
     * get the tour parameter.
     *
     * @return tour
     */
    public static Tour getTour() {
        return tour;
    }

    /**
     * Create the list of nodes from the Xml file.
     *
     * @param path path to the Xml file
     * @return List of Point
     */
    public ArrayList<Point> getGraphFromXml(final String path) {
        nodes = new ArrayList<Point>();

        if (path == null) {
            ApplicationManagerImpl.sendMessage(ErrorMessage.PATH_NULL);
            return nodes;
        }
        if (path.equals(EMPTY_STRING)) {
            ApplicationManagerImpl.sendMessage(ErrorMessage.PATH_EMPTY);
            return nodes;
        }
        // Get an instance of class "DocumentBuilderFactory".
        final DocumentBuilderFactory factory;
        factory = DocumentBuilderFactory.newInstance();
        try {
            // Creation of a parser.
            final DocumentBuilder builder = factory.newDocumentBuilder();

            //Creation of a document.
            final Document document = builder.parse(new File(path));

            // Get the root Element.
            final Element root = document.getDocumentElement();

            // Get the nodes tag and display the number of nodes.
            final NodeList nodeList = root.getElementsByTagName("noeud");
            final int nbNodeElements = nodeList.getLength();
            if (nbNodeElements == 0) {
                ApplicationManagerImpl.sendMessage(ErrorMessage.FILE_CORRUPTED);
                return nodes;
            }
            System.out.println("nbNodes :" + nbNodeElements);

            // Reading of all nodes in the fil and addition to the ArrayList.
            for (int nodeIndex = 0; nodeIndex < nbNodeElements; nodeIndex++) {
                final Element node = (Element) nodeList.item(nodeIndex);
                long nodeId = Long.parseLong(node.getAttribute("id"));
                String latString = node.getAttribute("latitude");
                double nodeLat = Double.parseDouble(latString);
                String longString = node.getAttribute("longitude");
                double nodeLong = Double.parseDouble(longString);
                Point point = new Point(nodeId, nodeLat, nodeLong);
                nodes.add(point);
            }

            // Get the segments tag and display the number of segments.
            final NodeList roadList = root.getElementsByTagName("troncon");
            final int nbRoadElements = roadList.getLength();
            if (nbRoadElements == 0) {
                ApplicationManagerImpl.sendMessage(ErrorMessage.FILE_CORRUPTED);
                return null;
            }
            System.out.println("nbRoad :" + nbRoadElements);

            // Reading of all segments in the file.
            for (int segmentIndex = 0; segmentIndex < nbRoadElements;
                 segmentIndex++) {
                final Element road = (Element) roadList.item(segmentIndex);
                String arrivalString = road.getAttribute("destination");
                long roadArrival = Long.parseLong(arrivalString);
                String lengthString = road.getAttribute("longueur");
                double roadLength = Double.parseDouble(lengthString);
                String roadName = road.getAttribute("nomRue");
                String departureString = road.getAttribute("origine");
                long roadDeparture = Long.parseLong(departureString);
                Segment segment = new Segment(roadDeparture, roadArrival,
                        roadLength, roadName);

                for (Point node : nodes) {
                    if (node.getId() == roadDeparture) {
                        node.addNeighbour(segment);
                    }
                }
            }

        } catch (final ParserConfigurationException | SAXException
                | IOException | NumberFormatException e) {
            ApplicationManagerImpl.sendMessage(ErrorMessage.XML_LOAD_ERROR);
        }
        return nodes;
    }

    /**
     * Create the list of deliveries from the provided file.
     *
     * @param path path to the Xml file
     * @return List of deliveries
     */
    public Tour getDeliveriesFromXml(final String path) {
        deliveries = new ArrayList<DeliveryProcess>();
        Tour fakeTour = new Tour();
        if (path == null) {
            ApplicationManagerImpl.sendMessage(ErrorMessage.PATH_NULL);
            return tour;
        }
        if (path.equals("")) {
            ApplicationManagerImpl.sendMessage(ErrorMessage.PATH_EMPTY);
            return tour;
        }

        // Get an instance of class "DocumentBuilderFactory".
        final DocumentBuilderFactory factory;
        factory = DocumentBuilderFactory.newInstance();
        try {
            // Creation of a parser.
            final DocumentBuilder builder = factory.newDocumentBuilder();

            // Creation of a document.
            final Document document = builder.parse(new File(path));

            // Get the root Element.
            final Element root = document.getDocumentElement();

            // Get the DeliveryProcess tag
            // and display the number of DeliveryProcess.
            final NodeList start = root.getElementsByTagName("entrepot");
            final Element startPoint = (Element) start.item(0);
            if (startPoint == null) {
                ApplicationManagerImpl.sendMessage(ErrorMessage.FILE_CORRUPTED);
                return fakeTour;
            }
            Long idBase = Long.parseLong(startPoint.getAttribute("adresse"));
            Point base = getPointById(idBase);
            if (base == null) {
                return fakeTour;
            }
            System.out.println("entrepot :" + idBase);
            // Recup startTime
            String startTimeString = startPoint.getAttribute("heureDepart");
            Time startTime = Time.valueOf(startTimeString);
            System.out.println("startTime = " + startTime);

            // get list livraison
            final NodeList deliveryList;
            deliveryList = root.getElementsByTagName("livraison");
            final int nbDeliveryElements = deliveryList.getLength();
            if (nbDeliveryElements == 0) {
                ApplicationManagerImpl.sendMessage(ErrorMessage.FILE_CORRUPTED);
                return fakeTour;
            }
            System.out.println("nbdeliveryelements :" + nbDeliveryElements);

            // Reading of all DeliveryProcess in the file
            // and addition to the ArrayList.
            for (int deliveryIndex = 0; deliveryIndex < nbDeliveryElements;
                 deliveryIndex++) {
                final Element deliveryXml;
                deliveryXml = (Element) deliveryList.item(deliveryIndex);
                String pickupIdString;
                pickupIdString = deliveryXml.getAttribute(
                        "adresseEnlevement");
                Long pickupPointId = Long.parseLong(pickupIdString);
                System.out.println("idPick " + pickupPointId);
                Point pickupPoint = getPointById(pickupPointId);
                if (pickupPoint == null) {
                    return fakeTour;
                }
                String deliveryIdString;
                deliveryIdString = deliveryXml.getAttribute(
                        "adresseLivraison");
                Long deliveryPointId = Long.parseLong(deliveryIdString);
                System.out.println("idDeliver "
                        + deliveryPointId);
                Point deliveryPoint = getPointById(deliveryPointId);
                if (deliveryPoint == null) {
                    return fakeTour;
                }
                String pickupTimeString;
                pickupTimeString = deliveryXml.getAttribute(
                        "dureeEnlevement");
                int pickupTimeInt = Integer.parseInt(pickupTimeString);
                Time pickupTime = durationToTime(pickupTimeInt);
                String deliveryTimeString;
                deliveryTimeString = deliveryXml.getAttribute(
                        "dureeLivraison");
                int deliveryTimeInt = Integer.parseInt(deliveryTimeString);
                Time deliveryTime = durationToTime(deliveryTimeInt);
                ActionPoint pickupActionpoint;
                pickupActionpoint = new ActionPoint(pickupTime, pickupPoint,
                        ActionType.PICK_UP);
                pickupActionpoint.setId(deliveryIndex + 1);
                ActionPoint deliveryActionpoint;
                deliveryActionpoint = new ActionPoint(deliveryTime,
                        deliveryPoint, ActionType.DELIVERY);
                deliveryActionpoint.setId(deliveryIndex + 1);
                DeliveryProcess deliv;
                deliv = new DeliveryProcess(pickupActionpoint,
                        deliveryActionpoint);
                deliveries.add(deliv);
            }
            tour = new Tour(deliveries, base, startTime);

        } catch (final ParserConfigurationException | SAXException
                | IOException | IllegalArgumentException e) {
            ApplicationManagerImpl.sendMessage(ErrorMessage.XML_LOAD_ERROR);
        }
        return tour;
    }

    /**
     * Get the point with the provided id.
     *
     * @param idPoint id of the Point
     * @return the Point
     */
    public static Point getPointById(final long idPoint) {
        Point point = null;
        for (Point p : nodes) {
            if (p.getId() == idPoint) {
                point = p;
            }
        }
        if (point == null) {
            ApplicationManagerImpl.sendMessage(ErrorMessage.POINT_DOESNT_EXIST);
            return point;
        }
        return point;
    }

    /**
     * transform a duration in a time.
     *
     * @param durationSec duration in Seconds
     * @return time object corresponding to durationSec
     */
    public Time durationToTime(final int durationSec) {
        int nbHour = durationSec / NB_SEC_IN_HOUR;
        int nbMin = (durationSec % NB_SEC_IN_HOUR) / NB_SEC_IN_MIN;
        int nbSec = (durationSec % NB_SEC_IN_MIN);
        String durationString;
        durationString = String.format("%d:%02d:%02d", nbHour, nbMin, nbSec);
        Time duration = Time.valueOf(durationString);
        System.out.println("duration = " + duration);
        return duration;
    }
}
