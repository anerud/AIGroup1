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
	 * Polls the first object in the priority queue and expands it.
	 * @return false if the queue is empty and true otherwise.
	 * one can loop by:
	 * while(iterate() || !getCurrentState().hasReachedGoal());
	 */
	public boolean iterate(){
		if(q.isEmpty()) return false;
		currentState = q.poll();
		q.addAll(currentState.expand());
		return true;
	}
	
	/**
	 * @return the current state
	 */
	public AStarState getCurrentState(){
		return currentState;
	}

}
