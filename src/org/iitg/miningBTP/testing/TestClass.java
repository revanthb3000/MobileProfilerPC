package org.iitg.miningBTP.testing;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Just a simple class used as a starting point for execution.
 * @author RB
 *
 */
public class TestClass {

	public static void main(String[] args) throws IOException{
		ArrayList<String> webPages = ExperimentalOutputWorker.parseFile("experimentalOutput.txt");
		ExperimentalOutputWorker.classifyPages(webPages);
	}
}
