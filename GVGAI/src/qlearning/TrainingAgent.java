package qlearning;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import core.game.Observation;
import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import ontology.Types.ACTIONS;

import tools.Vector2d;

import tools.ElapsedCpuTimer;

public class TrainingAgent extends AbstractPlayer {
	boolean verbose = StateManager.verbose;
	
	/* Parametros del Aprendizaje */
	private double alpha = 0.2; // Factor Exploracion tama�o del paso
	private double gamma = 0.1; // Factor descuento recompensa futura

	
	boolean randomPolicy=true; // RandomPolicy o MaxQ
	
	/* Variables */
	ArrayList<Observation>[] inmov;
	Dimension dim;
	private int numFilas;
	private int numCol;
	private char[][] mapaObstaculos;
	
	/* Variables Q-Learning */
	//private int vidaAnterior;
	private Vector2d posBolaAnterior;
	static int numAccionesPosibles;
    protected Random randomGenerator; // Random generator for the agent
    protected ArrayList<Types.ACTIONS> actions; // List of available actions for the agent

    /**
     * Public constructor with state observation and time due.
     * @param so state observation of the current game.
     * @param elapsedTimer Timer for the controller creation.
     * En el constructor mirar y guardar las cosas estaticas
     */
    public TrainingAgent(StateObservation so, ElapsedCpuTimer elapsedTimer)
    {
    	StateManager.inicializaJuego();
    	if(verbose) System.out.println("______________________\nCOMIENZA LA PARTIDA\n_______________________");
        randomGenerator = new Random();
        actions = so.getAvailableActions();
        inmov = so.getImmovablePositions();
        dim = so.getWorldDimension();

        so.getBlockSize();
        
		numCol = so.getWorldDimension().width / so.getBlockSize();
		numFilas = so.getWorldDimension().height / so.getBlockSize();
		
		if(verbose) System.out.println("DIMENSION MUNDO: " + so.getWorldDimension().toString());
		if(verbose) System.out.println("NUM FILAS = " + numFilas);
		if(verbose) System.out.println("NUM COL = " + numCol);
		
		// Inicializamos el modulo Util
		StateManager.numCol = this.numCol;
		StateManager.numFilas = this.numFilas;
		
		//vidaAnterior = so.getAvatarHealthPoints();
		posBolaAnterior = new Vector2d(-1, -1);
    	numAccionesPosibles = StateManager.ACCIONES.length;
    	
    	
    	// Criterio de selecci�n: epsilon greedy
    	// A medida que transcurre el entrenamiento, aumenta epsilon
    	double epsilon = (double)StateManager.iteracionActual / (double)StateManager.numIteraciones;

    	if(new Random().nextDouble() > epsilon) { // 
    		randomPolicy = true; // Exploraci�n: m�s probable al principio del entrenamiento
    		System.out.println("Epsilon = " + epsilon + "\nAcci�n: random");
    		}
    	
    	else {
    		randomPolicy = false; // Explotaci�n: m�s probable al final del entrenamiento
    		System.out.println("Epsilon = " + epsilon + "\nAcci�n: maxQ(s)");
    	}
    	
    }
    
    /**
     * Picks an action. This function is called every game step to request an
     * action from the player.
     * @param stateObs Observation of the current state.
     * @param elapsedTimer Timer when the action returned is due.
     * @return An action for the current state
     */
    public Types.ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
    	// -----------------------------------------------------------------------
    	// 						01 - PERCEPCI�N DEL ENTORNO
    	// -----------------------------------------------------------------------
    	//int vidaActual = stateObs.getAvatarHealthPoints();

    	//System.out.println(stateObs.getGameTick());
    	
    	// SI no hay bola, la lanza autom�ticamente. (No es objeto de aprendizaje)
    	if(!StateManager.hayBola(stateObs))
    		return ACTIONS.ACTION_USE;
    	
		if(StateManager.contadorNIL >= 300)
			return ACTIONS.ACTION_ESCAPE;
		
    	
    	double[] pos = StateManager.getCeldaPreciso(stateObs.getAvatarPosition(),dim);
    	int[] posJugador = StateManager.getIndiceMapa(pos); // Indice del mapa
    	Vector2d posBola = StateManager.getPosBolaReal(stateObs);
    	

    		
    	
    	//if(verbose) System.out.println("VIDA ACTUAL = "+vidaActual);
    	if(verbose) System.out.println("POSICION = " + posJugador[0] + "-" + posJugador[1]);   	
    	
    	this.mapaObstaculos = StateManager.getMapaObstaculos(stateObs); // Actualizamos el mapa percibido
    	
    	
//    	mapaObstaculos[posJugador[0]][posJugador[1]] = '='; // Marcamos la posicion del jugador
//    	mapaObstaculos[posJugador[0]][posJugador[1]+1] = '=';
//    	mapaObstaculos[posJugador[0]][posJugador[1]-1] = '=';
    	Vector2d posBolaActual = StateManager.getPosBolaReal(stateObs);

    	if(verbose) StateManager.pintaMapaObstaculos(mapaObstaculos);
    	
    	// Percibimos el estado actual e incrementamos su contador
    	String estadoActual = StateManager.getEstado(stateObs, this.posBolaAnterior, this.mapaObstaculos);

    	if(verbose) System.out.println("\t\t\tEstado actual: " + estadoActual.toString());
    	

    	
    	// Captura el estado en ese momento de ejecuci�n //
/*   	
		if ( !StateManager.diccionarioEstadoCaptura.get(estadoActual) ) {
			try {
				Thread.sleep(200);
				StateManager.capturaEstado(estadoActual.name());
				StateManager.diccionarioEstadoCaptura.put(estadoActual, true);
			} catch(Exception ex) {
				System.out.println("Fallo al realizar la captura. " + ex.getMessage());
			}
		}
 */
    	// -----------------------------------------------------------------------
    	// 							ALGORITMO Q LEARNING
    	// -----------------------------------------------------------------------
    	
    	if(verbose)StateManager.pintaQTable(estadoActual);
    	
    	// Seleccionar una entre las posibles acciones desde el estado actual
    	ACTIONS action;
    	
    	
//    	// Criterio de selecci�n: random hasta 1/3 iteraciones
//    	if(StateManager.iteracionActual < StateManager.numIteraciones * 0.3)
//    		randomPolicy = true;
//    	else
//    		randomPolicy = false;
    	
    	
    	
    	// Criterio de selecci�n: random
    	if(randomPolicy) {
	    	
	        int index = randomGenerator.nextInt(numAccionesPosibles);
	        action = StateManager.ACCIONES[index];
    	}
    	else // Criterio seleccion: maxQ
    	{
    		
    		action = StateManager.getAccionMaxQ(estadoActual);
    	}

    	if(verbose) System.out.println("--> DECIDE HACER: " + action.toString());
        
        // Calcular el siguiente estado habiendo eleggetEstadoFuturoido esa acci�n
    	StateObservation stateObsFuture = stateObs.copy();
    	stateObsFuture.advance(action);
    	String estadoSiguiente = StateManager.getEstadoFuturo(stateObsFuture, posBola);
    	if(verbose) System.out.println("ESTADO SIGUIENTE: " + estadoSiguiente);
    
        // Using this possible action, consider to go to the next state
        double q = StateManager.getQ(estadoActual, action);
    	if(verbose) System.out.println("Consulto q actual Q<" + estadoActual.toString() +","+action.toString()+"> = " + q);

        double maxQ = StateManager.maxQ(estadoSiguiente);
        //int r = StateManager.R.get(new ParEstadoAccion(estadoActual, action));
        double r = StateManager.getR(estadoSiguiente, stateObsFuture);
        
        if(verbose) System.out.println("MaxQ ("+estadoSiguiente+ ") ="+ maxQ);
        if(verbose) System.out.println("RECOMPENSA ("+estadoSiguiente+ ") ="+ r);
        
        double value = q + alpha * (r + gamma * maxQ - q);
        
        if(verbose) System.out.println("Q nuevo = " + value);
        
        // Actualizamos la tabla Q
        StateManager.actualizaQ(estadoActual, action, value);
        
        if(verbose) System.out.println("--> DECIDE HACER: " + action.toString());		
		
	  	//if(stateObs.isGameOver()) this.saveQTable(); //Guardamos la tablaQ si termina el juego
	  	
//		if(verbose)
//			try {
//				Thread.sleep(250);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
		
		
        StateManager.estadoAnterior = estadoActual;
		posBolaAnterior = posBolaActual;
		

		
        return action;
    }
	

	

	
	
}