import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Scanner;

public class Main {
	static Scanner input = new Scanner(System.in);

	/**
	 * In order to keep the program more uniform,
	 * we're going to be using absolute paths.
	 * This way, the program can easily run on every machine and
	 * through terminal execution or IDE execution
	 */
	static final String ABSOLUTE_PATH = "C:\\Users\\PC\\Desktop\\1º ano Faculdade\\1ºSemestre\\Laboratório e projeto1\\SOSS_Projeto\\Code_ProjetoLAPR1\\";
	static final String GRAPHS_PATH = ABSOLUTE_PATH + "graphs/";
	static final byte LIMIT = 127;

	public static void main(String[] args) throws IOException {
		if(!isTerminalExecution(args)) {
			menuInteractive();
		} else {
			menuNointeractive(args);
		}
	}

	/**
	 * In this function we're going to verify if the program is being executed by terminal or by
	 * the IDE execution.
	 * We're going to verify this by the number of arguments that exist in the execution of the file,
	 * and if their length is greater than 0, then it means the program is being run un the terminal
	 *
	 * @return boolean
	 */
	public static boolean isTerminalExecution(String[] args) {
		return (args.length > 0);
	}

	public static void menuNointeractive(String[] args) throws IOException {
		int filePosition = lookForFilenamePosition(args);

		if(filePosition == -1) {
			System.out.println("Ficheiro em falta");
			System.exit(0);
		}

		File file = null;

		try {
			file = new File(ABSOLUTE_PATH + args[filePosition]);
		} catch(Exception exception) {
			System.out.println("O ficheiro pretendido está em falta");
			System.exit(0);
		}

		double method = 0;
		double h = 0;
		double population = 0;
		double totalDays = 0;

		try {
			method = findValueToParameter(args,"-m");
			h = findValueToParameter(args,"-p");
			population = findValueToParameter(args,"-t");
			totalDays = findValueToParameter(args, "-d");
		} catch(Exception exception) {
			System.out.println("Parâmetro em falta");
			System.exit(0);
		}

		if(!validateInformation(method, h, population, totalDays)) {
			System.out.println("Parâmetro inválido");
			System.exit(0);
		}

		loopThroughUsers(file, method, population, totalDays, h);
	}

	/**
	 * In order to make our user's life easier, we're going to allow him to write the filename
	 * everywhere he/she wants to.
	 * For that, we have to look for the position of the filename in the arguments array and
	 * return it. If that file doesn't exist, then we return -1
	 *
	 * @param args
	 * @return
	 */
	public static int lookForFilenamePosition(String[] args) {
		for (int i = 0; i < args.length; i++) {
			if(args[i].contains(".csv")) return i;
		}

		return -1;
	}

	/**
	 * In this function we're going to get a certain value from the parameter
	 * that we're analyzing.
	 * In example, being the parameter we want to analyze the -m, we're going to
	 * return the value next to it
	 *
	 * @return int
	 */
	public static double findValueToParameter(String[] args, String argument) {
		/**
		 * First, we're going to look for the position of the
		 * argument in the array
		 */
		int position = Arrays.asList(args).indexOf(argument);
		int nextPosition = position + 1;

		/**
		 * As all the arguments come as string, but
		 * we know they're all doubles, we'll convert
		 * our output into a double.
		 *
		 * Easy? Good, have some cake 🍰
		 */
		return Double.parseDouble(args[nextPosition]);
	}

	public static boolean validateInformation(double method, double h, double population, double totalDays) {
		if(method != 1 && method != 2) {
			return false;
		}

		if(h < 0 || h > 1) {
			return false;
		}

		if(population < 0 || totalDays < 0) {
			return false;
		}

		return true;
	}

	public static void loopThroughUsers(File file, double method, double population, double totalDays, double h) throws IOException {
		/**
		 * We're going to take advantage of the readData function in order
		 * to get the data we need for our execution
		 */
		String[][] data = readData(file);
		int lines = countFileLines(file);

		int convertedPopulation = (int) Math.round(population);
		int convertedTotalDays = (int) Math.round(totalDays);

		/**
		 * Please forgive me teacher, for what I've sinned
		 */
		String formattedH = String.valueOf(h).replace(".", "").replace(",", "");

		/**
		 * In this function we're going to loop through all the users in the file
		 * and then generate their respective files and graphs
		 */
		for (int i = 0; i < (lines - 1); i++) {
			String[] userData = data[i];

			double beta = Double.parseDouble(userData[1]);
			double gama = Double.parseDouble(userData[2]);
			double ro = Double.parseDouble(userData[3]);
			double alpha = Double.parseDouble(userData[4]);

			double susceptibleInitial = population - 1;
			double infectedInitial = 1;
			double recoveredInitial = 0;

			String outputFilename = userData[0] + "m" + String.valueOf((int) Math.round(method)) + "p" + formattedH + "t" + String.valueOf((int) Math.round(population)) + "d" + String.valueOf((int) Math.round(totalDays));

			if(method == 1) {
				methodEuler(convertedPopulation, convertedTotalDays, susceptibleInitial, infectedInitial, recoveredInitial, h, beta, gama, ro, alpha, outputFilename);
			} else {
				rK4(convertedPopulation, convertedTotalDays, h, susceptibleInitial, infectedInitial, recoveredInitial, beta, ro, gama, alpha, outputFilename);
			}
		}

		/**
		 * Let's compare the first two users with their graphics
		 */
		String[] firstUserData = data[0];
		String[] secondUserData = data[1];

		String firstOutputFilename = firstUserData[0] + "m" + String.valueOf((int) Math.round(method)) + "p" + formattedH + "t" + String.valueOf((int) Math.round(population)) + "d" + String.valueOf((int) Math.round(totalDays));
		String secondOutputFilename = secondUserData[0] + "m" + String.valueOf((int) Math.round(method)) + "p" + formattedH + "t" + String.valueOf((int) Math.round(population)) + "d" + String.valueOf((int) Math.round(totalDays));

		if(method == 1) {
			graphicComparationUsers(firstUserData[0], secondUserData[0], "Euler", GRAPHS_PATH + firstOutputFilename + ".csv", GRAPHS_PATH + secondOutputFilename + ".csv");
		} else {
			graphicComparationUsers(firstUserData[0], secondUserData[0], "Runge-Kutta", GRAPHS_PATH + firstOutputFilename + ".csv", GRAPHS_PATH + secondOutputFilename + ".csv");
		}
	}

	public static void menuInteractive() throws IOException {
		int method;
		String[][] data;

		System.out.println("Introduza o número total da população: ");
		int population = input.nextInt();

		System.out.println("Introduza o número total de dias a analisar: ");
		int totalDays = input.nextInt();

		System.out.println("Introduza o passo de integração (h):");
		double h = input.nextDouble();

		input.nextLine();

		System.out.println("Introduza o nome do ficheiro associado aos parâmetros:");
		String parametersFile= input.nextLine() + ".csv";

		if(!parametersFile.contains(".csv")) {
			parametersFile = parametersFile + ".csv";
		}

		if(!new File(ABSOLUTE_PATH + parametersFile).isFile()) {
			System.out.println("Ficheiro não existe!");
			System.exit(0);
		}

		File file = new File(ABSOLUTE_PATH + parametersFile);

		int lines = countFileLines(file);

		/**
		 * No matter which option the user chooses,
		 * we'll read the data and add it to the
		 * respective variables
		 */
		data = readData(file);

		int userLine;

		do {
			System.out.println("Introduza o nome associado aos parâmetros: ");
			String name = input.nextLine();

			/**
			 * Look for the line where the name is
			 */
			userLine = lookForName(data, name, lines);

			if (userLine == -1) {
				clearConsole();
				System.out.println("Por favor introduza um nome válido (que exista no ficheiro)");
			}
		} while (userLine == -1);

		/**
		 * After we read all the data we assign them
		 * to their variables as they're divided
		 * in specific positions
		 */
		double beta = Double.parseDouble(data[userLine][1]);
		double gama = Double.parseDouble(data[userLine][2]);
		double ro = Double.parseDouble(data[userLine][3]);
		double alpha = Double.parseDouble(data[userLine][4]);

		String outputFilenameEuler = "Interactive" + data[userLine][0] + "Euler";
		String outputFilenameRk4 = "Interactive" + data[userLine][0] + "RK4";

		double susceptibleInitial = population - 1;
		double infectedInitial = 1;
		double recoveredInitial = 0;

		do {
			clearConsole();

			System.out.print("******************** Método ********************\n");
			System.out.println("Deseja resolver a partir de que método:");
			System.out.println("[1] Método de Euler");
			System.out.println("[2] Método de Runge-Kutta de 4ª ordem");
			System.out.println("[3] Sair");

			System.out.print("Introduza o número do método que deseja utilizar: ");

			method = input.nextInt();

			switch (method) {
				case 1 :
					methodEuler(population, totalDays, susceptibleInitial, infectedInitial, recoveredInitial, h, beta, gama, ro, alpha, outputFilenameEuler);
					graphicComparationMethods(data[userLine][0], outputFilenameEuler + ".csv",outputFilenameRk4 + ".csv");
					break;
				case 2 :
					rK4(population, totalDays, h, susceptibleInitial, infectedInitial, recoveredInitial, beta, ro, gama, alpha, outputFilenameRk4);
					graphicComparationMethods(data[userLine][0], outputFilenameEuler + ".csv",outputFilenameRk4 + ".csv");
					break;
				case 3 :
					System.exit(0);
					break;
				default :
					System.out.println("Número introduzido inválido, tente novamente");
			}
		} while (method != 3);
	}

	/**
	 * In this function we're going to get all the data we read from the
	 * csv file, and we're going to return the line where the name is.
	 *
	 * By default, we return 0, meaning, that the name doesn't exist
	 *
	 * @param data
	 * @param intendedName
	 * @return int
	 */
	public static int lookForName(String[][] data, String intendedName, int lines) {
		for (int i = 0; i < (lines - 1); i++) {
			String lowercaseName = data[i][0].toLowerCase();
			String lowercaseIntendedName = intendedName.toLowerCase();

			/**
			 * Verify if the names are the same, as the name introduced in
			 * the file is in index 0
			 */
			if(lowercaseName.equals(lowercaseIntendedName)) {
				return i;
			}
		}

		return -1;
	}

	/**
	 * In this method we'll be reading all the data
	 * from a file edited by the user
	 */
	public static String[][] readData(File file) throws FileNotFoundException {
		String dataChunk;
		String processedData;

		int rowPlacement;
		int lines = countFileLines(file);

		/**
		 * This bad boy right here is the number of rows we have in a line
		 */
		int rows = 5;

		String[][] data = new String[lines][rows];

		for (int i = 2; i <= lines; i++) {
			rowPlacement = 0;

			dataChunk = findLine(file, i);
			processedData = processData(dataChunk);

			/**
			 * We're going to loop through each string chunk in a line
			 * and add it to our data array, creating that way a nice and
			 * clean matrix with all the info we need.
			 *
			 * The rowPlacement variable is here to place the data in its
			 * specific positions
			 */
			for (String parameter : processedData.split(";")) {
				int line = i - 2;

				data[line][rowPlacement] = parameter;
				rowPlacement++;
			}
		}

		return data;
	}

	public static String findLine(File file, int line) throws FileNotFoundException {
		Scanner read = new Scanner(file);
		String[] lines = new String[LIMIT];
		int counter = 0;

		do {
			lines[counter] = read.nextLine();
			counter++;
		} while(read.hasNextLine());

		read.close();

		/**
		 * If the line doesn't exist in the file,
		 * we're going to return an empty string
		 */
		try {
			return lines[line - 1];
		} catch (Exception exception) {
			return "";
		}
	}

	public static int countFileLines(File file) throws FileNotFoundException {
		Scanner read = new Scanner(file);
		int counter = 0;

		while(read.hasNextLine()) {
			if(read.nextLine().equals("")) {
				break;
			}

			counter++;
		}

		read.close();

		return counter;
	}

	public static String processData(String dataChunk) {
		/**
		 * We're going to take the string with all of its spaces
		 * just so it's easier to get the data from the string
		 * after we split it
		 */
		return dataChunk.replaceAll("\\s", "");
	}

	public static int calculateN(int i, double h) {
		int n = (int) (i/h);

		return n;
	}

	public static double[] eulerEachDay(int daySteps,double beta, double ro, double alpha, double gama,double h, double susceptibleInitial, double infectedInitial, double recoveredInitial) {
		double[] values = new double[3];
		double susceptibles = 0, infected = 0, recovered = 0;

		for (int i = 0; i< daySteps ;i++) {
			susceptibles  = susceptibleInitial + h * derivativeSusceptible(beta, susceptibleInitial, infectedInitial);
			infected = infectedInitial + h * derivativeInfected(ro, beta, susceptibleInitial, infectedInitial, gama, recoveredInitial, alpha);
			recovered = recoveredInitial + h * derivativeRecovered(ro, beta, susceptibleInitial, infectedInitial, gama, recoveredInitial, alpha);

			susceptibleInitial = susceptibles;
			infectedInitial = infected;
			recoveredInitial = recovered;

		}

		values[0] = susceptibles;
		values[1] = infected;
		values[2] = recovered;

		return values;
	}

	public static double[][] methodEuler(int population,int totalDays,double susceptibleInitial,double infectedInitial, double recoveredInitial,double h, double beta, double gama,double ro,double alpha, String outputName) throws IOException {
		String file = GRAPHS_PATH + outputName;

		double[][] valuesDays = new double[5][totalDays];
		double soma = 0;
		valuesDays[0][0]=0;
		valuesDays[1][0]=population-1;
		valuesDays[2][0]=1;
		valuesDays[3][0]=0;
		valuesDays[4][0]=population;

		for (int i = 1; i<totalDays; i++) {
			int daySteps=calculateN(i,h);
			double valuesEachDay[]=eulerEachDay(daySteps,beta,ro,alpha,gama,h,susceptibleInitial,infectedInitial,recoveredInitial);
			valuesDays[0][i]=i;
			valuesDays[1][i]=valuesEachDay[0];
			valuesDays[2][i]=valuesEachDay[1];
			valuesDays[3][i]=valuesEachDay[2];
			soma = valuesEachDay[0]+valuesEachDay[1]+valuesEachDay[2];
			valuesDays[4][i]=soma;

		}

		saveValues(valuesDays,totalDays,file);

		graphic(file + ".txt", file + ".png", "Método de Euler", file + ".csv");

		return valuesDays;
	}

	public static double[][] rK4(int population,int totalDays, double h, double susceptibleInitial, double infectedInitial, double recoveredInitial,double beta,double ro,double gama,double alpha, String outputName) throws IOException {
		double [][] valuesDays = new double[5][totalDays];
		double soma = 0;
		String file = GRAPHS_PATH + outputName;
		valuesDays[0][0]= 0;
		valuesDays[1][0]= population-1;
		valuesDays[2][0]= 1;
		valuesDays[3][0]= 0;
		valuesDays[4][0]= population;

		for (int i=1;i < totalDays;i++){
			int daySteps = calculateN(i,h);

			double[] valuesEachDay = rk4EachDay(daySteps, h, susceptibleInitial, infectedInitial, recoveredInitial, beta, ro, gama, alpha);

			soma = valuesEachDay[0]+valuesEachDay[1]+valuesEachDay[2];

			valuesDays[0][i]= i;
			valuesDays[1][i]= valuesEachDay[0];
			valuesDays[2][i]= valuesEachDay[1];
			valuesDays[3][i]= valuesEachDay[2];
			valuesDays[4][i]= soma;
		}

		saveValues(valuesDays,totalDays,file);

		graphic(file + ".txt", file + ".png", "Método de Runge-Kutta 4", file + ".csv");

		return valuesDays;
	}

	public static double[] rk4EachDay (int daySteps, double h, double susceptibleInitial, double infectedInitial, double recoveredInitial,double beta,double ro,double gama,double alpha){
		double K1S, K1I, K1R;
		double K2S, K2I, K2R;
		double K3S, K3I, K3R;
		double K4S, K4I, K4R;
		double KS, KI, KR;
		double susceptibles=0,infected=0,recovered=0;
		double [] values = new double[3];

		for (int i=0;i < daySteps;i++){
			K1S = calculateKSusceptible(h, beta, susceptibleInitial, infectedInitial);
			K1I = calculateKInfected(h, ro, beta, gama, alpha, susceptibleInitial, infectedInitial, recoveredInitial);
			K1R = calculateKRecovered(h, ro, beta, gama, alpha, susceptibleInitial, infectedInitial, recoveredInitial);

			K2S = calculateKSusceptible(h, beta,  (susceptibleInitial+(K1S/2)),  (infectedInitial+(K1I/2)));
			K2I = calculateKInfected(h, ro, beta, gama, alpha,  (susceptibleInitial+K1S/2),  (infectedInitial+K1I/2),  (recoveredInitial +(K1R/2)));
			K2R = calculateKRecovered(h, ro, beta, gama, alpha, (susceptibleInitial+K1S/2), (infectedInitial+K1I/2), (recoveredInitial+K1R/2));

			K3S= calculateKSusceptible(h, beta, (susceptibleInitial+(K2S/2)), (infectedInitial+(K2I/2)));
			K3I= calculateKInfected(h, ro, beta, gama, alpha, (susceptibleInitial+K2S/2),  (infectedInitial+K2I/2),  (recoveredInitial +(K2R/2)));
			K3R= calculateKRecovered(h, ro, beta, gama, alpha, (susceptibleInitial+K2S/2), (infectedInitial+K2I/2), (recoveredInitial+K2R/2));

			K4S= calculateKSusceptible(h, beta, (susceptibleInitial+(K3S)), (infectedInitial+(K3I)));
			K4I= calculateKInfected(h, ro, beta, gama, alpha, (susceptibleInitial+K3S),  (infectedInitial+K3I),  (recoveredInitial +(K3R)));
			K4R= calculateKRecovered(h, ro, beta, gama, alpha, (susceptibleInitial+K3S), (infectedInitial+K3I), (recoveredInitial+K3R));

			KS = ((K1S+2 * K2S+2 * K3S+K4S) / 6);
			KI = ((K1I+2 * K2I+2 * K3I+K4I) / 6);
			KR = ((K1R+2 * K2R+2 * K3R+K4R) / 6);

			susceptibles = susceptibleInitial + KS;
			infected = infectedInitial + KI;
			recovered = recoveredInitial + KR;

			susceptibleInitial = susceptibles;
			infectedInitial =  infected;
			recoveredInitial = recovered;
		}

		values[0] = susceptibles;
		values[1] = infected;
		values[2] = recovered;

		return values;
	}

	public static double calculateKSusceptible(double h, double beta, double susceptible, double infected) {
		return h * derivativeSusceptible(beta, susceptible, infected);
	}

	public static double calculateKInfected(double h, double ro, double beta, double gama, double alpha, double susceptible, double infected, double recovered) {
		double calculatedInfected = derivativeInfected(ro, beta, susceptible, infected, gama, recovered, alpha);

		return h * calculatedInfected;
	}

	public static double calculateKRecovered(double h, double ro, double beta, double gama, double alpha, double susceptible, double infected, double recovered) {
		double calculatedRecovered = derivativeRecovered(ro, beta, susceptible, infected, gama, recovered, alpha);

		return h * calculatedRecovered;
	}

	public static double derivativeSusceptible(double beta, double susceptible, double infected){
		return - productInfectedSusceptibleBeta(beta, susceptible, infected);
	}

	public static double derivativeInfected(double ro,double beta, double susceptible,double infected,double gama,double recovered, double alpha){
		double product = productInfectedSusceptibleBeta(beta, susceptible, infected);

		return (ro * product) - (gama * infected) + (alpha * recovered);
	}

	public static double derivativeRecovered(double ro, double beta, double susceptible, double infected,double gama, double recovered, double alpha){
		double product = productInfectedSusceptibleBeta(beta, susceptible, infected);

		return (gama * infected) - (alpha * recovered) + (1-ro) * product;
	}

	public static double productInfectedSusceptibleBeta(double beta, double susceptible, double infected) {
		return beta * susceptible * infected;
	}
	public static void saveValues(double[][] valuesDays,int totalDays,String file) throws FileNotFoundException {
		PrintWriter writeValues = new PrintWriter(file + ".csv");
		writeValues.println("Dia ; S ; I ; R ; N");

		for (int i = 0; i < totalDays; i++) {
			writeValues.print(i);
			writeValues.print(";");
			writeValues.print(valuesDays[1][i]);
			writeValues.print(";");
			writeValues.print(valuesDays[2][i]);
			writeValues.print(";");
			writeValues.print(valuesDays[3][i]);
			writeValues.print(";");
			writeValues.print(valuesDays[4][i]);
			writeValues.println();
		}

		writeValues.close();
	}

	/**
	 * This method was created in order to simply clear our console.
	 * Don't overthink it, 'cause I definitely won't
	 *
	 * OBS: this might now work when testing the code on an IDE
	 */
	public static void clearConsole()
	{
		try
		{
			final String os = System.getProperty("os.name");

			if (os.contains("Windows"))
			{
				new ProcessBuilder("cls").start();
			}
			else
			{
				new ProcessBuilder("clear").start();
			}
		}
		catch (final Exception e)
		{
			//  Handle any exceptions.
		}
	}

	public static void graphic (String outputFile, String outputImage, String methodName, String outputCSV) throws IOException {
		PrintWriter pr = new PrintWriter(outputFile);
		pr.println("set term pngcairo size 1920, 1080");
		pr.printf("set output '%s'\n", outputImage);
		pr.println("set datafile separator ';'");
		pr.printf("set title font 'arial,22' '%s' \n", methodName);
		pr.println("set xlabel font 'arial,18' 'Nº de dias' ");
		pr.println("set ylabel font 'arial,18' 'População' ");
		pr.println("set grid ");
		pr.printf("plot '%s' u 1:2 w lp lc 1 pt -1 lw 4 title 'S', '' u 1:3 w lp lc 3 pt -1 lw 4 title 'I', '' u 1:4 w lp lc 4 pt -1 lw 4 title 'R'\n", outputCSV);
		pr.println("replot");
		pr.close();

		new ProcessBuilder("gnuplot", outputFile, "devices", "-l").start();
	}

	/**
	 * In this function we'll be generating a graph comparing the same method output
	 * to two different users
	 *
	 * @param first_name
	 * @param second_name
	 * @param method
	 * @param first_graph
	 * @param second_graph
	 * @throws IOException
	 */
	public static void graphicComparationUsers(String first_name, String second_name, String method, String first_graph, String second_graph) throws IOException {
		String outputComparation = GRAPHS_PATH + "outputComparation.txt";

		PrintWriter print = new PrintWriter(outputComparation);
		print.println("set terminal pngcairo size 1920, 1080");
		print.printf("set output '%s'\n", GRAPHS_PATH + "twoUsersComparation.png");
		print.println("set size 1,1");
		print.println("set multiplot layout 2,1 ");
		print.println("set datafile separator ';'");
		print.printf("set title font 'arial,15' 'Gráfico de %s de %s'\n", method, first_name);
		print.println("set xlabel font 'calibri,15' 'Nº de dias' ");
		print.println("set ylabel font 'calibri,15' 'População' ");
		print.println("set grid ");
		print.printf("plot '%s' u 1:2 w lp lc 1 pt -1 lw 4 title 'S' , '' u 1:3 w lp lc 3 pt -1 lw 4 title 'I' , '' u 1:4 w lp lc 4 pt -1 lw 4 title 'R'\n", first_graph);
		print.printf("set title font 'arial,15' 'Gráfico de %s de %s'\n", method, second_name);
		print.println("set xlabel font 'calibri,15' 'Nº de dias' ");
		print.println("set ylabel font 'calibri,15' 'População' ");
		print.println("set grid ");
		print.printf("plot '%s' u 1:2 w lp lc 1 pt -1 lw 4 title 'S', '' u 1:3 w lp lc 3 pt -1 lw 4 title 'I', '' u 1:4 w lp lc 4 pt -1 lw 4 title 'R'\n", second_graph);
		print.println("unset multiplot");
		print.close();

		new ProcessBuilder("gnuplot", outputComparation, "devices", "-l").start();
	}

	/**
	 * In this function we're going to compare the two graphs generated to one single
	 * user.
	 * Which means, we're going to compare both RK4 graph and Euler graph
	 */
	public static void graphicComparationMethods(String first_name, String first_graph, String second_graph ) throws IOException{
		String outputComparationMethods = GRAPHS_PATH + "outputComparationsMethods.txt";

		PrintWriter print = new PrintWriter(outputComparationMethods);
		print.println("set term pngcairo size 1920, 1080");
		print.printf("set output '%s'\n", GRAPHS_PATH + "twoMethodsComparation.png");
		print.println("set datafile separator ';'");
		print.printf("set title font 'arial,22' 'Análise comparativa dos métodos de Euler e de RK4 de %s'\n", first_name);
		print.println("set xlabel font 'calibri,18' 'Nº de dias'");
		print.println("set ylabel font 'calibri,18' 'População'");
		print.println("set grid");
		print.printf("plot '%s' u 1:2 w lp lc 1 pt -1 lw 3 title 'S' , '' u 1:3 w lp lc 3 pt -1 lw 3 title 'I' , '' u 1:4 w lp lc 4 pt -1 lw 3 title 'R', '%s' u 1:2 w lp lc 1 pt -1 lw 3 dt 2 title 'Srk' , '' u 1:3 w lp lc 3 pt -1 lw 3 dt 2 title 'Irk' , '' u 1:4 w lp lc 4 pt -1 lw 3 dt 2 title 'Rrk' \n",GRAPHS_PATH + first_graph, GRAPHS_PATH + second_graph);
		print.println("replot");
		print.close();

		new ProcessBuilder("gnuplot", outputComparationMethods, "devices", "-l").start();
	}
}

