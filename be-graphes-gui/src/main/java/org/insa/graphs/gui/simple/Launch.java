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
        
        System.out.println("DÉMARRAGE DES TESTS");

        // on crée qu'une seule fenetre de drawing car sinon trop de fenetres ouvertes
        final Drawing drawing =createDrawing();
        // visit these directory to see the list of available files on commetud.
        final String[] mapFiles = {
                "C:\\Users\\josse\\Downloads\\ile-de-france.mapgr",
                "C:\\Users\\josse\\Downloads\\carre-dense.mapgr",
        };
        // final String pathName =
        //         "/mnt/commetud/3eme Annee MIC/Graphes-et-Algorithmes/Paths/path_fr31insa_rangueil_r2.path";

        for( String mapName: mapFiles){

            //Affiche nom Map
            System.out.println("Nom de la map: "+ mapName);    


            final Graph graph;
            final Path path;

            // create a graph reader
            try (final GraphReader reader = new BinaryGraphReader(new DataInputStream(
                    new BufferedInputStream(new FileInputStream(mapName))))) {

                // TODO: read the graph
                graph = reader.read();
            }


            // TODO: draw the graph on the drawing
            drawing.drawGraph(graph);
            
            // Test automatique
            // on teste plusieurs scénarios { origine, destination, 0 si distance OU 1 si temps}

            int[][] scenarios={
                {1,40000,0}, // test du plus court chemin au niveau de la distance
                {100,80000,1},// test du plus court chemin au niveau du temps
                {30,30,0}, //test pour scenario avec origine=arrivee
                {1,1000000,0}, //test pour scenarios avec mauvais indices
            };

            int TotalTest=scenarios.length;
            int TestSucces= 0;

            for ( int[] scenario: scenarios){  // on va test chaque scenario
                int origine = scenario[0];
                int destination= scenario[1];
                int test= scenario[2];

                //Test pour vérifier de pas etre index out of range
                if(origine>= graph.size() || destination >= graph.size()){
                    System.out.println(" Ce scénario est ignoré"+ origine+ "->" +destination+" car indice invalide");
                    System.out.println("________________________________________________________________________________");
                    continue;

                }
                

                // savoir si t'utilise le filtre du temps ou de la distance
                ArcInspector inspector = (test == 0) ?ArcInspectorFactory.getAllFilters().get(0) : ArcInspectorFactory.getAllFilters().get(2);

                // Je crée mon objet ShortestPathData qui va être en donnée pour l'algo djikstra
                // il contien ( graph travail, noeud origin, noeud dest, type test distance ou temps)

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

                    double marge_erreur= 0.01; // j'avais pris 1e-3 mais trop strict pour la map carre dense

                    // on va vérifier ou non si dijkstra et bellman ford on plus ou moins les même resultats ou pas
                    boolean MemeLongueur = Math.abs(chemin_1.getLength() - chemin_2.getLength()) < marge_erreur;
                    boolean MemeTemps = Math.abs(chemin_1.getMinimumTravelTime() - chemin_2.getMinimumTravelTime()) < marge_erreur;

                    // Affichage
                    System.out.printf(" Dijkstra: %.2f m, %.2f s%n", chemin_1.getLength(),chemin_1.getMinimumTravelTime());
                    System.out.printf(" Bellman: %.2f m, %.2f s%n", chemin_2.getLength(),chemin_2.getMinimumTravelTime());
                    System.out.printf("                                                                                 %n");

                

            
                    if ((test==0 && MemeLongueur) || (test==1 && MemeTemps) ){
                        System.out.println(" Test Reussi");
                        System.out.println("_ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _");
                        TestSucces++;
                        drawing.drawPath(chemin_1 );

                    }else{
                        System.out.println("test Echoué: resultats differents");
                        System.out.println("_ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _");
                    }
                }
                else if(DeuxPasFaisable){
                    System.out.println("Pas de chemin trouvé");
                    System.out.println("_ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _");
                    TestSucces++;
                }else{
                    System.out.println(" Test échoué : incohérence entre les deux algorithmes.");
                    System.out.println("_ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _");
                }


            
            }
            System.out.println("Résumé pour la map: " + mapName+ ": "+ TestSucces + " / " +TotalTest + " tests réussis.");
            System.out.println("========================================================================================");
        }
    }
}
