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
		OBTENGO_GASOLINA(0),
		ESQUIVO_OBSTACULO(0),
		MUERTE_SEGURA(0),
		//GASOLINA_ARRIBA(0),
		//GASOLINA_ABAJO(0),
		GASOLINA_IZQDA(0),
		GASOLINA_DCHA(0),
		HUECO_ARRIBA(0),
		//HUECO_ABAJO(0),
		HUECO_IZQDA(0),
		HUECO_DCHA(0),
		//OBSTACULO_ARRIBA(0),
		OBSTACULOS_IZQDA(0),
		OBSTACULOS_DCHA(0),
		BORDE_DCHA(0),
		BORDE_IZQDA(0),
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
	public static final ACTIONS[] ACCIONES = {ACTIONS.ACTION_UP,ACTIONS.ACTION_DOWN, ACTIONS.ACTION_LEFT, ACTIONS.ACTION_RIGHT, ACTIONS.ACTION_NIL, ACTIONS.ACTION_USE};
	
	//Direcciones
	private enum DIRECCIONES{ARRIBA, ABAJO, IZQDA, DCHA};
	
	public static HashMap<ParEstadoAccion, Integer> R; // TABLA R
	public static HashMap<ParEstadoAccion, Double> Q; // TABLA Q
		
	/* Variables */
	//private static char mapaObstaculos[][];
	private static int posActual[];
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
				
				if(estado.equals(ESTADOS.OBTENGO_GASOLINA))
					valorR = 100;
				
				else if(estado.equals(ESTADOS.ESQUIVO_OBSTACULO))
					valorR = 75;
				
				else if(estado.equals(ESTADOS.MUERTE_SEGURA))
					valorR = -150;
				
//				else if(estado.toString().contains("HUECO"))
//				{
//					switch (estado.toString().split("_")[1])
//					{
//					case "ARRIBA": 
//						if(!accion.equals(ACTIONS.ACTION_UP))
//							R.put(new ParEstadoAccion(estado,accion), -1000);
//						break;
//						
//					case "ABAJO": 
//						if(!accion.equals(ACTIONS.ACTION_DOWN))
//							R.put(new ParEstadoAccion(estado,accion), -1000);
//						break;
//						
//					case "IZQDA": 
//						if(!accion.equals(ACTIONS.ACTION_LEFT))
//							R.put(new ParEstadoAccion(estado,accion), -1000);
//						break;
//						
//					case "DCHA": 
//						if(!accion.equals(ACTIONS.ACTION_DOWN))
//							R.put(new ParEstadoAccion(estado,accion), -1000);
//						break;
//					}	
//				}
				/* 
				 En nuestro caso, recompensamos más que vaya a coger gasolina,
				 puesto que no sirve de nada que esquive si se queda sin gasolina.
				 */
				
				R.put(new ParEstadoAccion(estado,accion), valorR);
			}
		// Castigamos el suicidio
		R.put(new ParEstadoAccion(ESTADOS.BORDE_IZQDA,ACTIONS.ACTION_LEFT), -100);
		R.put(new ParEstadoAccion(ESTADOS.BORDE_DCHA,ACTIONS.ACTION_RIGHT), -100);
		R.put(new ParEstadoAccion(ESTADOS.OBSTACULOS_IZQDA,ACTIONS.ACTION_LEFT), -100);
		R.put(new ParEstadoAccion(ESTADOS.OBSTACULOS_DCHA,ACTIONS.ACTION_RIGHT), -100);
		//R.put(new ParEstadoAccion(ESTADOS.OBSTACULO_ARRIBA,ACTIONS.ACTION_UP), -100);
		R.put(new ParEstadoAccion(ESTADOS.HUECO_ARRIBA,ACTIONS.ACTION_UP), -50);
		R.put(new ParEstadoAccion(ESTADOS.ESQUIVO_OBSTACULO,ACTIONS.ACTION_UP), -50);
		
		//R.put(new ParEstadoAccion(ESTADOS.HUECO_ABAJO,ACTIONS.ACTION_DOWN), 100);
		R.put(new ParEstadoAccion(ESTADOS.HUECO_ARRIBA,ACTIONS.ACTION_NIL), 100);
		R.put(new ParEstadoAccion(ESTADOS.HUECO_IZQDA,ACTIONS.ACTION_LEFT), 100);
		R.put(new ParEstadoAccion(ESTADOS.HUECO_DCHA,ACTIONS.ACTION_RIGHT), 100);
		
		//R.put(new ParEstadoAccion(ESTADOS.GASOLINA_ABAJO,ACTIONS.ACTION_DOWN), 100);
		//R.put(new ParEstadoAccion(ESTADOS.GASOLINA_ARRIBA,ACTIONS.ACTION_NIL), 100);
		R.put(new ParEstadoAccion(ESTADOS.GASOLINA_IZQDA,ACTIONS.ACTION_LEFT), 100);
		R.put(new ParEstadoAccion(ESTADOS.GASOLINA_DCHA,ACTIONS.ACTION_RIGHT), 100);
		
		R.put(new ParEstadoAccion(ESTADOS.NIL,ACTIONS.ACTION_NIL), 1000);
		R.put(new ParEstadoAccion(ESTADOS.ESQUIVO_OBSTACULO,ACTIONS.ACTION_DOWN), 100);
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
		int vidaActual = obs.getAvatarHealthPoints();
		obs.advance(action);
		return getEstado(obs, vidaActual, getMapaObstaculos(obs));
	}
	
	public static ESTADOS getEstado(StateObservation obs, int vidaAnterior, char[][] mapaObstaculos)
	{
		int vidaActual = obs.getAvatarHealthPoints();
		double [] pos = getCeldaPreciso(obs.getAvatarPosition(), obs.getWorldDimension()); 
		posActual = getIndiceMapa(pos);
		
		if (verbose) System.out.println("POS ACTUAL = " + pos[0]+"-"+pos[1]);
		if(verbose) System.out.println("POSICION REAL: " + obs.getAvatarPosition().toString());		
		
		int distanciaVisionHuecos = 3;
		HashSet<Integer> huecosProximos = getHuecosFila(posActual,distanciaVisionHuecos,mapaObstaculos);
		if(verbose) System.out.println("HUECOS DIST "+ distanciaVisionHuecos+ ": " + huecosProximos.toString());
		
	
	   // ESTADOS DE MUERTE		
		if(obs.isGameOver() || posActual[0]<0 && posActual[1]<0 || estoyRodeadoObstaculos(mapaObstaculos))
			return ESTADOS.MUERTE_SEGURA; // "MUERTE SEGURA"
		
		if(posActual[1] <= 2)
			return ESTADOS.BORDE_IZQDA;
		
		if(posActual[1] >= numCol*2-4)
			return ESTADOS.BORDE_DCHA;
		
		if(!hayObstaculosDireccion(pos, DIRECCIONES.ARRIBA, 1.0, mapaObstaculos) &&
				!hayObstaculosDireccion(pos, DIRECCIONES.IZQDA, 0.5, mapaObstaculos) && 
				!hayObstaculosDireccion(pos, DIRECCIONES.DCHA, 0.5, mapaObstaculos) &&
				(hayObstaculosDireccion(pos, DIRECCIONES.IZQDA, 2, mapaObstaculos) ||
				hayObstaculosDireccion(pos, DIRECCIONES.DCHA, 2, mapaObstaculos)))
			return ESTADOS.ESQUIVO_OBSTACULO;
		
		// Si se acerca el pasillo para sobrevivir
		if(huecosProximos.size() > 0)
			return getEstadoHueco(huecosProximos, posActual);
		
		if(posActual[0] > 0 && posActual[1] > 0) {
			//mapaObstaculos = Util.getMapaObstaculos(obs);
			
			if(vidaActual > vidaAnterior)
				return ESTADOS.OBTENGO_GASOLINA;// "+GASOLINA"
				
			int[] numObstaculosFila = getObstaculosFila(mapaObstaculos);
			int numObstaculosIzqda = numObstaculosFila[0];
			int numObstaculosDcha = numObstaculosFila[1];
			
			if (verbose) System.out.println("N obstaculos izqda = " + numObstaculosIzqda);
			if (verbose) System.out.println("N obstaculos dcha = " + numObstaculosDcha);
			

/*						
			// Percibimos los huecos
			if(posActual[0]-2 > 0) {

				if(mapaObstaculos[posActual[0]-1][posActual[1]] == ' ' && mapaObstaculos[posActual[0]-2][posActual[1]] == ' ')
					return ESTADOS.HUECO_ARRIBA;
			}
			
			if(posActual[0]+1 < numFilas) {
				if(mapaObstaculos[posActual[0]+1][posActual[1]] == ' ')
					return ESTADOS.HUECO_ABAJO;
			}
			
			if(posActual[1]-1 > 0) {
				
				if(mapaObstaculos[posActual[0]][posActual[1]-1] == ' ')
					return ESTADOS.HUECO_IZQDA;
				
			}
			
			if(posActual[0]+1 < numCol) {
			
				if(mapaObstaculos[posActual[0]][posActual[1]+1] == ' ')
					return ESTADOS.HUECO_DCHA;
			}
			
*/
			if(hayObstaculosDireccion(pos, DIRECCIONES.IZQDA, 2.0, mapaObstaculos))
				return ESTADOS.OBSTACULOS_IZQDA;
			
			if(hayObstaculosDireccion(pos, DIRECCIONES.DCHA, 2.0, mapaObstaculos))
				return ESTADOS.OBSTACULOS_DCHA;
			
			int[] posGasolina = getPosGasolina(mapaObstaculos);
			
			if (verbose) System.out.println("POS GASOLINA: " + posGasolina[0] + "-" + posGasolina[1]);
			
			// SI TENGO CAPACIDAD PARA REPOSTAR Y SI HAY GASOLINA, ESTADO SEGUN LA POSICION DE LA GASOLINA
			if(vidaActual < 11 && posGasolina[0] != -1 && posGasolina[1] != -1) { // SI HAY GASOLINA
				ESTADOS estadoGasolina = getEstadoGasolina(posGasolina); // Obtiene el estado en funcion de la posicion de la gasolina
				//if(!estadoGasolina.equals(ESTADOS.GASOLINA_ARRIBA))
					return estadoGasolina;
			}
			
			//if(hayObstaculosDireccion(pos, DIRECCIONES.ARRIBA, 3.0, mapaObstaculos))
			//	return ESTADOS.OBSTACULO_ARRIBA;
			
/*			
			if(numObstaculosDcha >= 1 && numObstaculosIzqda >= 1 || mapaObstaculos[posActual[0]-1][posActual[1]] == ' ' && mapaObstaculos[posActual[0]-2][posActual[1]] == ' ')
				return ESTADOS.ESQUIVO_OBSTACULO; // ESQUIVO OBSTACULOS
			
			if(posActual[0]-1 > numFilas)	
				if(mapaObstaculos[posActual[0]-1][posActual[1]] == 'X' || mapaObstaculos[posActual[0]-2][posActual[1]] == 'X' || mapaObstaculos[posActual[0]-3][posActual[1]] == 'X' || mapaObstaculos[posActual[0]-4][posActual[1]] == 'X')
					return ESTADOS.OBSTACULO_ARRIBA;
			
			if(posActual[1]+1 < numCol)
				if(mapaObstaculos[posActual[0]-1][posActual[1]+1] == 'X' || mapaObstaculos[posActual[0]][posActual[1]+1] == 'X')
					return ESTADOS.OBSTACULOS_DCHA;
			
			if(posActual[1]-1 > 0)
				if(mapaObstaculos[posActual[0]-1][posActual[1]-1] == 'X' || mapaObstaculos[posActual[0]][posActual[1]-1] == 'X')
					return ESTADOS.OBSTACULOS_IZQDA;
*/
					
		}
		
		return ESTADOS.NIL;
	}
	
	/*
	 * Devuelve un array de dos enteros indicando el numero de obstaculos a la izqda y a la derecha del agente
	 */
	private static int[] getObstaculosFila(char[][] mapaObstaculos)
	{	
		int numObstaculosDcha = 0;
		int numObstaculosIzqda = 0;
				
		//Desde la casilla a la derecha hasta el arbol de la derecha
		for (int i = posActual[1]+1; i < numCol*2 -1; i++) {
			// Si en la fila actual, columna i tenemos un obstaculo
			//if (verbose) System.out.println("mirando casilla derecha: " + posActual[0]+"-"+i);
			if(mapaObstaculos[posActual[0]][i] == 'X')
				numObstaculosDcha++; //Incrementamos el contador
		}
		
		//Desde la casilla a la izqda hasta el arbol de la izqda
		for (int i = posActual[1]-1; i > 1; i--) {
			// Si en la fila actual, columna i tenemos un obstaculo
			if(mapaObstaculos[posActual[0]][i] == 'X')
				numObstaculosIzqda++; //Incrementamos el contador
		}
		
		
		return new int[] {numObstaculosIzqda,numObstaculosDcha};
		
	}
		
	private static boolean estoyRodeadoObstaculos(char[][] mapaObstaculos)
	{
		// X X X ||   X 
		//   O   || X O X
		
		boolean cond1=false, cond2=false, cond3=false, cond4=false, cond5=false, cond6=false, cond7=false;
		
		try {
			cond1 = mapaObstaculos[posActual[0]-1][posActual[1]]=='X' || mapaObstaculos[posActual[0]-2][posActual[1]]=='X'; //arriba
			
			cond2 = mapaObstaculos[posActual[0]-1][posActual[1]+1]=='X' || mapaObstaculos[posActual[0]-1][posActual[1]+2]=='X'; //arriba derecha
			cond3 = mapaObstaculos[posActual[0]-1][posActual[1]-1]=='X' || mapaObstaculos[posActual[0]-1][posActual[1]-2]=='X'; //arriba izqda
			cond6 = mapaObstaculos[posActual[0]-2][posActual[1]+1]=='X' || mapaObstaculos[posActual[0]-2][posActual[1]+2]=='X'; //+arriba derecha
			cond7 = mapaObstaculos[posActual[0]-2][posActual[1]-1]=='X' || mapaObstaculos[posActual[0]-2][posActual[1]-2]=='X'; //+arriba izqda
			
			cond4 = mapaObstaculos[posActual[0]][posActual[1]+1]=='X' || mapaObstaculos[posActual[0]][posActual[1]+2]=='X'; // derecha
			cond5 = mapaObstaculos[posActual[0]][posActual[1]-1]=='X' || mapaObstaculos[posActual[0]][posActual[1]-2]=='X';	//izquierda
			
			
			
		} catch(Exception ex) {
			//
		}
		
		return(cond1 && ( (cond2 && cond3 || cond6 && cond7) || (cond4 && cond5) ));
		
		/*return(mapaObstaculos[posActual[0]-1][posActual[1]]=='X' 
				&& ( (mapaObstaculos[posActual[0]-1][posActual[1]+1]=='X'
					&& mapaObstaculos[posActual[0]-1][posActual[1]-1]=='X')
				|| (mapaObstaculos[posActual[0]][posActual[1]-1]=='X'
						&& mapaObstaculos[posActual[0]][posActual[1]-1]=='X') )
				);*/
		
	}
	
	private static int[] getPosGasolina(char[][] mapaObstaculos)
	{
		int posGasolina[] = new int[] {-1,-1};
		boolean encontrado = false;

			for(int i=0; i< numFilas*2-1; i++) {
				for (int j = 2; j < numCol*2-2; j++)  //Quitando los arboles del borde
					if(mapaObstaculos[i][j] == 'G') {
						posGasolina = new int[]{i,j};
						encontrado = true;
						break;
					}
				if(encontrado) break;
			}

		
		return posGasolina;
	}
	
	private static ESTADOS getEstadoGasolina(int [] posGasolina)
	{
		
		/* Misma columna y gasolina por encima */
		//if(posGasolina[1] == posActual[1] && posGasolina[0] <= posActual[0]) 
		//	return ESTADOS.GASOLINA_ARRIBA; 
		
		/* Gasolina a la derecha */
		 if(posGasolina[1] > posActual[1])
			return ESTADOS.GASOLINA_DCHA;
		
		/* Gasolina a la izqda */
		else if(posGasolina[1] < posActual[1])
			return ESTADOS.GASOLINA_IZQDA;
		
		/* Gasolina abajo */
		//else if(posGasolina[0] > posActual[0])
		//	return ESTADOS.GASOLINA_ABAJO;	
		
		return ESTADOS.NIL;
	}
	
	public void getContadoresEstados()
	{
		System.out.println("____________ CONTADORES ESTADOS _____________________");
		for (ESTADOS s : ESTADOS.values()) {
			
			System.out.println(s.toString() + " : " + s.getContador());
		}
	}
	
	private static HashSet<Integer> getHuecosFila(int[] indiceMapaJugador, double distancia, char[][] mapaObstaculos)
	{
		int indiceUltimaCasilla;
		HashSet<Integer> huecosDetectados = new HashSet<Integer>();
		HashSet<Integer> posiblesHuecos = new HashSet<Integer>();
		
		if(indiceMapaJugador[0] - distancia*2 > 0)// Limitamos el radio de busqueda al limite del mapa
			indiceUltimaCasilla = indiceMapaJugador[0] - ((int)distancia*2);
		else // Se sale del mapa
			indiceUltimaCasilla = 1; 
		
		if(verbose) System.out.println("Indice fila pos jugador: " + indiceMapaJugador[0]);
		if(verbose) System.out.println("Indice ultima casilla: "+ indiceUltimaCasilla);
		if(verbose) System.out.println("Maxima col a explorar = " + (numCol*2-4)/2);
		
		boolean huecoValido = true;
		
		for(int iCol=2; iCol <= (numCol*2-4); iCol++) { // De izquierda a derecha del mapa
			huecoValido = true;
			for(int iFila=indiceMapaJugador[0]; iFila >= indiceUltimaCasilla; iFila--)  //Desde fila del jugador hasta la ultima casilla a explorar
				if(mapaObstaculos[iFila][iCol] == 'X')
				{
					huecoValido = false;
					break; // Pasamos a mirar la siguiente columna
					
				}
			if(huecoValido) {
				posiblesHuecos.add(iCol);
				//if(verbose) System.out.println("POSIBLE HUECO cols: " +iCol);
			}
		}
		
		for(int iCol : posiblesHuecos) {	
				
			huecoValido = true;
			// Tenemos una columna de dos posiciones de ancho despejada,
			// hay que ver si el hueco es la subcasilla de la izqda o de la dcha
			if(iCol-1 > 1) {
				for(int iFila1=indiceMapaJugador[0]-1; iFila1 >= indiceUltimaCasilla; iFila1--) {
					if(mapaObstaculos[iFila1][iCol-1] == 'X') {
						//System.out.println("X en "+iFila1 + " "+ (iCol-1) );
						huecoValido = false;
						break; //Encuentra obstaculos pegados al hueco por la izqda							
					}
				}
			}
				
			if(huecoValido)
				if(iCol+1 < numCol*2-2) 
					for(int iFila2=indiceMapaJugador[0]-1; iFila2 >= indiceUltimaCasilla; iFila2--) 
						if(mapaObstaculos[iFila2][iCol+1] == 'X') {
							huecoValido = false;
							break; //Encuentra obstaculos pegados al hueco por la dcha	
					}
					
			if(huecoValido)// Solo es valido si sirve para esquivar un obstaculo
			{
				huecoValido = false;
				
				if(iCol+1 < numCol*2-2) 
					for(int iFila2=indiceMapaJugador[0]-1; iFila2 >= indiceUltimaCasilla; iFila2--) 
						if(mapaObstaculos[iFila2][iCol-2] == 'X') {
							huecoValido = true;
							break; //Encuentra obstaculos pegados al hueco por la dcha	
					}
				
				if(!huecoValido)
					if(iCol+1 < numCol*2-2) 
						for(int iFila2=indiceMapaJugador[0]-1; iFila2 >= indiceUltimaCasilla; iFila2--) 
							if(mapaObstaculos[iFila2][iCol+2] == 'X') {
								huecoValido = true;
								break; //Encuentra obstaculos pegados al hueco por la dcha	
						}
				
				if(huecoValido)
					huecosDetectados.add(iCol); // Si llega aqui es que el hueco está libre por la dcha
			}
				
		
		}	
		
		return huecosDetectados;
	
	}
	
	private static ESTADOS getEstadoHueco(HashSet<Integer> huecos, int[] posActual)
	{
		int indiceMapaHueco=posActual[1];
		boolean encontradoHuecoPosJugador = false;
		
		//Si encuentra un hueco en la misma columna del jugador
		for(int hueco : huecos) 
			if(hueco == posActual[1])
			{
				encontradoHuecoPosJugador = true; // Le damos prioridad
				break;
			}
		
		//Si no, elige el hueco mas cercano al jugador
		if(!encontradoHuecoPosJugador) {
			int difMenor = Integer.MAX_VALUE;
			
			for(int hueco : huecos) {
				int difDistancia = Math.abs(hueco - posActual[1]); //Distancia en indice de columnas hasta el hueco
				
				if(difDistancia < difMenor) { // Si es menor que el minimo actual
					difMenor = difDistancia; // Actualizamos la diferencia
					indiceMapaHueco = hueco; // Guardamos ese posible mejor hueco
				}
				
			}
		}

		/* Misma columna */
		if(indiceMapaHueco == posActual[1]) 
			return ESTADOS.HUECO_ARRIBA; 
		
		/* Hueco a la derecha */
		if(indiceMapaHueco > posActual[1])
			return ESTADOS.HUECO_DCHA;
		
		/* Gasolina a la izqda */
		else if(indiceMapaHueco < posActual[1])
			return ESTADOS.HUECO_IZQDA;
		
		
		return ESTADOS.NIL;
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
	    		
	    			
	    			//System.out.println("Objeto en " + pos[0] + "-" + pos[1] + " = "+ objeto.itype + " REAL: " + objeto.position.toString());
	    			//System.out.println(this.mapaObstaculos[pos[0]][pos[1]]);
	    			
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