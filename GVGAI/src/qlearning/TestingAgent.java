package qlearning;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Random;

import core.game.Observation;
import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import ontology.Types.ACTIONS;
import qlearning.StateManager.ESTADOS;

import tools.ElapsedCpuTimer;


public class TestingAgent extends AbstractPlayer {
	boolean verbose = StateManager.verbose;
	/* Variables */
	ArrayList<Observation>[] inmov;
	Dimension dim; 
	private int numFilas;
	private int numCol;
	private char[][] mapaObstaculos;
	
	/* Variables Q-Learning */
	private int vidaAnterior;
	int numAccionesPosibles;
	
    protected Random randomGenerator; // Random generator for the agent.
    protected ArrayList<Types.ACTIONS> actions; // List of available actions for the agent

    /**
     * Public constructor with state observation and time due.
     * @param so state observation of the current game.
     * @param elapsedTimer Timer for the controller creation.
     * En el constructor mirar y guardar las cosas estaticas
     */
    public TestingAgent(StateObservation so, ElapsedCpuTimer elapsedTimer)
    {
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
		
		vidaAnterior = so.getAvatarHealthPoints();
    	numAccionesPosibles = StateManager.ACCIONES.length;
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
    	// 						01 - PERCEPCIÓN DEL ENTORNO
    	// -----------------------------------------------------------------------
    	int vidaActual = stateObs.getAvatarHealthPoints();
    	
    	double[] pos = StateManager.getCeldaPreciso(stateObs.getAvatarPosition(),dim);
    	int[] posJugador = StateManager.getIndiceMapa(pos); // Indice del mapa
    	
    	if(verbose) System.out.println("VIDA ACTUAL = "+vidaActual);
    	if(verbose) System.out.println("POSICION = " + posJugador[0] + "-" + posJugador[1]);
    	
    	this.mapaObstaculos = StateManager.getMapaObstaculos(stateObs); // Actualizamos el mapa percibido
    	mapaObstaculos[posJugador[0]][posJugador[1]] = 'O'; // Marcamos la posicion del jugador

    	if(verbose) StateManager.pintaMapaObstaculos(mapaObstaculos);
    	
    	// Percibimos el estado actual e incrementamos su contador
    	ESTADOS estadoActual = StateManager.getEstado(stateObs, vidaAnterior, this.mapaObstaculos);
    	estadoActual.incrementa();
    	if(verbose) System.out.println("Estado actual: " + estadoActual.toString());
    	
    	// -----------------------------------------------------------------------
    	// 				ALGORITMO Q LEARNING EXPLOTACION DE LA TABLA Q
    	// -----------------------------------------------------------------------
    	if(verbose) StateManager.pintaQTable(estadoActual);
    	
    	// Criterio seleccion: maxQ
    	ACTIONS action = StateManager.getAccionMaxQ(estadoActual);
    	
    	if(verbose) System.out.println("--> DECIDE HACER: " + action.toString());
   
		vidaAnterior = vidaActual;
		
        return action;
    }
} 
    