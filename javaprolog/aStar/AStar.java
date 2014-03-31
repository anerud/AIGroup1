package aStar;

import java.util.Collection;
import java.util.PriorityQueue;

public class AStar<E extends AStarState> {
	
	private PriorityQueue<E> q;
	private E currentState;
	
	/**
	 * Creates an AStar object with an empty priority queue
	 */
	public AStar() {
		q = new PriorityQueue<E>();
	}
	
	/**
	 * Creates an AStar object with a starting point.
	 * @param staringPoint
	 */
	public AStar(E staringPoint) {
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
		q.addAll((Collection<? extends E>) currentState.expand());
		return true;
	}
	
	/**
	 * @return the current state
	 */
	public E getCurrentState(){
		return currentState;
	}

}
