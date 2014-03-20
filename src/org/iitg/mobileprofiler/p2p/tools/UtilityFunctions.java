package org.iitg.mobileprofiler.p2p.tools;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public class UtilityFunctions {
	
	/**
	 * Utility function that generates the hexdigest for a given input string.
	 * @param inputString
	 * @return
	 */
	public static String getHexDigest(String inputString){
		MessageDigest md = null;;
		try {
			md = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return "";
		}
		md.update(inputString.getBytes());
		byte[] digest = md.digest();
		StringBuffer sb = new StringBuffer();
		for (byte b : digest) {
			sb.append(Integer.toHexString((int) (b & 0xff)));
		}
		return sb.toString();
	}
	
	/**
	 * Generates a random class distribution. This is used for testing
	 * @return
	 */
	public static ArrayList<Integer> getRandomClassDistribution(){
		ArrayList<Integer> classDistribution = new ArrayList<Integer>();
		for(int i=1;i<=229;i++){
			//Get random number between 0 and 50
			Integer randomInt = 50 + (int)(Math.random() * ((50 - 0) + 1));
			classDistribution.add(randomInt);
		}
		return classDistribution;
	}

	/**
	 * Given a string, the arraylist of class contents is obtained.
	 * @param inputString
	 * @return
	 */
	public static ArrayList<Integer> getClassDistributionFromString(String inputString){
		ArrayList<Integer> classDistribution = new ArrayList<Integer>();
		inputString = inputString.replace("[", "").replace("]", "");
		for(String content : inputString.split(",")){
			classDistribution.add(Integer.parseInt(content));
		}
		return classDistribution;
	}
	
	/**
	 * Get the cosine similarity between two vectors.
	 * @param contents1
	 * @param contents2
	 * @return
	 */
	public static Double getSimilarityScore(ArrayList<Integer> contents1, ArrayList<Integer> contents2){
		Double similarity = 0.0;
		for(int i=0;i<contents1.size();i++){
			similarity += (contents1.get(i)*contents2.get(i));
		}
		similarity = similarity/getVectorMagnitude(contents1);
		similarity = similarity/getVectorMagnitude(contents2);
		return similarity;
	}
	
	/**
	 * Get the magnitude of a vector.
	 * @param classVector
	 * @return
	 */
	public static Double getVectorMagnitude(ArrayList<Integer> classVector){
		Double magnitude = 0.0;
		for(Integer classContent : classVector){
			magnitude += (classContent*classContent);
		}
		magnitude = Math.sqrt(magnitude);
		return magnitude;
	}
	
}
