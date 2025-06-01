package org.insa.graphs.gui.simple;


import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;


import javax.swing.JFrame;
import javax.swing.SwingUtilities;


import org.insa.graphs.gui.drawing.Drawing;
import org.insa.graphs.gui.drawing.components.BasicDrawing;
import org.insa.graphs.model.Graph;
import org.insa.graphs.model.Path;
import org.insa.graphs.model.io.BinaryGraphReader;
import org.insa.graphs.model.io.GraphReader;
import org.insa.graphs.model.io.PathReader;
import org.insa.graphs.algorithm.ArcInspector;
import org.insa.graphs.algorithm.ArcInspectorFactory;
import org.insa.graphs.algorithm.AbstractInputData.Mode;
import org.insa.graphs.algorithm.shortestpath.ShortestPathData;
import org.insa.graphs.algorithm.shortestpath.ShortestPathSolution;
import org.insa.graphs.algorithm.shortestpath.BellmanFordAlgorithm;
import org.insa.graphs.algorithm.shortestpath.DijkstraAlgorithm;


public class Launch {


    /**
     * Create a new Drawing inside a JFrame an return it.
     *
     * @return The created drawing.
     * @throws Exception if something wrong happens when creating the graph.
     */
    public static Drawing createDrawing() throws Exception {
        BasicDrawing basicDrawing = new BasicDrawing();
        SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                JFrame frame = new JFrame("BE Graphes - Launch");
                frame.setLayout(new BorderLayout());
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setVisible(true);
                frame.setSize(new Dimension(800, 600));
                frame.setContentPane(basicDrawing);
                frame.validate();
            }
        });
        return basicDrawing;
    }


    public static void main(String[] args) throws Exception {


        // visit these directory to see the list of available files on commetud.
        final String mapName =
                "C:\\Users\\lolin\\Downloads\\ile-de-france.mapgr"; // à changer
        // final String pathName =
        //         "/mnt/commetud/3eme Annee MIC/Graphes-et-Algorithmes/Paths/path_fr31insa_rangueil_r2.path";


        final Graph graph;
        final Path path;


        // create a graph reader
        try (final GraphReader reader = new BinaryGraphReader(new DataInputStream(
                new BufferedInputStream(new FileInputStream(mapName))))) {


            // TODO: read the graph
            graph = reader.read();
        }


        // create the drawing
        final Drawing drawing = createDrawing();


        // TODO: draw the graph on the drawing
        drawing.drawGraph(graph);
       
        // Test automatique
        // on teste plusieurs scénarios { origine, destination, 0 si distance OU 1 si temps}
         int[][] scenarios={
             // test du chemin distance nulle
            {5,4000,0}, //test chemin long
            {5,6000,1}, // test temps long
            //...
        };


        int TotalTest=scenarios.length;
        int TestSucces= 0;


        for ( int[] scenario: scenarios){  // on va test chaque scenario
            int origine = scenario[0];
            int destination= scenario[1];
            int test= scenario[2];
            
           


            // savoir si t'utilise le filtre du temps ou de la distance
            ArcInspector inspector = (test == 0) ?ArcInspectorFactory.getAllFilters().get(0) : ArcInspectorFactory.getAllFilters().get(2);


            // Je crée mon objet ShortestPathData qui va être en donnée pour l'algo djikstra
            // il contient ( graph travail, noeud origin, noeud dest, type test distance ou temps)


            ShortestPathData data= new ShortestPathData( graph, graph.get(origine),graph.get(destination), inspector);
           
            // Djikstra
            DijkstraAlgorithm dijkstra =new DijkstraAlgorithm(data); // data objet créer par classe ShortestPathtest
            // resultat de l'algo ( renvoie pah, feasible or not, longueur et temps)
            ShortestPathSolution dijkstrasol= dijkstra.run();


            //Bellman-Ford
            BellmanFordAlgorithm bellman = new BellmanFordAlgorithm(data);
            ShortestPathSolution bellmansol = bellman.run();


            // Affichage
            System.out.println("Scénario"+ origine+ "->" +destination+"(" + (test==0 ? "distance" : "temps")+")");

            boolean DeuxFaisable = dijkstrasol.isFeasible() && bellmansol.isFeasible();
            boolean DeuxPasFaisable= !dijkstrasol.isFeasible() && !bellmansol.isFeasible();


            if(DeuxFaisable){
                Path chemin_1 = dijkstrasol.getPath();
                Path chemin_2 =bellmansol.getPath();


                double marge_erreur= 1e-3; // choisi au hasard


                // on va vérifier ou non si dijkstra et bellman ford on plus ou moins les même resultats ou pas
                boolean MemeLongueur = Math.abs(chemin_1.getLength() - chemin_2.getLength()) < marge_erreur;
                boolean MemeTemps = Math.abs(chemin_1.getMinimumTravelTime() - chemin_2.getMinimumTravelTime()) < marge_erreur;


                // Affichage
                System.out.printf(" Dijkstra: %.2f m, %.2f s%n", chemin_1.getLength(),chemin_1.getMinimumTravelTime());
                System.out.printf(" Bellman: %.2f m, %.2f s%n", chemin_2.getLength(),chemin_2.getMinimumTravelTime());
           


       
                if ((test==0 && MemeLongueur) || (test==1 && MemeTemps) ){
                    System.out.println(" Test Reussi");
                    TestSucces++;
                    drawing.drawPath(chemin_1);


                }else{
                    System.out.println("test Echoué: resultats differents");
                }
            }
            else if(DeuxPasFaisable){
                System.out.println("Pas de chemin trouvé");
                TestSucces++;
            }else{
                 System.out.println(" Test échoué : incohérence entre les deux algorithmes.");
            }




       
        }
        System.out.println("Résumé : " + TestSucces + " / " +TotalTest + " tests réussis.");


    }


}


