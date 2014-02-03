package org.iitg.mobileProfiler.db;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.iitg.mobileProfiler.core.FeatureDistribution;
import org.iitg.mobileProfiler.dal.FileStorageUtilities;

/**
 * This is the main class used to interact with the database.
 * A class with several methods in it. Contains methods for all database queries that will be used.
 * @author RB
 *
 */
public class DatabaseConnector {

	private static final String DATABASE_FILE_NAME = "mobileclassifier.db";

	private Connection connection;
	
	/**
	 * Basic constructor. Opens a database connection.
	 */
	public DatabaseConnector() {
		openDBConnection();
	}

	public static String getDatabaseFileName() {
		return DATABASE_FILE_NAME;
	}

	public Connection getConnection() {
		return connection;
	}

	/**
	 * Tries to open a JDBC connection with the sqlite database.
	 */
	public void openDBConnection() {
		try {
			Class.forName("org.sqlite.JDBC");
			connection = DriverManager.getConnection("jdbc:sqlite:"
					+ DATABASE_FILE_NAME);
		} catch (ClassNotFoundException | SQLException e) {
			System.out.println("Exception Caught." + e);
			e.printStackTrace();
		}
	}

	/**
	 * Does what it says. Calling this function is crucial. Don't want any open connections lingering around !
	 */
	public void closeDBConnection() {
		try {
			connection.close();
		} catch (SQLException e) {
			System.out.println("Exception Caught." + e);
			e.printStackTrace();
		}
	}

	/**
	 * A one time only function. Creates the tables with the required DB schema.
	 */
	public void createTables() {
		String query = "";
		try {
			Statement statement = connection.createStatement();
			query = "CREATE TABLE IF NOT EXISTS `classcontents` "
					+ "(`classId` int(11) NOT NULL,`numberOfDocs` "
					+ "int(11) NOT NULL,PRIMARY KEY (`classId`));";
			statement.executeUpdate(query);

			query = "CREATE TABLE IF NOT EXISTS `termdistribution` "
					+ "(`feature` varchar(255) NOT NULL,`classId` int(11) NOT NULL,"
					+ "`A` int(11) NOT NULL,"
					+ "PRIMARY KEY (`feature`,`classId`));";
			statement.executeUpdate(query);

			query = "CREATE TABLE IF NOT EXISTS `classmapping` "
					+ "(`classId` int(11) NOT NULL,`className` "
					+ "varchar(255) NOT NULL,PRIMARY KEY (`classId`));";
			statement.executeUpdate(query);

			query = "CREATE TABLE IF NOT EXISTS "
					+ "`featurelist` (`feature` varchar(255) "
					+ "NOT NULL,PRIMARY KEY (`feature`));";
			statement.executeUpdate(query);

			// THIS WON'T SUFFICE. OPEN THE SQLITE FILE AND SET activitiesId to
			// INTEGER PRIMARY KEY
			query = "CREATE TABLE IF NOT EXISTS `activities` "
					+ "(`activityId` int(11) NOT NULL,`activityType` varchar(255) NOT NULL,"
					+ "`activityInfo` text NOT NULL,"
					+ "`timeStamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,"
					+ "`assignedClass` varchar(255) NOT NULL,PRIMARY KEY (`activityId`))";
			statement.executeUpdate(query);

			query = "CREATE TABLE IF NOT EXISTS `userdataterms` "
					+ "(`feature` varchar(255) NOT NULL,`classId` int(11) NOT NULL,"
					+ "`A` int(11) NOT NULL,"
					+ "PRIMARY KEY (`feature`,`classId`));";
			statement.executeUpdate(query);
		} catch (SQLException e) {
			System.out.println("Exception Caught for query " + query + " \n"
					+ e);
			e.printStackTrace();
		}
	}

	/**
	 * Fill the classContents table with the class distribution of the training dataset.
	 * @throws IOException
	 */
	public void fillClassContents() throws IOException {
		List<Integer> classContents = FileStorageUtilities
				.getClassContentInfo("inputSourceFiles/classInfo.dat");
		String query = "";
		try {
			Statement statement = connection.createStatement();
			for (int i = 0; i < classContents.size(); i++) {
				query = "INSERT INTO `classcontents`"
						+ " (`classId` ,`numberOfDocs`)" + "VALUES ('" + i
						+ "', '" + classContents.get(i) + "');";
				statement.executeUpdate(query);
			}
		} catch (SQLException e) {
			System.out.println("Exception Caught for query " + query + " \n"
					+ e);
			e.printStackTrace();
		}
	}

	/**
	 * Fills the mapping tables with values from 1 to 237 along with the respective class names.
	 * @throws IOException
	 */
	public void fillClassMappings() throws IOException {
		List<String> classMapping = FileStorageUtilities
				.getClassMappingInfo("inputSourceFiles/classthreshold.txt");
		String query = "";
		try {
			Statement statement = connection.createStatement();
			for (int i = 0; i < classMapping.size(); i++) {
				query = "INSERT INTO `classmapping`"
						+ " (`classId` ,`className`)" + "VALUES ('" + i
						+ "', '" + classMapping.get(i) + "');";
				statement.executeUpdate(query);
			}
		} catch (SQLException e) {
			System.out.println("Exception Caught for query " + query + " \n"
					+ e);
			e.printStackTrace();
		}
	}

	/**
	 * Takes the training dataset index table and fills this table using that data.
	 * @throws IOException
	 */
	public void fillTermDistribution() throws IOException {
		ArrayList<FeatureDistribution> featureDistributions = FileStorageUtilities
				.getFeatureDistribution("inputSourceFiles/featureDistribution.dat");
		System.out.println("In here");
		String query = "";
		int wordCount = 0;
		Statement statement;
		try {
			statement = connection.createStatement();
			for (FeatureDistribution featureDistribution : featureDistributions) {
				wordCount++;
				if (wordCount % 1000 == 0) {
					System.out.println(featureDistribution.getFeatureName()
							+ " Done !!");
				}
				for (int i = 0; i < featureDistribution.featureClassRelation
						.size(); i++) {
					if (featureDistribution.featureClassRelation.get(i).getA() != 0) {
						query = "INSERT INTO `termdistribution`(`feature` ,`classId` ,`A`) VALUES ('"
								+ featureDistribution.getFeatureName()
								+ "', '"
								+ i
								+ "', '"
								+ featureDistribution.featureClassRelation.get(
										i).getA() + "');";
						statement.executeUpdate(query);
					}
				}
			}
		} catch (SQLException e) {
			System.out.println("Exception Caught for query " + query + " \n"
					+ e);
			e.printStackTrace();
		}
	}

	/**
	 * Initial set of features with gini = 0.95
	 * @throws IOException
	 */
	public void fillFeaturesList() throws IOException {
		ArrayList<String> features = FileStorageUtilities
				.getFeatures("inputSourceFiles/giniCoefficient0.95.dat");
		Statement statement = null;
		String query = "";
		int wordCount = 0;
		try {
			statement = connection.createStatement();
			for (String feature : features) {
				if (wordCount % 100 == 0) {
					System.out.println(wordCount);
				}
				wordCount++;
				query = "INSERT INTO `featurelist`(`feature`) VALUES('"
						+ feature + "')";
				statement.executeUpdate(query);
			}
			statement.close();
		} catch (SQLException e) {
			System.out.println("Exception Caught for query " + query + " \n"
					+ e);
			e.printStackTrace();
		}
	}

	/**
	 * Given a word and classId, this function will return a TermDistributionDao object which will also contain the 'A' value.
	 * @param term
	 * @param classId
	 * @return
	 */
	public TermDistributionDao getTermDistribution(String term, int classId) {
		String query = "SELECT * from termdistribution where feature='" + term
				+ "' AND classId=" + classId + ";";

		TermDistributionDao termDistributionDao = null;
		try {
			Statement statement = connection.createStatement();
			ResultSet resultSet;
			resultSet = statement.executeQuery(query);
			if (resultSet.next()) {
				termDistributionDao = new TermDistributionDao(term, classId,
						resultSet.getInt("a"));
			}
		} catch (SQLException e) {
			System.out.println("Exception Caught for query " + query + " \n"
					+ e);
			e.printStackTrace();
		}
		return termDistributionDao;
	}

	/**
	 * Given a term, this function will return a Map between classId and termDistributionDao objects. Useful function if you need the distribution of a term across all classes.
	 * @param term
	 * @return
	 */
	public Map<Integer, TermDistributionDao> getAllTermDistribution(String term) {
		String query = "SELECT * from termdistribution where feature='" + term
				+ "';";

		Map<Integer, TermDistributionDao> termDistributionDaos = new HashMap<Integer, TermDistributionDao>();
		try {
			Statement statement = connection.createStatement();
			ResultSet resultSet;
			resultSet = statement.executeQuery(query);
			while (resultSet.next()) {
				TermDistributionDao termDistributionDao = new TermDistributionDao(
						term, resultSet.getInt("classId"),
						resultSet.getInt("a"));
				termDistributionDaos.put(termDistributionDao.getClassId(),
						termDistributionDao);
			}
		} catch (SQLException e) {
			System.out.println("Exception Caught for query " + query + " \n"
					+ e);
			e.printStackTrace();
		}
		return termDistributionDaos;
	}

	/**
	 * Similar to the previous function. Given a set of tokens, a Map is returned that will contain info of the termDistribution of each term present in the ArrayList.
	 * @param tokens
	 * @return
	 */
	public Map<String, Map<Integer, TermDistributionDao>> getAllTokensDistribution(
			ArrayList<String> tokens) {
		if (tokens.size() == 0) {
			return null;
		}
		String term = tokens.get(0), previousTerm = tokens.get(0);
		String query = "SELECT * from termdistribution where feature='" + term
				+ "'";
		for (int i = 1; i < tokens.size(); i++) {
			term = tokens.get(i);
			if (term.equals(previousTerm)) {
				continue;
			}
			previousTerm = term;
			query += " OR feature='" + term + "'";
		}
		query += ";";
		Map<String, Map<Integer, TermDistributionDao>> termDistributionMap = new HashMap<String, Map<Integer, TermDistributionDao>>();
		try {
			Statement statement = connection.createStatement();
			ResultSet resultSet;
			resultSet = statement.executeQuery(query);
			while (resultSet.next()) {
				String token = resultSet.getString("feature");
				int classId = resultSet.getInt("classId");
				int A = resultSet.getInt("a");
				TermDistributionDao termDistributionDao = new TermDistributionDao(
						token, classId, A);
				if (termDistributionMap.containsKey(token)) {
					termDistributionMap.get(token).put(
							termDistributionDao.getClassId(),
							termDistributionDao);
				} else {
					Map<Integer, TermDistributionDao> termDistributionDaos = new HashMap<Integer, TermDistributionDao>();
					termDistributionMap.put(token, termDistributionDaos);
					termDistributionMap.get(token).put(
							termDistributionDao.getClassId(),
							termDistributionDao);
				}
			}
		} catch (SQLException e) {
			System.out.println("Exception Caught for query " + query + " \n"
					+ e);
			e.printStackTrace();
		}
		return termDistributionMap;
	}

	/**
	 * Given a set of words, this function returns all those terms that are a part of our feature set.
	 * @param tokens
	 * @return
	 */
	public ArrayList<String> getTokensList(ArrayList<String> tokens) {
		if (tokens.size() == 0) {
			return null;
		}
		String term = tokens.get(0), previousTerm = tokens.get(0);
		String query = "SELECT * from featurelist where feature='" + term + "'";
		for (int i = 1; i < tokens.size(); i++) {
			term = tokens.get(i);
			if (term.equals(previousTerm)) {
				continue;
			}
			previousTerm = term;
			query += " OR feature='" + term + "'";
		}
		query += ";";
		ArrayList<String> features = new ArrayList<String>();
		try {
			Statement statement = connection.createStatement();
			ResultSet resultSet;
			resultSet = statement.executeQuery(query);
			while (resultSet.next()) {
				String token = resultSet.getString("feature");
				features.add(token);
			}
		} catch (SQLException e) {
			System.out.println("Exception Caught for query " + query + " \n"
					+ e);
			e.printStackTrace();
		}
		return features;
	}

	/**
	 * Returns a list of all the features used by the classifier.
	 * @return
	 */
	public ArrayList<String> getAllFeaturesList() {
		String query = "SELECT * from featurelist;";
		ArrayList<String> features = new ArrayList<String>();
		try {
			Statement statement = connection.createStatement();
			ResultSet resultSet;
			resultSet = statement.executeQuery(query);
			while (resultSet.next()) {
				String token = resultSet.getString("feature");
				features.add(token);
			}
		} catch (SQLException e) {
			System.out.println("Exception Caught for query " + query + " \n"
					+ e);
			e.printStackTrace();
		}
		return features;
	}

	/**
	 * Tells if a given term is a feature or not.
	 * @param term
	 * @return
	 */
	public Boolean isTermFeature(String term) {
		String query = "SELECT * from featurelist where feature='" + term
				+ "';";
		Boolean isTermFeature = false;
		try {
			Statement statement = connection.createStatement();
			ResultSet resultSet;
			resultSet = statement.executeQuery(query);
			if (resultSet.next()) {
				isTermFeature = true;
			}
		} catch (SQLException e) {
			System.out.println("Exception Caught for query " + query + " \n"
					+ e);
			e.printStackTrace();
		}
		return isTermFeature;
	}

	/**
	 * Queries the database and gets the total number of classes used.
	 * @return
	 */
	public int getNumberOfClasses() {
		String query = "SELECT Count(className) from classmapping;";
		int numberOfClasses = 0;
		try {
			Statement statement = connection.createStatement();
			ResultSet resultSet;
			resultSet = statement.executeQuery(query);
			if (resultSet.next()) {
				numberOfClasses = resultSet.getInt("Count(className)");
			}
		} catch (SQLException e) {
			System.out.println("Exception Caught for query " + query + " \n"
					+ e);
			e.printStackTrace();
		}
		return numberOfClasses;
	}

	/**
	 * Queries the database and gets the total number of documents that have been classified till now.
	 * @return
	 */
	public int getTotalNumberOfDocuments() {
		String query = "SELECT SUM(numberOfDocs) from classcontents;";
		int totalNumberOfDocs = 0;
		try {
			Statement statement = connection.createStatement();
			ResultSet resultSet;
			resultSet = statement.executeQuery(query);
			if (resultSet.next()) {
				totalNumberOfDocs = resultSet.getInt("SUM(numberOfDocs)");
			}
		} catch (SQLException e) {
			System.out.println("Exception Caught for query " + query + " \n"
					+ e);
			e.printStackTrace();
		}
		return totalNumberOfDocs;
	}

	/**
	 * Returns the number of docs. that belong the classIds between two numbers.
	 * @param startingClassId
	 * @param endingClassId
	 * @return
	 */
	public ArrayList<Integer> getNumberOfDocuments(int startingClassId,
			int endingClassId) {
		String query = "SELECT numberOfDocs from classcontents where classId>="
				+ startingClassId + " AND classId<=" + endingClassId + ";";
		ArrayList<Integer> classContents = new ArrayList<Integer>();
		try {
			Statement statement = connection.createStatement();
			ResultSet resultSet;
			resultSet = statement.executeQuery(query);
			while (resultSet.next()) {
				classContents.add(resultSet.getInt("numberOfDocs"));
			}
		} catch (SQLException e) {
			System.out.println("Exception Caught for query " + query + " \n"
					+ e);
			e.printStackTrace();
		}
		return classContents;
	}

	/**
	 * Given a classId, this function returns the name of the class.
	 * @param classId
	 * @return
	 */
	public String getClassName(int classId) {
		String query = "SELECT className from classmapping where classId="
				+ classId + ";";
		String className = "";
		try {
			Statement statement = connection.createStatement();
			ResultSet resultSet;
			resultSet = statement.executeQuery(query);
			if (resultSet.next()) {
				className = resultSet.getString("className");
			}
		} catch (SQLException e) {
			System.out.println("Exception Caught for query " + query + " \n"
					+ e);
			e.printStackTrace();
		}
		return className;
	}

	/**
	 * Returns the list of all terms present in the database.
	 * @return
	 */
	public ArrayList<String> getTermsList() {
		String query = "SELECT distinct(feature) from termdistribution;";
		ArrayList<String> termsList = new ArrayList<String>();
		try {
			Statement statement = connection.createStatement();
			ResultSet resultSet;
			resultSet = statement.executeQuery(query);
			while (resultSet.next()) {
				termsList.add(resultSet.getString("feature"));
			}
		} catch (SQLException e) {
			System.out.println("Exception Caught for query " + query + " \n"
					+ e);
			e.printStackTrace();
		}
		return termsList;
	}

	/**
	 * Clears the features table and removes all entries.
	 */
	public void deleteFeatures() {
		String query = "Delete from featurelist";
		try {
			Statement statement = connection.createStatement();
			statement.executeUpdate(query);
		} catch (SQLException e) {
			System.out.println("Exception Caught for query " + query + " \n"
					+ e);
			e.printStackTrace();
		}
	}

	/**
	 * Deletes all those entries where A=0. Those are useless and redundant.
	 * NOTE: Thus function is obsolete.
	 */
	public void deleteEmptyDistributions() {
		String query = "Delete from termdistribution where A=0;";
		try {
			Statement statement = connection.createStatement();
			statement.executeUpdate(query);
		} catch (SQLException e) {
			System.out.println("Exception Caught for query " + query + " \n"
					+ e);
			e.printStackTrace();
		}
	}

	/**
	 * Important Note: Inserting stuff one by one is highly inefficient and is
	 * slow as fuck. Why ? Multiple reads and writes. So, what do we do now ?
	 * Write a single query to do multiple inserts. http://goo.gl/6zVjeQ for
	 * reference. I'm doing 450 inserts in one query because apparently, Sqlite
	 * cuts you off at 500.
	 * 
	 * @param features
	 */
	public void insertFeatures(ArrayList<String> features) {
		String query = "";
		try {
			Statement statement = connection.createStatement();
			for (int i = 0; i < features.size(); i++) {
				if (i % 450 == 0) {
					statement.executeUpdate(query);
					query = "INSERT INTO `featurelist` SELECT '"
							+ features.get(i) + "' AS 'feature' ";
				} else {
					query += "UNION SELECT '" + features.get(i) + "' ";
				}
			}
			statement.executeUpdate(query);
		} catch (SQLException e) {
			System.out.println("Exception Caught for query " + query + " \n"
					+ e);
			e.printStackTrace();
		}
	}

	/**
	 * Given a term and classId, this functions tells you if there's a <term,classId,A> mapping
	 * @param term
	 * @param classId
	 * @return
	 */
	public Boolean isTermPresentInClassDistribution(String term, int classId) {
		String query = "SELECT * from termdistribution where feature='" + term
				+ "' and classId = " + classId + ";";
		Boolean isTermPrsentInDataBase = false;
		try {
			Statement statement = connection.createStatement();
			ResultSet resultSet;
			resultSet = statement.executeQuery(query);
			if (resultSet.next()) {
				isTermPrsentInDataBase = true;
			}
		} catch (SQLException e) {
			System.out.println("Exception Caught for query " + query + " \n"
					+ e);
			e.printStackTrace();
		}
		return isTermPrsentInDataBase;
	}

	/**
	 * Given a classId, the class contents for that classId are incremented.
	 * @param classId
	 */
	public void updateClassContents(int classId) {
		String query = "Update `classcontents` SET numberOfDocs = numberOfDocs + 1 Where classId="
				+ classId + ";";
		try {
			Statement statement = connection.createStatement();
			statement.executeUpdate(query);
		} catch (SQLException e) {
			System.out.println("Exception Caught for query " + query + " \n"
					+ e);
			e.printStackTrace();
		}
	}

	/**
	 * Given a set of terms, the termDistribution table is updated. Old terms are updated and new terms are added.
	 * @param tokens
	 * @param classId
	 */
	public void updateTermDistribution(ArrayList<String> tokens, int classId) {
		ArrayList<String> oldTerms = new ArrayList<String>();
		ArrayList<String> newTerms = new ArrayList<String>();
		for (String term : tokens) {
			if (!isTermPresentInClassDistribution(term, classId)) {
				newTerms.add(term);
			} else {
				oldTerms.add(term);
			}
		}
		updateTermInfo(oldTerms, classId);
		insertTermInfo(newTerms, classId);
	}

	/**
	 * Given a couple of terms and a classId, this function adds the <term,classId,A> mapping
	 * 
	 * @param newTerms
	 * @param classId
	 */
	public void insertTermInfo(ArrayList<String> newTerms, int classId) {
		String query = "";
		try {
			Statement statement = connection.createStatement();
			for (int i = 0; i < newTerms.size(); i++) {
				if (i % 450 == 0) {
					statement.executeUpdate(query);
					query = "INSERT INTO `termdistribution` Select '"
							+ newTerms.get(i) + "' AS `feature`, " + classId
							+ " AS `classId`, 1 AS `A`";
				} else {
					query += "UNION SELECT '" + newTerms.get(i) + "',"
							+ classId + ",1 ";
				}
			}
			statement.executeUpdate(query);
		} catch (SQLException e) {
			System.out.println("Exception Caught for query " + query + " \n"
					+ e);
			e.printStackTrace();
		}
	}

	/**
	 * Increments the count of <term,classId,A> mappings.
	 * @param oldTerms
	 * @param classId
	 */
	public void updateTermInfo(ArrayList<String> oldTerms, int classId) {
		int numOfTerms = oldTerms.size();
		int iterator = 0;
		while (iterator < numOfTerms) {
			String query = "Update termdistribution SET `A` = `A` + 1 Where (feature='"
					+ oldTerms.get(iterator) + "' ";
			iterator++;
			while (iterator % 950 != 0 && iterator < numOfTerms) {
				query += " OR feature='" + oldTerms.get(iterator) + "'";
				iterator++;
			}
			query += ") and classId=" + classId + ";";
			try {
				Statement statement = connection.createStatement();
				statement.executeUpdate(query);
			} catch (SQLException e) {
				System.out.println("Exception Caught for query " + query
						+ " \n" + e);
				e.printStackTrace();
			}
		}
	}

	/**
	 * Basic function that inserts an activityDao into the Database.
	 * @param activityDaos
	 */
	public void insertActivityIntoDB(ArrayList<ActivityDao> activityDaos) {
		String query = "";
		try {
			Statement statement = connection.createStatement();
			for (int i = 0; i < activityDaos.size(); i++) {
				if (i % 450 == 0) {
					statement.executeUpdate(query);
					query = "INSERT INTO `activities`( `activityType`, `activityInfo`, `timeStamp`, `assignedClass`) Select '"
							+ activityDaos.get(i).getActivityType()
							+ "' AS `activityType`, '"
							+ activityDaos.get(i).getActivityInfo()
							+ "' AS `activityInfo`, '"
							+ activityDaos.get(i).getTimeStamp()
							+ "' AS `timeStamp`, '"
							+ activityDaos.get(i).getAssignedClass()
							+ "' AS `assignedClass`";
				} else {
					query += "UNION SELECT '"
							+ activityDaos.get(i).getActivityType() + "','"
							+ activityDaos.get(i).getActivityInfo() + "','"
							+ activityDaos.get(i).getTimeStamp() + "','"
							+ activityDaos.get(i).getAssignedClass() + "'  ";
				}
			}
			statement.executeUpdate(query);
		} catch (SQLException e) {
			System.out.println("Exception Caught for query " + query + " \n"
					+ e);
			e.printStackTrace();
		}
	}

	/**
	 * This function tells you the timeStamp of the last performed activity. Useful while inserting activities to the table. You don't insert something with a higher timestamp than this.
	 * @return
	 */
	public String getMaxActivityTimeStamp() {
		String query = "SELECT MAX(timeStamp) from activities;";
		String maxTimeStamp = "";
		try {
			Statement statement = connection.createStatement();
			ResultSet resultSet;
			resultSet = statement.executeQuery(query);
			if (resultSet.next()) {
				maxTimeStamp = resultSet.getString("MAX(timeStamp)");
			}
		} catch (SQLException e) {
			System.out.println("Exception Caught for query " + query + " \n"
					+ e);
			e.printStackTrace();
		}
		if (maxTimeStamp == null) {
			maxTimeStamp = "0";
		}
		return maxTimeStamp;
	}

	/**
	 * Gets an ArrayList of activityDaos which have not been assigned a class yet.
	 * @return
	 */
	public ArrayList<ActivityDao> getUnclassifiedActivities() {
		String query = "SELECT `activityId`, `activityType`, `activityInfo`, `timeStamp`, `assignedClass` from activities where assignedClass='Not Assigned'";
		ArrayList<ActivityDao> activityDaos = new ArrayList<ActivityDao>();
		try {
			Statement statement = connection.createStatement();
			ResultSet resultSet;
			resultSet = statement.executeQuery(query);
			while (resultSet.next()) {
				int activityId = resultSet.getInt("activityId");
				String activityType = resultSet.getString("activityType");
				String activityInfo = resultSet.getString("activityInfo");
				String timeStamp = resultSet.getString("timeStamp");
				String assignedClass = resultSet.getString("assignedClass");
				ActivityDao activityDao = new ActivityDao(activityId,
						activityType, activityInfo, timeStamp, assignedClass);
				activityDaos.add(activityDao);
			}
		} catch (SQLException e) {
			System.out.println("Exception Caught for query " + query + " \n"
					+ e);
			e.printStackTrace();
		}
		return activityDaos;
	}

	/**
	 * Updates the className field of the activities table.
	 * @param activityDaos
	 */
	public void updateActivities(ArrayList<ActivityDao> activityDaos) {
		String query = "UPDATE `activities` SET `assignedClass` = CASE activityId ";
		String queryPartTwo = "";
		for (ActivityDao activityDao : activityDaos) {
			query += " When " + activityDao.getActivityId() + " THEN '"
					+ activityDao.getAssignedClass() + "'";
			queryPartTwo += activityDao.getActivityId() + ",";
		}
		query += " END WHERE activityId in (" + queryPartTwo + ");";
		query = query.replace(",);", ");");
		System.out.println(query);
		try {
			Statement statement = connection.createStatement();
			statement.executeUpdate(query);
		} catch (SQLException e) {
			System.out.println("Exception Caught for query " + query + " \n"
					+ e);
			e.printStackTrace();
		}
	}

	/*************************************************************************
	 * DB Calls on the temporary table follow:
	 **************************************************************************/

	/**
	 * Gets a list of terms that have been encountered in the user's activities.
	 * @return
	 */
	public ArrayList<String> getTermsInUserData() {
		String query = "SELECT distinct(feature) from userdataterms;";
		ArrayList<String> userTerms = new ArrayList<String>();
		try {
			Statement statement = connection.createStatement();
			ResultSet resultSet;
			resultSet = statement.executeQuery(query);
			while (resultSet.next()) {
				userTerms.add(resultSet.getString("feature"));
			}
		} catch (SQLException e) {
			System.out.println("Exception Caught for query " + query + " \n"
					+ e);
			e.printStackTrace();
		}
		return userTerms;
	}

	/**
	 * Similar to updating the termDistribution table. Different table, same story.
	 * @param tokens
	 * @param classId
	 */
	public void updateUserDataTermDistribution(ArrayList<String> tokens,
			int classId) {
		ArrayList<String> oldTerms = new ArrayList<String>();
		ArrayList<String> newTerms = new ArrayList<String>();
		for (String term : tokens) {
			if (!isTermPresentInUserDataDistribution(term, classId)) {
				newTerms.add(term);
			} else {
				oldTerms.add(term);
			}
		}
		updateUserDataTermInfo(oldTerms, classId);
		insertUserDataTermInfo(newTerms, classId);
	}

	/**
	 * Checks if a term has been encountered in the user's profiled data.
	 * @param term
	 * @param classId
	 * @return
	 */
	public Boolean isTermPresentInUserDataDistribution(String term, int classId) {
		String query = "SELECT * from userdataterms where feature='" + term
				+ "' and classId = " + classId + ";";
		Boolean isTermPrsentInDataBase = false;
		try {
			Statement statement = connection.createStatement();
			ResultSet resultSet;
			resultSet = statement.executeQuery(query);
			if (resultSet.next()) {
				isTermPrsentInDataBase = true;
			}
		} catch (SQLException e) {
			System.out.println("Exception Caught for query " + query + " \n"
					+ e);
			e.printStackTrace();
		}
		return isTermPrsentInDataBase;
	}

	/**
	 * Adds new entries to the userDataTerms table.
	 * @param newTerms
	 * @param classId
	 */
	public void insertUserDataTermInfo(ArrayList<String> newTerms, int classId) {
		String query = "";
		try {
			Statement statement = connection.createStatement();
			for (int i = 0; i < newTerms.size(); i++) {
				if (i % 450 == 0) {
					statement.executeUpdate(query);
					query = "INSERT INTO `userdataterms` Select '"
							+ newTerms.get(i) + "' AS `feature`, " + classId
							+ " AS `classId`, 1 AS `A`";
				} else {
					query += "UNION SELECT '" + newTerms.get(i) + "',"
							+ classId + ",1 ";
				}
			}
			statement.executeUpdate(query);
		} catch (SQLException e) {
			System.out.println("Exception Caught for query " + query + " \n"
					+ e);
			e.printStackTrace();
		}
	}

	/**
	 * Updates existing entries in the userDataTerms table.
	 * @param oldTerms
	 * @param classId
	 */
	public void updateUserDataTermInfo(ArrayList<String> oldTerms, int classId) {
		int numOfTerms = oldTerms.size();
		int iterator = 0;
		while (iterator < numOfTerms) {
			String query = "Update userdataterms SET `A` = `A` + 1 Where (feature='"
					+ oldTerms.get(iterator) + "' ";
			iterator++;
			while (iterator % 950 != 0 && iterator < numOfTerms) {
				query += " OR feature='" + oldTerms.get(iterator) + "'";
				iterator++;
			}
			query += ") and classId=" + classId + ";";
			try {
				Statement statement = connection.createStatement();
				statement.executeUpdate(query);
			} catch (SQLException e) {
				System.out.println("Exception Caught for query " + query
						+ " \n" + e);
				e.printStackTrace();
			}
		}
	}

}
