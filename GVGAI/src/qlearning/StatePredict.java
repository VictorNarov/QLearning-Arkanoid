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
		StringBuilder estado = new StringBuilder(new String(new char[6]).replace("\0", "9")); //Inicializamos a todo 9
		

		posActual = obs.getAvatarPosition();
		Vector2d posBola = StateManager.getPosBolaReal(obs);
		double velocidadJugador = obs.getAvatarOrientation().x*obs.getAvatarSpeed();
		StateManager.velocidad = velocidadJugador;
		StateManager.vida = obs.getAvatarHealthPoints();
			
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
			char posDistTrayectoriaBolaTrayectoriaBola[] = this.getEstadoTrayectoriaDistanciaBola(posActual, ColSueloBola);
			
//			if(posDistTrayectoriaBolaTrayectoriaBola[1] <= '2' && posActual.y - posBola.y > 100)
//				posDistTrayectoriaBolaTrayectoriaBola[1] = '3'; // Si no está a punto de darle		
			
			estado.setCharAt(0, posDistTrayectoriaBolaTrayectoriaBola[0]);
			estado.setCharAt(2, posDistTrayectoriaBolaTrayectoriaBola[1]);
		}
		else //Bola sube
		{
			
			//Obtenemos la posicion de la bola y la distancia en columnas
			char posDistBola[] = this.getEstadoPosDistBola(posActual, posBola);
			
			estado.setCharAt(0, posDistBola[0]);
			estado.setCharAt(2, posDistBola[1]);
		}
		
		
		
		// Dígito 3: VELOCIDAD DE LA PLATAFORMA
		estado.setCharAt(3, StateManager.getEstadoVelocidadJugador(velocidadJugador));
		
		// Dígito 4: ORIENTACIÓN DEL DESPLAZAMIENTO PLATAFORMA
		if(obs.getAvatarOrientation().x == 1)
			estado.setCharAt(4,'1'); // Se mueve derecha
		else
			estado.setCharAt(4,'0'); // Se mueve izqda


////		//Dígito 5: MISMA PENDIENTE GOLPEO ANTERIOR
		double pendienteActual = StateManager.getPendienteBola(posBolaAnterior, posBola);
		double[] celdaPosBola =  StateManager.getCeldaPreciso(posBola, obs.getWorldDimension());
		double[] celdaPosBolaAnterior =  StateManager.getCeldaPreciso(posBolaAnterior, obs.getWorldDimension());
		
		if(StateManager.golpeaBola(posBola, posBolaAnterior)){
			
			if(StateManager.contadorNIL >= 100) // Mas de 200 ticks sin puntos
			{
				estado.setCharAt(5,'1'); // Golpea sin conseguir puntos
				//pendientesMalas.add(pendienteActual);
			}
			else if(StateManager.contadorNIL == 0) { // Ha conseguido puntos
				//pendientesMalas.clear();
				estado.setCharAt(5,'2'); 
			}			
		}
		else
			estado.setCharAt(5,'0'); 
			return estado.toString();
		
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
	
	private char[] getEstadoTrayectoriaDistanciaBola(Vector2d posActual, double colCorteBola)
	{
		double colJugador = posActual.x;
		double longPlataforma = 70;
		char posTrayectoriaBola;
		char disTrayectoriaBola;
		
		double distancia = colCorteBola - colJugador;
		StateManager.distancia = Math.abs(distancia);
		
		if(distancia >= 0 && distancia <= longPlataforma || distancia < 0 && Math.abs(distancia) <= 20) // Dentro de la zona de golpeo de la plataforma
		{
			posTrayectoriaBola = '1'; //centro
			
			if(distancia <= longPlataforma / 3)
				disTrayectoriaBola = '0'; // muy cerca izqda
			else if (distancia <= longPlataforma / 3 *2)
				disTrayectoriaBola = '1'; // muy cerca centro
			else
				disTrayectoriaBola = '2'; // muy cerca dcha
		}
		else
		{
			if(colCorteBola < colJugador)
				posTrayectoriaBola = '0'; //izqda
			else
				posTrayectoriaBola = '2'; //dcha
			
			if(Math.abs(distancia) <= StateManager.xmax * 0.15)
				disTrayectoriaBola = '3'; // 10% mapa
			else if(Math.abs(distancia) <= StateManager.xmax * 0.25)
				disTrayectoriaBola = '4'; // 15 % mapa
			else if(Math.abs(distancia) <= StateManager.xmax * 0.35)
				disTrayectoriaBola = '5'; // 20 % mapa
			else if(Math.abs(distancia) <= StateManager.xmax * 0.5)
				disTrayectoriaBola = '6'; // 30 % mapa
			else if(Math.abs(distancia) <= StateManager.xmax * 0.6)
				disTrayectoriaBola = '7'; // 40 % mapa
			else if(Math.abs(distancia) <= StateManager.xmax * 0.7)
				disTrayectoriaBola = '8'; // 50 % mapa
			else
				disTrayectoriaBola = '9'; // > 50% mapa (MUY LEJOS)
		}
		
		return new char[] {posTrayectoriaBola,disTrayectoriaBola};
	}
	
	private char[] getEstadoPosDistBola(Vector2d posActual, Vector2d Bola)
	{
		double colBola = Bola.x;	
		double colJugador = posActual.x;
		double longPlataforma = 70;
		double distancia = colBola - colJugador;
		StateManager.distancia = Math.abs(distancia);
		char pos;
		char dist;
		
		if(distancia >= 0 && distancia <= longPlataforma)
			pos = '1'; //dentro del margen de la plataforma
		else if(colBola > colJugador)
			pos = '2'; //dcha
		else
			pos = '0'; //izqda
		
		if(Math.abs(distancia) <= StateManager.xmax * 0.15)
			dist = '3'; // 10% mapa
		else if(Math.abs(distancia) <= StateManager.xmax * 0.25)
			dist = '4'; // 15 % mapa
		else if(Math.abs(distancia) <= StateManager.xmax * 0.35)
			dist = '5'; // 20 % mapa
		else if(Math.abs(distancia) <= StateManager.xmax * 0.5)
			dist = '6'; // 30 % mapa
		else if(Math.abs(distancia) <= StateManager.xmax * 0.6)
			dist = '7'; // 40 % mapa
		else if(Math.abs(distancia) <= StateManager.xmax * 0.7)
			dist = '8'; // 50 % mapa
		else
			dist = '9'; // > 50% mapa (MUY LEJOS)

		return new char[] {pos,dist};
		
	}
	
}
