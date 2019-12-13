package view;

import com.lynden.gmapsfx.GoogleMapView;
import com.lynden.gmapsfx.MapComponentInitializedListener;
import com.lynden.gmapsfx.javascript.event.*;
import com.lynden.gmapsfx.javascript.object.*;
import com.lynden.gmapsfx.shapes.Polyline;
import com.lynden.gmapsfx.shapes.PolylineOptions;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import lombok.Getter;
import model.data.*;
import model.data.Point;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang.math.NumberUtils;

import java.awt.*;
import java.io.File;
import java.net.URL;
import java.sql.Time;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;

public class DashBoardController implements Initializable, MapComponentInitializedListener {

    // Map Style.
    private static final String mapStyle = "[{\"featureType\":\"administrative\",\"elementType\":\"all\",\"stylers\":[{\"visibility\":\"off\"}]},{\"featureType\":\"landscape.man_made\",\"elementType\":\"geometry.fill\",\"stylers\":[{\"color\":\"#e9e5dc\"}]},{\"featureType\":\"landscape.natural\",\"elementType\":\"geometry.fill\",\"stylers\":[{\"visibility\":\"on\"},{\"color\":\"#b8cb93\"}]},{\"featureType\":\"poi\",\"elementType\":\"all\",\"stylers\":[{\"visibility\":\"off\"}]},{\"featureType\":\"poi.attraction\",\"elementType\":\"all\",\"stylers\":[{\"visibility\":\"off\"}]},{\"featureType\":\"poi.business\",\"elementType\":\"all\",\"stylers\":[{\"visibility\":\"off\"}]},{\"featureType\":\"poi.government\",\"elementType\":\"all\",\"stylers\":[{\"visibility\":\"off\"}]},{\"featureType\":\"poi.medical\",\"elementType\":\"all\",\"stylers\":[{\"visibility\":\"off\"}]},{\"featureType\":\"poi.park\",\"elementType\":\"all\",\"stylers\":[{\"visibility\":\"off\"}]},{\"featureType\":\"poi.park\",\"elementType\":\"geometry.fill\",\"stylers\":[{\"color\":\"#ccdca1\"}]},{\"featureType\":\"poi.place_of_worship\",\"elementType\":\"all\",\"stylers\":[{\"visibility\":\"off\"}]},{\"featureType\":\"poi.school\",\"elementType\":\"all\",\"stylers\":[{\"visibility\":\"off\"}]},{\"featureType\":\"poi.sports_complex\",\"elementType\":\"all\",\"stylers\":[{\"visibility\":\"off\"}]},{\"featureType\":\"road\",\"elementType\":\"geometry.fill\",\"stylers\":[{\"hue\":\"#ff0000\"},{\"saturation\":-100},{\"lightness\":99}]},{\"featureType\":\"road\",\"elementType\":\"geometry.stroke\",\"stylers\":[{\"color\":\"#808080\"},{\"lightness\":54},{\"visibility\":\"off\"}]},{\"featureType\":\"road\",\"elementType\":\"labels.text.fill\",\"stylers\":[{\"color\":\"#767676\"}]},{\"featureType\":\"road\",\"elementType\":\"labels.text.stroke\",\"stylers\":[{\"color\":\"#ffffff\"}]},{\"featureType\":\"transit\",\"elementType\":\"all\",\"stylers\":[{\"visibility\":\"off\"}]},{\"featureType\":\"water\",\"elementType\":\"all\",\"stylers\":[{\"saturation\":43},{\"lightness\":-11},{\"color\":\"#89cada\"}]}]";

    // Local Save of Tour.
    private Tour tourLoaded;
    private DeliveryProcess deliveryProcessLoaded;

    // Manage Local Storage.
    public void setTour(Tour tour) {
        tourLoaded = tour;

    }

    public void deleteDp() {
        if (showConfirmationAlert("Are you sur you want to delete this Delivery Process ?")) {
            this.mainApp.deleteDp(deliveryProcessLoaded);
        }
    }

    public void setActionPoints(final Tour tour) {
        actionPointTableView.getSelectionModel().clearSelection();
        actionPoints.remove(0, actionPoints.size());
        actionPoints.addAll(tour.getActionPoints());
    }

    public void modifieDP() {
        int result = showModifieDeliveryDialog(deliveryProcessLoaded);
        int index = actionPointTableView.getSelectionModel().getFocusedIndex();
        if (result != -1) {
            List<ActionPoint> actionPoints = tourLoaded.getActionPoints();
            ActionPoint actionPoint = actionPoints.remove(index);
            actionPoints.add(result, actionPoint);
            this.mainApp.modifyOrder(actionPoints);
        }
    }

    //Enum Marker Types.
    @Getter
    public enum MarkerType {
        PICKUP("Pick-Up Point", "P", "icons/marker.png"),
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

    // Manage New DeliveryProcess
    private ActionPoint newPickUpActionPoint = null;
    private ActionPoint newDeliveryActionPoint = null;
    // Markers of new DeliveryProcess
    private Marker newPickUpPointMarker = null;
    private Marker newDeliveryPointMarker = null;

    @FXML
    private TableView<ActionPoint> actionPointTableView;

    @FXML
    private TableColumn<ActionPoint, String> deliveryRank;

    @FXML
    private TableColumn<ActionPoint, String> deliveryType;

    @FXML
    private TableColumn<ActionPoint, String> timeAtPoint;

    @FXML
    private Label labelPickUpCoordonates;

    @FXML
    private Label labelDeliveryCoordonates;

    @FXML
    private TextField inputDeliveryTimeH;
    @FXML
    private TextField inputDeliveryTimeM;
    @FXML
    private TextField inputPickUpTimeH;
    @FXML
    private TextField inputPickUpTimeM;

    @FXML
    private Label dpNumber;

    @FXML
    private Label dpDuration;

    @FXML
    private Label dPDistance;

    @FXML
    private Label dPPuPoint;

    @FXML
    private Label dPDPoint;

    @FXML
    private Label dpPUDuration;

    @FXML
    private Label dpDDuration;

    @FXML
    private Label arrivalTime;

    @FXML
    private Label numberDeliveries;

    @FXML
    private Label startTime;

    private void setBigLabels() {
        numberDeliveries.setText(String.valueOf(tourLoaded.getDeliveryProcesses().size()));
        startTime.setText(tourLoaded.getStartTime().toString());
        if (tourLoaded.getCompleteTime() != null) {
            final List<Journey> journeys = tourLoaded.getJourneyList();
            final int journeysLength = journeys.size();
            arrivalTime.setText(journeys.get(journeysLength - 1).getFinishTime().toString());
        }
    }


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
                (String.valueOf(cellData.getValue().getId())));
        deliveryType.setCellValueFactory(
                cellData -> new SimpleStringProperty(cellData.getValue().getActionType().toString()));

        timeAtPoint.setCellValueFactory(
                cellData -> new SimpleStringProperty(cellData.getValue().getPassageTime()));

        actionPointTableView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> handelTableSelection(newValue));

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
        clearNewDeliveryProcess();
        this.mainApp.calculateTour();
    }

    // Create / Update

    public void createFakeActionPointList() {
        List<ActionPoint> listActionPoints = new ArrayList<>();
        // Create a base actionPoint.
        ActionPoint base = new ActionPoint(tourLoaded.getStartTime(), tourLoaded.getBase(), ActionType.BASE);
        ActionPoint end = new ActionPoint(tourLoaded.getStartTime(), tourLoaded.getBase(), ActionType.END);

        listActionPoints.add(base);
        listActionPoints.add(end);
        for (DeliveryProcess deliveryProcess : tourLoaded.getDeliveryProcesses()) {
            final ActionPoint pickUp = new ActionPoint(deliveryProcess.getPickUP()
                    .getTime(), deliveryProcess.getPickUP().getLocation(),
                    deliveryProcess.getPickUP().getActionType());
            pickUp.setId(deliveryProcess.getPickUP().getId());
            listActionPoints.add(pickUp);
            final ActionPoint delivery = new ActionPoint
                    (deliveryProcess.getDelivery().getTime(),
                            deliveryProcess.getDelivery().getLocation(),
                            deliveryProcess.getDelivery().getActionType());
            delivery.setId(deliveryProcess.getDelivery().getId());
            listActionPoints.add(delivery);
        }
        tourLoaded.setActionPoints(listActionPoints);
    }

    public Marker createMarker(final ActionPoint actionPoint, final MarkerType mType) {
        String label = mType.firstLetter;
        if(actionPoint.getActionType() != ActionType.BASE && actionPoint.getActionType() != ActionType.END) {
            label += actionPoint.getId();
        };
        MarkerOptions markerPoint = new MarkerOptions();
        markerPoint.title(mType.getTitle() + " - " + label + "\n\r" +
                    "Passage Time: " + actionPoint.getPassageTime() + "\n" +
                    "Time of Action: " + actionPoint.getTime() + "\n"
                )
                .label(label)
                .position(new LatLong(actionPoint.getLocation().getLatitude(), actionPoint.getLocation().getLongitude()));
        Marker pointMarker = new Marker(markerPoint);
        return pointMarker;
    }

    public void addNewDeliveryProcess() {
        if (canAddDeliveryProcess()) {
            if (newPickUpActionPoint != null && newDeliveryActionPoint != null) {
                newPickUpActionPoint.setTime(parseStringToTime(inputPickUpTimeH.getText(), inputPickUpTimeM.getText()));
                newDeliveryActionPoint.setTime(parseStringToTime(inputDeliveryTimeH.getText(), inputDeliveryTimeM.getText()));
                this.mainApp.addDeliveryProcess(tourLoaded, newPickUpActionPoint, newDeliveryActionPoint);
            } else {
                showAlert("Action Impossible", "Error :", "The Delivery Process is not created", Alert.AlertType.ERROR);
            }
        } else {
            showAlert("Action Impossible", "Error :", "All the fields to create a delivery process are not completes", Alert.AlertType.ERROR);
        }
    }

    private Time parseStringToTime(final String hours, final String minutes) {
        Validate.notNull(hours, "hours null");
        Validate.notNull(minutes, "minutes null");
        Validate.isTrue(NumberUtils.isNumber(hours), "hours not a number");
        Validate.isTrue(NumberUtils.isNumber(minutes), "minutes not a number");
        Validate.isTrue(Integer.parseInt(hours) < 24 && Integer.parseInt(hours) >= 0, "not an hour");
        Validate.isTrue(Integer.parseInt(minutes) < 59 && Integer.parseInt(minutes) >= 0, "not a minute");
        final String toParse = hours + ":" + minutes + ":00";
        return Time.valueOf(toParse);
    }
    // Update view

    public void showDeliveryProcess(final DeliveryProcess deliveryProcess) {
        deliveryProcessLoaded = deliveryProcess;
        final String pickUpDuration = deliveryProcess.getPickUP().getTime().toString();
        final String deliveryDuration = deliveryProcess.getDelivery().getTime().toString();
        final String pickUpPointName = deliveryProcess.getPickUP().getLocation().getSegments().get(0).getName();
        final String deliveryPointName = deliveryProcess.getDelivery().getLocation().getSegments().get(0).getName();
        dPPuPoint.setText(pickUpPointName);
        dPDPoint.setText(deliveryPointName);
        dpDDuration.setText(deliveryDuration);
        dpPUDuration.setText(pickUpDuration);
        // actionPointTableView.setItems(null);
        if (tourLoaded.getJourneyList() != null) {
            if (deliveryProcess.getPickUP().getActionType() == ActionType.BASE) {
                dpDuration.setText(tourLoaded.getCompleteTime().toString());
                dPDistance.setText(String.valueOf(tourLoaded.getTotalDistance()) + " m");
                List<Journey> journeyList = new ArrayList<Journey>();
                journeyList.add(tourLoaded.getJourneyList().get(0));
                displayMap(getSelectedActionPoint().getLocation());
                drawAllActionPoints();
                drawFullTour();
                drawPolyline(getMCVPathFormJourneyListe(journeyList), 0.5, 2);
            } else {
                this.mainApp.getJourneyList(tourLoaded.getJourneyList(), deliveryProcess);
                if (deliveryProcess.getTime() != null) {
                    dpDuration.setText(deliveryProcess.getTime().toString());
                }
                if (deliveryProcess.getDistance() != null) {
                    dPDistance.setText(String.valueOf(deliveryProcess.getDistance()) + " m");
                }
            }
        }
    }

    // Display on Map

    public void displayMap(Point center) {
        //  Set new center for the map
        MapOptions mapOptions = new MapOptions();
        mapOptions.center(new LatLong(center.getLatitude(),center.getLongitude()))
                .styleString(mapStyle)
                .overviewMapControl(false)
                .mapType(MapTypeIdEnum.ROADMAP)
                .panControl(false)
                .rotateControl(false)
                .scaleControl(false)
                .streetViewControl(false)
                .zoomControl(false)
                .zoom(13);
        // Add map to the view
        map = mapView.createMap(mapOptions);
    }

    public void displayLoadedDeliveryProcess() {
        setBigLabels();

        // Create a fake list of action Points To display.
        createFakeActionPointList();
        actionPoints.remove(0, actionPoints.size());
        actionPoints.addAll(tourLoaded.getActionPoints());

        System.out.println(actionPoints.size() + " action point size");
        // Add observable list data to the table
        actionPointTableView.setItems(actionPoints);
        drawAllActionPoints();
    }

    // Draw

    public void drawFullTour() {
        setBigLabels();
        map.clearMarkers();
        drawPolyline(getMCVPathFormJourneyListe(tourLoaded.getJourneyList()),0.4,1);
        drawAllActionPoints();
    }


    public void drawAllActionPoints() {

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

    public void drawPolyline(final MVCArray mvcArray, double opacity, int type) {

        String color = "blue";

        switch (type) {
            // 1 = Draw Full tour
            case 1 : color = "blue";
                    break;
            // 2 = Draw First journey
            case 2 : color = "red";
                     break;
            // 3 = Draw End journey
            case 3 : color = "black";
                    break;
            // 4 = Draw with Auto color
            case 4 : if(deliveryProcessLoaded != null) color = pointToColour(deliveryProcessLoaded.getPickUP());
                    break;

        }

        PolylineOptions polyOpts = new PolylineOptions()
                .path(mvcArray)
                .strokeColor(color)
                .clickable(false)
                .strokeOpacity(opacity)
                .strokeWeight(4);
        Polyline poly = new Polyline(polyOpts);
        map.addMapShape(poly);
    }

    public void drawAndSaveNewActionPoint(ActionPoint actionPoint) {
        //Clear past Markers
        map.clearMarkers();
        // Draw all tour Action point
        drawAllActionPoints();

        if (actionPoint.getActionType() == ActionType.PICK_UP) {
            newPickUpActionPoint = actionPoint;
            newPickUpPointMarker = createMarker(actionPoint, MarkerType.PICKUP);
            labelPickUpCoordonates.setText(stringFormater(actionPoint.getLocation()));
        }
        if (actionPoint.getActionType() == ActionType.DELIVERY) {
            newDeliveryActionPoint = actionPoint;
            newDeliveryPointMarker = createMarker(actionPoint, MarkerType.DELIVERY);
            labelDeliveryCoordonates.setText(stringFormater(actionPoint.getLocation()));
        }

        // Eventually draw newPickUp and Delivery Point
        if (newPickUpPointMarker != null) map.addMarker(newPickUpPointMarker);
        if (newDeliveryPointMarker != null)
            map.addMarker(newDeliveryPointMarker);
    }

    // Clear / Reset.

    public void clearNewDeliveryProcess() {
        clearNewDeliveryPoint();
        clearNewPickUpPoint();
        inputDeliveryTimeM.setText("");
        inputPickUpTimeM.setText("");
        inputPickUpTimeH.setText("");
        inputDeliveryTimeH.setText("");
        newPickUpPointMarker = null;
        newDeliveryPointMarker = null;
        newPickUpActionPoint = null;
        newDeliveryActionPoint = null;
    }

    public void clearNewPickUpPoint() {
        labelPickUpCoordonates.setText("");
    }

    public void clearNewDeliveryPoint() {
        labelDeliveryCoordonates.setText("");
    }

    public void clearAll() {
        displayMap(tourLoaded.getBase());
        clearNewDeliveryProcess();
    }
    // Utils

    public MVCArray getMCVPathFormJourneyListe(final List<Journey> journeyList) {
        int count = 0;
        LinkedList<Point> fullListOfPoints = new LinkedList<Point>();
        for (Journey journey : journeyList) {
            // Reverse List
            LinkedList<Point> newPointsList = new LinkedList<Point>();
            for (Point point : journey.getPoints()) {
                newPointsList.addFirst(point);
            }
            fullListOfPoints.addAll(newPointsList);
        }
        LatLong[] ary = new LatLong[fullListOfPoints.size()];
        int i = 0;
        for (Point point : fullListOfPoints) {
            LatLong latLong = new LatLong(point.getLatitude(), point.getLongitude());
            ary[i++] = latLong;
        }
        MVCArray mvc = new MVCArray(ary);
        return mvc;
    }

    public void handelMouseClickOnPoint(ActionEvent actionEvent) {
        Button btn = (Button) actionEvent.getSource();
        String id = btn.getId();
        System.out.println(id);
        map.addMouseEventHandler(UIEventType.click, (GMapMouseEvent event) -> {
            LatLong latLong = event.getLatLong();
            if (id.contains("setPickUp") && editable(labelPickUpCoordonates)) {
                System.out.println("this is a test");
                this.mainApp.getNearPoint(latLong.getLatitude(), latLong.getLongitude(), ActionType.PICK_UP, new Time(0, 0, 0));
            }
            if (id.contains("setDelivery") && editable(labelDeliveryCoordonates)) {
                this.mainApp.getNearPoint(latLong.getLatitude(), latLong.getLongitude(), ActionType.DELIVERY, new Time(0, 0, 0));
            }
        });
    }

    public String stringFormater(final Point point) {
        if (point != null) {
            DecimalFormat numberFormat = new DecimalFormat("#.0000");
            return numberFormat.format(point.getLatitude()) + ", " + numberFormat.format(point.getLongitude());
        } else {
            return "";
        }
    }

    public Boolean editable(Label label) {
        return label.getText() == "";
    }

    public Boolean canAddDeliveryProcess() {
        if (inputPickUpTimeH.getText().equals("")) {
            inputPickUpTimeH.setText("0");
        }
        if (inputDeliveryTimeH.getText().equals("")) {
            inputDeliveryTimeH.setText("0");
        }
        return labelDeliveryCoordonates.getText() != ""
                && labelDeliveryCoordonates.getText() != ""
                && inputDeliveryTimeM.getText() != ""
                && inputPickUpTimeM.getText() != "";
    }

    // Utils Pop Up

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

    private void handelTableSelection(ActionPoint newValue) {
        pointToColour(newValue);
        if (newValue != null && newValue.getActionType() == ActionType.END && tourLoaded.getJourneyList() != null) {
            // Manage the end of the tour.
            dpDuration.setText(tourLoaded.getCompleteTime().toString());
            dPDistance.setText(String.valueOf(tourLoaded.getTotalDistance()) + " m");
            List<Journey> journeyList = new ArrayList<Journey>();
            journeyList.add(tourLoaded.getJourneyList().get(tourLoaded.getJourneyList().size() - 1));
            displayMap(newValue.getLocation());
            drawAllActionPoints();
            drawFullTour();
            drawPolyline(getMCVPathFormJourneyListe(journeyList), 0.5, 3);
        } else {
            mainApp.showDeliveryProcess(newValue, tourLoaded);
        }
    }

    public void showAlert(String title, String header, String msg, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private Boolean showConfirmationAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText(msg);

        ButtonType buttonTypeOne = new ButtonType("Yes");
        ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(buttonTypeOne, buttonTypeCancel);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == buttonTypeOne) {
            return true;
        } else {
            return false;
        }
    }

    private int showModifieDeliveryDialog(final DeliveryProcess deliveryProcess) {
        List<Integer> choices = new ArrayList<Integer>();
        for (int i = 1; i < tourLoaded.getActionPoints().size() - 1; i++) {
            choices.add(i);
        }

        ChoiceDialog<Integer> dialog = new ChoiceDialog<Integer>(actionPointTableView.getSelectionModel().getFocusedIndex(), choices);
        dialog.setTitle("Modify Order");
        dialog.setHeaderText("Change Order");
        dialog.setContentText("Choose the new N°:");

        Optional<Integer> result = dialog.showAndWait();
        if (result.isPresent()) {
            return result.get();
        } else {
            return -1;
        }

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

    public String pointToColour(ActionPoint actionPoint) {
        int numberFromId = (int)(actionPoint.getId() * 6.8 * 10000 * Math.pow(2,actionPoint.getId()));
        Color color = new Color(numberFromId).brighter();
        return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }

    public ActionPoint getSelectedActionPoint() {
        return actionPointTableView.getSelectionModel().getSelectedItem();
    }

}
