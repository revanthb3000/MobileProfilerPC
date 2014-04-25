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

import org.iitg.mobileprofiler.db.DatabaseConnector;
import org.iitg.mobileprofiler.p2p.msg.JoinMessage;
import org.iitg.mobileprofiler.p2p.msg.PeerListMessage;
import org.iitg.mobileprofiler.p2p.msg.PeerListRequestMessage;
import org.iitg.mobileprofiler.p2p.msg.PingMessage;
import org.iitg.mobileprofiler.p2p.msg.QueryReplyMessage;
import org.iitg.mobileprofiler.p2p.msg.RepoStorageMessage;
import org.iitg.mobileprofiler.p2p.msg.ResponseDataMessage;
import org.iitg.mobileprofiler.p2p.msg.ResponseRequestMessage;
import org.iitg.mobileprofiler.p2p.msg.UserQueryMessage;
import org.iitg.mobileprofiler.p2p.tools.PendingQuestion;
import org.iitg.mobileprofiler.p2p.tools.UtilityFunctions;

import com.google.gson.Gson;

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
	 * Bootstrap Address
	 */
	private String bootstrapAddress;
	
	/**
	 * This is the number of peers to which you wish to connect to (max)
	 * 0 - infinite.
	 */
	private int numberOfPeers;
	
	private ArrayList<PendingQuestion> pendingQuestions;
	
	public UserNodePeer(String key, String peerName, int peerPort, ArrayList<Integer> userClassContents, String bootstrapInfo, String SBCAddress, int numOfPeers){
		super(null, key, peerName, peerPort);
		classContents = userClassContents;
		bootstrapAddress = bootstrapInfo;
		numberOfPeers = numOfPeers;
		nodeConfig.sbc = SBCAddress;
		pendingQuestions = new ArrayList<PendingQuestion>();
	}
	
	public PeerListManager getPeerList(){
		return peerList;
	}
	
	public ArrayList<Integer> getClassContents() {
		return classContents;
	}

	public ArrayList<PendingQuestion> getPendingQuestions() {
		return pendingQuestions;
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
				int questionId = Integer.parseInt(peerMsg.get("askerQuestionId").toString());
				String question = peerMsg.get("textMessage").toString();
				String className = peerMsg.get("className").toString();
				String userName = params.get("name").toString();
				String ipAddress = peerMsg.getString("fromAddress");
				ArrayList<Integer> questionClassDistribution = UtilityFunctions.getClassDistributionFromString(peerMsg.get("classDistribution").toString());
				Double similarity = UtilityFunctions.getSimilarityScore(questionClassDistribution, classContents);
				
				System.out.println("Question from " + userName + ": " + question);
				
				pendingQuestions.add(new PendingQuestion(question, className, similarity, questionId, ipAddress, this));
			}
			if(peerMsg.get("type").equals(QueryReplyMessage.MSG_QUERY_REPLY)){
				System.out.println("Got a reply");
				String question = peerMsg.get("question").toString();
				String userName = params.get("name").toString();
				
				System.out.println(userName + " answered : " + question);
				System.out.println("Rating : " + peerMsg.get("answer"));
				System.out.println("Similarity : " + peerMsg.get("similarity"));
				
				int questionId = Integer.parseInt(peerMsg.get("askerQuestionId").toString());
				int answer = Integer.parseInt(peerMsg.get("answer").toString());
				Double similarity = Double.parseDouble(peerMsg.get("similarity").toString());

				DatabaseConnector databaseConnector = new DatabaseConnector();
				databaseConnector.addAnswer(questionId, answer, similarity);
				databaseConnector.closeDBConnection();
			}
			if(peerMsg.get("type").equals(ResponseDataMessage.MSG_RESPONSE_DATA)){
				System.out.println("Repo Updated");
				Gson gson = new Gson();
				ResponseDataMessage responseDataMessage = gson.fromJson(peerMsg.toString(), ResponseDataMessage.class);
				DatabaseConnector databaseConnector = new DatabaseConnector();
				databaseConnector.insertResponses(responseDataMessage.getResponses());
				databaseConnector.closeDBConnection();
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
	
	public void sendQuestionToPeers(String message, String className){
		DatabaseConnector databaseConnector = new DatabaseConnector();
		int questionId = databaseConnector.getMaxQuestionId() + 1;
		databaseConnector.addQuestion(message, className);
		databaseConnector.closeDBConnection();
		
		UserQueryMessage textMessage = new UserQueryMessage(peerDescriptor, message, className, classContents,questionId, getAddress().getHost() + ":" + getAddress().getPort());
		send(new Address(bootstrapAddress), textMessage);
	}
	
	public void sendReply(String question, String className, Double similarity, int answer, int questionId, String destinationIpAddress, Boolean isPublic){
		QueryReplyMessage queryReplyMessage = new QueryReplyMessage(peerDescriptor, question, similarity, answer, questionId);
		send(new Address(destinationIpAddress), queryReplyMessage);
		if(isPublic){
			RepoStorageMessage repoStorageMessage = new RepoStorageMessage(peerDescriptor, question, className, peerDescriptor.getName() , answer);
			send(new Address(bootstrapAddress), repoStorageMessage);	
		}
		DatabaseConnector databaseConnector = new DatabaseConnector();
		databaseConnector.insertResponse(peerDescriptor.getName(), question, answer, className);
		databaseConnector.closeDBConnection();
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
	
	public void updateRepo(){
		DatabaseConnector databaseConnector = new DatabaseConnector();
		int maxResponseId = databaseConnector.getMaxResponseId();
		databaseConnector.closeDBConnection();
		ResponseRequestMessage responseRequestMessage = new ResponseRequestMessage(peerDescriptor, maxResponseId);
		send(new Address(bootstrapAddress), responseRequestMessage);
	}

	public void disconnectGWP(){
		closePublicAddress();
	}
}