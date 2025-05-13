package org.insa.graphs.algorithm.shortestpath;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import org.insa.graphs.algorithm.AbstractSolution.Status;
import org.insa.graphs.algorithm.utils.BinaryHeap;
import org.insa.graphs.model.Arc;
import org.insa.graphs.model.Graph;
import org.insa.graphs.model.Node;
import org.insa.graphs.model.Path;

public class DijkstraAlgorithm extends ShortestPathAlgorithm {

    public DijkstraAlgorithm(ShortestPathData data) {
        super(data);
    }

    @Override
    protected ShortestPathSolution doRun() {

        // retrieve data from the input problem (getInputData() is inherited from the
        // parent class ShortestPathAlgorithm)
        final ShortestPathData data = getInputData();
        Graph graph = data.getGraph();

        final int nbNodes = graph.size();


        // variable that will contain the solution of the shortest path problem
        ShortestPathSolution solution = null;

        // TODO: implement the Dijkstra algorithm
        BinaryHeap<Label> tas = new BinaryHeap<>();
        HashMap<Node,Label> lienNodeLabel = new HashMap<Node,Label>();

        notifyOriginProcessed(data.getOrigin());

        Node origin = data.getOrigin();
        Node dest = data.getDestination();
        Node node;
        double newCout;
        double oldCout;
        Label label;
        ArrayList<Arc> predecessorArcs = new ArrayList<>();

        label = new Label(origin);
        lienNodeLabel.put(origin, label);
        label.setCoutRealise(0);
        label.setMarque(true);

        tas.insert(label);

        lienNodeLabel.put(dest, new Label(dest));

        while(! lienNodeLabel.get(dest).getMarque()){
            for(Arc arc : origin.getSuccessors()){

                node = arc.getDestination();

                // Si le noeud n'existait pas encore dans la HashMap
                if(!lienNodeLabel.containsKey(node)){
                    label = new Label(node);
                    lienNodeLabel.put(node, label);
                    tas.insert(label);
                    notifyNodeReached(arc.getDestination());
                }
                label = lienNodeLabel.get(node);

                oldCout = label.getCost();
                newCout = lienNodeLabel.get(origin).getCost() + data.getCost(arc);

                if (newCout<oldCout){
                    label.setCoutRealise(newCout);
                    label.setPere(arc);
                    tas.remove(label);
                    tas.insert(label);
                }
            }
            origin = tas.findMin().getSommetCourant();
            tas.findMin().setMarque(true);
            predecessorArcs.add(tas.findMin().getPere());
            tas.deleteMin();
            
        }

        notifyDestinationReached(data.getDestination());
        
        // Create the path from the array of predecessors...
        ArrayList<Arc> arcs = new ArrayList<>();
        Arc arc;
        arc = lienNodeLabel.get(data.getDestination()).getPere();
        arcs.add(arc);
        while(arc.getOrigin() != data.getOrigin()) {
            arc = lienNodeLabel.get(arc.getOrigin()).getPere();
            arcs.add(arc);
        }

        // Reverse the path...
        Collections.reverse(arcs);

        // Create the final solution.
        solution = new ShortestPathSolution(data, Status.OPTIMAL,
                new Path(graph, arcs));

        // when the algorithm terminates, return the solution that has been found
        return solution;
    }

}
