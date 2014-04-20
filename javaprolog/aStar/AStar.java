package aStar;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
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
	public boolean run() throws CloneNotSupportedException {
        long time1 = 0;
        long time2 = 0;
        long diff = 0;
        int iters = 0;
        int sum = 0;
		do {
//            nStatesChecked++;
//
//            try (PrintWriter asdf = new PrintWriter(new BufferedWriter(
//                    new FileWriter("GoalLog.txt", true)))) {
//                asdf.append(getRepresentString() + " \n");
//                asdf.close();
//            } catch (IOException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
            iters++;
            time1 = System.currentTimeMillis();
			currentState = q.poll();
			q.addAll(currentState.expand());
            time2 = System.currentTimeMillis();
            diff = time2 - time1;
            sum += diff;
		} while (!q.isEmpty() && !currentState.hasReachedGoal());
        double average = (double)sum/iters;
		return currentState.hasReachedGoal();
	}
	
	/**
	 * @return the current state
	 */
	public IAStarState getCurrentState(){
		return currentState;
	}

}
