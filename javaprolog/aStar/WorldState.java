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
	private double heuristicWeight = 1.5;
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
        HashMap<Integer, Set<WorldObject>> heuristic = computeHeuristic(goal.getExpression());
        Set<WorldObject> set1 =  heuristic.get(1);
        Set<WorldObject> set2 =  heuristic.get(2);
        set1.removeAll(set2);
		this.heuristicValue = set1.size()*2 + set2.size()*4;
		this.actionsToGetHere = actionToGetHere;
	}

    /**
     * Only supports dnf expressions. If not dnf, an empty set is returned.   //TODO: make it support cnf as well, as some expressions are not practical in dnf. Consider for example the sentence "put all objects beside an object"
     * @param le
     * @return
     */

	private HashMap<Integer, Set<WorldObject>> computeHeuristic(LogicalExpression<WorldObject> le) throws CloneNotSupportedException {
        HashMap<Integer, Set<WorldObject>> minObjs = new HashMap<>();
        Set<WorldObject> moveAtleastOnce = new HashSet<>();
        Set<WorldObject> moveAtleastTwice = new HashSet<>();
        minObjs.put(1, moveAtleastOnce); minObjs.put(2, moveAtleastTwice);
        if(!le.isDnf()){
            return minObjs;
        }
        if(le.getOp().equals(LogicalExpression.Operator.AND) || le.size() <= 1){
            if(le.getObjs() != null){
                for(WorldObject wo : le.getObjs()) {
                    HashMap<Integer, Set<WorldObject>> herps = calculateMinObjsToMove(wo, null);
                    moveAtleastOnce.addAll(herps.get(1)); moveAtleastTwice.addAll(herps.get(2));
                }
            } //Note that the expression is simplified and dnf, so we do not need to check the logicalexpressions..
            return minObjs;
        } else {
            //return the smallest set
            HashMap<Integer, Set<WorldObject>> smallestSet = new HashMap<>();
            boolean first = true;
            if(le.getObjs() != null){
                for(WorldObject wo : le.getObjs()) {
                    HashMap<Integer, Set<WorldObject>> thisSet = calculateMinObjsToMove(wo, null);
                    if(first){
                        smallestSet = thisSet;
                        first = false;
                    } else {
                        Set<WorldObject> set1 =  thisSet.get(1);
                        Set<WorldObject> set2 =  thisSet.get(2);
                        set1.removeAll(set2);
                        Set<WorldObject> smallestSet1 =  smallestSet.get(1);
                        Set<WorldObject> smallestSet2 =  smallestSet.get(2);
                        set1.removeAll(set2);
                        if(set1.size() + set2.size()*2 < smallestSet1.size() + smallestSet2.size()*2){
                            smallestSet = thisSet;
                        }
                    }
                }
            }
            for(LogicalExpression<WorldObject> le1 : le.getExpressions()){
                //This is an AND expression
                HashMap<Integer, Set<WorldObject>> thisSet = computeHeuristic(le1);
                if(first){
                    smallestSet = thisSet;
                    first = false;
                } else {
                    Set<WorldObject> set1 =  thisSet.get(1);
                    Set<WorldObject> set2 =  thisSet.get(2);
                    set1.removeAll(set2);
                    Set<WorldObject> smallestSet1 =  smallestSet.get(1);
                    Set<WorldObject> smallestSet2 =  smallestSet.get(2);
                    set1.removeAll(set2);
                    if(set1.size() + set2.size()*2 < smallestSet1.size() + smallestSet2.size()*2){
                        smallestSet = thisSet;
                    }
                }
            }
            return smallestSet;
        }
	}

    private HashMap<Integer, Set<WorldObject>> calculateMinObjsToMove(WorldObject wo, HashMap<Integer, Set<WorldObject>> minObjsRef) {
        HashMap<Integer, Set<WorldObject>> minObjs = null;
        Set<WorldObject> moveAtleastOnce = null;
        Set<WorldObject> moveAtleastTwice = null;
        if(minObjsRef == null){
            minObjs = new HashMap<>();
            moveAtleastOnce = new HashSet<>();
            moveAtleastTwice = new HashSet<>();
        } else {
            minObjs = minObjsRef;
            moveAtleastOnce = minObjsRef.get(1);
            moveAtleastTwice = minObjsRef.get(2);
        }
        minObjs.put(1, moveAtleastOnce); minObjs.put(2, moveAtleastTwice);

        if(!(wo instanceof RelativeWorldObject)){
            moveAtleastOnce.addAll(world.objectsAbove(wo));
            return minObjs;
        }
        WorldObject woRel = ((RelativeWorldObject) wo).getRelativeTo();

        if(woRel instanceof RelativeWorldObject){
            HashMap<Integer, Set<WorldObject>> mins = calculateMinObjsToMove(woRel, minObjs);
            moveAtleastOnce.addAll(mins.get(1));
            moveAtleastTwice.addAll(mins.get(2));
        }

        switch (((RelativeWorldObject)wo).getRelation()) {
            case ONTOP:
                    if(!world.hasRelation(WorldConstraint.Relation.ONTOP, wo, woRel)){
                        if(!woRel.getForm().equals("floor")){
                            moveAtleastOnce.addAll(world.objectsAbove(new WorldObject(woRel)));
                            if(world.hasRelation(WorldConstraint.Relation.ABOVE, wo, woRel)){
                                moveAtleastTwice.add(new WorldObject(wo));
                            }
                        }
                        moveAtleastOnce.addAll(world.objectsAbove(new WorldObject(wo)));
                        moveAtleastOnce.add(new WorldObject(wo));
                    } else {
                        if(moveAtleastOnce.contains(new WorldObject(wo))){
                            moveAtleastTwice.add(new WorldObject(wo));
                        }
                    }
                break;
            case INSIDE:
                if(!world.hasRelation(WorldConstraint.Relation.INSIDE, wo, woRel)){
                    if(!woRel.getForm().equals("floor")){
                        moveAtleastOnce.addAll(world.objectsAbove(new WorldObject(woRel)));
                        if(world.hasRelation(WorldConstraint.Relation.ABOVE, wo, woRel)){
                            moveAtleastTwice.add(new WorldObject(wo));
                        }
                    }
                    moveAtleastOnce.addAll(world.objectsAbove(new WorldObject(wo)));
                    moveAtleastOnce.add(new WorldObject(wo));
                } else {
                    if(moveAtleastOnce.contains(new WorldObject(wo))){
                        moveAtleastTwice.add(new WorldObject(wo));
                    }
                }
                break;
            case ABOVE:
            	if(!world.hasRelation(WorldConstraint.Relation.ABOVE, wo, woRel)) {
            		moveAtleastOnce.addAll(world.objectsAbove(wo));
            		moveAtleastOnce.add(new WorldObject(wo));
            	} else {
                    if(moveAtleastOnce.contains(new WorldObject(wo))){
                        moveAtleastTwice.add(new WorldObject(wo));
                    }
                }
                break;
            case UNDER:
            	if(!world.hasRelation(WorldConstraint.Relation.UNDER, wo, woRel)) {
            		moveAtleastOnce.addAll(world.objectsAbove(woRel));
            		moveAtleastOnce.add(new WorldObject(woRel));
            	}
                break;
            case BESIDE:
                break;
            case LEFTOF:
                break;
            case RIGHTOF:
                break;
        }

        Set<WorldObject> indirectRelations = ((RelativeWorldObject) wo).inferIndirectRelations();
        for(WorldObject o : indirectRelations){
            HashMap<Integer, Set<WorldObject>> inobjs2move = calculateMinObjsToMove(o, minObjs);
            moveAtleastOnce.addAll(inobjs2move.get(1));
            moveAtleastTwice.addAll(inobjs2move.get(2));
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
