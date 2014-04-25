package org.iitg.mobileprofiler.p2p.msg;

import java.util.ArrayList;

import it.unipr.ce.dsg.s2p.message.BasicMessage;
import it.unipr.ce.dsg.s2p.message.Payload;
import it.unipr.ce.dsg.s2p.peer.PeerDescriptor;

/**
 * This is the text message class that we use to send and receive data.
 * @author RB
 *
 */
public class UserQueryMessage extends BasicMessage {
	
	public static final String MSG_USER_QUERY = "user_query_message";
	
	private String fromAddress;
	
	private String textMessage;
	
	private String className;
	
	private int askerQuestionId;
	
	private ArrayList<Integer> classDistribution;
	
	public UserQueryMessage(PeerDescriptor peerDesc, String message,String classType, ArrayList<Integer> userClassContents, int questionId, String address) {
		super(MSG_USER_QUERY, new Payload(peerDesc));
		this.textMessage = message;
		this.classDistribution = userClassContents;
		this.askerQuestionId = questionId;
		this.fromAddress = address;
		this.className = classType;
	}

	public String getFromAddress() {
		return fromAddress;
	}

	public String getTextMessage() {
		return textMessage;
	}

	public String getClassName() {
		return className;
	}

	public ArrayList<Integer> getClassDistribution() {
		return classDistribution;
	}

	public int getAskerQuestionId() {
		return askerQuestionId;
	}
	
}

