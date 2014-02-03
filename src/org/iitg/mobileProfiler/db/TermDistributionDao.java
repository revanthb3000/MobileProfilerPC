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
	private String feature;
	
	/**
	 * ClassId that is considered for this object
	 */
	private int classId;
	
	/**
	 * Number of documents that belong to "this.classId" contain the term "this.feature"
	 */
	private int A;
	
	public TermDistributionDao(String feature, int classId, int a) {
		super();
		this.feature = feature;
		this.classId = classId;
		A = a;
	}

	public String getFeature() {
		return feature;
	}

	public void setFeature(String feature) {
		this.feature = feature;
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
		return "TermDistributionDao [feature=" + feature + ", classId="
				+ classId + ", A=" + A + "]";
	}

}
