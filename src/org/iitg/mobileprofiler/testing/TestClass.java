package org.iitg.mobileprofiler.testing;

import java.io.IOException;

import org.iitg.mobileprofiler.db.DatabaseConnector;

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
		DatabaseConnector databaseConnector = new DatabaseConnector();
//		databaseConnector.createTables();
		databaseConnector.addQuestion("What is my dick size ?");
		databaseConnector.addQuestion("Kiss my dick ?");
		databaseConnector.addAnswer(1, 6, 0.9);
		databaseConnector.addAnswer(1, 10, 0.2);
		databaseConnector.addAnswer(1, 5, 0.8);
		databaseConnector.addAnswer(1, 9, 0.1);
		databaseConnector.addAnswer(1, 1, 0.03);
		System.out.println(databaseConnector.getWeightedAnswer(1));
		System.out.println(databaseConnector.getMaxQuestionId());
		databaseConnector.closeDBConnection();
	}
}
