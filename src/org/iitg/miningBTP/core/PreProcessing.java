package org.iitg.miningBTP.core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.ardverk.collection.Trie;
import org.iitg.miningBTP.dal.FileStorageUtilities;
import org.iitg.miningBTP.dal.PostingList;

public class PreProcessing {
	
	public static void getClassContentInfo() throws IOException{
		File file = new File("trainingPages");
		int numberOfPages = 0;
		List<Integer> classContents = new ArrayList<Integer>();
		for (File fileName : file.listFiles()) {
			numberOfPages = getNumberOfPages(fileName);
			classContents.add(numberOfPages);
		}
		FileWriter fileWriter = new FileWriter("classInfo.dat");
		BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
		for(int i =0;i<classContents.size();i++){
			bufferedWriter.write(i + " - " + classContents.get(i) + "\n");
		}
		bufferedWriter.close();
		fileWriter.close();
	}

	public static void calculateClassProbability() throws IOException{
		File file = new File("trainingPages");
		int numberOfPages = 0,totalNumberOfPages = 0;
		List<Integer> classContents = new ArrayList<Integer>();
		for (File fileName : file.listFiles()) {
			numberOfPages = getNumberOfPages(fileName);
			classContents.add(numberOfPages);
			totalNumberOfPages += numberOfPages;
		}
		FileWriter fileWriter = new FileWriter("classProbability.dat");
		BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
		for(int i =0;i<classContents.size();i++){
			bufferedWriter.write(i + " - " + ((1.0*classContents.get(i))/totalNumberOfPages) + "\n");
		}
		bufferedWriter.close();
		fileWriter.close();
	}

	public static void splitFiles() throws IOException {
		File file = new File("webpages");
		int numberOfPages = 0;
		int numOfTrainingPages = 0, numOfTestingPages = 0;
		for (File fileName : file.listFiles()) {
			numberOfPages = getNumberOfPages(fileName);
			numOfTrainingPages = (int) (0.8 * numberOfPages);
			numOfTestingPages = numberOfPages - numOfTrainingPages;
			splitFile(fileName, numOfTrainingPages, numOfTestingPages);
		}
	}

	public static ArrayList<Integer> getClassContents(String folderName)
			throws IOException {
		ArrayList<Integer> classContents = new ArrayList<Integer>();
		File file = new File(folderName);
		int numberOfPages = 0;
		for (File fileName : file.listFiles()) {
			numberOfPages = getNumberOfPages(fileName);
			classContents.add(numberOfPages);
		}
		return classContents;
	}

	public static int getNumberOfPages(File fileName) throws IOException {
		int numberOfPages = 0;
		FileReader fileReader = new FileReader(fileName);
		BufferedReader bufferedReader = new BufferedReader(fileReader);
		String line = "";
		while ((line = bufferedReader.readLine()) != null) {
			if (line.contains("URL -#-")) {
				numberOfPages++;
			}
		}
		bufferedReader.close();
		fileReader.close();
		return numberOfPages;
	}

	public static void splitFile(File fileName, int numOfTrainingPages,
			int numOfTestingPages) throws IOException {
		int numberOfPages = 0;
		String testingFileName = fileName.toString().replace("webpages",
				"testPages");
		String trainingFileName = fileName.toString().replace("webpages",
				"trainingPages");

		FileWriter fileWriter = new FileWriter(trainingFileName);
		BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

		FileReader fileReader = new FileReader(fileName);
		BufferedReader bufferedReader = new BufferedReader(fileReader);
		String line = "";
		while ((line = bufferedReader.readLine()) != null) {
			if (line.contains("URL -#-")) {
				numberOfPages++;
				if (numberOfPages == numOfTrainingPages) {
					bufferedWriter.close();
					fileWriter.close();
					fileWriter = new FileWriter(testingFileName);
					bufferedWriter = new BufferedWriter(fileWriter);
				}
			}

			bufferedWriter.write(line + "\n");
		}
		bufferedWriter.close();
		fileWriter.close();
		bufferedReader.close();
		fileReader.close();
	}

	public static int loadNumberOfClasses() throws IOException {
		FileReader fileReader = new FileReader("classthreshold.txt");
		BufferedReader bufferedReader = new BufferedReader(fileReader);
		int cnt = 0;
		while (bufferedReader.readLine() != null) {
			cnt++;
		}
		bufferedReader.close();
		fileReader.close();
		return cnt;
	}
	
	public static void constructIndexTable() throws IOException {
		ParseData parseData = new ParseData();
		File file = new File("trainingPages");
		for (File fileName : file.listFiles()) {
			parseData.parseFile(fileName);
			System.out.println("Done with " + fileName);
		}
		FileStorageUtilities.storePOJOToFile(parseData.getIndexTrie(),
				"indexTable20.Dat", 20);
		FileStorageUtilities.storePOJOToFile(parseData.getIndexTrie(),
				"indexTable50.Dat", 50);
	}

	public static void constructFeatureDistribution(String fileName) throws IOException {
		ArrayList<Integer> classContents = PreProcessing
				.getClassContents("trainingPages");
		int totalNumberOfDocs = 0;
		for (int i = 0; i < classContents.size(); i++) {
			totalNumberOfDocs += classContents.get(i);
		}
		Trie<String, PostingList> trie;
		trie = FileStorageUtilities.constuctPOJOFromFile(fileName);
		int numberOfClasses = loadNumberOfClasses();
		String keyString = "";
		PostingList postingList = null;
				
		FileWriter fileWriter = new FileWriter("featureDistribution.dat");
		BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

		FileWriter fileWriter2 = new FileWriter("chiStats.dat");
		BufferedWriter bufferedWriter2 = new BufferedWriter(fileWriter2);
		for (Entry<String, PostingList> entry : trie.entrySet()) {
			keyString = entry.getKey();
			postingList = entry.getValue();
			FeatureDistribution featureDistribution = new FeatureDistribution(
					keyString, numberOfClasses);
			featureDistribution.loadStats(postingList, numberOfClasses,
					classContents, totalNumberOfDocs);
			bufferedWriter.write(featureDistribution.getFeatureDistStringForm() + "\n");
			bufferedWriter2.write(featureDistribution.getChiSquareStringForm() + "\n");
		}
		bufferedWriter.close();
		fileWriter.close();

		bufferedWriter2.close();
		fileWriter2.close();
	}

	public static void getGini(double giniThreshold,String fileName) throws IOException{
		ArrayList<Integer> classContents = PreProcessing
				.getClassContents("trainingPages");
		int totalNumberOfDocs = 0;
		for (int i = 0; i < classContents.size(); i++) {
			totalNumberOfDocs += classContents.get(i);
		}
		Trie<String, PostingList> trie;
		trie = FileStorageUtilities.constuctPOJOFromFile(fileName);
		int numberOfClasses = loadNumberOfClasses();
		String keyString = "";
		PostingList postingList = null;

		FileWriter fileWriter = new FileWriter("giniCoefficient.dat");
		BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
		for (Entry<String, PostingList> entry : trie.entrySet()) {
			keyString = entry.getKey();
			postingList = entry.getValue();
			FeatureDistribution featureDistribution = new FeatureDistribution(
					keyString, numberOfClasses);
			featureDistribution.loadStats(postingList, numberOfClasses,
					classContents, totalNumberOfDocs);
			featureDistribution.calculateGiniCoefficient(numberOfClasses);
			if(featureDistribution.getGiniCoefficient()>giniThreshold){
				bufferedWriter.write(keyString + " - " + featureDistribution.getGiniCoefficient() + "\n");	
			}
		}
		bufferedWriter.close();
		fileWriter.close();
	}

	/* A broken function. Not used anyway.
	public static void calculateConditionalProbability(String fileName) throws IOException{
		ArrayList<Integer> classContents = PreProcessing
				.getClassContents("trainingPages");
		int totalNumberOfDocs = 0;
		for (int i = 0; i < classContents.size(); i++) {
			totalNumberOfDocs += classContents.get(i);
		}
		Trie<String, PostingList> trie;
		trie = FileStorageUtilities.constuctPOJOFromFile(fileName);
		int numberOfClasses = loadNumberOfClasses();
		String keyString = "";
		PostingList postingList = null;
				
		FileWriter fileWriter = new FileWriter("conditionalProbability.dat");
		BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
		for (Entry<String, PostingList> entry : trie.entrySet()) {
			keyString = entry.getKey();
			postingList = entry.getValue();
			FeatureDistribution featureDistribution = new FeatureDistribution(
					keyString, numberOfClasses);
			featureDistribution.loadStats(postingList, numberOfClasses,
					classContents, totalNumberOfDocs);
			Map<Integer, FeatureInfo> featureClassRelation = featureDistribution.getFeatureClassRelation();
			double probability = 0;
			probability = (1.0*(1+featureClassRelation.get(0).getA()))/(classContents.get(0) + totalNumberOfDocs);
			String fileOutput = keyString + " (" + probability;
			for(int i=1;i<numberOfClasses;i++){
				probability = (1.0*(1+featureClassRelation.get(i).getA()))/(classContents.get(i) + totalNumberOfDocs);
				fileOutput += "," + probability;
			}
			fileOutput += ")";
			bufferedWriter.write(fileOutput + "\n");
		}
		bufferedWriter.close();
		fileWriter.close();
	}
	*/
	
}
