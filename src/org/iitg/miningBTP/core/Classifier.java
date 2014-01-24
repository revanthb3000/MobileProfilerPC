package org.iitg.miningBTP.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.iitg.miningBTP.db.DatabaseConnector;
import org.iitg.miningBTP.db.TermDistributionDao;

public class Classifier {

	private int totalNumberOfDocs;

	private int numberOfClasses;

	private ArrayList<Integer> classContents;

	private DatabaseConnector databaseConnector;

	private final int SUPPORT_FACTOR = 20;

	public Classifier(DatabaseConnector inputDatabaseConnector) {
		this.databaseConnector = inputDatabaseConnector;
		this.totalNumberOfDocs = this.databaseConnector
				.getTotalNumberOfDocuments();
		this.numberOfClasses = this.databaseConnector.getNumberOfClasses();
		this.classContents = this.databaseConnector.getNumberOfDocuments(0,
				this.totalNumberOfDocs);
	}

	public void closeDBConnection() {
		databaseConnector.closeDBConnection();
	}

	public int classifyDoc(ArrayList<String> tokens) {
		int numOfMatchedFeatures = 0;
		ArrayList<Double> probabilities = new ArrayList<Double>();
		Double temp = 0.0;
		for (int i = 0; i < numberOfClasses; i++) {
			temp = (1.0 * classContents.get(i))/* / totalNumberOfDocs */;
			probabilities.add(temp);
		}

		Map<String, Map<Integer, TermDistributionDao>> termDistributions = databaseConnector
				.getAllTokensDistribution(tokens);

		for (String token : tokens) {
			numOfMatchedFeatures++;
			for (int i = 0; i < numberOfClasses; i++) {
				int termDistA = 0;
				if (termDistributions.get(token).containsKey(i)) {
					termDistA = termDistributions.get(token).get(i).getA();
				}
				temp = 1000 * ((1.0 * (1 + termDistA)) / (classContents.get(i) + numberOfClasses));
				probabilities.set(i, temp * probabilities.get(i));
			}
		}
		if (numOfMatchedFeatures == 0) {
			return -1;
		}
		int maxIndex = 0;
		Double maximumValue = -1.0;
		for (int i = 0; i < numberOfClasses; i++) {
			if (probabilities.get(i) > maximumValue) {
				maximumValue = probabilities.get(i);
				maxIndex = i;
			}
		}
		return maxIndex;
	}

	public void recomputeFeatures() {
		databaseConnector.deleteFeatures();
		databaseConnector.insertFeatures(getFeaturesList());
	}

	public ArrayList<String> getFeaturesList() {
		ArrayList<String> termsList = databaseConnector
				.getTermsList();
		ArrayList<String> featuresList = new ArrayList<String>();
		for (String term : termsList) {
			double giniCoefficient = calculateGiniCoefficient(term);
			if (giniCoefficient >= 0.95) {
				featuresList.add(term);
			}
		}
		return featuresList;
	}

	public double calculateGiniCoefficient(String term) {
		Map<Integer, TermDistributionDao> termDistributionDaos = databaseConnector
				.getAllTermDistribution(term);
		double giniCoefficient = 0.0;
		List<Double> chiSquareValues = new ArrayList<Double>();
		double chiSquareMean = 0;
		int totalNumberOfOccurences = 0;
		TermDistributionDao termDistributionDao = null;
		for (int classId : termDistributionDaos.keySet()) {
			termDistributionDao = termDistributionDaos.get(classId);
			totalNumberOfOccurences += termDistributionDao.getA();
		}
		//If support factor constraint is not satisfied, I'm getting rid of the term.
		if (totalNumberOfOccurences < SUPPORT_FACTOR) {
			return 0.0;
		}
		for (int classId = 0; classId < numberOfClasses; classId++) {
			int A = 0;
			if (termDistributionDaos.containsKey(classId)) {
				A = termDistributionDaos.get(classId).getA();
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
		int C = classContents.get(classId) - A;
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

/*
 * THIS FUNCTION IS NOW OBSOLETE
 * 
 * public int classifyDocIndividualTerms(ArrayList<String> tokens) { int
 * numOfMatchedFeatures = 0; ArrayList<Double> probabilities = new
 * ArrayList<Double>(); Double temp = 0.0; for (int i = 0; i < numberOfClasses;
 * i++) { temp = (1.0 * classContents.get(i)); probabilities.add(temp); }
 * 
 * ArrayList<TermDistributionDao> termDistributionDaos = null; String
 * previousToken = ""; for (String token : tokens) {
 * if(!(token.equals(previousToken))){ termDistributionDaos =
 * databaseConnector.getAllTermDistribution(token); } numOfMatchedFeatures++;
 * for (int i = 0; i < numberOfClasses; i++) { int termDistA =
 * termDistributionDaos.get(i).getA(); temp = 1000 * ((1.0 * (1 + termDistA)) /
 * (classContents.get(i) + numberOfClasses)); probabilities.set(i,
 * temp*probabilities.get(i)); } } if (numOfMatchedFeatures == 0) { return -1; }
 * int maxIndex = 0; Double maximumValue = -1.0; for (int i = 0; i <
 * numberOfClasses; i++) { if (probabilities.get(i) > maximumValue) {
 * maximumValue = probabilities.get(i); maxIndex = i; } } return maxIndex; }
 */