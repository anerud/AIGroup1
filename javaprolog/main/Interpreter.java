package main;

import logic.LogicalExpression;
import logic.Quantifier;
import main.Goal.Action;
import tree.*;
import world.RelativeWorldObject;
import world.World;
import world.WorldConstraint.Relation;
import world.WorldObject;

import gnu.prolog.io.Operator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.sun.corba.se.impl.ior.NewObjectKeyTemplateBase;
import com.sun.corba.se.impl.naming.cosnaming.InterOperableNamingImpl;


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
	public Set<Goal> interpret(List<NTree> trees) throws InterpretationException, CloneNotSupportedException {
		Set<Goal> goalsPerTree = new HashSet<>(trees.size());
		Set<InterpretationException> exceptions = new HashSet<InterpretationException>();
		for(NTree tree : trees){
			Goal g = null;
			try{
				g = tree.getRoot().accept(new ActionVisitor(), world.getWorldObjects());
				if(g != null){
					world.removeImpossibleLogic(g.getExpression());
					g.getExpression().simplifyExpression();
				}
			} catch(InterpretationException e){
				exceptions.add(e);
			}
			if(g != null){
				goalsPerTree.add(g);
			}
		}
		if(goalsPerTree.isEmpty()){
			if(!exceptions.isEmpty()){
				//Take one of the exceptions and throw it.
				throw exceptions.iterator().next();
			}
		}

		return goalsPerTree;
	}

	private class ActionVisitor implements IActionVisitor<Goal, Set<WorldObject>>{

		/**
		 * The put operation only operates on objects of the form "it". That is, "it" refers to the object currently being held.
		 * Note that most usages of put as input to main.Shrdlite translates to move operations here and not put operations.
		 * @param n
		 * @param worldObjects
		 * @return
		 * @throws InterpretationException
		 */
		@Override
		public Goal visit(PutNode n, Set<WorldObject> worldObjects) throws InterpretationException, CloneNotSupportedException {
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
			Goal goal = null;
			if(!(attached.size() == 0)){
				goal = new Goal(attached, Goal.Action.PUT);
			}
			return goal;
		}

		@Override
		public Goal visit(TakeNode n, Set<WorldObject> worldObjects) throws InterpretationException, CloneNotSupportedException {
			//is the (handempty) precondition fulfilled?
			if(world.getHolding() != null){
				throw new InterpretationException("You need to put down what you are holding first."); //TODO: the planner should perhaps actually just put the object down by itself first..
			}

			//identify the objects
			LogicalExpression<WorldObject> filteredObjects = n.getEntityNode().accept(new NodeVisitor(), worldObjects, null);

			//Filter the objects since the take operation requires the relations to already exist in the world.
			LogicalExpression<WorldObject> filteredObjectsNew = new LogicalExpression<>(world.filterByExistsInWorld(filteredObjects), LogicalExpression.Operator.OR);
			//            world.filterByRelation()
			//Create PDDL goals
			//We can only hold one object, but if many objects are returned, the planner can choose the closest one.

			//Clear the relations of the objects
			for(WorldObject ob : filteredObjectsNew.getObjs()){
				if(ob instanceof RelativeWorldObject){
					((RelativeWorldObject) ob).setRelativeTo(null);
					((RelativeWorldObject) ob).setRelation(null);
				}
			}

			Goal goal = null;
			if(!(filteredObjectsNew.size() == 0)){
				goal = new Goal(filteredObjectsNew, Goal.Action.TAKE);
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
		public Goal visit(MoveNode n, Set<WorldObject> worldObjects) throws InterpretationException, CloneNotSupportedException {

			LogicalExpression<WorldObject> firstObjects = n.getEntityNode().accept(new NodeVisitor(), worldObjects, null);
			//Filter the objects since the move operation requires the first parameter to already exist in the world.
			LogicalExpression<WorldObject> filteredObjectsNew = new LogicalExpression<>(world.filterByExistsInWorld(firstObjects), firstObjects.getOp());
			LogicalExpression<WorldObject> placedRelativeObjs = n.getLocationNode().accept(new NodeVisitor(), null, null);

			LogicalExpression<WorldObject> attached = world.attachWorldObjectsToRelation(filteredObjectsNew, placedRelativeObjs);

			LogicalExpression<WorldObject> simplified = attached.simplifyExpression();

			//Create PDDL goal
			Goal goal = null;
			if(!(attached.size() == 0)){
				goal = new Goal(simplified, Goal.Action.MOVE);
			}
			return goal;
		}

		@Override

		//create a stack
		//naive implementation that (if possible) sorts all selected object in a stackable way
		// and then moves them in  top of each other in that order. 
		// this does not produce the fastest reachable stack, only one of all O(n!) possible stacks. 
		// TODO: does not handle stacks with multiple boxes and tables of the same size, yet. 
		public Goal visit(StackNode n, Set<WorldObject> worldObjects)
				throws InterpretationException, CloneNotSupportedException {

			LogicalExpression<WorldObject> firstObjects = n.getThingsToStackNode().accept(new NodeVisitor(), worldObjects, null);

			// create comparator that orders WorldObjects in stackable order
			Comparator<WorldObject> stackComparator = new Comparator<WorldObject>(){

				@Override
				public int compare(WorldObject o1, WorldObject o2) {

					if(!o1.getSize().equals(o2.getSize())){	
						if(o1.getSize().equals("large"))
						{	return -1;}
						else
						{return 1;}
					}	
					if (o1.getForm().equals(o2.getForm()))
						return o1.getColor().compareTo(o2.getColor());

					if (o1.getForm().equals("ball"))
						return 1;
					if (o2.getForm().equals("ball"))
						return -1;
					if (o1.getForm().equals("box"))
						return 1;
					if (o2.getForm().equals("box"))
						return -1;
					if (o1.getForm().equals("pyramid"))
						return 1;
					if (o2.getForm().equals("pyramid"))
						return -1;

					if (o1.getForm().equals("brick"))
						return 1;
					if (o2.getForm().equals("brick"))
						return -1;

					if (o1.getForm().equals("plank"))
						return 1;
					if (o2.getForm().equals("plank"))
						return -1;				
					return 0;
				}
			};

			List<WorldObject> stackOrder = new ArrayList<WorldObject>();
			stackOrder.addAll( firstObjects.getObjs());
			Collections.sort(stackOrder, stackComparator );
			Iterator<WorldObject> i = stackOrder.iterator();
			Set<WorldObject> theSet = new HashSet<WorldObject>();

			// create a relativeWorldObject for the stack

			if (!i.hasNext())
				throw new InterpretationException("Something is wrong with the stacking");


			WorldObject nex = i.next();
			WorldObject prev ;
			RelativeWorldObject rwo = new  RelativeWorldObject(nex,new WorldObject("floor","floor","floor","floor"),Relation.ONTOP);

			while (i.hasNext())
			{ 
				prev = nex;
				nex = i.next();
				if (!world.isValidRelation(Relation.ONTOP, nex, prev)){
				    disambiguator d = new disambiguator();
				    Set<WorldObject> theTwo = new HashSet<WorldObject>();
				    theTwo.add(prev);
				    theTwo.add(nex);
				  
					//String ontop = d.minimalUniqueDiscription(nex, theTwo, false);
					//String below = d.minimalUniqueDiscription(prev, theTwo, false);
					String ontop = d.minimalUniqueDiscription(nex, worldObjects, true);
					String below = d.minimalUniqueDiscription(prev, worldObjects, true);

					String err = "Sorry, it is not possible to stack "+ n.getThingsToStackNode().toNaturalString()+". ";
				     err = err+ "I would have to put "+ ontop + " on top of " + below +". Duuh.";
				     
					throw new InterpretationException( err);
				}
				rwo =  new RelativeWorldObject(nex,rwo, Relation.ONTOP);
			} 
			theSet.add(rwo);

			LogicalExpression<WorldObject> expr  = new LogicalExpression<WorldObject>(theSet, LogicalExpression.Operator.NONE);
			//world.removeImpossibleLogic(expr);



			Goal r = new Goal(expr, Action.MOVE); 
			return r ;


		}
	}

	private class NodeVisitor implements INodeVisitor<LogicalExpression<WorldObject>,Set<WorldObject>,Quantifier> {


		// visit basic
		@Override
		public LogicalExpression<WorldObject> visit(BasicEntityNode n, Set<WorldObject> worldObjects, Quantifier quantifier) throws InterpretationException, CloneNotSupportedException {
			return n.getObjectNode().accept(this, worldObjects, n.getQuantifierNode().getQuantifier());
		}


		//visit Relative Entity
		@Override
		public LogicalExpression<WorldObject> visit(RelativeEntityNode n, Set<WorldObject> worldObjects, Quantifier dummy) throws InterpretationException, CloneNotSupportedException {
			Quantifier q = n.getQuantifierNode().getQuantifier();
			LogicalExpression<WorldObject> matchesArg1 = n.getObjectNode().accept(this, worldObjects, q);
			LogicalExpression<WorldObject> matchesLocation = n.getLocationNode().accept(this, null, q); //Null because the argument is not relevant...
			//            WorldConstraint.Relation relation = n.getLocationNode().getRelationNode().getRelation();
			if(q.equals(Quantifier.THE)){
				Set<WorldObject> wobjs = world.filterByRelation(matchesArg1.getObjs(), matchesLocation, LogicalExpression.Operator.OR);
				if(wobjs.size() > 1){
					if(!Shrdlite.debug){

						disambiguator d = new disambiguator();
						d.disambiguate(wobjs, n);


						throw new InterpretationException(d.getMessage());


						//throw new InterpretationException("Several objects match the description '" + n.getObjectNode().getChildren().toString() +  "' with relation '" + n.getLocationNode().getRelationNode().getRelation() + "' to '" + n.getLocationNode().getEntityNode().getChildren().toString() + "'. Which one do you mean?");
					}
				} else if(wobjs.isEmpty()){
					if(!Shrdlite.debug){

						throw new InterpretationException("I cannot see any " + n.toNaturalString());
						//throw new InterpretationException("There are no objects which match the description '" + n.getObjectNode().getChildren().toString() +  "' with relation '" + n.getLocationNode().getRelationNode().getRelation() + "' to '" + n.getLocationNode().getEntityNode().getChildren().toString());

					}
				}
				LogicalExpression<WorldObject> le = new LogicalExpression<WorldObject>(wobjs, LogicalExpression.Operator.NONE);
				return le;
			} else { //ANY, AND
				//don't filter since the planner may arrange this situation to exist. e.g. for "put the white ball in (a box on the floor)", the planner might first put the box on the floor
				//this also applies to the "all" operator. Consider the sentence "put a box (to the right of (all bricks on a table))". Here, the planner can first put bricks on a table (or choose not to). TODO: As it is now, all bricks must be on a table after the planner finishes. There are two interpretations! Ambiguous!
				return world.attachWorldObjectsToRelation(matchesArg1, matchesLocation);
			}
			//            Set<WorldObject> wobjs = world.filterByRelation(matchesArg1.getObjs(), matchesLocation, LogicalExpression.Operator.OR);
			//            if(wobjs.isEmpty() && !Shrdlite.debug){
			//                throw new InterpretationException("There are no objects which match the description '" + n.getObjectNode().getChildren().toString() +  "' with relation '" + n.getLocationNode().getRelationNode().getRelation() + "' to '" + n.getLocationNode().getEntityNode().getChildren().toString());
			//            }
			//            LogicalExpression<WorldObject> le = new LogicalExpression<WorldObject>(wobjs, LogicalExpression.Operator.AND);
			//            return le;
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

		// visit relativeNode
		public LogicalExpression<WorldObject> visit(RelativeNode n, Set<WorldObject> worldObjects, Quantifier dummy2) throws InterpretationException, CloneNotSupportedException {
			LogicalExpression<WorldObject> relativeTo = n.getEntityNode().accept(this, worldObjects == null ? world.getWorldObjects() : worldObjects, Quantifier.ANY);

			relativeTo.simplifyExpression();

			//Convert tops to RelativeWorldObjects
			Set<WorldObject> objsNew = new HashSet<WorldObject>();
			Set<LogicalExpression> expNew = new HashSet<LogicalExpression>();
			for(LogicalExpression<WorldObject> le : relativeTo.getExpressions()){
				Set<WorldObject> wRelObjs = new HashSet<WorldObject>();
				for(WorldObject wo : le.getObjs()){
					wRelObjs.add(new RelativeWorldObject(wo, n.getRelationNode().getRelation()));
				}
				expNew.add(new LogicalExpression(wRelObjs, le.getExpressions(),le.getOp()));
			}
			if(relativeTo.getObjs() != null){
				for(WorldObject wo : relativeTo.getObjs()){
					objsNew.add(new RelativeWorldObject(wo, n.getRelationNode().getRelation()));
				}
			}
			LogicalExpression<WorldObject> relativeToNew = new LogicalExpression<WorldObject>(objsNew, expNew, relativeTo.getOp());//new HashSet<LogicalExpression<WorldObject>>();
			if(relativeTo.isEmpty() && !Shrdlite.debug){
				//throw new InterpretationException("There are no objects which match the description '"  + n.toNaturalString() + ".");
				throw new InterpretationException("I cannot see any " + n.toNaturalString() +". Try again.");
			}

			return relativeToNew;
		}

		// visit floor

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
			if(quantifier.equals(Quantifier.THE) && logObjs.size() > 1 && n.getParent() instanceof BasicEntityNode){

				disambiguator d = new disambiguator();
				d.disambiguate(logObjs.getObjs(), n);


				throw new InterpretationException(d.getMessage());
				//throw new InterpretationException("Several objects match the description '" + n.getChildren().toString() +  "'. Which one do you mean?");//TODO: Proper error message
			}

			//This is actually OK. Consider the sentence "["take", "the", "box", "under", "an", "object", "on", "a", "green", "object"]". "The box" can be several boxes..
			//On the other hand, it is explicitly stated that "THE" always refers to a unique object. 
			//In the example above, one should really say "take a box.. " if one is referring to any box that matches the description that follows. 


			if(logObjs.isEmpty() && !Shrdlite.debug) {
				//throw new InterpretationException("There are no objects which match the description '" + n.getChildren().toString() + ".");
				throw new InterpretationException("I cannot see any " + n.toNaturalString() +". Try again.");

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
