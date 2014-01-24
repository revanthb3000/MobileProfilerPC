package org.iitg.miningBTP.dal;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.ardverk.collection.PatriciaTrie;
import org.ardverk.collection.StringKeyAnalyzer;
import org.ardverk.collection.Trie;
import org.iitg.miningBTP.core.FeatureDistribution;

import com.google.gson.Gson;

public class FileStorageUtilities {

	public static void storePOJOToFile(Trie<String, PostingList> indexTrie,
			String fileName, int supportFactor) throws IOException {
		FileWriter fileWriter = null;
		BufferedWriter bufferedWriter = null;
		Gson gson = new Gson();
		File file = new File(fileName);
		fileWriter = new FileWriter(file);
		bufferedWriter = new BufferedWriter(fileWriter);

		int wordcount = -1;

		for (Entry<String, PostingList> entry : indexTrie.entrySet()) {
			/* This if condition skips the first node - the root node */
			if (wordcount == -1) {
				wordcount++;
				continue;
			}
			if (entry.getValue().getNumberOfDocuments() >= supportFactor) {
				bufferedWriter.write(entry.getKey() + "\n");
				bufferedWriter.write(gson.toJson(entry.getValue()) + "\n");
			}
		}
		bufferedWriter.close();
		fileWriter.close();
	}

	/**
	 * This function takes a list of files and then constructs the required
	 * Trie.
	 * 
	 * @param filesList
	 * @return
	 * @throws IOException
	 */
	public static Trie<String, PostingList> constuctPOJOFromFile(String fileName)
			throws IOException {

		Trie<String, PostingList> trie = new PatriciaTrie<String, PostingList>(
				StringKeyAnalyzer.BYTE);
		File file = new File(fileName);
		FileReader fileReader = new FileReader(file);
		BufferedReader bufferedReader = new BufferedReader(fileReader);
		String line = "";
		String key = "";
		Gson gson = new Gson();

		while ((line = bufferedReader.readLine()) != null) {
			if (line.trim().equals("")) {
				break;
			}

			key = line;
			line = bufferedReader.readLine();
			PostingList postingList = new PostingList();
			postingList = gson.fromJson(line, PostingList.class);
			trie.put(key, postingList);
		}
		return trie;
	}

	public static List<Integer> getClassContentInfo(String fileName) throws IOException{
		FileReader fileReader = new FileReader(fileName);
		BufferedReader bufferedReader = new BufferedReader(fileReader);
		String line = "";
		List<Integer> classContents = new ArrayList<Integer>();;
		while((line=bufferedReader.readLine())!=null){
			classContents.add(Integer.parseInt(line.split("-")[1].trim()));
		}
		bufferedReader.close();
		fileReader.close();
		return classContents;
	}
	
	public static List<String> getClassMappingInfo(String fileName) throws IOException{
		FileReader fileReader = new FileReader(fileName);
		BufferedReader bufferedReader = new BufferedReader(fileReader);
		String line = "";
		List<String> classContents = new ArrayList<String>();;
		while((line=bufferedReader.readLine())!=null){
			classContents.add((line.split("-")[0].trim()));
		}
		bufferedReader.close();
		fileReader.close();
		return classContents;
	}
	
	public static ArrayList<FeatureDistribution> getFeatureDistribution(String fileName) throws IOException{
		int numberOfClasses = getClassMappingInfo("classthreshold.txt").size();
		int a=0,b=0,c=0,d=0,classId=0;
		FileReader fileReader = new FileReader(fileName);
		BufferedReader bufferedReader = new BufferedReader(fileReader);
		String line = "",keyWord = "",temp = "";
		ArrayList<FeatureDistribution> featureDistributions = new ArrayList<FeatureDistribution>();
		while((line=bufferedReader.readLine())!=null){
			keyWord = line.split("-")[0].trim();
			FeatureDistribution featureDistribution = new FeatureDistribution(keyWord,numberOfClasses);
			temp = line.split("-")[1].trim();
			for(String classDist : temp.split("\\s")){
				classDist = classDist.replace("{", "").replace("[", "").replace("]","").replace("}", "").replace(":", ",");
				classId = Integer.parseInt(classDist.split(",")[0]);
				a = Integer.parseInt(classDist.split(",")[1]);
				b = Integer.parseInt(classDist.split(",")[2]);
				c = Integer.parseInt(classDist.split(",")[3]);
				d = Integer.parseInt(classDist.split(",")[4]);
				featureDistribution.featureClassRelation.get(classId).setA(a);
				featureDistribution.featureClassRelation.get(classId).setB(b);
				featureDistribution.featureClassRelation.get(classId).setC(c);
				featureDistribution.featureClassRelation.get(classId).setD(d);
			}
			featureDistributions.add(featureDistribution);
		}
		bufferedReader.close();
		fileReader.close();
		return featureDistributions;
	}
	
	
	public static ArrayList<String> getFeatures(String fileName)
			throws IOException {
		FileReader fileReader = new FileReader(fileName);
		BufferedReader bufferedReader = new BufferedReader(fileReader);
		String line = "";
		ArrayList<String> features = new ArrayList<String>();
		while((line=bufferedReader.readLine())!=null){
			features.add(line.split("-")[0].trim());
		}
		bufferedReader.close();
		fileReader.close();
		return features;
	}
	
}
