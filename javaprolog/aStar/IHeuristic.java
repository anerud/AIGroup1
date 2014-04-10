package aStar;

import main.Goal;

public interface IHeuristic<E extends IAStarState> {
	
	double h(E state , Goal goal);
	
}