package view;

import com.lynden.gmapsfx.GoogleMapView;
import com.lynden.gmapsfx.MapComponentInitializedListener;
import com.lynden.gmapsfx.javascript.event.GMapMouseEvent;
import com.lynden.gmapsfx.javascript.event.UIEventType;
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
import javafx.stage.FileChooser;
import model.data.*;
import org.apache.commons.lang.Validate;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Time;
import java.util.*;

public class DashBoardController implements Initializable,
        MapComponentInitializedListener {

    /**
     * Empty String.
     */
    private static final String EMPTY_STRING = "";

    private static final String ZERO_STRING = "0";

    /**
     * The style of our map.
     */
    private static String MAP_STYLE;

    static {
        try {
            MAP_STYLE = Files.readString(Path.of(DashBoardController.class.
                            getResource("map_style.txt").getPath()),
                    StandardCharsets.US_ASCII);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * The {@link Polyline} used to represent the calculated {@link Tour} on
     * the map.
     */
    private Polyline poly;

    // Reference to the main application
    private UserInterface mainApp;

    // List des ActionPoints en Observable pour la view
    private ObservableList<ActionPoint> actionPoints =
            FXCollections.observableArrayList();

    // Manage New DeliveryProcess
    private ActionPoint newPickUpActionPoint = null;

    private ActionPoint newDeliveryActionPoint = null;

    // Markers of new DeliveryProcess
    private Marker newPickUpPointMarker = null;

    private Marker newDeliveryPointMarker = null;

    private static double LYON_LATITUDE = 45.771606;

    private static double LYON_LONGITUDE = 4.880959;

    private static double ZOOM_SETTING = 13;

    @FXML
    private TableView<ActionPoint> actionPointTableView;

    @FXML
    private TableColumn<ActionPoint, String> deliveryRank;

    @FXML
    private TableColumn<ActionPoint, String> deliveryType;

    @FXML
    private TableColumn<ActionPoint, String> timeAtPoint;

    @FXML
    private Label labelPickUpCoordinates;

    @FXML
    private Label labelDeliveryCoordinates;

    @FXML
    public Label rectangle;

    @FXML
    private TextField inputDeliveryTimeH;

    @FXML
    private TextField inputDeliveryTimeM;

    @FXML
    private TextField inputPickUpTimeH;

    @FXML
    private TextField inputPickUpTimeM;

    @FXML
    private GoogleMapView mapView;

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

    private GoogleMap map;

    // Local Save of Tour.
    private Tour tourLoaded;

    private DeliveryProcess deliveryProcessLoaded;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        // Initialize the actionPoints table with the 3 columns.
        actionPointTableView.setItems(null);

        deliveryRank.setCellValueFactory(cellData ->
                new SimpleStringProperty(String.valueOf(cellData.
                        getValue().getId())));
        deliveryType.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getActionType().
                        toString()));

        timeAtPoint.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getPassageTime()));

        actionPointTableView.getSelectionModel().selectedItemProperty().
                addListener((observable, oldValue, newValue) ->
                        handelTableSelection(newValue));

        mapView.addMapInializedListener(this);
        mapView.setKey("AIzaSyDJDcPFKsYMTHWJUxVzoP0W7ERsx3Bhdgc");
    }

    @Override
    public void mapInitialized() {
        //Set the initial properties of the map.
        MapOptions mapOptions = new MapOptions();
        mapOptions.center(new LatLong(LYON_LATITUDE, LYON_LONGITUDE))
                .styleString(MAP_STYLE).overviewMapControl(false)
                .mapType(MapTypeIdEnum.ROADMAP).panControl(false)
                .rotateControl(false).scaleControl(false)
                .streetViewControl(false).zoomControl(false).zoom(ZOOM_SETTING);
    }

    /**
     * Resets the information to be displayed to zero. Should be called after
     * a new map was loaded.
     */
    void resetTour() {
        actionPointTableView.getItems().clear();
        startTime.setText(EMPTY_STRING);
        numberDeliveries.setText(EMPTY_STRING);
        arrivalTime.setText(EMPTY_STRING);
        clearRectangleColor();
    }

    /**
     * Sets the loadedTour, to be able to make correct assessments of the
     * users interaction with the gui.
     *
     * @param tour the {@link} tour to be set.
     */
    public void setTour(final Tour tour) {
        tourLoaded = tour;
    }

    /**
     * Asks the user if he wants to delete a selected DeliveryProcess. If
     * confirmed the {@link UserInterface} is invoked.
     */
    public void deleteDp() {
        if (showConfirmationAlert()) {
            this.mainApp.deleteDp(deliveryProcessLoaded);
        }
    }

    /**
     * Sets the actionPoints represented by the views. The table needs to be
     * cleaned before the new {@link ActionPoint}s can be displayed.
     *
     * @param tour the {@link} from which the {@link ActionPoint} should be
     *             displayed.
     */
    public void setActionPoints(final Tour tour) {
        actionPointTableView.getSelectionModel().clearSelection();
        actionPoints.remove(0, actionPoints.size());
        actionPoints.addAll(tour.getActionPoints());
    }

    /**
     * Interaction with the user when he desires to change the order of certain
     * ActionPoints. If the user chooses a valid number, the
     * {@link UserInterface} will be invoked.
     */
    public void modifyDeliveryOrder() {
        int result = showModifiedDeliveryDialog();
        int index = actionPointTableView.getSelectionModel().getFocusedIndex();
        if (result != -1) {
            List<ActionPoint> actionPoints = tourLoaded.getActionPoints();
            ActionPoint actionPoint = actionPoints.remove(index);
            actionPoints.add(result, actionPoint);
            this.mainApp.changeDeliveryOrder(actionPoints);
        }
    }

    /**
     * Sets the important labels on the top of the view.
     */
    private void setBigLabels() {
        numberDeliveries.setText(String.valueOf(tourLoaded
                .getDeliveryProcesses().size()));
        startTime.setText(tourLoaded.getStartTime().toString());

        // Checking whether the tour was calculated or not, if yes set the
        // corresponding labels if not set an empty string as a placeholder.
        if (tourLoaded.getCompleteTime() != null) {
            final List<Journey> journeys = tourLoaded.getJourneyList();
            final int journeysLength = journeys.size();
            arrivalTime.setText(journeys.get(journeysLength - 1).getFinishTime()
                    .toString());
        } else {
            arrivalTime.setText(EMPTY_STRING);
        }
    }

    /**
     * Clears the Information stored about the current delivery Process, to
     * reduce conflict with the future incoming information. Invokes the
     * {@link UserInterface} to calculate the tour.
     */
    public void calculateTour() {
        clearNewDeliveryProcess();
        this.mainApp.calculateTour();
    }

    /**
     * In order to display the ActionPoints of a Tour, before the latter was
     * optimized, we need to acces those. This is done by this methods. The Ac
     */
    void createFakeActionPointList() {
        List<ActionPoint> listActionPoints = new ArrayList<>();

        // Create a BASE and END actionPoint.
        ActionPoint base = new ActionPoint(tourLoaded.getStartTime(),
                tourLoaded.getBase(), ActionType.BASE);
        ActionPoint end = new ActionPoint(tourLoaded.getStartTime(),
                tourLoaded.getBase(), ActionType.END);

        listActionPoints.add(base);
        listActionPoints.add(end);

        for (DeliveryProcess deliveryProcess :
                tourLoaded.getDeliveryProcesses()) {

            final ActionPoint pickUp =
                    new ActionPoint(deliveryProcess.getPickUP().getTime(),
                            deliveryProcess.getPickUP().getLocation(),
                            deliveryProcess.getPickUP().getActionType());
            pickUp.setId(deliveryProcess.getPickUP().getId());
            listActionPoints.add(pickUp);

            final ActionPoint delivery =
                    new ActionPoint(deliveryProcess.getDelivery().getTime(),
                            deliveryProcess.getDelivery().getLocation(),
                            deliveryProcess.getDelivery().getActionType());
            delivery.setId(deliveryProcess.getDelivery().getId());
            listActionPoints.add(delivery);
        }
        tourLoaded.setActionPoints(listActionPoints);
    }

    /**
     * Invokes the {@link UserInterface} after a click on the undo button was
     * made.
     */
    public void undo() {
        this.mainApp.undo();
    }

    /**
     * Create a {@link Marker} which will have all the necessary information
     * for the user stored on itself. This is done to highlight the necessary
     * information when the user passes his mouse over the point.
     *
     * @param actionPoint the given {@link ActionPoint} to create a marker for.
     * @param mType       the {@link MarkerType}.
     * @return the created {@link Marker}
     */
    private Marker createMarker(final ActionPoint actionPoint,
                                final MarkerType mType) {
        String label = mType.firstLetter;
        if (actionPoint.getActionType() != ActionType.BASE &&
                actionPoint.getActionType() != ActionType.END) {
            label += actionPoint.getId();
        }
        ;
        MarkerOptions markerPoint = new MarkerOptions();
        markerPoint.title(mType.getTitle() + " - " + label + "\n\r" +
                "Passage Time: " + actionPoint.getPassageTime() + "\n" +
                "Time of Action: " + actionPoint.getTime() + "\n").label(label).
                position(new LatLong(actionPoint.getLocation().getLatitude(),
                        actionPoint.getLocation().getLongitude()));
        return new Marker(markerPoint);
    }

    /**
     * Checks whether the necessary information to add a {@link DeliveryProcess}
     * to the {@code loadedTour}. If it is the case the {@link UserInterface} is
     * invoked, if not an error message is displayed.
     */
    public void addNewDeliveryProcess() {
        if (canAddDeliveryProcess()) {
            if (newPickUpActionPoint != null
                    && newDeliveryActionPoint != null) {
                newPickUpActionPoint.setTime(Utils.parseStringToTime(
                        inputPickUpTimeH.getText(),
                        inputPickUpTimeM.getText()));
                newDeliveryActionPoint.setTime(Utils.parseStringToTime(
                        inputDeliveryTimeH.getText(),
                        inputDeliveryTimeM.getText()));
                this.mainApp.addDeliveryProcess(
                        newPickUpActionPoint, newDeliveryActionPoint);
            } else {
                showAlert("Action Impossible", "Error :",
                        "The Delivery " +
                                "Process is not created"
                );
            }
        } else {
            showAlert("Action Impossible", "Error :",
                    "All the fields to create a delivery process are "
                            + "not completes"
            );
        }
    }

    /**
     * Displays a selected {@link DeliveryProcess} to the User. First useful
     * information is set and displayed.
     *
     * @param deliveryProcess the {@link DeliveryProcess} to sho
     */
    void showDeliveryProcess(final DeliveryProcess deliveryProcess) {
        deliveryProcessLoaded = deliveryProcess;
        final String pickUpDuration =
                deliveryProcess.getPickUP().getTime().toString();
        final String deliveryDuration =
                deliveryProcess.getDelivery().getTime().toString();
        final String pickUpPointName =
                deliveryProcess.getPickUP().getLocation().getSegments().get(0)
                        .getName();
        final String deliveryPointName =
                deliveryProcess.getDelivery().getLocation().getSegments().get(0)
                        .getName();
        dPPuPoint.setText(pickUpPointName);
        dPDPoint.setText(deliveryPointName);
        dpDDuration.setText(deliveryDuration);
        dpPUDuration.setText(pickUpDuration);
        if (tourLoaded.getJourneyList() != null) {
            if (deliveryProcess.getPickUP().getActionType()
                    == ActionType.BASE) {
                dpDuration.setText(tourLoaded.getCompleteTime().toString());
                dPDistance.setText(String.valueOf(tourLoaded.getTotalDistance())
                        + " m");
                List<Journey> journeyList = new ArrayList<Journey>();
                journeyList.add(tourLoaded.getJourneyList().get(0));
                displayMap(getSelectedActionPoint().getLocation());
                drawAllActionPoints();
                drawFullTour();
                clearRectangleColor();
                drawPolyline(getMCVPathFormJourneyList(journeyList),
                        0.5, 2);
            } else {
                this.mainApp.getJourneyList(deliveryProcess);
                if (deliveryProcess.getTime() != null) {
                    dpDuration.setText(deliveryProcess.getTime().toString());
                }
                if (deliveryProcess.getDistance() != null) {
                    dPDistance.setText((deliveryProcess.getDistance()) + " m");
                }
            }
        }
    }

    /**
     * Handles the tasks after the user clicked on the button load map:
     * presents a file chooser to the user and hands accordingly.
     */
    public void handleLoadMap() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select a Map XML");
        fileChooser.getExtensionFilters().addAll(new FileChooser.
                ExtensionFilter("XML", "*.xml"));
        final File selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null) {
            if (selectedFile.getName().contains("xml")) {
                this.mainApp.loadMap(selectedFile);
            } else {
                showAlert("File Selection", "Error", "Error" +
                        " Loading not xmFile");
            }
        }
    }

    /**
     * Handles the tasks after the user clicked on the button load tour:
     * presents a file chooser to the user and hands accordingly.
     */
    public void handleLoadTour() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select a Tour XML");
        fileChooser.getExtensionFilters().addAll(new FileChooser.
                ExtensionFilter("XML", "*.xml"));
        File selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null) {
            if (selectedFile.getName().contains("xml")) {
                this.mainApp.loadDeliveryRequest(selectedFile);
            } else {
                showAlert("File Selection", "Error", "Error" +
                        " Loading not xmFile");
            }
        }
    }

    /**
     * Handles the ActionPoint Selection on the tableView. The goal is to
     * display information about the corresponding {@link DeliveryProcess}.
     *
     * @param newValue the selected {@link ActionPoint}
     */
    private void handelTableSelection(final ActionPoint newValue) {
        Utils.pointToColour(newValue);
        if (newValue != null && newValue.getActionType() == ActionType.END &&
                tourLoaded.getJourneyList() != null) {
            // Manage the end of the tour.
            dpDuration.setText(tourLoaded.getCompleteTime().toString());
            dPDistance.setText(String.valueOf(tourLoaded.getTotalDistance())
                    + " m");
            List<Journey> journeyList = new ArrayList<Journey>();
            journeyList.add(tourLoaded.getJourneyList().get(tourLoaded.
                    getJourneyList().size() - 1));
            displayMap(newValue.getLocation());
            drawAllActionPoints();
            drawFullTour();
            drawPolyline(getMCVPathFormJourneyList(journeyList), 0.5,
                    3);
        } else {
            mainApp.getDeliveryProcessFromActionPoint(newValue, tourLoaded);
        }
    }

    /**
     * Displays the map by setting the correct Marker options.
     *
     * @param center the wished center of the map
     */
    void displayMap(final Point center) {
        try {
            Thread.sleep(60);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //  Set new center for the map
        MapOptions mapOptions = new MapOptions();
        mapOptions.center(new LatLong(center.getLatitude(),
                center.getLongitude())).styleString(MAP_STYLE).
                overviewMapControl(false).mapType(MapTypeIdEnum.ROADMAP).
                panControl(false).rotateControl(false).scaleControl(false).
                streetViewControl(false).zoomControl(false).zoom(12);

        // Add map to the view
        map = mapView.createMap(mapOptions);
    }

    /**
     * Displays the {@link DeliveryProcess}s of a given tour.
     */
    void displayTourWhenNotCalculated() {
        map.setCenter(new LatLong(tourLoaded.getBase().getLatitude(),
                tourLoaded.getBase().getLongitude()));
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

    /**
     * Draws  a complete tour by clearing markers, drawing {@link ActionPoint}
     * and the necessary Polyline (connection the different Points).
     */
    void drawFullTour() {
        setBigLabels();
        map.clearMarkers();
        drawAllActionPoints();
        drawPolyline(getMCVPathFormJourneyList(tourLoaded.getJourneyList()),
                0.4, 1);
    }

    /**
     * Draws all the {@link ActionPoint} of the loaded {@link Tour} on the map.
     * Sets the Marker according to the type of the {@link ActionPoint} and
     * it's {@code id}.
     */
    void drawAllActionPoints() {

        // First Action Point is the Base
        map.clearMarkers();
        if (poly != null) {
            poly.setVisible(false);
            clearRectangleColor();
        }
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

    /**
     * Draws a PolyLine on the map, following the path that is displayed in
     * the {@link MVCArray}.
     *
     * @param mvcArray The path to follow to draw the line.
     * @param opacity  opacity of the line.
     * @param type     int parameter to determine the colour of the line.
     */
    void drawPolyline(final MVCArray mvcArray, final double opacity,
                      final int type) {

        String color = "blue";

        switch (type) {
            // 1 = Draw Full tour
            case 1:
                color = "blue";
                break;
            // 2 = Draw First journey
            case 2:
                color = "red";
                break;
            // 3 = Draw End journey
            case 3:
                color = "cyan";
                break;
            // 4 = Draw with Auto color
            case 4:
                if (deliveryProcessLoaded != null)
                    color = Utils.pointToColour(deliveryProcessLoaded.
                            getPickUP());
                break;
        }
        setRectangleColor(color);
        PolylineOptions polyOpts =
                new PolylineOptions().path(mvcArray).strokeColor(color).
                        clickable(false).strokeOpacity(opacity).
                        strokeWeight(4).visible(true);
        poly = new Polyline(polyOpts);
        poly.setVisible(true);
        map.addMapShape(poly);
    }

    /**
     * Displays an {@link ActionPoint} that was previosuly selected by the
     * user. It will also save the information about the coordinates of the
     * point, to later inform the model.
     *
     * @param actionPoint The actionPoint to save and draw.
     */
    void drawAndSaveNewActionPoint(final ActionPoint actionPoint) {
        //Clear past Markers
        map.clearMarkers();
        // Draw all tour Action point
        drawAllActionPoints();

        if (actionPoint.getActionType() == ActionType.PICK_UP) {
            newPickUpActionPoint = actionPoint;
            newPickUpPointMarker = createMarker(actionPoint, MarkerType.PICKUP);
            labelPickUpCoordinates.setText(Utils.pointToString(actionPoint.
                    getLocation()));
        }
        if (actionPoint.getActionType() == ActionType.DELIVERY) {
            newDeliveryActionPoint = actionPoint;
            newDeliveryPointMarker = createMarker(actionPoint,
                    MarkerType.DELIVERY);
            labelDeliveryCoordinates.setText(Utils.pointToString(actionPoint.
                    getLocation()));
        }

        // Eventually draw newPickUp and Delivery Point
        if (newPickUpPointMarker != null) {
            map.addMarker(newPickUpPointMarker);
        }
        if (newDeliveryPointMarker != null) {
            map.addMarker(newDeliveryPointMarker);
        }
    }

    /**
     * Sets the field that indicates the color of the shown delivery process
     * to the user.
     *
     * @param color the shown color.
     */
    private void setRectangleColor(final String color) {
        rectangle.setStyle("-fx-background-color:" + color + ";" + "-fx" +
                "-opacity: 0.5;");
    }

    /**
     * Sets the about the selected {@link DeliveryProcess} back to zero.
     */
    private void clearNewDeliveryProcess() {
        clearNewDeliveryPoint();
        clearNewPickUpPoint();
        inputDeliveryTimeM.setText(EMPTY_STRING);
        inputPickUpTimeM.setText(EMPTY_STRING);
        inputPickUpTimeH.setText(EMPTY_STRING);
        inputDeliveryTimeH.setText(EMPTY_STRING);
        newPickUpPointMarker = null;
        newDeliveryPointMarker = null;
        newPickUpActionPoint = null;
        newDeliveryActionPoint = null;
    }

    /**
     * Clears new pick up point field.
     */
    public void clearNewPickUpPoint() {
        labelPickUpCoordinates.setText(EMPTY_STRING);
    }

    /**
     * Clears new delivery point field.
     */
    public void clearNewDeliveryPoint() {
        labelDeliveryCoordinates.setText(EMPTY_STRING);
    }

    /**
     * Displays map freshly loaded, centered around the base of the loaded
     * tour. The fields necessary to it are cleared.
     */
    void clearAll() {
        displayMap(tourLoaded.getBase());
        clearRectangleColor();
        clearNewDeliveryProcess();
    }

    /**
     * Puts the color indicator back to it's background color, should be
     * called when no {@link DeliveryProcess} is selected.
     */
    private void clearRectangleColor() {
        rectangle.setStyle("-fx-background-color: #393e46;");
    }

    /**
     * Calculates an {@link MVCArray} from a list of journey. The goal is to
     * have a list of points that will be drawn on the map.
     *
     * @param journeyList the given list of journey.
     * @return the {@link MVCArray} corresponding to the list of journey
     */
    MVCArray getMCVPathFormJourneyList(final List<Journey> journeyList) {
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
            LatLong latLong = new LatLong(point.getLatitude(),
                    point.getLongitude());
            ary[i++] = latLong;
        }
        return new MVCArray(ary);
    }

    /**
     * Handles a MouseClick of a user to add a new delivery or pick up point.
     *
     * @param actionEvent event sent by clicking on th point.
     */
    public void handelMouseClickOnPoint(final ActionEvent actionEvent) {
        Button btn = (Button) actionEvent.getSource();
        String id = btn.getId();
        System.out.println(id);
        map.addMouseEventHandler(UIEventType.click, (GMapMouseEvent event) -> {
            LatLong latLong = event.getLatLong();
            if (id.contains("setPickUp") && editable(labelPickUpCoordinates)) {
                this.mainApp.getNearestPoint(latLong.getLatitude(),
                        latLong.getLongitude(), ActionType.PICK_UP,
                        new Time(0, 0, 0));
            }
            if (id.contains("setDelivery")
                    && editable(labelDeliveryCoordinates)) {
                this.mainApp.getNearestPoint(latLong.getLatitude(),
                        latLong.getLongitude(), ActionType.DELIVERY,
                        new Time(0, 0, 0));
            }
        });
    }

    /**
     * checks whether a label is empty or not
     *
     * @param label the label to check.
     * @return true if the label is
     */
    private Boolean editable(final Label label) {
        return label.getText().equals(EMPTY_STRING);
    }

    /**
     * Checks whether a delivery process can be added to the tour, i.e the
     * user has given us enough information.
     *
     * @return true iff the {@link DeliveryProcess} can be added to the list.
     */
    private Boolean canAddDeliveryProcess() {
        if (inputPickUpTimeH.getText().equals(EMPTY_STRING)) {
            inputPickUpTimeH.setText(ZERO_STRING);
        }
        if (inputDeliveryTimeH.getText().equals(EMPTY_STRING)) {
            inputDeliveryTimeH.setText(ZERO_STRING);
        }
        return !labelDeliveryCoordinates.getText().equals(EMPTY_STRING)
                && !labelDeliveryCoordinates.getText().equals(EMPTY_STRING)
                && !inputDeliveryTimeM.getText().equals(EMPTY_STRING)
                && !inputPickUpTimeM.getText().equals(EMPTY_STRING);
    }

    /**
     * Alert Message to interact with the user and draw his attention to an
     * important message.
     *
     * @param title  title of the message.
     * @param header header of the message.
     * @param msg    message itself.
     */
    void showAlert(final String title, final String header, final String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    /**
     * Shows the user a confiramtion alert to be able to make sure he wants
     * to delete the seleted {@link DeliveryProcess}.
     *
     * @return True if the user agrees to delete the {@link DeliveryProcess}
     * false otherwise.
     */
    private Boolean showConfirmationAlert() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Are you sur you want to delete this Delivery " +
                "Process ?");

        ButtonType buttonTypeOne = new ButtonType("Yes");
        ButtonType buttonTypeCancel = new ButtonType("Cancel",
                ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(buttonTypeOne, buttonTypeCancel);

        Optional<ButtonType> result = alert.showAndWait();
        return result.get() == buttonTypeOne;
    }

    /**
     * Show a dialog to be able to change the order of the
     *
     * @return the chosen index at which the selected {@link ActionPoint}
     * should be relocated.
     */
    private int showModifiedDeliveryDialog() {
        List<Integer> choices = new ArrayList<>();
        for (int i = 1; i < tourLoaded.getActionPoints().size() - 1; i++) {
            choices.add(i);
        }

        ChoiceDialog<Integer> dialog =
                new ChoiceDialog<Integer>(actionPointTableView
                        .getSelectionModel().getFocusedIndex(), choices);
        dialog.setTitle("Modify Order");
        dialog.setHeaderText("Change Order");
        dialog.setContentText("Choose the new N°:");

        Optional<Integer> result = dialog.showAndWait();
        return result.orElse(-1);

    }

    /**
     * Is called by the main application to give a reference back to itself.
     *
     * @param mainApp the mainApp.
     */
    public void setMainApp(final UserInterface mainApp) {
        Validate.notNull(mainApp);
        this.mainApp = mainApp;
    }

    /**
     * @return the selected actionPoint by the use
     */
    ActionPoint getSelectedActionPoint() {
        return actionPointTableView.getSelectionModel().getSelectedItem();
    }

}




