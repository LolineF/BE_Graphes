package org.insa.graphs.algorithm.shortestpath;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import org.insa.graphs.algorithm.utils.BinaryHeap;
import org.insa.graphs.model.Arc;
import org.insa.graphs.model.Graph;
import org.insa.graphs.model.Path;
import org.insa.graphs.model.Node;
import org.insa.graphs.algorithm.AbstractSolution.Status;
import org.insa.graphs.algorithm.AbstractInputData;

public class AStarAlgorithm extends DijkstraAlgorithm {

    public AStarAlgorithm(ShortestPathData data) {
        super(data);
    }
    @Override
    protected ShortestPathSolution doRun() {

        ShortestPathData data = getInputData();
        Graph graph = data.getGraph();
        final int nbNodes = graph.size();

        ShortestPathSolution solution = null;

        BinaryHeap<Label> tas = new BinaryHeap<>();
        HashMap<Node, Label> lienNodeLabel = new HashMap<>();

        Node origin = data.getOrigin();
        Node destination = data.getDestination();

        // Initialisation de l'origine
        
        double heuristiqueOrigin;
        if(data.getMode()==AbstractInputData.Mode.LENGTH){
            heuristiqueOrigin = origin.getPoint().distanceTo(destination.getPoint());
        }else{
            // dans le cas ou on fait le plus rapide on utilise quand même distanceTo mais on divise par la vitesse max
            double maxvitesse= Math.max(data.getMaximumSpeed(),1); // Math.max permet de proteger la division par 0;
            heuristiqueOrigin= origin.getPoint().distanceTo(destination.getPoint())/(maxvitesse/3.6); // temps estimé
        }
        
        
        LabelStar originLabel = new LabelStar(origin, heuristiqueOrigin);
        originLabel.setCoutRealise(0);
        lienNodeLabel.put(origin, originLabel);
        tas.insert(originLabel);
        notifyOriginProcessed(origin);

        while (!tas.isEmpty()) {

            Label currentLabel = tas.deleteMin();
            Node currentNode = currentLabel.getSommetCourant();
            currentLabel.setMarque(true);
            notifyNodeMarked(currentNode);

            if (currentNode == destination) {
                break;
            }

            for (Arc arc : currentNode.getSuccessors()) {
                if (!data.isAllowed(arc)) continue;

                Node neighbor = arc.getDestination();
                double newCost = currentLabel.getCost() + data.getCost(arc);
                Label neighborLabel = lienNodeLabel.get(neighbor);

                if (neighborLabel == null) {
                    double heuristique;
                    if(data.getMode()== AbstractInputData.Mode.LENGTH){
                        heuristique = neighbor.getPoint().distanceTo(destination.getPoint());

                    }else{
                        double maxvitesse= Math.max(data.getMaximumSpeed(),1);
                        heuristique=neighbor.getPoint().distanceTo(destination.getPoint()) /(maxvitesse/3.6);
                    }



                    neighborLabel = new LabelStar(neighbor, heuristique);
                    lienNodeLabel.put(neighbor, neighborLabel);
                    tas.insert(neighborLabel);
                    notifyNodeReached(neighbor);
                }

                if (!neighborLabel.getMarque() && newCost < neighborLabel.getCost()) {
                    tas.remove(neighborLabel);
                    neighborLabel.setCoutRealise(newCost);
                    neighborLabel.setPere(arc);
                    tas.insert(neighborLabel);
                }
            }
        }

        Label destLabel = lienNodeLabel.get(destination);
        if (destLabel == null || destLabel.getPere() == null) {
            solution = new ShortestPathSolution(data, Status.INFEASIBLE);
        } else {
            ArrayList<Arc> arcs = new ArrayList<>();
            Arc arc = destLabel.getPere();
            while (arc != null) {
                arcs.add(arc);
                arc = lienNodeLabel.get(arc.getOrigin()).getPere();
            }
            Collections.reverse(arcs);
            notifyDestinationReached(destination);
            solution = new ShortestPathSolution(data, Status.OPTIMAL, new Path(graph, arcs));
        }

        return solution;
    }
}

