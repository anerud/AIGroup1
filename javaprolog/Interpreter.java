import java.util.ArrayList;
import java.util.List;

import gnu.prolog.term.Term;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;


public class Interpreter {
	
	private JSONArray world;
	private String holding;
	private JSONObject objects;

	public Interpreter(JSONArray world, String holding, JSONObject objects) {
		this.world = world;
		this.holding = holding;
		this.objects = objects;
	}

	public List<Goal> interpret(Term tree) {
		//TODO: Implement the interpretation of the tree
		List<Goal> goals = new ArrayList<Goal>();
		goals.add(new Goal());
		return goals;
	}
	
	

}
