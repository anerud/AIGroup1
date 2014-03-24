import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;


public class Interpreter {
	
	private JSONArray world;
	private String holding;
	private JSONObject objects;
	private PrintWriter log;
	private PrintWriter tlog;
	private ParseTree pt;
	private int treeDepth;

	public Interpreter(JSONArray world, String holding, JSONObject objects) {
		this.world = world;
		this.holding = holding;
		this.objects = objects;
		try {
			log = new PrintWriter("intepreter log.txt", "UTF-8");
			tlog = new PrintWriter("intepreter treelog.txt", "UTF-8");
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		pt = new ParseTree();
	}

	public List<Goal> interpret(String tree) {
        //Some initial preprocessing
		String cleanString = cleanupString(tree);
		log.println(cleanString);
		tlog.println(cleanString);

        //Build an internal representation of the parse tree which is easier to work with
        pt = new ParseTree();
        buildParseTree(cleanString, pt);

        //Now use the internal representation to extract the goals from the tree. That is, create logical pddl-expressions from the tree.
		return new ArrayList<Goal>();//TODO: return extractPDDLGoals(pt);
	}

    /**
     * Extracts the PDDL goals from the parse tree
     * @param pt the parse tree
     * @return a list of goals
     */
    private List<Goal> extractPDDLGoals(ParseTree pt) {
        ArrayList<Goal> goals = new ArrayList<Goal>();
        pt.returnToRoot();
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
        if(pt.nextChild().toString().equals("take")){
            //is the (handempty) precondition fulfilled?
            if(holding == null){
                //identify the objects
                JSONObject wantedObjects = filterObjects(pt);
                //Create PDDL goals
                for(String wantedObject : (Set<String>)wantedObjects.keySet()){
                    //The PDDL goals should be of the type "(HOLDING OBJECT1)", that is, the goal describes the final state of the world
                    Goal goal =  null; //new Goal(some Exp..);TODO
                    goals.add(goal);
                }
            } else {
                //The action cannot be executed. Either notify the GUI that the object in hand needs to be dropped, or just drop it and try again...
            }
        }
//		goals.add(new Goal());  //TODO
        return goals;
    }

    /**
     * Filters the objects in the world and returns the ones which match the subtree of the current node of pt
     * TODO: perhaps not use JSONObject.. use something more efficient.
     * @param pt
     * @return
     */
    private JSONObject filterObjects(ParseTree pt) {
        if(pt.nextChild().toString().equals("basic_entity")) {
            if(pt.nextChild().toString().equals("the")) {
                if(pt.getNextChild().toString().equals("object")){
                    //Now simply find the unique object in the world which matches the description. If multiple, return an error message.
                    //TODO: compare this.objects and the children of the current node in pt
                } else{
                    //..?
                }
            } else {
                //Quantifiers: any, some, etc...
            }

        } else {
            //relative entity...
        }
        return new JSONObject();
    }

    private String cleanupString(String tree) {
		while(tree.indexOf("(-)") > 0){
			tree = tree.substring(0,tree.indexOf("(-)")) + "-" +
					tree.substring(tree.indexOf("(-)")+3,tree.length());
		}
		return tree;
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
	
	public ParseTree getParseTree(){
		return pt;
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
