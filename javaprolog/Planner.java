import java.util.*;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
public class Planner {
	
	private JSONArray world;
	private JSONObject objects;
	private String holding;
	
	public Planner(JSONArray world, String holding, JSONObject objects) {
		this.world = world;
		this.holding = holding;
		this.objects = objects;
	}

	public List<String> solve(Goal goal){
		int column = 0;
		while (((JSONArray)world.get(column)).isEmpty()) column++;
		List<String> plan = new ArrayList<String>(); 
        plan.add("I pick up . . ."); 
        plan.add("pick " + column);
        plan.add(". . . and then I drop down"); 
        plan.add("drop " + (column+1));
        return plan;
	}
}
