package aStar;

import main.Goal;

public class HeuristicONE implements IHeuristic<WorldState>{

	@Override
	public double h(WorldState state, Goal goal) {
		return 0;
	}

}
