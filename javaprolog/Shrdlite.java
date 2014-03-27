// First compile the program:
// javac -cp gnuprologjava-0.2.6.jar:json-simple-1.1.1.jar:. Shrdlite.java

// Then test from the command line:
// java -cp gnuprologjava-0.2.6.jar:json-simple-1.1.1.jar:. Shrdlite < ../examples/medium.json

import gnu.prolog.term.*;
import gnu.prolog.vm.PrologException;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

public class Shrdlite {

	public static void main(String[] args) throws PrologException, ParseException, IOException {
		JSONObject jsinput = null;
		if (args.length == 0) {
			jsinput = (JSONObject) JSONValue.parse(readFromReader(new InputStreamReader(System.in)));
		} else {
			jsinput = (JSONObject) JSONValue.parse(readFromReader(new FileReader(args[0])));
		}
		JSONArray utterance = (JSONArray) jsinput.get("utterance");
		JSONArray worldJSON = (JSONArray) jsinput.get("world");
		String holdingId = (String) jsinput.get("holding");
		JSONObject objsJSON = (JSONObject) jsinput.get("objects");
		
		ArrayList<LinkedList<WorldObject>> worldArr = new ArrayList<LinkedList<WorldObject>>(worldJSON.size());
		HashMap<String, WorldObject> objsArr = new HashMap<String,WorldObject>();

		PrintWriter log = new PrintWriter("Log.txt", "UTF-8");
		
		for(Object o : objsJSON.keySet().toArray()){
			if(o instanceof String){
				HashMap<String,String> obj = (HashMap<String, String>)objsJSON.get(o);
				WorldObject wo = new WorldObject(obj.get("form"), obj.get("size"), obj.get("color"), (String)o);
				objsArr.put((String) o, wo);
			}
		}
        //Initialize world
        World world = new World(worldArr);
        //Initialize holding object
        WorldObject holding = objsArr.get(holdingId);
		
		for(int i =0;i<worldJSON.size();i++){
			LinkedList<WorldObject> objList = new LinkedList<WorldObject>();	
			for(String s : (List<String>)worldJSON.get(i)){
				objList.add(objsArr.get(s));
			}
			worldArr.add(objList);
		}
		
		JSONObject result = new JSONObject();
		result.put("utterance", utterance);


		DCGParser parser = new DCGParser("shrdlite_grammar.pl");
		List<Term> trees = parser.parseSentence("command", utterance);

		List<NTree> treeList = new ArrayList<NTree>();
		for (Term t : trees) {
			treeList.add(termsToTree((CompoundTerm) t, null));
			
			log.println(termsToTree((CompoundTerm) t, null).getAsList());
			log.println(t.toString());
		}

		if (trees.isEmpty()) {
			result.put("output", "Parse error!");
		} else {
			List<Goal> goals = new ArrayList<Goal>();

			Interpreter interpreter = new Interpreter(world);
			for (NTree tree : treeList) {
				for (Goal goal : interpreter.interpret(tree)) {
					goals.add(goal);
				}
			}
			
			if (goals.isEmpty()) {
				result.put("output", "Interpretation error!");

			} else if (goals.size() > 1) { // TODO: This can be OK as long as
											// only one of the goals is
											// reachable for the planner. If
											// more than one goal is reachable
											// and a pair of reachable goals
											// come from different parse trees,
											// there is an ambiguity which needs
											// a clarification question.
				result.put("output", "Ambiguity error!");

			} else {
				Planner planner = new Planner(world);
				List<String> plan = planner.solve(goals.get(0)); /*
																 * TODO: if we
																 * have several
																 * goals from
																 * the same
																 * tree, we
																 * return the
																 * solution to
																 * the fastest
																 * one which the
																 * planner can
																 * find a plan
																 * for. If
																 * ambiguities
																 * arise from
																 * the same
																 * tree, it is
																 * up to the
																 * user to add
																 * more
																 * information
																 * if more
																 * specific
																 * goals are
																 * intended.
																 */
				result.put("plan", plan);

				if (plan.isEmpty()) {
					result.put("output", "Planning error!");
				} else {
					result.put("output", "Success!");
				}
			}
		}

		System.out.print(result);

		// Also print the result to a file
		FileWriter fw = new FileWriter("./result.json");
        String jsonString = result.toJSONString();
		String pretty = com.cedarsoftware.util.io.JsonWriter.formatJson(jsonString);
		fw.write(pretty);
		fw.close();

		log.close();
	}

	public static String readFromReader(Reader reader) throws IOException {
		BufferedReader in = new BufferedReader(reader);
		StringBuilder data = new StringBuilder();
		String line;
		while ((line = in.readLine()) != null) {
			data.append(line).append('\n');
		}
		return data.toString();
	}

	//Build an internal representation of the parse tree which is easier to work with
	private static NTree termsToTree(CompoundTerm t, Node parent) {
		Node n = new Node(parent, t.tag.functor.value);
		NTree tree = new NTree(n);
		
		for (Term tt : t.args) {
			if (tt instanceof CompoundTerm) {
				CompoundTerm ttt = (CompoundTerm) tt;
				NTree tr = termsToTree(ttt, n);
				n.getChildren().add(tr.getRoot());
			}else if (tt instanceof AtomTerm) {
				AtomTerm ttt = (AtomTerm) tt;
				n.getChildren().add(new Node(parent, ttt.value));
			} else {
			}
		}
		
		
		return tree;
	}

}
