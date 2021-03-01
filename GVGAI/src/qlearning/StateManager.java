package qlearning;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
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
	//private static double velocidadAnterior=0;
	public static boolean verbose = false;
	//Variables simulacion training
	public static int numIteraciones;
	public static int iteracionActual;
	Random randomGenerator;
	
	public static int contadorNIL = 0;
//	static String estadoAnterior = "99999";
	static StateObservation obsAnterior;
	
	// Variables heuristica de recompensas
	static double distanciaAnterior = 0;
	static double velocidadAnterior = 0;
	static int vidaAnterior = 0;
	static double distancia = 0;
	static double velocidad = 0;
	static int vida = 0;

	
	//public static int numObjetivos;

	//Variables comunes a varias clases
	public static int numCol;
	public static int numFilas;
	

	// Acciones posibles
	public static final ACTIONS[] ACCIONES = {ACTIONS.ACTION_LEFT, ACTIONS.ACTION_RIGHT, ACTIONS.ACTION_NIL};
	
	//Direcciones
	public enum DIRECCIONES{IZQDA, DCHA, MEDIO};
	
	//public static HashMap<String, Integer> R; // TABLA R
	public static HashMap<String, double[]> Q = new HashMap<String, double[]>(); // TABLA Q
	public static HashMap<String, ACTIONS> QResumen = new HashMap<String, ACTIONS>(); // TABLA Q RESUMEN
	public static HashMap<String, Integer> contadoresEstados = new HashMap<String, Integer>(); 
		
	/* Variables */
	//private static char mapaObstaculos[][];
	//private static Vector2d posActual;
//	private int numEstados = 360;
	static int numAcciones = ACCIONES.length;
	
	// Variables prediccion trayectoria
	private static double desplazamiento = 35;
	static double scoreAnterior = 0;
	static double pendienteAnterior;
	static int numVecesSinPuntos = 0;
	static DIRECCIONES posReboteLadrillo=DIRECCIONES.MEDIO;
	private static boolean primeraVez = true;
	static ArrayList<Integer> huecos;
	
	public StateManager(boolean randomTablaQ, boolean verbose) {
		if(verbose) System.out.println("Inicializando tablas Q y R.....");
		
		randomGenerator = new Random();
		//inicializaTablaR();
		
		//inicializaTablaQ(randomTablaQ);
		
		StateManager.verbose = verbose;
		
//		for(ESTADOS estado : StateManager.ESTADOS.values()) {
//			diccionarioEstadoCaptura.put(estado, false);
//		}
	}
	
	public StateManager(String ficheroTablaQ, boolean verbose)
	{
		if(verbose) System.out.println("Inicializando tablas Q y R.....");
		
		randomGenerator = new Random();
		//inicializaTablaR();
		//inicializaTablaQ(true);
		
		if(ficheroTablaQ.contains("Resumen"))
			cargaTablaQResumen(ficheroTablaQ);
		else
			cargaTablaQ(ficheroTablaQ);
		
		StateManager.verbose = verbose;
	}

	/*
	 * Reinicializa las variables en un cambio de partida
	 */
	public static void inicializaJuego()
	{
		//velocidadAnterior=0;
		xmax = 519;
		xmin = 25;
		desplazamiento = 35;
		scoreAnterior = 0;
		numVecesSinPuntos = 0;
		contadorNIL = 0;
		primeraVez = true;
	}
	
	public static void capturaEstado(String fileName) throws Exception {
		   Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		   Rectangle screenRectangle = new Rectangle(8,2, 585, 535);
		   
		   Robot robot = new Robot();
		   BufferedImage image = robot.createScreenCapture(screenRectangle);
		   ImageIO.write(image, "bmp", new File("./capturas/" + fileName + ".bmp"));
    }
	
// ---------------------------------------------------------------------
//  					METODOS TABLAS APRENDIZAJE
// ---------------------------------------------------------------------
	static double getR(String estado)
	{
		double recompensa = 0;
		
		double incDistancia = distancia - distanciaAnterior;
		double incVelocidad = velocidad - velocidadAnterior;
		int incVida = vida - vidaAnterior;
		
		if(verbose) System.out.println("INC DISTANCIA: "+ distancia+ " - " +  +distanciaAnterior +"= "+ incDistancia);
		if(verbose) System.out.println("INC VELOCIDAD: "+ velocidad+ " - " +  +velocidadAnterior +"= "+ incVelocidad);
		if(verbose) System.out.println("INC VIDA: "+ vida+ " - " +  +vidaAnterior +"= "+ incVida);
		
		if(incVida < 0 && estado.charAt(2) <= '3') {
			if(verbose)System.out.println("-300: PIERDE VIDA ESTADO CERCA");
			recompensa -= 300;
		}
				
		
		if(estado.charAt(2) >='3' && incDistancia < 0) { //Si se ha acercado a la bola estando lejos
			if(verbose)System.out.println("+75: se ha acercado a la bola");
			recompensa += 75;
			
		}
		else if(incDistancia < 0) { //Si se ha acercado a la bola estando cerca
			if(verbose)System.out.println("+50: se ha acercado a la bola");
			recompensa += 50;
			
		}
		
		if(estado.charAt(2) < '3' && incVelocidad < 0) { // Si ha reducido la velocidad a poca distancia
			recompensa += 35;
			if(verbose)System.out.println("+35: poca distancia y ha reducido velocidad");
		}
		
//		//Si se ha alejado de la bola CASTIGO
//		if(estadoAnterior.charAt(2) >='3' && distancia > distanciaAnterior) { 
//			recompensa -= 75;
//			if(verbose)System.out.println("-75: se ha alejado de la bola ");
//		}
		// No reduce distancia CASTIGO
//		else if(estadoAnterior.charAt(2) >='3' && Math.abs(difDistancia) < 5 && estado.charAt(0) != '1') { 
//				recompensa -= 30;
//				if(verbose)System.out.println("-30: no ha reducido distancia");
//			}
		
//		//Si ha dejado de estar en la zona de golpeo: CASTIGO
//		if(estadoAnterior.charAt(0) == '1' && estado.charAt(0) != '1') {
//			recompensa -= 100;
//			if(verbose)System.out.println("-100: ha dejado de estar en la zona de golpeo");
//		}
		//Si sigue estando en la zona de golpeo: PREMIO
		if(estado.charAt(0) == '1' && Math.abs(incDistancia) <= 10 ) {
			recompensa += 50;
			if(verbose)System.out.println("+50: sigue estando en la zona de golpeo");
		}
		//Si ha entrado en la zona de golpeo: PREMIO
//		else if(estado.charAt(0) == '1' && Math.abs(incDistancia) <= 10) {
//			recompensa += 200;
//			if(verbose)System.out.println("+200: ha entrado en la zona de golpeo");
//		}
		
		// Estar lejos y parado
		if(estado.charAt(2) >= '5' && estado.charAt(3) == '0') {
			recompensa -= 100;
			if(verbose)System.out.println("-100: esta lejos y parado");
		}
		
		// Lejos y empezar a moverse hacia la pelota
	
		if(estado.charAt(2) > '3' && Math.round(Math.abs(incVelocidad)) == 5.0 && estado.charAt(3) == '1' && 
				(estado.charAt(4)=='1' && estado.charAt(0)=='2' || estado.charAt(4)=='0' && estado.charAt(0)=='0')) {
			recompensa += 50;
			if(verbose)System.out.println("+50: estaba lejos y ha empezado a moverse hacia el objetivo");
		}
		
		// Lejos y empezar a moverse en contra de la pelota
		else if(estado.charAt(2) >= '3' && Math.round(Math.abs(incVelocidad)) == 5.0 && estado.charAt(3) == '1' && 
			(estado.charAt(4)=='1' && estado.charAt(0)=='0' || estado.charAt(4)=='0' && estado.charAt(0)=='2')) {
		recompensa -= 100;
		if(verbose)System.out.println("-50: estaba lejos y ha empezado a moverse en contra del objetivo" + Math.round(Math.abs(incVelocidad)));
		}
		
		// Lejos y aumenta velocidad hacia la pelota
		else if(estado.charAt(2) >= '3' && (
				(incVelocidad > 0 && estado.charAt(0)=='2') ||
				(incVelocidad < 0 && estado.charAt(0)=='0'))) {
			
			if(Math.abs(incVelocidad) > 3 && estado.charAt(2) >= '4') {
				recompensa += 100;
				if(verbose)System.out.println("+100: aumenta mucho velocidad estando lejos hacia objetivo ("+incVelocidad+")");
			}
			else if(incDistancia < 0)
			{
				recompensa += 50;
				if(verbose)System.out.println("+50: aumenta velocidad hacia objetivo ("+incVelocidad+")");
			}
			
		}
		// Lejos y aumenta velocidad en contra de la pelota
		else if(estado.charAt(2) >= '3' && (
				(incVelocidad < 0 && estado.charAt(0)=='2') ||
				(incVelocidad > 0 && estado.charAt(0)=='0'))) {
			recompensa -= 100;
			if(verbose)System.out.println("-100: aumenta velocidad en contra del objetivo "+incVelocidad+")");
		}
		
		if(estado.charAt(0) == '1') // Pelota dentro margen de la plataforma
			recompensa +=20;
		
		if(estado.charAt(2) == '3') // Distancia a la pelota cerca
			recompensa += 10;
		
		
		if(estado.charAt(2) == '0') { // Va a golpear con parte izqda
			if(estado.charAt(1) == '0') { // Hueco izqda
				recompensa += 150;
				if(verbose)System.out.println("+75: muy cerca izqda y hueco izqda");
				
				if(Math.abs(incDistancia) <= 10) {
					recompensa += 100; 
					if(verbose)System.out.println("+100: sigue estando muy cerca izqda y hueco izqda");
				}
			}
//			else if(estado.charAt(1) == '1') { // Hueco medio
//				recompensa += 25;
//				if(verbose)System.out.println("+25: muy cerca izqda y hueco medio");
//			}
//			else {
//				recompensa += 20; //Hueco dcha
//				if(verbose)System.out.println("+20: muy cerca izqda y hueco dcha");
//				
//			}
		
		}
		else if(estado.charAt(2) == '1')  // Va a golpear con parte centro
//			if(estado.charAt(1) == '0') { // Hueco izqda
//				recompensa += 25;
//				if(verbose)System.out.println("+25: muy cerca centro y hueco izqda");
//			}
			if(estado.charAt(1) == '1') { // Hueco medio
				recompensa += 150;
				if(verbose)System.out.println("+75: muy cerca centro y hueco centro");
				
				if(Math.abs(incDistancia) <= 10) {
					recompensa += 100;
					if(verbose)System.out.println("+100: sigue estando muy cerca centro y hueco centro");
				}
			
//			}
//			else {
//				recompensa += 25; //Hueco dcha
//				if(verbose)System.out.println("+25: muy cerca centro y hueco dcha");
//			}
		
		}
		else if(estado.charAt(2) == '2')  // Va a golpear con parte derecha
//			if(estado.charAt(1) == '0') { // Hueco izqda
//				recompensa += 20;
//				if(verbose)System.out.println("+20: muy cerca dcha y hueco izqda");
//			}
//			else if(estado.charAt(1) == '1') { // Hueco medio
//				recompensa += 25;
//				if(verbose)System.out.println("+25: muy cerca dcha y hueco centro");
//			}
			if(estado.charAt(1) == '2') {
				recompensa += 150; //Hueco dcha
				if(verbose)System.out.println("+75: muy cerca dcha y hueco dcha");
				
				if(Math.abs(incDistancia) <= 10) {
					recompensa += 100; //Hueco dcha
					if(verbose)System.out.println("+100: sigue estando muy cerca dcha y hueco dcha");
				}
			}
			
			
		
		

		
//		//Distancia cerca y velocidad de aproximación media
//		if(estado.charAt(2) == '3' && estado.charAt(3) == '2') {
//			recompensa += 20;
//			if(verbose)System.out.println("+20: distancia cerca y velocidad media");
//		}
//		
//		//Distancia cerca o menor y velocidad alta: CASTIGO
//		if(estado.charAt(2) <= '3' && estado.charAt(3) == '3') {
//			recompensa -= 75;
//			if(verbose)System.out.println("-75: distancia cerca o menor y velocidad alta");
//		}
//		
//		//Distancia cerca o menor y velocidad muy alta: CASTIGO+
//		else if(estado.charAt(2) <= '3' && estado.charAt(3) == '4'){
//			recompensa -= 100;
//			if(verbose)System.out.println("-100: distancia cerca o menor y velocidad muy alta");
//		}
		
//		//Distancia media  y velocidad baja: PREMIO
//		else if(estado.charAt(2) == '3' && estado.charAt(3) == '1') {
//			recompensa += 30;
//			if(verbose)System.out.println("+30: distancia cerca y velocidad baja");
//		}
		
		// Reducir velocidad y distancia estando cerca: efecto secundario, le da mucho con los extremos
		if(estado.charAt(2) <= '2' && incVelocidad < 0 && incDistancia < 0)
		{
			recompensa += 75;
			if(verbose)System.out.println("+75: distancia  cerca y reduce velocidad y distancia");
		}
		//Distancia muy cerca  y velocidad baja: PREMIO
		else if(estado.charAt(2) <= '2' && estado.charAt(3) == '1') {
			recompensa += 50;
			if(verbose)System.out.println("+50: distancia muy cerca y velocidad baja");
		}
		
		//Distancia muy cerca  y velocidad muy baja: PREMIO+
		else if(estado.charAt(2) <= '2' && estado.charAt(3) == '0') {
			recompensa += 100;
			if(verbose)System.out.println("+100: distancia muy cerca y velocidad muy baja");
		}
		

		
		
		return recompensa;
		

	}
	
	/*
	 * Inializamos la TablaQ
	 */
//	private void inicializaTablaQ(boolean random)
//	{
//		Q = new HashMap<ParEstadoAccion, Double>();
//		
//		if(random) {
//			/* Inicializamos todos los valores Q a random */
//			for (ESTADOS estado: ESTADOS.values()) 
//				for(ACCIONES accion : ACCIONES.values())			
//					Q.put(new ParEstadoAccion(estado,accion), (randomGenerator.nextDouble()+1) * 50);
//		}
//		else {
//			/* Inicializamos todos los valores Q a cero */
//			for (ESTADOS estado: ESTADOS.values()) 
//				for(ACCIONES accion : ACCIONES.values()) {
//					Q.put(new ParEstadoAccion(estado,accion), 0.0);
//					//System.out.println(estado.toString() + "," + accion.toString() + " = 0.0");
//				}
//		}
//						
//	}
	/**
	 * Si no le indicamos el nombre del fichero, usa uno por defecto.
	 */
	public void saveQTable() {
		saveQTable("TablaQ.csv");
	}
	
	/**
	 * Si no le indicamos el nombre del fichero, usa uno por defecto.
	 */
	public void saveQTableResumen() {
		saveQTableResumen("TablaQResumen.csv");
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
			
			for(ACTIONS accion : StateManager.ACCIONES ) {
				buffer.append( accion.toString() );
				buffer.append(";");
			}
			
			buffer.append("\n");
			
			for (String estado: Q.keySet()) {
				buffer.append(estado.toString());
				buffer.append(";");

				for(int i=0; i<numAcciones; i++){
					double value = StateManager.Q.get(estado)[i];
					
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
	
	/**
	 * Escribe la tabla Q del atributo de la clase en 
	 * el fichero QTable.csv, para poder ser leída en 
	 * una siguiente etapa de aprendizaje.
	 */
	public void saveQTableResumen(String fileName) 
	{
		/* Creación del fichero de salida */
	    try (PrintWriter csvFile = new PrintWriter(new File(fileName))) {
			
			if( verbose ) System.out.println(" GENERANDO EL FICHERO DE LA TABLAQ RESUMEN... ");
			
			StringBuilder buffer = new StringBuilder();
			buffer.append("ESTADOS");
			buffer.append(";");
			buffer.append("IND_MEJOR_ACCION");
			buffer.append("\n");
			
			for (String estado: Q.keySet()) {
				buffer.append(estado.toString());
				buffer.append(";");	
				buffer.append(getIndAccion(getAccionMaxQ(estado)));
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
				String estado = campos[0];
				
				
				//Por cada celda, le metemos el valor Q reemplazando coma por punto
				double[] qNuevo = new double[numAcciones];
		
				for(int i=1; i<campos.length; i++)
					qNuevo[i] = Double.parseDouble(campos[i].replace(',', '.').replace('"', Character.MIN_VALUE));
				
				Q.put(estado, qNuevo);
					
					
			}
			
			fichero.close();
	
	    } catch( Exception ex ) {
	    	System.out.println(ex.getMessage());
		}
	}

	private void cargaTablaQResumen(String filename) {
		
		/* Creación del fichero de salida */
	    try (Scanner fichero = new Scanner(new File(filename));){
	    	
			if( verbose ) System.out.println(" CARGANDO EL FICHERO DE LA TABLAQ RESUMEN: "+filename);
				    	
			String linea = fichero.nextLine();
		
			
			while(fichero.hasNextLine())
			{
				linea = fichero.nextLine();
				
				String [] campos = linea.split(";");

				QResumen.put(campos[0], ACCIONES[Integer.valueOf(campos[1])]);
							
			}
			
			fichero.close();
	
	    } catch( Exception ex ) {
	    	System.out.println(ex.getMessage());
		}
	}

	/*
	 * Obtiene la acción de mayor valor Q para el estado pasado por parámetro
	 */
	public static ACTIONS getAccionMaxQ(String s)
	{
		 ACTIONS actions[] = StateManager.ACCIONES; // Acciones posibles
         ACTIONS accionMaxQ = ACTIONS.ACTION_NIL;
         
         double maxValue = Double.NEGATIVE_INFINITY; // - inf
         
        
	        for (int i = 0; i < numAcciones; i++) {
	        	
	        	//if(verbose) System.out.print("Actual maxQ<"+ s.toString() + "," );
	        	//if(verbose) System.out.print(actions[i]+"> = ");
	            double value = getQ(s,ACCIONES[i]);
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
	
	/*
	 * Obtiene el valor de la Tabla Q dado un estado y acción.
	 * Si no existe, crea la fila y devuelve su valor inicial random.
	 */
	static double getQ(String s, ACTIONS a)
	{
		if(StateManager.Q.containsKey(s))  //Existe entrada para el estado actual
			return StateManager.Q.get(s)[StateManager.getIndAccion(a)];
		
		else { // Crea la entrada a random
			if(verbose) System.out.println("Creando nueva entrada Q<"+s+","+a.toString()+">");
			creaQ(s);
			return getQ(s, a);
		}

	}
	
	/*
	 * Crea una fila de la tabla Q a random
	 */
	static void creaQ(String s)
	{
		double[] qNuevo = new double[StateManager.numAcciones];
		
		for (int i = 0; i < qNuevo.length; i++) {
			double valor = new Random().nextDouble()*100;
			
			qNuevo[i] = valor;
		}

		StateManager.Q.put(s, qNuevo); //Cremos la fila en la tabla Q
	}
	
	/*
	 * Actualiza el valor de la tabla Q(estado,accion)
	 */
	static void actualizaQ(String s, ACTIONS a, double value)
	{
		if(!StateManager.Q.containsKey(s))//NO Existe entrada para el estado actual
			creaQ(s);
		
		double [] Qs = StateManager.Q.get(s); // Obtenemos la fila de qs actuales
        Qs[StateManager.getIndAccion(a)] = value; // Actualizamos la casilla 
        StateManager.Q.put(s, Qs); // Actualizamos la fila en la tabla Q
		
	}
	
	/*
	 * Obtiene el valor mayor de Q para el estado pasado por parámetro
	 */
	static double maxQ(String s) {

        double maxValue = Double.MIN_VALUE;
        
        for (int i = 0; i < StateManager.ACCIONES.length; i++) {
        	
        	//if(verbose) System.out.print("maxQ<"+ s.toString() + "," );
        	//if(verbose) System.out.print(actions[i]+"> = ");
            double value = getQ(s,StateManager.ACCIONES[i]);
            //if(verbose) System.out.println(value);
 
            if (value > maxValue)
                maxValue = value;
        }
        
        return maxValue;
    }
		
// _____________________________________________________________________
//  METODOS PERCEPCION ESTADOS
//_____________________________________________________________________

	public static String getEstadoFuturo(StateObservation obs, Vector2d posBolAnt)
	{	
		//Instancia para predecir el estado sin alterar las variables actuales de StateManager
		StatePredict statePredict = new StatePredict(); 
		
		char [][] mapaObs = getMapaObstaculos(obs);
		String estado = statePredict.getEstado(obs, posBolAnt, mapaObs);
		
		
//		int contador = 0;
		//System.out.println("HOLAAA");
/*
		do
		{
			Vector2d posBola = getPosBolaReal(obs);
			//mapaObs = getMapaObstaculos(obs);
			//StateManager.pintaMapaObstaculos(mapaObs);
			estado = statePredict.getEstado(obs, posBola,mapaObs);
			ACTIONS action = statePredict.getMovimiento(obs, posBolAnt, mapaObs);
			obs.advance(action);
			
			posBolAnt = posBola;
			//mapaObs = getMapaObstaculos(obs);
			
			contador++;
			//System.out.println(contador + "\t" + estado + "\t" + action + " equals=" + estado.equals(ESTADOS.NIL) + " over = " + obs.isGameOver());
		}while(estado.equals("") && !obs.isGameOver() && contador <= 500 && estado.toString().startsWith("HUECO"));
		
		 //System.out.println("Calculado estado futuro en "+contador+" iteraciones: "+estado);
*/	
		
		return estado;
	}
	
	public static String getEstado(StateObservation obs, Vector2d posBolaAnterior, char[][] mapaObstaculos)
	{
		StringBuilder estado = new StringBuilder(new String(new char[5]).replace("\0", "9")); //Inicializamos a todo 9
				
		if(primeraVez) { //Localiza los huecos en las filas de osbtauclos del mapa
			 huecos = getHuecos(mapaObstaculos);
			 if(huecos.size() > 0) desplazamiento = getDesplazamientoDir(getDirHueco(mapaObstaculos));
			 //StateManager.numObjetivos = cuentaObjetivos(mapaObstaculos); //Actualiza el numero de objetivos de este mapa
			 primeraVez = false;
		}
		
		
		Vector2d posBola = getPosBolaReal(obs);
		Vector2d posActual = obs.getAvatarPosition();

		double[] celdaPosBola = getCeldaPreciso(posBola, obs.getWorldDimension());
		double[] celdaPosBolaAnterior = getCeldaPreciso(posBolaAnterior, obs.getWorldDimension());
		double[] celdaPosActual = getCeldaPreciso(posActual, obs.getWorldDimension());
		
		double velocidadJugador = obs.getAvatarOrientation().x*obs.getAvatarSpeed();
		StateManager.velocidadAnterior = velocidadJugador;
		StateManager.vidaAnterior = obs.getAvatarHealthPoints();
		//double aceleracion = velocidadJugador - velocidadAnterior;
		//velocidadAnterior = velocidadJugador;
		
		ACTIONS ultimaAccion = obs.getAvatarLastAction();
		
		// Recalculamos el limite de las paredes del mapa
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
		//if(verbose) System.out.println("ACELERACION = " + aceleracion);
		if(verbose) System.out.println("VELOCIDAD BOLA= " + getVelocidadBola(posBola, posBolaAnterior));
		
		
		golpeaMuro(posBola, posBolaAnterior); // Actualizamos la zona donde golpea el muro (si lo hace)
		//System.out.println("huecos" + huecos.size());
		double scoreActual = obs.getGameScore();
		if(scoreActual == scoreAnterior)
			StateManager.contadorNIL++; //Contador de numero iteraciones sin puntos
		else
			StateManager.contadorNIL = 0; 
		
		scoreAnterior = scoreActual;

		// Dígito 1: UBICACIÓN
		// Percibimos el estado segun el hueco si lo hay
		if(huecos.size() > 0 )//&& huecos.size() < 4
			estado.setCharAt(1, getEstadoDirHueco(mapaObstaculos)); 
		else// Si el mapa no tiene huecos o tiene muchos, busca la direccion de la mediana de los objetivos
			estado.setCharAt(1, getEstadoDirObjetivo(mapaObstaculos));
		
		// Digito 0: UBICACIÓN POS INTERÉS PELOTA
		// Digito 2: DISTANCIA POS INTERÉS
		double distanciaBola = Math.sqrt(posActual.sqDist(posBola));
		if(verbose) System.out.println("Hay bola DISTANCIA: "+distanciaBola); 
		
		if(estaBajandoBola(celdaPosBola, celdaPosBolaAnterior) && distanciaBola > 30)
		{
			if(verbose) System.out.println("Bola bajando." ); 
			
			double ColSueloBola = getColPredict(obs, posBolaAnterior);
			
			//Obtenemos la posicion de la trayectoria y la distancia a la posicion predicha
			char posDistTrayectoriaBolaTrayectoriaBola[] = getEstadoTrayectoriaDistanciaBola(posActual, ColSueloBola);
			
			estado.setCharAt(0, posDistTrayectoriaBolaTrayectoriaBola[0]);
			estado.setCharAt(2, posDistTrayectoriaBolaTrayectoriaBola[1]);
		}
		else //Bola sube
		{
			if(verbose) System.out.println("Bola sube." ); 
			//Obtenemos la posicion de la bola y la distancia en columnas
			char posDistBola[] = getEstadoPosDistBola(posActual, posBola);
			
			estado.setCharAt(0, posDistBola[0]);
			estado.setCharAt(2, posDistBola[1]);
		}
		
		
		
		// Dígito 3: VELOCIDAD DE LA PLATAFORMA
		estado.setCharAt(3, getEstadoVelocidadJugador(velocidadJugador));
		
		// Dígito 4: ORIENTACIÓN DEL DESPLAZAMIENTO PLATAFORMA
		if(obs.getAvatarOrientation().x == 1)
			estado.setCharAt(4,'1'); // Se mueve derecha
		else
			estado.setCharAt(4,'0'); // Se mueve izqda
		
		

		
		String estadoPercibido = estado.toString();
		//Incrementamos su contador
		//System.out.println(estadoPercibido);
		if(contadoresEstados.containsKey(estadoPercibido)) {
			int c = contadoresEstados.get(estadoPercibido);
			contadoresEstados.put(estadoPercibido, ++c);
		}
		else //Primera vez percibido
			contadoresEstados.put(estadoPercibido, 1);
			
		
		return estadoPercibido;
		
	}

	static char[] getEstadoPosDistBola(Vector2d posActual, Vector2d Bola)
	{
		double colBola = Bola.x;	
		double colJugador = posActual.x;
		double longPlataforma = 70;
		double distancia = colBola - colJugador;
		StateManager.distanciaAnterior = Math.abs(distancia);
		char pos;
		char dist;
		
		if(distancia >= 0 && distancia <= longPlataforma)
			pos = '1'; //dentro del margen de la plataforma
		else if(colBola > colJugador)
			pos = '2'; //dcha
		else
			pos = '0'; //izqda
		
		if(Math.abs(distancia) <= 150)
			dist = '3'; // cerca
		else if(Math.abs(distancia) <= 300)
			dist = '4'; // lejos
		else
			dist = '5'; // muy lejos
		
//		
//		if(Math.abs(distancia) <= xmax * 0.15)
//			dist = '3'; // 10% mapa
//		else if(Math.abs(distancia) <= xmax * 0.25)
//			dist = '4'; // 15 % mapa
//		else if(Math.abs(distancia) <= xmax * 0.35)
//			dist = '5'; // 20 % mapa
//		else if(Math.abs(distancia) <= xmax * 0.5)
//			dist = '6'; // 30 % mapa
//		else if(Math.abs(distancia) <= xmax * 0.6)
//			dist = '7'; // 40 % mapa
//		else if(Math.abs(distancia) <= xmax * 0.7)
//			dist = '8'; // 50 % mapa
//		else
//			dist = '9'; // > 50% mapa (MUY LEJOS)

		return new char[] {pos,dist};
		
	}
	
	
	static boolean hayBola(StateObservation obs)
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
	
	static boolean estaBajandoBola(double[] posBola, double[] posBolaAnterior)
	{
		return (posBola[0] > posBolaAnterior[0]);
	}
	
	static double getPendienteBola(Vector2d posBola, Vector2d posBolaAnterior)
	{
		double bolaX = posBola.x;
		double bolaY = (-1) * posBola.y;
		double bolaAntX = posBolaAnterior.x;
		double bolaAntY = (-1) * posBolaAnterior.y;
		

		// Pendiente
		double m = (bolaY-bolaAntY) / (bolaX-bolaAntX);
		
		return m;
	}
	
	private static double[] getCentroideObjetivos(char mapaObstaculos[][])
	{
		double fila = 0;
		double col = 0;
		int contador = 0;
		
		// Buscamos todos los objetivos y vamos acumulado sus coordenadas
		for (int i = 0; i < numCol; i++) 
			for (int j = numFilas-1; j >=0; j--)
				if(mapaObstaculos[j][i] == 'X')
				{
					fila+=j;
					col+=i;
					contador++;
				}
		
		// Devolvemos la media
		return new double[] {fila/contador, col/contador};
							
	}
	
	private static int getMedianaObjetivos(char mapaObstaculos[][])
	{

		ArrayList<Integer> objetivos = new ArrayList<Integer>();
		
		// Buscamos todos los objetivos y vamos acumulado sus coordenadas
		for (int i = 0; i < numCol; i++) 
			for (int j = numFilas-1; j >=0; j--)
				if(mapaObstaculos[j][i] == 'X')
				{
					objetivos.add(i);
				}
		
		if(objetivos.size() > 0) {
			// Devolvemos la mediana
			Collections.sort(objetivos);
			return objetivos.get((int)objetivos.size()/2);
		}
		else return -1;
							
	}
	
	
	/*
	 * Obtiene una lista de todas las columnas de huecos en la fila de obstaculos mas cercana
	 */
	private static ArrayList<Integer> getHuecos(char mapaObstaculos[][])
	{
		ArrayList<Integer> huecos = new ArrayList<Integer>();
		int filaObstaculos = -1;
		
		//Busca la primera fila donde haya una linea de obstaculos
		for (int i = 0; i < numCol; i++) {
			for (int j = numFilas-1; j >=0; j--) {
				if(mapaObstaculos[j][i] == '=') //Ladrillo irrompible
				{
					filaObstaculos = j;

				}
			}
			if(filaObstaculos != -1) break; //Localiza la fila del hueco
		}
		
		
		if(filaObstaculos != -1) //Localiza la fila del hueco
			for (int i = 0; i < numCol; i++)// Añadimos todas las columnas de los huecos de esa fila
				if(mapaObstaculos[filaObstaculos][i] == '.' || mapaObstaculos[filaObstaculos][i] == 'X'){
					huecos.add(i);
				}
		
		if(verbose) System.out.println("Huecos del mapa encontrados en: " +huecos.toString());
		return huecos;
	}
	
	/*
	 * Busca el hueco de ladrillos más cercano al centroide de objetivos
	 */
	private static DIRECCIONES getDirHueco(char mapaObstaculos[][])
	{
		DIRECCIONES dirHueco = null;
		double centroideObjetivos[] = getCentroideObjetivos(mapaObstaculos);
		double minDistancia = Double.POSITIVE_INFINITY;
		int huecoMenorDistancia = -1;
		
		for(int hueco : huecos) {
			double distancia = Math.pow(0 - centroideObjetivos[0], 2) + Math.pow(hueco - centroideObjetivos[1], 2);
			
			if(distancia < minDistancia)
			{
				minDistancia = distancia;
				huecoMenorDistancia = hueco;
			}
		}
		
		int tercio = numCol / 3;
		if(huecoMenorDistancia <= tercio)
			dirHueco = DIRECCIONES.IZQDA;
		
		else if(huecoMenorDistancia > tercio && huecoMenorDistancia < 2*tercio)
			dirHueco = DIRECCIONES.MEDIO;
					
		else dirHueco = DIRECCIONES.DCHA;
					
		if(verbose) System.out.println("Hueco más cercano encontrado en la parte: " + dirHueco);
		return dirHueco;
				

	}
	

	static char getEstadoDirHueco(char mapaObstaculos[][])
	{
		char dirHueco = '9';
		double centroideObjetivos[] = getCentroideObjetivos(mapaObstaculos);
		double minDistancia = Double.POSITIVE_INFINITY;
		int huecoMenorDistancia = -1;
		
		for(int hueco : huecos) {
			double distancia = Math.pow(0 - centroideObjetivos[0], 2) + Math.pow(hueco - centroideObjetivos[1], 2);
			
			if(distancia < minDistancia)
			{
				minDistancia = distancia;
				huecoMenorDistancia = hueco;
			}
		}
		
		int tercio = numCol / 3;
		if(huecoMenorDistancia <= tercio) {
			dirHueco = '0'; // Izqda
			if(verbose) System.out.println("HUECO IZQDA");
		}
		
		else if(huecoMenorDistancia > tercio && huecoMenorDistancia < 2*tercio) {
			dirHueco = '1'; // Centro
			if(verbose) System.out.println("HUECO CENTRO");
		}
					
		else {
			dirHueco = '2'; //Dcha
			if(verbose) System.out.println("HUECO DCHA");
		}
					
		//if(verbose) System.out.println("Hueco más cercano encontrado en la parte: " + dirHueco);
		return dirHueco;
	}
	
	static char getEstadoDirObjetivo(char mapaObstaculos[][])
	{
		char dirObjetivo = '9';
		int mediana = getMedianaObjetivos(mapaObstaculos);
		
		int tercio = numCol / 3;
		if(mediana <= tercio)
			dirObjetivo = '0'; //izqda
		
		else if(mediana > tercio && mediana < 2*tercio)
			dirObjetivo = '1'; //medio
					
		else dirObjetivo = '2'; //dcha
					
		if(verbose) System.out.println("Mediana objetivos encontrado en la parte: " + dirObjetivo);
		return dirObjetivo;
	}
	
	static double getColPredict(StateObservation obs, Vector2d posBolaAnterior)
	{
		Vector2d posBola = getPosBolaReal(obs);
		double bolaX = posBola.x;
		double bolaY = (-1) * posBola.y;
		
		//double bolaAntX = posBolaAnterior.x;
		//double bolaAntY = (-1) * posBolaAnterior.y;
		
		double muroIzqX = xmin; // Recta colisión con muro izqda: 	X = 25
		double muroDchX = xmax;// Recta colisión muro dcha:			X = 525
		double sueloY = -430; // Recta horizontal colisión suelo 	Y = -430
		

		// Pendiente
		double m = getPendienteBola(posBola, posBolaAnterior);
		
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
	
	static char[] getEstadoTrayectoriaDistanciaBola(Vector2d posActual, double colCorteBola)
	{
		double colJugador = posActual.x;
		double longPlataforma = 70.0;
		char posTrayectoriaBola;
		char disTrayectoriaBola;
		
		double distancia = colCorteBola - colJugador;
		StateManager.distanciaAnterior = Math.abs(distancia);
		if(verbose)System.out.println("PREDICCIÓN BOLA: "+ colCorteBola +" ; JUGADOR: "+ colJugador + " ; DISTANCIA = " + distancia);
		
		if(distancia >= 0.0 && distancia <= longPlataforma) // Dentro de la zona de golpeo de la plataforma || distancia < 0 && Math.abs(distancia) <= 20
		{
			posTrayectoriaBola = '1'; //centro
			
			if(distancia <= longPlataforma / 4.0)
				disTrayectoriaBola = '0'; // muy cerca izqda
			else if (distancia <= longPlataforma / 4.0 * 3.0)
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
			
//			if(Math.abs(distancia) <= xmax * 0.15)
//				disTrayectoriaBola = '3'; // 10% mapa
//			else if(Math.abs(distancia) <= xmax * 0.25)
//				disTrayectoriaBola = '4'; // 15 % mapa
//			else if(Math.abs(distancia) <= xmax * 0.35)
//				disTrayectoriaBola = '5'; // 20 % mapa
//			else if(Math.abs(distancia) <= xmax * 0.5)
//				disTrayectoriaBola = '6'; // 30 % mapa
//			else if(Math.abs(distancia) <= xmax * 0.6)
//				disTrayectoriaBola = '7'; // 40 % mapa
//			else if(Math.abs(distancia) <= xmax * 0.7)
//				disTrayectoriaBola = '8'; // 50 % mapa
//			else
//				disTrayectoriaBola = '9'; // > 50% mapa (MUY LEJOS)
			
			if(Math.abs(distancia) <= 150)
				disTrayectoriaBola = '3'; // cerca
			else if(Math.abs(distancia) <= 300)
				disTrayectoriaBola = '4'; // lejos
			else
				disTrayectoriaBola = '5'; // muy lejos
		}
		
		return new char[] {posTrayectoriaBola,disTrayectoriaBola};
		
		//colCorteBola = colCorteBola -10;
		
		
//		if(colJugador >= colCorteBola-20 && colJugador <= colCorteBola+20 && velocidadJugador > 10)
//			return ESTADOS.BOLA_IZQDA;
//		
//		if(colJugador >= colCorteBola-20 && colJugador <= colCorteBola+20 && velocidadJugador < -10)
//			return ESTADOS.BOLA_DCHA;
		
//		if(colJugador >= colCorteBola-25 && colJugador <= colCorteBola+25 ) {
//			if (velocidadJugador > 5)
//				return '0';//izqda
//			else if (velocidadJugador < -5)
//				return '2';//dcha
//			else
//				return '1';//centro
//		}
//		else if( colCorteBola < xmin || colCorteBola > xmax) {
//			if(verbose)System.out.println("xmin: " + xmin + " xmax: " + xmax);
//			return '1';//centro
//		}
//			
//		else if(colJugador < colCorteBola)
//			return '2';//dcha
//		
//		else
//			return '0';//izqda
	}
	
	private static double getVelocidadBola(Vector2d posBola, Vector2d posBolaAnterior)
	{
		return Math.sqrt(Math.pow((posBola.x - posBolaAnterior.x), 2) + Math.pow((posBola.y - posBolaAnterior.y), 2));
	}
	
	private static double getVelocidadJugador(StateObservation obs)
	{
		return obs.getAvatarSpeed() * obs.getAvatarOrientation().x;
	}

	static char getEstadoVelocidadJugador(double velocidad)
	{
		velocidad = Math.abs(velocidad);
		if(velocidad <= 3)
			return '0'; // muy baja
		else if(velocidad <= 5)
			return '1'; //  baja
		else if(velocidad <= 10)
			return '2'; //media
		else if(velocidad <= 15)
			return '3'; // alta
		else
			return '4'; // muy alta
	}
	static boolean golpeaBola(Vector2d posBola, Vector2d posBolaAnterior)
	{
		return(posBola.y >= posBolaAnterior.y  && posBola.y>=420 && posBola.y<=440);
			
	}
	
	/*
	 * Actualiza la variable posReboteLadrillo con la direccion donde la bola cambió de dirección por última vez
	 */
	private static void golpeaMuro(Vector2d posBola, Vector2d posBolaAnterior)
	{
		if(getPendienteBola(posBola,posBolaAnterior) != pendienteAnterior && posBola.y <= 425) {
			double tercio = xmax / 3;
			DIRECCIONES dir=null;
			if(posBola.x <= tercio)
				dir = DIRECCIONES.IZQDA;
			else if(posBola.x > tercio && posBola.x < 2*tercio)
				dir = DIRECCIONES.MEDIO;
			else
				dir = DIRECCIONES.DCHA;
				
			posReboteLadrillo = dir;
			if(verbose) System.out.println("BOLA REBOTA EN LA ZONA: "+dir);
		}
			
		
	}
	
	/*
	 * Obtiene un desplazamiento aleatorio en el rango de direccion pasada por parametro
	 */
	private static double getDesplazamientoDir(DIRECCIONES dir)
	{
		Random r = new Random();
		
		
		if(dir.equals(DIRECCIONES.IZQDA))
			return r.nextInt(10);
		
		else if(dir.equals(DIRECCIONES.DCHA))
			return r.nextInt(10)+60;
		
		else return r.nextInt(20) + 25;
	}
	
	public void getContadoresEstados()
	{
		System.out.println("____________ CONTADORES ESTADOS _____________________");
		for (String s : contadoresEstados.keySet()) {
			
			System.out.println(s.toString() + " : " + contadoresEstados.get(s));
		}
	}
// _____________________________________________________________________
//                    METODOS PERCEPCION MAPA
// _____________________________________________________________________
	
	public static char[][] getMapaObstaculos(StateObservation obs)
	{
		// El desplazamiento de un jugador es en 0.5 casillas
		char[][] mapaObstaculos = new char[numFilas][numCol];
		
		for(int i=0; i<numFilas; i++)
			for(int j=0; j<numCol; j++)
				mapaObstaculos[i][j] = '.';
		
		ArrayList<Observation>[][] listaObs = obs.getObservationGrid();
		for (int i = 0; i < listaObs.length; i++) 
			for (int j = 0; j < listaObs[i].length; j++) 
				if(listaObs[i][j].size() > 0)
					
				{						
						Observation objeto = listaObs[i][j].get(0);
						
						double[] pos = getCeldaPreciso(objeto.position, obs.getWorldDimension()); // Posicion en casilla real 0.5
		    			//int [] indicePos = getIndiceMapa(pos); // Indice del mapa 

						int [] indicePos = new int[] {j,i};
		    			
		    			//System.out.println(this.mapaObstaculos[pos[0]][pos[1]]);
		    			//System.out.println("Objeto en " + pos[0] + "-" + pos[1] + " = "+ objeto.itype + " REAL: " + objeto.position.toString());

		    			switch(objeto.itype)
						{    					
							case 0:// Muro
								mapaObstaculos[indicePos[0]][indicePos[1]] = '|';
								break;
							case 1: // Barra
								mapaObstaculos[indicePos[0]][indicePos[1]] = '-';
								mapaObstaculos[indicePos[0]][indicePos[1]-1] = '-';
								mapaObstaculos[indicePos[0]][indicePos[1]+1] = '-';
								break;
							case 11: // Ladrillo que ocupa dos posiciones
								mapaObstaculos[indicePos[0]][indicePos[1]] = '=';
								mapaObstaculos[indicePos[0]][indicePos[1]+1] = '=';
								break;
							case 5: //Bola cuando baja
							case 4: //Bola cuando sube
								mapaObstaculos[indicePos[0]][indicePos[1]] = 'O';
								if(verbose)System.out.println("Bola en " + pos[0] + "-" + pos[1] + " = "+ objeto.itype + " REAL: " + objeto.position.toString());
								break;
							case 8: // Ladrillos que se rompen x2
								mapaObstaculos[indicePos[0]][indicePos[1]] = 'X';
								mapaObstaculos[indicePos[0]][indicePos[1]+1] = 'X';
								break;
							case 9: // Ladrillo que se rompe x1
								mapaObstaculos[indicePos[0]][indicePos[1]] = 'X';
								break;
							default: //
								//if(verbose)System.out.println("TYPE: " + objeto.itype );
								mapaObstaculos[indicePos[0]][indicePos[1]] = '?';
								
								break;
		    		}	
											
		}
		
		
//    	for(ArrayList<Observation>[] lista : obs.getObservationGrid())
//    		for(ArrayList<Observation> objetos : lista)
//    			for(Observation objeto : objetos)
//	    		{
	    			
	    			
    	
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
		return new int[]{(int)(pos[0]), (int)(pos[1])};
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
	
	/*
	 * Cuenta el numero de bloques objetivo en la partida
	 */
	static int cuentaObjetivos(char mapaObstaculos[][])
	{
		int objetivos = 0;
		for (int i = 0; i < numFilas; i++) {
			for (int j = 0; j < numCol; j++) {
				if(mapaObstaculos[i][j] == 'X')
					objetivos++;
			}
		}
		
		//pintaMapaObstaculos(mapaObstaculos);
		
		return objetivos;
	}
// _____________________________________________________________________
//  METODOS VISUALES
//_____________________________________________________________________	
	public static void pintaQTable(String s)
	{
		
        System.out.println("----------Q TABLE -----------------");
        
        for (int i = 0; i < StateManager.ACCIONES.length; i++) {
        	 System.out.print("Actual Q<"+ s + "," );
        	 System.out.print(StateManager.ACCIONES[i]+"> = ");
        	
        	double value = TestingAgent.getQ(s, ACCIONES[i]);
        	
            System.out.println(value);
        }
	        
        System.out.println("----------Q TABLE -----------------");
	}
	
	public static void pintaQTableResumen()
	{
		
		
        System.out.println("____________________ Q TABLE RESUMEN ______________________");
        
        for (String s : contadoresEstados.keySet()) {
        	ACTIONS accion = getAccionMaxQ(s);
        	double q = StateManager.Q.get(s)[getIndAccion(accion)];
        	
        	 System.out.println("maxQ<"+ s + "," + accion.toString() +"> = "+ q);
      	
        }
	        
        System.out.println("_________________________________________________________");
	}
	
	public static void pintaMapaObstaculos(char [][] mapaObstaculos)
	{
		
		System.out.println("-----------------------------------");
		for(int i=0; i<numFilas; i++) {
    		System.out.println();
    		for(int j=0; j<numCol; j++)
    			System.out.print(mapaObstaculos[i][j]);
    	}
    	System.out.println();
	}
	/*
	 * Obtiene el indice del vector de acciones de la accion pasada por parametro
	 */
	public static int getIndAccion(ACTIONS a)
	{
		int pos = -1;
		int i=0;
		while(pos == -1 && i < ACCIONES.length)
			if(a.equals(ACCIONES[i]))
				pos=i;
			else
				i++;
		return pos;
	}
}