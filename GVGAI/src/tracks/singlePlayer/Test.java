package tracks.singlePlayer;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;

import ontology.Types.ACTIONS;
import qlearning.Grafica;
import qlearning.StateManager;
import qlearning.TrainingAgent;
import tools.Utils;
import tracks.ArcadeMachine;

public class Test {

    public static void main(String[] args) {
    	
    	String QLearningTraining = "qlearning.TrainingAgent";
    	String QLearningTesting = "qlearning.TestingAgent";

    	double maxPuntuacionJuego[] = new double[] {110, 62, 98, 76, 50, 72, 62, 50, 140, 50, 16};
    	int nivelesTraining[] = new int[] {0,1,3,2};
    	int nivelesTest[] = new int[] {5,2,4,6,7};

		//Load available games
		String spGamesCollection =  "examples/all_games_sp.csv";
		String[][] games = Utils.readGames(spGamesCollection);

		//Game settings
		boolean visuals = true;
		int seed = new Random().nextInt();

		// Game and level to play
		int gameIdx = 111;
		
		String gameName = games[gameIdx][1];
		
		String game = games[gameIdx][0];
		System.out.println(game);


		String recordActionsFile = null;// "actions_" + games[gameIdx] + "_lvl"
	
		
		int levelIdx = 2; // level names from 0 to 4 (game_lvlN.txt).
		String level1 = game.replace(gameName, gameName + "_lvl" + levelIdx);
		StateManager stateManager;
		
//		ArcadeMachine.playOneGame(game, level1, recordActionsFile, 234234234);
		
//		stateManager = new StateManager(true,true);
//		StateManager.numIteraciones = 1; // Numero de partidas a jugar
//		ArcadeMachine.runOneGame(game, level1, visuals, QLearningTraining, recordActionsFile, seed, 0);
//		
		
		boolean training = true; // Modo entrenamiento, crea una nueva tabla Q y juega M partidas aleatorias
		boolean verbose = true; // Mostrar informacion de la partida mientras se ejecuta
		
		if(training)	// Crea la tabla Q a random y juega partidas con acciones aleatorias
		{
			visuals = true;
			boolean testingAfterTraining = true; // Probar todos los niveles despues del entrenamiento
			boolean randomTablaQ = true; // Verdadero: crea la tabla Q con valores random, si no, a cero
			boolean guardarGrafica = true; // Si queremos guardar una imagen de la grafica Ticks/epoca
			stateManager = new StateManager(randomTablaQ,false);
			StateManager.numIteraciones = 100; // Numero de partidas a jugar

			/*
			 * Grafica Aprendizaje Resultado Score / Epoca
			 */
			double [] YScore = null;
			double [] X = null;
			Grafica graficaTicks = null;
			Grafica graficaScore = null;
			
			if(guardarGrafica) {
				graficaScore = new Grafica();
				graficaTicks = new Grafica();
				
				X = new double[StateManager.numIteraciones]; // Epoca
				YScore = new double[StateManager.numIteraciones]; // Resultado Score
				
				for (int i = 0; i < X.length; i++) {
					X[i] = i;
				}
			}
			
		
			for (StateManager.iteracionActual = 1; StateManager.iteracionActual <= StateManager.numIteraciones; StateManager.iteracionActual++) {
				levelIdx = nivelesTraining[new Random().nextInt(nivelesTraining.length)]; // level names from 0 to 4 (game_lvlN.txt).
				level1 = game.replace(gameName, gameName + "_lvl" + levelIdx);
				
				visuals = false; TrainingAgent.forzarMaxQ = false; //StateManager.verbose = false;
				if(StateManager.iteracionActual % 25 == 0) { // Mostrar cada 25% partidas
					visuals = true;
					TrainingAgent.forzarMaxQ = true;
					StateManager.verbose = true;
					levelIdx = 4; // level names from 0 to 4 (game_lvlN.txt).
					level1 = game.replace(gameName, gameName + "_lvl" + levelIdx);
				}
				
				System.out.println("\t\t\t\t\t\t\t\t\t\tIteración " + StateManager.iteracionActual + " / "+ StateManager.numIteraciones);
				System.out.println("\t\t\t\t\t\t\t\t\t\tlevel: " + levelIdx);
				
				double puntuacion = ArcadeMachine.runOneGame(game, level1, visuals, QLearningTraining, recordActionsFile, seed, 0)[1];
				double aciertoPartida = Math.round(puntuacion / maxPuntuacionJuego[levelIdx] *100);

				System.out.println("\t\t\tPartida completada al " + aciertoPartida + " % [" +puntuacion +"/"+maxPuntuacionJuego[levelIdx]+"]");
				System.out.println("\t\t\tN Estados = " + StateManager.contadoresEstados.size());
				
				YScore[StateManager.iteracionActual-1] = aciertoPartida;
			}
		
			stateManager.saveQTable();
			stateManager.saveQTableResumen();
			
			if(guardarGrafica) {
				String fecha = java.time.LocalDate.now().toString();
				String nombreFich = fecha+"_GraficaScore.jpeg";
				
				graficaScore.plot(X, YScore, "-r", 2.0f, "% Score");
				graficaScore.RenderPlot(); 
				graficaScore.title("Score (%) / Epoca de Training");
				graficaScore.xlim(1, StateManager.numIteraciones);
				graficaScore.ylim(1, 100);
				graficaScore.xlabel("Epoca de Training");                  
				graficaScore.ylabel("Resultado % Score Partida");                 
				graficaScore.saveas(nombreFich, 1280, 720);
				
				File file = new File( nombreFich );
				try {
					Desktop.getDesktop().open( file );
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
			
			guardaScoreTraining(YScore);
				
			
			if(testingAfterTraining) // Probar todos los niveles
			{
				visuals = verbose;
				double[] scorePartidas = new double[nivelesTest.length];
				
				stateManager = new StateManager("TablaQResumen.csv", verbose);
				for (int i = 0; i < nivelesTest.length; i++) {
					levelIdx = nivelesTest[i]; // level names from 0 to 4 (game_lvlN.txt).
					level1 = game.replace(gameName, gameName + "_lvl" + levelIdx);
					double puntuacion = ArcadeMachine.runOneGame(game, level1, visuals, QLearningTesting, recordActionsFile, seed, 0)[1];
					scorePartidas[i] = Math.round(puntuacion / maxPuntuacionJuego[levelIdx] *100);
				}
				
				System.out.println("____________________________________________________");
				System.out.println("____________ ESTADÍSTICAS PARTIDAS _________________");
				double total = 0;
				for(int i = 0; i < nivelesTest.length; i++) {
						System.out.println("% JUEGO " + i + " =\t"+ scorePartidas[i]);
						total += scorePartidas[i];
				}
				
				System.out.println("PUNTUACIÓN MEDIA =" + total / nivelesTest.length);
				System.out.println("____________________________________________________");
				
			}
		}
		else // Modo Test, probar el nivel indicado
		{
			stateManager = new StateManager("TablaQResumen.csv", verbose);
			ArcadeMachine.runOneGame(game, level1, visuals, QLearningTesting, recordActionsFile, seed, 0);
			System.out.println("xmax = "+ StateManager.xmax + " xmin = "+ StateManager.xmin);
		}
		
		
		stateManager.getContadoresEstados();
		

		StateManager.pintaQTableResumen();
		
		

		}
    
    
    	private static void guardaScoreTraining(double [] scorePartidas)
    	{

    		/* Creación del fichero de salida */
    	    try (PrintWriter csvFile = new PrintWriter(new File("TrainingScore.csv"))) {
    			
    			
    			StringBuilder buffer = new StringBuilder();
    			buffer.append("Epoca_Training;Score_Partida");
    			buffer.append("\n");
    			
    			for(int i=0; i<scorePartidas.length; i++) {
    				buffer.append( String.valueOf(i+1) );
    				buffer.append(";");
    				buffer.append(Double.toString(scorePartidas[i]).replace('.', ','));
    				buffer.append("\n");
    			}
    			
    			
    			csvFile.write(buffer.toString());
    			

    			
    			csvFile.close();
    			
    	    } catch( Exception ex ) {
    	    	System.out.println(ex.getMessage());
    	    	}
    	}
    }

