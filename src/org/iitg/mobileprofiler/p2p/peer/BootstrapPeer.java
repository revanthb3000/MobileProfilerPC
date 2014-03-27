package org.iitg.mobileprofiler.p2p.peer;

import it.unipr.ce.dsg.s2p.message.parser.JSONParser;
import it.unipr.ce.dsg.s2p.org.json.JSONException;
import it.unipr.ce.dsg.s2p.org.json.JSONObject;
import it.unipr.ce.dsg.s2p.peer.NeighborPeerDescriptor;
import it.unipr.ce.dsg.s2p.peer.Peer;
import it.unipr.ce.dsg.s2p.peer.PeerDescriptor;
import it.unipr.ce.dsg.s2p.sip.Address;

import org.iitg.mobileprofiler.p2p.msg.JoinMessage;
import org.iitg.mobileprofiler.p2p.msg.PeerListMessage;
import org.iitg.mobileprofiler.p2p.msg.PeerListRequestMessage;

/**
 * This is the BootStrap peer that is your one stop shop for nodes info.
 * @author RB
 *
 */
public class BootstrapPeer extends Peer {

	public BootstrapPeer(String key, String peerName, int peerPort) {
		super(null, key, peerName, peerPort);
	}

	/**
	 * Our bootstrap will only receive two types of messages. The join message and the 'give me peer list' message.
	 */
	@Override
	protected void onReceivedJSONMsg(JSONObject peerMsg, Address sender) {
		System.out.println(peerMsg);
		try {
			
			//Useful for logging
			String typeMsg = peerMsg.get("type").toString();
			int lengthMsg = peerMsg.toString().length();

			JSONObject info = new JSONObject();
			info.put("timestamp", System.currentTimeMillis());
			info.put("type", "recv");
			info.put("typeMessage", typeMsg);
			info.put("byte", lengthMsg);
			info.put("sender", sender.getURL());

			if(peerMsg.get("type").equals(JoinMessage.MSG_PEER_JOIN)){
				JSONObject params = peerMsg.getJSONObject("payload").getJSONObject("params");
				PeerDescriptor neighborPD = new PeerDescriptor(params.get("name").toString(), params.get("address").toString(), params.get("key").toString(), params.get("contactAddress").toString());
				NeighborPeerDescriptor neighborPeer = addNeighborPeer(neighborPD);
	
				//check the numPeerList field
				int numPeer = (Integer) peerMsg.get("numPeerList");
				if(numPeer>=0){
					PeerListMessage newPLMsg = null;
					if(numPeer==0 || (this.peerList.size()<=numPeer)){
						//create message and add the peer list 
						newPLMsg = new PeerListMessage(this.peerList);	
					}
					else{
						newPLMsg = new PeerListMessage(this.peerList.getRandomPeers(numPeer+1));	
					}
	
					//remove the current peer from payload
					if(newPLMsg.getPayload().containsKey(params.get("key").toString()))
						newPLMsg.getPayload().removeParam(params.get("key").toString());
	
					//send peer list to peer
					if(newPLMsg!=null){
						send(neighborPeer, newPLMsg);
					}
					
				}
			}
			else if(peerMsg.get("type").equals(PeerListRequestMessage.MSG_PEER_LIST_REQUEST)){
				JSONObject params = peerMsg.getJSONObject("payload").getJSONObject("params");
//				System.out.println("Got a peer list request message from " + params.get("name").toString());
				PeerListMessage newPLMsg = new PeerListMessage(this.peerList);
				if(newPLMsg.getPayload().containsKey(params.get("key").toString()))
					newPLMsg.getPayload().removeParam(params.get("key").toString());
				send(new Address(params.get("address").toString()), newPLMsg);
			}
			
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected void onDeliveryMsgFailure(String peerMsg, Address receiver, String contentType) {
		String typeMessage = null;
		JSONObject jsonMsg = null;
		long rtt = 0;
		
		if(contentType.equals(JSONParser.MSG_JSON)){
			try {
				jsonMsg = new JSONObject(peerMsg);
				typeMessage= (String) jsonMsg.get("type");

				long sendedTime = (Long) jsonMsg.get("timestamp");
				long receivedTime = System.currentTimeMillis();
				rtt = receivedTime - sendedTime;
			} catch (JSONException e) {
				e.printStackTrace();
			}

			//Useful for logging
			try {
				JSONObject info = new JSONObject();
				info.put("timestamp", System.currentTimeMillis());
				info.put("type", "sent");
				info.put("typeMessage", typeMessage);
				info.put("transaction", "failed");
				info.put("receiver", receiver.getURL());
				info.put("RTT", rtt);
				info.put("byte", peerMsg.length());
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
		}
	}

	@Override
	protected void onDeliveryMsgSuccess(String peerMsg, Address receiver, String contentType) {
		String typeMessage = null;
		JSONObject jsonMsg = null;
		long rtt = 0;
		
		if(contentType.equals(JSONParser.MSG_JSON)){

			try {
				jsonMsg = new JSONObject(peerMsg);
				typeMessage= (String) jsonMsg.get("type");

				long sendedTime = (Long) jsonMsg.get("timestamp");
				long receivedTime = System.currentTimeMillis();
				rtt = receivedTime - sendedTime;

			} catch (JSONException e) {
				e.printStackTrace();
			}


			//Useful for logging
			try {
				JSONObject info = new JSONObject();
				info.put("timestamp", System.currentTimeMillis());
				info.put("type", "sent");
				info.put("typeMessage", typeMessage);
				info.put("transaction", "successful");
				info.put("receiver", receiver.getURL());
				info.put("RTT", rtt);
				info.put("byte", peerMsg.length());
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
		}
	}

}
