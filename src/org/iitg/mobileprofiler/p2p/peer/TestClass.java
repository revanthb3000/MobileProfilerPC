package org.iitg.mobileprofiler.p2p.peer;

import it.unipr.ce.dsg.s2p.peer.PeerDescriptor;
import it.unipr.ce.dsg.s2p.sip.Address;

import java.util.Scanner;

import org.iitg.mobileprofiler.p2p.msg.JoinMessage;

/**
 * This is used to test the P2P classes
 * @author RB
 *
 */
public class TestClass {

	public static void main(String[] args){
	      Integer userInput;
	      Scanner in = new Scanner(System.in);	 
	      System.out.print("What type of node ?\n1.Bootstrap Node.\n2.User Node.\nYour choice : ");
	      userInput = Integer.parseInt(in.nextLine());
	      in.close();
	      if(userInput == 1){
	    	  startBootstrapNode();
	      }
	      else{
	    	  startUserNode();
	      }
	}
	
	public static void startBootstrapNode(){
		BootstrapPeer peer = new BootstrapPeer("config/bs.cfg", "443cb4f4e3894579a84e341199c58588");
		System.out.println("BootStrap Node has started - " + peer.toString());
	}
	

	
	/**
	 * 3 Application arguments: 
	 * 	configuration file
	 * 	address of destination peer
	 *  contact address of the destination peer
	 * 
	 * address and contact could be the same.
	 * 
	 * E.g: k.cfg kate@192.168.2.5:5075 kate@192.168.2.5:5075
	 * 
	 * @param args
	 */
	public static void startUserNode(){
		SimplePeer peer = new SimplePeer("config/m.cfg");
		System.out.println("Peer Descriptor: " + peer.getPeerDescriptor());
		PeerDescriptor peerDesc = peer.getPeerDescriptor();
		JoinMessage joinMessage = new JoinMessage(peerDesc);
		peer.send(new Address("192.168.1.3:5080"), joinMessage);
		System.out.println("Message sent !!");
	}
	
}
