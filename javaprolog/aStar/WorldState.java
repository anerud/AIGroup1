package aStar;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import main.Goal;
import world.World;
import world.WorldObject;

public class WorldState implements IAStarState {

	private int distance;
	private int heuristic;
	private World world;
	private Goal goal;
	private List<String> actionsToGetHere;
	private Set<WorldObject> objectsToMove;
	private double heuristicWeight = 1;
	public WorldState(World world, Goal goal, int distance, List<String> actionToGetHere){
		this.world = world;
		this.goal = goal;
		this.distance = distance;
		this.heuristic = computeHeuristic();
		this.actionsToGetHere = actionToGetHere;
		objectsToMove = goal.getPddlExpression().getObjs();
	}
	
	private int computeHeuristic() {
		int h = 0;
		for(WorldObject wo : objectsToMove) {
			h += 2*world.nObjectsOnTopOf(wo);
		}
		return h;
	}
	
	@Override
	public double getStateValue() {
		return this.distance + this.heuristic*heuristicWeight;
	}
	
	@Override
	public boolean hasReachedGoal() {
		Set<WorldObject> s = world.filterByExistsInWorld(goal.getPddlExpression());
		return s.size() >= 1;
	}
	
	public void setHeuristicWeight(double heuristicWeight) {
		this.heuristicWeight = heuristicWeight;
	}

	@Override
	public int compareTo(IAStarState o) {
		//Here one can decide whether one wants FIFO or LILO behavior on queue.
		if(this.getStateValue() - o.getStateValue() > 0){
			return 1;
		}
		return -1;
	}

	@Override
	public Collection<? extends IAStarState> expand() {
		Collection<IAStarState> l = new LinkedList<IAStarState>();
		List<String> newList = new LinkedList<String>();
		Collections.copy(newList, actionsToGetHere);

		if(world.getHolding() != null){
			for(int i = 0;i<world.getStacks().size();i++){
				if(world.isPlaceable(i,world.getHolding())){
					World w = world.clone();
					w.getStacks().get(i).add(w.getHolding());
					w.setHolding(null);
					newList.add("(drop " + i + ")");
					WorldState state = new WorldState(w, goal, this.distance+1,newList);
					l.add(state);
				}
			}
		}else{
			for(int i = 0;i<world.getStacks().size();i++){
				if(!world.getStacks().get(i).isEmpty()){
					World w = world.clone();
					w.setHolding(w.getStacks().get(i).pop());
					newList.add("(pick " + i + ")");
					WorldState state = new WorldState(w, goal, this.distance+1,newList);
					l.add(state);
				}
			}
		}
		return l;
	}
	

}
