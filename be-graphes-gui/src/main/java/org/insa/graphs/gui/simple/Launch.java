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
import org.insa.graphs.algorithm.shortestpath.AStarAlgorithm;

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
                "C:\\Users\\lolin\\Downloads\\ile-de-france (1).mapgr",                   
                "C:\\Users\\lolin\\Downloads\\carre-dense.mapgr",
        
        };
        //"C:\\Users\\josse\\Downloads\\ile-de-france.mapgr",
        //"C:\\Users\\josse\\Downloads\\carre-dense.mapgr"




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
            // on teste plusieurs scénarios { origine, destination, 0 si distance OU 1 si temps, 0 toutes routes 1 si only voiture}

            Object[][] scenarios={
                {30,30,0,0," Test origine=arrivée"}, //test pour scenario avec origine=arrivee
                {1,1000000,0,0, "Test mauvais indiçage"}, //test pour scenarios avec mauvais indices
                {7232,29607,0,0, "Test chemin plus court, toutes les routes"}, // test chemin plus court, toute les routes
                {7232,29607,0,1, " Test chemin plus court, que voiture"}, // test chemin plus court, que voiture
                {7232,29607,1,0, "Test chemin plus rapide, toute les routes"}, // test chemin plus rapide, toute les routes
                {7232,29607,1,1, "Test chemin plud rapide, piétons"}, // test chemin plus rapide, que les piétons
            };

            int TotalTest=scenarios.length;
            int TestSucces= 0;

            for ( Object[] scenario: scenarios){  // on va test chaque scenario
                int origine = (int) scenario[0];
                int destination= (int) scenario[1];
                int test= (int) scenario[2];
                int voiture = (int) scenario[3];
                String description = (String) scenario[4];

                //Test pour vérifier de pas etre index out of range
                if(origine>= graph.size() || destination >= graph.size()){
                    System.out.println(" Ce scénario est ignoré"+ origine+ "->" +destination+" car indice invalide");
                    System.out.println("________________________________________________________________________________");
                    continue;

                }
                
                // savoir si t'utilise le filtre du temps ou de la distance  et/ou mode voiture ou toute les routes
                int indexFiltre = (test==0 ? 0:2) + (voiture == 1 ? 1 : 0);
                ArcInspector inspector = ArcInspectorFactory.getAllFilters().get(indexFiltre);

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

                //A star
                AStarAlgorithm astar = new AStarAlgorithm(data);
                ShortestPathSolution astarsol = astar.run();

                
                
                // Affichage
                System.out.println("Scénario"+ description+" ["+origine+ "->" +destination+"]");
                
                boolean tousFaisables = dijkstrasol != null && dijkstrasol.isFeasible() && bellmansol != null && bellmansol.isFeasible() && astarsol != null && astarsol.isFeasible();
                boolean tousInfeasibles= (dijkstrasol == null || !dijkstrasol.isFeasible()) && (bellmansol == null || !bellmansol.isFeasible()) && (astarsol==null || !astarsol.isFeasible());
                

                if(tousFaisables){
                    Path chemin_1 = dijkstrasol.getPath();
                    Path chemin_2 =bellmansol.getPath();
                    Path chemin_3 = astarsol.getPath();

                    double marge_erreur= 0.01; // j'avais pris 1e-3 mais trop strict pour la map carre dense

                    // on va vérifier ou non si dijkstra et bellman ford on plus ou moins les même resultats ou pas
                    boolean MemeLongueur = Math.abs(chemin_1.getLength() - chemin_2.getLength()) < marge_erreur && Math.abs(chemin_1.getLength()-chemin_3.getLength()) < marge_erreur;
                    boolean MemeTemps = Math.abs(chemin_1.getMinimumTravelTime() - chemin_2.getMinimumTravelTime()) < marge_erreur && Math.abs(chemin_1.getMinimumTravelTime() - chemin_3.getMinimumTravelTime())< marge_erreur;

                    // Affichage
                    System.out.printf(" Dijkstra: %.2f m, %.2f s%n", chemin_1.getLength(),chemin_1.getMinimumTravelTime());
                    System.out.printf(" Bellman: %.2f m, %.2f s%n", chemin_2.getLength(),chemin_2.getMinimumTravelTime());
                    System.out.printf(" A*: %.2f m, %.2f s%n", chemin_3.getLength(),chemin_3.getMinimumTravelTime());
                    System.out.printf("                                                                                 %n");

                    System.out.flush();
                    Thread.sleep(500);
                

            
                    if ((test==0 && MemeLongueur) || (test==1 && MemeTemps) ){
                        System.out.println(" Test Reussi");
                        System.out.println("_ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _");
                        System.out.flush();
                        Thread.sleep(500);
                        TestSucces++;
                        drawing.drawPath(chemin_1);

                    }else{
                        System.out.println("test Echoué: resultats differents");
                        System.out.println("_ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _");
                    }
                }
                else if(tousInfeasibles){
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
        // ============================= TEST SPECIFIQUE POUR LA CARTE INSA =========================================
    
        System.out.println("TEST SPECIFIQUE POUR LA CARTE DE L'INSA");

        final String customMapPath="C:\\Users\\lolin\\Downloads\\insa (1).mapgr";
        final Graph customGraph;

        // create a graph reader
            try (final GraphReader reader = new BinaryGraphReader(new DataInputStream(
                    new BufferedInputStream(new FileInputStream(customMapPath))))) {

                // TODO: read the graph
                customGraph = reader.read();
            }


            // TODO: draw the graph on the drawing
            drawing.drawGraph(customGraph);

            // Test automatique
            // on teste plusieurs scénarios { origine, destination, 0 si distance OU 1 si temps, 0 toutes routes 1 si only voiture}

            Object[][] customScenarios={
                {30,30,0,0," Test origine=arrivée"}, //test pour scenario avec origine=arrivee
                {1,1000000,0,0, "Test mauvais indiçage"}, //test pour scenarios avec mauvais indices
                {1090,1103,1,1, "Test chemin traversant canal à pieds"},
                
                {467,965,0,0, "Test chemin plus court, toutes les routes"}, // test d'un chemin accessible voiture et a pied par voie differentes et 
                {467,965,0,1, " Test chemin plus court, que voiture"}, //      dont le chemin le plus rzpide c'est celui plus court en voiture
                {467,965,1,0, "Test chemin plus rapide, toute les routes"}, 
                {467,965,1,1, "Test chemin plud rapide, piétons"},

                {277,526,0,0, "Test chemin plus court, toutes les routes"}, // test d'un chemin accessible que à pieds
                {277,526,0,1, " Test chemin plus court, que voiture"},
                {277,526,1,0, "Test chemin plus rapide, toute les routes"}, // donc le test en voiture marche pas
                {277,526,1,1, "Test chemin plud rapide, piétons"},
                
                
            };
           
            int TotalTest1=customScenarios.length;
            int TestSucces1= 0;

            for ( Object[] scenario: customScenarios){  // on va test chaque scenario
                int origine = (int) scenario[0];
                int destination= (int) scenario[1];
                int test= (int) scenario[2];
                int voiture = (int) scenario[3];
                String description = (String) scenario[4];

                //Test pour vérifier de pas etre index out of range
                if(origine>= customGraph.size() || destination >= customGraph.size()){
                    System.out.println(" Ce scénario est ignoré"+ origine+ "->" +destination+" car indice invalide");
                    System.out.println("________________________________________________________________________________");
                    continue;

                }
                
                // savoir si t'utilise le filtre du temps ou de la distance  et/ou mode voiture ou toute les routes
                int indexFiltre = (test==0 ? 0:2) + (voiture == 1 ? 1 : 0);
                ArcInspector inspector = ArcInspectorFactory.getAllFilters().get(indexFiltre);

                // Je crée mon objet ShortestPathData qui va être en donnée pour l'algo djikstra
                // il contien ( graph travail, noeud origin, noeud dest, type test distance ou temps)

                ShortestPathData data= new ShortestPathData( customGraph, customGraph.get(origine),customGraph.get(destination), inspector);
                
                // Djikstra
                DijkstraAlgorithm dijkstra =new DijkstraAlgorithm(data); // data objet créer par classe ShortestPathtest
                // resultat de l'algo ( renvoie pah, feasible or not, longueur et temps)
                ShortestPathSolution dijkstrasol= dijkstra.run();

                //Bellman-Ford
                BellmanFordAlgorithm bellman = new BellmanFordAlgorithm(data);
                ShortestPathSolution bellmansol = bellman.run();

                //A star
                AStarAlgorithm astar = new AStarAlgorithm(data);
                ShortestPathSolution astarsol = astar.run();

                
                
                // Affichage
                System.out.println("Scénario"+ description+" ["+origine+ "->" +destination+"]");
                
                boolean tousFaisables = dijkstrasol != null && dijkstrasol.isFeasible() && bellmansol != null && bellmansol.isFeasible() && astarsol != null && astarsol.isFeasible();
                boolean tousInfeasibles= (dijkstrasol == null || !dijkstrasol.isFeasible()) && (bellmansol == null || !bellmansol.isFeasible()) && (astarsol==null || !astarsol.isFeasible());
                

                if(tousFaisables){
                    Path chemin_1 = dijkstrasol.getPath();
                    Path chemin_2 =bellmansol.getPath();
                    Path chemin_3 = astarsol.getPath();

                    double marge_erreur= 0.01; // j'avais pris 1e-3 mais trop strict pour la map carre dense

                    // on va vérifier ou non si dijkstra et bellman ford on plus ou moins les même resultats ou pas
                    boolean MemeLongueur = Math.abs(chemin_1.getLength() - chemin_2.getLength()) < marge_erreur && Math.abs(chemin_1.getLength()-chemin_3.getLength()) < marge_erreur;
                    boolean MemeTemps = Math.abs(chemin_1.getMinimumTravelTime() - chemin_2.getMinimumTravelTime()) < marge_erreur && Math.abs(chemin_1.getMinimumTravelTime() - chemin_3.getMinimumTravelTime())< marge_erreur;

                    // Affichage
                    System.out.printf(" Dijkstra: %.2f m, %.2f s%n", chemin_1.getLength(),chemin_1.getMinimumTravelTime());
                    System.out.printf(" Bellman: %.2f m, %.2f s%n", chemin_2.getLength(),chemin_2.getMinimumTravelTime());
                    System.out.printf(" A*: %.2f m, %.2f s%n", chemin_3.getLength(),chemin_3.getMinimumTravelTime());
                    System.out.printf("                                                                                 %n");
                    System.out.flush();
                    Thread.sleep(500);
                

            
                    if ((test==0 && MemeLongueur) || (test==1 && MemeTemps) ){
                        System.out.println(" Test Reussi");
                        System.out.println("_ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _");
                        System.out.flush();
                        Thread.sleep(500);
                        TestSucces1++;
                        drawing.drawPath(chemin_1);

                    }else{
                        System.out.println("test Echoué: resultats differents");
                        System.out.println("_ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _");
                    }
                }
                else if(tousInfeasibles){
                    System.out.println("Pas de chemin trouvé");
                    System.out.println("_ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _");
                    TestSucces1++;
                }else{
                    System.out.println(" Test échoué : incohérence entre les deux algorithmes.");
                    System.out.println("_ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _");
                }


            
            }
            System.out.println("Résumé pour la map: " + customMapPath+ ": "+ TestSucces1 + " / " +TotalTest1 + " tests réussis.");
            System.out.println("========================================================================================");

            // ============================= TEST SPECIFIQUE POUR LA CARTE JAPON =========================================

            System.out.println("TEST SPECIFIQUE POUR LA CARTE DU JAPON");

        final String customMapPath2="C:\\Users\\lolin\\Downloads\\japan (1).mapgr";
        final Graph customGraph2;

        // create a graph reader
            try (final GraphReader reader = new BinaryGraphReader(new DataInputStream(
                    new BufferedInputStream(new FileInputStream(customMapPath2))))) {

                // TODO: read the graph
                customGraph2 = reader.read();
            }


            // TODO: draw the graph on the drawing
            drawing.drawGraph(customGraph2);

            // Test automatique
            // on teste plusieurs scénarios { origine, destination, 0 si distance OU 1 si temps, 0 toutes routes 1 si only voiture}

            Object[][] customScenarios2={
                {30,30,0,0," Test origine=arrivée"}, //test pour scenario avec origine=arrivee
                {1,1000000,0,0, "Test mauvais indiçage"}, //test pour scenarios avec mauvais indices
                {1470724,2176266,0,0, "Test chemin d'une ile à une autre"}, // chemin plus court distance entre deux ile ( pas de path)
                // pas besoin de test tout le monde car 0 path pour tout le monde mais prends trop de temps
                
                {3717422,1776881,0,0, "Test chemin plus court, toutes les routes"}, // test classique de points qui fonctionnent
                {3717422,1776881,0,1, " Test chemin plus court, que voiture"}, 
                {3717422,1776881,1,0, "Test chemin plus rapide, toute les routes"}, 
                {3717422,1776881,1,1, "Test chemin plud rapide, piétons"},

                {832620,2244578,0,0, "Test chemin plus court, toutes les routes"}, // test d'un chemin coupé par des montagnes
                {832620,2244578,0,1, " Test chemin plus court, que voiture"}, // tous les tests renvoient diff et surtout celui a pieds qui est bcp plus chiant a cause du relief
                {832620,2244578,1,0, "Test chemin plus rapide, toute les routes"}, // donc logique donc marche bien
                {832620,2244578,1,1, "Test chemin plud rapide, piétons"},
                
                
            };
           
            int TotalTest2=customScenarios2.length;
            int TestSucces2= 0;

            for ( Object[] scenario: customScenarios2){  // on va test chaque scenario
                int origine = (int) scenario[0];
                int destination= (int) scenario[1];
                int test= (int) scenario[2];
                int voiture = (int) scenario[3];
                String description = (String) scenario[4];

                //Test pour vérifier de pas etre index out of range
                if(origine>= customGraph2.size() || destination >= customGraph2.size()){
                    System.out.println(" Ce scénario est ignoré"+ origine+ "->" +destination+" car indice invalide");
                    System.out.println("________________________________________________________________________________");
                    continue;

                }
                
                // savoir si t'utilise le filtre du temps ou de la distance  et/ou mode voiture ou toute les routes
                int indexFiltre = (test==0 ? 0:2) + (voiture == 1 ? 1 : 0);
                ArcInspector inspector = ArcInspectorFactory.getAllFilters().get(indexFiltre);

                // Je crée mon objet ShortestPathData qui va être en donnée pour l'algo djikstra
                // il contien ( graph travail, noeud origin, noeud dest, type test distance ou temps)

                ShortestPathData data= new ShortestPathData( customGraph2, customGraph2.get(origine),customGraph2.get(destination), inspector);
                
                // Djikstra
                DijkstraAlgorithm dijkstra =new DijkstraAlgorithm(data); // data objet créer par classe ShortestPathtest
                // resultat de l'algo ( renvoie pah, feasible or not, longueur et temps)
                ShortestPathSolution dijkstrasol= dijkstra.run();

                // //Bellman-Ford
                // BellmanFordAlgorithm bellman = new BellmanFordAlgorithm(data);
                // ShortestPathSolution bellmansol = bellman.run();s

                //A star
                AStarAlgorithm astar = new AStarAlgorithm(data);
                ShortestPathSolution astarsol = astar.run();

                
                
                // Affichage
                System.out.println("Scénario"+ description+" ["+origine+ "->" +destination+"]");
                
                boolean dFeasible = dijkstrasol != null && dijkstrasol.isFeasible();
                boolean aFeasible=  (astarsol != null && astarsol.isFeasible());
                
                boolean tousFaisables = dFeasible && aFeasible;
                boolean tousInfeasibles = !dFeasible && !aFeasible;

                if(tousFaisables){
                    Path chemin_1 = dijkstrasol.getPath();
                    //Path chemin_2 =bellmansol.getPath();
                    Path chemin_3 = astarsol.getPath();

                    double marge_erreur= 0.01; // j'avais pris 1e-3 mais trop strict pour la map carre dense

                    // on va vérifier ou non si dijkstra et bellman ford on plus ou moins les même resultats ou pas
                    boolean MemeLongueur = Math.abs(chemin_1.getLength() - chemin_3.getLength()) < marge_erreur ;
                    boolean MemeTemps = Math.abs(chemin_1.getMinimumTravelTime() - chemin_3.getMinimumTravelTime())< marge_erreur;

                    // Affichage
                    System.out.printf(" Dijkstra: %.2f m, %.2f s%n", chemin_1.getLength(),chemin_1.getMinimumTravelTime());
                    // System.out.printf(" Bellman: %.2f m, %.2f s%n", chemin_2.getLength(),chemin_2.getMinimumTravelTime());
                    System.out.printf(" A*: %.2f m, %.2f s%n", chemin_3.getLength(),chemin_3.getMinimumTravelTime());
                    System.out.printf("                                                                                 %n");
                    System.out.flush();
                    Thread.sleep(500);
                

            
                    if ((test==0 && MemeLongueur) || (test==1 && MemeTemps) ){
                        System.out.println(" Test Reussi");
                        System.out.println("_ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _");
                        System.out.flush();
                        Thread.sleep(500); 
                        TestSucces2++;
                        drawing.drawPath(chemin_1);

                    }else{
                        System.out.println("test Echoué: resultats differents");
                        System.out.println("_ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _");
                    }
                }
                else if(tousInfeasibles){
                    System.out.println("Pas de chemin trouvé");
                    System.out.println("_ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _");
                    TestSucces2++;
                }else{
                    System.out.println(" Test échoué : incohérence entre les deux algorithmes.");
                    System.out.println("_ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _");
                }


            
            }
            System.out.println("Résumé pour la map: " + customMapPath2+ ": "+ TestSucces2 + " / " +TotalTest2 + " tests réussis.");
            System.out.println("========================================================================================");
    
    
    
    
    
    
    }
}
