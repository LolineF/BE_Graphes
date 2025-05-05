package org.insa.graphs.algorithm.shortestpath;

import org.insa.graphs.model.Arc;
import org.insa.graphs.model.Node;


public class Label implements Comparable<Label> {
    
    private Node sommetCourant;

    private boolean marque;

    private double coutRealise;

    private Arc pere ;

    public Label(Node sommetCourant){
        this.marque = false;
        this.coutRealise = Double.POSITIVE_INFINITY;
        this.pere = null;
        this.sommetCourant = sommetCourant;
    }

    public Node getSommetCourant(){
        return this.sommetCourant;
    }

    public boolean getMarque(){
        return this.marque;
    }

    public void setMarque(){
        this.marque = !(marque);
    }

    public double getCoutRealise(){
        return this.coutRealise;
    }

    public void setCoutRealise(double cout){
        this.coutRealise = cout;
    }

    public Arc getPere(){
        return this.pere;
    }

    public void setPere(Arc arc){
        this.pere = arc;
    }

    public double getCost(){
        return this.coutRealise;
    }

    /**
     * @param l
     * @return 1 si l est plus grand et -1 sinon
     */
    @Override
    public int compareTo(Label l){
        return (this.getCost() < l.getCost()) ? 1 : -1;
    }
    
}
