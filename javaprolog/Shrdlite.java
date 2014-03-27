

// Then test from the command line:
// java -cp gnuprologjava-0.2.6.jar:json-simple-1.1.1.jar:. Shrdlite < ../examples/medium.json

import gnu.prolog.term.*;
import gnu.prolog.vm.PrologException;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.management.relation.RelationException;

import main.DCGParser;
import main.Goal;
import main.Interpreter;
import main.Planner;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

import tree.*;
import world.World;
import world.WorldObject;

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
		HashMap<String, WorldObject> objsArr = new HashMap<String, WorldObject>();

		PrintWriter log = new PrintWriter("Log.txt", "UTF-8");

		for (Object o : objsJSON.keySet().toArray()) {
			if (o instanceof String) {
				HashMap<String, String> obj = (HashMap<String, String>) objsJSON.get(o);
				WorldObject wo = new WorldObject(obj.get("form"), obj.get("size"), obj.get("color"), (String) o);
				objsArr.put((String) o, wo);
			}
		}
		// Initialize world
		World world = new World(worldArr);
		// Initialize holding object
		WorldObject holding = objsArr.get(holdingId);

		for (int i = 0; i < worldJSON.size(); i++) {
			LinkedList<WorldObject> objList = new LinkedList<WorldObject>();
			for (String s : (List<String>) worldJSON.get(i)) {
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
//			log.println(termsToTree((CompoundTerm) t, null).toString());
			//log.println(t.toString());
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

	// Build an internal representation of the parse tree which is easier to
	// work with
	private static NTree termsToTree(CompoundTerm t, Node parent) {
		Node n = getNodeFromData(parent, t.tag.functor.value, t);
		NTree tree = new NTree(n);
		/*
		 * for (Term tt : t.args) { if (tt instanceof CompoundTerm) {
		 * CompoundTerm ttt = (CompoundTerm) tt; //NTree tr = termsToTree(ttt,
		 * n);
		 * 
		 * // n.getChildren().add(tr.getRoot()); } else if (tt instanceof
		 * AtomTerm) { AtomTerm ttt = (AtomTerm) tt; //
		 * n.getChildren().add(getNodeFromData(parent, ttt.value)); } }
		 */
		return tree;
	}

	private static String dataFromTerm(Term t) {
		if (t instanceof CompoundTerm) {
			return ((CompoundTerm) t).tag.functor.value;
		} else {
			return ((AtomTerm) t).value;
		}
	}

	private static Node getNodeFromData(Node parent, String data, Term t) {
		if (data.equals("basic_entity")) {
			BasicEntityNode n = new BasicEntityNode(parent, data);
			CompoundTerm tt = (CompoundTerm) t;
			n.setQuantifierNode(getNodeFromData(n, dataFromTerm(tt.args[0]), null));
			n.setObjectNode(getNodeFromData(n, dataFromTerm(tt.args[1]), tt.args[1]));
			return n;
		} else if (data.equals("relative_entity")) {
			RelativeEntityNode n = new RelativeEntityNode(parent, data);
			CompoundTerm tt = (CompoundTerm) t;
			n.setQuantifierNode(getNodeFromData(n, dataFromTerm(tt.args[0]), null));
			n.setObjectNode(getNodeFromData(n, dataFromTerm(tt.args[1]), tt.args[1]));
			n.setLocationNode(getNodeFromData(n, dataFromTerm(tt.args[2]), tt.args[2]));
			return n;
		} else if (data.equals("relative")) {
			RelativeNode n = new RelativeNode(parent, data);
			CompoundTerm tt = (CompoundTerm) t;
			n.setRelationNode(getNodeFromData(n, dataFromTerm(tt.args[0]), tt.args[0]));
			n.setEntityNode(getNodeFromData(n, dataFromTerm(tt.args[1]), tt.args[1]));
			return n;
		} else if (data.equals("move")) {
			MoveNode n = new MoveNode(parent, data);
			CompoundTerm tt = (CompoundTerm) t;
			n.setLocationNode(getNodeFromData(n, dataFromTerm(tt.args[1]), tt.args[1]));
			n.setEntityNode(getNodeFromData(n, dataFromTerm(tt.args[0]), tt.args[0]));
			return n;
		} else if (data.equals("take")) {
			TakeNode n = new TakeNode(parent, data);
			CompoundTerm tt = (CompoundTerm) t;
			n.setEntityNode(getNodeFromData(n, dataFromTerm(tt.args[0]), tt.args[0]));
			return n;
		} else if (data.equals("put")) {
			PutNode n = new PutNode(parent, data);
			CompoundTerm tt = (CompoundTerm) t;
			n.setLocationNode(getNodeFromData(n, dataFromTerm(tt.args[0]), tt.args[0]));
			return n;
		} else if (data.equals("floor")) {
			return new FloorNode(parent, data);
		} else if (data.equals("any") || data.equals("all") || data.equals("the")) {
			return new QuantifierNode(parent, data);
		} else if (data.equals("object")) {
			ObjectNode n = new ObjectNode(parent, data);
			CompoundTerm tt = (CompoundTerm) t;
			n.setFormNode(getNodeFromData(n, dataFromTerm(tt.args[0]), tt.args[0]));
			n.setSizeNode(getNodeFromData(n, dataFromTerm(tt.args[1]), tt.args[1]));
			n.setColorNode(getNodeFromData(n, dataFromTerm(tt.args[2]), tt.args[2]));
			return n;
		} else if (data.equals("under") || data.equals("beside") || data.equals("above") || data.equals("leftof")
				|| data.equals("rightof") || data.equals("ontop") || data.equals("inside")) {
			return new RelationNode(parent, data);
		}
		return new AttributeNode(parent, data);
	}

}
