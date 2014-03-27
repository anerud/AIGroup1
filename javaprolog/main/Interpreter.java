package main;
import java.util.*;


import tree.*;
import world.World;
import world.WorldObject;


public class Interpreter {

    public class InterpretationException extends Exception{
        public InterpretationException(String s) {
            super(s);
        }
    }

    private World world;

    public Interpreter(World world) {
        this.world = world;
    }

    /**
     * Extracts the PDDL goals from the parse tree
     * @param tree the parse tree
     * @return a list of goals
     */
    public List<Goal> interpret(NTree tree) throws InterpretationException {
        return tree.getRoot().accept(new ActionVisitor(), world.getWorldObjects());
    }

    private class ActionVisitor implements IActionVisitor<List<Goal>, List<WorldObject>>{

		@Override
		public List<Goal> visit(PutNode n, List<WorldObject> worldObjects) throws InterpretationException {

            if(world.getHolding() == null){
                throw new InterpretationException("You are not holding anything!");
            }

            //identify the object on which the held object should be placed (or the column for the floor)
            List<WorldObject> placedOnObjs = n.getLocationNode().getChildren().getLast().accept(new NodeVisitor(), worldObjects);

            //Create PDDL goals
            List<Goal> goals = new ArrayList<Goal>();
            StringBuilder pddlString = new StringBuilder();
            if(placedOnObjs.size() > 1){
                pddlString.append("(OR ");
                for(WorldObject des : placedOnObjs){
                    pddlString.append("(on " + world.getHolding().getId() +" " + (des.getForm().equals("floor") ? "floor" : des.getId()) + ") ");
                }
                pddlString.deleteCharAt(pddlString.length() - 1);
                pddlString.append(")");
            } else if (placedOnObjs.size() == 1) {
                WorldObject obj = placedOnObjs.get(0);
                pddlString.append("(on " + world.getHolding().getId() +" " + (obj.getForm().equals("floor") ? "floor" : obj.getId()) + ")");
            }
            if(placedOnObjs.size() >= 1){
                Goal goal =  new Goal(pddlString.toString()); //TODO new Goal(some Exp..);
                goals.add(goal);
            }

			return goals;
		}

		@Override
        public List<Goal> visit(TakeNode n, List<WorldObject> worldObjects) throws InterpretationException {
        	//is the (handempty) precondition fulfilled?
            if(world.getHolding() != null){
                throw new InterpretationException("You need to put down what you are holding first.");
            }

            //identify the objects
            List<WorldObject> filteredObjects = n.getEntityNode().accept(new NodeVisitor(), worldObjects);

            //Create PDDL goals
            //We can only hold one object, but if many objects are returned, the planner can choose the closest one.
            List<Goal> goals = new ArrayList<Goal>();
            StringBuilder pddlString = new StringBuilder();
            if(filteredObjects.size() > 1){
                pddlString.append("(OR ");
                for(WorldObject des : filteredObjects){
                    //The PDDL goals should be of the type "(HOLDING OBJECT1)", that is, the goal describes the final state of the world
                    pddlString.append("(holding " + des.getId() + ") ");
                }
                pddlString.deleteCharAt(pddlString.length() - 1);
                pddlString.append(")");
            } else if (filteredObjects.size() == 1) {
                pddlString.append("(holding " + filteredObjects.get(0).getId() + ") ");
            }
            if(filteredObjects.size() >= 1){
                Goal goal =  new Goal(pddlString.toString()); //TODO new Goal(some Exp..);
                goals.add(goal);
            }
            return goals;
        }

		@Override
		public List<Goal> visit(MoveNode n, List<WorldObject> a) {
            //TODO
            return null;
		}
    	
    }

    private class NodeVisitor implements INodeVisitor<List<WorldObject>, List<WorldObject>>{

        @Override
        public List<WorldObject> visit(BasicEntityNode n, List<WorldObject> worldObjects) throws InterpretationException {
        	List<WorldObject> filteredObjects = n.getObjectNode().accept(this, worldObjects);
        	if(n.getQuantifierNode().getData().equals("the") && filteredObjects.size() > 1) {
        		throw new InterpretationException("Several objects match the description '" + n.getObjectNode().getChildren().toString() +  "'. Which one do you mean?");//TODO: Proper error message
        	}
			return filteredObjects;
        }

        @Override
        public List<WorldObject> visit(RelativeEntityNode n, List<WorldObject> worldObjects) throws InterpretationException {
        	
        	List<WorldObject> filteredObjects = n.getObjectNode().accept(this, worldObjects); 
            List<WorldObject> relativeFilteredObjects = n.getLocationNode().accept(this, filteredObjects);
            
            if(n.getQuantifierNode().getData().equals("the") && relativeFilteredObjects.size() > 1) {
                throw new InterpretationException("Several objects match the description '" + n.getObjectNode().getChildren().toString() +  "'. Which one do you mean?");//TODO: Proper error message
            }// Else quantifier any, so everything is ok..

            return relativeFilteredObjects;
        }

        @Override
        public List<WorldObject> visit(RelativeNode n, List<WorldObject> worldObjects) throws InterpretationException {
        	
        	List<WorldObject> relativeObjects = n.getEntityNode().accept(this, world.getWorldObjects());
        	
            //retain objects for which toBeFiltered is inside or ontop theRelativeObjects
            filterByRelation(worldObjects, relativeObjects, n.getRelationNode().getData());
            return worldObjects;
        }

        @Override
        public List<WorldObject> visit(FloorNode n, List<WorldObject> worldObjects) {
        	worldObjects.clear();
        	worldObjects.add(new WorldObject("floor", "floor", "floor"));
            return worldObjects;
        }

        @Override
        public List<WorldObject> visit(QuantifierNode n, List<WorldObject> worldObjects) {
            return null;
        }

        @Override
        public List<WorldObject> visit(ObjectNode n, List<WorldObject> worldObjects) {
            filterByMatch(worldObjects, new WorldObject(n.getFormNode().getData(),
            		n.getSizeNode().getData(), n.getColorNode().getData()));
            return worldObjects;
        }

        @Override
        public List<WorldObject> visit(AttributeNode n, List<WorldObject> worldObjects) {
            return null;
        }

        @Override
        public List<WorldObject> visit(RelationNode n, List<WorldObject> worldObjects) {
            return null;
        }
    }

    private void filterByMatch(List<WorldObject> toBeFiltered, WorldObject match) {
        Set<WorldObject> toBeRetained = new HashSet<WorldObject>();
        for(WorldObject wo : toBeFiltered){
            if(wo.matchesPattern(match)){
                toBeRetained.add(wo);
            }
        }
        toBeFiltered.retainAll(toBeRetained);
    }

    /**
     * Retains the objects in toBeFiltered which are "relation" to ANY of theRelativeObjects
     * @param toBeFiltered
     * @param theRelativeObjects
     * @param relation
     */
    private void filterByRelation(List<WorldObject> toBeFiltered, List<WorldObject> theRelativeObjects, String relation) {
        List<WorldObject> toBeRetained = new LinkedList<>();
        for(WorldObject wo : toBeFiltered){
            for(WorldObject worel : theRelativeObjects){
                if(world.hasRelation(relation, wo, worel)){
                    toBeRetained.add(wo);
                }
            }
        }
        toBeFiltered.retainAll(toBeRetained);
    }
}
