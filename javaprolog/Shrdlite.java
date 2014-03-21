
// First compile the program:
// javac -cp gnuprologjava-0.2.6.jar:json-simple-1.1.1.jar:. Shrdlite.java

// Then test from the command line:
// java -cp gnuprologjava-0.2.6.jar:json-simple-1.1.1.jar:. Shrdlite < ../examples/medium.json

import gnu.prolog.term.Term;
import gnu.prolog.vm.PrologException;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

public class Shrdlite {

	public static void main(String[] args) throws PrologException, ParseException, IOException {
        JSONObject jsinput = null;
        if(args.length == 0){
            jsinput   = (JSONObject) JSONValue.parse(readFromReader(new InputStreamReader(System.in)));
        } else {
            jsinput = (JSONObject) JSONValue.parse(readFromReader(new FileReader(args[0])));
        }
        JSONArray  utterance = (JSONArray)  jsinput.get("utterance");
        JSONArray  world     = (JSONArray)  jsinput.get("world");
        String     holding   = (String)     jsinput.get("holding");
        JSONObject objects   = (JSONObject) jsinput.get("objects");
        
        PrintWriter log = new PrintWriter("Log.txt", "UTF-8");
        
        JSONObject result = new JSONObject();
        result.put("utterance", utterance);

         // This is how to get information about the top object in column 1:
//         JSONArray column = (JSONArray) world.get(1);
//         String topobject = (String) column.get(column.size() - 1);
//         JSONObject objectinfo = (JSONObject) objects.get(topobject);
//         String form = (String) objectinfo.get("form");
//         log.println(topobject);
//         log.println(objectinfo.toString());
//         log.println(form);

        DCGParser parser = new DCGParser("shrdlite_grammar.pl");
        List<Term> trees = parser.parseSentence("command", utterance);
        List<String> tstrs = new ArrayList<String>();
        result.put("trees", tstrs);
        for (Term t : trees) {
            tstrs.add(t.toString());
            log.println(t.toString());
        }

        if (trees.isEmpty()) {
            result.put("output", "Parse error!");
        } else {
            List<Goal> goals = new ArrayList<Goal>();
            Interpreter interpreter = new Interpreter(world, holding, objects);
            for (String tree : tstrs) {
                 for (Goal goal : interpreter.interpret(tree)) {
                     goals.add(goal);
                 }
            }
            result.put("goals", goals);

            if (goals.isEmpty()) {
                result.put("output", "Interpretation error!");

            } else if (goals.size() > 1) {
                result.put("output", "Ambiguity error!");

            } else {
                Planner planner = new Planner(world, holding, objects);
                List<String> plan = planner.solve(goals.get(0));
                
                result.put("plan", plan);

                if(plan.isEmpty()) {
                    result.put("output", "Planning error!");
                } else {
                    result.put("output", "Success!");
                }
            }
        }

        System.out.print(result);

        //Also print the result to a file
        FileWriter fw = new FileWriter("./result.json");
        String pretty = com.cedarsoftware.util.io.JsonWriter.formatJson(result.toJSONString());
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

}

