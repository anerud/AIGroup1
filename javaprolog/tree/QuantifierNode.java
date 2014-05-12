package tree;

import logic.Quantifier;
import main.Interpreter;

import java.util.LinkedList;

public class QuantifierNode extends Node{
	public boolean IsPlural()
	{
		return (quantifier==Quantifier.ALL);
	}

    public Quantifier getQuantifier() {
        return quantifier;
    }

    private final Quantifier quantifier;

    public QuantifierNode(Node parent, String data) {
		super(parent, data);
        this.quantifier = Quantifier.valueOf(data.toUpperCase());
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
