package org.iitg.miningBTP.testing;

import java.io.IOException;
import java.util.ArrayList;

public class TestClass {
	
	public static void main(String[] args) throws IOException{
//		UtilityFunctions.recomputeFeatures();
//		UtilityFunctions.writeFeaturesToFile();
//		String webpageUrl = "http://en.wikipedia.org/wiki/Alex_Morgan";
//		UtilityFunctions.classifyUrl(webpageUrl,false);
		ArrayList<String> webPages = ExperimentalOutputWorker.parseFile("experimentalOutput.txt");
		ExperimentalOutputWorker.classifyPages(webPages);
	}
}
