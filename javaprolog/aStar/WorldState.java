package aStar;

import java.util.Collection;
import java.util.LinkedList;

import main.Goal;

import world.World;
import world.WorldObject;

public class WorldState implements AStarState {
	
	private int distance;
	private int heuristic;
	private World world;
	private Goal goal;
	private double heuristicWeight = 1;
	public WorldState(World world, Goal goal, int distance){
		this.world = world;
		this.goal = goal;
		this.distance = distance;
		this.heuristic = computeHeuristic();
	}
	
	private int computeHeuristic() {
		return 0;
	}
	
	@Override
	public double getStateValue() {
		return this.distance + this.heuristic*heuristicWeight;
	}
	
	@Override
	public boolean hasReachedGoal() {
		// TODO Auto-generated method stub
		return false;
	}
	
	public void setHeuristicWeight(double heuristicWeight) {
		this.heuristicWeight = heuristicWeight;
	}

	@Override
	public int compareTo(AStarState o) {
		//Here one can decide whether one wants FIFO or LILO behavior on queue.
		if(this.getStateValue() - o.getStateValue() > 0){
			return 1;
		}
		return -1;
	}

	@Override
	public Collection<? extends AStarState> expand() {
		Collection<AStarState> l = new LinkedList<AStarState>();
		if(world.getHolding() != null){
			for(int i = 0;i<world.getStacks().size();i++){
				if(world.isPlaceable(i,world.getHolding())){
					World w = world.clone();
					w.getStacks().get(i).add(w.getHolding());
					w.setHolding(null);
					WorldState state = new WorldState(w, goal, this.distance+1);
					l.add(state);
				}
			}
		}else{
			for(int i = 0;i<world.getStacks().size();i++){
				if(!world.getStacks().get(i).isEmpty()){
					World w = world.clone();
					w.setHolding(w.getStacks().get(i).pop());
					WorldState state = new WorldState(w, goal, this.distance+1);
					l.add(state);
				}
			}
		}
		return l;
	}
	

}
