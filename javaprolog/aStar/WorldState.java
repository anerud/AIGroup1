package aStar;

import java.util.*;

import logic.LogicalExpression;
import main.Goal;
import world.RelativeWorldObject;
import world.World;
import world.WorldConstraint;
import world.WorldObject;

public class WorldState implements IAStarState {


    private List<String> actionsToGetHere; //Best found so far, considering we are using dijkstra..
	private double heuristicWeight = 2;
	private int heuristicValue;
	private World world;
	private Goal goal;
	private Set<WorldObject> objectsToMove;
	private IHeuristic<WorldState> heuristic = new HeuristicONE();

    public static Set<String> getVisitedWorld() {
        return visitedWorld;
    }

    public static void setVisitedWorld(Set<String> visitedWorld) {
        WorldState.visitedWorld = visitedWorld;
    }

    private static Set<String> visitedWorld = new HashSet<String>();
	
    /**
     *
     * @param world
     * @param goal
     * @param actionToGetHere
     */
    public WorldState(World world, Goal goal, List<String> actionToGetHere) throws CloneNotSupportedException {
		this.world = world;
		this.goal = goal;
		this.heuristicValue = (int) heuristic.h(this, goal);
		this.heuristicValue = computeHeuristic(goal.getExpression()).size()*2;
		this.actionsToGetHere = actionToGetHere;
	}

    /**
     * Only supports dnf expressions. If not dnf, an empty set is returned.   //TODO: make it support cnf as well, as some expressions are not practical in dnf. Consider for example the sentence "put all objects beside an object"
     * @param le
     * @return
     */

	private Set<WorldObject> computeHeuristic(LogicalExpression<WorldObject> le) throws CloneNotSupportedException {
        if(!le.isDnf()){
            return new HashSet<WorldObject>();
        }
        if(le.getOp().equals(LogicalExpression.Operator.AND) || le.size() <= 1){
            Set<WorldObject> minObjsToMoveSet = new HashSet<WorldObject>();
            if(le.getObjs() != null){
                for(WorldObject wo : le.getObjs()) {
                    minObjsToMoveSet.addAll(calculateMinObjsToMove(wo));
                }
            } //Note that the expression is simplified and dnf, so we do not need to check the logicalexpressions..
            return minObjsToMoveSet;
        } else {
            //return the smallest set
            Set<WorldObject> smallestSet = new HashSet<WorldObject>();
            boolean first = true;
            if(le.getObjs() != null){
                for(WorldObject wo : le.getObjs()) {
                    Set<WorldObject> thisSet = calculateMinObjsToMove(wo);
                    if(first){
                        smallestSet = thisSet;
                        first = false;
                    } else if(thisSet.size() < smallestSet.size()){
                        smallestSet = thisSet;
                    }
                }
            }
            for(LogicalExpression<WorldObject> le1 : le.getExpressions()){
                //This is an AND expression
                Set<WorldObject> thisSet = computeHeuristic(le1);
                if(first){
                    smallestSet = thisSet;
                    first = false;
                } else if(thisSet.size() < smallestSet.size()){
                    smallestSet = thisSet;
                }
            }
            return smallestSet;
        }
	}

    private Set<WorldObject> calculateMinObjsToMove(WorldObject wo) {
        Set<WorldObject> minObjs = new HashSet<>();
        if(!(wo instanceof RelativeWorldObject)){
            minObjs = new HashSet<>(world.objectsAbove(wo));
            return minObjs;
        }
        WorldObject woRel = ((RelativeWorldObject) wo).getRelativeTo();

        if(woRel instanceof RelativeWorldObject){
            minObjs.addAll(calculateMinObjsToMove(woRel));
        }

        switch (((RelativeWorldObject)wo).getRelation()) {
            case ONTOP:
                    if(!world.hasRelation(WorldConstraint.Relation.ONTOP, wo, woRel)){
                        minObjs.addAll(world.objectsAbove(new WorldObject(wo)));
                        minObjs.addAll(world.objectsAbove(new WorldObject(woRel)));
                        minObjs.add(new WorldObject(wo));
                    }
                break;
            case INSIDE:
                if(!world.hasRelation(WorldConstraint.Relation.ONTOP, wo, woRel)){
                    minObjs.addAll(world.objectsAbove(new WorldObject(wo)));
                    minObjs.addAll(world.objectsAbove(new WorldObject(woRel)));
                    minObjs.add(new WorldObject(wo));
                }
                break;
            case ABOVE:
                break;
            case UNDER:
                break;
            case BESIDE:
                break;
            case LEFTOF:
                break;
            case RIGHTOF:
                break;
        }
        return minObjs;
    }
    
    public Goal getGoal(){
    	return this.goal;
    }
	
	@Override
	public double getStateValue() {
		return actionsToGetHere.size() + this.heuristicValue*heuristicWeight;
	}
	
	@Override
	public boolean hasReachedGoal() {
        return world.isGoalFulFilled(goal);
	}
	
	public void setHeuristicWeight(double heuristicWeight) {
		this.heuristicWeight = heuristicWeight;
	}

    public List<String> getActionsToGetHere() {
        return actionsToGetHere;
    }

    public void setActionsToGetHere(List<String> bestActionsToGetHere) {
        this.actionsToGetHere = bestActionsToGetHere;
    }

	@Override
	public int compareTo(IAStarState o) {
		//Here one can decide whether one wants FIFO or LIFO behavior on queue.
		if(this.getStateValue() - o.getStateValue() >= 0){
			return 1;
		}
		return -1;
	}

    /**
     * @return
     */
	@Override
	public Collection<? extends IAStarState> expand() throws CloneNotSupportedException {
		Collection<IAStarState> l = new LinkedList<IAStarState>();
		if(world.getHolding() != null){
            for(int i = 0; i<world.getStacks().size(); i++){
                World w = world.clone();
                if(w.drop(i) && !visitedWorld.contains(w.getRepresentString())){
                    visitedWorld.add(w.getRepresentString());
                    List<String> newList = new LinkedList<String>(actionsToGetHere);
                    newList.add("drop " + i);
                    WorldState state = new WorldState(w, goal, newList);
                    l.add(state);
                }
			}
		} else {
			for(int i = 0;i<world.getStacks().size();i++){
                World w = world.clone();
                if(w.pick(i) && !visitedWorld.contains(w.getRepresentString())){
                	visitedWorld.add(w.getRepresentString());
                    List<String> newList = new LinkedList<String>(actionsToGetHere);
                    newList.add("pick " + i);
                    WorldState state = new WorldState(w, goal, newList);
                    l.add(state);
                }
			}
		}
		return l;
	}

	

}
