package org.iitg.mobileprofiler.p2p.tools;

import org.iitg.mobileprofiler.p2p.peer.UserNodePeer;

public class PendingQuestion {

	private String question;

	private Double similarity;
	
	private int answer;
	
	private int questionId;
	
	private String destinationIpAddress;
	
	private UserNodePeer userNodePeer;

	public PendingQuestion(String question, Double similarity, int questionId, String destinationIpAddress, UserNodePeer userNodePeer) {
		this.question = question;
		this.similarity = similarity;
		this.questionId = questionId;
		this.destinationIpAddress = destinationIpAddress;
		this.userNodePeer = userNodePeer;
	}
	
	public void sendReply(){
		userNodePeer.sendReply(question, similarity, answer, questionId, destinationIpAddress);
	}

	public String getQuestion() {
		return question;
	}

	public void setQuestion(String question) {
		this.question = question;
	}

	public Double getSimilarity() {
		return similarity;
	}

	public void setSimilarity(Double similarity) {
		this.similarity = similarity;
	}

	public int getAnswer() {
		return answer;
	}

	public void setAnswer(int answer) {
		this.answer = answer;
	}

	public int getQuestionId() {
		return questionId;
	}

	public void setQuestionId(int questionId) {
		this.questionId = questionId;
	}

	public String getDestinationIpAddress() {
		return destinationIpAddress;
	}

	public void setDestinationIpAddress(String destinationIpAddress) {
		this.destinationIpAddress = destinationIpAddress;
	}

	public UserNodePeer getUserNodePeer() {
		return userNodePeer;
	}

	public void setUserNodePeer(UserNodePeer userNodePeer) {
		this.userNodePeer = userNodePeer;
	}
	
}
