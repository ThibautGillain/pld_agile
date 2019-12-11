package view;

import com.lynden.gmapsfx.GoogleMapView;
import com.lynden.gmapsfx.MapComponentInitializedListener;
import com.lynden.gmapsfx.javascript.JavascriptObject;
import com.lynden.gmapsfx.javascript.event.GMapMouseEvent;
import com.lynden.gmapsfx.javascript.event.UIEventType;
import com.lynden.gmapsfx.javascript.object.*;
import com.lynden.gmapsfx.service.directions.*;
import com.lynden.gmapsfx.shapes.Polyline;
import com.lynden.gmapsfx.shapes.PolylineOptions;
import com.lynden.gmapsfx.util.MarkerImageFactory;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.FileChooser;
import lombok.Getter;
import model.core.service.TourService;
import model.data.*;
import org.apache.commons.lang.Validate;

import javax.swing.*;
import javax.xml.validation.Validator;
import java.io.File;
import java.lang.management.BufferPoolMXBean;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;

public class DashBoardController implements Initializable, MapComponentInitializedListener  {

    //Map Style.
    private static final String mapStyle = "[{\"featureType\":\"administrative\",\"elementType\":\"all\",\"stylers\":[{\"visibility\":\"off\"}]},{\"featureType\":\"landscape.man_made\",\"elementType\":\"geometry.fill\",\"stylers\":[{\"color\":\"#e9e5dc\"}]},{\"featureType\":\"landscape.natural\",\"elementType\":\"geometry.fill\",\"stylers\":[{\"visibility\":\"on\"},{\"color\":\"#b8cb93\"}]},{\"featureType\":\"poi\",\"elementType\":\"all\",\"stylers\":[{\"visibility\":\"off\"}]},{\"featureType\":\"poi.attraction\",\"elementType\":\"all\",\"stylers\":[{\"visibility\":\"off\"}]},{\"featureType\":\"poi.business\",\"elementType\":\"all\",\"stylers\":[{\"visibility\":\"off\"}]},{\"featureType\":\"poi.government\",\"elementType\":\"all\",\"stylers\":[{\"visibility\":\"off\"}]},{\"featureType\":\"poi.medical\",\"elementType\":\"all\",\"stylers\":[{\"visibility\":\"off\"}]},{\"featureType\":\"poi.park\",\"elementType\":\"all\",\"stylers\":[{\"visibility\":\"off\"}]},{\"featureType\":\"poi.park\",\"elementType\":\"geometry.fill\",\"stylers\":[{\"color\":\"#ccdca1\"}]},{\"featureType\":\"poi.place_of_worship\",\"elementType\":\"all\",\"stylers\":[{\"visibility\":\"off\"}]},{\"featureType\":\"poi.school\",\"elementType\":\"all\",\"stylers\":[{\"visibility\":\"off\"}]},{\"featureType\":\"poi.sports_complex\",\"elementType\":\"all\",\"stylers\":[{\"visibility\":\"off\"}]},{\"featureType\":\"road\",\"elementType\":\"geometry.fill\",\"stylers\":[{\"hue\":\"#ff0000\"},{\"saturation\":-100},{\"lightness\":99}]},{\"featureType\":\"road\",\"elementType\":\"geometry.stroke\",\"stylers\":[{\"color\":\"#808080\"},{\"lightness\":54},{\"visibility\":\"off\"}]},{\"featureType\":\"road\",\"elementType\":\"labels.text.fill\",\"stylers\":[{\"color\":\"#767676\"}]},{\"featureType\":\"road\",\"elementType\":\"labels.text.stroke\",\"stylers\":[{\"color\":\"#ffffff\"}]},{\"featureType\":\"transit\",\"elementType\":\"all\",\"stylers\":[{\"visibility\":\"off\"}]},{\"featureType\":\"water\",\"elementType\":\"all\",\"stylers\":[{\"saturation\":43},{\"lightness\":-11},{\"color\":\"#89cada\"}]}]";

    private Tour tourLoaded;

    public void setTour(Tour tour) {
        tourLoaded = tour;
    }

    //Enum Marker Types.
    @Getter
    public enum MarkerType {
        PICKUP("Pick-Up Point", "P","icons/marker.png"),
        DELIVERY("Delivery Point", "D", "flag.png"),
        BASE("Base Point", "B", "home-icon-silhouette.png");

        private String title = "";
        private String firstLetter = "";
        private String iconPath = "";

        MarkerType(String title, String firstLetter, String iconPath) {
            this.title = title;
            this.firstLetter = firstLetter;
            this.iconPath = iconPath;
        }
    }

    // Reference to the main application
    private UserInterface mainApp;

    // List des ActionPoints en Observable pour la view
    private ObservableList<ActionPoint> actionPoints = FXCollections.observableArrayList();

    @FXML
    private TableView<ActionPoint> actionPointTableView;

    @FXML
    private TableColumn<ActionPoint, String> deliveryRank;

    @FXML
    private TableColumn<ActionPoint, String> deliveryType;

    @FXML
    private TableColumn<ActionPoint, String> timeAtPoint;

    @FXML
    private Label labelPickUp;

    @FXML
    private Label labelDelivery;

    @FXML
    private Label baseLocation;

    @FXML
    private GoogleMapView mapView;

    private GoogleMap map;

    public DashBoardController() {
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        // Initialize the actionPoints table with the 3 columns.
        actionPointTableView.setItems(null);

        deliveryRank.setCellValueFactory(cellData -> new SimpleStringProperty
                (String.valueOf(actionPoints.indexOf(cellData.getValue()))));
        deliveryType.setCellValueFactory(
                cellData -> new SimpleStringProperty(cellData.getValue().getActionType().toString()));
        timeAtPoint.setCellValueFactory(
                cellData -> new SimpleStringProperty(TourService.calculateTimeAtPoint(tourLoaded,cellData.getValue())));

        mapView.addMapInializedListener(this);
        mapView.setKey("AIzaSyDJDcPFKsYMTHWJUxVzoP0W7ERsx3Bhdgc");
    }

    @Override
    public void mapInitialized() {
        //Set the initial properties of the map.
        MapOptions mapOptions = new MapOptions();

        //TODO Create a graph center and pass it to display Map;
        mapOptions.center(new LatLong(45.771606, 4.880959))
                .styleString(mapStyle)
                .overviewMapControl(false)
                .mapType(MapTypeIdEnum.ROADMAP)
                .panControl(false)
                .rotateControl(false)
                .scaleControl(false)
                .streetViewControl(false)
                .zoomControl(false)
                .zoom(12);

        /*
        InfoWindowOptions infoWindowOptions = new InfoWindowOptions();
        infoWindowOptions.content("<h2>Fred Wilkie</h2>"
                + "Current Location: Safeway<br>"
                + "ETA: 45 minutes" );

        InfoWindow bobUnderwoodWindow = new InfoWindow(infoWindowOptions);
        bobUnderwoodWindow.open(map, bobUnderwoodMarker);
        map.addMarker( bobUnderwoodMarker );
         */
    }

    public void calculateTour() {
        this.mainApp.calculateTour();
    }

    public void handleLoadMap() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select a Map XML");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("XML", "*.xml")
        );
        final File selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null) {
            if (selectedFile.getName().contains("xml")) {
                System.out.println("File selected: " + selectedFile.getName());
                this.mainApp.loadMap(selectedFile);
            } else {
                System.out.println("Error Loading not xml File");
            }
        } else {
            System.out.println("File selection cancelled.");
        }
    }

    public void handleLoadTour() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select a Tour XML");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("XML", "*.xml")
        );
        File selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null) {
            if (selectedFile.getName().contains("xml")) {
                System.out.println("File selected: " + selectedFile.getName());
                this.mainApp.loadDeliveryRequest(selectedFile);
            } else {
                System.out.println("Error Loading not xml File");
            }
        } else {
            System.out.println("File selection cancelled.");
        }
    }

    public void traceDirection() {
        drawPolyline(getMCVPathFormJourney(0), "red");
    }

    /**
     * Is called by the main application to give a reference back to itself.
     *
     * @param mainApp
     */
    public void setMainApp(final UserInterface mainApp) {
        Validate.notNull(mainApp);
        this.mainApp = mainApp;
    }

    public void displayMap() {
        //  Set new center for the map
        MapOptions mapOptions = new MapOptions();
        mapOptions.center(new LatLong(45.771606, 4.880959))
                .styleString(mapStyle)
                .overviewMapControl(false)
                .mapType(MapTypeIdEnum.ROADMAP)
                .panControl(false)
                .rotateControl(false)
                .scaleControl(false)
                .streetViewControl(false)
                .zoomControl(false)
                .zoom(12);

        // Add map to the view
        map = mapView.createMap(mapOptions);
    }

    public void displayLoadedDeliveryProcess() {
        //TODO Commenter

        // Set The Base ID on the view
        baseLocation.setText(String.valueOf(tourLoaded.getBase().getId()) );

        // Create a fake list of action Points To display.
        List<ActionPoint> fakeListActionPoints = createFakeActionPointList(tourLoaded.getDeliveryProcesses());

        actionPoints.addAll(fakeListActionPoints);

        // Add observable list data to the table
        actionPointTableView.setItems(actionPoints);
        drawAllActionPoints(fakeListActionPoints);
    }

    //TODO A REPENSER
    public List<ActionPoint> createFakeActionPointList(final List<DeliveryProcess> listDeliveryProcess) {
        List<ActionPoint> listActionPoints = new ArrayList<ActionPoint>();
        for (DeliveryProcess deliveryProcess : listDeliveryProcess) {
            listActionPoints.add(new ActionPoint(deliveryProcess.getPickUP()
                    .getTime(), deliveryProcess.getPickUP().getLocation(),
                    deliveryProcess.getPickUP().getActionType()));
            listActionPoints.add(new ActionPoint(deliveryProcess.getDelivery().getTime(), deliveryProcess.getDelivery().getLocation(), deliveryProcess.getDelivery().getActionType()));
        }
        return listActionPoints;
    }

    public void drawAllActionPoints(final List<ActionPoint> actionPoints) {
        // First Action Point is the Base
        map.clearMarkers();
        map.addMarker(createMarker(actionPoints.get(0), MarkerType.BASE));

        //According to ActionType set the good MarkerType
        for (ActionPoint actionPoint : actionPoints) {
            if (actionPoint.getActionType() == ActionType.DELIVERY) {
                map.addMarker(createMarker(actionPoint, MarkerType.DELIVERY));
            } else if (actionPoint.getActionType() == ActionType.PICK_UP) {
                map.addMarker(createMarker(actionPoint, MarkerType.PICKUP));
            }
        }
    }

    public Marker createMarker(final ActionPoint actionPoints, final MarkerType mType) {
        //String path= MarkerImageFactory.createMarkerImage("/Users/martingermain/Public/Git/pld_agile/code/src/main/resources/view/icons/marker.png", "png");
        //path = path.replace("(", "");
        //path = path.replace(")", "");

        MarkerOptions markerPoint = new MarkerOptions();
        markerPoint.title(mType.title)
                .label(mType.firstLetter)
                .position(new LatLong(actionPoints.getLocation().getLatitude(), actionPoints.getLocation().getLongitude()));
        Marker pointMarker = new Marker(markerPoint);
        return pointMarker;
    }

    public void drawFullTour() {
        map.clearMarkers();
        drawPolyline(getMCVPathFormTour(),"blue");
        drawAllActionPoints(tourLoaded.getActionPoints());
    }

    public void drawDeliveryProcess(int id, Boolean clearMarkers, Boolean clearPolylines) {
        // Clear all markers
        if(clearMarkers) map.clearMarkers();
        //TODO Clear POLYLINES ?
        Journey journey = tourLoaded.getJourneyList().get(id);
        DeliveryProcess deliveryProcess = tourLoaded.getDeliveryProcesses().get(id);

        //TODO ADD POLYLINE

        map.addMarker(createMarker(deliveryProcess.getPickUP(),MarkerType.PICKUP));
        map.addMarker(createMarker(deliveryProcess.getDelivery(),MarkerType.DELIVERY));
    }

    public void drawPolyline(final MVCArray mvcArray, String color) {
        PolylineOptions polyOpts = new PolylineOptions()
                .path(mvcArray)
                .strokeColor(color)
                .clickable(false)
                .strokeOpacity(0.4)
                .strokeWeight(4);
        Polyline poly = new Polyline(polyOpts);
        map.addMapShape(poly);
    }

    public MVCArray getMCVPathFormJourney(final int id) {
        Journey journey = tourLoaded.getJourneyList().get(id);
        // Reverse List
        LinkedList<Point> newPointsList = new LinkedList<Point>();
        for(Point point: journey.getPoints()) {
            newPointsList.addFirst(point);
        }

        LatLong[] ary = new LatLong[newPointsList.size()];
        int i = 0;
        for(Point point: newPointsList) {
            LatLong latLong = new LatLong(point.getLatitude(), point.getLongitude());
            ary[i++] = latLong;
        }
        MVCArray mvc = new MVCArray(ary);
        return mvc;
    }

    public MVCArray getMCVPathFormTour() {

        int count = 0;
        LinkedList<Point> fullListOfPoints = new LinkedList<Point>();
        for( Journey journey: tourLoaded.getJourneyList()) {
            // Reverse List
            LinkedList<Point> newPointsList = new LinkedList<Point>();
            for(Point point: journey.getPoints()) {
                newPointsList.addFirst(point);
            }
            fullListOfPoints.addAll(newPointsList);
        }
        LatLong[] ary = new LatLong[fullListOfPoints.size()];
        int i = 0;
        for(Point point: fullListOfPoints) {
            LatLong latLong = new LatLong(point.getLatitude(), point.getLongitude());
            ary[i++] = latLong;
        }
        MVCArray mvc = new MVCArray(ary);
        return mvc;
    }

    public void setPickUpPoint(ActionEvent actionEvent) {
        map.addMouseEventHandler(UIEventType.click, (GMapMouseEvent event) -> {
            LatLong latLong = event.getLatLong();
            labelPickUp.setText(latLong.getLatitude() + ", " + latLong.getLongitude());
        });
    }

    public void setDeliveryPoint(ActionEvent actionEvent) {
        map.addMouseEventHandler(UIEventType.click, (GMapMouseEvent event) -> {
            LatLong latLong = event.getLatLong();
            labelDelivery.setText(latLong.getLatitude() + ", " + latLong.getLongitude());
        });
    }

    public void addNewDeliveryProcess(ActionEvent actionEvent) {
    }

    public void clearNewDeliveryProcess(ActionEvent actionEvent) {
    }
}
