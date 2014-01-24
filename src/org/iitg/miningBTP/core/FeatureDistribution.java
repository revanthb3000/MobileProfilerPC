package org.iitg.miningBTP.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.iitg.miningBTP.dal.PostingList;

public class FeatureDistribution {

	public class FeatureInfo {
		/**
		 * Class and feature
		 */
		private int A;

		/**
		 * Not class and feature
		 */
		private int B;

		/**
		 * Class and not feature
		 */
		private int C;

		/**
		 * Not Class and not feature.
		 */
		private int D;

		private double chiSquare;

		public FeatureInfo() {
			A = 0;
			B = 0;
			C = 0;
			D = 0;
			chiSquare = 0.0;
		}

		public int getA() {
			return A;
		}

		public void setA(int a) {
			A = a;
		}

		public int getB() {
			return B;
		}

		public void setB(int b) {
			B = b;
		}

		public int getC() {
			return C;
		}

		public void setC(int c) {
			C = c;
		}

		public int getD() {
			return D;
		}

		public void setD(int d) {
			D = d;
		}

		public double getChiSquare() {
			return chiSquare;
		}

		public void setChiSquare(double chiSquare) {
			this.chiSquare = chiSquare;
		}

	}

	private String featureName;

	/**
	 * Document Id to featureInfo
	 */
	public Map<Integer, FeatureInfo> featureClassRelation;
	
	private double giniCoefficient;
	
	public String getFeatureName() {
		return featureName;
	}

	public void setFeatureName(String featureName) {
		this.featureName = featureName;
	}

	public double getGiniCoefficient() {
		return giniCoefficient;
	}

	public void setGiniCoefficient(double giniCoefficient) {
		this.giniCoefficient = giniCoefficient;
	}

	public Map<Integer, FeatureInfo> getFeatureClassRelation() {
		return featureClassRelation;
	}

	public void setFeatureClassRelation(
			Map<Integer, FeatureInfo> featureClassRelation) {
		this.featureClassRelation = featureClassRelation;
	}

	public FeatureDistribution(String feature, int numberOfClasses) {
		featureName = feature;
		giniCoefficient = 0;
		featureClassRelation = new HashMap<Integer, FeatureDistribution.FeatureInfo>();
		for (int i = 0; i < numberOfClasses; i++) {
			FeatureInfo featureInfo = new FeatureInfo();
			featureClassRelation.put(i, featureInfo);
		}
	}

	public void loadStats(PostingList postingList, int numberOfClasses,
			ArrayList<Integer> classContents, int totalNumberOfDocs) {
		for (int i = 0; i < numberOfClasses; i++) {
			int A = getAStat(i, postingList);
			int B = getBStat(i, postingList);
			int C = classContents.get(i) - A;
			int D = totalNumberOfDocs - (A + B + C);
			double chiSquare = calculateChiSquare(A, B, C, D, totalNumberOfDocs);
			featureClassRelation.get(i).setA(A);
			featureClassRelation.get(i).setB(B);
			featureClassRelation.get(i).setC(C);
			featureClassRelation.get(i).setD(D);
			featureClassRelation.get(i).setChiSquare(chiSquare);
		}
	}

	public int getAStat(int classId, PostingList postingList) {
		int A = 0;
		for (int docId : postingList.m.keySet()) {
			if (postingList.m.get(docId).getClassId() == classId) {
				A++;
			}
		}
		return A;
	}

	public int getBStat(int classId, PostingList postingList) {
		int B = 0;
		for (int docId : postingList.m.keySet()) {
			if (postingList.m.get(docId).getClassId() != classId) {
				B++;
			}
		}
		return B;
	}

	public double calculateChiSquare(int A, int B, int C, int D,
			int totalNumberOfDocs) {
		double chiSquare = totalNumberOfDocs;
		chiSquare /= (A + C);
		chiSquare /= (A + B);
		chiSquare *= (A * D - B * C);
		chiSquare /= (B + D);
		chiSquare /= (C + D);
		chiSquare *= (A * D - B * C);
		return chiSquare;
	}

	public String getFeatureDistStringForm() {
		String output = featureName + "-";
		for (int classId : featureClassRelation.keySet()) {
			output += "{" + classId + ":["
//					+ featureClassRelation.get(classId).getChiSquare() + ","
					+ featureClassRelation.get(classId).getA() + ","
					+ featureClassRelation.get(classId).getB() + ","
					+ featureClassRelation.get(classId).getC() + ","
					+ featureClassRelation.get(classId).getD() + "]} ";
		}
		return output.trim();
	}
	
	public String getChiSquareStringForm() {
		String output = featureName + "-";
		for (int classId : featureClassRelation.keySet()) {
			output += "{" + classId + " : " + featureClassRelation.get(classId).getChiSquare() + "} ";
		}
		return output.trim();
	}
	
	public void calculateGiniCoefficient(int numberOfClasses){
		List<Double> chiSquareValues = new ArrayList<Double>();
		double chiSquareMean = 0;
		for(int i : featureClassRelation.keySet()){
			chiSquareValues.add(featureClassRelation.get(i).getChiSquare());
			chiSquareMean += featureClassRelation.get(i).getChiSquare();
		}
		chiSquareMean /= numberOfClasses;
		Collections.sort(chiSquareValues);
		giniCoefficient = 0;
		for(int i=0;i<numberOfClasses;i++){
			giniCoefficient += chiSquareValues.get(i) * (2*(i+1) - numberOfClasses - 1);
		}
		giniCoefficient /= (numberOfClasses*numberOfClasses*chiSquareMean);
	}

}
