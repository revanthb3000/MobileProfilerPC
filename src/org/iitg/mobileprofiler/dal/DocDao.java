package org.iitg.mobileprofiler.dal;

public class DocDao {
	
	//numberOfOccurences
	public int n;
	
	//ClassId
	public int c;
	
	public DocDao(int classId) {
		this.c = classId;
		this.n = 1;
	}

	public int getClassId() {
		return c;
	}

	public void setClassId(int docLength) {
		this.c = docLength;
	}
	
	public int getNumberOfOccurences() {
		return n;
	}
	
	public void setNumberOfOccurences(int numberOfOccurences) {
		this.n = numberOfOccurences;
	}

	public void incrementOccurenceCount(){
		n++;
	}

	@Override
	public String toString() {
		return "DocDao [numberOfOccurences=" + n
				+ ", classId=" + c + "]";
	}
	
}
