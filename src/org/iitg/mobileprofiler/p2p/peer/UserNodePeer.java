package org.iitg.mobileprofiler.p2p.peer;

import it.unipr.ce.dsg.s2p.message.parser.JSONParser;
import it.unipr.ce.dsg.s2p.org.json.JSONException;
import it.unipr.ce.dsg.s2p.org.json.JSONObject;
import it.unipr.ce.dsg.s2p.peer.NeighborPeerDescriptor;
import it.unipr.ce.dsg.s2p.peer.Peer;
import it.unipr.ce.dsg.s2p.peer.PeerDescriptor;
import it.unipr.ce.dsg.s2p.peer.PeerListManager;
import it.unipr.ce.dsg.s2p.sip.Address;

import java.util.ArrayList;
import java.util.Iterator;

import org.iitg.mobileprofiler.p2p.msg.JoinMessage;
import org.iitg.mobileprofiler.p2p.msg.PeerListMessage;
import org.iitg.mobileprofiler.p2p.msg.PeerListRequestMessage;
import org.iitg.mobileprofiler.p2p.msg.PingMessage;
import org.iitg.mobileprofiler.p2p.msg.QueryReplyMessage;
import org.iitg.mobileprofiler.p2p.msg.UserQueryMessage;
import org.iitg.mobileprofiler.p2p.tools.UtilityFunctions;

/**
 * The UserNodePeer node that we'll use for user nodes.
 * @author RB
 *
 */
public class UserNodePeer extends Peer {
	
	/**
	 * Class contents of the given user
	 */
	private ArrayList<Integer> classContents;
	
	/**
	 * Bootsta
	 */
	private String bootstrapAddress;
	
	/**
	 * This is the number of peers to which you wish to connect to (max)
	 * 0 - infinite.
	 */
	private int numberOfPeers;
	
	public UserNodePeer(String key, String peerName, int peerPort, ArrayList<Integer> userClassContents, String bootstrapInfo, int numOfPeers){
		super(null, key, peerName, peerPort);
		classContents = userClassContents;
		bootstrapAddress = bootstrapInfo;
		numberOfPeers = numOfPeers;
	}
	
	public PeerListManager getPeerList(){
		return peerList;
	}
	
	public ArrayList<Integer> getClassContents() {
		return classContents;
	}

	@Override
	protected void onReceivedJSONMsg(JSONObject peerMsg, Address sender) {
		try {
			JSONObject params = peerMsg.getJSONObject("payload").getJSONObject("params");
			
			//Useful for logging
			if(nodeConfig.log_path!=null){
				String typeMsg = peerMsg.get("type").toString();
				int lengthMsg = peerMsg.toString().length();
				JSONObject info = new JSONObject();
				info.put("timestamp", System.currentTimeMillis());
				info.put("type", "recv");
				info.put("typeMessage", typeMsg);
				info.put("byte", lengthMsg);
				info.put("sender", sender.getURL());
			}

			//add peer descriptor to list
			if(peerMsg.get("type").equals(PingMessage.MSG_PEER_PING)){
				PeerDescriptor neighborPeerDesc = new PeerDescriptor(params.get("name").toString(), params.get("address").toString(), params.get("key").toString(), params.get("contactAddress").toString());
				addNeighborPeer(neighborPeerDesc);
			}
			if(peerMsg.get("type").equals(PeerListMessage.MSG_PEER_LIST)){
				@SuppressWarnings("unchecked")
				Iterator<String> iter = params.keys();

				while(iter.hasNext()){
					String key = (String) iter.next();
					JSONObject keyPeer = params.getJSONObject(key);
					PeerDescriptor neighborPeerDesc = new PeerDescriptor(keyPeer.get("name").toString(), keyPeer.get("address").toString(), keyPeer.get("key").toString());
					if(keyPeer.get("contactAddress").toString()!="null")
						neighborPeerDesc.setContactAddress(keyPeer.get("contactAddress").toString());

					addNeighborPeer(neighborPeerDesc);
				}
			}
			if(peerMsg.get("type").equals(UserQueryMessage.MSG_USER_QUERY)){
				String question = peerMsg.get("textMessage").toString();
				String userName = params.get("name").toString();
				String ipAddress = params.get("contactAddress").toString().split("@")[1];
				ArrayList<Integer> questionClassDistribution = UtilityFunctions.getClassDistributionFromString(peerMsg.get("classDistribution").toString());
				
				System.out.println("Question from " + userName + ": " + question);
				
				int rating = 8;
				
				QueryReplyMessage queryReplyMessage = new QueryReplyMessage(peerDescriptor, question, UtilityFunctions.getSimilarityScore(questionClassDistribution, classContents), rating);
				send(new Address(ipAddress), queryReplyMessage);
			}
			if(peerMsg.get("type").equals(QueryReplyMessage.MSG_QUERY_REPLY)){
				System.out.println("Got a reply");
				String question = peerMsg.get("question").toString();
				String userName = params.get("name").toString();
				System.out.println(userName + " answered : " + question);
				System.out.println("Rating : " + peerMsg.get("answer"));
				System.out.println("Similarity : " + peerMsg.get("similarity"));
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
			if(nodeConfig.log_path!=null){
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
			if(nodeConfig.log_path!=null){

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

	public void joinToBootstrapPeer(){
		if(bootstrapAddress!=null){
			JoinMessage newJoinMsg = new JoinMessage(peerDescriptor);
			newJoinMsg.setNumPeerList(numberOfPeers);
			send(new Address(bootstrapAddress), newJoinMsg);
		}
	}
	
	public void sendQuestionToPeer(String toAddress, String message){
		UserQueryMessage textMessage = new UserQueryMessage(peerDescriptor, message, classContents);
		send(new Address(toAddress), textMessage);
	}
	
	public void sendPeerListRequestMessage(){
		if(bootstrapAddress!=null){
			PeerListRequestMessage peerListRequestMessage = new PeerListRequestMessage(peerDescriptor);
			send(new Address(bootstrapAddress), peerListRequestMessage);
		}
	}

	public void pingToPeer(String address){
		PingMessage newPingMsg = new PingMessage(peerDescriptor);
		send(new Address(address), null, newPingMsg);
	}

	public void pingToPeerFromList(){
		PingMessage newPingMsg = new PingMessage(peerDescriptor);
		if(!peerList.isEmpty()){
			Iterator<String> iter = peerList.keySet().iterator();
			//send pingMessage to first peer in the PeerListManager
			String key = iter.next();
			NeighborPeerDescriptor neighborPeer = peerList.get(key);
			send(neighborPeer, newPingMsg);
		}
	}

	public void pingToPeerRandomFromList(){
		PingMessage newPingMsg = new PingMessage(peerDescriptor);
		NeighborPeerDescriptor neighborPeer;
		if(!peerList.isEmpty()){
			System.out.println(peerList);
			//get set size
			int nKeys =  peerList.keySet().size();
			//get a random number
			int indexKey = (int) (Math.random()*nKeys);
			Iterator<String> iter = peerList.keySet().iterator();
			int i=0;
			String key = null;
			//break while when i is equal to random number
			while(iter.hasNext()){
				key = iter.next();

				if(i==indexKey){
					break;
				}
				i++;
			}
			//send ping message to peer	
			if(key!=null){
				neighborPeer = peerList.get(key);
				System.out.println(neighborPeer);
				send(neighborPeer, newPingMsg);
			}
		}
	}

	public void contactSBC(){
		if(nodeConfig.sbc!=null){
			requestPublicAddress();
		}
		else
			System.out.println("no sbc address found");
	}

	public void disconnectGWP(){
		closePublicAddress();
	}
}