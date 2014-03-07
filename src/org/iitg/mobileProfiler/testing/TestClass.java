package org.iitg.mobileprofiler.testing;

import java.io.IOException;

/**
 * Just a simple class used as a starting point for execution.
 * @author RB
 *
 */
public class TestClass {

	public static void main(String[] args) throws IOException{
		String className = UtilityFunctions.classifyUrl("http://cricinfo.com", false);
		System.out.println(className);
		className = UtilityFunctions.classifyUrl("http://nfl.com", false);
		System.out.println(className);
		className = UtilityFunctions.classifyUrl("https://code.google.com/p/sqlite-manager/issues/list", false);
		System.out.println(className);
//		UtilityFunctions.classifyExperimentalOutput("experimentalOutput.txt");
//		UtilityFunctions.writeClassContentsToFile("classContents.txt", true);
	}	
}
