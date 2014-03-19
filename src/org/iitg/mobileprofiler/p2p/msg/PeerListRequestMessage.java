package org.iitg.mobileprofiler.p2p.msg;

import it.unipr.ce.dsg.s2p.message.BasicMessage;
import it.unipr.ce.dsg.s2p.message.Payload;
import it.unipr.ce.dsg.s2p.peer.PeerDescriptor;

public class PeerListRequestMessage extends BasicMessage{
	
	public static final String MSG_PEER_LIST_REQUEST="peer_list_request";

	public PeerListRequestMessage(PeerDescriptor peerDesc) {
		super(MSG_PEER_LIST_REQUEST, new Payload(peerDesc));
	}
	
}
