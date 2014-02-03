package org.iitg.mobileProfiler.testing;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.iitg.mobileProfiler.core.TextParser;
import org.iitg.mobileProfiler.db.DatabaseConnector;


/**
 * Just a simple class used as a starting point for execution.
 * @author RB
 *
 */
public class TestClass {

	public static void main(String[] args) throws IOException{
		String className = UtilityFunctions.classifyUrl("https://github.com/revanthb3000", false);
		System.out.println(className);
		//potentialFeaturesVerification();
	}
	
	public static void potentialFeaturesVerification() throws IOException{
		int totalSumChanges = 0, totalSumPredictedChanges = 0;
		for(int i=1;i<=88;i++){
			ArrayList<String> difference = readArrayListFromFile("analysisResults/difference"+i+".txt");
			ArrayList<String> fileSourceWords = readArrayListFromFile("analysisResults/fileSource"+i+".txt");
			int totalNumberOfChanges = difference.size();
			int changesPresentInSource = 0;
			for(String term : difference){
				term = term.replace("   -----removed", "").replace("   -----added", "");
				if(fileSourceWords.contains(term.trim())){
					changesPresentInSource++;
				}
			}
			System.out.println("Document #"+i + ":");
			System.out.println("Total number of changes : "+ totalNumberOfChanges);
			System.out.println("Number of changes in source code " + changesPresentInSource + "\n");
			totalSumChanges+=totalNumberOfChanges;
			totalSumPredictedChanges+=changesPresentInSource;
		}
		System.out.println("\n\nFinal Count: " + totalSumChanges + " " + totalSumPredictedChanges);
	}
	
	public static void candidateFeaturesAnalysis() throws IOException{
		ArrayList<String> webPages = ExperimentalOutputWorker.parseFile("experimentalOutput.txt");
		TextParser textParser = new TextParser();
		int docCount = 0;
		for(String webPage : webPages){
			String className = UtilityFunctions.classifyUrl(webPage, true);
			if(!className.trim().equals("")){
				docCount++;
				System.out.println("URL is : " + webPage);
				System.out.println("Class is : " + className + "\n");
				ArrayList<String> difference = UtilityFunctions.getChangeInFeatures();
				ArrayList<String> webPageTokens = textParser.getAllTokens(UtilityFunctions.getPageSourceCode(webPage), true);
				writeArrayListToFile("analysisResults/difference"+docCount+".txt", difference);
				writeArrayListToFile("analysisResults/fileSource"+docCount+".txt", webPageTokens);
				UtilityFunctions.recomputeFeatures();
			}
		}
	}
	
	public static void calculateExperimentalFeatures() throws IOException{
		DatabaseConnector databaseConnector = new DatabaseConnector();
		ArrayList<String> userDataTerms = databaseConnector.getTermsInUserData();
		databaseConnector.closeDBConnection();
		UtilityFunctions.recomputeSelectFeatures(userDataTerms);
		UtilityFunctions.writeFeaturesToFile();
	}
	
	public static void writeArrayListToFile(String fileName, ArrayList<String> list) throws IOException{
		FileWriter fileWriter = new FileWriter(fileName);
		BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
		for(String word : list){
			bufferedWriter.write(word + "\n");
		}
		bufferedWriter.close();
		fileWriter.close();
	}
	
	public static ArrayList<String> readArrayListFromFile(String fileName) throws IOException{
		ArrayList<String> arrayList = new ArrayList<String>();
		FileReader fileReader = new FileReader(fileName);
		BufferedReader bufferedReader = new BufferedReader(fileReader);
		String line = "";
		while((line=bufferedReader.readLine())!=null){
			arrayList.add(line);
		}
		bufferedReader.close();
		fileReader.close();
		return arrayList;
	}
	
}
