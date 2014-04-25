package org.iitg.mobileprofiler.p2p.msg;

import it.unipr.ce.dsg.s2p.message.BasicMessage;
import it.unipr.ce.dsg.s2p.message.Payload;
import it.unipr.ce.dsg.s2p.peer.PeerDescriptor;

/**
 * See if we need to add a class type element.
 * This is used by nodes that answer questions. They send this type of message 
 * to the bootstrap and the bootstrap stores this info in the repo
 * @author RB
 *
 */
public class RepoStorageMessage extends BasicMessage {
	
	public static String MSG_REPO_STORAGE = "repo_storage_message";
	
	private String question;
	
	private String userId;
	
	private Integer answer;
	
	private String className;

	public RepoStorageMessage(PeerDescriptor peerDesc, String question, String className, String userId, Integer answer) {
		super(MSG_REPO_STORAGE, new Payload(peerDesc));
		this.question = question;
		this.userId = userId;
		this.answer = answer;
		this.className = className;
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

	public String getClassName() {
		return className;
	}
	
}
