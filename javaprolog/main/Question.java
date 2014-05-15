package main;

import java.util.List;

public class Question {

	private String question;
	private List<String> answer;
    private int questionId;
    private int subQuestionId;
    


	public Question(String question, int questionId, int subQuestionId) {
		super();
		this.question = question;
		this.questionId = questionId;
		this.subQuestionId = subQuestionId;
	}

	public int getQuestionId() {
		return questionId;
	}

	public void setQuestionId(int questionId) {
		this.questionId = questionId;
	}

	public int getSubQuestionId() {
		return subQuestionId;
	}

	public void setSubQuestionId(int subQuestionId) {
		this.subQuestionId = subQuestionId;
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
