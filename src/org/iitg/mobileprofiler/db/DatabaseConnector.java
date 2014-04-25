package org.iitg.mobileprofiler.db;

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

import org.iitg.mobileprofiler.dal.FileStorageUtilities;
import org.iitg.mobileprofiler.preprocessing.FeatureDistribution;

/**
 * This is the main class used to interact with the database. A class with
 * several methods in it. Contains methods for all database queries that will be
 * used.
 * 
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
	 * Does what it says. Calling this function is crucial. Don't want any open
	 * connections lingering around !
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
					+ "(`term` varchar(255) NOT NULL,`classId` int(11) NOT NULL,"
					+ "`A` int(11) NOT NULL,"
					+ "PRIMARY KEY (`term`,`classId`));";
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
			// INTEGER PRIMARY KEY. This is for the AUTO_INCREMENT thing.
			query = "CREATE TABLE IF NOT EXISTS `activities` "
					+ "(`activityId` int(11) NOT NULL,`activityType` varchar(255) NOT NULL,"
					+ "`activityInfo` text NOT NULL,"
					+ "`timeStamp` timestamp NOT NULL DEFAULT CURRENT_TIM8ESTAMP,"
					+ "`assignedClass` varchar(255) NOT NULL,PRIMARY KEY (`activityId`))";
			statement.executeUpdate(query);

			query = "CREATE TABLE IF NOT EXISTS `userdataterms` "
					+ "(`term` varchar(255) NOT NULL,`classId` int(11) NOT NULL,"
					+ "`A` int(11) NOT NULL,"
					+ "PRIMARY KEY (`term`,`classId`));";
			statement.executeUpdate(query);

			query = "CREATE TABLE IF NOT EXISTS `userdataclasscontents` "
					+ "(`classId` int(11) NOT NULL,`numberOfDocs` "
					+ "int(11) NOT NULL,PRIMARY KEY (`classId`));";
			statement.executeUpdate(query);

			// THIS WON'T SUFFICE. OPEN THE SQLITE FILE AND SET questionId to
			// INTEGER PRIMARY KEY. This is for the AUTO_INCREMENT thing.
			query = "CREATE TABLE IF NOT EXISTS `questionmessages` "
					+ "(`questionId` int(11) NOT NULL,"
					+ "`question` varchar(255) NOT NULL,"
					+ "`className` varchar(255) NOT NULL,"
					+ " PRIMARY KEY (`questionId`))";
			statement.executeUpdate(query);

			// THIS WON'T SUFFICE. OPEN THE SQLITE FILE AND SET answerId to
			// INTEGER PRIMARY KEY. This is for the AUTO_INCREMENT thing.
			query = "CREATE TABLE IF NOT EXISTS `answermessages` "
					+ "(`answerId` int(11) NOT NULL,"
					+ "`questionId` int(11) NOT NULL,"
					+ "`answer` int(11) NOT NULL,"
					+ "`similarity` real NOT NULL,"
					+ " PRIMARY KEY (`answerId`))";
			statement.executeUpdate(query);

			// THIS WON'T SUFFICE. OPEN THE SQLITE FILE AND SET responseId to
			// INTEGER PRIMARY KEY. This is for the AUTO_INCREMENT thing.
			query = "CREATE TABLE IF NOT EXISTS `responses` (`responseId` int(11) NOT NULL,"
					+ "`userId` varchar(255) NOT NULL,`question` varchar(255) NOT NULL,"
					+ "`answer` int(11) NOT NULL,`className` varchar(255) NOT NULL)";
			statement.executeUpdate(query);

		} catch (SQLException e) {
			System.out.println("Exception Caught for query " + query + " \n"
					+ e);
			e.printStackTrace();
		}
	}

	/**
	 * Fill the classContents table with the class distribution of the training
	 * dataset.
	 * 
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
	 * Basically fills the userDataClassContents table with initial mappings.
	 * Everything is set to zero.
	 */
	public void fillUserDataClassContents() {
		String query = "";
		try {
			Statement statement = connection.createStatement();
			for (int i = 0; i < getNumberOfClasses(); i++) {
				query = "INSERT INTO `userdataclasscontents`"
						+ " (`classId` ,`numberOfDocs`)" + "VALUES ('" + i
						+ "', '" + 0 + "');";
				statement.executeUpdate(query);
			}
		} catch (SQLException e) {
			System.out.println("Exception Caught for query " + query + " \n"
					+ e);
			e.printStackTrace();
		}
	}

	/**
	 * Fills the mapping tables with values from 1 to 237 along with the
	 * respective class names.
	 * 
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
	 * Takes the training dataset index table and fills this table using that
	 * data.
	 * 
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
						query = "INSERT INTO `termdistribution`(`term` ,`classId` ,`A`) VALUES ('"
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
	 * 
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
	 * Given a word and classId, this function will return a TermDistributionDao
	 * object which will also contain the 'A' value.
	 * 
	 * @param term
	 * @param classId
	 * @param isUserDataTable
	 * @return
	 */
	public TermDistributionDao getTermDistribution(String term, int classId,
			boolean isUserDataTable) {
		String query = "SELECT * from "
				+ (isUserDataTable ? "userdataterms" : "termdistribution")
				+ " where term='" + term + "' AND classId=" + classId + ";";

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
	 * Given a term, this function will return a Map between classId and
	 * termDistributionDao objects. Useful function if you need the distribution
	 * of a term across all classes.
	 * 
	 * @param term
	 * @param isUserDataTable
	 * @return
	 */
	public Map<Integer, TermDistributionDao> getAllTermDistribution(
			String term, boolean isUserDataTable) {
		String query = "SELECT * from "
				+ (isUserDataTable ? "userdataterms" : "termdistribution")
				+ " where term='" + term + "';";

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
	 * Similar to the previous function. Given a set of tokens, a Map is
	 * returned that will contain info of the termDistribution of each term
	 * present in the ArrayList.
	 * 
	 * @param tokens
	 * @param isUserDataTable
	 * @return
	 */
	public Map<String, Map<Integer, TermDistributionDao>> getAllTokensDistribution(
			ArrayList<String> tokens, boolean isUserDataTable) {
		if (tokens.size() == 0) {
			return null;
		}
		String term = tokens.get(0), previousTerm = tokens.get(0);
		String query = "SELECT * from "
				+ (isUserDataTable ? "userdataterms" : "termdistribution")
				+ " where term='" + term + "'";
		for (int i = 1; i < tokens.size(); i++) {
			term = tokens.get(i);
			if (term.equals(previousTerm)) {
				continue;
			}
			previousTerm = term;
			query += " OR term='" + term + "'";
		}
		query += ";";
		Map<String, Map<Integer, TermDistributionDao>> termDistributionMap = new HashMap<String, Map<Integer, TermDistributionDao>>();
		try {
			Statement statement = connection.createStatement();
			ResultSet resultSet;
			resultSet = statement.executeQuery(query);
			while (resultSet.next()) {
				String token = resultSet.getString("term");
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
	 * Given a set of words, this function returns all those terms that are a
	 * part of our feature set.
	 * 
	 * @param tokens
	 * @return
	 */
	public ArrayList<String> getFeaturesFromTokensList(ArrayList<String> tokens) {
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
	 * 
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
	 * 
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
	 * 
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
	 * Queries the database and gets the total number of documents that have
	 * been classified till now.
	 * 
	 * @param isUserDataTable
	 * @return
	 */
	public int getTotalNumberOfDocuments(boolean isUserDataTable) {
		String query = "SELECT SUM(numberOfDocs) from "
				+ (isUserDataTable ? "userdataclasscontents" : "classcontents")
				+ ";";
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
	 * 
	 * @param startingClassId
	 * @param endingClassId
	 * @param isUserDataTable
	 * @return
	 */
	public ArrayList<Integer> getNumberOfDocuments(int startingClassId,
			int endingClassId, boolean isUserDataTable) {
		String query = "SELECT numberOfDocs from "
				+ (isUserDataTable ? "userdataclasscontents" : "classcontents")
				+ " where classId>=" + startingClassId + " AND classId<="
				+ endingClassId + ";";
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
	 * 
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
	 * 
	 * @param isUserDataTable
	 * @return
	 */
	public ArrayList<String> getTermsList(boolean isUserDataTable) {
		String query = "SELECT distinct(term) from "
				+ (isUserDataTable ? "userdataterms" : "termdistribution")
				+ ";";
		ArrayList<String> termsList = new ArrayList<String>();
		try {
			Statement statement = connection.createStatement();
			ResultSet resultSet;
			resultSet = statement.executeQuery(query);
			while (resultSet.next()) {
				termsList.add(resultSet.getString("term"));
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
	 * Given a term and classId, this functions tells you if there's a
	 * <term,classId,A> mapping
	 * 
	 * @param term
	 * @param classId
	 * @param isUserDataTable
	 * @return
	 */
	public Boolean isTermPresentInTermDistribution(String term, int classId,
			boolean isUserDataTable) {
		String query = "SELECT * from "
				+ (isUserDataTable ? "userdataterms" : "termdistribution")
				+ " where term='" + term + "' and classId = " + classId + ";";
		Boolean isTermPresent = false;
		try {
			Statement statement = connection.createStatement();
			ResultSet resultSet;
			resultSet = statement.executeQuery(query);
			if (resultSet.next()) {
				isTermPresent = true;
			}
		} catch (SQLException e) {
			System.out.println("Exception Caught for query " + query + " \n"
					+ e);
			e.printStackTrace();
		}
		return isTermPresent;
	}

	/**
	 * Given a classId, the class contents for that classId are incremented.
	 * 
	 * @param classId
	 */
	public void updateClassContents(int classId, boolean isUserDataTable) {
		String query = "Update `"
				+ (isUserDataTable ? "userdataclasscontents" : "classcontents")
				+ "` SET numberOfDocs = numberOfDocs + 1 Where classId="
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
	 * Given a set of terms, the termDistribution table is updated. Old terms
	 * are updated and new terms are added.
	 * 
	 * @param tokens
	 * @param classId
	 */
	public void updateTermDistribution(ArrayList<String> tokens, int classId,
			boolean isUserDataTable) {
		ArrayList<String> oldTerms = new ArrayList<String>();
		ArrayList<String> newTerms = new ArrayList<String>();
		for (String term : tokens) {
			if (!isTermPresentInTermDistribution(term, classId, isUserDataTable)) {
				newTerms.add(term);
			} else {
				oldTerms.add(term);
			}
		}
		updateTermInfo(oldTerms, classId, isUserDataTable);
		insertTermInfo(newTerms, classId, isUserDataTable);
	}

	/**
	 * Given a couple of terms and a classId, this function adds the
	 * <term,classId,A> mapping
	 * 
	 * @param newTerms
	 * @param classId
	 * @param isUserDataTable
	 */
	public void insertTermInfo(ArrayList<String> newTerms, int classId,
			boolean isUserDataTable) {
		String query = "";
		try {
			Statement statement = connection.createStatement();
			for (int i = 0; i < newTerms.size(); i++) {
				if (i % 450 == 0) {
					statement.executeUpdate(query);
					query = "INSERT INTO `"
							+ (isUserDataTable ? "userdataterms"
									: "termdistribution") + "` Select '"
							+ newTerms.get(i) + "' AS `term`, " + classId
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
	 * 
	 * @param oldTerms
	 * @param classId
	 */
	public void updateTermInfo(ArrayList<String> oldTerms, int classId,
			boolean isUserDataTable) {
		int numOfTerms = oldTerms.size();
		int iterator = 0;
		while (iterator < numOfTerms) {
			String query = "Update "
					+ (isUserDataTable ? "userdataterms" : "termdistribution")
					+ " SET `A` = `A` + 1 Where (term='"
					+ oldTerms.get(iterator) + "' ";
			iterator++;
			while (iterator % 950 != 0 && iterator < numOfTerms) {
				query += " OR term='" + oldTerms.get(iterator) + "'";
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

	/*************************************************************************
	 * Methods that work on the activities table follow
	 **************************************************************************/

	/**
	 * Basic function that inserts an activityDao into the Database.
	 * 
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
	 * This function tells you the timeStamp of the last performed activity.
	 * Useful while inserting activities to the table. You don't insert
	 * something with a higher timestamp than this.
	 * 
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
	 * Gets an ArrayList of activityDaos which have not been assigned a class
	 * yet.
	 * 
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
	 * 
	 * @param activityDaos
	 */
	public void updateActivities(ArrayList<ActivityDao> activityDaos) {
		if (activityDaos.size() == 0) {
			return;
		}
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
	 * Methods that work on the messages tables follow
	 **************************************************************************/

	/**
	 * Basic question that adds a question asked by our user to the
	 * questionmessages table.
	 * 
	 * @param question
	 */
	public void addQuestion(String question, String className) {
		String query = "INSERT INTO `questionmessages`(`question`,`className`) VALUES(\""
				+ question + "\",\"" + className + "\");";
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
	 * 
	 * When an answer is received, we store it here.
	 * 
	 * @param questionId
	 * @param answer
	 * @param similarity
	 */
	public void addAnswer(int questionId, int answer, Double similarity) {
		String query = "INSERT INTO `answermessages`(`questionId`,`answer`,`similarity`) VALUES("
				+ questionId + "," + answer + "," + similarity + ");";
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
	 * Get the questionId of the last asked question.
	 * 
	 * @return
	 */
	public int getMaxQuestionId() {
		String query = "SELECT MAX(questionId) from questionmessages;";
		int questionId = 0;
		try {
			Statement statement = connection.createStatement();
			ResultSet resultSet;
			resultSet = statement.executeQuery(query);
			while (resultSet.next()) {
				questionId = resultSet.getInt("MAX(questionId)");
			}
		} catch (SQLException e) {
			System.out.println("Exception Caught for query " + query + " \n"
					+ e);
			e.printStackTrace();
		}
		return questionId;
	}

	/**
	 * Given a questionId, I'll return the weighted answer
	 * 
	 * @return
	 */
	public Double getWeightedAnswer(int questionId) {
		String query = "Select * from `answermessages` WHERE questionId = "
				+ questionId + ";";
		Double totalSimilarity = 0.0;
		Double weightedAnswer = 0.0;
		try {
			Statement statement = connection.createStatement();
			ResultSet resultSet;
			resultSet = statement.executeQuery(query);
			while (resultSet.next()) {
				int answer = resultSet.getInt("answer");
				Double similarity = resultSet.getDouble("similarity");
				weightedAnswer += (answer * similarity);
				totalSimilarity += similarity;
			}
			if (totalSimilarity == 0.0) {
				weightedAnswer = 0.0;
			} else {
				weightedAnswer = weightedAnswer / totalSimilarity;
			}
		} catch (SQLException e) {
			System.out.println("Exception Caught for query " + query + " \n"
					+ e);
			e.printStackTrace();
		}
		return weightedAnswer;
	}

	/**
	 * Given a questionId, this will return the question. Null, if not.
	 * 
	 * @param questionId
	 * @return
	 */
	public String getQuestion(int questionId) {
		String query = "Select question from `questionmessages` WHERE questionId = "
				+ questionId + ";";
		String question = null;
		try {
			Statement statement = connection.createStatement();
			ResultSet resultSet;
			resultSet = statement.executeQuery(query);
			while (resultSet.next()) {
				question = resultSet.getString("question");
			}
		} catch (SQLException e) {
			System.out.println("Exception Caught for query " + query + " \n"
					+ e);
			e.printStackTrace();
		}
		return question;
	}

	/*************************************************************************
	 * Queries that run on the response table follow.
	 **************************************************************************/

	/**
	 * Given response data, this query inserts it into the table.
	 * 
	 * @param userId
	 * @param question
	 * @param answer
	 * @param className
	 */
	public void insertResponse(String userId, String question, int answer,
			String className) {
		String query = "INSERT INTO `responses` (`userId` ,`question` ,`answer` ,`className`)"
				+ "VALUES ('"
				+ userId
				+ "', '"
				+ question
				+ "', '"
				+ answer
				+ "', '" + className + "');";
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
	 * Given an arraylist of response daos, this function will insert them into
	 * the DB.
	 * 
	 * @param responseDaos
	 */
	public void insertResponses(ArrayList<ResponseDao> responseDaos) {
		String query = "";
		try {
			Statement statement = connection.createStatement();
			for (int i = 0; i < responseDaos.size(); i++) {
				if (i % 450 == 0) {
					statement.executeUpdate(query);
					query = "INSERT INTO `responses` (`userId` ,`question` ,`answer` ,`className`) Select '"
							+ responseDaos.get(i).getUserId()
							+ "' AS `userId`, '"
							+ responseDaos.get(i).getQuestion()
							+ "' AS `question`, '"
							+ responseDaos.get(i).getAnswer()
							+ "' AS `answer`, '"
							+ responseDaos.get(i).getClassName()
							+ "' AS `className`";
				} else {
					query += "UNION SELECT '" + responseDaos.get(i).getUserId()
							+ "','" + responseDaos.get(i).getQuestion() + "','"
							+ responseDaos.get(i).getAnswer() + "','"
							+ responseDaos.get(i).getClassName() + "'  ";
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
	 * Gets the max responseId present in the table (we remove the user's Id
	 * from consideration)
	 * 
	 * @return
	 */
	public int getMaxResponseId(String blacklistUserId) {
		String query = "SELECT MAX(responseId) from responses WHERE userId!=\""
				+ blacklistUserId + "\";";
		int responseId = 0;
		try {
			Statement statement = connection.createStatement();
			ResultSet resultSet;
			resultSet = statement.executeQuery(query);
			while (resultSet.next()) {
				responseId = resultSet.getInt("MAX(responseId)");
			}
		} catch (SQLException e) {
			System.out.println("Exception Caught for query " + query + " \n"
					+ e);
			e.printStackTrace();
		}
		return responseId;
	}

	/**
	 * Given a response Dao, this function returns the maximum responseId.
	 * @param responseDao
	 * @return
	 */
	public int getResponseIdGivenDao(ResponseDao responseDao) {
		String query = "SELECT MAX(responseId) from responses WHERE userId=\""
				+ responseDao.getUserId() + "\" AND question=\""
				+ responseDao.getQuestion() + "\"" + " AND answer="
				+ responseDao.getAnswer() + " AND className=\""
				+ responseDao.getClassName() + "\" AND ;";
		int responseId = 0;
		try {
			Statement statement = connection.createStatement();
			ResultSet resultSet;
			resultSet = statement.executeQuery(query);
			while (resultSet.next()) {
				responseId = resultSet.getInt("MAX(responseId)");
			}
		} catch (SQLException e) {
			System.out.println("Exception Caught for query " + query + " \n"
					+ e);
			e.printStackTrace();
		}
		return responseId;
	}

	/**
	 * Given a startingId and an endingId, this query will return an ArrayList
	 * of Responses.
	 * 
	 * @param startingId
	 * @param endingId
	 * @return
	 */
	public ArrayList<ResponseDao> getResponses(int startingId, int endingId) {
		ArrayList<ResponseDao> responseDaos = new ArrayList<ResponseDao>();
		String query = "Select * from `responses` where responseId>="
				+ startingId + " AND responseId<=" + endingId + "";
		try {
			Statement statement = connection.createStatement();
			ResultSet resultSet;
			resultSet = statement.executeQuery(query);
			while (resultSet.next()) {
				String userId = resultSet.getString("userId");
				String question = resultSet.getString("question");
				int answer = resultSet.getInt("answer");
				String className = resultSet.getString("className");
				responseDaos.add(new ResponseDao(userId, question, answer,
						className));
			}
		} catch (SQLException e) {
			System.out.println("Exception Caught for query " + query + " \n"
					+ e);
			e.printStackTrace();
		}
		return responseDaos;
	}

	/**
	 * Given a question, this function returns all responses.
	 * 
	 * @param question
	 * @return
	 */
	public ArrayList<ResponseDao> getAnswersOfQuestion(String question) {
		String query = "Select * from `responses` Where question='" + question
				+ "';";
		ArrayList<ResponseDao> responseDaos = new ArrayList<ResponseDao>();
		try {
			Statement statement = connection.createStatement();
			ResultSet resultSet;
			resultSet = statement.executeQuery(query);
			while (resultSet.next()) {
				String userId = resultSet.getString("userId");
				int answer = resultSet.getInt("answer");
				String className = resultSet.getString("className");
				responseDaos.add(new ResponseDao(userId, question, answer,
						className));
			}
		} catch (SQLException e) {
			System.out.println("Exception Caught for query " + query + " \n"
					+ e);
			e.printStackTrace();
		}
		return responseDaos;
	}

	/**
	 * Returns a list of unique questions in repo.
	 * 
	 * @return
	 */
	public ArrayList<String> getQuestionsList() {
		String query = "Select distinct(question) from `responses`;";
		ArrayList<String> questions = new ArrayList<String>();
		try {
			Statement statement = connection.createStatement();
			ResultSet resultSet;
			resultSet = statement.executeQuery(query);
			while (resultSet.next()) {
				String question = resultSet.getString("question");
				questions.add(question);
			}
		} catch (SQLException e) {
			System.out.println("Exception Caught for query " + query + " \n"
					+ e);
			e.printStackTrace();
		}
		return questions;
	}

}
