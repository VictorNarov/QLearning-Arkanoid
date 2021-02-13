package tracks.singlePlayer;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.Random;

import qlearning.Grafica;
import qlearning.StateManager;
import qlearning.StateManager.ESTADOS;
import tools.Utils;
import tracks.ArcadeMachine;

public class Test {

    public static void main(String[] args) {
    	
    	String QLearningTraining = "qlearning.TrainingAgent";
    	String QLearningTesting = "qlearning.TestingAgent";


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
	
		
		int levelIdx = 5; // level names from 0 to 4 (game_lvlN.txt).
		String level1 = game.replace(gameName, gameName + "_lvl" + levelIdx);
		StateManager stateManager;
		
//		ArcadeMachine.playOneGame(game, level1, recordActionsFile, 234234234);
		
//		stateManager = new StateManager(true,true);
//		StateManager.numIteraciones = 1; // Numero de partidas a jugar
//		ArcadeMachine.runOneGame(game, level1, visuals, QLearningTraining, recordActionsFile, seed, 0);
//		
		
		boolean training = false; // Modo entrenamiento, crea una nueva tabla Q y juega M partidas aleatorias
		boolean verbose = true; // Mostrar informacion de la partida mientras se ejecuta
		
		if(training)	// Crea la tabla Q a random y juega partidas con acciones aleatorias
		{
			visuals = false;
			boolean testingAfterTraining = true; // Probar todos los niveles despues del entrenamiento
			boolean randomTablaQ = true; // Verdadero: crea la tabla Q con valores random, si no, a cero
			boolean guardarGrafica = true; // Si queremos guardar una imagen de la grafica Ticks/epoca
			stateManager = new StateManager(randomTablaQ,false);
			StateManager.numIteraciones = 50; // Numero de partidas a jugar

			/*
			 * Grafica Aprendizaje Resultado Score / Epoca
			 */
			double [] Y = null;
			double [] X = null;
			Grafica graficaTicks = null;
			
			if(guardarGrafica) {
				graficaTicks = new Grafica();
				
				X = new double[StateManager.numIteraciones]; // Epoca
				Y = new double[StateManager.numIteraciones]; // Resultado Ticks
				
				for (int i = 0; i < X.length; i++) {
					X[i] = i;
				}
			}
			
		
			for (StateManager.iteracionActual = 1; StateManager.iteracionActual <= StateManager.numIteraciones; StateManager.iteracionActual++) {
				levelIdx = new Random().nextInt(5); // level names from 0 to 4 (game_lvlN.txt).
				level1 = game.replace(gameName, gameName + "_lvl" + levelIdx);
				System.out.println("\t\t\t\t\t\t\t\t\t\tIteración " + StateManager.iteracionActual + " / "+ StateManager.numIteraciones);
				System.out.println("\t\t\t\t\t\t\t\t\t\tlevel: " + levelIdx);
				
				double scorePartida = ArcadeMachine.runOneGame(game, level1, visuals, QLearningTraining, recordActionsFile, seed, 0)[1];
				double aciertoPartida = scorePartida/2 / StateManager.numObjetivos * 100; //Acierto porcentual
				System.out.println("Bloques partida = " + StateManager.numObjetivos);
				System.out.println("\t\t\tPartida completada al " + aciertoPartida + " %");
				
				if(guardarGrafica)
					Y[StateManager.iteracionActual-1] = aciertoPartida;
			}
		
			stateManager.saveQTable();
			if(guardarGrafica) {
				String fecha = java.time.LocalDate.now().toString();
				String nombreFich = fecha+"_PoliticaRandom.jpeg";
				
				graficaTicks.plot(X, Y, "-r", 2.0f, "TICKS");
				graficaTicks.RenderPlot(); 
				graficaTicks.title("Resultado partida % Acierto / Epoca de Training");
				graficaTicks.xlim(1, StateManager.numIteraciones);
				graficaTicks.ylim(1, 100);
				graficaTicks.xlabel("Epoca de Training");                  
				graficaTicks.ylabel("Resultado Ticks partida");                 
				graficaTicks.saveas(nombreFich, 640, 480);
				
				File file = new File( nombreFich );
				try {
					Desktop.getDesktop().open( file );
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
				
			
			if(testingAfterTraining) // Probar todos los niveles
			{
				visuals = verbose;
				double[] scorePartidas = new double[7];
				
				stateManager = new StateManager("TablaQ.csv", verbose);
				for (int i = 0; i <= 4; i++) {
				
					levelIdx = i; // level names from 0 to 4 (game_lvlN.txt).
					level1 = game.replace(gameName, gameName + "_lvl" + levelIdx);
					scorePartidas[i] = ArcadeMachine.runOneGame(game, level1, visuals, QLearningTesting, recordActionsFile, seed, 0)[1];
				}
				
				System.out.println("____________________________________________________");
				System.out.println("____________ ESTADÍSTICAS PARTIDAS _________________");
				double total = 0;
				for(int i = 0; i <= 4; i++) {
						System.out.println("SCORE JUEGO " + i + " =\t"+ scorePartidas[i]);
						total += scorePartidas[i];
				}
				
				System.out.println("PUNTUACIÓN MEDIA =\t" + total / 5.0);
				System.out.println("____________________________________________________");
				
			}
		}
		else // Modo Test, probar el nivel indicado
		{
			stateManager = new StateManager("TablaQ.csv", verbose);
			ArcadeMachine.runOneGame(game, level1, visuals, QLearningTesting, recordActionsFile, seed, 0);
			System.out.println("xmax = "+ StateManager.xmax + " xmin = "+ StateManager.xmin);
		}
		
		
		stateManager.getContadoresEstados();
		

		StateManager.pintaQTableResumen();
		
		

		}
    }

