package tree;

import logic.Tense;
import main.Interpreter;

import java.util.LinkedList;

/**
 * Created by Roland on 2014-05-12.
 */
public class TenseNode extends Node{

    public Tense getTense() {
        return tense;
    }

    private final Tense tense;

    public TenseNode(Node parent, String data) {
        super(parent, data);
        this.tense = Tense.valueOf(data.toUpperCase());
    }

    @Override
    public <R, A, A2> R accept(INodeVisitor<R, A, A2> v, A arg, A2 arg2) throws Interpreter.InterpretationException {
        return v.visit(this, arg, arg2);
    }

    @Override
    public LinkedList<Node> getChildren() {
        return new LinkedList<Node>();
    }

    @Override
    public <R, A> R accept(IActionVisitor<R, A> v, A arg) {
        return null;
    }

}
