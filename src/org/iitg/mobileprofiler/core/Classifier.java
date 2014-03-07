package org.iitg.mobileprofiler.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.iitg.mobileprofiler.db.DatabaseConnector;
import org.iitg.mobileprofiler.db.TermDistributionDao;

/**
 * This class does all work related to classification. Gini, Naive Bayes - all that stuff goes here.
 * Usage is simple. Create a new instance of classifier by giving an active DBConnection to the constructor 
 * and then use the classifyDoc function. It's important to create a new instance of the classifier every time.
 * Don't use the same object over and over again. Why ? Because the class members aren't updated.
 * @author RB
 *
 */
public class Classifier {

	/**
	 * Total number of documents present in the DB.
	 */
	private int totalNumberOfDocs;

	/**
	 * The number of classes. This is pretty much a constant.
	 */
	private int numberOfClasses;

	/**
	 * A mapping between classId and number of docs classified to that class. Related to the original dataset.
	 */
	private ArrayList<Integer> classContents;

	/**
	 * A mapping between classId and number of docs classified to that class. This Map is for the user data contents.
	*/
	private ArrayList<Integer> userDataClassContents;

	/**
	 * The databaseConnector. This serves as a conduit to the DB.
	 */
	private DatabaseConnector databaseConnector;

	/**
	 * Used while calculating GiniCoefficient. If a term doesn't satisfy the support factor constraint, we assign it a gini value of 0.
	 */
	private final int SUPPORT_FACTOR = 5;

	/**
	 * The minimum gini value required for a term to be considered as a feature.
	 */
	private final double GINI_THRESHOLD = 0.95;

	public Classifier(DatabaseConnector inputDatabaseConnector) {
		this.databaseConnector = inputDatabaseConnector;
		int numberOfUserDocs = this.databaseConnector.getTotalNumberOfDocuments(true);
		int numberOfDatasetDocs = this.databaseConnector.getTotalNumberOfDocuments(false);
		this.totalNumberOfDocs = numberOfUserDocs + numberOfDatasetDocs;
		this.numberOfClasses = this.databaseConnector.getNumberOfClasses();
		this.classContents = this.databaseConnector.getNumberOfDocuments(0,
				this.numberOfClasses, false);
		this.userDataClassContents = this.databaseConnector
				.getNumberOfDocuments(0, this.numberOfClasses, true);
	}

	public int getSupportFactor() {
		return SUPPORT_FACTOR;
	}

	public double getGiniThreshold() {
		return GINI_THRESHOLD;
	}

	public void closeDBConnection() {
		databaseConnector.closeDBConnection();
	}

	/**
	 * The key part of this class. This function takes in a set of tokens, runs the classifier on them and returns the classId to which they're classified.
	 * //TODO Fix a logic to be followed and describe it over here.
	 * @param tokens
	 * @return
	 */
	public int classifyDoc(ArrayList<String> tokens) {
		int numOfMatchedFeatures = 0;
		ArrayList<Double> datasetClassifierProbabilities = new ArrayList<Double>();
		ArrayList<Double> userDataClassifierProbabilities = new ArrayList<Double>();
		Double temp = 0.0;
		for (int i = 0; i < numberOfClasses; i++) {
			temp = (1.0 * classContents.get(i))/* / totalNumberOfDocs */;
			datasetClassifierProbabilities.add(temp);
			temp = (1.0 * userDataClassContents.get(i))/* / totalNumberOfDocs */;
			userDataClassifierProbabilities.add(temp);
		}

		Map<String, Map<Integer, TermDistributionDao>> termDistributions = databaseConnector.getAllTokensDistribution(tokens, false);
		Map<String, Map<Integer, TermDistributionDao>> userDataTermDistributions = databaseConnector.getAllTokensDistribution(tokens, true);

		
		ArrayList<Double> productProbabilities = new ArrayList<Double>();
		for(int i=0;i<numberOfClasses;i++){
			productProbabilities.add(-1.0);
		}
		for (String token : tokens) {
			numOfMatchedFeatures++;
			for (int i = 0; i < numberOfClasses; i++) {
				int termDistA = 0;
				if ((termDistributions.containsKey(token))&&(termDistributions.get(token).containsKey(i))) {
					termDistA = termDistributions.get(token).get(i).getA();
				}
				temp = 1000 * ((1.0 * (1 + termDistA)) / (classContents.get(i) + numberOfClasses));
				if(productProbabilities.get(i)<0){
					productProbabilities.set(i, temp);
				}
				else{
					productProbabilities.set(i, temp*productProbabilities.get(i));
				}
			}
		}
		for(int i=0;i<numberOfClasses;i++){
			temp = productProbabilities.get(i);
			if(temp<0){
				temp = 0.0;
			}
			datasetClassifierProbabilities.set(i, temp);
		}
		
		productProbabilities.clear();
		for(int i=0;i<numberOfClasses;i++){
			productProbabilities.add(-1.0);
		}
		for (String token : tokens) {
			numOfMatchedFeatures++;
			for (int i = 0; i < numberOfClasses; i++) {
				int termDistA = 0;
				if ((userDataTermDistributions.containsKey(token))&&(userDataTermDistributions.get(token).containsKey(i))) {
					termDistA = userDataTermDistributions.get(token).get(i).getA();
				}
				temp = 1000 * ((1.0 * (1 + termDistA)) / (userDataClassContents.get(i) + numberOfClasses));
				if(productProbabilities.get(i)<0){
					productProbabilities.set(i, temp);
				}
				else{
					productProbabilities.set(i, temp*productProbabilities.get(i));
				}
			}
		}
		for(int i=0;i<numberOfClasses;i++){
			temp = productProbabilities.get(i);
			if(temp<0){
				temp = 0.0;
			}
			userDataClassifierProbabilities.set(i, temp);
		}
		
		if (numOfMatchedFeatures == 0) {
			return -1;
		}
		ArrayList<Double> combinedProbabilities = new ArrayList<Double>();
		for (int i = 0; i < numberOfClasses; i++) {
			double alpha = 1.0, beta = 0.0;
			double c1 = classContents.get(i);
			double c2 = userDataClassContents.get(i);
			if(c2>=5){
				if(c2>=c1){
					alpha = 0.5;
					beta = 0.5;
				}
				else{
					beta = c1/(c1+c2);
					alpha = c2/(c1+c2);
				}
			}
			Double combinedProbability = (alpha * datasetClassifierProbabilities.get(i)) + (beta * userDataClassifierProbabilities.get(i));
			combinedProbabilities.add(combinedProbability);
		}

		int maxIndex = 0;
		Double maximumValue = -1.0;
		for (int i = 0; i < numberOfClasses; i++) {
			if (combinedProbabilities.get(i) > maximumValue) {
				maximumValue = combinedProbabilities.get(i);
				maxIndex = i;
			}
		}
		return maxIndex;
	}

	/**
	 * This method is used to calculate the Chi-squared values given the values of A,B for a given classId.
	 * Measures the level of dependence between a term and a classId.
	 * A - number of documents belonging to the given classId which contain our term.
	 * B - number of documents belonging to a different class which contain our term.
	 * C - number of documents belonging to the given classId which do not contain our term.
	 * D - number of documents belonging to a different class which do not contain our term.
	 * @param A
	 * @param B
	 * @param classId
	 * @return
	 */
	public double calculateChiSquare(int A, int B, int classId) {
		int C = (classContents.get(classId) + userDataClassContents.get(classId)) - A;
		int D = totalNumberOfDocs - (A + B + C);
		double chiSquare = totalNumberOfDocs;
		chiSquare /= (A + C);
		chiSquare /= (A + B);
		chiSquare *= (A * D - B * C);
		chiSquare /= (B + D);
		chiSquare /= (C + D);
		chiSquare *= (A * D - B * C);
		return chiSquare;
	}

	/**
	 * This function calculates the gini coefficient of a given term.
	 * It is used to measure the global goodness of a term.
	 * Gini coefficient of a term that doesn't satisfy the support factor constraint is taken to be 0.
	 * @param term
	 * @return
	 */
	public double calculateGiniCoefficient(String term) {
		Map<Integer, TermDistributionDao> termDistributionDaos = databaseConnector
				.getAllTermDistribution(term, false);
		Map<Integer, TermDistributionDao> userDataTermDistributionDaos = databaseConnector
				.getAllTermDistribution(term, true);
		double giniCoefficient = 0.0;
		List<Double> chiSquareValues = new ArrayList<Double>();
		double chiSquareMean = 0;
		int totalNumberOfOccurences = 0;
		TermDistributionDao termDistributionDao = null, userDataTermDistributionDao = null;
		Set<Integer> classIdSet = new HashSet<Integer>();
		for (Integer classId : termDistributionDaos.keySet()) {
			classIdSet.add(classId);
		}
		for (Integer classId : userDataTermDistributionDaos.keySet()) {
			classIdSet.add(classId);
		}
		for (int classId : classIdSet) {
			termDistributionDao = termDistributionDaos.get(classId);
			userDataTermDistributionDao = userDataTermDistributionDaos
					.get(classId);
			if (termDistributionDao != null) {
				totalNumberOfOccurences += termDistributionDao.getA();
			}
			if (userDataTermDistributionDao != null) {
				totalNumberOfOccurences += userDataTermDistributionDao.getA();
			}
		}
		// If support factor constraint is not satisfied, I'm getting rid of the
		// term.
		if (totalNumberOfOccurences < SUPPORT_FACTOR) {
			return 0.0;
		}
		for (int classId = 0; classId < numberOfClasses; classId++) {
			int A = 0;
			if (termDistributionDaos.containsKey(classId)) {
				A += termDistributionDaos.get(classId).getA();
			}
			if (userDataTermDistributionDaos.containsKey(classId)) {
				A += userDataTermDistributionDaos.get(classId).getA();
			}
			double chiSquare = calculateChiSquare(A, totalNumberOfOccurences
					- A, classId);
			chiSquareValues.add(chiSquare);
			chiSquareMean += chiSquare;
		}
		chiSquareMean /= numberOfClasses;
		Collections.sort(chiSquareValues);
		giniCoefficient = 0;
		for (int i = 0; i < numberOfClasses; i++) {
			giniCoefficient += chiSquareValues.get(i)
					* (2 * (i + 1) - numberOfClasses - 1);
		}
		giniCoefficient /= (numberOfClasses * numberOfClasses * chiSquareMean);
		return giniCoefficient;
	}
	
	/**
	 * This method takes in all the unique terms present in our DB, calculates the Gini Coefficient of each term.
	 * If the term satisfies both the Gini Coefficient and Support factor requirements, it's considered to be a feature.
	 * @return
	 */
	public ArrayList<String> calculateFeaturesList() {
		ArrayList<String> termsList = databaseConnector.getTermsList(false);
		ArrayList<String> userDataTermsList = databaseConnector
				.getTermsList(true);
		for (String term : userDataTermsList) {
			if (!termsList.contains(term)) {
				termsList.add(term);
			}
		}
		ArrayList<String> featuresList = new ArrayList<String>();
		for (String term : termsList) {
			double giniCoefficient = calculateGiniCoefficient(term);
			if (giniCoefficient >= GINI_THRESHOLD) {
				featuresList.add(term);
			}
		}
		return featuresList;
	}
	

	/**
	 * Flushes the 'features' table and adds the newly computed features.
	 */
	public void recomputeFeatures() {
		databaseConnector.deleteFeatures();
		databaseConnector.insertFeatures(calculateFeaturesList());
	}

	/**
	 * Simple method that calculates the gini values for each term and returns that mapping.
	 * Uses the ValueComparator class to get the values in a descending order.
	 * Perfect for testing and analysis.
	 * @return
	 */
	public Map<String, Double> getGiniMapping() {
		ArrayList<String> termsList = databaseConnector.getTermsList(false);
		ArrayList<String> userDataTermsList = databaseConnector
				.getTermsList(true);
		for (String term : userDataTermsList) {
			if (!termsList.contains(term)) {
				termsList.add(term);
			}
		}
		Map<String, Double> termGiniMapping = new HashMap<String, Double>();
		ValueComparator valueComparator = new ValueComparator(termGiniMapping);
		TreeMap<String, Double> sortedTermGiniMapping = new TreeMap<String, Double>(
				valueComparator);
		for (String term : termsList) {
			double giniCoefficient = calculateGiniCoefficient(term);
			termGiniMapping.put(term, giniCoefficient);
		}
		sortedTermGiniMapping.putAll(termGiniMapping);
		return sortedTermGiniMapping;
	}
	
}

/**
 * Just a comparator class that is used to obtain a mapping in descending order of value.
 * @author RB
 *
 */
class ValueComparator implements Comparator<String> {
	Map<String, Double> base;

	public ValueComparator(Map<String, Double> base) {
		this.base = base;
	}

	/**
	 * Why do I have a minus sign in there ? It's to get my things in a
	 * descending order.
	 */
	public int compare(String a, String b) {
		return -Double.compare(base.get(a), base.get(b));
	}
}
