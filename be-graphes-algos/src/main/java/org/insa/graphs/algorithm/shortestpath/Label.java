package org.insa.graphs.algorithm.shortestpath;

import org.insa.graphs.model.Arc;
import org.insa.graphs.model.Node;

public class Label implements Comparable<Label> {
    
    private Node sommetCourant;
    private boolean marque;
    private double coutRealise;
    private Arc pere;

    public Label(Node sommetCourant) {
        this.marque = false;
        this.coutRealise = Double.POSITIVE_INFINITY;
        this.pere = null;
        this.sommetCourant = sommetCourant;
    }

    public Node getSommetCourant() {
        return this.sommetCourant;
    }

    public boolean getMarque() {
        return this.marque;
    }

    public void setMarque(boolean b) {
        this.marque = b;
    }

    public double getCoutRealise() {
        return this.coutRealise;
    }

    public void setCoutRealise(double cout) {
        this.coutRealise = cout;
    }

    public Arc getPere() {
        return this.pere;
    }

    public void setPere(Arc arc) {
        this.pere = arc;
    }

    public double getCost() {
        return this.coutRealise;
    }

    public void setCost(double cout) {
        this.coutRealise = cout;
    }

    public double getTotalCost(){
        return this.getCost();
    }

    @Override
    public int compareTo(Label l) {
        return Double.compare(this.getCost(), l.getCost());
    }
}
