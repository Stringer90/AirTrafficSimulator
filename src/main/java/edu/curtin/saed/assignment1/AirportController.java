package edu.curtin.saed.assignment1;
import java.io.*;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Logger;

public class AirportController {
    
    private List<Airport> airports;

    // For each airport: Generate requests and add them to the airport.
    private List<Thread> requestGenerators;

    // For each airport: Get most recent queue, wait for plane to become 
    // available, call PlaneController to complete the request.
    private List<Thread> requestHandlers;

    // To be able to call doFlight() with a plane
    private PlaneController planeController;

    private static final Logger logger = Logger.getLogger(AirportController.class.getName());

    public AirportController(List<Airport> airports, PlaneController planeController) {
        this.airports = airports;
        this.planeController = planeController;
        this.requestGenerators = new ArrayList<>();
        this.requestHandlers = new ArrayList<>();
    }

    // Start threads to generate and handle flight requests
    public void beginSim(){
        for (Airport airport : airports) {
            Thread newThread = new Thread(() -> beginGeneration(airport));
            this.requestGenerators.add(newThread);
            newThread.start();

            newThread = new Thread(() -> beginHandling(airport, planeController));
            this.requestHandlers.add(newThread);
            newThread.start();
        }
    }

    /* GENERATE FLIGHT REQUESTS - FOR EACH AIRPORT
     * 
     * Generates flight requests for a specific airport.
     * Stores them in the airport's blocking queue that holds flight requests.
     * These will be removed from the queue by the thread that handles flight requests.
     * 
     * This method is run in a thread, with a further, internal thread for 'flightRequests.go()'.
     * (As demonstrated in provided demo code 'comms')
     */
    private void beginGeneration(Airport pAirport) {

        var flightRequests = new StandardFlightRequests(10, pAirport.getId());
        Thread thread = new Thread(
            () -> {
                try
                {
                    flightRequests.go();
                }
                catch(InterruptedException e)
                {
                    logger.info(() -> String.format("Sub-Thread to generate requests for airport %d ends.", pAirport.getId()));
                }
            });

        logger.info(() -> String.format("Sub-Thread to generate requests for airport %d starts.", pAirport.getId()));
        thread.start();

        logger.info(() -> String.format("Thread to insert requests into airport %d starts.", pAirport.getId()));
        BlockingQueue<String> airportRequests = pAirport.getRequests();
        try(BufferedReader br = flightRequests.getBufferedReader())
        {
            while(true)
            {
                if(Thread.currentThread().isInterrupted()) {
                    throw new InterruptedException();
                }
                String line = br.readLine();
                airportRequests.put(line);
            }
        }
        catch(InterruptedException e)
        {
            logger.info(() -> String.format("Thread to insert requests into airport %d ends by interruption.", pAirport.getId()));
        }
        catch(IOException e)
        {
            System.err.printf("Could not run command: %s: %s", e.getClass(), e.getMessage());
            logger.info(() -> String.format("Thread to insert requests into airport %d ends by IOException.", pAirport.getId()));
        }
        finally
        {
            thread.interrupt();
        }
    }

    /*
     * Match next request to an available flight.
     */
    /* HANDLE FLIGHT REQUESTS - FOR EACH AIRPORT
     * 
     * Matches the next flight request in the airport's flight request queue to an available
     * plane that is at the same airport.
     * 
     * Once there is a match, make the PlaneController start a thread to handle the flight itself.
     */
    private void beginHandling(Airport pAirport, PlaneController planeController) {
        try
        {
            logger.info(() -> String.format("Thread to handle requests for airport %d starts.", pAirport.getId()));
            BlockingQueue<String> airportRequests = pAirport.getRequests();
            BlockingQueue<Plane> availablePlanes = pAirport.getAvailablePlanes();
            while(true)
            {
                if(Thread.currentThread().isInterrupted()) { throw new InterruptedException(); }

                String request = airportRequests.take();
                Plane plane = availablePlanes.take();
                Airport destAirport = getAirportById(Integer.parseInt(request));
                pAirport.removePlane(plane.getId());
                plane.setDestAirport(destAirport);

                if(Thread.currentThread().isInterrupted()) { throw new InterruptedException(); }

                planeController.processRequest(plane, pAirport, destAirport);
            }
        }
        catch(InterruptedException e)
        {
            logger.info(() -> String.format("Thread to handle requests for airport %d ends.", pAirport.getId()));
        }
    }

    // Get an airport by matching its ID.
    // For getting destination airport object when calling processRequest() in beginHandling().
    public Airport getAirportById(int id) {
        for (Airport airport : airports) {
            if (airport.getId() == id) {
                return airport;
            }
        }
        return null;
    }

    // Interrupt all threads in AirportController.
    // All request handler threads (one for each airport)
    // All request generator threads, and their sub-threads (for each airport)
    public void interruptThreads() {
        for (Thread thread : requestHandlers) {
            thread.interrupt();
        }
        for (Thread thread : requestGenerators) {
            thread.interrupt();
        }
    }

    // Join all threads to ensure they have properly ended
    public void joinThreads() {
        for (Thread thread : requestHandlers) {
            try {
                thread.join();
            }
            catch (InterruptedException e) {
                logger.info(() -> String.format("requestHandler thread joined."));
            } 
        }
        for (Thread thread : requestGenerators) {
            try {
                thread.join();
            }
            catch (InterruptedException e) {
                logger.info(() -> String.format("requestGenerator thread joined."));
            } 
        }
    }
}
