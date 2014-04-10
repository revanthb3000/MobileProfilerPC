package org.iitg.mobileprofiler.p2p.msg;

import java.util.ArrayList;

import org.iitg.mobileprofiler.db.ResponseDao;

import it.unipr.ce.dsg.s2p.message.BasicMessage;
import it.unipr.ce.dsg.s2p.message.Payload;
import it.unipr.ce.dsg.s2p.peer.PeerDescriptor;

/**
 * This is the message the bootstrap sends the peer that requests the repo update.
 * Only the requried response data is sent.
 * @author RB
 *
 */
public class ResponseDataMessage extends BasicMessage {
	
	public static String MSG_RESPONSE_DATA = "response_data_message";
	
	private ArrayList<ResponseDao> responses;

	public ResponseDataMessage(PeerDescriptor peerDesc, ArrayList<ResponseDao> responses) {
		super(MSG_RESPONSE_DATA, new Payload(peerDesc));
		this.responses = responses;
	}

	public ArrayList<ResponseDao> getResponses() {
		return responses;
	}

	public void setResponses(ArrayList<ResponseDao> responses) {
		this.responses = responses;
	}

}
