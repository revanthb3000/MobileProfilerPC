package org.iitg.mobileprofiler.testing;

import java.io.IOException;

import org.iitg.mobileprofiler.core.ResponseRecommendations;
import org.iitg.mobileprofiler.p2p.tools.UtilityFunctions;

/**
 * Just a simple class used as a starting point for execution.
 * @author RB
 *
 */
public class TestClass {

	public static void main(String[] args) throws IOException{
//		String className = UtilityFunctions.classifyUrl("http://cricinfo.com", false);
//		System.out.println(className);
//		className = UtilityFunctions.classifyUrl("http://nfl.com", false);
//		System.out.println(className);
//		className = UtilityFunctions.classifyUrl("https://code.google.com/p/sqlite-manager/issues/list", false);
//		System.out.println(className);
//		UtilityFunctions.classifyExperimentalOutput("input.txt");
//		UtilityFunctions.writeClassContentsToFile("classContents.txt", true);
//		System.out.println("Done !!!");
//		UtilityFunctions.insertExperimentalResponses();
		ResponseRecommendations responseRecommendations = new ResponseRecommendations(UtilityFunctions.getHexDigest("Revanth"));

		System.out.println("\nUsing Average Score : \n");
		responseRecommendations.getAverageRecommendation();
		
		System.out.println("\nUsing Participation History : \n");
		responseRecommendations.getParticipationHistoryRecommendation();
		
		System.out.println("\nUsing Entropy : \n");
		responseRecommendations.getEntropyRecommendation();
	}
}
