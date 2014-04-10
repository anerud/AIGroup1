package logic;

import sun.rmi.runtime.Log;
import world.WorldObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A logical expression of arbitrary form of some objects. Note that the LogicalExpressions can be nested as desired.
 *
 * Created by Roland on 2014-03-28.
 */
public class LogicalExpression<T> implements Cloneable{

    private static final int MAXIMUM_CLAUSES = 1000;

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

    /**
     * This method, unlike topObjs, allows for duplicate entries
     * @return
     */
    public List<T> topObjsList() {
        List<T> top = new ArrayList<>();
        if(objs != null){
            top.addAll(objs);
        }
        for(LogicalExpression le : expressions){
            top.addAll(le.topObjsList());
        }
        return top;
    }

    public void removeAll(Set<T> toBeRemoved) {
        if(getObjs() != null){
            getObjs().removeAll(toBeRemoved);
        }
        for(LogicalExpression<T> exp : expressions){
            exp.removeAll(toBeRemoved);
        }
    }

    public boolean isDnf() {
        //TODO: implement
        return true;
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

    /**
     * Simplifies this expression and removes unnecessary operators.
     */
    public LogicalExpression<T> simplifyExpression() throws CloneNotSupportedException {
        if(this.expressions.isEmpty()){
            return this;
        }
        //First simplify all subexpressions
        for(LogicalExpression<T> exp : this.expressions){ //TODO: make sure all subexpressions are either cnf or dnf: not a mixture!
            exp.simplifyExpression();
        }
        //Since the expressions have changed, some of them might be equal now. We therefore readd them to the set to ensure the set properties are still retained.
        Set<LogicalExpression> refreshed = new HashSet<>();
        refreshed.addAll(this.expressions);
        this.expressions = refreshed;

        //Then simplify this expression..
        LogicalExpression<T> currentExp = this;

        //First remove unnecessary leading operators
        if((currentExp.getObjs() == null || currentExp.getObjs().size() == 0) && currentExp.getExpressions().size() <= 1){
            //In this case, we can delete the top operator
            //put them all in the same expression
            currentExp = currentExp.getExpressions().iterator().next();
        }
        //Then remove all empty expressions
        if(!currentExp.getExpressions().isEmpty()){
            Set<LogicalExpression> toBeRemoved = new HashSet<>();
            for(LogicalExpression<T> le : currentExp.getExpressions()){
                if(le.size() == 0){
                    toBeRemoved.add(le);
                }
            }
            currentExp.getExpressions().removeAll(toBeRemoved);
        }
        //Then remove unnecessary leading operators again..
        if((currentExp.getObjs() == null || currentExp.getObjs().size() == 0) && currentExp.getExpressions().size() <= 1){
            currentExp = currentExp.getExpressions().iterator().next();
        }
        //Can we simplify more?
        if(currentExp.getExpressions().isEmpty()){
            this.objs = currentExp.getObjs();
            this.expressions = currentExp.getExpressions();
            this.op = currentExp.getOp();
            return this;
        }

        //Promote all subexpressions which are the same as this expression
        Operator op = currentExp.getOp();
        Set<LogicalExpression> toBeAdded = new HashSet<LogicalExpression>();
        Set<LogicalExpression> toBeRemoved = new HashSet<LogicalExpression>();
        for(LogicalExpression<T> exp : currentExp.getExpressions()){
            if(exp.getOp().equals(op)){
                if(exp.getObjs() != null){
                    if(currentExp.getObjs() == null && !exp.getObjs().isEmpty()){
                        currentExp.setObjs(new HashSet<T>());
                    }
                    currentExp.getObjs().addAll(exp.getObjs());
                }
                toBeAdded.addAll(exp.getExpressions());
                toBeRemoved.add(exp);
            }
        }
        currentExp.getExpressions().removeAll(toBeRemoved);
        currentExp.getExpressions().addAll(toBeAdded);

        //If this is an AND operator, invert the relationship to create a disjunctive normal form (if practical).
        if(currentExp.getOp().equals(Operator.AND)){
            //First see if it is practical to convert..
            int size = currentExp.getObjs() == null ? 1 : currentExp.getObjs().size() == 0 ? 1 : currentExp.getObjs().size();
            for(LogicalExpression<WorldObject> le : currentExp.getExpressions()){
                if(le.getOp().equals(Operator.OR)){
                    size *= le.getExpressions().size() + le.getObjs().size();
                }
            }
            if(size < MAXIMUM_CLAUSES){ //less than 1000 clauses
                Set<LogicalExpression> newExps = new HashSet<LogicalExpression>();
                for(LogicalExpression<T> exp : currentExp.getExpressions()){
                    if(exp.getOp().equals(Operator.OR)){
                        currentExp.setOp(Operator.OR);
                        if(newExps.isEmpty()){
                            //add all expressions without OR to a new LogicalExpression
                            Set<LogicalExpression> lset = new HashSet<LogicalExpression>();
                            for(LogicalExpression<T> le : currentExp.getExpressions()){
                                if(!le.getOp().equals(Operator.OR)){
                                    lset.add(le);
                                }
                            }
                            LogicalExpression<T> toAdd = new LogicalExpression<>(currentExp.getObjs(), lset, Operator.AND);
                            if(!toAdd.isEmpty()){
                                newExps.add(toAdd);
                            }
                        }
                        Set<LogicalExpression> newExpsNew = new HashSet<LogicalExpression>();
                        if(exp.getObjs() != null){
                            for(T t : exp.getObjs()){
                                if(newExps.isEmpty()){
                                    Set<T> ns = new HashSet<T>();
                                    ns.add(t);
                                    newExpsNew.add(new LogicalExpression<T>(ns, Operator.AND));
                                } else {
                                    for(LogicalExpression le : newExps){
                                        LogicalExpression copy = le.clone();
                                        if(copy.getObjs() != null){
                                            copy.getObjs().add(t);
                                        }
                                        newExpsNew.add(new LogicalExpression<T>(copy.getObjs(), copy.getExpressions(), Operator.AND));
                                    }
                                }
                            }
                        }
                        for(LogicalExpression t : exp.getExpressions()){
                            if(newExps.isEmpty()){
                                Set<T> ns = new HashSet<T>();
                                Set<LogicalExpression> nles = new HashSet<>();
                                nles.add(t);
                                newExpsNew.add(new LogicalExpression<T>(ns, nles, Operator.AND));
                            } else {
                                for(LogicalExpression le : newExps){
                                    LogicalExpression copy = le.clone();
                                    copy.getExpressions().add(t);
                                    newExpsNew.add(new LogicalExpression<T>(copy.getObjs(), copy.getExpressions(), Operator.AND));
                                }
                            }
                        }
                        newExps = newExpsNew;
                    }
                }
                if(!newExps.isEmpty()){
                    currentExp.setObjs(null); //All objects were moved to an expression
                    currentExp.setExpressions(newExps);
                }
            }
        }

        //oldTODO: "remove subexpressions which are identical..". This is probably done automatically

        //Can it be further simplified?
//        if(((currentExp.getObjs() == null || currentExp.getObjs().size() == 0) && currentExp.getExpressions().size() <= 1) || same2 && ()){
//            return currentExp.simplifyExpression();
//        }
        this.objs = currentExp.getObjs();
        this.expressions = currentExp.getExpressions();
        this.op = currentExp.getOp();
        return this;
    }


    @Override
    public LogicalExpression<T> clone() throws CloneNotSupportedException {
        LogicalExpression<T> le = (LogicalExpression<T>) super.clone();
        le.setExpressions(new HashSet<LogicalExpression>(le.getExpressions()));
        le.setObjs(new HashSet<T>(le.getObjs()));
        return le;
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
}
