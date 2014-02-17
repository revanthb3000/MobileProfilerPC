package org.iitg.mobileProfiler.testing;

import java.io.IOException;


/**
 * Just a simple class used as a starting point for execution.
 * @author RB
 *
 */
public class TestClass {

	public static void main(String[] args) throws IOException{
		String className = UtilityFunctions.classifyUrl("https://github.com/revanthb3000", false);
		System.out.println(className);
//		UtilityFunctions.recomputeFeatures();
//		UtilityFunctions.writeFeaturesToFile();
		UtilityFunctions.writeClassContentsToFile("classContents.txt", true);
	}	
}
