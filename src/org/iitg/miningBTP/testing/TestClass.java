package org.iitg.miningBTP.testing;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.iitg.miningBTP.core.TextParser;
import org.iitg.miningBTP.db.DatabaseConnector;


/**
 * Just a simple class used as a starting point for execution.
 * @author RB
 *
 */
public class TestClass {

	public static void main(String[] args) throws IOException{
		candidateFeaturesAnalysis();
		//calculateExperimentalFeatures();
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
	
}
