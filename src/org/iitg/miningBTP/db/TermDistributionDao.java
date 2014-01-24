package org.iitg.miningBTP.db;

public class TermDistributionDao {
	
	private String feature;
	
	private int classId;
	
	/**
	 * Class and feature
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
