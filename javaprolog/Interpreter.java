import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.*;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;


public class Interpreter {
	
	private World world;
	private PrintWriter log;
	private PrintWriter tlog;
	private int treeDepth;

	public Interpreter(World world) {
		this.world = world;
		try {
			log = new PrintWriter("intepreter log.txt", "UTF-8");
			tlog = new PrintWriter("intepreter treelog.txt", "UTF-8");
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public List<Goal> interpret(NTree tree) {
        //Now use the internal representation to extract the goals from the tree. That is, create logical pddl-expressions from the tree.
		return extractPDDLGoals(tree);
	}

    /**
     * Extracts the PDDL goals from the parse tree
     * @param tree the parse tree
     * @return a list of goals
     */
    private List<Goal> extractPDDLGoals(NTree tree) {
        ArrayList<Goal> goals = new ArrayList<Goal>();
        //Let's hard-code stuff for the moment to get the idea..
        //Note that the root is merely symbolic here, it contains nothing. TODO: Eventually, the rules for every action should be dynamically read from the PDDL-format. See the following example PDDL for the action pick-up:
        /*
         (:action pick-up
             :parameters (?x - block)
             :precondition (and (clear ?x) (ontable ?x) (handempty))
             :effect
             (and (not (ontable ?x))
               (not (clear ?x))
               (not (handempty))
               (holding ?x)))
         */
        //What action is it?
        String action = tree.getRoot().toString();
        LinkedList<Node> args = tree.getRoot().getChildren();
        if(action.equals("take")){
            //is the (handempty) precondition fulfilled?
            if(world.getHolding() == null){
                //identify the objects
                LinkedList<WorldObject> desiredObjs = world.getWorldObjects();
                filterObjects(args.getFirst(), desiredObjs);   //getfirst should return a basic_entity

                //Create PDDL goals
                //We can only hold one object, but if many objects are returned, the planner can choose the closest one.
                StringBuilder pddlString = new StringBuilder();
                if(desiredObjs.size() > 1){
                    pddlString.append("(OR ");
                    for(WorldObject des : desiredObjs){
                        //The PDDL goals should be of the type "(HOLDING OBJECT1)", that is, the goal describes the final state of the world
                        pddlString.append("(holding " + des.getId() + ") ");
                    }
                    pddlString.deleteCharAt(pddlString.length() - 1);
                    pddlString.append(")");
                } else {
                    pddlString.append("(holding " + desiredObjs.getFirst().getId() + ") ");
                }

                Goal goal =  new Goal(pddlString.toString()); //TODO new Goal(some Exp..);
                goals.add(goal);
            } else {
                //The action cannot be executed. TODO: Either notify the GUI that the object in hand needs to be dropped, or just drop it and try again...
            }
        } else if(action.equals("put")){
            //TODO: Ska "put" fungera på ett vettigt sätt, eller som angivet i exemplen, d.v.s. put fungerar likadant som move?
//            if(holding != null){
//                //identify the objects
//                JSONObject wantedObjects = filterObjects(tree);
//                //Create PDDL goals
//                for(String wantedObject : (Set<String>)wantedObjects.keySet()){
//                    //The PDDL goals should be of the type "(HOLDING OBJECT1)", that is, the goal describes the final state of the world
//                    Goal goal =  null; //new Goal(some Exp..);TODO
//                    goals.add(goal);
//                }
//            } else {
//                //The action cannot be executed. Notify GUI or do stuff..
//            }
        } else if(action.equals("move")){
            //TODO: Check preconditions..

            LinkedList<WorldObject> desiredObjs = world.getWorldObjects(); //First argument
            LinkedList<WorldObject> desiredObjs2 = world.getWorldObjects(); //Second argument

            filterObjects(args.getFirst(), desiredObjs);

            //Now filter these depending on the relative objects..
            List<Node> relArgs = args.get(1).getChildren();
            if(args.get(1).getData().equals("relative")) {
                filterObjects(relArgs.get(1), desiredObjs2);
            } else { //
                // Cannot happen probably..
            }

            //Create PDDL goals
            //TODO: What happens if many objects are to be moved to many places?
            //Use: relArgs.get(0) to get the relative keyword
            for(WorldObject des : desiredObjs){
                //The PDDL goals should be of the type "(HOLDING OBJECT1)", that is, the goal describes the final state of the world
                Goal goal =  null; //new Goal(some Exp..);TODO
                goals.add(goal);
            }
        }
//		goals.add(new Goal());  //TODO
        return goals;
    }

    /**
     * Filters the objects in the world and returns the ones which match the subtree of the current node of entity
     * TODO: perhaps not use JSONObject.. use something more efficient.
     *
     * @param rules
     * @return
     */
    private void filterObjects(Node rules, List<WorldObject> toBeFiltered) {
        //Basic_entity and floor are the base cases in the recursion
        List<WorldObject> toBeFilteredOrig = new LinkedList<>(toBeFiltered);

        String str = rules.getData();
        List<Node> args = rules.getChildren();
        if(str.equals("basic_entity")) {
            //Filter out the object
            if(args.get(1).getData().equals("object")){
                List<Node> objArs = args.get(1).getChildren();
                //Leaf.. Now simply filter out the unique object in toBeFiltered which matches the description. If multiple, return an error message.
                filterByMatch(toBeFiltered, new WorldObject(objArs.get(0).getData(), objArs.get(1).getData(), objArs.get(2).getData()));
            } else{
                //..? above is prob. always satisfied.
            }
            //
            if(args.get(0).getData().equals("the") && toBeFiltered.size() > 1) {
                //TODO return error message to GUI
            }// Else quantifier any, so everything is ok..
        } else if(str.equals("floor")){
            toBeFiltered.clear();
            toBeFiltered.add(new WorldObject("floor", "floor", "floor"));
        } else if(str.equals("relative_entity")){ //Here, we first filter the objects depending on the first argument object, then move on to the recursion..
            if(args.get(0).getData().equals("the")) {
                if(args.get(1).getData().equals("object")){
                    //Leaf.. now simply find the unique object in the world which matches the description. If multiple, it can still be filtered below..
                    List<Node> objArs = args.get(1).getChildren();
                    filterByMatch(toBeFiltered, new WorldObject(objArs.get(0).getData(), objArs.get(1).getData(), objArs.get(2).getData()));
                } else{
                    //..? above is prob. always satisfied.
                }
            } else { //Quantifier any
                if(args.get(1).getData().equals("object")){
                    //Leaf.. find the objects which match the description
                } else{
                    //..? above is prob. always satisfied.
                }
            }

            Node relative = args.get(2);
            if(relative.getData().equals("relative")) {
                List<Node> relArgs = relative.getChildren();
                // call filterObjects recursively to get the relative objects
                List<WorldObject> theRelativeObjects = new LinkedList<WorldObject>(toBeFilteredOrig);
                filterObjects(relArgs.get(1), theRelativeObjects);

                //retain objects for which toBeFiltered is inside or ontop theRelativeObjects
                filterByRelation(toBeFiltered, theRelativeObjects, relArgs.get(0).getData());
            } else {
                //..? above is prob. always satisfied.
            }
            //Note that the "relative" keyword is never encountered here
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


    /**
	 * Recursive function that builds a ParseTree structure from a linearized String tree
	 * @param tree the linearized tree
     * @param treeToBuild the ParseTree representation of this parse tree
	 */
	private void buildParseTree(String tree, ParseTree treeToBuild) {
		
		int parent = tree.indexOf("(");
		if(parent <= 0) 
			parent = Integer.MAX_VALUE;
		int child = tree.indexOf(",");
		if(child < 0) 
			child = Integer.MAX_VALUE;
		int closure = tree.indexOf(")");
		if(closure < 0) 
			closure = Integer.MAX_VALUE;
		int min = Math.min(parent, Math.min(child, closure));
		if(min < Integer.MAX_VALUE) {
			String e1 = tree.substring(0, min);
			String rest = tree.substring(min+1, tree.length());
			String ending = "";
			int d = 0;
			if(min == parent) {
				ending = "(";
				d = 1;
                treeToBuild.addChild(e1);
				tlog.println("Added parent: " + e1);
			} else if(min == closure) {
				ending = ")";
				d = -1;
				if(e1.length() > 0) {
                    treeToBuild.addLeaf(e1);
					tlog.println("Added leaf (length > 1): " + e1);
				} else {
					tlog.println("Went to parent");
				}
                treeToBuild.parent();
			} else if(min == child) {
				if(e1.length() > 0) {
                    treeToBuild.addLeaf(e1);
					tlog.println("Added child: " + e1);
				}
			}
			log.println(toWhiteSpace(treeDepth) + e1 + ending);
			treeDepth += d;
			buildParseTree(rest, treeToBuild);
		}
		tlog.close();
		log.close();
	}

	
	/**
	 * Checks if an object fits a description
	 * example: fitsDescription("ball, (-), white", o) returns true if
	 * the object o is white and a ball (independent of its size)
	 */
	private boolean fitsDescription(String discription, JSONObject o){
		return true;
	}
	
	private String toWhiteSpace(int n) {
		String s = "";
		for(int i = 0; i<n;i++) {
			s = s + "\t";
		}
		return s;
	}
	
	

}
