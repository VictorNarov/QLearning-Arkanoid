package qlearning;

import java.util.Random;

import core.game.StateObservation;
import ontology.Types.ACTIONS;
import qlearning.StateManager.DIRECCIONES;
import qlearning.StateManager.ESTADOS;
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

	public ESTADOS getEstado(StateObservation obs, Vector2d posBolaAnterior, char[][] mapaObstaculos)
	{
		ESTADOS estado = ESTADOS.NIL;

		posActual = obs.getAvatarPosition();
		Vector2d posBola = StateManager.getPosBolaReal(obs);

			
		this.golpeaMuro(posBola, posBolaAnterior);
		
		// Percibimos el estado segun el hueco si lo hay
		if(StateManager.huecos.size() > 0)
			estado = StateManager.getEstadoDirHueco(mapaObstaculos);
		
		
		if(StateManager.golpeaBola(posBola, posBolaAnterior))
		{
	
			double scoreActual = obs.getGameScore();
			if(scoreActual == this.scoreAnterior) // No ha roto ningun ladrillo
			{
				this.numVecesSinPuntos++;
				
				if(this.numVecesSinPuntos >= 3) {
					
					if(posReboteLadrillo.equals(DIRECCIONES.IZQDA) && this.posActual.x <= 2*StateManager.xmax/10)
						return ESTADOS.ATRAPADO_IZQDA_CERCA;
					else if(this.posReboteLadrillo.equals(DIRECCIONES.IZQDA) && this.posActual.x > 2*StateManager.xmax/10)
						return ESTADOS.ATRAPADO_IZQDA;
					else if(this.posReboteLadrillo.equals(DIRECCIONES.DCHA) && this.posActual.x >= 8*StateManager.xmax/10 )
						return ESTADOS.ATRAPADO_DCHA_CERCA;
					else if(this.posReboteLadrillo.equals(DIRECCIONES.DCHA) && this.posActual.x < 8*StateManager.xmax/10 )
						return ESTADOS.ATRAPADO_DCHA;
					else
						return ESTADOS.ATRAPADO_MEDIO;				
				}
				else
					return ESTADOS.NO_CONSIGUE_PUNTOS;
		
			} // Gana puntos
			else {
				this.numVecesSinPuntos = 0;
				this.scoreAnterior = scoreActual;
				
				return ESTADOS.CONSIGUE_PUNTOS;
				
				
			}
				
		}
		
		return estado;
		
	}
	
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
