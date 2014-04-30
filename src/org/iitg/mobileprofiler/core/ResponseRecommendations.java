package org.iitg.mobileprofiler.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.iitg.mobileprofiler.db.DatabaseConnector;
import org.iitg.mobileprofiler.db.ResponseDao;


/**
 * This class contains the methods that are used to compute recommendations: Entropy, voting and participation.
 * @author RB
 *
 */
public class ResponseRecommendations {

	private String mobileOwnerId;
	
	private ArrayList<ResponseDao> responseDaos = null;
	
	private ArrayList<String> questions = null;
	
	private ArrayList<String> peerUserIds = null;
	
	private int numberOfClasses;
	
	public ResponseRecommendations(String username){
		this.mobileOwnerId = username;
		
		DatabaseConnector databaseConnector = new DatabaseConnector();

		int maxResponseId = databaseConnector.getMaxResponseId("");
		responseDaos = databaseConnector.getResponses(0, maxResponseId);

		this.numberOfClasses = databaseConnector.getNumberOfResponseClasses();
		
		databaseConnector.closeDBConnection();
		
		questions = new ArrayList<String>();
		peerUserIds = new ArrayList<String>();
		
		extractQuestionsAndUsers();		
	}
	
	public void extractQuestionsAndUsers(){
		for(ResponseDao responseDao : responseDaos){
			String question = responseDao.getQuestion();
			String user = responseDao.getUserId();
			if(!questions.contains(question)){
				questions.add(question);
			}
			if(!peerUserIds.contains(user) && !user.equals(mobileOwnerId)){
				peerUserIds.add(user);
			}
		}
	}
	
	/**
	 * General function that when given similarities would calculate the recommended scores.
	 * @return 
	 */
	public Map<String, Double> getRecommendations(Map<String, Double> similarities){
		Map<String, Double> recommendedScore = new HashMap<String, Double>();
		Map<String, Double> questionResponseCount = new HashMap<String, Double>();
		for(String question : questions){
			recommendedScore.put(question, 0.0);
			questionResponseCount.put(question, 0.0);
		}
		for(ResponseDao responseDao : responseDaos){
			if(responseDao.getUserId().equals(mobileOwnerId)){
				continue;
			}
			String question = responseDao.getQuestion();
			int answer = responseDao.getAnswer();
			String user = responseDao.getUserId();
			recommendedScore.put(question, recommendedScore.get(question) + (similarities.get(user)*answer));
			questionResponseCount.put(question, questionResponseCount.get(question)+similarities.get(user));
		}
		for(String question : recommendedScore.keySet()){
			Double recommendation = recommendedScore.get(question)/questionResponseCount.get(question);
			System.out.println(question.trim() + " - " + recommendation);
			recommendedScore.put(question, recommendation);
		}
		return recommendedScore;
	}
	
	/**
	 * Basic recommendation scheme.
	 * Just take the average.
	 * @return 
	 */
	public Map<String, Double> getAverageRecommendation(){
		Map<String, Double> similarities = new HashMap<String, Double>();
		ArrayList<Double> userVector = new ArrayList<Double>();
		for(int i=0;i<numberOfClasses;i++){
			userVector.add(1.0);
		}
		for(String user : peerUserIds){
			similarities.put(user, getSimilarityScore(userVector, userVector));
		}
		return getRecommendations(similarities);
	}
	
	/**
	 * Recommendation using participation history.
	 * Just take the average.
	 * @return 
	 */
	public Map<String, Double> getParticipationHistoryRecommendation(){
		Map<String, Double> similarities = new HashMap<String, Double>();
		ArrayList<Double> userVector = getClassAverageUserVector(mobileOwnerId);
		for(String user : peerUserIds){
			similarities.put(user, getSimilarityScore(userVector, getClassAverageUserVector(user)));
		}
		return getRecommendations(similarities);
	}
	
	/**
	 * Recommendation using Entropy of participation history.
	 * Just take the average.
	 * @return 
	 */
	public Map<String, Double> getEntropyRecommendation(){
		Map<String, Double> similarities = new HashMap<String, Double>();
		ArrayList<Double> userVector = getEntropyUserVector(mobileOwnerId);
		for(String user : peerUserIds){
			similarities.put(user, getSimilarityScore(userVector, getClassAverageUserVector(user)));
		}
		return getRecommendations(similarities);
	}
	
	/**
	 * Given a userId, this guys calulates the entropy distribution of it.
	 * @param userId
	 * @return
	 */
	public ArrayList<Double> getEntropyUserVector(String userId){
		ArrayList<Double> userVector = new ArrayList<Double>();
		ArrayList<Double> classCount = new ArrayList<Double>();
		for(int i=0;i<numberOfClasses;i++){
			userVector.add(0.0);
			classCount.add(0.0);
		}
		for(ResponseDao responseDao : responseDaos){
			int classId = responseDao.getClassId() - 1;
			int answer = responseDao.getAnswer();
			if(responseDao.getUserId().equals(userId)){
				classCount.set(classId, classCount.get(classId) + answer);
			}
		}
		for(ResponseDao responseDao : responseDaos){
			int classId = responseDao.getClassId() - 1;
			int answer = responseDao.getAnswer();
			if(responseDao.getUserId().equals(userId)){
				Double probability = answer/classCount.get(classId);
				Double temp = -probability*Math.log(probability);
				userVector.set(classId, userVector.get(classId) + temp);
			}
		}
		return userVector;
	}
	
	/**
	 * This function gets the class Average User vector.
	 * Basically it gets the average score given by a user for each class.
	 * @param userId
	 * @return
	 */
	public ArrayList<Double> getClassAverageUserVector(String userId){
		ArrayList<Double> userVector = new ArrayList<Double>();
		ArrayList<Integer> occurenceCount = new ArrayList<Integer>();
		for(int i=0;i<numberOfClasses;i++){
			userVector.add(0.0);
			occurenceCount.add(0);
		}
		for(ResponseDao responseDao : responseDaos){
			int classId = responseDao.getClassId() - 1;
			int answer = responseDao.getAnswer();
			if(responseDao.getUserId().equals(userId)){
				occurenceCount.set(classId, occurenceCount.get(classId) + 1);
				userVector.set(classId, userVector.get(classId) + answer);
			}
		}
		for(int i=0; i<numberOfClasses; i++){
			if(occurenceCount.get(i)!=0){
				userVector.set(i, userVector.get(i)/occurenceCount.get(i));	
			}
		}
		return userVector;
	}
	
	/**
	 * Get the cosine similarity between two vectors.
	 * @param contents1
	 * @param contents2
	 * @return
	 */
	public static Double getSimilarityScore(ArrayList<Double> contents1, ArrayList<Double> contents2){
		Double similarity = 0.0;
		for(int i=0;i<contents1.size();i++){
			similarity += (contents1.get(i)*contents2.get(i));
		}
		if(similarity == 0){
			return similarity;
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
	public static Double getVectorMagnitude(ArrayList<Double> classVector){
		Double magnitude = 0.0;
		for(Double classContent : classVector){
			magnitude += (classContent*classContent);
		}
		magnitude = Math.sqrt(magnitude);
		return magnitude;
	}
	
}
