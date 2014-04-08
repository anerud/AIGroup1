package aStar;

import java.util.*;

import main.Goal;
import world.World;
import world.WorldObject;

public class WorldState implements IAStarState {

	private int bestDistanceToGetHere; //Best found so far, considering we are using dijkstra..

    private List<String> bestActionsToGetHere; //Best found so far, considering we are using dijkstra..
	private int distanceToGoHeuristic;
	private double heuristicWeight = 1;

	private World world;
	private Goal goal;

    /**
     *
     * @param world
     * @param goal
     * @param bestDistanceToGetHere
     * @param actionToGetHere if null, this is assumed to be the initial state
     */
    public WorldState(World world, Goal goal, int bestDistanceToGetHere, List<String> actionToGetHere){
		this.world = world;
		this.goal = goal;
		this.bestDistanceToGetHere = bestDistanceToGetHere;
		this.distanceToGoHeuristic = 0; //computeHeuristic();
		this.bestActionsToGetHere = actionToGetHere == null ? new ArrayList<String>() : actionToGetHere;

        //Debug
        if(bestActionsToGetHere.size() == 3){
            if(bestActionsToGetHere.get(1).equals("pick 9")){
                String hupp = "drop " + bestActionsToGetHere.get(0).split(" ")[1];
                if(bestActionsToGetHere.get(2).equals(hupp)){
                    bestActionsToGetHere.size();
                }
            }
        }
	}
	
	private int computeHeuristic() {     //TODO: this will not work.. The heuristic below is not a lower bound.
		int h = 0;
		for(WorldObject wo : goal.getExpression().getObjs()) {
			h += 2*world.nObjectsOnTopOf(wo);
		}
		return h;
	}
	
	@Override
	public double getStateValue() {
		return this.bestDistanceToGetHere + this.distanceToGoHeuristic *heuristicWeight;
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
		//Here one can decide whether one wants FIFO or LILO behavior on queue.     // <--- Same thing, different name
		if(this.getStateValue() - o.getStateValue() > 0){
			return 1;
		}
        if(this.getStateValue() == o.getStateValue()) return 0;
		return -1;
	}

    /**
     * TODO: don't check the same state more than once
     * @return
     */
	@Override
	public Collection<? extends IAStarState> expand() {
		Collection<IAStarState> l = new LinkedList<IAStarState>();

		if(world.getHolding() != null){
            for(int i = 0; i<world.getStacks().size(); i++){
                World w = world.clone();

                //Debug
                if(bestActionsToGetHere.size() == 2){
                    if(bestActionsToGetHere.get(1).equals("pick 9")){
//                        String hupp = "drop " + bestActionsToGetHere.get(0).split(" ")[1];
                        bestActionsToGetHere.size();
                    }
                }
                if(w.drop(i)){
                    List<String> newList = new LinkedList<String>(bestActionsToGetHere);
                    newList.add("drop " + i);
                    WorldState state = new WorldState(w, goal, this.bestDistanceToGetHere +1, newList);
                    l.add(state);
                }
			}
		} else {
			for(int i = 0;i<world.getStacks().size();i++){
                World w = world.clone();
                if(w.pick(i)){
                    List<String> newList = new LinkedList<String>(bestActionsToGetHere);
                    newList.add("pick " + i);
                    WorldState state = new WorldState(w, goal, this.bestDistanceToGetHere +1, newList);
                    l.add(state);
                }
			}
		}
		return l;
	}
	

}
