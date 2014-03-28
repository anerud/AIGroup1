package logic;

import world.WorldObject;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Roland on 2014-03-28.
 */
public class LogicalObject<T> {

    public static <A> Set<LogicalObject<A>> toLogicalObjects(Set<A> objs, Quantifier q) {
        Set<LogicalObject<A>> logObjs = new HashSet<LogicalObject<A>>();
        for(A obj : objs){
            switch(q){
                case THE:
                    logObjs.add(new LogicalObject<A>(obj, Operator.NONE));
                    break;
                case ALL:
                    logObjs.add(new LogicalObject<A>(obj, Operator.AND));
                    break;
                case ANY:
                    logObjs.add(new LogicalObject<A>(obj, Operator.OR));
                    break;
            }
        }
        return logObjs;
    }

    public enum Operator{
        OR, AND, NOT, NONE
    }

    private T obj;
    private Operator op;

    public LogicalObject(T obj, LogicalObject.Operator op){
        this.obj = obj;
        this.op = op;
    }

    public T getObj() {
        return obj;
    }

    public void setObj(T obj) {
        this.obj = obj;
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

        LogicalObject that = (LogicalObject) o;

        if (obj != null ? !obj.equals(that.obj) : that.obj != null) return false;
        if (op != that.op) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = obj != null ? obj.hashCode() : 0;
        result = 31 * result + (op != null ? op.hashCode() : 0);
        return result;
    }
}
