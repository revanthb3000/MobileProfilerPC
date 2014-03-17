package org.iitg.mobileprofiler.p2p.peer;

/*
 * Copyright (C) 2010 University of Parma - Italy
 * 
 * This source code is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This source code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this source code; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 * Designer(s):
 * Marco Picone (picone@ce.unipr.it)
 * Fabrizio Caramia (fabrizio.caramia@studenti.unipr.it)
 * Michele Amoretti (michele.amoretti@unipr.it)
 * 
 * Developer(s)
 * Fabrizio Caramia (fabrizio.caramia@studenti.unipr.it)
 * 
 */


import org.iitg.mobileprofiler.p2p.msg.JoinMessage;

import it.unipr.ce.dsg.s2p.org.json.JSONObject;
import it.unipr.ce.dsg.s2p.peer.Peer;
import it.unipr.ce.dsg.s2p.peer.PeerDescriptor;
import it.unipr.ce.dsg.s2p.sip.Address;


/**
 * 
 * A very simple peer.
 * 
 * @author Fabrizio Caramia
 *
 */
public class SimplePeer extends Peer {

	public SimplePeer(String pathConfig) {
		super(pathConfig, "a5ds465a465a45d4s64d6a");

	}

	public PeerDescriptor getPeerDescriptor(){
		return peerDescriptor;
	}
	
	public void joinToPeer(String toAddress, String contactAddress){

		JoinMessage peerMsg = new JoinMessage(peerDescriptor);
		
		send(new Address(toAddress), new Address(contactAddress), peerMsg);

	}
	
	
	@Override
	protected void onReceivedJSONMsg(JSONObject jsonMsg, Address sender) {
		// TODO Auto-generated method stub
		super.onReceivedJSONMsg(jsonMsg, sender);
	}
	
	@Override
	protected void onDeliveryMsgFailure(String peerMsgSended, Address receiver,
			String contentType) {
		System.out.println("onDeliveryMsgFailure: " + peerMsgSended);
		
	}

	@Override
	protected void onDeliveryMsgSuccess(String peerMsgSended, Address receiver,
			String contentType) {
		System.out.println("onDeliveryMsgSuccess: " + peerMsgSended);
	}
	
	@Override
	protected void onReceivedMsg(String peerMsg, Address sender,
			String contentType) {
		// TODO Auto-generated method stub
		super.onReceivedMsg(peerMsg, sender, contentType);
		System.out.println("onReceivedMsg: " + peerMsg);
	}
}
