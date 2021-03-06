package org.iitg.mobileprofiler.testing;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

import org.iitg.mobileprofiler.core.Classifier;
import org.iitg.mobileprofiler.core.TextParser;
import org.iitg.mobileprofiler.db.DatabaseConnector;
import org.iitg.mobileprofiler.db.ResponseDao;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

/**
 * This class contains utility functions and routines that are generally used
 * during classification.
 * 
 * @author RB
 * 
 */
public class UtilityFunctions {

	/**
	 * Classifies the entire experimentalOutput.txt file and puts those changes into the DB. Also outputs the features and gini Mapping files.
	 * @throws IOException
	 */
	public static void classifyExperimentalOutput(String webHistoryFileName) throws IOException{
		ArrayList<String> webPages = ExperimentalOutputWorker.parseFile(webHistoryFileName);
		ExperimentalOutputWorker.classifyPages(webPages,true);
		recomputeFeatures();
		writeFeaturesToFile();
		writeGiniCoeffsToFile();
	}

	/**
	 * This function writes the classIds and classNames to a file.
	 * @throws IOException 
	 */
	public static void writeClassMappingToFile() throws IOException{
		FileWriter fileWriter = new FileWriter("classMapping.txt");
		BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
		DatabaseConnector databaseConnector = new DatabaseConnector();
		for(int i=0; i< databaseConnector.getNumberOfClasses();i++){
			bufferedWriter.write(i + " - " + databaseConnector.getClassName(i) + "\n");
		}
		bufferedWriter.close();
		fileWriter.close();
	}
	
	/**
	 * This function: 1) Deletes the existing features. 2) Uses the term
	 * distribution data to compute the new features (Gini) and inserts it into
	 * the features table.
	 */
	public static void recomputeFeatures() {
		DatabaseConnector databaseConnector = new DatabaseConnector();
		databaseConnector.deleteFeatures();
		System.out.println("Features Gone");
		Classifier classifier = new Classifier(databaseConnector);
		ArrayList<String> features = classifier.calculateFeaturesList();
		databaseConnector.insertFeatures(features);
		databaseConnector.closeDBConnection();
	}

	/**
	 * This function recomputes the features list and gives out the symmetric difference.
	 * @return Symmetric difference of oldFeatures and newFeatures along with a tag to indicate if a feature has been added or removed.
	 */
	public static ArrayList<String> getChangeInFeatures(){
		DatabaseConnector databaseConnector = new DatabaseConnector();
		Classifier classifier = new Classifier(databaseConnector);
		
		ArrayList<String> originalFeatures = databaseConnector.getAllFeaturesList();
		ArrayList<String> newFeatures = classifier.calculateFeaturesList();
		ArrayList<String> difference = new ArrayList<String>();
		for(String feature : originalFeatures){
			if(!newFeatures.contains(feature)){
				difference.add(feature + "   -----removed");
			}
		}
		for(String feature : newFeatures){
			if(!originalFeatures.contains(feature)){
				difference.add(feature + "   -----added");
			}
		}
		databaseConnector.closeDBConnection();
		return difference;
	}
	
	/**
	 * How is this different from the previous function ? I'm only using a set of potential features to make my decision. 
	 * Calculate Gini of only these features and then do the work.
	 */
	public static void recomputeSelectFeatures(ArrayList<String> potentialFeatures){
		DatabaseConnector databaseConnector = new DatabaseConnector();
		Classifier classifier = new Classifier(databaseConnector);
		ArrayList<String> featuresList = databaseConnector.getAllFeaturesList();
		for(String potentialFeature : potentialFeatures){
			double giniCoefficient = classifier.calculateGiniCoefficient(potentialFeature);
			if(featuresList.contains(potentialFeature)){
				if(giniCoefficient<classifier.getGiniThreshold()){
					featuresList.remove(potentialFeature);
				}
			}
			else{
				if(giniCoefficient>=classifier.getGiniThreshold()){
					featuresList.add(potentialFeature);
				}
			}
		}
		databaseConnector.deleteFeatures();
		databaseConnector.insertFeatures(featuresList);
	}

	/**
	 * When I'm given a filename, I'll read the features from it, store it in an ArrayList and dump this stuff into the database.
	 * @param fileName
	 * @throws IOException 
	 */
	public static void insertFeaturesFromFile(String fileName) throws IOException{
		ArrayList<String> featuresList = new ArrayList<String>();
		String line = "";
		FileReader fileReader = new FileReader(fileName);
		BufferedReader bufferedReader = new BufferedReader(fileReader);
		while((line=bufferedReader.readLine())!=null){
			featuresList.add(line.trim());
		}
		bufferedReader.close();
		fileReader.close();
		DatabaseConnector databaseConnector = new DatabaseConnector();
		databaseConnector.deleteFeatures();
		databaseConnector.insertFeatures(featuresList);
		databaseConnector.closeDBConnection();
	}
	
	/**
	 * Basic function that will dump the contents of the features table to a
	 * text file.
	 * 
	 * @throws IOException
	 */
	public static void writeFeaturesToFile() throws IOException {
		DatabaseConnector databaseConnector = new DatabaseConnector();
		ArrayList<String> features = databaseConnector.getAllFeaturesList();
		FileWriter fileWriter = new FileWriter("Features.txt");
		for (String feature : features) {
			fileWriter.write(feature + "\n");
		}
		fileWriter.close();
	}

	/**
	 * This method calculates the Gini coefficients of all terms in the database and pipes the list to a file.
	 * Terms are sorted in descending order of their Gini coefficients.
	 * @throws IOException
	 */
	public static void writeGiniCoeffsToFile() throws IOException {
		DatabaseConnector databaseConnector = new DatabaseConnector();
		Classifier classifier = new Classifier(databaseConnector);
		Map<String, Double> termGiniMapping = classifier.getGiniMapping();
		System.out.println("Gini Mapping Loaded");
		FileWriter fileWriter = new FileWriter("GiniMapping.txt");
		for (String term : termGiniMapping.keySet()) {
			fileWriter.write(term + " - " + termGiniMapping.get(term) + "\n");
		}
		fileWriter.close();
		databaseConnector.closeDBConnection();
	}

	/**
	 * Given a fileName, this functions dumps the classcontents table into it.
	 * @param fileName
	 * @param isUserDataTable
	 * @throws IOException
	 */
	public static void writeClassContentsToFile(String fileName, boolean isUserDataTable) throws IOException{
		DatabaseConnector databaseConnector = new DatabaseConnector();
		int numberOfClasses = databaseConnector.getNumberOfClasses();
		ArrayList<Integer> classContents = databaseConnector.getNumberOfDocuments(0, numberOfClasses, isUserDataTable);
		databaseConnector.closeDBConnection();
		FileWriter fileWriter = new FileWriter(fileName);
		BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
		for(Integer numberOfDocs : classContents){
			bufferedWriter.write(numberOfDocs + " ");
		}
		bufferedWriter.close();
		fileWriter.close();
	}
	
	/**
	 * This function takes in a URL and classifies it. Crucial part of the
	 * android program too.
	 * 
	 * @param webpageUrl
	 *            WebPage that has to be classified.
	 * @param shouldMerge
	 *            Tells the function whether to update the term distribution and
	 *            class counts after classification.
	 * @return Returns the className
	 * @throws IOException
	 */
	public static String classifyUrl(String webpageUrl, Boolean shouldMerge)
			throws IOException {
		String sourceCode = getPageSourceCode(webpageUrl);

		DatabaseConnector databaseConnector = new DatabaseConnector();
		TextParser textParser = new TextParser();
		Classifier classifier = new Classifier(databaseConnector);
		
		int classId = classifier.classifyDocClassic(textParser.tokenizeString(sourceCode, true));	//tokenize function eliminates words that are not feeatures. No worries there !
		String className = "";
		className = databaseConnector.getClassName(classId);
		
		System.out.println("Number of terms is : " + textParser.getAllTokens(sourceCode, true).size());
		
		if (shouldMerge && (!className.equals(""))) {
			databaseConnector.updateClassContents(classId,true);
			databaseConnector.updateTermDistribution(textParser.getAllTokens(sourceCode, true), classId, true);
			System.out.println("Finished updating distribution");
		}
		
		databaseConnector.closeDBConnection();
		return className;
	}
	
	/**
	 * This function visits a webpage, gets the source code, removes the html tags and gives you text to work with.
	 * @param webpageUrl
	 * @return
	 * @throws IOException
	 */
	public static String getPageSourceCode(String webpageUrl) throws IOException{
		URL url = new URL(webpageUrl);
		//Use GProxy for this !
		Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 8080));
		URLConnection urlConnection = url.openConnection(proxy);
		// UserAgent is set because of some websites that decide to block bots !
		urlConnection.setRequestProperty("User-Agent","Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
		urlConnection.connect();

		String line = null;
		StringBuffer webPageBuffer = new StringBuffer();
		BufferedReader inputReader = new BufferedReader(new InputStreamReader(
				urlConnection.getInputStream()));
		while ((line = inputReader.readLine()) != null) {
			webPageBuffer.append(line);
		}
		Document document = Jsoup.parse(String.valueOf(webPageBuffer), "UTF-8");
		Elements title = document.select("title");
		Elements body = document.select("body");
		return title.text() + " " + body.text(); 
	}

	/**
	 * Given a filename as an input, this function reads that file, extracts the
	 * text from the source code and returns that String.
	 * 
	 * @param fileName
	 *            Name of the file containing the source code.
	 * 
	 * @return Text present on the page
	 */
	public static String getSoupedPageContent(String fileName) {
		String line = "";
		String sourceCode = "";
		try {
			FileReader fileReader = new FileReader(fileName + ".htm");
			BufferedReader bufferedReader = new BufferedReader(fileReader);

			while ((line = bufferedReader.readLine()) != null) {
				sourceCode += line + "\n";
			}
			bufferedReader.close();
			fileReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		Document document = Jsoup.parse(sourceCode);
		Elements title = document.select("title");
		Elements body = document.select("body");
		return title.text() + " " + body.text();
	}

	/**
	 * This function sets up a DB from scratch. Creates all the tables. Fills it
	 * up with content.
	 * 
	 * Will probably not be used much. Is a one-time-only function.
	 * 
	 * @throws IOException
	 */
	public static void initializeDatabase() throws IOException {
		System.out.println("Start");
		DatabaseConnector databaseConnector = new DatabaseConnector();
		System.out.println("DB Up");
		databaseConnector.createTables();
		System.out.println("Tables Created");
		databaseConnector.fillClassMappings();
		System.out.println("Mappings Filled");
		databaseConnector.fillClassContents();
		databaseConnector.fillUserDataClassContents();
		System.out.println("Contents Filled");
		databaseConnector.fillFeaturesList();
		System.out.println("Features List Filled");
		databaseConnector.fillTermDistribution();
		System.out.println("Feature Dist. Done");
		databaseConnector.closeDBConnection();
	}

	/**
	 * Another one-time-only function. This method reads the testing dataset and
	 * classifies each document in it. Used to compare initial system's
	 * efficiency.
	 * 
	 * @throws IOException
	 * @throws SQLException
	 */
	public static void classifyTestingSet() throws IOException, SQLException {
		int numOfDocsProcessed = 0;
		TextParser textParser = new TextParser();
		FileWriter fileWriter = new FileWriter("Results.txt");
		BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
		File file = new File("testPages");
		for (File fileName : file.listFiles()) {
			FileReader fileReader = new FileReader(fileName);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			String line = "";
			String pageContent = "";
			String currentURL = "";
			while ((line = bufferedReader.readLine()) != null) {
				if (line.contains("URL -#- ")) {
					ArrayList<String> tokens = textParser.tokenizeString(
							pageContent, true);
					if (!(pageContent.equals(""))) {
						DatabaseConnector databaseConnector = new DatabaseConnector();
						Classifier classifier = new Classifier(
								databaseConnector);
						bufferedWriter.write(currentURL + "\n");
						bufferedWriter.write("Actual: "
								+ fileName.toString()
										.replace("testPages\\", "")
								+ "\nClassified: "
								+ databaseConnector.getClassName(
										classifier.classifyDoc(tokens))
										.replace("/", "-") + "\n\n");
						numOfDocsProcessed++;
						databaseConnector.closeDBConnection();
					}
					if (numOfDocsProcessed % 100 == 0) {
						System.out.println(numOfDocsProcessed + " - Done");
					}
					currentURL = line.replace("URL -#- ", "").trim();
					pageContent = "";
				}
				line = line.replace("URL -#- ", " ").replace("TITLE -#- ", " ")
						.replace("DESC -#- ", " ");
				pageContent = pageContent + " " + line;
			}
			bufferedReader.close();
			fileReader.close();
		}
		bufferedWriter.close();
		fileWriter.close();
	}

	/**
	 * Basically just inserts the response classes into the table.
	 */
	public static void fillResponseClasses(){
		DatabaseConnector databaseConnector = new DatabaseConnector();

		databaseConnector.insertResponseClass("Animation");
		databaseConnector.insertResponseClass("Cricket");
		databaseConnector.insertResponseClass("Entertainment");
		databaseConnector.insertResponseClass("Football");
		databaseConnector.insertResponseClass("Movies");
		databaseConnector.insertResponseClass("News");
		databaseConnector.insertResponseClass("Politics");
		databaseConnector.insertResponseClass("Social");
		databaseConnector.insertResponseClass("Technology");
		databaseConnector.insertResponseClass("Tennis");
		databaseConnector.insertResponseClass("TV Show");


		databaseConnector.closeDBConnection();
	}
	
	/**
	 * I extract data from the csv file and then add them to the DB.
	 */
	public static void insertExperimentalResponses(){
		try{
			FileReader fileReader = new FileReader("experiment.csv");
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			
			ArrayList<String> questions = new ArrayList<String>();
			ArrayList<Integer> classIds = new ArrayList<Integer>();
			
			ArrayList<ResponseDao> responseDaos = new ArrayList<ResponseDao>();
			
			String[] splitArray = null;
			
			String line = "";
			//Questions
			line = bufferedReader.readLine();
			splitArray = line.split(",");
			for(int i=1;i<splitArray.length;i++){
				questions.add(splitArray[i]);
			}
			
			//Class Ids
			line = bufferedReader.readLine();
			splitArray = line.split(",");
			for(int i=1;i<splitArray.length;i++){
				classIds.add(Integer.parseInt(splitArray[i]));
			}
			
			
			while((line=bufferedReader.readLine())!=null){
				splitArray = line.split(",");
				String userName = splitArray[0];
				for(int i=1;i<splitArray.length;i++){
					if(splitArray[i].trim().equals("")){
						//Question Not Answered
//						System.out.println("Here for " + userName + " at question : " + questions.get(i-1));
						continue;
					}
					else{
						int answer = Integer.parseInt(splitArray[i].trim());
						responseDaos.add(new ResponseDao(org.iitg.mobileprofiler.p2p.tools.UtilityFunctions.getHexDigest(userName), questions.get(i-1), answer, classIds.get(i-1)));
					}
				}
			}
			
			System.out.println(responseDaos.size());
			Collections.shuffle(responseDaos);
			for(ResponseDao responseDao : responseDaos){
				System.out.println(responseDao);
			}
			
			DatabaseConnector databaseConnector = new DatabaseConnector();
			databaseConnector.insertResponses(responseDaos);
			databaseConnector.closeDBConnection();
			
			bufferedReader.close();
			fileReader.close();	
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
}
