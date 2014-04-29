package org.iitg.mobileprofiler.db;

public class ResponseDao {

	private String userId;
	
	private String question;
	
	private int answer;
	
	private int classId;
	
	public ResponseDao(String userId, String question, int answer,
			int classId) {
		super();
		this.userId = userId;
		this.question = question;
		this.answer = answer;
		this.classId = classId;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getQuestion() {
		return question;
	}

	public void setQuestion(String question) {
		this.question = question;
	}

	public int getAnswer() {
		return answer;
	}

	public void setAnswer(int answer) {
		this.answer = answer;
	}

	public int getClassId() {
		return classId;
	}

	public void setClassId(int classId) {
		this.classId = classId;
	}

	@Override
	public String toString() {
		return "ResponseDao [userId=" + userId + ", question=" + question
				+ ", answer=" + answer + ", classId=" + classId + "]";
	}
	
}
