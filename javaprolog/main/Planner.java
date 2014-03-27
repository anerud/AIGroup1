package main;
import java.util.*;

import world.World;
import world.WorldObject;

public class Planner {
	
	private World world;

	public Planner(World world) {
		this.world = world;
	}

	public List<String> solve(Goal goal){
        List<String> plan = new ArrayList<String>();

        //TODO: Exp expression = goal.getPddlExpression();

        //the expression determines the final state which we want to reach somehow.

        //TODO: do proper pddl parsing with the library
        String[] tmp1 = goal.toString().split(" ");
        String[] tmp = tmp1[0].split("\\(");
        String mainPredicate = tmp[tmp.length - 1];
        if(mainPredicate.equals("OR")){
            String[] parts = goal.toString().split(" \\(");
            //TODO take the quickest path
//            for(int i = 1; i < parts.length; i++){ //skipping the "OR predicate"
//                parts[i].split(" ")[0];
//            }
            mainPredicate = parts[1].split(" ")[0]; //Take the first one for now..
            goal = new Goal("(" + parts[1]);
        }
        if(mainPredicate.equals("holding")){
            String argtmp = goal.toString().split(" ")[1];
            String arg = argtmp.substring(0, argtmp.length() - 1);
            WorldObject wo = world.getWorldObject(arg);
            int woColumn = world.columnOf(wo);
            //TODO check that no object is being held
            if(world.isOntopOfStack(wo)){
                if(world.pick(woColumn)){
                    plan.add("I pick up...");
                    plan.add("pick " + woColumn);
                }
            } else {
                freeWorldObject(wo, plan);
                if(world.pick(woColumn)){
                    plan.add("I pick up...");
                    plan.add("pick " + woColumn);
                }
            }
        } else if(mainPredicate.equals("on")){
            String[] parts = goal.toString().split(" ");
            String arg1 = parts[1];
            String arg2 = parts[2].substring(0, parts[2].length() - 1);
            WorldObject wo1 = world.getWorldObject(arg1);
            WorldObject wo2 = world.getWorldObject(arg2);
            WorldObject holding = world.getHolding();
            if(holding !=null && holding.getId().equals(wo1.getId())){
                if(world.isOntopOfStack(wo2)){
                    int wo2Column = world.columnOf(wo2);
                    if(world.drop(wo2Column)){
                        plan.add("I drop down...");
                        plan.add("drop " + wo2Column);
                    }
                } else {
                    //TODO.. get's a bit more complicated.. time for some proper algorithms
                }
            } else if (holding == null && world.isOntopOfStack(wo1) && world.isOntopOfStack(wo2)){

            } else {
                //TODO: get's a bit more complicated.. time for some proper algorithms
            }
        } else {
            //TODO: Replace the following
            int column = 0;
            plan.add("I pick up . . .");
            plan.add("pick " + 1);
            plan.add(". . . and then I drop down");
            plan.add("drop " + (2));
        }
        return plan;
	}

    private void freeWorldObject(WorldObject wo, List<String> plan) {
        int woColumn = world.columnOf(wo);
        while(!world.isOntopOfStack(wo)){
            moveTopToNextColumn(woColumn, plan);
        }
    }

    /**
     * Moves the WorldObject to the column which is one step to the right of the specified column (modulo)
     * @param fromColumn
     * @param plan
     */
    private void moveTopToNextColumn(int fromColumn, List<String> plan) {
        //TODO: check that nothing is being held : if(holding != null) ...
        int numCol = world.numberOfColumns();
        if(world.moveTopToNextColumn(fromColumn)){
            plan.add("I pick up...");
            plan.add("pick " + fromColumn);
            plan.add("I put down...");
            plan.add("drop " + (fromColumn + 1) % numCol);
        }
    }
}