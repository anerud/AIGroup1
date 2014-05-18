package main;

import aStar.AStar;
import aStar.WorldState;
import world.World;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Planner {
	
	private World world;

	public Planner(World world) {
		this.world = world;
	}

	public List<String> solve(Goal goal) throws CloneNotSupportedException {
        WorldState ws = new WorldState(world, goal, new LinkedList<String>(),0);
        AStar astar = new AStar(ws);
        if(astar.run()){
            return ((WorldState)astar.getCurrentState()).getActionsToGetHere();
        }
        List<String> plan = new ArrayList<String>();
        return plan;

    }
}
