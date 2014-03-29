package org.iitg.mobileprofiler.p2p.msg;

import it.unipr.ce.dsg.s2p.message.BasicMessage;
import it.unipr.ce.dsg.s2p.message.Payload;
import it.unipr.ce.dsg.s2p.peer.PeerDescriptor;

/**
 * This is the reply to the user's question. A simple rating and similarity
 * score.
 * 
 * @author RB
 * 
 */
public class QueryReplyMessage extends BasicMessage {

	public static final String MSG_QUERY_REPLY = "query_reply__message";
	
	private String question;
	
	private int askerQuestionId;
	
	private Double similarity;
	
	private Integer answer;

	public QueryReplyMessage(PeerDescriptor peerDesc, String questionToAnswer, Double similarityScore, Integer rating, int questionId) {
		super(MSG_QUERY_REPLY, new Payload(peerDesc));
		question = questionToAnswer;
		similarity = similarityScore;
		answer = rating;
		askerQuestionId = questionId;
	}

	public String getQuestion() {
		return question;
	}

	public Double getSimilarity() {
		return similarity;
	}

	public Integer getAnswer() {
		return answer;
	}

	public int getAskerQuestionId() {
		return askerQuestionId;
	}
	

}