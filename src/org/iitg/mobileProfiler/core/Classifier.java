package org.iitg.mobileProfiler.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.iitg.mobileProfiler.db.DatabaseConnector;
import org.iitg.mobileProfiler.db.TermDistributionDao;

public class Classifier {

	private int totalNumberOfDocs;

	private int numberOfClasses;

	private ArrayList<Integer> classContents;

	private ArrayList<Integer> userDataClassContents;

	private DatabaseConnector databaseConnector;

	private final int SUPPORT_FACTOR = 5;

	private final double GINI_THRESHOLD = 0.95;

	public Classifier(DatabaseConnector inputDatabaseConnector) {
		this.databaseConnector = inputDatabaseConnector;
		this.totalNumberOfDocs = this.databaseConnector
				.getTotalNumberOfDocuments();
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
				if (!termDistributions.containsKey(token)) {
					continue;
				}
				if (termDistributions.get(token).containsKey(i)) {
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
				if (!userDataTermDistributions.containsKey(token)) {
					continue;	//Basically, if this feature is not present in the user data, we ignore it.
				}
				if (userDataTermDistributions.get(token).containsKey(i)) {
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

	public void recomputeFeatures() {
		databaseConnector.deleteFeatures();
		databaseConnector.insertFeatures(calculateFeaturesList());
	}

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
			if (term.equals("revanth")) {
				System.out.println("Howdy !! " + giniCoefficient);
			}
			termGiniMapping.put(term, giniCoefficient);
		}
		sortedTermGiniMapping.putAll(termGiniMapping);
		return sortedTermGiniMapping;
	}

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

	public double calculateChiSquare(int A, int B, int classId) {
		int C = (classContents.get(classId) + userDataClassContents
				.get(classId)) - A;
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
}

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
