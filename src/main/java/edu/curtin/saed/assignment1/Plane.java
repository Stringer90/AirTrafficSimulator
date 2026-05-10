package edu.curtin.saed.assignment1;

public class Plane {
    
    private int id;
    private Airport currAirport;
    private Airport destAirport = null;
    private GridAreaIcon icon;

    public Plane(int id, GridAreaIcon icon){ 
        this.id = id;
        this.icon = icon;
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

    public Airport getCurrAirport() {
        return this.currAirport;
    }

    public Airport getDestAirport() {
        return this.destAirport;
    }

    public void setCurrAirport(Airport pCurrAirport) {
        this.currAirport = pCurrAirport;
    }

    public void setPos(double newX, double newY) {
        this.icon.setPosition(newX, newY);
    }

    public void setDestAirport(Airport pDestAirport) {
        this.destAirport = pDestAirport;
    }

    public void setShown(boolean shown)
    {
        this.icon.setShown(shown);
    }
}