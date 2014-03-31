package aStar;

import java.util.Collection;

import main.Goal;

import world.World;

public class WorldState implements AStarState {
	
	private double distance;
	private double heuristic;
	private World world;
	private Goal goal;
	
	public WorldState(World world, Goal goal, double distance){
		this.world = world;
		this.goal = goal;
		this.distance = distance;
		this.heuristic = computeHeuristic();
	}
	
	private double computeHeuristic() {
		return 0;
	}
	
	@Override
	public double getStateValue() {
		return this.distance + this.heuristic;
	}
	
	@Override
	public boolean hasReachedGoal() {
		// TODO Auto-generated method stub
		return false;
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
		// TODO Auto-generated method stub
		return null;
	}

}
