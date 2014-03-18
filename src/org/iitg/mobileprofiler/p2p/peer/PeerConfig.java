package org.iitg.mobileprofiler.p2p.peer;

import org.zoolu.tools.Configure;
import org.zoolu.tools.Parser;

/**
 * Class for peer configuration
 * 
 * @author Fabrizio Caramia
 *
 */
public class PeerConfig extends Configure {


	/*
	 * Address of the bootstrap peer, for ex. bootstrap@192.168.1.2:8090
	 * 
	 */
	public String bootstrap_peer = null;
	
	/*
	 *Number of peers returned from BootstrapPeer from its list
	 *Default value: 0 (return all peer)
	 */
	public int req_npeer = 0;  



	public PeerConfig(String file){

		// load configuration
		loadFile(file);


	}


	/** Parses a single line (loaded from the config file) */
	protected void parseLine(String line)
	{  

		String attribute;
		Parser par;
		int index=line.indexOf("=");
		if (index>0) {  attribute=line.substring(0,index).trim(); par=new Parser(line,index+1);  }
		else {  attribute=line; par=new Parser("");  }


		if (attribute.equals("bootstrap_peer")) 	  {  bootstrap_peer=par.getString();  return;  } 
		if (attribute.equals("req_npeer")) 	  {  req_npeer=par.getInt();  return;  } 
	}




}
