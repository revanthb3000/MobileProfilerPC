package org.iitg.mobileProfiler.dal;

import java.util.HashMap;
import java.util.Map;

public class PostingList {
	
	//DocumentMapping
	public Map<Integer, DocDao> m;
	
	public PostingList(){
		m = new HashMap<Integer,DocDao>();
	}
	
	public void addDocument(int docId, int classId){
		if(m.containsKey(docId)){
			m.get(docId).incrementOccurenceCount();
		}
		else{
			DocDao docDao = new DocDao(classId);
			m.put(docId, docDao);	
		}
	}

	public int getNumberOfDocuments(){
		return this.m.keySet().size();
	}
	

}
