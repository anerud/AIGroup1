package main;

import logic.LogicalExpression;
import logic.Quantifier;
import logic.Tense;
import main.Goal.Action;
import tree.*;
import world.RelativeWorldObject;
import world.World;
import world.WorldConstraint.Relation;
import world.WorldObject;

import java.util.*;


public class Interpreter {




	public class InterpretationException extends Exception{
		public InterpretationException(String s) {
			super(s);
		}
	}

	//thrown when a reference to an object (THE) matched more than one object
	public class AmbiguousReferenceException extends InterpretationException{
		int questionId;
		int subQuestionId;

		public AmbiguousReferenceException(String s) {
			this(s,0,0);
		}
		public AmbiguousReferenceException(String s,int questionId, int subQuestionId) {
			super(s);
			this.questionId =questionId;
			this.subQuestionId = subQuestionId;
		}
		public int getQuestionId() {
			return questionId;
		}
		public void setQuestionId(int questionId) {
			this.questionId = questionId;
		}
		public int getSubQuestionId() {
			return subQuestionId;
		}
		public void setSubQuestionId(int subQuestionId) {
			this.subQuestionId = subQuestionId;
		}
	}

	//thrown when a reference to an object (THE) matches no object  
	public class EmptyReferenceException extends InterpretationException{
		public  EmptyReferenceException(String s) {
			super(s);
		}
	}

	private World world;
	private Map<Integer, List<NTree>> answers;
	private int questionID = 0;


	public Interpreter(World world) {
		this.world = world;
	}

	/**
	 * Extracts the PDDL goals from the parse tree
	 *
	 * @param trees the parse tree
	 * @param answerMap 
	 * @return a list of goals
	 */

	//TODO:  correct exception handling for ambiguity + user questions
	public Set<Goal> interpret(List<NTree> trees, Map<Integer, List<NTree>> answerMap) throws InterpretationException, AmbiguousReferenceException, CloneNotSupportedException {
		Set<Goal> okGoals = new HashSet<>(trees.size());
		Set<NTree> ambiguousTrees = new HashSet<>(trees.size());
		Set<NTree> failedTrees = new HashSet<>(trees.size());
		answers = answerMap;

		Set<EmptyReferenceException> emptyReferenceExceptions = new HashSet<>();
		Set<AmbiguousReferenceException> ambiguousReferenceExceptions = new HashSet<>();
		Set<InterpretationException> exceptions = new HashSet<>();

		//traverse trees
		for(NTree tree : trees){

			// generate a goal from the tree
			// if there is no unambiguous goal, no goal will be created.
			Goal treeGoal = null;
			try{
				treeGoal = tree.getRoot().accept(new ActionVisitor(), world.getWorldObjects());
				if(treeGoal != null){
					world.removeImpossibleLogic(treeGoal.getExpression());
					treeGoal.getExpression().simplifyExpression();
				}
			} catch(EmptyReferenceException e){
				//there was a problem with the interpretation.  Some object did not exist.
				failedTrees.add(tree);
				emptyReferenceExceptions.add(e);
			}
			catch(AmbiguousReferenceException e){
				//there was an ambiguous THE reference.
				ambiguousTrees.add(tree);
				ambiguousReferenceExceptions.add(e);
			}
			catch(InterpretationException e){
				//there was some error. 
				exceptions.add(e);
			}

			//Interpretation was successful and unambiguous
			if(treeGoal != null){
				okGoals.add(treeGoal);
			}
		}

		// if there is exactly one good interpretation, assume it 
		if (okGoals.size()==1) return okGoals;

        // if there is more than one valid goal, we have more than one ok
		// parse trees: Todo: disambiguate using questions
		// for now, return error message
		if (okGoals.size()>1) throw new InterpretationException("I dont know what you mean exactly...");
		
		//if there are unresolved ambiguituies left, throw exception to create new question
		if (!ambiguousReferenceExceptions.isEmpty())
			throw ambiguousReferenceExceptions.iterator().next();

        // this should not happen
		return null;
		
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
					Disambiguator d = new Disambiguator();
					Set<WorldObject> theTwo = new HashSet<WorldObject>();
					theTwo.add(prev);
					theTwo.add(nex);

					//String ontop = d.minimalUniqueDiscription(nex, theTwo, false);
					//String below = d.minimalUniqueDiscription(prev, theTwo, false);
					String ontop = d.minimalUniqueDiscription(nex, worldObjects, true);
					String below = d.minimalUniqueDiscription(prev, worldObjects, true);

					String err = "Sorry, it is not possible to stack "+ n.getThingsToStackNode().toNaturalString()+". ";
					err = err+ "I would have to put "+ ontop + " on top of " + below +". Duuh.";

					throw new InterpretationException(err);
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

			TenseNode tenseNode = n.getTenseNode();

			if((tenseNode != null && tenseNode.getTense().equals(Tense.NOW))){
				Set<WorldObject> wobjs = world.filterByRelation(matchesArg1.getObjs(), matchesLocation, LogicalExpression.Operator.OR);
				LogicalExpression<WorldObject> le = new LogicalExpression<WorldObject>(wobjs, matchesArg1.getOp());
				return le;
			}

			if(q.equals(Quantifier.THE)){
				Set<WorldObject> wobjs = world.filterByRelation(matchesArg1.getObjs(), matchesLocation, LogicalExpression.Operator.OR);
				if(wobjs.size() > 1){
					if(!Shrdlite.debug){

						//ambiguous THE reference to a relative entity

						// there is an ambiguity in the reference. THE matches more than one object
						Disambiguator d = new Disambiguator();
						questionID++;
						if (!answers.containsKey(questionID))
							answers.put(questionID, new ArrayList<NTree>());
						List<NTree> subAnswers= answers.get(questionID);
						try{
							//try to resolve the ambiguity using the answers 
							WorldObject picked = d.disambiguate(wobjs ,n, subAnswers); 

							//delete all matches except the picked one. 
							wobjs.clear();
							wobjs.add(picked);

						}
						catch(AmbiguousReferenceException e )
						{
							//the ambiguity was not resolved  
							//tag on question ID and throw.  
							//A new question will be generated for the next
							//query.
							e.setQuestionId(questionID);
							e.setSubQuestionId(subAnswers.size());
							throw e;
						}


					}
				} else if(wobjs.isEmpty()){
					if(!Shrdlite.debug){
						throw new InterpretationException("I cannot see any " + n.toNaturalString());
						//throw new InterpretationException("There are no objects which match the description '" + n.getObjectNode().getChildren().toString() +  "' with relation '" + n.getLocationNode().getRelationNode().getRelation() + "' to '" + n.getLocationNode().getEntityNode().getChildren().toString());
					}
				}
				LogicalExpression<WorldObject> le = new LogicalExpression<WorldObject>(wobjs, matchesArg1.getOp());
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

				// there is an ambiguity in the reference. THE matches more than one object
				questionID++;
				Disambiguator d = new Disambiguator();
				if (!answers.containsKey(questionID))
					answers.put(questionID, new ArrayList<NTree>());
				List<NTree> subAnswers= answers.get(questionID);
				try{
					//try to resolve the ambiguity using the answers 
					WorldObject picked = d.disambiguate(logObjs.getObjs(),n, subAnswers); 

					//delete all matches except the picked one. 
					logObjs.getObjs().clear();
					logObjs.getObjs().add(picked);

				}
				catch(AmbiguousReferenceException e )
				{
					//the ambiguity was not resolved  
					//tag on question ID and throw.  
					//A new question will be generated for the next
					//query.
					e.setQuestionId(questionID);
					e.setSubQuestionId(subAnswers.size());
					throw e;
				}

			}
			// the reference did not match any objects 
			if(logObjs.isEmpty() && !Shrdlite.debug) {
				throw new EmptyReferenceException("I cannot see any " + n.toNaturalString() +". Try again.");
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

		@Override
		public LogicalExpression<WorldObject> visit(TenseNode tenseNode, Set<WorldObject> arg, Quantifier arg2) throws InterpretationException {
			//Never used
			throw new InterpretationException("Something went wrong during the interpretation.");
		}
	}
}
