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
public class TextMessage extends BasicMessage {
	
	public static final String MSG_TEXT = "text_message";
	
	private String textMessage;
	
	public TextMessage(PeerDescriptor peerDesc, String message) {
		super(MSG_TEXT, new Payload(peerDesc));
		this.textMessage = message;
	}

	public String getTextMessage() {
		return textMessage;
	}
	
}

