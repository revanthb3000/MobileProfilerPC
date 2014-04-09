package org.iitg.mobileprofiler.p2p.msg;

import it.unipr.ce.dsg.s2p.message.BasicMessage;
import it.unipr.ce.dsg.s2p.message.Payload;
import it.unipr.ce.dsg.s2p.peer.PeerDescriptor;

/**
 * See if we need to add a class type element.
 * @author RB
 *
 */
public class RepoStorageMessage extends BasicMessage {
	
	public static String MSG_REPO_STORAGE = "repo_storage_message";
	
	private String question;
	
	private String userId;
	
	private Integer answer;

	public RepoStorageMessage(PeerDescriptor peerDesc, String question, String userId, Integer answer) {
		super(MSG_REPO_STORAGE, new Payload(peerDesc));
		this.question = question;
		this.userId = userId;
		this.answer = answer;
	}

	public String getQuestion() {
		return question;
	}

	public String getUserId() {
		return userId;
	}

	public Integer getAnswer() {
		return answer;
	}
	
}
