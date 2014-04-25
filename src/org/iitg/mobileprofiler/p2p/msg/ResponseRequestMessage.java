package org.iitg.mobileprofiler.p2p.msg;

import org.iitg.mobileprofiler.db.ResponseDao;

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
	
	private ResponseDao responseDao;

	public ResponseRequestMessage(PeerDescriptor peerDesc, ResponseDao responseDao) {
		super(MSG_RESPONSE_REQUEST, new Payload(peerDesc));
		this.responseDao = responseDao;
	}

	public ResponseDao getResponseDao() {
		return responseDao;
	}

	public void setResponseDao(ResponseDao responseDao) {
		this.responseDao = responseDao;
	}

}
