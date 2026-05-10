package edu.curtin.saed.assignment1;

import java.util.logging.Logger;
import java.util.*;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

/**
 * This is demonstration code intended for you to modify. Currently, it sets up a rudimentary
 * JavaFX GUI with the basic elements required for the assignment.
 *
 * (There is an equivalent Swing version of this, which you can use if you have trouble getting
 * JavaFX as a whole to work.)
 *
 * You will need to use the GridArea object, and create various GridAreaIcon objects, to represent
 * the on-screen map.
 *
 * Use the startBtn, endBtn, statusText and textArea objects for the other input/output required by
 * the assignment specification.
 *
 * Break this up into multiple methods and/or classes if it seems appropriate. Promote some of the
 * local variables to fields if needed.
 */
public class App extends Application
{
    private volatile boolean simEnded = false; // To help control grid area refreshing
    private AirportController airportController;
    private PlaneController planeController;

    private TextArea textArea;
    private Label statusText;
    private GridArea area;

    private static final Logger logger = Logger.getLogger(App.class.getName());

    // For automatically refreshing the grid area (i.e. requesting layout)
    private AnimationTimer repaintTimer;
    private long last = 0;

    public static void main(String[] args)
    {
        launch();
    }

    @Override
    public void start(Stage stage)
    {
        area = new GridArea(12, 12);
        area.setStyle("-fx-background-color: #006000;");
        statusText = new Label("Label Text");
        textArea = new TextArea();
        
        var startBtn = new Button("Start");
        var endBtn = new Button("End");
        endBtn.setDisable(true);

        // START BUTTON
        startBtn.setOnAction((event) ->
        {
            System.out.println("Start button pressed");
            logger.info(() -> String.format("Start button pressed"));
            this.startRepainting(); // Start animation timer for repainting
            this.beginSim(); // Begin simulation (initialising controllers, starting threads)
            startBtn.setDisable(true);
            endBtn.setDisable(false);
        });

        // END BUTTON
        endBtn.setOnAction((event) ->
        {
            System.out.println("End button pressed");
            logger.info(() -> String.format("End button pressed"));
            endBtn.setDisable(true);
            this.simEnded = true; // So that area content is not refreshed
            this.endSim(); // End simulation (Interrupt threads and prevent further GUI updates)
        });

        // WINDOW EXIT
        stage.setOnCloseRequest((event) ->
        {
            System.out.println("Close button pressed");
            logger.info(() -> String.format("Close button pressed"));
            this.simEnded = true; // So that area content is not refreshed
            this.endSim(); // End simulation (Interrupt threads and prevent further GUI updates)
        });

        // Remove?
        textArea.appendText("Sidebar\n");
        textArea.appendText("Text\n");
        
        // Below is basically just the GUI "plumbing" (connecting things together).
        var toolbar = new ToolBar();
        toolbar.getItems().addAll(startBtn, endBtn, new Separator(), statusText);

        var splitPane = new SplitPane();
        splitPane.getItems().addAll(area, textArea);
        splitPane.setDividerPositions(0.75);

        stage.setTitle("Air Traffic Simulator");
        var contentPane = new BorderPane();
        contentPane.setTop(toolbar);
        contentPane.setCenter(splitPane);

        var scene = new Scene(contentPane, 1200, 1000);
        stage.setScene(scene);
        stage.show();
    }

    /* BEGIN SIMULATION
     * 
     * Create airports and planes
     * Add icons to grid area
     * Create controllers
     * Begin AirportController threads
     */
    private void beginSim() {
        /*
         * Repeating 10 times, create 10 planes and add them to a new airport
         * (Grid area icons created and added in the process)
         * 
         * Create PlaneController and AirportController
         * Start simulation by starting threads in AirportController
         */

        List<Plane> planes = new ArrayList<>(); // To hold all planes, not really needed anymore
        List<Airport> airports = new ArrayList<>(); // To hold all airports to give to AirportController
        List<List<Double>> airportPositions = SimUtils.generateAirportPositions(); // Get random airport positions
        
        // Keep track of amount of airports and planes created, so that IDs and names can be given
        int planesCreated = 0;
        int airportsCreated = 0;

        // For each airport (placeholder position), create 10 planes and add a corresponding to the grid area
        for (List<Double> position : airportPositions) {
            
            List<Plane> airportPlanes = new ArrayList<>(); // To hold the planes for the currently generating airport
            
            // Create 10 planes, with position at their beginning airport, adding them to the grid area
            for (int i = 1; i <= 10; i++) {
                planesCreated++;
                GridAreaIcon planeIcon = new GridAreaIcon(
                                        position.get(0),
                                        position.get(1),
                                        45.0,
                                        1.0,
                                        App.class.getClassLoader().getResourceAsStream("plane.png"),
                                        "Plane " + planesCreated);
                planeIcon.setShown(false);
                Plane plane = new Plane(planesCreated, planeIcon);
                planes.add(plane); // To add to plane controller
                airportPlanes.add(plane); // To add to airport
                area.getIcons().add(planeIcon);
            }

            // Create the airport, passing in its starting planes and initialising it's grid area icon
            GridAreaIcon airportIcon = new GridAreaIcon(
                                    position.get(0),
                                    position.get(1),
                                    0.0,
                                    1.0,
                                    App.class.getClassLoader().getResourceAsStream("airport.png"),
                                    "Airport " + (airportsCreated + 1));
            area.getIcons().add(airportIcon);
            Airport airport = new Airport(airportsCreated, airportIcon, airportPlanes);
            airports.add(airport);
            airportsCreated++;
        }

        // Initialise the controllers
        // Make the AirportController start threads to generate and handle flight requests
        planeController = new PlaneController(statusText, textArea);
        airportController = new AirportController(airports, planeController);
        airportController.beginSim();
    }

    /* END SIMULATION
     * 
     * End all processes
     */
    private void endSim() {
        // Set PlaneController 'simEnded' variable to 'true', to not update GUI text areas anymore
        planeController.endSim();

        // Interrupt all threads in both controllers
        airportController.interruptThreads();
        planeController.interruptThreads();

        // Stop repainting (requesting area layout) the GUI area
        repaintTimer.stop();

        // Join all threads in both controllers
        airportController.joinThreads();
        planeController.joinThreads();
    }

    /* START REFRESHING SIMULATION AREA
     * 
     * Starts to automatically refresh the grid area every 90ms
     * (90ms to somewhat ensure the minimum of 10 refreshes per second requirement)
     * 
     * Stops if 'simEnded' is 'true'
     */
    private void startRepainting() {
        repaintTimer = new AnimationTimer() {
            @Override public void handle(long now) {
                if (simEnded) {
                    return;
                }
                if (now - last >= 90_000_000L) {
                    area.requestLayout();
                    last = now;
                }
            }
        };
        repaintTimer.start();
    }
}