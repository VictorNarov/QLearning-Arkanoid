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
	
		
		int levelIdx = 3; // level names from 0 to 4 (game_lvlN.txt).
		String level1 = game.replace(gameName, gameName + "_lvl" + levelIdx);
		StateManager stateManager;
		
//		ArcadeMachine.playOneGame(game, level1, recordActionsFile, 234234234);
		
		stateManager = new StateManager(true,true);
		StateManager.numIteraciones = 1; // Numero de partidas a jugar
		ArcadeMachine.runOneGame(game, level1, visuals, QLearningTraining, recordActionsFile, seed, 0);
		
		
		boolean training = true; // Modo entrenamiento, crea una nueva tabla Q y juega M partidas aleatorias
		boolean verbose = true; // Mostrar informacion de la partida mientras se ejecuta
		
		if(training)	// Crea la tabla Q a random y juega partidas con acciones aleatorias
		{
			visuals = false;
			boolean testingAfterTraining = true; // Probar todos los niveles despues del entrenamiento
			boolean randomTablaQ = true; // Verdadero: crea la tabla Q con valores random, si no, a cero
			boolean guardarGrafica = false; // Si queremos guardar una imagen de la grafica Ticks/epoca
			stateManager = new StateManager(randomTablaQ,false);
			StateManager.numIteraciones = 500; // Numero de partidas a jugar

			/*
			 * Grafica Aprendizaje Resultado Ticks / Epoca
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
				
				double ticksPartida = ArcadeMachine.runOneGame(game, level1, visuals, QLearningTraining, recordActionsFile, seed, 0)[2];
				
				if(guardarGrafica)
					Y[StateManager.iteracionActual-1] = ticksPartida;
			}
		
			stateManager.saveQTable();
			if(guardarGrafica) {
				String fecha = java.time.LocalDate.now().toString();
				String nombreFich = fecha+"_PoliticaRandom.jpeg";
				
				graficaTicks.plot(X, Y, "-r", 2.0f, "TICKS");
				graficaTicks.RenderPlot(); 
				graficaTicks.title("Resultado partida en Ticks / Epoca de Training");
				graficaTicks.xlim(1, StateManager.numIteraciones);
				graficaTicks.ylim(1, 550);
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
				visuals = true;
				double[] ticksPartidas = new double[7];
				
				stateManager = new StateManager("TablaQ.csv", verbose);
				for (int i = 0; i <= 6; i++) {
				
					levelIdx = i; // level names from 0 to 4 (game_lvlN.txt).
					level1 = game.replace(gameName, gameName + "_lvl" + levelIdx);
					ticksPartidas[i] = ArcadeMachine.runOneGame(game, level1, visuals, QLearningTesting, recordActionsFile, seed, 0)[2];
				}
				
				System.out.println("____________________________________________________");
				System.out.println("____________ ESTADISTICAS PARTIDAS _________________");
				double total = 0;
				for(int i = 0; i <= 6; i++) {
						System.out.println("TICKS JUEGO " + i + " =\t"+ ticksPartidas[i]);
						total += ticksPartidas[i];
				}
				
				System.out.println("MEDIA TICKS =\t" + total / 7.0);
				System.out.println("____________________________________________________");
				
			}
		}
		else // Modo Test, probar el nivel indicado
		{
			stateManager = new StateManager("TablaQ.csv", true);
			ArcadeMachine.runOneGame(game, level1, visuals, QLearningTesting, recordActionsFile, seed, 0);
		}
		
		
		stateManager.getContadoresEstados();
		

		StateManager.pintaQTableResumen();
		
		

		}
    }

