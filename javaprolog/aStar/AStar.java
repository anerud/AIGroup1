package aStar;

import java.util.PriorityQueue;

public class AStar {
	
	private PriorityQueue<AStarState> q;
	private AStarState currentState;
	
	/**
	 * Creates an AStar object with an empty priority queue
	 */
	public AStar() {
		q = new PriorityQueue<AStarState>();
	}
	
	/**
	 * Creates an AStar object with a starting point.
	 * @param staringPoint
	 */
	public AStar(AStarState staringPoint) {
		q = new PriorityQueue<>();
		q.add(staringPoint);
	}
	
	/**
	 * Runs the aStar algorithm until the goal is reached or
	 * until the search space is explored.
	 * @return false if the goal could not be reached. True if
	 * the goal was reached.
	 */
	public boolean run(){
		while(!q.isEmpty() && !currentState.hasReachedGoal()) {
			currentState = q.poll();
			q.addAll(currentState.expand());
		}
		return currentState.hasReachedGoal();
	}
	
	/**
	 * @return the current state
	 */
	public AStarState getCurrentState(){
		return currentState;
	}

}
