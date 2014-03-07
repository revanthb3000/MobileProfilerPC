package org.iitg.mobileprofiler.db;

public class ActivityDao {
	
	private int activityId;
	
	private String activityType;
	
	private String activityInfo;
	
	private String timeStamp;
	
	private String assignedClass;
	
	public ActivityDao(int activityId, String activityType,
			String activityInfo, String timeStamp, String assignedClass) {
		super();
		this.activityId = activityId;
		this.activityType = activityType;
		this.activityInfo = activityInfo;
		this.timeStamp = timeStamp;
		this.assignedClass = assignedClass;
	}

	public int getActivityId() {
		return activityId;
	}

	public void setActivityId(int activityId) {
		this.activityId = activityId;
	}

	public String getActivityType() {
		return activityType;
	}

	public void setActivityType(String activityType) {
		this.activityType = activityType;
	}

	public String getActivityInfo() {
		return activityInfo;
	}

	public void setActivityInfo(String activityInfo) {
		this.activityInfo = activityInfo;
	}

	public String getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(String timeStamp) {
		this.timeStamp = timeStamp;
	}

	public String getAssignedClass() {
		return assignedClass;
	}

	public void setAssignedClass(String assignedClass) {
		this.assignedClass = assignedClass;
	}

	@Override
	public String toString() {
		return "ActivityDao [activityId=" + activityId + ", activityType="
				+ activityType + ", activityInfo=" + activityInfo
				+ ", timeStamp=" + timeStamp + ", assignedClass="
				+ assignedClass + "]";
	}
	
	

}
