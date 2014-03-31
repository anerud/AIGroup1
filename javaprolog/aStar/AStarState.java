package aStar;

import java.util.Collection;

public interface AStarState extends Comparable<AStarState>{

	public Collection<? extends AStarState> expand();
	
	public double getStateValue();
	
	public boolean hasReachedGoal();
	
}
