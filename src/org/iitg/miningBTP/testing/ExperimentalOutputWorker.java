package org.iitg.miningBTP.testing;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * This class parses the experimentalOutput.txt file and gets a list of web URLs
 * @author RB
 *
 */
public class ExperimentalOutputWorker {

	/**
	 * Delimiter is |||
	 * Using \\ for regex.
	 */
	private static final String DELIMITER = "\\|\\|\\|";

	public static ArrayList<String> parseFile(String fileName) throws IOException{
		FileReader fileReader = new FileReader(fileName);
		BufferedReader bufferedReader = new BufferedReader(fileReader);
		ArrayList<String> webPages = new ArrayList<String>();
		String line = "";
		while((line=bufferedReader.readLine())!=null){
			webPages.add(line.split(DELIMITER)[2].trim());
		}
		bufferedReader.close();
		fileReader.close();
		return webPages;
	}
	
	public static void classifyPages(ArrayList<String> webPages) throws IOException{
		int totalCount = 0;
		int classifiedCount = 0;
		for(String webPage : webPages){
			String className = UtilityFunctions.classifyUrl(webPage, false);
			totalCount++;
			if(!className.trim().equals("")){
				System.out.println("URL is : " + webPage);
				System.out.println("Class is : " + className + "\n");
				classifiedCount++;
			}
		}
		System.out.println("Total number of pages is : " + totalCount);
		System.out.println("Total number of pages classified is : " + classifiedCount);
	}
	
}
