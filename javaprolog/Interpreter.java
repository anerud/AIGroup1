import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.*;

<<<<<<< HEAD
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import tree.NTree;
import tree.Node;
import world.World;
import world.WorldObject;

=======
>>>>>>> 126d9050c15383146a7a3ea325a112f6a4cc63a2

public class Interpreter {

    private World world;

    public Interpreter(World world) {
        this.world = world;
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
                if(desiredObjs.size() >= 1){
                    Goal goal =  new Goal(pddlString.toString()); //TODO new Goal(some Exp..);
                    goals.add(goal);
                }
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
            if(args.get(0).getData().equals("the") && toBeFiltered.size() > 1) {
                //TODO return error message to GUI
            }// Else quantifier any, so everything is ok..
        } else if(str.equals("floor")){
            toBeFiltered.clear();
            toBeFiltered.add(new WorldObject("floor", "floor", "floor"));
        } else if(str.equals("relative_entity")){ //Here, we first filter the objects depending on the first argument object, then move on to the recursion..
            if(args.get(1).getData().equals("object")){
                //Leaf.. now simply find the unique object in the world which matches the description. If multiple, it can still be filtered below..
                List<Node> objArs = args.get(1).getChildren();
                Node objectType = objArs.get(0);
                Node size = objArs.get(0);
                Node color = objArs.get(0);
                filterByMatch(toBeFiltered, new WorldObject(objArs.get(0).getData(), objArs.get(1).getData(), objArs.get(2).getData()));
            } else{
                //..? above is prob. always satisfied.
            }
            if(args.get(0).getData().equals("the") && toBeFiltered.size() > 1) {
                //TODO return error message to GUI
            }// Else quantifier any, so everything is ok..

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
        }
        //Note that the "relative" keyword is never encountered here
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
}
