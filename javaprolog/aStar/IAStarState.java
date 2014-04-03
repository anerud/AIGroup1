package aStar;

import java.util.Collection;

public interface IAStarState extends Comparable<IAStarState>{

	public Collection<? extends IAStarState> expand();
	
	public double getStateValue();
	
	public boolean hasReachedGoal();
	
}
