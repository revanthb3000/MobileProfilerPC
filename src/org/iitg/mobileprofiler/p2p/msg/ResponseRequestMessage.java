package org.iitg.mobileprofiler.p2p.msg;

import it.unipr.ce.dsg.s2p.message.BasicMessage;
import it.unipr.ce.dsg.s2p.message.Payload;
import it.unipr.ce.dsg.s2p.peer.PeerDescriptor;

/**
 * This message is sent by the peer that wants to sync with the central repo.
 * Sends in the maxResponseId so that we get a clue of what responses that need to be sent.
 * @author RB
 *
 */
public class ResponseRequestMessage extends BasicMessage {
	
	public static String MSG_RESPONSE_REQUEST = "repo_update_message";
	
	private Integer maxResponseId;

	public ResponseRequestMessage(PeerDescriptor peerDesc, Integer maxResponseId) {
		super(MSG_RESPONSE_REQUEST, new Payload(peerDesc));
		this.maxResponseId = maxResponseId;
	}

	public Integer getMaxResponseId() {
		return maxResponseId;
	}

	public void setMaxResponseId(Integer maxResponseId) {
		this.maxResponseId = maxResponseId;
	}

}
