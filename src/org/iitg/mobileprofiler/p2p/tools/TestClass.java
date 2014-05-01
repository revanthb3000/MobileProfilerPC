package org.iitg.mobileprofiler.p2p.tools;

import it.unipr.ce.dsg.s2p.org.json.JSONException;
import it.unipr.ce.dsg.s2p.peer.PeerListManager;

import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

import org.iitg.mobileprofiler.db.DatabaseConnector;
import org.iitg.mobileprofiler.p2p.peer.UserNodePeer;

/**
 * This is used to test the P2P classes
 * 
 * @author RB
 * 
 */
public class TestClass {
	
	private static Scanner in = null;
	
	private static String ipAddress = "192.168.1.2";
	
	private static int boostrapPort = 5080;
	
	private static int SBCPort = 6066;

	public static void main(String[] args) throws JSONException {
		startFullPeerUserNode();
	}
	
	public static void startFullPeerUserNode(){
		in = new Scanner(System.in);
		System.out.print("Peer Name and port : ");
		String peerName = in.nextLine().trim();
		int portNumber = Integer.parseInt(peerName.split(":")[1]);
		peerName = peerName.split(":")[0];

		ArrayList<Integer> userClassContents = null;
		int numberOfClasses = 0;
		
		DatabaseConnector databaseConnector = new DatabaseConnector();
		numberOfClasses = databaseConnector.getNumberOfClasses();
		userClassContents = databaseConnector.getNumberOfDocuments(0, numberOfClasses, true);
		databaseConnector.closeDBConnection();
		
		if(peerName.contains("all")){
			userClassContents = new ArrayList<Integer>();
			for(int i=0;i<numberOfClasses;i++){
				if(i%3==0){
					userClassContents.add(8);
				}
				else{
					userClassContents.add(0);
				}
			}	
		}
		
		System.out.println(userClassContents);

		UserNodePeer peer = new UserNodePeer(UtilityFunctions.getHexDigest(peerName), 
											 peerName, portNumber,  
											 /*UtilityFunctions.getRandomClassDistribution()*/userClassContents,
											 ipAddress + ":" + boostrapPort, 
											 ipAddress + ":" + SBCPort ,0);
		
		while(true){
			System.out.print("What would you like to do ?\n1.Join the network.\n2.Get list of peers.\n3.Update peers list.\n4.Ask a question.\n5.See pending questions and reply to all.\n6.Update Repo\nYour option : ");
			Integer userInput = in.nextInt();
			if(userInput==1){
				peer.joinToBootstrapPeer();	
			}
			else if (userInput==2){
				PeerListManager peerList = peer.getPeerList();
				for(String key : peerList.keySet()){
					System.out.println(peerList.get(key));
				}
			}
			else if (userInput==3){
				peer.sendPeerListRequestMessage();
			}
			else if(userInput==4){
				Scanner scanner = new Scanner(System.in);
				System.out.print("What is your message ? ");
				String message = scanner.nextLine();
				
				peer.sendQuestionToPeers(message, "Random Class");
			}
			else if(userInput==5){
				for(PendingQuestion pendingQuestion : peer.getPendingQuestions()){

					System.out.println("What would you like to send for " + pendingQuestion.getQuestion() + " ?");
					Integer rating = in.nextInt();
					System.out.println("Public or private ?");
					
					Integer privacy = in.nextInt();
					pendingQuestion.setAnswer(rating);
					pendingQuestion.sendReply((privacy==1)?true:false);
				}
			}
			else if(userInput==6){
				peer.updateRepo();
			}
		}
	}

}
