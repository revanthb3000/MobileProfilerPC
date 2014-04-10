package org.iitg.mobileprofiler.db;

public class ResponseDao {

	private String userId;
	
	private String question;
	
	private int answer;
	
	private String className;
	
	public ResponseDao(String userId, String question, int answer,
			String className) {
		super();
		this.userId = userId;
		this.question = question;
		this.answer = answer;
		this.className = className;
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

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	@Override
	public String toString() {
		return "ResponseDao [userId=" + userId + ", question=" + question
				+ ", answer=" + answer + ", className=" + className + "]";
	}
	
}
