package main;

import logic.LogicalExpression;
import world.RelativeWorldObject;
import world.WorldObject;

/**
 * Wraps a pddl expression Exp. A goal is a logical expression (Exp) which describes the desired relationship between objects in the world.
 */
public class Goal {

    public enum Action{
        MOVE, PUT, TAKE
    }

    private LogicalExpression<WorldObject> expression;

    public Action getAction() {
        return action;
    }

    private Action action;
    private String pddlString;

    public Goal(LogicalExpression<WorldObject> expression, Action action){
        this.expression = expression;
        this.action = action;
    }

    public Goal(String pddlString){
        this.pddlString = pddlString;
    }


    @Override
    public String toString(){
        return toPDDLString();
    }

    public String toPDDLString(){
        if(pddlString != null){
            return pddlString;
        }
        if(action.equals(Action.TAKE)){
            return toPDDLString(expression, "holding ");
        } else {
            return toPDDLString(expression, "");
        }
    }

    /**
     * Recursively builds a PDDL expression from a LogicalExpression
     * @param expression
     * @return an empty string if there is nothing contained in the LogicalExpression
     */
    private String toPDDLString(LogicalExpression<WorldObject> expression, String singlePredicate) {
        StringBuilder pddlString = new StringBuilder();
        if(expression.size() > 1){
            LogicalExpression.Operator op = expression.getOp();
            pddlString.append("(" + op.toString() + " ");
            if(expression.getObjs() != null){
                for(WorldObject obj : expression.getObjs()){
                    if(obj instanceof RelativeWorldObject && ((RelativeWorldObject)obj).getRelativeTo() != null){
                        RelativeWorldObject relObj = (RelativeWorldObject)obj;
                        pddlString.append("(" + relObj.getRelation().toString() + " " + relObj.getId() + " " + relObj.getRelativeTo().getId() + ") ");
                    } else {
                        if(singlePredicate.equals("")){
                            pddlString.append(obj.getId());
                        } else {
                            pddlString.append("(" + singlePredicate + obj.getId() + ") ");}

                    }
                }
            }
            for(LogicalExpression exp : expression.getExpressions()){
                pddlString.append(toPDDLString(exp, singlePredicate) + " "); //Recursively build string
            }
            //Remove last extra space
            pddlString.deleteCharAt(pddlString.length() - 1);
            pddlString.append(")");
        } else if (expression.size() == 1) {
            //Assume the RelativeWorldobject in question is at the top level of the expression
            WorldObject wo = expression.getObjs().iterator().next();
            if(wo instanceof RelativeWorldObject && ((RelativeWorldObject)wo).getRelativeTo() != null){
                RelativeWorldObject relObj = (RelativeWorldObject)wo;
                pddlString.append("(" + relObj.getRelation().toString() + " " + relObj.getId() +" " + relObj.getRelativeTo().getId() + ")");
            } else {
                if(singlePredicate.equals("")){
                    pddlString.append(wo.getId());
                } else {
                    pddlString.append("(" + singlePredicate + wo.getId() + ")");
                }
            }
        }
        return pddlString.toString();
    }


    public LogicalExpression<WorldObject> getExpression() {
        return expression;
    }

    public void setExpression(LogicalExpression<WorldObject> expression) {
        this.expression = expression;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Goal goal = (Goal) o;

        if (action != goal.action) return false;
        if (expression != null ? !expression.equals(goal.expression) : goal.expression != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = expression != null ? expression.hashCode() : 0;
        result = 31 * result + (action != null ? action.hashCode() : 0);
        return result;
    }
}