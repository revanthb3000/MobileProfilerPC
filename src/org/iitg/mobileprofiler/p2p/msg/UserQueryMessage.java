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
	
	private String textMessage;
	
	private ArrayList<Integer> classDistribution;
	
	public UserQueryMessage(PeerDescriptor peerDesc, String message, ArrayList<Integer> userClassContents) {
		super(MSG_USER_QUERY, new Payload(peerDesc));
		this.textMessage = message;
		this.classDistribution = userClassContents;
	}

	public String getTextMessage() {
		return textMessage;
	}

	public ArrayList<Integer> getClassDistribution() {
		return classDistribution;
	}
	
}

