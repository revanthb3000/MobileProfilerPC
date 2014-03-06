package org.iitg.mobileProfiler.db;

/**
 * This DAO is used to represent objects that are present in the TermDistribution table.
 * @author RB
 *
 */
public class TermDistributionDao {
	
	/**
	 * Term we're referring to.
	 */
	private String term;
	
	/**
	 * ClassId that is considered for this object
	 */
	private int classId;
	
	/**
	 * Number of documents that belong to "this.classId" contain the term "this.feature"
	 */
	private int A;
	
	public TermDistributionDao(String term, int classId, int a) {
		super();
		this.term = term;
		this.classId = classId;
		A = a;
	}

	public String getTerm() {
		return term;
	}

	public void setTerm(String feature) {
		this.term = feature;
	}

	public int getClassId() {
		return classId;
	}

	public void setClassId(int classId) {
		this.classId = classId;
	}

	public int getA() {
		return A;
	}

	public void setA(int a) {
		A = a;
	}

	@Override
	public String toString() {
		return "TermDistributionDao [term=" + term + ", classId="
				+ classId + ", A=" + A + "]";
	}

}
