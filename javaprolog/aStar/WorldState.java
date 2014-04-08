package aStar;

import java.util.*;

import logic.LogicalExpression;
import main.Goal;
import world.World;
import world.WorldObject;

public class WorldState implements IAStarState {


    private List<String> bestActionsToGetHere; //Best found so far, considering we are using dijkstra..
	private double heuristicWeight = 1;
	private int distanceToGoHeuristic;
	private World world;
	private Goal goal;
	private Set<WorldObject> objectsToMove;
	private static Set<String> visitedWorld = new HashSet<String>();
	
    /**
     *
     * @param world
     * @param goal
     * @param actionToGetHere if null, this is assumed to be the initial state
     */
    public WorldState(World world, Goal goal, List<String> actionToGetHere){
		this.world = world;
		this.goal = goal;
		this.distanceToGoHeuristic = 0; //TODO computeHeuristic();
		this.bestActionsToGetHere = actionToGetHere;
	}
	
	private int computeHeuristic() {     //TODO: this will not work.. The heuristic below is not a lower bound.
		LogicalExpression<WorldObject> asdf = goal.getExpression();
		for(WorldObject wo : asdf.getObjs()) {
			//Do something here
		}
		return 0;
	}
	
	
	@Override
	public double getStateValue() {
		return bestActionsToGetHere.size() + this.distanceToGoHeuristic*heuristicWeight;
	}
	
	@Override
	public boolean hasReachedGoal() {
        return world.isGoalFulFilled(goal);
	}
	
	public void setHeuristicWeight(double heuristicWeight) {
		this.heuristicWeight = heuristicWeight;
	}

    public List<String> getBestActionsToGetHere() {
        return bestActionsToGetHere;
    }

    public void setBestActionsToGetHere(List<String> bestActionsToGetHere) {
        this.bestActionsToGetHere = bestActionsToGetHere;
    }

	@Override
	public int compareTo(IAStarState o) {
		//Here one can decide whether one wants FIFO or LIFO behavior on queue.
		if(this.getStateValue() - o.getStateValue() >= 0){
			return 1;
		}
		return -1;
	}

    /**
     * @return
     */
	@Override
	public Collection<? extends IAStarState> expand() {
		Collection<IAStarState> l = new LinkedList<IAStarState>();
		if(world.getHolding() != null){
            for(int i = 0; i<world.getStacks().size(); i++){
                World w = world.clone();
                if(w.drop(i) && !visitedWorld.contains(w.getRepresentString())){
                    visitedWorld.add(w.getRepresentString());
                    List<String> newList = new LinkedList<String>(bestActionsToGetHere);
                    newList.add("drop " + i);
                    WorldState state = new WorldState(w, goal, newList);
                    l.add(state);
                }
			}
		} else {
			for(int i = 0;i<world.getStacks().size();i++){
                World w = world.clone();
                if(w.pick(i) && !visitedWorld.contains(w.getRepresentString())){
                	visitedWorld.add(w.getRepresentString());
                    List<String> newList = new LinkedList<String>(bestActionsToGetHere);
                    newList.add("pick " + i);
                    WorldState state = new WorldState(w, goal, newList);
                    l.add(state);
                }
			}
		}
		return l;
	}

	

}
