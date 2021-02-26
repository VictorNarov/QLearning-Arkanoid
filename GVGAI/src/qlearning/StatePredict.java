package qlearning;

import java.util.Random;

import core.game.StateObservation;
import ontology.Types.ACTIONS;
import qlearning.StateManager.DIRECCIONES;

import tools.Vector2d;

public class StatePredict {

	
	private double pendienteAnterior;
	private DIRECCIONES posReboteLadrillo;
	private double scoreAnterior;
	private int numVecesSinPuntos;
	private Vector2d posActual;
	private double desplazamiento;

	public StatePredict()
	{
		this.pendienteAnterior = StateManager.pendienteAnterior;
		this.posReboteLadrillo = StateManager.posReboteLadrillo;
		this.scoreAnterior = StateManager.scoreAnterior;
		this.numVecesSinPuntos = StateManager.numVecesSinPuntos;
	}

	public String getEstado(StateObservation obs, Vector2d posBolaAnterior, char[][] mapaObstaculos)
	{
		StringBuilder estado = new StringBuilder(new String(new char[4]).replace("\0", "9")); //Inicializamos a todo 9
		

		posActual = obs.getAvatarPosition();
		Vector2d posBola = StateManager.getPosBolaReal(obs);
		double velocidadJugador = obs.getAvatarOrientation().x*obs.getAvatarSpeed();
			
		this.golpeaMuro(posBola, posBolaAnterior);
		
		// Dígito 1: UBICACIÓN POS INTERÉS MAPA
		// Percibimos el estado segun el hueco si lo hay
		if(StateManager.huecos.size() > 0)
			estado.setCharAt(1, StateManager.getEstadoDirHueco(mapaObstaculos)); 
		else// Si el mapa no tiene huecos, busca el centroide de los objetivos
			estado.setCharAt(1, StateManager.getEstadoDirObjetivo(mapaObstaculos));
		
		// Digito 0: UBICACIÓN POS INTERÉS PELOTA
		// Digito 2: DISTANCIA POS INTERÉS
		double distanciaBola = Math.sqrt(posActual.sqDist(posBola));
		
		// Esta bajando la bola
		if(posBola.y > posBolaAnterior.y && distanciaBola > 30)
		{
			
			double ColSueloBola = StateManager.getColPredict(obs, posBolaAnterior);
			
			//Obtenemos la posicion de la trayectoria y la distancia a la posicion predicha
			char posDistTrayectoriaBolaTrayectoriaBola[] = StateManager.getEstadoTrayectoriaDistanciaBola(posActual, ColSueloBola);
			
			estado.setCharAt(0, posDistTrayectoriaBolaTrayectoriaBola[0]);
			estado.setCharAt(2, posDistTrayectoriaBolaTrayectoriaBola[1]);
		}
		else //Bola sube
		{
			
			//Obtenemos la posicion de la bola y la distancia en columnas
			char posDistBola[] = StateManager.getEstadoPosDistBola(posActual, posBola);
			
			estado.setCharAt(0, posDistBola[0]);
			estado.setCharAt(2, posDistBola[1]);
		}
		
		
		
		// Dígito 3: VELOCIDAD DE LA PLATAFORMA
		estado.setCharAt(3, StateManager.getEstadoVelocidadJugador(velocidadJugador));
		


		return estado.toString();
		
	}

	/*
	public ACTIONS getMovimiento(StateObservation obs, Vector2d posBolaAnterior, char[][] mapaObstaculos)
	{
		Vector2d posActual = obs.getAvatarPosition();

		//posActual = getIndiceMapa(pos);
		Vector2d posBola = StateManager.getPosBolaReal(obs);
		double[] celdaPosBola = StateManager.getCeldaPreciso(posBola, obs.getWorldDimension());
		double[] celdaPosBolaAnterior = StateManager.getCeldaPreciso(posBolaAnterior, obs.getWorldDimension());
		double[] celdaPosActual = StateManager.getCeldaPreciso(posActual, obs.getWorldDimension());
		
		double velocidadJugador = obs.getAvatarOrientation().x*obs.getAvatarSpeed();
		//double aceleracion = velocidadJugador - velocidadAnterior;
		//velocidadAnterior = velocidadJugador;
		
		ACTIONS ultimaAccion = obs.getAvatarLastAction();
		
		golpeaMuro(posBola, posBolaAnterior);
		
		//if(huecos.size() > 0) desplazamiento = getDesplazamientoDir(getDirHueco(mapaObstaculos));
		
		
		
		//if(Math.abs(pendienteAnterior) >= 1)
		//	desplazamiento = 0;
			

		posActual.x += this.desplazamiento; // 0 - 70
		
		
		// Si hay bola
		if(StateManager.hayBola(obs))
		{
			double distanciaBola = Math.sqrt(posActual.sqDist(posBola));
			
			if(StateManager.estaBajandoBola(celdaPosBola, celdaPosBolaAnterior) && distanciaBola > 30)
			{
				
				double ColSueloBola = StateManager.getColPredict(obs, posBolaAnterior);
				
				
				return StateManager.getMovimientoTrayectoriaBola(posActual,ColSueloBola, velocidadJugador, ultimaAccion);
			}
			else //Bola sube
			{
				
				return StateManager.getMovimientoBola(obs);
			}
					
		}
		else
			return ACTIONS.ACTION_USE;
			
	}

	
	
	/*
	 * Actualiza la variable posReboteLadrillo con la direccion donde la bola cambió de dirección por última vez
	 */
	private void golpeaMuro(Vector2d posBola, Vector2d posBolaAnterior)
	{
		if(StateManager.getPendienteBola(posBola,posBolaAnterior) != this.pendienteAnterior && posBola.y <= 425) {
			double tercio = StateManager.xmax / 3;
			DIRECCIONES dir=null;
			if(posBola.x <= tercio)
				dir = DIRECCIONES.IZQDA;
			else if(posBola.x > tercio && posBola.x < 2*tercio)
				dir = DIRECCIONES.MEDIO;
			else
				dir = DIRECCIONES.DCHA;
				
			this.posReboteLadrillo = dir;
		}
	}
	
}
