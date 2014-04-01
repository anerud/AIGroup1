package main;
import java.util.*;


import logic.LogicalExpression;
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

        /**
         * Recursively builds a PDDL expression from a LogicalExpression
         * @param expression
         * @return an empty string if there is nothing contained in the LogicalExpression
         */
        private String toPDDLString(LogicalExpression<WorldObject> expression, String singlePredicate) {
            //TODO: first remove redundant logic!
            StringBuilder pddlString = new StringBuilder();
            if(expression.size() > 1){
                LogicalExpression.Operator op = expression.getOp();
                pddlString.append("(" + op.toString() + " ");
                if(expression.getObjs() != null){
                    for(WorldObject obj : expression.getObjs()){
                        if(obj instanceof RelativeWorldObject && ((RelativeWorldObject)obj).getRelativeTo() != null){
                            RelativeWorldObject relObj = (RelativeWorldObject)obj;
                            pddlString.append("(" + relObj.getRelation().toString() + " " + relObj.getId() +" " + toPDDLString(relObj.getRelativeTo(), singlePredicate) + ") "); //Recursively build string
                        } else {
                            if(singlePredicate.equals("")){
                                pddlString.append(obj.getId());
                            } else {
                                pddlString.append("(" + singlePredicate + obj.getId() + ") ");}

                        }
                    }
                }
                for(LogicalExpression exp : expression.getExpressions()){
                    pddlString.append(toPDDLString(exp, singlePredicate) + " "); //Recursively build string
                }
                //Remove last extra space
                pddlString.deleteCharAt(pddlString.length() - 1);
                pddlString.append(")");
            } else if (expression.size() == 1) {
                //Assume the RelativeWorldobject in question is at the top level of the expression
                WorldObject wo = expression.getObjs().iterator().next();
                if(wo instanceof RelativeWorldObject && ((RelativeWorldObject)wo).getRelativeTo() != null){
                    RelativeWorldObject relObj = (RelativeWorldObject)wo;
                    pddlString.append("(" + relObj.getRelation().toString() + " " + relObj.getId() +" " + toPDDLString(relObj.getRelativeTo(), singlePredicate) + ")");  //Recursively build string
                } else {
                    if(singlePredicate.equals("")){
                        pddlString.append(wo.getId());
                    } else {
                        pddlString.append("(" + singlePredicate + wo.getId() + ")");
                    }
                }
            }
            return pddlString.toString();
        }

        /**
         * The put operation only operates on objects of the form "it". That is, "it" refers to the object currently being held.
         * Note that most usages of put as input to main.Shrdlite translates to move operations here and not put operations.
         * @param n
         * @param worldObjects
         * @return
         * @throws InterpretationException
         */
		@Override
		public Goal visit(PutNode n, Set<WorldObject> worldObjects) throws InterpretationException {
            if(world.getHolding() == null){
                throw new InterpretationException("You are not holding anything!");
            }

            //identify the object to which the held object should be placed relative (or the column for the floor)
            Set<WorldObject> worldObjs = new HashSet<WorldObject>(worldObjects);
            worldObjs.remove(world.getHolding());
            LogicalExpression<WorldObject> placedRelativeObjs = n.getLocationNode().accept(new NodeVisitor(), worldObjs, null);

            Set<WorldObject> objsDummy = new HashSet<WorldObject>();
            objsDummy.add(world.getHolding());
            LogicalExpression<WorldObject> attached = world.attachWorldObjectsToRelation(objsDummy, placedRelativeObjs, LogicalExpression.Operator.OR);

            //NOTE: It's not possible to create one simple "ontop" PDDL goal for each possible placement which fulfils a relation unless the quantifier "THE" was used. //TODO: when "THE" is used, determine the exact possible relations.. or perhaps leave this to the planner
            // That is, in some cases it's up to the planner to make a situation possible.
            //Create PDDL goals
            String pddlString = toPDDLString(attached, "");
            Goal goal = null;
            if(!pddlString.equals("")){
                goal = new Goal(pddlString);
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
            LogicalExpression<WorldObject> filteredObjects = n.getEntityNode().accept(new NodeVisitor(), worldObjects, null);

            //Filter the objects since the take operation requires the relations to already exist in the world.
            LogicalExpression<WorldObject> filteredObjectsNew = new LogicalExpression<>(world.filterByExistsInWorld(filteredObjects.topObjs()), LogicalExpression.Operator.OR);

            //Create PDDL goals
            //We can only hold one object, but if many objects are returned, the planner can choose the closest one.

            //Clear the relations of the objects
            for(WorldObject ob : filteredObjectsNew.getObjs()){
                if(ob instanceof RelativeWorldObject){
                    ((RelativeWorldObject) ob).setRelativeTo(null);
                    ((RelativeWorldObject) ob).setRelation(null);
                }
            }

            String pddlString = toPDDLString(filteredObjectsNew, "holding ");
            Goal goal = null;
            if(!pddlString.equals("")){
                goal = new Goal(pddlString);
            }
            return goal;
        }

        /**
         * TODO: should move retain relationships? E.g.: "move (a ball on a box) on a table". Should the ball still be on a box afterwards?
         * @param n
         * @param worldObjects
         * @return
         * @throws InterpretationException
         */
		@Override
		public Goal visit(MoveNode n, Set<WorldObject> worldObjects) throws InterpretationException {

            LogicalExpression<WorldObject> firstObjects = n.getEntityNode().accept(new NodeVisitor(), worldObjects, null);
            //Filter the objects since the move operation requires the first parameter to already exist in the world.
            LogicalExpression<WorldObject> filteredObjectsNew = new LogicalExpression<>(world.filterByExistsInWorld(firstObjects.topObjs()), firstObjects.getOp());
            LogicalExpression<WorldObject> placedRelativeObjs = n.getLocationNode().accept(new NodeVisitor(), null, null);
            //TODO perhaps make sure objects relative to themselves are avoided

            LogicalExpression<WorldObject> attached = world.attachWorldObjectsToRelation(filteredObjectsNew, placedRelativeObjs); //TODO: should depend on the operators in filteredObjectsNew

            LogicalExpression<WorldObject> simplified = attached.simplifyExpression();

            //Create PDDL goal
            String pddlString = toPDDLString(simplified, "");
            Goal goal = null;
            if(!pddlString.equals("")){
                goal = new Goal(pddlString);
            }

            return goal;
		}
    	
    }

    private class NodeVisitor implements INodeVisitor<LogicalExpression<WorldObject>,Set<WorldObject>,Quantifier> {

        @Override
        public LogicalExpression<WorldObject> visit(BasicEntityNode n, Set<WorldObject> worldObjects, Quantifier quantifier) throws InterpretationException {
			return n.getObjectNode().accept(this, worldObjects, n.getQuantifierNode().getQuantifier());
        }

        @Override
        public LogicalExpression<WorldObject> visit(RelativeEntityNode n, Set<WorldObject> worldObjects, Quantifier dummy) throws InterpretationException {
            Quantifier q = n.getQuantifierNode().getQuantifier();
            LogicalExpression<WorldObject> matchesArg1 = n.getObjectNode().accept(this, worldObjects, q);
            LogicalExpression<WorldObject> matchesLocation = n.getLocationNode().accept(this, null, q); //Null because the argument is not relevant...
//            WorldConstraint.Relation relation = n.getLocationNode().getRelationNode().getRelation();
            if(q.equals(Quantifier.THE)){
                Set<WorldObject> wobjs = world.filterByRelation(matchesArg1.getObjs(), matchesLocation);
                if(wobjs.size() > 1){
                    if(!Shrdlite.debug){
                        throw new InterpretationException("Several objects match the description '" + n.getObjectNode().getChildren().toString() +  "' with relation '" + n.getLocationNode().getRelationNode().getRelation() + "' to '" + n.getLocationNode().getEntityNode().getChildren().toString() + "'. Which one do you mean?");
                    }
                } else if(wobjs.isEmpty()){
                    if(!Shrdlite.debug){
                        throw new InterpretationException("There are no objects which match the description '" + n.getObjectNode().getChildren().toString() +  "' with relation '" + n.getLocationNode().getRelationNode().getRelation() + "' to '" + n.getLocationNode().getEntityNode().getChildren().toString());
                    }
                }
                LogicalExpression<WorldObject> le = new LogicalExpression<WorldObject>(wobjs, LogicalExpression.Operator.NONE);
                return le;
            } else if(q.equals(Quantifier.ANY)){
                //don't filter since the planner may arrange this situation to exist. e.g. for "put the white ball in (a box on the floor)", the planner might first put the box on the floor
                return world.attachWorldObjectsToRelation(matchesArg1, matchesLocation);
            }
            //For "ALL", it is not up to the planner to rearrange objects to create a situation (unlike any). We can therefore simply filter the objects.
            Set<WorldObject> wobjs = world.filterByRelation(matchesArg1.getObjs(), matchesLocation);
            if(wobjs.isEmpty()){
                throw new InterpretationException("There are no objects which match the description '" + n.getObjectNode().getChildren().toString() +  "' with relation '" + n.getLocationNode().getRelationNode().getRelation() + "' to '" + n.getLocationNode().getEntityNode().getChildren().toString());
            }
            LogicalExpression<WorldObject> le = new LogicalExpression<WorldObject>(wobjs, LogicalExpression.Operator.AND);
            return le;
        }

        /**
         *
         * @param n
         * @param worldObjects if null, all objects in the world are used
         * @param dummy2
         * @return
         * @throws InterpretationException
         */
        @Override
        public LogicalExpression<WorldObject> visit(RelativeNode n, Set<WorldObject> worldObjects, Quantifier dummy2) throws InterpretationException {
            LogicalExpression<WorldObject> relativeTo = n.getEntityNode().accept(this, worldObjects == null ? world.getWorldObjects() : worldObjects, Quantifier.ANY);

            //Convert tops to RelativeWorldObjects
            Set<WorldObject> objsNew = new HashSet<WorldObject>();
            Set<LogicalExpression> expNew = new HashSet<LogicalExpression>();
            for(LogicalExpression<WorldObject> le : relativeTo.getExpressions()){
                Set<WorldObject> wRelObjs = new HashSet<WorldObject>();
                for(WorldObject wo : le.getObjs()){
                    Set<WorldObject> s = new HashSet<WorldObject>();
                    s.add(wo);
                    wRelObjs.add(new RelativeWorldObject(new LogicalExpression<WorldObject>(s, LogicalExpression.Operator.NONE), n.getRelationNode().getRelation()));
                }
                expNew.add(new LogicalExpression(wRelObjs, le.getExpressions(),le.getOp()));
            }
            if(relativeTo.getObjs() != null){
                for(WorldObject wo : relativeTo.getObjs()){
                    Set<WorldObject> s = new HashSet<WorldObject>();
                    s.add(wo);
                    objsNew.add(new RelativeWorldObject(new LogicalExpression<WorldObject>(s, LogicalExpression.Operator.NONE), n.getRelationNode().getRelation()));
                }
            }
            LogicalExpression<WorldObject> relativeToNew = new LogicalExpression<WorldObject>(objsNew, expNew, relativeTo.getOp());//new HashSet<LogicalExpression<WorldObject>>();
            if(relativeTo.isEmpty()){
                throw new InterpretationException("There are no objects which match the description '"  + n.getEntityNode().getChildren().toString() + ".");
            }

            return relativeToNew;
        }

        @Override
        public LogicalExpression<WorldObject> visit(FloorNode n, Set<WorldObject> worldObjects, Quantifier dummy) {
            Set<WorldObject> toBeFiltered = new HashSet<>();
        	toBeFiltered.add(new WorldObject("floor", "floor", "floor", "floor"));
            return new LogicalExpression<WorldObject>(toBeFiltered, LogicalExpression.Operator.NONE); //"THE" floor is the only quantifier that makes sense...
        }

        @Override
        public LogicalExpression<WorldObject> visit(QuantifierNode n, Set<WorldObject> worldObjects, Quantifier quantifier) throws InterpretationException {
            //Never used
            throw new InterpretationException("Something went wrong during the interpretation.");
        }

        @Override
        public LogicalExpression<WorldObject> visit(ObjectNode n, Set<WorldObject> worldObjects, Quantifier quantifier) throws InterpretationException {
            Set<WorldObject> toBeFiltered = new HashSet<>(worldObjects);
            world.filterByMatch(toBeFiltered, new WorldObject(n.getFormNode().getData(),
                    n.getSizeNode().getData(), n.getColorNode().getData(), null));
            LogicalExpression.Operator op = LogicalExpression.Operator.NONE;
            switch(quantifier){
                case ALL:
                    op = LogicalExpression.Operator.AND;
                    break;
                case ANY:
                    op = LogicalExpression.Operator.OR;
                    break;
                case THE:
                    op = LogicalExpression.Operator.NONE;
                    break;
            }
            LogicalExpression<WorldObject> logObjs = new LogicalExpression<>(toBeFiltered, op);//LogicalExpression.toLogicalObjects(toBeFiltered, quantifier);
//            if(quantifier.equals(Quantifier.THE) && logObjs.size() > 1){
//                throw new InterpretationException("Several objects match the description '" + n.getChildren().toString() +  "'. Which one do you mean?");//TODO: Proper error message
//            } //This is actually OK. Consider the sentence "["take", "the", "box", "under", "an", "object", "on", "a", "green", "object"]". "The box" can be several boxes..
            if(logObjs.isEmpty()) {
                throw new InterpretationException("There are no objects which match the description '" + n.getChildren().toString() + ".");
            }
            return logObjs;
        }

        @Override
        public LogicalExpression<WorldObject> visit(AttributeNode n, Set<WorldObject> worldObjects, Quantifier quantifier) throws InterpretationException {
            //Never used
            throw new InterpretationException("Something went wrong during the interpretation.");
        }

        @Override
        public LogicalExpression<WorldObject> visit(RelationNode n, Set<WorldObject> worldObjects, Quantifier quantifier) throws InterpretationException {
            //Never used
            throw new InterpretationException("Something went wrong during the interpretation.");
        }
    }
}
