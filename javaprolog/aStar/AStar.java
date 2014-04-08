package aStar;

import java.util.PriorityQueue;

public class AStar {
	
	private PriorityQueue<IAStarState> q;
	private IAStarState currentState;
	
	/**
	 * Creates an AStar object with an empty priority queue
	 */
	public AStar() {
		q = new PriorityQueue<IAStarState>();
	}
	
	/**
	 * Creates an AStar object with a starting point.
	 * @param staringPoint
	 */
	public AStar(IAStarState staringPoint) {
		q = new PriorityQueue<>();
		q.add(staringPoint);
	}
	
	/**
	 * Runs the aStar algorithm until the goal is reached or
	 * until the search space is explored.
	 * @return false if the goal could not be reached. True if
	 * the goal was reached.
	 */
	public boolean run(){ //TODO: dijkstra... this is Breadth first
		do {
			currentState = q.poll();
			q.addAll(currentState.expand());
		} while (!q.isEmpty() && !currentState.hasReachedGoal());
		return currentState.hasReachedGoal();
	}
	
	/**
	 * @return the current state
	 */
	public IAStarState getCurrentState(){
		return currentState;
	}

}
