package org.insa.graphs.algorithm.shortestpath;

import org.insa.graphs.model.Node;

public class LabelStar extends Label {

    private double CoutEstime; // Heuristique h(n)

    public LabelStar(Node sommetCourant, double CoutEstime) {
        super(sommetCourant);
        this.CoutEstime = CoutEstime;
    }

    /**
     * Retourne f(n) = g(n) + h(n)
     */
    @Override
    public double getTotalCost() {
        return getCost() + this.CoutEstime;
    }

    public double getEstimatedCost() {
        return this.CoutEstime;
    }

    public void setEstimatedCost(double estimatedCost) {
        this.CoutEstime = estimatedCost;
    }
}
