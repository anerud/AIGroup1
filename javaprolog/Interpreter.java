import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.*;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;


public class Interpreter {
	
	private ArrayList<LinkedList<WorldObject>> world;
	private String holding;
	private PrintWriter log;
	private PrintWriter tlog;
	private int treeDepth;

	public Interpreter(ArrayList<LinkedList<WorldObject>> world, String holding) {
		this.world = world;
		this.holding = holding;
		try {
			log = new PrintWriter("intepreter log.txt", "UTF-8");
			tlog = new PrintWriter("intepreter treelog.txt", "UTF-8");
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public List<Goal> interpret(NTree tree) {
        //Some initial preprocessing
		
		
		
		
        //Build an internal representation of the parse tree which is easier to work with

        //Now use the internal representation to extract the goals from the tree. That is, create logical pddl-expressions from the tree.
		return new ArrayList<Goal>();//TODO: return extractPDDLGoals(pt);
	}

    /**
     * Extracts the PDDL goals from the parse tree
     * @param tree the parse tree
     * @return a list of goals
     */
    private List<Goal> extractPDDLGoals(NTree tree) {
        ArrayList<Goal> goals = new ArrayList<Goal>();
        //Let's hard-code stuff for the moment to get the idea.. The following is an example that should work for the sentence "take the white ball"
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
        if(action.equals("take")){
            //is the (handempty) precondition fulfilled?
            if(holding == null){
                //identify the objects
                JSONObject wantedObjects = filterObjects(tree.getRoot().getChildren().getFirst());
                //Create PDDL goals
                for(String wantedObject : (Set<String>)wantedObjects.keySet()){
                    //The PDDL goals should be of the type "(HOLDING OBJECT1)", that is, the goal describes the final state of the world
                    Goal goal =  null; //new Goal(some Exp..);TODO
                    goals.add(goal);
                }
            } else {
                //The action cannot be executed. Either notify the GUI that the object in hand needs to be dropped, or just drop it and try again...
            }
        } else if(action.equals("put")){
            if(holding != null){
                //identify the objects
                JSONObject wantedObjects = filterObjects(tree);
                //Create PDDL goals
                for(String wantedObject : (Set<String>)wantedObjects.keySet()){
                    //The PDDL goals should be of the type "(HOLDING OBJECT1)", that is, the goal describes the final state of the world
                    Goal goal =  null; //new Goal(some Exp..);TODO
                    goals.add(goal);
                }
            } else {
                //The action cannot be executed. Notify GUI or do stuff..
            }
        } else if(action.equals("move")){
            //dostuff
        }

//		goals.add(new Goal());  //TODO
        return goals;
    }

    /**
     * Filters the objects in the world and returns the ones which match the subtree of the current node of entity
     * TODO: perhaps not use JSONObject.. use something more efficient.
     *
     * @param entity
     * @return
     */
    private JSONObject filterObjects(Node entity) {
        //Basic_entity and floor are the base cases in the recursion

        String str = entity.getData();
        List<Node> args = entity.getChildren();
        if(str.equals("basic_entity")) {
            if(args.get(0).getData().equals("the")) {
                if(args.get(1).getData().equals("object")){
                    //Leaf..
                    //Now simply filter out the unique object in the world which matches the description. If multiple, return an error message.
                    //TODO: compare this.objects and the children of the current node in entity
                } else{
                    //..? above is prob. always satisfied.
                }
            } else { //Quantifier any
                if(args.get(1).getData().equals("object")){
                    //Leaf.. filter out the objects which match the description
                } else{
                    //..? above is prob. always satisfied.
                }
            }
        } else if(str.equals("floor")){
            //leaf..
        } else if(str.equals("relative_entity")){ //Here, we first filter the objects depending on the first argument object, then move on to the recursion..
            if(args.get(0).getData().equals("the")) {
                if(args.get(1).getData().equals("object")){
                    //Leaf..
                    //Now simply find the unique object in the world which matches the description. If multiple, it can still be filtered below..
                    //TODO: compare this.objects and the children of the current node in entity
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

            if(args.get(2).getData().equals("relative")) {

            } else {
                //..? above is prob. always satisfied.
            }

        } else { //"relative"
            // call filterObjects recursively
        }
        return new JSONObject();
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
	 * Check if an object, described in the tree, exists in the world.
	 * @return the number of existencies of an object.
	 */
	private int existsInWorld() {
		JSONArray column = (JSONArray) world.get(1);
        String topobject = (String) column.get(column.size() - 1);
        JSONObject objectinfo = (JSONObject) objects.get(topobject);
        String form = (String) objectinfo.get("form");

        return 0;
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
