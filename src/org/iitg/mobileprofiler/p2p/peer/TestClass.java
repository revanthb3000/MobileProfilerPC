package org.iitg.mobileprofiler.p2p.peer;

import it.unipr.ce.dsg.s2p.peer.PeerDescriptor;
import it.unipr.ce.dsg.s2p.peer.PeerListManager;
import it.unipr.ce.dsg.s2p.sip.Address;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.Scanner;

import org.iitg.mobileprofiler.p2p.msg.JoinMessage;

/**
 * This is used to test the P2P classes
 * 
 * @author RB
 * 
 */
public class TestClass {
	
	private static Scanner in = null;

	public static void main(String[] args) {
		in = new Scanner(System.in);
		System.out.print("What type of node ?\n1.Bootstrap Node.\n2.UserNode Peer.\nYour choice : ");
		
		Integer userInput;
		userInput = Integer.parseInt(in.nextLine());

		if (userInput == 1) {
			startBootstrapNode();
		} else if (userInput == 2) {
			startFullPeerUserNode();
		}
	}

	public static void startBootstrapNode() {
		BootstrapPeer peer = new BootstrapPeer("config/bs.cfg",getHexDigest("bootstrap"));
		System.out.println("BootStrap Node has started - " + peer.toString());
	}

	public static void startSimplePeerUserNode() {
		in = new Scanner(System.in);
		System.out.print("Config file name : ");
		String configFileName = in.nextLine().trim();
		
		SimplePeer peer = new SimplePeer("config/" + configFileName);
		System.out.println("Peer Descriptor: " + peer.getPeerDescriptor());
		PeerDescriptor peerDesc = peer.getPeerDescriptor();
		
		JoinMessage joinMessage = new JoinMessage(peerDesc);
		peer.send(new Address("172.16.27.15:5080"), joinMessage);
	}
	
	public static void startFullPeerUserNode(){
		in = new Scanner(System.in);
		System.out.print("Config file name : ");
		String configFileName = in.nextLine().trim();
		
		String currentTime = "" + (new Date()).getTime();
		UserNodePeer peer = new UserNodePeer("config/"+configFileName, getHexDigest(currentTime));
		
		while(true){
			System.out.print("What would you like to do ?\n1.Join the network.\n2.Get list of peers.\n3.Update peers list.\n4.Send a message to peer.\n5.Send ping to random peer.\nYour option : ");
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
				
				System.out.print("Where should I send it to ? ");
				String toAddress = scanner.nextLine();
				peer.sendTextMessageToPeer(toAddress, message);
			}
			else if(userInput==5){
				peer.pingToPeerRandomFromList();
			}
		}
	}
	
	public static String getHexDigest(String inputString){
		MessageDigest md = null;;
		try {
			md = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return "";
		}
		md.update(inputString.getBytes());
		byte[] digest = md.digest();
		StringBuffer sb = new StringBuffer();
		for (byte b : digest) {
			sb.append(Integer.toHexString((int) (b & 0xff)));
		}
		return sb.toString();
	}

}
