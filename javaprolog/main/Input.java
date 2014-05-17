package main;

import world.WorldObject;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Input {

	private String holding;

	private List<String> utterance;

	private List<List<String>> worldoriginal;

	private List<List<String>> examples;

	private List<List<String>> world;

	private Map<String, WorldObject> objects;

	private String output;

	private String goals;

	private List<String> plan;

	private List<Question> questions;

	public Input() {
		utterance = new LinkedList<>();
		worldoriginal = new LinkedList<>();
		examples = new LinkedList<>();
		world = new LinkedList<>();
		objects = new HashMap<>();
		plan = new LinkedList<>();
		questions = new LinkedList<>();
	}

	public List<Question> getQuestions() {
		return questions;
	}

	public void setQuestions(List<Question> questions) {
		this.questions = questions;
	}

	public List<String> getPlan() {
		return plan;
	}

	public void setPlan(List<String> plan) {
		this.plan = plan;
	}

	public void setGoals(String goal) {
		this.goals = goal;
	}

	public String getGoals() {
		return goals;
	}

	public String getOutput() {
		return output;
	}

	public void setOutput(String output) {
		this.output = output;
	}

	public String getHolding() {
		return holding;
	}

	public void setHolding(String holding) {
		this.holding = holding;
	}

	public List<String> getUtterance() {
		return utterance;
	}

	public void setUtterance(List<String> utterance) {
		this.utterance = utterance;
	}

	public List<List<String>> getExamples() {
		return examples;
	}

	public void setExamples(List<List<String>> examples) {
		this.examples = examples;
	}

	public List<List<String>> getWorld() {
		return world;
	}

	public List<List<String>> getWorldoriginal() {
		return worldoriginal;
	}

	public void setWorld(List<List<String>> world) {
		this.world = world;
	}

	public void setWorldoriginal(List<List<String>> worldoriginal) {
		this.worldoriginal = worldoriginal;
	}

	public Map<String, WorldObject> getObjects() {
		return objects;
	}

	public void setObjects(Map<String, WorldObject> objects) {
		this.objects = objects;
	}

}
