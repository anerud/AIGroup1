package aStar;

import world.WorldObject;
import logic.LogicalExpression;
import main.Goal;

public class HeuristicONE implements IHeuristic<WorldState>{

	@Override
	public double h(WorldState state, Goal goal) {
		Goal g = state.getGoal();
		LogicalExpression<WorldObject> le = g.getExpression();
		
		
		return 0;
	}

}