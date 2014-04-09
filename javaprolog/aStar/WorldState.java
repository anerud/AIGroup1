package aStar;

import java.util.*;

import logic.LogicalExpression;
import main.Goal;
import world.RelativeWorldObject;
import world.World;
import world.WorldConstraint;
import world.WorldObject;

public class WorldState implements IAStarState {


    private List<String> bestActionsToGetHere; //Best found so far, considering we are using dijkstra..
	private double heuristicWeight = 1;
	private int distanceToGoHeuristic;
	private World world;
	private Goal goal;
	private Set<WorldObject> objectsToMove;

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
    public WorldState(World world, Goal goal, List<String> actionToGetHere){
		this.world = world;
		this.goal = goal;
		this.distanceToGoHeuristic = 0; //computeHeuristic(goal.getExpression()).size()*2;
		this.bestActionsToGetHere = actionToGetHere;
	}

//    //TODO: delete and do properly
//	private Set<WorldObject> computeHeuristic(LogicalExpression<WorldObject> le) {
//        Set<WorldObject> minObjsToMove = new HashSet<WorldObject>();
//        if(le.getOp().equals(LogicalExpression.Operator.AND) || le.size() <= 1){
//            if(le.getObjs() != null){
//                for(WorldObject wo : le.getObjs()) {
//                    minObjsToMove.addAll(minObjsToMove(wo));
//                }
//            }
//            for(LogicalExpression<WorldObject> le2 : le.getExpressions()){ //om underuttrycken är OR, måste vi kolla alla kombinationer av returvärden och jämföra dessa. Orimligt. Vi måste alltså ha dnf.
//                minObjsToMove.addAll(computeHeuristic(le2));
//            }
//        }
//		return minObjsToMove;
//	}
//
//    //TODO: delete and do properly
//    private Set<WorldObject> minObjsToMove(WorldObject wo) {
//        int sum = 0;
//        Set<WorldObject> minObjs = new HashSet<>(world.objectsAbove(wo));
//        if(!(wo instanceof RelativeWorldObject)){
//            return minObjs;
//        }
//        RelativeWorldObject woRel = (RelativeWorldObject)wo;
//        LogicalExpression<WorldObject> relativeTo = woRel.getRelativeTo();
//        LogicalExpression.Operator op = relativeTo.getOp();
//        if(op.equals(LogicalExpression.Operator.AND) || relativeTo.size() <= 1){
//            switch (woRel.getRelation()) {
//                case ONTOP:
//                    for(WorldObject wo2 : relativeTo.getObjs()){
//                        if(!world.hasRelation(WorldConstraint.Relation.ONTOP, wo, wo2)){
//                            minObjs.addAll(world.objectsAbove(new WorldObject(wo)));
//                            minObjs.addAll(world.objectsAbove(new WorldObject(wo2)));
//                            minObjs.add(new WorldObject(wo));
//                        }
//                    }
//                    break;
//                case INSIDE:
//                    for(WorldObject wo2 : relativeTo.getObjs()){
//                        if(!world.hasRelation(WorldConstraint.Relation.ONTOP, wo, wo2)){
//                            minObjs.addAll(world.objectsAbove(new WorldObject(wo)));
//                            minObjs.addAll(world.objectsAbove(new WorldObject(wo2)));
//                            minObjs.add(new WorldObject(wo));
//                        }
//                    }
//                    break;
//                case ABOVE:
//                    break;
//                case UNDER:
//                    break;
//                case BESIDE:
//                    break;
//                case LEFTOF:
//                    break;
//                case RIGHTOF:
//                    break;
//            }
//        }
//        return minObjs;
//    }
	
	@Override
	public double getStateValue() {
        if(this.distanceToGoHeuristic*heuristicWeight > 0){
            this.getClass();
        }
		return bestActionsToGetHere.size() + this.distanceToGoHeuristic*heuristicWeight;
	}
	
	@Override
	public boolean hasReachedGoal() {
        return world.isGoalFulFilled(goal);
	}
	
	public void setHeuristicWeight(double heuristicWeight) {
		this.heuristicWeight = heuristicWeight;
	}

    public List<String> getBestActionsToGetHere() {
        return bestActionsToGetHere;
    }

    public void setBestActionsToGetHere(List<String> bestActionsToGetHere) {
        this.bestActionsToGetHere = bestActionsToGetHere;
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
	public Collection<? extends IAStarState> expand() {
		Collection<IAStarState> l = new LinkedList<IAStarState>();
		if(world.getHolding() != null){
            for(int i = 0; i<world.getStacks().size(); i++){
                World w = world.clone();
                if(w.drop(i) && !visitedWorld.contains(w.getRepresentString())){
                    visitedWorld.add(w.getRepresentString());
                    List<String> newList = new LinkedList<String>(bestActionsToGetHere);
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
                    List<String> newList = new LinkedList<String>(bestActionsToGetHere);
                    newList.add("pick " + i);
                    WorldState state = new WorldState(w, goal, newList);
                    l.add(state);
                }
			}
		}
		return l;
	}

	

}
