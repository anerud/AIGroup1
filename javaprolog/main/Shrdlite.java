package main;

// Then test from the command line:
// java -cp gnuprologjava-0.2.6.jar:json-simple-1.1.1.jar:. main.Shrdlite < ../examples/medium.json

import aStar.AStar;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import gnu.prolog.term.AtomTerm;
import gnu.prolog.term.CompoundTerm;
import gnu.prolog.term.Term;
import gnu.prolog.vm.PrologException;
import tree.*;
import world.World;
import world.WorldObject;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Shrdlite {

	public static boolean debug;

	public static void main(String[] args) throws IOException, PrologException, CloneNotSupportedException {
		long start = System.currentTimeMillis();
//		try {
		String jsinput = null;
		if (args.length == 0) {
			jsinput = readFromReader(new InputStreamReader(System.in));
		} else {
			jsinput = readFromReader(new FileReader(args[0]));
		}
		Input p = new Gson().fromJson(jsinput, Input.class);

		if (args.length > 1 && args[1].equals("debug")) {
			debug = true;
		}

		ArrayList<LinkedList<WorldObject>> worldArr = new ArrayList<LinkedList<WorldObject>>(p.getWorld().size());

		PrintWriter log = new PrintWriter("Log.txt", "UTF-8");

		for (String s : p.getObjects().keySet()) {
			p.getObjects().get(s).setId(s);
		}

		// Initialize holding object
		WorldObject holding = p.getObjects().get(p.getHolding());
		// Initialize world
        for (int i = 0; i < p.getWorld().size(); i++) {
            LinkedList<WorldObject> objList = new LinkedList<WorldObject>();
            for (String s : p.getWorld().get(i)) {
                objList.add(p.getObjects().get(s));
            }
            worldArr.add(objList);
        }
		World world = new World(worldArr, holding);

		Input result = new Input();
		result.setUtterance(p.getUtterance());

		DCGParser parser = new DCGParser("shrdlite_grammar.pl");
		List<Term> trees = parser.parseSentence("command", p.getUtterance());

		if (p.getQuestion() != null && p.getQuestion().getAnswer() != null) {
			String q = p.getQuestion().getQuestions().get(p.getQuestion().getAnswer());
			if (q != null) {
				//TODO: F�tt svar p� fr�gan, q �r den valda fr�gan!
			}else{
				//TODO: F�tt d�ligt svar p� fr�gan
			}
		} else {
			//TODO: Ingen fr�ga st�lld!
		}

		List<NTree> treeList = new ArrayList<NTree>();
		for (Term t : trees) {
			treeList.add(termsToTree((CompoundTerm) t, null));
		}
		if (trees.isEmpty()) {
			result.setOutput("Parse error!");
		} else {
			List<Goal> goals = new ArrayList<Goal>();

			Interpreter interpreter = new Interpreter(world);

			try {
				goals.addAll(interpreter.interpret(treeList));
				if (debug) {
					result.setGoals(goals.toString());
				}
			} catch (Interpreter.InterpretationException e) {
				result.setOutput(e.getMessage());
			}

			if (goals.isEmpty()) {
				if (result.getOutput() == null) {
					result.setOutput("Interpretation error!");
				}
			} else if (goals.size() > 1) {
				result.setOutput("Ambiguity error!");

			} else {
				log.println(goals.get(0).toString());
				Planner planner = new Planner(world);
				List<String> plan = planner.solve(goals.get(0));
				result.setPlan(plan);
				log.println("number of states checked: " + AStar.nStatesChecked);
				if (plan.isEmpty()) {
					result.setOutput("Planning error!");
				} else {
					log.println(plan.toString());
					result.setOutput("Success!");
				}
			}
		}

		log.print(jsinput);
		FileWriter fw = new FileWriter("./result.json");
		String jsonString = new Gson().toJson(result);
		String pretty = new GsonBuilder().setPrettyPrinting().create().toJson(p);
		fw.write(pretty);
		fw.close();
		
		long end = System.currentTimeMillis();
		log.println(start);
		log.println(end);
		log.println(end-start);
		log.close();
		
		
		System.out.println(jsonString);
//		} catch (Exception e) {
//			PrintWriter asdf = new PrintWriter("errorlog.txt");
//			asdf.println(e.getMessage());
//			long end = System.currentTimeMillis();
//			asdf.println(start);
//			asdf.println(end);
//			asdf.println(end-start);
//			asdf.close();
//		}
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
			
		} else if (data.equals("stack")) {
			StackNode n = new StackNode(parent, data);
			CompoundTerm tt = (CompoundTerm) t;
			n.setThingsToStackNode(getNodeFromData(n, dataFromTerm(tt.args[0]), tt.args[0]));
			return n;
		} else if (data.equals("floor")) {
			return new FloorNode(parent, data);
		} else if (data.equals("any") || data.equals("all") || data.equals("the")) {
			return new QuantifierNode(parent, data);
		} else if (data.equals("object")) {
			ObjectNode n = new ObjectNode(parent, data);
			CompoundTerm tt = (CompoundTerm) t;
			n.setFormNode((AttributeNode)getNodeFromData(n, dataFromTerm(tt.args[0]), tt.args[0]));
			n.setSizeNode((AttributeNode)getNodeFromData(n, dataFromTerm(tt.args[1]), tt.args[1]));
			n.setColorNode((AttributeNode)getNodeFromData(n, dataFromTerm(tt.args[2]), tt.args[2]));
			
	
			
			
			return n;
		} else if (data.equals("under") || data.equals("beside") || data.equals("above") || data.equals("leftof")
				|| data.equals("rightof") || data.equals("ontop") || data.equals("inside")) {
			return new RelationNode(parent, data);
		}
		AttributeNode a=  new AttributeNode(parent, data);
		// add plural forms for different object types. 
		// neccessary for natuaral language generation
		
		if (data.equals("box")) a.setPluralForm("boxes");
		if (data.equals("ball")) a.setPluralForm("balls");
		if (data.equals("plank")) a.setPluralForm("planks");
		if (data.equals("anyform")) a.setPluralForm("objects");
		if (data.equals("pyramid")) a.setPluralForm("pyramids");
		if (data.equals("table")) a.setPluralForm("tables");
		if (data.equals("brick")) a.setPluralForm("bricks");
		return a;
		
		
	
		
		
	}

}
