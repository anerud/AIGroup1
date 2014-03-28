package main;
import java.util.*;


import logic.LogicalObject;
import logic.Quantifier;
import tree.*;
import world.*;


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
     *
     * @param trees the parse tree
     * @return a list of goals
     */
    public Set<Goal> interpret(List<NTree> trees) throws InterpretationException {
        Set<Goal> goalsPerTree = new HashSet<>(trees.size());
        for(NTree tree : trees){
            Goal g = tree.getRoot().accept(new ActionVisitor(), world.getWorldObjects());
            if(g != null){
                goalsPerTree.add(g);//TODO: it now skips some trees if null..
            }
        }
//        for(Goal g : goalsPerTree){
//            if(!goalsPerTree.get(0).equals(g)){
//                throw new InterpretationException("Ambiguous sentence. Please be more precise.");
//            }
//        }                   //todo
        return goalsPerTree;
    }

    private class ActionVisitor implements IActionVisitor<Goal, Set<WorldObject>>{

		@Override
		public Goal visit(PutNode n, Set<WorldObject> worldObjects) throws InterpretationException {

            if(world.getHolding() == null){
                throw new InterpretationException("You are not holding anything!");
            }

            //identify the object to which the held object should be placed relative (or the column for the floor)
            Set<LogicalObject<WorldObject>> placedRelativeObjs= n.getLocationNode().getChildren().getLast().accept(new NodeVisitor(), worldObjects, null);
            String relation = n.getLocationNode().getChildren().getFirst().getData();

            //TODO now create one PDDL goal for each placement which fulfils the relation

            //Create PDDL goals
            Goal goal = null;
            StringBuilder pddlString = new StringBuilder();
            if(placedRelativeObjs.size() > 1){
                pddlString.append("(OR ");
                for(LogicalObject<WorldObject> des : placedRelativeObjs){
                    pddlString.append("(on " + world.getHolding().getId() +" " + (des.getObj().getForm().equals("floor") ? "floor" : des.getObj().getId()) + ") ");
                }
                pddlString.deleteCharAt(pddlString.length() - 1);
                pddlString.append(")");
            } else if (placedRelativeObjs.size() == 1) {
                LogicalObject<WorldObject> obj = placedRelativeObjs.iterator().next();
                pddlString.append("(on " + world.getHolding().getId() +" " + (obj.getObj().getForm().equals("floor") ? "floor" : obj.getObj().getId()) + ")");
            }
            if(placedRelativeObjs.size() >= 1){
                goal =  new Goal(pddlString.toString()); //TODO new Goal(some Exp..);
            }
			return goal;
		}

		@Override
        public Goal visit(TakeNode n, Set<WorldObject> worldObjects) throws InterpretationException {
        	//is the (handempty) precondition fulfilled?
            if(world.getHolding() != null){
                throw new InterpretationException("You need to put down what you are holding first."); //TODO: the planner should perhaps actually just put the object down by itself first..
            }

            //identify the objects
            Set<LogicalObject<WorldObject>> filteredObjects = n.getEntityNode().accept(new NodeVisitor(), worldObjects, null);

            //Create PDDL goals
            //We can only hold one object, but if many objects are returned, the planner can choose the closest one.
            Goal goal = null;
            StringBuilder pddlString = new StringBuilder();
            if(filteredObjects.size() > 1){
                pddlString.append("(OR ");
                for(LogicalObject<WorldObject> des : filteredObjects){
                    //The PDDL goals should be of the type "(HOLDING OBJECT1)", that is, the goal describes the final state of the world
                    pddlString.append("(holding " + des.getObj().getId() + ") ");
                }
                pddlString.deleteCharAt(pddlString.length() - 1);
                pddlString.append(")");
            } else if (filteredObjects.size() == 1) {
                pddlString.append("(holding " + filteredObjects.iterator().next().getObj().getId() + ") ");
            }
            if(filteredObjects.size() >= 1){
                goal =  new Goal(pddlString.toString()); //TODO new Goal(some Exp..);
            }
            return goal;
        }

		@Override
		public Goal visit(MoveNode n, Set<WorldObject> worldObjects) throws InterpretationException {

            Set<LogicalObject<WorldObject>> firstObjects = n.getEntityNode().accept(new NodeVisitor(), worldObjects, null);
            Set<LogicalObject<WorldObject>> placedOnObjs = n.getLocationNode().getChildren().getLast().accept(new NodeVisitor(), worldObjects, null);

            //Create PDDL goal
            Goal goal = null;             //TODO
//            StringBuilder pddlString = new StringBuilder();
//            if(firstObjects.size() > 1 || placedOnObjs.size() > 1){
//                pddlString.append("(OR ");
//                for(WorldObject des1 : firstObjects){
//                    for(WorldObject des2 : placedOnObjs){
//                        //The PDDL goals should be of the type "(HOLDING OBJECT1)", that is, the goal describes the final state of the world
//                        pddlString.append("(on " + (des1.getForm().equals("floor") ? "floor" : des1.getId()) +" " + (des2.getForm().equals("floor") ? "floor" : des2.getId()) + ") ");
//                    }
//                }
//                pddlString.deleteCharAt(pddlString.length() - 1);
//                pddlString.append(")");
//            } else if (firstObjects.size() == 1 && placedOnObjs.size() == 1) {
//                WorldObject first = firstObjects.iterator().next();
//                WorldObject second = placedOnObjs.iterator().next();
//                pddlString.append("(on " + (first.getForm().equals("floor") ? "floor" : first.getId()) +" " + (second.getForm().equals("floor") ? "floor" : second.getId()) + ") ");
//            }
//            if(firstObjects.size() >= 1 && placedOnObjs.size() >= 1){
//                goal =  new Goal(pddlString.toString()); //TODO new Goal(some Exp..);
//            }
            return goal;
		}
    	
    }

    private class NodeVisitor implements INodeVisitor<Set<LogicalObject<WorldObject>>,Set<WorldObject>,Quantifier> {

        @Override
        public Set<LogicalObject<WorldObject>> visit(BasicEntityNode n, Set<WorldObject> worldObjects, Quantifier quantifier) throws InterpretationException {
			return n.getObjectNode().accept(this, worldObjects, n.getQuantifierNode().getQuantifier());
        }

        @Override
        public Set<LogicalObject<WorldObject>> visit(RelativeEntityNode n, Set<WorldObject> worldObjects, Quantifier dummy) throws InterpretationException {
            //TODO: use quantifier dummy to identify if the operation is a take or a move. If a take, filtering is ok even when q = any. But if a move, it should be done as below.
            Quantifier q = n.getQuantifierNode().getQuantifier();
            Set<LogicalObject<WorldObject>> matchesArg1 = n.getObjectNode().accept(this, worldObjects, q);
            Set<LogicalObject<WorldObject>> matchesRelation = n.getLocationNode().accept(this, null, q); //Null because the argument is not relevant...
            if(q.equals(Quantifier.THE)){
                matchesArg1.retainAll(matchesRelation);
                if(matchesArg1.size() > 1){
                    throw new InterpretationException("Several objects match the description '" + n.getObjectNode().getChildren().toString() +  "' with relation '" + n.getLocationNode().getRelationNode().getRelation() + "' to '" + n.getLocationNode().getEntityNode().getChildren().toString() + "'. Which one do you mean?");
                } else if(matchesArg1.isEmpty()){
                    throw new InterpretationException("There are no objects which match the description '" + n.getObjectNode().getChildren().toString() +  "' with relation '" + n.getLocationNode().getRelationNode().getRelation() + "' to '" + n.getLocationNode().getEntityNode().getChildren().toString());
                }
                matchesArg1.iterator().next().setOp(LogicalObject.Operator.NONE);
                return matchesArg1;
            } else if(q.equals(Quantifier.ANY)){
                //don't filter since the planner may arrange this situation to exist. e.g. for "put the white ball in (a box on the floor)", the planner might first put the box on the floor
                Set<LogicalObject<WorldObject>> relobjs = new HashSet<LogicalObject<WorldObject>>();
                for(LogicalObject<WorldObject> lo : matchesArg1){
                    for(LogicalObject<WorldObject> lo1 : matchesRelation){
                        relobjs.add(new LogicalObject<WorldObject>(new RelativeWorldObject(lo.getObj(), lo1.getObj(), n.getLocationNode().getRelationNode().getRelation()), LogicalObject.Operator.OR));
                    }
                }
                return relobjs;
            }
            //For all, it is not up to the planner to rearrange objects to create a situation (unlike any). We can therefore simply filter the objects.
            matchesArg1.retainAll(matchesRelation);
            if(matchesArg1.isEmpty()){
                throw new InterpretationException("There are no objects which match the description '" + n.getObjectNode().getChildren().toString() +  "' with relation '" + n.getLocationNode().getRelationNode().getRelation() + "' to '" + n.getLocationNode().getEntityNode().getChildren().toString());
            }
            for(LogicalObject<WorldObject> lo : matchesArg1){
                lo.setOp(LogicalObject.Operator.AND);
            }
            return matchesArg1;
        }

        @Override
        public Set<LogicalObject<WorldObject>> visit(RelativeNode n, Set<WorldObject> dummy, Quantifier quantifier) throws InterpretationException {
            Set<LogicalObject<WorldObject>> relativeTo = n.getEntityNode().accept(this, world.getWorldObjects(), Quantifier.ANY);
            if(relativeTo.isEmpty()){
                throw new InterpretationException("There are no objects which match the description '"  + n.getEntityNode().getChildren().toString() + ".");
            }
            if(quantifier.equals(Quantifier.THE)){
                Set<LogicalObject<WorldObject>> toBeFiltered = new HashSet<LogicalObject<WorldObject>>(LogicalObject.toLogicalObjects(world.getWorldObjects(), Quantifier.ANY));
                //retain objects for which toBeFiltered is relation to theRelativeObjects
                filterByRelation(toBeFiltered, relativeTo, n.getRelationNode().getRelation());
                return toBeFiltered;
            } else if(quantifier.equals(Quantifier.ANY)){
                return relativeTo;
            }
            //Should not be reachable
            throw new InterpretationException("Something went wrong during the interpretation.");
        }

        @Override
        public Set<LogicalObject<WorldObject>> visit(FloorNode n, Set<WorldObject> worldObjects, Quantifier dummy) {
            Set<WorldObject> toBeFiltered = new HashSet<>();
        	toBeFiltered.add(new WorldObject("floor", "floor", "floor"));
            return LogicalObject.toLogicalObjects(toBeFiltered, Quantifier.THE);//"The" floor is the only thing that makes sense...
        }

        @Override
        public Set<LogicalObject<WorldObject>> visit(QuantifierNode n, Set<WorldObject> worldObjects, Quantifier quantifier) throws InterpretationException {
            //Never used
            throw new InterpretationException("Something went wrong during the interpretation.");
        }

        @Override
        public Set<LogicalObject<WorldObject>> visit(ObjectNode n, Set<WorldObject> worldObjects, Quantifier quantifier) throws InterpretationException {
            Set<WorldObject> toBeFiltered = new HashSet<>(worldObjects);
            filterByMatch(toBeFiltered, new WorldObject(n.getFormNode().getData(),
            		n.getSizeNode().getData(), n.getColorNode().getData()));
            Set<LogicalObject<WorldObject>> logObjs = LogicalObject.toLogicalObjects(toBeFiltered, quantifier);
            if(quantifier.equals(Quantifier.THE) && logObjs.size() > 1){
                throw new InterpretationException("Several objects match the description '" + n.getChildren().toString() +  "'. Which one do you mean?");//TODO: Proper error message
            }
            if(logObjs.isEmpty()) {
                throw new InterpretationException("There are no objects which match the description '" + n.getChildren().toString() + ".");
            }
            return logObjs;
        }

        @Override
        public Set<LogicalObject<WorldObject>> visit(AttributeNode n, Set<WorldObject> worldObjects, Quantifier quantifier) throws InterpretationException {
            //Never used
            throw new InterpretationException("Something went wrong during the interpretation.");
        }

        @Override
        public Set<LogicalObject<WorldObject>> visit(RelationNode n, Set<WorldObject> worldObjects, Quantifier quantifier) throws InterpretationException {
            //Never used
            throw new InterpretationException("Something went wrong during the interpretation.");
        }
    }

    private void filterByMatch(Set<WorldObject> toBeFiltered, WorldObject match) {
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
     * The method assumes all objects in toBeFiltered have the same logical operator, and the same applies for theRelativeObjects
     * @param toBeFiltered
     * @param theRelativeObjects
     * @param relation
     */
    private void filterByRelation(Set<LogicalObject<WorldObject>> toBeFiltered, Set<LogicalObject<WorldObject>> theRelativeObjects, WorldConstraint.Relation relation) {
        Set<LogicalObject<WorldObject>> toBeRetained = new HashSet<>();
        LogicalObject.Operator op1 = toBeFiltered.iterator().next().getOp();
        LogicalObject.Operator op2 = theRelativeObjects.iterator().next().getOp();
        if((op1.equals(LogicalObject.Operator.OR) || op1.equals(LogicalObject.Operator.NONE) && (op2.equals(LogicalObject.Operator.OR) || op2.equals(LogicalObject.Operator.NONE)))){
            for(LogicalObject<WorldObject> wo : toBeFiltered){
                for(LogicalObject<WorldObject> worel : theRelativeObjects){
                    if(world.hasRelation(relation, wo.getObj(), worel.getObj())){
                        wo.setOp(LogicalObject.Operator.NONE);
                        toBeRetained.add(wo);
                    }
                }
            }
        } else if(op2.equals(LogicalObject.Operator.AND)){  //TODO: do all logical combinations necessary
            for(LogicalObject<WorldObject> wo : toBeFiltered){
                for(LogicalObject<WorldObject> worel : theRelativeObjects){
                    boolean retain = true;
                    if(!world.hasRelation(relation, wo.getObj(), worel.getObj())){
                        retain = false;
                    }
                    if(retain){
                        wo.setOp(LogicalObject.Operator.NONE);
                        toBeRetained.add(wo);
                    }
                }
            }
        }
        toBeFiltered.retainAll(toBeRetained);
    }
}
