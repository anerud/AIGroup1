package logic;

import world.WorldObject;

import java.beans.Expression;
import java.util.HashSet;
import java.util.Set;

/**
 * A logical expression of arbitrary form of some objects. Note that the LogicalExpressions can be nested as desired.
 *
 * Created by Roland on 2014-03-28.
 */
public class LogicalExpression<T> implements Cloneable{

    /**
     *
     * @return the total number of objects in this expressions
     */
    public int size() {
        int expSize = 0;
        for(LogicalExpression exp : expressions){
            expSize += exp.size();
        }
        return (objs == null ? 0 : objs.size()) + expSize;
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    /**
     *
     * @return all objects on the top level of this expression, that is all objects in getObj() and all objects in the expressions of getExpressions
     */
    public Set<T> topObjs() {
        Set<T> top = new HashSet<T>();
        if(objs != null){
            top.addAll(objs);
        }
        for(LogicalExpression le : expressions){
            top.addAll(le.topObjs());
        }
        return top;
    }

    public enum Operator{
        OR, AND, NOT, NONE
    }

    private Set<T> objs;
    private Set<LogicalExpression> expressions;
    private Operator op;

    public LogicalExpression(Set<T> objs, LogicalExpression.Operator op){
        this.objs = objs;
        this.expressions = new HashSet<LogicalExpression>();
        this.op = op;
    }

    public LogicalExpression(Set<T> objs, Set<LogicalExpression> expressions, LogicalExpression.Operator op){
        this.objs = objs;
        this.expressions = expressions;
        this.op = op;
    }

    public Set<T> getObjs() {
        return objs;
    }

    public void setObjs(Set<T> objs) {
        this.objs = objs;
    }

    public Set<LogicalExpression> getExpressions() {
        return expressions;
    }

    public void setExpressions(Set<LogicalExpression> expressions) {
        this.expressions = expressions;
    }

    public Operator getOp() {
        return op;
    }

    public void setOp(Operator op) {
        this.op = op;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LogicalExpression that = (LogicalExpression) o;

        if (expressions != null ? !expressions.equals(that.expressions) : that.expressions != null) return false;
        if (objs != null ? !objs.equals(that.objs) : that.objs != null) return false;
        if (op != that.op) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = objs != null ? objs.hashCode() : 0;
        result = 31 * result + (expressions != null ? expressions.hashCode() : 0);
        result = 31 * result + (op != null ? op.hashCode() : 0);
        return result;
    }

    /**
     * Simplifies this expression and removes unnecessary operators.
     * Use the returned expression, as there is no guarantee for the state of this expression after calling this method. //TODO change so that this is not the case
     */
    public LogicalExpression<T> simplifyExpression(){
        if(expressions.isEmpty()){
            return this;
        }
        //First simplify all subexpressions
        for(LogicalExpression<T> exp : expressions){
            exp.simplifyExpression();
        }
        //Then simplify this expression..

        //Do all expressions have the same operator?
        Operator op = expressions.iterator().next().getOp();
        boolean same = true;
        for(LogicalExpression<T> exp : expressions){
            if(exp.getOp() != op){
                same = false;
            }
        }
        LogicalExpression<T> unified = null;
        //Is the operator of this expression also the same as the unified expression?
        if(same && (op.equals(getOp()) || getOp().equals(Operator.NONE))){
            //put them all in the same expression
            Set<T> objs = new HashSet<T>();
            Set<LogicalExpression> exps = new HashSet<LogicalExpression>();
            for(LogicalExpression<T> exp : expressions){
                objs.addAll(exp.getObjs());
                exps.addAll(exp.getExpressions());
            }
            unified = new LogicalExpression<T>(objs, exps, op);

            //merge this expression with the subexpression
            if(getObjs() != null){
                unified.getObjs().addAll(getObjs());
            }
            return unified;
        }
        return this;
    }

}
