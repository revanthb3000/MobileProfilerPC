package org.iitg.miningBTP.testing;

import java.io.IOException;
import java.util.ArrayList;

import org.iitg.miningBTP.db.DatabaseConnector;


/**
 * Just a simple class used as a starting point for execution.
 * @author RB
 *
 */
public class TestClass {

	public static void main(String[] args) throws IOException{
		DatabaseConnector databaseConnector = new DatabaseConnector();
		ArrayList<String> userDataTerms = databaseConnector.getTermsInUserData();
		databaseConnector.closeDBConnection();
		UtilityFunctions.recomputeSelectFeatures(userDataTerms);
		UtilityFunctions.writeFeaturesToFile();
	}
}
