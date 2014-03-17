package org.iitg.mobileprofiler.p2p.msg;

import it.unipr.ce.dsg.s2p.message.BasicMessage;
import it.unipr.ce.dsg.s2p.message.Payload;
import it.unipr.ce.dsg.s2p.peer.PeerDescriptor;


/**
 * Class <code>PingMessage</code> implements a simple message sent by the peer to other peer.
 * The payload of PingMessage contains the peer descriptor.
 * 
 * @author Fabrizio Caramia
 *
 */
public class PingMessage extends BasicMessage {
	
	public static final String MSG_PEER_PING="peer_ping"; 
	
	public PingMessage(PeerDescriptor peerDesc) {
		super(MSG_PEER_PING, new Payload(peerDesc));
	}
}

