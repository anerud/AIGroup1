import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

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

        //Now use the internal representation to extract the goals from the tree. That is, create a logical pddl-expression from the tree.
        ArrayList<Goal> goals = new ArrayList<Goal>();


//		goals.add(new Goal());  //TODO
		return goals;
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
