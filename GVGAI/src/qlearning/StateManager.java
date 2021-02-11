package qlearning;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Scanner;

import javax.imageio.ImageIO;

import core.game.StateObservation;
import core.game.Observation;
import ontology.Types.ACTIONS;
import tools.Vector2d;

public class StateManager {
	public static double xmax = 519;
	public static double xmin = 25;
	private static double velocidadAnterior=0;
	public static boolean verbose = false;
	//Variables simulacion training
	public static int numIteraciones;
	public static int iteracionActual;
	Random randomGenerator;
	
	//Variables comunes a varias clases
	public static int numCol;
	public static int numFilas;
	
	// Diccionario ESTADOS-BOOLEAN, que indicará si un estado ha sido capturado en imagen
	public static HashMap <ESTADOS, Boolean> diccionarioEstadoCaptura = new HashMap <ESTADOS, Boolean> ();
	
	/* Contenedor de constantes para identificar los estados */
	public static enum ESTADOS {
		SIN_BOLA(0),
		BOLA_IZQDA(0),
		BOLA_DCHA(0),
		BOLA_CENTRO(0),
		NIL(0);

		private int contador; //Cuenta cada vez que se percibe ese estado
		
		ESTADOS(int c) { this.contador = c; }
		
		ESTADOS(){	this.contador = 0; }
		
		public void incrementa() { this.contador++; }
		
		public int getContador(){ return this.contador;}
		
		// Devuelve el enum ESTADOS al que se corresponde la cadena pasada por parametro
		public static ESTADOS buscaEstado(String nombreEstado)
		{
			for(ESTADOS s : ESTADOS.values()) {
				if(s.toString().equals(nombreEstado))
					return s;
			}
			
			return null;
		}
	}
	
	// Acciones posibles
	public static final ACTIONS[] ACCIONES = {ACTIONS.ACTION_USE, ACTIONS.ACTION_LEFT, ACTIONS.ACTION_RIGHT, ACTIONS.ACTION_NIL};
	
	//Direcciones
	private enum DIRECCIONES{ARRIBA, ABAJO, IZQDA, DCHA};
	
	public static HashMap<ParEstadoAccion, Integer> R; // TABLA R
	public static HashMap<ParEstadoAccion, Double> Q; // TABLA Q
		
	/* Variables */
	//private static char mapaObstaculos[][];
	private static Vector2d posActual;
	private int numEstados = ESTADOS.values().length;
	private int numAcciones = ACCIONES.length;
	
	public StateManager(boolean randomTablaQ, boolean verbose) {
		if(verbose) System.out.println("Inicializando tablas Q y R.....");
		
		randomGenerator = new Random();
		inicializaTablaR();
		
		inicializaTablaQ(randomTablaQ);
		
		StateManager.verbose = verbose;
		
		for(ESTADOS estado : StateManager.ESTADOS.values()) {
			diccionarioEstadoCaptura.put(estado, false);
		}
	}
	
	public StateManager(String ficheroTablaQ, boolean verbose)
	{
		if(verbose) System.out.println("Inicializando tablas Q y R.....");
		
		randomGenerator = new Random();
		inicializaTablaR();
		inicializaTablaQ(true);
		cargaTablaQ(ficheroTablaQ);
		
		StateManager.verbose = verbose;
	}

	public static void capturaEstado(String fileName) throws Exception {
		   Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		   Rectangle screenRectangle = new Rectangle(8,2, 240, 390);
		   
		   Robot robot = new Robot();
		   BufferedImage image = robot.createScreenCapture(screenRectangle);
		   ImageIO.write(image, "bmp", new File("./capturas/" + fileName + ".bmp"));
    }
	
// ---------------------------------------------------------------------
//  					METODOS TABLAS APRENDIZAJE
// ---------------------------------------------------------------------
	private void inicializaTablaR()
	{
		R = new HashMap<ParEstadoAccion, Integer>(numEstados*numAcciones);
		
		// Inicializamos todas las recompensas a cero
		// excepto la de obtener gasolina y esquivar obstaculos, que serán premiadas
		
		for (ESTADOS estado: ESTADOS.values()) 
			for(ACTIONS accion : ACCIONES)
			{
				int valorR = 0;
				
//				if(estado.equals(ESTADOS.BOLA_CENTRO))
//					valorR = -50;
//				
//				if(estado.equals(ESTADOS.BOLA_IZQDA))
//					valorR = -50;
//				
//				if(estado.equals(ESTADOS.BOLA_DCHA))
//					valorR = -50;
//				
//				if(estado.equals(ESTADOS.NIL))
//					valorR = -50;
				
				
			
				
				R.put(new ParEstadoAccion(estado,accion), valorR);
			}
		// Castigamos no ir hacia la bola
		R.put(new ParEstadoAccion(ESTADOS.BOLA_IZQDA,ACTIONS.ACTION_RIGHT), -100);
		R.put(new ParEstadoAccion(ESTADOS.BOLA_DCHA,ACTIONS.ACTION_LEFT), -100);
		
		// Premiamos ir hacia la bola
		R.put(new ParEstadoAccion(ESTADOS.BOLA_IZQDA,ACTIONS.ACTION_LEFT), 150);
		R.put(new ParEstadoAccion(ESTADOS.BOLA_DCHA,ACTIONS.ACTION_RIGHT), 150);
		
		// Premiamos iniciar el juego
		R.put(new ParEstadoAccion(ESTADOS.SIN_BOLA,ACTIONS.ACTION_USE), 100);
		
		// Premiamos que se quede en el centro si la bola está alineada
		R.put(new ParEstadoAccion(ESTADOS.BOLA_CENTRO,ACTIONS.ACTION_NIL), 100);
		R.put(new ParEstadoAccion(ESTADOS.NIL,ACTIONS.ACTION_NIL), 100);
	}
	
	/*
	 * Inializamos la TablaQ
	 */
	private void inicializaTablaQ(boolean random)
	{
		Q = new HashMap<ParEstadoAccion, Double>(numEstados*numAcciones);
		
		if(random) {
			/* Inicializamos todos los valores Q a random */
			for (ESTADOS estado: ESTADOS.values()) 
				for(ACTIONS accion : ACCIONES)			
					Q.put(new ParEstadoAccion(estado,accion), (randomGenerator.nextDouble()+1) * 50);
		}
		else {
			/* Inicializamos todos los valores Q a cero */
			for (ESTADOS estado: ESTADOS.values()) 
				for(ACTIONS accion : ACCIONES) {
					Q.put(new ParEstadoAccion(estado,accion), 0.0);
					//System.out.println(estado.toString() + "," + accion.toString() + " = 0.0");
				}
		}
						
	}
	/**
	 * Si no le indicamos el nombre del fichero, usa uno por defecto.
	 */
	public void saveQTable() {
		saveQTable("TablaQ.csv");
	}
	
	/**
	 * Escribe la tabla Q del atributo de la clase en 
	 * el fichero QTable.csv, para poder ser leída en 
	 * una siguiente etapa de aprendizaje.
	 */
	public void saveQTable(String fileName) 
	{
		/* Creación del fichero de salida */
	    try (PrintWriter csvFile = new PrintWriter(new File(fileName))) {
			
			if( verbose ) System.out.println(" GENERANDO EL FICHERO DE LA TABLAQ... ");
			
			StringBuilder buffer = new StringBuilder();
			buffer.append("ESTADOS");
			buffer.append(";");
			
			for( ACTIONS accion : StateManager.ACCIONES ) {
				buffer.append( accion.toString() );
				buffer.append(";");
			}
			
			buffer.append("\n");
			
			for ( ESTADOS estado: ESTADOS.values() ) {
				buffer.append(estado.toString());
				buffer.append(";");

				for( ACTIONS accion : StateManager.ACCIONES ) {
					double value = StateManager.Q.get(new ParEstadoAccion(estado, accion));
					
					buffer.append( '"' + Double.toString(value).replace('.', ',') + '"');
					buffer.append(";");
				}
				
				buffer.append("\n");
			}
			
			csvFile.write(buffer.toString());
			
			if ( verbose ) System.out.println( " FICHERO GENERADO CORRECTAMENTE! " );
			
			csvFile.close();
			
	    } catch( Exception ex ) {
	    	System.out.println(ex.getMessage());
		}
	}
	
	private void cargaTablaQ(String filename) {
		
		/* Creación del fichero de salida */
	    try (Scanner fichero = new Scanner(new File(filename));){
	    	
			if( verbose ) System.out.println(" CARGANDO EL FICHERO DE LA TABLAQ: "+filename);
			
		
	    	
			String linea = fichero.nextLine();
			String [] cabecera = linea.split(";");
			
			ACTIONS[] actions = new ACTIONS[cabecera.length];
						
			for(int i = 1; i<cabecera.length; i++)
			{
				for(ACTIONS a : ACCIONES)
				{
					if(verbose) System.out.println("NOMBRE ACCION: " + a.toString());
					if(a.toString().equals(cabecera[i])) {
						actions[i] = a;
						if(verbose) System.out.println(actions[i] + " = " + a.toString());
						break;
					}
				}
			}
			
			while(fichero.hasNextLine())
			{
				linea = fichero.nextLine();
				
				String [] campos = linea.split(";");
	
				
				//Según el estado
				ESTADOS estado = ESTADOS.buscaEstado(campos[0]);
				
				
				//Por cada celda, le metemos el valor Q reemplazando coma por punto
				for(int i=1; i<campos.length; i++)
					Q.put(new ParEstadoAccion(estado,actions[i]), Double.parseDouble(campos[i].replace(',', '.').replace('"', Character.MIN_VALUE)));
					
			}
			
			fichero.close();
	
	    } catch( Exception ex ) {
	    	System.out.println(ex.getMessage());
		}
	}

	public static ACTIONS getAccionMaxQ(ESTADOS s)
	{
		 ACTIONS[] actions = StateManager.ACCIONES; // Acciones posibles
         ACTIONS accionMaxQ = ACTIONS.ACTION_NIL;
         
         double maxValue = Double.NEGATIVE_INFINITY; // - inf
	        
	        for (int i = 0; i < actions.length; i++) {
	        	
	        	//if(verbose) System.out.print("Actual maxQ<"+ s.toString() + "," );
	        	//if(verbose) System.out.print(actions[i]+"> = ");
	            double value = StateManager.Q.get(new ParEstadoAccion(s, actions[i]));
	            //if(verbose) System.out.println(value);
	 
	            if (value > maxValue) {
	                maxValue = value;
	                accionMaxQ = actions[i];
	            }
	        }

	        if(maxValue == 0) // Inicialmente estan a 0, una random
	        {
	        	
        	  int index = new Random().nextInt(StateManager.ACCIONES.length-1);
	          accionMaxQ = actions[index];
		        
	        }
	        
	        return accionMaxQ;
	}
		
// _____________________________________________________________________
//  METODOS PERCEPCION ESTADOS
//_____________________________________________________________________

	public static ESTADOS getEstadoFuturo(StateObservation obs, ACTIONS action)
	{	
		Vector2d posBola = getPosBolaReal(obs);
		obs.advance(action);
		return getEstado(obs, posBola, getMapaObstaculos(obs));
	}
	
	public static ESTADOS getEstado(StateObservation obs, Vector2d posBolaAnterior, char[][] mapaObstaculos)
	{
		//int vidaActual = obs.getAvatarHealthPoints();
		posActual = obs.getAvatarPosition();
		double desplazamiento = ((double)70*obs.getGameTick()) / 2000.0;

//
//		double desplazamiento = 0;
//		if(obs.getGameTick() < 1333 && obs.getGameTick() >= 666)
//			desplazamiento = 35;
//		else if(obs.getGameTick() >= 1333)
//			desplazamiento = 70;
		
		if(verbose)System.out.println("Desplazamiento: " + desplazamiento);
		posActual.x += desplazamiento;
		//posActual = getIndiceMapa(pos);
		Vector2d posBola = getPosBolaReal(obs);
		double[] celdaPosBola = getCeldaPreciso(posBola, obs.getWorldDimension());
		double[] celdaPosBolaAnterior = getCeldaPreciso(posBolaAnterior, obs.getWorldDimension());
		double[] celdaPosActual = getCeldaPreciso(posActual, obs.getWorldDimension());
		
		double velocidadJugador = obs.getAvatarOrientation().x*obs.getAvatarSpeed();
		double aceleracion = velocidadJugador - velocidadAnterior;
		velocidadAnterior = velocidadJugador;
		
		ACTIONS ultimaAccion = obs.getAvatarLastAction();
		
		if(posBola.x > xmax)
			xmax = posBola.x;
		
		if(posBola.x > 0 && posBola.x < xmin)
			xmin = posBola.x;
		
		
		if (verbose) System.out.println("POS ACTUAL = " + celdaPosActual[0]+"-"+celdaPosActual[1]);
		if(verbose) System.out.println("POSICION REAL: " + obs.getAvatarPosition().toString());		
		if(verbose) System.out.printf("CeldaBolaAnterior = \t{%f, %f}\nCeldaBolaActual = \t{%f, %f} \n", celdaPosBolaAnterior[0],celdaPosBolaAnterior[1],celdaPosBola[0],celdaPosBola[1]);
		if(verbose) System.out.printf("BolaAnterior = \t{%f, %f}\nBolaActual = \t{%f, %f} \n", posBolaAnterior.x,posBolaAnterior.y,posBola.x,posBola.y);
		if(verbose) System.out.println("VELOCIDAD = " + velocidadJugador);
		if(verbose) System.out.println("ULTIMA ACT = " + ultimaAccion);
		if(verbose) System.out.println("ACELERACION = " + aceleracion);
		if(verbose) System.out.println("VELOCIDAD BOLA= " + getVelocidadBola(posBola, posBolaAnterior));
		
		
		
		
		
		// Si hay bola
		if(hayBola(obs))
		{
			double distanciaBola = Math.sqrt(posActual.sqDist(posBola));
			if(verbose) System.out.println("Hay bola DISTANCIA: "+distanciaBola); 
			
			if(estaBajandoBola(celdaPosBola, celdaPosBolaAnterior) && distanciaBola > 30)
			{
				if(verbose) System.out.println("Bola bajando." ); 
				
				double ColSueloBola = getColPredict(obs, posBolaAnterior);
				
				
				return getEstadoTrayectoriaBola(ColSueloBola, velocidadJugador, aceleracion, ultimaAccion);
			}
			else //Bola sube
			{
				
				return getEstadoBola(obs);
			}
					
		}
		else
			return ESTADOS.SIN_BOLA;
			
	}
	
	private static ESTADOS getEstadoBola(StateObservation obs)
	{
		Vector2d posBola = getPosBolaReal(obs);
		
		
		if(posBola.x == posActual.x)
			return ESTADOS.BOLA_CENTRO;
		else if(posBola.x > posActual.x)
			return ESTADOS.BOLA_DCHA;
		else
			return ESTADOS.BOLA_IZQDA;

	}
	
	private static boolean hayBola(StateObservation obs)
	{
		Observation bola; boolean encontrado = false;
		
		for(ArrayList<Observation> lista : obs.getMovablePositions()) {
    		for(Observation objeto : lista)
    			if(objeto.itype == 5) {
    				bola = objeto;
    				encontrado = true;
    				break;
    			}
    		if(encontrado) break;
		}
		
		return encontrado;
	}
	
	static int[] getPosBola(char[][] mapaObstaculos)
	{
		int posBola[] = new int[] {-1,-1};
		boolean encontrado = false;

			for(int i=0; i< numFilas*2-1; i++) {
				for (int j = 2; j < numCol*2-1; j++)  //Quitando los arboles del borde
					if(mapaObstaculos[i][j] == 'X') {
						posBola = new int[]{i,j};
						encontrado = true;
						break;
					}
				if(encontrado) break;
			}
			
		return posBola;
	}

	static double[] getPosBolaPreciso(StateObservation obs)
	{
		boolean encontrado=false;
		Observation bola = null;
		for(ArrayList<Observation> lista : obs.getMovablePositions()) {
    		for(Observation objeto : lista)
    			if(objeto.itype == 5)
    			{
    				encontrado = true;
    				bola = objeto;
    				break;
    			}
    		if(encontrado) break;
		}
		if(!encontrado) 
			return new double[] {-1,-1}; 
		else
			return getCeldaPreciso(bola.position, obs.getWorldDimension());
	}
	
	static Vector2d getPosBolaReal(StateObservation obs)
	{
		boolean encontrado=false;
		Observation bola = null;
		for(ArrayList<Observation> lista : obs.getMovablePositions()) {
    		for(Observation objeto : lista)
    			if(objeto.itype == 5)
    			{
    				encontrado = true;
    				bola = objeto;
    				break;
    			}
    		if(encontrado) break;
		}
		if(!encontrado) 
			return new Vector2d(-1,-1); 
		else
			return bola.position;
	}
	
	private static boolean estaBajandoBola(double[] posBola, double[] posBolaAnterior)
	{
		return (posBola[0] > posBolaAnterior[0]);
	}
	
	private static int getColCorteBola(double[] posBola, double[] posBolaAnterior)
	{
		/*
		 * Recta determinada por los puntos P1{f1,c1} - P2{f1,c2}
		 * Ecuación punto-pendiente
		 * X = FILA ; Y = COL
		 * Pendiente m = (c2-c1) / (f2-f1)
		 * 
		 * C(F) = c1 + m*(F-f1)
		 */

		int colMax = 40;
		int filaMax = 36;
		
		
		double f1 = posBola[0]; double c1 = posBola[1];
		double f2 = posBolaAnterior[0]; double c2 = posBolaAnterior[1];
		
		if(verbose) System.out.printf("P1 = {%f, %f} - P2 = {%f, %f} \n", f1,c1,f2,c2);
		
		double m = (double)(f2-f1) / (double)(c2-c1);
		
		if(verbose) System.out.println("m = ("+f2 + "-" + f1 + ")/("+c2 +"-"+c1+")= "+ m);
		
		double corteOX = m*(0-f1) + c1;
		if(verbose) System.out.println("CorteOX= "+ corteOX);
		// Rebota en la pared izquierda
		if(corteOX < 0)
		{
			if(verbose) System.out.println("Rebota con pared izquierda");
			// Componente x Punto de corte con el eje y 
			double filaCorteOY = -1 * c1 / m + f1;
			m = -1 * m; //Pendiente al rebotar con la pared	
			
			corteOX = m * (0-filaCorteOY);	
		}
		else if(corteOX > colMax) // Rebota en la pared derecha
		{
			if(verbose) System.out.println("Rebota con pared derecha");
			//Componente x Punto de corte con el borde derecho
			double filaCorteBorde = (colMax - c1) / m + f1;
			m = -1 * m; //Pendiente al rebotar con la pared	
			
			corteOX = m * (0-filaCorteBorde) + colMax;
		}
		
		//double angulo = Math.toDegrees(Math.atan(m));
		
		
		
		
		return (int)Math.floor(corteOX);
		
	}

	private static double getColPredict(StateObservation obs, Vector2d posBolaAnterior)
	{
		Vector2d posBola = getPosBolaReal(obs);
		double bolaX = posBola.x;
		double bolaY = (-1) * posBola.y;
		
		double bolaAntX = posBolaAnterior.x;
		double bolaAntY = (-1) * posBolaAnterior.y;
		
		double muroIzqX = xmin; // Recta colisión con muro izqda: 	X = 25
		double muroDchX = xmax;// Recta colisión muro dcha:			X = 525
		double sueloY = -430; // Recta horizontal colisión suelo 	Y = -430
		

		// Pendiente
		double m = (bolaY-bolaAntY) / (bolaX-bolaAntX);
		
		if(verbose) System.out.println("Pendiente m = " + m);
		
		// Punto corte con eje del suelo (X, -430)
		// sueloY - y0 = m *(X-x0)
		// X = (sueloY - y0) / m + x0
		double PCSueloX =  (sueloY - bolaY) / m + bolaX;
		
		// Si se sale de los bordes
		if( PCSueloX > muroDchX) // Rebotará en la pared derecha
		{
			//Punto corte pared x=paredX:
			// Y = m*(paredX - x0) + y0	
			double PCDchY = m * (muroDchX - bolaX) + bolaY;  
			
			if (verbose)System.out.println("Rebotará en la pared derecha en Y="+PCDchY);
			
			//Actualizamos la pendiente, al rebotar cambia el ángulo
			m = -1 * m;
			
			// Calculamos el nuevo punto de corte con el suelo desde que rebota en la pared
			PCSueloX =  (sueloY - PCDchY) / m + muroDchX;
		}
		else if(PCSueloX < muroIzqX)// Rebotará en la pared izqda
		{
			//Punto corte pared x=paredX:
			// Y = m*(paredX - x0) + y0	
			double PCIzqY = m * (muroIzqX - bolaX) + bolaY;  
			
			if (verbose)System.out.println("Rebotará en la pared izqda en Y="+PCIzqY);
			
			//Actualizamos la pendiente, al rebotar cambia el ángulo
			m = -1 * m;
			
			// Calculamos el nuevo punto de corte con el suelo desde que rebota en la pared
			PCSueloX =  (sueloY - PCIzqY) / m + muroIzqX;
		}
		
		double[] PCSueloCelda = getCeldaPreciso(new Vector2d(PCSueloX, -1*sueloY), obs.getWorldDimension());
		
		if (verbose)System.out.printf("Punto bola suelo estimado: \t(%f , %f) - Celda(%f , %f)\n",PCSueloX, -1*sueloY, PCSueloCelda[0], PCSueloCelda[1]) ;
		
		
		return PCSueloX;
		
	}
	
	private static ESTADOS getEstadoTrayectoriaBola(double colCorteBola, double velocidadJugador, double aceleracion, ACTIONS ultimaAccion)
	{
		double colJugador = posActual.x;
		System.out.println(colJugador + ":::::" + colCorteBola);
		//colCorteBola = colCorteBola -10;
		
		
//		if(colJugador >= colCorteBola-20 && colJugador <= colCorteBola+20 && velocidadJugador > 10)
//			return ESTADOS.BOLA_IZQDA;
//		
//		if(colJugador >= colCorteBola-20 && colJugador <= colCorteBola+20 && velocidadJugador < -10)
//			return ESTADOS.BOLA_DCHA;
		
		if(colJugador >= colCorteBola-20 && colJugador <= colCorteBola+20 ) {
			if (velocidadJugador > 5)
				return ESTADOS.BOLA_IZQDA;
			else if (velocidadJugador < -5)
				return ESTADOS.BOLA_DCHA;
			else
				return ESTADOS.BOLA_CENTRO;
		}
		else if( colCorteBola < xmin || colCorteBola > xmax) {
			System.out.println("xmin: " + xmin + " xmax: " + xmax);
			return ESTADOS.NIL;
		}
			
		
		else if(colJugador < colCorteBola)
			return ESTADOS.BOLA_DCHA;
		
		else
			return ESTADOS.BOLA_IZQDA;
	}
	
	private static double getVelocidadBola(Vector2d posBola, Vector2d posBolaAnterior)
	{
		return Math.sqrt(Math.pow((posBola.x - posBolaAnterior.x), 2) + Math.pow((posBola.y - posBolaAnterior.y), 2));
	}

	public void getContadoresEstados()
	{
		System.out.println("____________ CONTADORES ESTADOS _____________________");
		for (ESTADOS s : ESTADOS.values()) {
			
			System.out.println(s.toString() + " : " + s.getContador());
		}
	}
// _____________________________________________________________________
//                    METODOS PERCEPCION MAPA
// _____________________________________________________________________
	
	public static char[][] getMapaObstaculos(StateObservation obs)
	{
		// El desplazamiento de un jugador es en 0.5 casillas
		char[][] mapaObstaculos = new char[numFilas*2][numCol*2];
		
		for(int i=0; i<numFilas*2; i++)
			for(int j=0; j<numCol*2; j++)
				mapaObstaculos[i][j] = ' ';
		
		
	    	for(ArrayList<Observation> lista : obs.getMovablePositions())
	    		for(Observation objeto : lista)
	    		{
	    			
	    			double[] pos = getCeldaPreciso(objeto.position, obs.getWorldDimension()); // Posicion en casilla real 0.5
	    			int [] indicePos = getIndiceMapa(pos); // Indice del mapa
	    		
	    			
	    			//System.out.println(this.mapaObstaculos[pos[0]][pos[1]]);
	    			//System.out.println("Objeto en " + pos[0] + "-" + pos[1] + " = "+ objeto.itype + " REAL: " + objeto.position.toString());

	    			switch(objeto.itype)
					{    					
						case 0:
							mapaObstaculos[indicePos[0]][indicePos[1]] = '|';
							break;
						case 11:
							mapaObstaculos[indicePos[0]][indicePos[1]] = '-';
							break;
						case 5:
							mapaObstaculos[indicePos[0]][indicePos[1]] = 'X';
							System.out.println("Objeto en " + pos[0] + "-" + pos[1] + " = "+ objeto.itype + " REAL: " + objeto.position.toString());
							break;
						default:
							mapaObstaculos[indicePos[0]][indicePos[1]] = '.';
							break;
	    		}
			}
    	
    	return mapaObstaculos;
	}
	
	/*
	 * Obtiene la posicion en filas,col con precisión .5
	 */
	public static double[] getCeldaPreciso(Vector2d vector, Dimension dim) {
		
    	double x = vector.x /  dim.getWidth() * numCol;
    	double y = vector.y /  dim.getHeight() * numFilas;
    	
    	return new double[] {y,x};
	}
	
	/*
	 * Devuelve el indice del mapa de obstaculos que corresponde el parametro de posicion
	 */
	public static int[] getIndiceMapa(double [] pos)
	{
		return new int[]{(int)(pos[0]*2), (int)(pos[1]*2)};
	}
	
	/*
	 * Obtiene la lista de objetos en una direccion y más cercanos a una distancia dada
	 * distancia: unidades de celda ( no 0.5 )
	 */
	public static ArrayList<Character> getObstaculosDireccion(double[] posJugador, DIRECCIONES dir, double distancia, char [][] mapaObstaculos)
	{
		int [] indiceMapaJugador = getIndiceMapa(posJugador);
		int indiceUltimaCasilla;
		ArrayList<Character> objetosDetectados = new ArrayList<Character>();
		
		switch(dir.toString())
		{
			case "ARRIBA":
				
				if(indiceMapaJugador[0] - distancia*2 > 0)// Limitamos el radio de busqueda al limite del mapa
					indiceUltimaCasilla = indiceMapaJugador[0] - ((int)distancia*2);
				else // Se sale del mapa
					indiceUltimaCasilla = 1; 
				
				for(int i=indiceMapaJugador[0]-1; i >= indiceUltimaCasilla; i--) //Desde media fila arriba del jugador hasta la ultima casilla a explorar
					if(mapaObstaculos[i][indiceMapaJugador[1]] == 'X')
						objetosDetectados.add(mapaObstaculos[i][indiceMapaJugador[1]]);
				break;
				
			case "ABAJO":
				
				if(indiceMapaJugador[0] + distancia*2 < numFilas*2)// Limitamos el radio de busqueda al limite del mapa
					indiceUltimaCasilla = indiceMapaJugador[0] + ((int)distancia*2);
				else // Se sale del mapa
					indiceUltimaCasilla = numFilas*2-1; 
				
				for(int i=indiceMapaJugador[0]+1; i <= indiceUltimaCasilla; i++)
					if(mapaObstaculos[i][indiceMapaJugador[1]] == 'X')
						objetosDetectados.add(mapaObstaculos[i][indiceMapaJugador[1]]);
				break;
				
			case "IZQDA":
				
				if(indiceMapaJugador[1] - distancia*2 > 0)// Limitamos el radio de busqueda al limite del mapa
					indiceUltimaCasilla = indiceMapaJugador[1] - ((int)distancia*2);
				else // Se sale del mapa
					indiceUltimaCasilla = 1; 
				
				for(int i=indiceMapaJugador[1]-1; i >= indiceUltimaCasilla; i--) //Desde media fila arriba del jugador hasta la ultima casilla a explorar
					if(mapaObstaculos[indiceMapaJugador[0]][i] == 'X')
						objetosDetectados.add(mapaObstaculos[indiceMapaJugador[0]][i]);
				break;
				
			case "DCHA":
				if(indiceMapaJugador[1] + distancia*2 < numCol*2)// Limitamos el radio de busqueda al limite del mapa
					indiceUltimaCasilla = indiceMapaJugador[1] + ((int)distancia*2);
				else // Se sale del mapa
					indiceUltimaCasilla = numCol*2-1; 
				
				for(int i=indiceMapaJugador[1]+1; i <= indiceUltimaCasilla; i++)
					if(mapaObstaculos[i][indiceMapaJugador[1]] == 'X')
						objetosDetectados.add(mapaObstaculos[indiceMapaJugador[0]][i]);
				break;
				
		}
		return objetosDetectados;
		
	}
	
	/*
	 * Obtiene si hay objetos en una direccion y más cercanos a una distancia dada
	 * distancia: unidades de celda ( no 0.5 )
	 */
	public static boolean hayObstaculosDireccion(double[] posJugador, DIRECCIONES dir, double distancia, char [][] mapaObstaculos)
	{
		int [] indiceMapaJugador = getIndiceMapa(posJugador);
		int indiceUltimaCasilla;
		
		
		switch(dir.toString())
		{
			case "ARRIBA":
				
				if(indiceMapaJugador[0] - distancia*2 > 0)// Limitamos el radio de busqueda al limite del mapa
					indiceUltimaCasilla = indiceMapaJugador[0] - ((int)distancia*2);
				else // Se sale del mapa
					indiceUltimaCasilla = 1; 
				
				for(int i=indiceMapaJugador[0]-1; i >= indiceUltimaCasilla; i--) //Desde media fila arriba del jugador hasta la ultima casilla a explorar
					if(mapaObstaculos[i][indiceMapaJugador[1]] == 'X')
						return true;
				
				if(indiceMapaJugador[1]+1 < numCol*2-1)
					for(int i=indiceMapaJugador[0]-1; i >= indiceUltimaCasilla; i--) //Columna de la derecha
						if(mapaObstaculos[i][indiceMapaJugador[1]+1] == 'X')
							return true;
				
				if(indiceMapaJugador[1]-1 > 1)
					for(int i=indiceMapaJugador[0]-1; i >= indiceUltimaCasilla; i--) //Columna de la izqda
						if(mapaObstaculos[i][indiceMapaJugador[1]-1] == 'X')
							return true;
			
				break;
				
			case "ABAJO":
				
				if(indiceMapaJugador[0] + distancia*2 < numFilas*2)// Limitamos el radio de busqueda al limite del mapa
					indiceUltimaCasilla = indiceMapaJugador[0] + ((int)distancia*2);
				else // Se sale del mapa
					indiceUltimaCasilla = numFilas*2-1; 
				
				for(int i=indiceMapaJugador[0]+1; i <= indiceUltimaCasilla; i++)
					if(mapaObstaculos[i][indiceMapaJugador[1]] == 'X')
						return true;
				
				
				break;
				
			case "IZQDA":
				
				if(indiceMapaJugador[1] - distancia*2 > 0)// Limitamos el radio de busqueda al limite del mapa
					indiceUltimaCasilla = indiceMapaJugador[1] - ((int)distancia*2);
				else // Se sale del mapa
					indiceUltimaCasilla = 1; 
				
				for(int i=indiceMapaJugador[1]-1; i >= indiceUltimaCasilla; i--) //Desde media fila arriba del jugador hasta la ultima casilla a explorar
					if(mapaObstaculos[indiceMapaJugador[0]][i] == 'X')
						return true;
				
				if(indiceMapaJugador[0]-1 > 1)
					for(int i=indiceMapaJugador[1]-1; i >= indiceUltimaCasilla; i--) //Columna de la arriba a la izqda
					{
			
						if(mapaObstaculos[indiceMapaJugador[0]-1][i] == 'X')
							return true;
						
					}
				
				if(indiceMapaJugador[0]+1 < numFilas*2-1)
					for(int i=indiceMapaJugador[1]-1; i >= indiceUltimaCasilla; i--) //Columna de la abajo a la izqda
						if(mapaObstaculos[indiceMapaJugador[0]+1][i] == 'X')
							return true;
				
				break;
				
			case "DCHA":
				if(indiceMapaJugador[1] + distancia*2 < numCol*2 - 1)// Limitamos el radio de busqueda al limite del mapa
					indiceUltimaCasilla = indiceMapaJugador[1] + ((int)distancia*2);
				else // Se sale del mapa
					indiceUltimaCasilla = numCol*2-1; 
				
				for(int i=indiceMapaJugador[1]+1; i <= indiceUltimaCasilla; i++)
					if(mapaObstaculos[indiceMapaJugador[0]][i] == 'X')
						return true;
				
				if(indiceMapaJugador[0]-1 > 1)
					for(int i=indiceMapaJugador[1]+1; i <= indiceUltimaCasilla; i++) //Columna de la arriba a la dcha
						if(mapaObstaculos[indiceMapaJugador[0]-1][i] == 'X')
							return true;
				
				if(indiceMapaJugador[0]+1 < numFilas*2-1)
					for(int i=indiceMapaJugador[1]+1; i <= indiceUltimaCasilla; i++) //Columna de la abajo a la izqda
						if(mapaObstaculos[indiceMapaJugador[0]+1][i] == 'X')
							return true;
				
				
				
				break;
				
		}
		return false;
		
	}
	
// _____________________________________________________________________
//  METODOS VISUALES
//_____________________________________________________________________	
	public static void pintaQTable(ESTADOS s)
	{
		ACTIONS[] actions = StateManager.ACCIONES;

        System.out.println("----------Q TABLE -----------------");
        
        for (int i = 0; i < actions.length; i++) {
        	 System.out.print("Actual Q<"+ s.toString() + "," );
        	 System.out.print(actions[i]+"> = ");
        	
        	double value = StateManager.Q.get(new ParEstadoAccion(s, actions[i]));
        	
            System.out.println(value);
        }
	        
        System.out.println("----------Q TABLE -----------------");
	}
	
	public static void pintaQTableResumen()
	{
		
		ESTADOS[] estados = ESTADOS.values();
		
        System.out.println("____________________ Q TABLE RESUMEN ______________________");
        
        for (int i = 0; i < estados.length; i++) {
        	ACTIONS accion = getAccionMaxQ(estados[i]);
        	double q = StateManager.Q.get(new ParEstadoAccion(estados[i], accion));
        	
        	 System.out.println("maxQ<"+ estados[i].toString() + "," + accion.toString() +"> = "+ q);
      	
        }
	        
        System.out.println("_________________________________________________________");
	}
	
	public static void pintaMapaObstaculos(char [][] mapaObstaculos)
	{
		
		System.out.println("-----------------------------------");
		for(int i=0; i<numFilas*2; i++) {
    		System.out.println();
    		for(int j=0; j<numCol*2; j++)
    			System.out.print(mapaObstaculos[i][j]);
    	}
    	System.out.println();
	}
}