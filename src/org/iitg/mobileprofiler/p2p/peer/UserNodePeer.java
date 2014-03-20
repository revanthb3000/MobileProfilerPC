package org.iitg.mobileprofiler.p2p.peer;

import it.unipr.ce.dsg.s2p.message.parser.JSONParser;
import it.unipr.ce.dsg.s2p.org.json.JSONException;
import it.unipr.ce.dsg.s2p.org.json.JSONObject;
import it.unipr.ce.dsg.s2p.peer.NeighborPeerDescriptor;
import it.unipr.ce.dsg.s2p.peer.Peer;
import it.unipr.ce.dsg.s2p.peer.PeerDescriptor;
import it.unipr.ce.dsg.s2p.peer.PeerListManager;
import it.unipr.ce.dsg.s2p.sip.Address;
import it.unipr.ce.dsg.s2p.util.FileHandler;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;

import org.iitg.mobileprofiler.p2p.msg.JoinMessage;
import org.iitg.mobileprofiler.p2p.msg.PeerListMessage;
import org.iitg.mobileprofiler.p2p.msg.PeerListRequestMessage;
import org.iitg.mobileprofiler.p2p.msg.PingMessage;
import org.iitg.mobileprofiler.p2p.msg.QueryReplyMessage;
import org.iitg.mobileprofiler.p2p.msg.UserQueryMessage;
import org.iitg.mobileprofiler.p2p.tools.PeerConfig;
import org.iitg.mobileprofiler.p2p.tools.UtilityFunctions;
import org.zoolu.tools.Log;

/**
 * The UserNodePeer node that we'll use for user nodes.
 * @author RB
 *
 */
public class UserNodePeer extends Peer {

	protected PeerConfig peerConfig;

	private FileHandler fileHandler;

	private Log log;
	
	private ArrayList<Integer> classContents;

	public UserNodePeer(String pathConfig, String key, ArrayList<Integer> userClassContents) {
		super(pathConfig, key);
		init(pathConfig);
		classContents = userClassContents;
	}

	public UserNodePeer(String pathConfig, String key, String peerName, int peerPort) {
		super(pathConfig, key, peerName, peerPort);
		init(pathConfig);
	}

	private void init(String pathConfig){
		this.peerConfig = new PeerConfig(pathConfig);
		fileHandler = new FileHandler();

		if(nodeConfig.log_path!=null){
			if(!fileHandler.isDirectoryExists(nodeConfig.log_path))
				fileHandler.createDirectory(nodeConfig.log_path);

			log = new Log(nodeConfig.log_path+"info_"+peerDescriptor.getAddress()+".log", Log.LEVEL_MEDIUM); 
		}
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
			if(nodeConfig.log_path!=null){
				String typeMsg = peerMsg.get("type").toString();
				int lengthMsg = peerMsg.toString().length();
				JSONObject info = new JSONObject();
				info.put("timestamp", System.currentTimeMillis());
				info.put("type", "recv");
				info.put("typeMessage", typeMsg);
				info.put("byte", lengthMsg);
				info.put("sender", sender.getURL());
				printJSONLog(info, log, false);
			}

			//add peer descriptor to list
			if(peerMsg.get("type").equals(PingMessage.MSG_PEER_PING)){
				PeerDescriptor neighborPeerDesc = new PeerDescriptor(params.get("name").toString(), params.get("address").toString(), params.get("key").toString(), params.get("contactAddress").toString());
				addNeighborPeer(neighborPeerDesc);
			}
			if(peerMsg.get("type").equals(PeerListMessage.MSG_PEER_LIST)){
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

			/*
			 *log - print info sent message 
			 */
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
					printJSONLog(info, log, false);
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

			/*
			 *log - print info sent message 
			 */
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
					printJSONLog(info, log, false);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}

		}

	}

	public void joinToBootstrapPeer(){
		if(peerConfig.bootstrap_peer!=null){
			JoinMessage newJoinMsg = new JoinMessage(peerDescriptor);
			newJoinMsg.setNumPeerList(peerConfig.req_npeer);
			send(new Address(peerConfig.bootstrap_peer), newJoinMsg);
		}
	}
	
	public void sendQuestionToPeer(String toAddress, String message){
		UserQueryMessage textMessage = new UserQueryMessage(peerDescriptor, message, classContents);
		send(new Address(toAddress), textMessage);
	}
	
	public void sendPeerListRequestMessage(){
		if(peerConfig.bootstrap_peer!=null){
			PeerListRequestMessage peerListRequestMessage = new PeerListRequestMessage(peerDescriptor);
			send(new Address(peerConfig.bootstrap_peer), peerListRequestMessage);
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