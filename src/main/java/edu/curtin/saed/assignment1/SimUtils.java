package edu.curtin.saed.assignment1;
import java.util.*;

/*
 * Math-related utility methods for generating airport locations and calculating flight path
 * positions.
 * All methods in this class were created with the assistance of ChatGPT.
 */
public final class SimUtils {

    // Get positions a plane must be on from current to destination airport, in 50ms increments.
    public static List<List<Double>> getFlightPositions(Airport currAirport, Airport destAirport) {
        List<List<Double>> positions = new ArrayList<>();
        double speedPerSec = 1.2;
        double updateEveryMs = 50.0;

        double x1 = currAirport.getX();
        double y1 = currAirport.getY();
        double x2 = destAirport.getX();
        double y2 = destAirport.getY();
        
        double dx = x2 - x1;
        double dy = y2 - y1;
        double dist = Math.hypot(dx, dy);

        double stepD = speedPerSec * (updateEveryMs / 1000.0);
        double ux = dx / dist;
        double uy = dy / dist;

        double x = x1;
        double y = y1;

        while (true) {
            double remaining = Math.hypot(x2 - x, y2 - y);
            if (remaining <= stepD) {
                break;
            }
            x += ux * stepD;
            y += uy * stepD;
            positions.add(new ArrayList<>(Arrays.asList(x, y)));
        }

        positions.add(new ArrayList<>(Arrays.asList(x2, y2)));
        return positions;
    }

    // To get positions the 10 airports are to be for the simulation.
    // Excludes a margin from the border and ensures they aren't too close to one another.
    public static List<List<Double>> generateAirportPositions() {
        Random rand = new Random();
        List<List<Double>> positions = new ArrayList<>();
        int num = 10;
        int attempts = 0;

        while (positions.size() < num && attempts < 100000) {
            attempts++;
            int xInt = 50 + rand.nextInt(1101);
            int yInt = 50 + rand.nextInt(1101);
            double x = xInt / 100.0;
            double y = yInt / 100.0;

            boolean ok = true;
            for (List<Double> pos : positions) {
                double dx = x - pos.get(0);
                double dy = y - pos.get(1);
                if (dx * dx + dy * dy < 1.0) {
                    ok = false;
                    break;
                }
            }

            if (ok) {
                positions.add(Arrays.asList(x, y));
            }
        }

        if (positions.size() < num) {
            throw new IllegalStateException("Could not generate all 10 points in the given area size.");
        }
        return positions;
    } 
}
