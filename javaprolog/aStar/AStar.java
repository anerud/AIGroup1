package aStar;

import java.util.Collection;
import java.util.PriorityQueue;

public class AStar {
	
	private PriorityQueue<IAStarState> q;
	private IAStarState currentState;
    public static int nStatesChecked;
    public static int nStatesAddedToQ;


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
            iters++;
            time1 = System.currentTimeMillis();
			currentState = q.poll();

            //Debugging
            //____________________________
            double pathLength = ((WorldState)currentState).getActionsToGetHere().size() + ((WorldState)currentState).getHeuristicValue();
            double stateValue = ((WorldState)currentState).getStateValue();
//            if(((WorldState)currentState).getWorld().getRepresentString().equals(".e,.a,l,n,o,p,q,r,.v,u,.t,s,.i,h,j,...k,g,c,b,..d,m,f,...........")){
//                this.getClass();
//            }
            //[["e"],["a","l", "n", "o", "p", "q", "r", "s", "t", "u", "v"],[],[],["i","h","j"],[],[],["k","g","c","b"],[],["d","m","f"],[],[],[],[],[],[],[],[],[],[]],
            //____________________________
            Collection<? extends IAStarState> neighbours = currentState.expand();
			q.addAll(neighbours);
            time2 = System.currentTimeMillis();
            diff = time2 - time1;
            sum += diff;

            //Debugging
            //__________________
            nStatesChecked++;
            nStatesAddedToQ += neighbours.size();
//            try (PrintWriter asdf = new PrintWriter(new BufferedWriter(
//                    new FileWriter("GoalLog.txt", true)))) {
//                asdf.append(((WorldState)currentState).getWorld().getRepresentString() + " \n");
//                asdf.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
            //__________________
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
