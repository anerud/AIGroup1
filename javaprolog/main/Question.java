package main;

import java.util.List;

public class Question {

	private String question;
	private List<String> answer;

	public Question(String question) {
		this.question = question;
	}

	public void setQuestion(String question) {
		this.question = question;
	}
	
	public List<String> getAnswer() {
		return answer;
	}

	public String getQuestion() {
		return question;
	}

	public void setAnswer(List<String> answer) {
		this.answer = answer;
	}
}
