package edu.curtin.saed.assignment1;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Airport {
    
    private int id;
    private GridAreaIcon icon;
    private List<Plane> planes;
    private BlockingQueue<Plane> availablePlanes;
    private BlockingQueue<String> requests;

    public Airport(int id, GridAreaIcon icon, List<Plane> planes) {
        this.id = id;
        this.icon = icon;
        this.planes = Collections.synchronizedList(planes);
        this.availablePlanes = new LinkedBlockingQueue<>(planes);
        this.requests = new LinkedBlockingQueue<>();
    }

    public BlockingQueue<Plane> getAvailablePlanes() {
        return this.availablePlanes;
    }

    public BlockingQueue<String> getRequests() {
        return this.requests;
    }

    public int getId() {
        return this.id;
    }

    public double getX() {
        return this.icon.getX();
    }

    public double getY() {
        return this.icon.getY();
    }

    public List<Plane> getPlanes() {
        return this.planes;
    }

    public void addPlane(Plane plane) {
        synchronized (planes) {
            planes.add(plane);
        }
    }

    public void removePlane(int planeId) {
        synchronized (planes) {
            planes.removeIf(it -> it.getId() == planeId);
        }
    }
}