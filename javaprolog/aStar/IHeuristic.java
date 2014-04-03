package aStar;

import main.Goal;

public interface IHeuristic {
	
	double h(IAStarState state , Goal goal);
	

}
