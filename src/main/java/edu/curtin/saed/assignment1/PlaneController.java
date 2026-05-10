package edu.curtin.saed.assignment1;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

public class PlaneController {

    private Label statusText;
    private TextArea textArea;
    private volatile int planesInFlight = 0;
    private volatile int planesBeingServiced = 0;
    private volatile int completedPlaneTrips = 0;

    private final ExecutorService executor; // Thread pool

    // To check before updating 'statusText' and 'TextArea'.
    private volatile boolean simEnded = false;

    private static final Logger logger = Logger.getLogger(AirportController.class.getName());

    public PlaneController(Label statusText, TextArea textArea) {
        this.statusText = statusText;
        this.textArea = textArea;
        this.executor = Executors.newFixedThreadPool(100);
    }

    // Called by AirportController to process a flight request
    // Starts a new thread to handle the request (movement and servicing of the plane)
    public void processRequest(Plane plane, Airport currAirport, Airport destAirport) {
        executor.execute(() -> 
        {
            doFlight(plane, currAirport, destAirport);
        });
    }

    /* MOVE AND SERVICE A PLANE
     * 
     * Moves the plane from current airport to destination airport
     * Services the plane
     * 
     * Update GUI text areas throughout when the plane takes-off, lands and finishes servicing
     */
    private void doFlight(Plane plane, Airport currAirport, Airport destAirport) {
        try
        {
            if(Thread.currentThread().isInterrupted()) { throw new InterruptedException(); }

            // Calculate and retrieve plane positions
            // List of positions the plane should be in every 50ms
            List<List<Double>> flightPositions = SimUtils.getFlightPositions(currAirport, destAirport);

            if(Thread.currentThread().isInterrupted()) { throw new InterruptedException(); }
            // Make the plane visible (i.e. in the air)
            plane.setShown(true);

            // Update GUI text areas for status and plane take-off
            Platform.runLater(() ->
            {
                if (simEnded == false) {
                    planesInFlight++;
                    String text = "Planes in flight: " + planesInFlight
                                + "\nPlanes being serviced: " + planesBeingServiced
                                + "\nCompleted flights: " + completedPlaneTrips;
                    statusText.setText(text);
                    textArea.appendText("Flight start: Plane " + plane.getId() 
                                        + " from Airport " + currAirport.getId() 
                                        + " to Airport " + destAirport.getId() + ".\n");
                }
            });

            // For every position in positions list, every 50 ms, update plane position
            for (List<Double> position : flightPositions) {
                if(Thread.currentThread().isInterrupted()) { throw new InterruptedException(); }
                Thread.sleep(50);

                // Note: Assumes first position in list is not the starting / current airport position
                plane.setPos(position.get(0), position.get(1));
            }

            if(Thread.currentThread().isInterrupted()) { throw new InterruptedException(); }
            // Make invisible (i.e. landed)
            plane.setShown(false);

            // Add plane to destAirport, update plane's currAirport
            destAirport.addPlane(plane);
            plane.setCurrAirport(destAirport);

            // Update GUI text areas for status and plane landing
            Platform.runLater(() ->
            {
                if (simEnded == false) {
                    planesInFlight--;
                    planesBeingServiced++;
                    completedPlaneTrips++;
                    String text = "Planes in flight: " + planesInFlight
                                + "\nPlanes being serviced: " + planesBeingServiced
                                + "\nCompleted flights: " + completedPlaneTrips;
                    statusText.setText(text);
                    textArea.appendText("Flight end: Plane " + plane.getId() 
                                        + " from Airport " + currAirport.getId() 
                                        + " to Airport " + destAirport.getId() + ".\n");
                }
            });

            if(Thread.currentThread().isInterrupted()) { throw new InterruptedException(); }
            // Service the plane
            StandardPlaneServicing.service(plane.getId(), destAirport.getId());

            // Update GUI text area for status
            Platform.runLater(() ->
            {
                if (simEnded == false) {
                    planesBeingServiced--;
                    String text = "Planes in flight: " + planesInFlight
                                + "\nPlanes being serviced: " + planesBeingServiced
                                + "\nCompleted flights: " + completedPlaneTrips;
                    statusText.setText(text);
                }
            });

            // Add plane to destination airport's available planes queue
            // Plane now able to be called by AiportController to doFlight()
            // (Note: Could have added method to Airport to add available plane)
            BlockingQueue<Plane> availablePlanes = destAirport.getAvailablePlanes();
            availablePlanes.put(plane);
        }
        catch (InterruptedException e)
        {
            logger.info(() -> String.format("Flight for plane " + plane.getId() + " interrupted."));
        }
    }

    // Prevent any text area updates
    public void endSim() {
        this.simEnded = true;
    }

    // Interrupt all threads in the thread pool
    public void interruptThreads() {
        this.executor.shutdownNow();
    }

    // Join all threads in the thread pool
    public void joinThreads() {
        try
        {
            this.executor.awaitTermination(5, TimeUnit.SECONDS);
        }
        catch (InterruptedException e)
        {
            logger.info(() -> String.format("Successfully awaited termination for the threads in the thread pool in PlaneController."));
        }
    }
}