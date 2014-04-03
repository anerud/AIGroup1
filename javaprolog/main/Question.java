package main;

import java.util.HashMap;
import java.util.Map;

public class Question {

	private Map<String, String> questions;
	private String answer;

	public Question() {
		questions = new HashMap<String, String>();
	}

	public String getAnswer() {
		return answer;
	}

	public void setAnswer(String answer) {
		this.answer = answer;
	}

	public Map<String, String> getQuestions() {
		return questions;
	}

	public void setQuestions(Map<String, String> questions) {
		this.questions = questions;
	}

	public void addQuestions(String q, String a) {
		this.questions.put(a, q);
	}
}
